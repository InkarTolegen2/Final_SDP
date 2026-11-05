package restaurant.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import restaurant.facade.RestaurantFacade;
import restaurant.facade.RestaurantFacade.ItemRequest;
import restaurant.facade.RestaurantFacade.LineItem;
import restaurant.facade.RestaurantFacade.OrderRequest;
import restaurant.facade.RestaurantFacade.PlacedOrder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Routes {
    private static final Pattern STATUS_PATH = Pattern.compile("^/order/(\\d+)/status$");

    private final RestaurantFacade facade;
    private final Path frontendDir;

    public Routes(RestaurantFacade facade, Path frontendDir) {
        this.facade = facade;
        this.frontendDir = frontendDir.toAbsolutePath().normalize();
    }

    public HttpServer start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handle);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        return server;
    }

    private void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();
        try {
            Matcher status = STATUS_PATH.matcher(path);
            if (method.equals("GET") && path.equals("/menu")) {
                json(ex, 200, menuJson());
            } else if (method.equals("POST") && path.equals("/order")) {
                PlacedOrder p = facade.placeOrder(parseOrder(body(ex)));
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", p.order().getId());
                m.put("status", p.order().getStatus());
                m.put("total", p.total());
                m.put("finalTotal", p.finalTotal());
                m.put("discountName", p.discountName());
                json(ex, 200, m);
            } else if (method.equals("GET") && path.equals("/orders")) {
                List<Object> list = new ArrayList<>();
                for (PlacedOrder p : facade.orders()) list.add(orderJson(p));
                json(ex, 200, list);
            } else if (status.matches() && method.equals("GET")) {
                json(ex, 200, statusJson(facade.order(Integer.parseInt(status.group(1)))));
            } else if (status.matches() && method.equals("POST")) {
                Object b = Json.parse(body(ex));
                String st = b instanceof Map<?, ?> m && m.get("status") != null ? String.valueOf(m.get("status")) : null;
                json(ex, 200, statusJson(facade.changeStatus(Integer.parseInt(status.group(1)), st)));
            } else if (method.equals("GET")) {
                serveFile(ex, path);
            } else {
                text(ex, 405, "Method not allowed");
            }
        } catch (NoSuchElementException e) {
            text(ex, 404, e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            text(ex, 400, e.getMessage() == null ? "Bad request" : e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            text(ex, 500, "Server error");
        }
    }

    @SuppressWarnings("unchecked")
    private OrderRequest parseOrder(String body) {
        Object parsed = Json.parse(body);
        if (!(parsed instanceof Map)) throw new IllegalArgumentException("Invalid JSON");
        Map<String, Object> m = (Map<String, Object>) parsed;

        List<ItemRequest> items = new ArrayList<>();
        if (m.get("items") instanceof List<?> list) {
            for (Object o : list) {
                if (!(o instanceof Map)) throw new IllegalArgumentException("Invalid item");
                Map<String, Object> it = (Map<String, Object>) o;
                List<String> toppings = new ArrayList<>();
                if (it.get("toppings") instanceof List<?> tl) for (Object t : tl) toppings.add(String.valueOf(t));
                items.add(new ItemRequest(str(it.get("type")), toppings, intOf(it.get("qty"), 1)));
            }
        }
        String discountType = null, discountValue = null;
        if (m.get("discount") instanceof Map<?, ?> d) {
            discountType = str(d.get("type"));
            discountValue = str(d.get("value"));
        }
        return new OrderRequest(intOf(m.get("table"), 0), m.get("comment") == null ? "" : str(m.get("comment")),
                str(m.get("payment")), discountType, discountValue, items);
    }

    private static String str(Object o) {
        if (o == null) return null;
        if (o instanceof Double d && d == Math.rint(d)) return String.valueOf(d.longValue());
        return String.valueOf(o);
    }

    private static int intOf(Object o, int fallback) {
        return o instanceof Number n ? n.intValue() : fallback;
    }

    private Map<String, Object> menuJson() {
        RestaurantFacade.Menu menu = facade.menu();
        List<Object> meals = new ArrayList<>();
        for (var m : menu.meals()) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("type", m.type()); o.put("name", m.name());
            o.put("description", m.description()); o.put("price", m.price());
            meals.add(o);
        }
        List<Object> toppings = new ArrayList<>();
        for (var t : menu.toppings()) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("id", t.id()); o.put("name", t.name()); o.put("label", t.label()); o.put("price", t.price());
            toppings.add(o);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("meals", meals);
        result.put("toppings", toppings);
        return result;
    }

    private Map<String, Object> statusJson(PlacedOrder p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.order().getId());
        m.put("status", p.order().getStatus());
        return m;
    }

    private Map<String, Object> orderJson(PlacedOrder p) {
        List<Object> items = new ArrayList<>();
        for (LineItem li : p.items()) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("type", li.type()); o.put("description", li.description());
            o.put("qty", li.qty()); o.put("unitPrice", li.unitPrice());
            items.add(o);
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.order().getId());
        m.put("table", p.order().getTableNumber());
        m.put("comment", p.order().getComment());
        m.put("payment", p.payment());
        m.put("items", items);
        m.put("total", p.total());
        m.put("finalTotal", p.finalTotal());
        m.put("discountName", p.discountName());
        m.put("status", p.order().getStatus());
        m.put("createdAt", p.createdAt());
        return m;
    }

    private void serveFile(HttpExchange ex, String path) throws IOException {
        String rel = path.equals("/") ? "index.html" : path.substring(1);
        Path file = frontendDir.resolve(rel).normalize();
        if (!file.startsWith(frontendDir) || !Files.isRegularFile(file)) {
            text(ex, 404, "Not found");
            return;
        }
        String name = file.getFileName().toString();
        String type = name.endsWith(".html") ? "text/html; charset=utf-8"
                : name.endsWith(".css") ? "text/css; charset=utf-8"
                : name.endsWith(".js") ? "application/javascript; charset=utf-8"
                : name.endsWith(".png") ? "image/png"
                : name.endsWith(".svg") ? "image/svg+xml"
                : "application/octet-stream";
        send(ex, 200, type, Files.readAllBytes(file));
    }

    // ---------- helpers ----------
    private static String body(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void json(HttpExchange ex, int code, Object o) throws IOException {
        send(ex, code, "application/json; charset=utf-8", Json.write(o).getBytes(StandardCharsets.UTF_8));
    }

    private static void text(HttpExchange ex, int code, String msg) throws IOException {
        send(ex, code, "text/plain; charset=utf-8", msg.getBytes(StandardCharsets.UTF_8));
    }

    private static void send(HttpExchange ex, int code, String type, byte[] data) throws IOException {
        ex.getResponseHeaders().set("Content-Type", type);
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(code, data.length);
        ex.getResponseBody().write(data);
        ex.close();
    }
}

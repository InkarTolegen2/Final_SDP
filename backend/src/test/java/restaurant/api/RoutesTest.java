package restaurant.api;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import restaurant.facade.RestaurantFacade;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Starts the real HTTP server on a free port and calls the REST API like the browser does. */
class RoutesTest {

    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> call(int port, String method, String path, String body) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        b = body == null ? b.method(method, HttpRequest.BodyPublishers.noBody())
                : b.method(method, HttpRequest.BodyPublishers.ofString(body)).header("Content-Type", "application/json");
        return client.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void fullOrderFlowOverHttp() throws Exception {
        Path front = Files.createTempDirectory("front");
        Files.writeString(front.resolve("index.html"), "<h1>menu</h1>");
        HttpServer server = new Routes(new RestaurantFacade(), front).start(0);
        int port = server.getAddress().getPort();
        try {
            // static files
            HttpResponse<String> page = call(port, "GET", "/", null);
            assertEquals(200, page.statusCode());
            assertTrue(page.body().contains("menu"));
            assertEquals(404, call(port, "GET", "/../secret.txt", null).statusCode());

            // menu
            HttpResponse<String> menu = call(port, "GET", "/menu", null);
            assertEquals(200, menu.statusCode());
            assertTrue(menu.headers().firstValue("Content-Type").orElse("").contains("json"));
            Map<?, ?> m = (Map<?, ?>) Json.parse(menu.body());
            assertEquals(3, ((List<?>) m.get("meals")).size());

            // place order (same JSON the cart page sends)
            String order = "{\"table\":5,\"comment\":\"no onion\",\"payment\":\"cash\","
                    + "\"discount\":{\"type\":\"promo\",\"value\":\"FIRST\"},"
                    + "\"items\":[{\"type\":\"pizza\",\"toppings\":[\"cheese\",\"large\"],\"qty\":2,"
                    + "\"description\":\"ignored by the server\"}]}";
            HttpResponse<String> placed = call(port, "POST", "/order", order);
            assertEquals(200, placed.statusCode());
            Map<?, ?> p = (Map<?, ?>) Json.parse(placed.body());
            assertEquals(6600.0, p.get("total"));
            assertEquals(5610.0, p.get("finalTotal"));
            assertEquals("NEW", p.get("status"));
            int id = ((Number) p.get("id")).intValue();

            // kitchen list
            List<?> orders = (List<?>) Json.parse(call(port, "GET", "/orders", null).body());
            assertEquals(1, orders.size());

            // status changes
            assertEquals(200, call(port, "POST", "/order/" + id + "/status", "{\"status\":\"COOKING\"}").statusCode());
            Map<?, ?> st = (Map<?, ?>) Json.parse(call(port, "GET", "/order/" + id + "/status", null).body());
            assertEquals("COOKING", st.get("status"));
            assertEquals(400, call(port, "POST", "/order/" + id + "/status", "{\"status\":\"NEW\"}").statusCode());

            // errors
            assertEquals(404, call(port, "GET", "/order/999/status", null).statusCode());
            assertEquals(400, call(port, "POST", "/order", "{\"table\":5,\"items\":[]}").statusCode());
            assertEquals(400, call(port, "POST", "/order", "not json").statusCode());
        } finally {
            server.stop(0);
        }
    }
}

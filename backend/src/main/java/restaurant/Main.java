package restaurant;

import restaurant.api.Routes;
import restaurant.facade.RestaurantFacade;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Starts the web server: REST API + frontend pages on one port.
 * Open http://localhost:7070 after start.
 * Console demos of the patterns: meal.MealDemo, order.OrderDemo, payment.PaymentDemo.
 */
public class Main {
    public static final int DEFAULT_PORT = 7070;

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        Path frontend = findFrontend();
        if (frontend == null) {
            System.err.println("Cannot find the frontend/ folder (with index.html). "
                    + "Run the server from the repository root or from backend/.");
            return;
        }

        RestaurantFacade facade = new RestaurantFacade();
        new Routes(facade, frontend).start(port);

        System.out.println("Frontend: " + frontend);
        System.out.println("Restaurant is open: http://localhost:" + port);
        System.out.println("Kitchen panel:      http://localhost:" + port + "/kitchen.html");
    }

    /** IntelliJ / mvn start the program either in the repo root or in backend/. */
    private static Path findFrontend() {
        for (String candidate : new String[] {"frontend", "../frontend"}) {
            Path p = Path.of(candidate).toAbsolutePath().normalize();
            if (Files.isRegularFile(p.resolve("index.html"))) {
                return p;
            }
        }
        return null;
    }
}

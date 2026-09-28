package co.edu.escuelaing.app;

import static co.edu.escuelaing.webframework.WebFramework.get;
import static co.edu.escuelaing.webframework.WebFramework.staticfiles;
import static co.edu.escuelaing.webframework.WebFramework.start;
import static co.edu.escuelaing.webframework.WebFramework.stop;

/**
 * Example application built on top of the webframework: it only registers static resources
 * and a handful of GET routes through lambdas. It never touches sockets or the connection loop.
 */
public class Application {

    public static void main(String[] args) throws Exception {

        staticfiles("/webroot");

        get("/hello", (req, resp) -> {
            String name = req.getValue("name");
            if (name == null || name.isBlank()) {
                name = "world";
            }

            String greetingPrefix = System.getenv().getOrDefault("GREETING_PREFIX", "Hello");
            return greetingPrefix + " " + name;
        });

        get("/pi", (req, resp) -> String.valueOf(Math.PI));

        get("/square", (req, resp) -> {
            String numberValue = req.getValue("number");
            if (numberValue == null || numberValue.isBlank()) {
                resp.setStatus("400 Bad Request");
                resp.setContentType("application/json; charset=UTF-8");
                return "{\"error\":\"number is required\"}";
            }
            try {
                double number = Double.parseDouble(numberValue);
                resp.setContentType("application/json; charset=UTF-8");
                return "{\"number\":" + number + ",\"square\":" + (number * number) + "}";
            } catch (NumberFormatException e) {
                resp.setStatus("400 Bad Request");
                resp.setContentType("application/json; charset=UTF-8");
                return "{\"error\":\"invalid number format\"}";
            }
        });

        get("/server-time", (req, resp) -> {
            resp.setContentType("application/json; charset=UTF-8");
            return "{\"serverTime\":\"" + java.time.Instant.now() + "\"}";
        });

        String environment = System.getenv().getOrDefault("APP_ENV", "development");
        if (environment.equals("development")) {
            get("/shutdown", (req, resp) -> {
                stop();
                return "Server will stop after this response.";
            });
        }

        start();
    }
}

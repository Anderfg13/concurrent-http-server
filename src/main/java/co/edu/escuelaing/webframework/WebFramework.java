package co.edu.escuelaing.webframework;

import java.io.IOException;

/**
 * Public facade of the application server. This is the only class an application developer
 * needs to import (statically) to register static files, GET routes, and to start or stop
 * the server:
 *
 * <pre>
 * import static co.edu.escuelaing.webframework.WebFramework.*;
 *
 * staticfiles("/webroot");
 * get("/hello", (req, resp) -&gt; "Hello " + req.getValue("name"));
 * start();
 * </pre>
 */
public class WebFramework {

    private static final Router router = new Router();
    private static final StaticFileService staticFileService = new StaticFileService();

    private WebFramework() {
    }

    /**
     * Configures where static resources are served from (a classpath folder under
     * src/main/resources, e.g. "/webroot"). Overridden at runtime by the
     * STATIC_FILES_PATH environment variable, which serves from a filesystem folder instead.
     */
    public static void staticfiles(String root) {
        staticFileService.setRoot(root);
    }

    /**
     * Registers a lambda that handles GET requests to the given path.
     */
    public static void get(String path, Service service) {
        router.addGetRoute(path, service);
    }

    /**
     * Starts the server on the port given by the PORT environment variable,
     * defaulting to 8080 for local execution.
     */
    public static void start() throws IOException {
        start(resolvePort());
    }

    public static void start(int port) throws IOException {
        HttpServer.start(port, router, staticFileService);
    }

    /**
     * Requests a graceful shutdown: the server finishes the current request and response,
     * then exits its main loop instead of accepting further connections.
     */
    public static void stop() {
        HttpServer.stop();
    }

    private static int resolvePort() {
        String portValue = System.getenv("PORT");
        return (portValue == null || portValue.isBlank()) ? 8080 : Integer.parseInt(portValue);
    }
}

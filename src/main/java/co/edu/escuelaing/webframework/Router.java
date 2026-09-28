package co.edu.escuelaing.webframework;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps an HTTP method + path to the lambda that should handle it.
 * The connection-processing loop never changes when a new route is registered here.
 */
public class Router {

    private final Map<String, Service> getRoutes = new HashMap<>();

    public void addGetRoute(String path, Service service) {
        getRoutes.put(path, service);
    }

    /**
     * Returns the registered handler for this method + path, or null if there isn't one
     * (the caller should then fall back to static-file resolution).
     */
    public Service resolve(String method, String path) {
        if (!"GET".equalsIgnoreCase(method)) {
            return null;
        }
        return getRoutes.get(path);
    }
}

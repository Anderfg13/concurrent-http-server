package co.edu.escuelaing.webframework;

import java.util.Collections;
import java.util.Map;

/**
 * Represents an incoming HTTP GET request: its path and its decoded query-string parameters.
 */
public class Request {

    private final String method;
    private final String path;
    private final Map<String, String> queryParams;

    public Request(String method, String path, Map<String, String> queryParams) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
    }

    /**
     * Returns the value of a query-string parameter, or null if it was not sent.
     */
    public String getValue(String name) {
        return queryParams.get(name);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }
}

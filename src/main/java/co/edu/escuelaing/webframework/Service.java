package co.edu.escuelaing.webframework;

/**
 * A GET route handler registered through {@link WebFramework#get(String, Service)}.
 */
@FunctionalInterface
public interface Service {

    String handle(Request request, Response response);
}

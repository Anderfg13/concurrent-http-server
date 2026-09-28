package co.edu.escuelaing.webframework;

/**
 * Lets a route handler customize the HTTP status and content type of its response.
 * Defaults to "200 OK" / "text/plain; charset=UTF-8" when a lambda leaves it untouched.
 */
public class Response {

    private String status = "200 OK";
    private String contentType = "text/plain; charset=UTF-8";

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }
}

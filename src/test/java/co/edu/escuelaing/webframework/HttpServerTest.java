package co.edu.escuelaing.webframework;

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HttpServerTest {

    @Test
    public void extractRoutePath_noQueryString() {
        assertEquals("/hello", HttpServer.extractRoutePath("/hello"));
    }

    @Test
    public void extractRoutePath_withQueryString() {
        assertEquals("/hello", HttpServer.extractRoutePath("/hello?name=Pedro"));
    }

    @Test
    public void extractQueryString_noQueryString() {
        assertNull(HttpServer.extractQueryString("/hello"));
    }

    @Test
    public void extractQueryString_withQueryString() {
        assertEquals("name=Pedro", HttpServer.extractQueryString("/hello?name=Pedro"));
    }

    @Test
    public void extractQueryParams_null_returnsEmptyMap() {
        assertTrue(HttpServer.extractQueryParams(null).isEmpty());
    }

    @Test
    public void extractQueryParams_singleParam() {
        Map<String, String> params = HttpServer.extractQueryParams("name=Pedro");
        assertEquals("Pedro", params.get("name"));
    }

    @Test
    public void extractQueryParams_multipleParams() {
        Map<String, String> params = HttpServer.extractQueryParams("name=Pedro&language=en");
        assertEquals("Pedro", params.get("name"));
        assertEquals("en", params.get("language"));
    }

    @Test
    public void extractQueryParams_missingParamDoesNotFail() {
        Map<String, String> params = HttpServer.extractQueryParams("name=Pedro");
        assertNull(params.get("language"));
    }

    @Test
    public void extractQueryParams_encodedValue() {
        Map<String, String> params = HttpServer.extractQueryParams("name=Anderson%20Garcia");
        assertEquals("Anderson Garcia", params.get("name"));
    }

    @Test
    public void buildHeader_containsStatusAndContentLength() {
        String header = HttpServer.buildHeader("200 OK", "text/plain; charset=UTF-8", 5);
        assertTrue(header.startsWith("HTTP/1.1 200 OK\r\n"));
        assertTrue(header.contains("Content-Type: text/plain; charset=UTF-8\r\n"));
        assertTrue(header.contains("Content-Length: 5\r\n"));
    }
}

package co.edu.escuelaing.webframework;

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RequestTest {

    @Test
    public void getValue_returnsParameterValue() {
        Request request = new Request("GET", "/hello", Map.of("name", "Pedro"));
        assertEquals("Pedro", request.getValue("name"));
    }

    @Test
    public void getValue_missingParameter_returnsNull() {
        Request request = new Request("GET", "/hello", Map.of("name", "Pedro"));
        assertNull(request.getValue("language"));
    }
}

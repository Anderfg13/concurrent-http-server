package co.edu.escuelaing.webframework;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RouterTest {

    @Test
    public void resolve_returnsRegisteredHandler() {
        Router router = new Router();
        router.addGetRoute("/hello", (req, resp) -> "Hello");

        Service resolved = router.resolve("GET", "/hello");

        assertNotNull(resolved);
        assertEquals("Hello", resolved.handle(null, null));
    }

    @Test
    public void resolve_unregisteredRoute_returnsNull() {
        Router router = new Router();
        assertNull(router.resolve("GET", "/unknown"));
    }

    @Test
    public void resolve_nonGetMethod_returnsNull() {
        Router router = new Router();
        router.addGetRoute("/hello", (req, resp) -> "Hello");
        assertNull(router.resolve("POST", "/hello"));
    }
}

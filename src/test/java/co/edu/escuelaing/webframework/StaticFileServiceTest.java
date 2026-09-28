package co.edu.escuelaing.webframework;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StaticFileServiceTest {

    @Test
    public void resolve_existingClasspathResource() throws IOException {
        StaticFileService service = new StaticFileService();
        service.setRoot("/webroot");

        StaticFileService.StaticResource resource = service.resolve("/index.html");

        assertNotNull(resource);
        assertEquals("text/html; charset=UTF-8", resource.getContentType());
        assertTrue(resource.getContent().length > 0);
    }

    @Test
    public void resolve_missingResource_returnsNull() throws IOException {
        StaticFileService service = new StaticFileService();
        service.setRoot("/webroot");

        assertNull(service.resolve("/does-not-exist.html"));
    }

    @Test
    public void resolve_pathTraversalAttempt_returnsNull() throws IOException {
        StaticFileService service = new StaticFileService();
        service.setRoot("/webroot");

        assertNull(service.resolve("/../pom.xml"));
    }

    @Test
    public void contentTypeFor_knownExtensions() {
        assertEquals("text/html; charset=UTF-8", StaticFileService.contentTypeFor("/index.html"));
        assertEquals("text/css; charset=UTF-8", StaticFileService.contentTypeFor("/styles.css"));
        assertEquals("text/javascript; charset=UTF-8", StaticFileService.contentTypeFor("/app.js"));
        assertEquals("image/png", StaticFileService.contentTypeFor("/images/logo.png"));
    }

    @Test
    public void contentTypeFor_unknownExtension_returnsOctetStream() {
        assertEquals("application/octet-stream", StaticFileService.contentTypeFor("/file.xyz"));
    }
}

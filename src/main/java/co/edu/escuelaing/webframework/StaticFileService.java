package co.edu.escuelaing.webframework;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves static resources (HTML, CSS, JS, images) when no dynamic route matches a request.
 *
 * <p>By default resources are read from the classpath under the root configured with
 * {@link WebFramework#staticfiles(String)} (e.g. {@code /webroot}, packaged inside the jar
 * from {@code src/main/resources}). Setting the {@code STATIC_FILES_PATH} environment variable
 * overrides this and serves instead from that folder on the filesystem, which is useful when
 * static resources need to be swapped without rebuilding the jar.</p>
 */
public class StaticFileService {

    private String classpathRoot = "/webroot";

    public void setRoot(String root) {
        this.classpathRoot = root.startsWith("/") ? root : "/" + root;
    }

    public StaticResource resolve(String requestPath) throws IOException {
        if (requestPath.contains("..")) {
            return null;
        }

        String externalPath = System.getenv("STATIC_FILES_PATH");
        if (externalPath != null && !externalPath.isBlank()) {
            return resolveFromFilesystem(externalPath, requestPath);
        }
        return resolveFromClasspath(requestPath);
    }

    private StaticResource resolveFromFilesystem(String externalPath, String requestPath) throws IOException {
        Path basePath = Paths.get(externalPath).toAbsolutePath().normalize();
        Path resolved = basePath.resolve(requestPath.substring(1)).normalize();

        if (!resolved.startsWith(basePath) || !Files.exists(resolved) || Files.isDirectory(resolved)) {
            return null;
        }

        byte[] content = Files.readAllBytes(resolved);
        return new StaticResource(content, contentTypeFor(requestPath));
    }

    private StaticResource resolveFromClasspath(String requestPath) throws IOException {
        String resourcePath = classpathRoot + requestPath;
        try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return null;
            }
            byte[] content = stream.readAllBytes();
            return new StaticResource(content, contentTypeFor(requestPath));
        }
    }

    public static String contentTypeFor(String path) {
        int dotIndex = path.lastIndexOf('.');
        String extension = dotIndex == -1 ? "" : path.substring(dotIndex + 1).toLowerCase();
        switch (extension) {
            case "html":
                return "text/html; charset=UTF-8";
            case "css":
                return "text/css; charset=UTF-8";
            case "js":
                return "text/javascript; charset=UTF-8";
            case "json":
                return "application/json; charset=UTF-8";
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            case "svg":
                return "image/svg+xml";
            case "ico":
                return "image/x-icon";
            default:
                return "application/octet-stream";
        }
    }

    public static class StaticResource {

        private final byte[] content;
        private final String contentType;

        public StaticResource(byte[] content, String contentType) {
            this.content = content;
            this.contentType = contentType;
        }

        public byte[] getContent() {
            return content;
        }

        public String getContentType() {
            return contentType;
        }
    }
}

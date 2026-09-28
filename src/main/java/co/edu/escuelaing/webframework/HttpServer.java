package co.edu.escuelaing.webframework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Accepts connections, parses HTTP requests, and writes HTTP responses.
 *
 * <p>The server is intentionally sequential: it fully processes one connection (accept, read,
 * dispatch, write, close) before calling {@code accept()} again. This class knows nothing about
 * application routes — it only asks the {@link Router} whether one matches, and otherwise falls
 * back to the {@link StaticFileService}. New routes never require touching this loop.</p>
 */
public class HttpServer {

    private static volatile boolean running = false;

    private HttpServer() {
    }

    public static void start(int port, Router router, StaticFileService staticFileService) throws IOException {
        running = true;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);
            while (running) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleConnection(clientSocket, router, staticFileService);
                } catch (IOException e) {
                    System.out.println("Error handling a connection: " + e.getMessage());
                }
            }
        }
        System.out.println("Server stopped gracefully.");
    }

    /**
     * Marks the server as no longer running. The current request still finishes and its
     * response is still sent; the main loop only checks this flag once it goes back around,
     * so the shutdown never terminates a connection abruptly.
     */
    public static void stop() {
        running = false;
    }

    private static void handleConnection(Socket socket, Router router, StaticFileService staticFileService) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        OutputStream out = socket.getOutputStream();

        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isBlank()) {
            writeError(out, "400 Bad Request", "Empty request");
            return;
        }

        String[] parts = requestLine.split(" ");
        if (parts.length < 2) {
            writeError(out, "400 Bad Request", "Malformed request line");
            return;
        }

        String method = parts[0];
        String rawPath = parts[1];

        if (!"GET".equalsIgnoreCase(method)) {
            writeError(out, "405 Method Not Allowed", "Only GET is supported");
            return;
        }

        String routePath = extractRoutePath(rawPath);
        Map<String, String> queryParams = extractQueryParams(extractQueryString(rawPath));

        Request request = new Request(method, routePath, queryParams);
        Response response = new Response();

        Service service = router.resolve(method, routePath);
        if (service != null) {
            handleDynamicRoute(out, service, request, response);
            return;
        }

        String staticPath = routePath.equals("/") ? "/index.html" : routePath;
        handleStaticResource(out, staticFileService, staticPath);
    }

    private static void handleDynamicRoute(OutputStream out, Service service, Request request, Response response) throws IOException {
        try {
            String body = service.handle(request, response);
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
            out.write(buildHeader(response.getStatus(), response.getContentType(), bodyBytes.length).getBytes(StandardCharsets.UTF_8));
            out.write(bodyBytes);
            out.flush();
        } catch (Exception e) {
            writeError(out, "500 Internal Server Error", "Route handler failed: " + e.getMessage());
        }
    }

    private static void handleStaticResource(OutputStream out, StaticFileService staticFileService, String path) throws IOException {
        StaticFileService.StaticResource resource = staticFileService.resolve(path);
        if (resource == null) {
            writeError(out, "404 Not Found", "404 Not Found");
            return;
        }
        out.write(buildHeader("200 OK", resource.getContentType(), resource.getContent().length).getBytes(StandardCharsets.UTF_8));
        out.write(resource.getContent());
        out.flush();
    }

    private static void writeError(OutputStream out, String status, String message) throws IOException {
        byte[] bodyBytes = message.getBytes(StandardCharsets.UTF_8);
        out.write(buildHeader(status, "text/plain; charset=UTF-8", bodyBytes.length).getBytes(StandardCharsets.UTF_8));
        out.write(bodyBytes);
        out.flush();
    }

    static String buildHeader(String status, String contentType, int contentLength) {
        return "HTTP/1.1 " + status + "\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + contentLength + "\r\n"
                + "Connection: close\r\n"
                + "\r\n";
    }

    static String extractRoutePath(String path) {
        int queryIndex = path.indexOf('?');
        return queryIndex == -1 ? path : path.substring(0, queryIndex);
    }

    static String extractQueryString(String path) {
        int queryIndex = path.indexOf('?');
        return queryIndex == -1 ? null : path.substring(queryIndex + 1);
    }

    static Map<String, String> extractQueryParams(String queryString) {
        Map<String, String> queryParams = new HashMap<>();
        if (queryString == null || queryString.isBlank()) {
            return queryParams;
        }
        for (String param : queryString.split("&")) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2) {
                queryParams.put(
                        URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
            } else if (keyValue.length == 1 && !keyValue[0].isBlank()) {
                queryParams.put(URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8), "");
            }
        }
        return queryParams;
    }
}

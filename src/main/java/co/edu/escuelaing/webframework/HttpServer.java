package co.edu.escuelaing.webframework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Accepts connections, parses HTTP requests, and writes HTTP responses.
 *
 * <p>Each accepted connection is handed off to a virtual thread (one per task, via
 * {@link Executors#newVirtualThreadPerTaskExecutor()}), so multiple requests are processed
 * concurrently instead of one at a time. This class knows nothing about application routes —
 * it only asks the {@link Router} whether one matches, and otherwise falls back to the
 * {@link StaticFileService}. New routes never require touching this loop.</p>
 *
 * <p>Shutdown is graceful: {@link #stop()} stops accepting new connections and then waits
 * (up to {@link #SHUTDOWN_TIMEOUT_SECONDS}) for connections already in flight to finish
 * before returning, so no in-progress request is cut off mid-response.</p>
 */
public class HttpServer {

    private static final int ACCEPT_POLL_TIMEOUT_MS = 500;
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 30;

    private static volatile boolean running = false;
    private static volatile ServerSocket serverSocket;
    private static volatile ExecutorService connectionExecutor;
    private static volatile CountDownLatch stoppedLatch = new CountDownLatch(1);

    private HttpServer() {
    }

    public static void start(int port, Router router, StaticFileService staticFileService) throws IOException {
        running = true;
        stoppedLatch = new CountDownLatch(1);
        connectionExecutor = Executors.newVirtualThreadPerTaskExecutor();

        try (ServerSocket socket = new ServerSocket(port)) {
            serverSocket = socket;
            socket.setSoTimeout(ACCEPT_POLL_TIMEOUT_MS);
            System.out.println("Server listening on port " + port);
            while (running) {
                try {
                    Socket clientSocket = socket.accept();
                    connectionExecutor.execute(() -> handleConnectionSafely(clientSocket, router, staticFileService));
                } catch (SocketTimeoutException e) {
                    // Expected: lets the loop re-check the running flag periodically.
                } catch (SocketException e) {
                    // Expected on shutdown: stop() closes the socket to unblock accept().
                    if (running) {
                        System.out.println("Error accepting a connection: " + e.getMessage());
                    }
                } catch (IOException e) {
                    System.out.println("Error accepting a connection: " + e.getMessage());
                }
            }
        } finally {
            awaitInFlightConnections();
            stoppedLatch.countDown();
        }
        System.out.println("Server stopped gracefully.");
    }

    /**
     * Requests a graceful shutdown: stops accepting new connections. The main server loop
     * (running {@link #start}) then drains connections already in flight before returning;
     * use {@link #awaitStopped(long, TimeUnit)} to block until that draining is complete —
     * important for a JVM shutdown hook, since the JVM does not otherwise wait for it.
     */
    public static void stop() {
        running = false;
        ServerSocket socket = serverSocket;
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing server socket during shutdown: " + e.getMessage());
            }
        }
    }

    /**
     * Blocks until the server has fully stopped (accept loop exited and in-flight
     * connections drained), or the timeout elapses. Returns {@code true} if it stopped
     * in time.
     */
    public static boolean awaitStopped(long timeout, TimeUnit unit) throws InterruptedException {
        return stoppedLatch.await(timeout, unit);
    }

    private static void awaitInFlightConnections() {
        ExecutorService executor = connectionExecutor;
        if (executor == null) {
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                System.out.println("Timed out waiting for in-flight requests; forcing shutdown.");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    private static void handleConnectionSafely(Socket socket, Router router, StaticFileService staticFileService) {
        try (Socket clientSocket = socket) {
            handleConnection(clientSocket, router, staticFileService);
        } catch (IOException e) {
            System.out.println("Error handling a connection: " + e.getMessage());
        }
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

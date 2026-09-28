/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package co.edu.escuelaing.webapplication.webapplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ander
 */
public class Webapplication {

    private static final String PUBLIC_DIR = "public"; //Debe ser la carpeta que representa los recursos publicos

    public static void main(String[] args) throws IOException {
        ServerSocket serversocket = new ServerSocket(35000); //Se abre el puerto para escuchar en el puerto 35000
        while (true) {
            try (Socket socket = serversocket.accept()) {//Aca es lo de acceptar la solicitud, no olvidar que es de tipo socket y por eso se llama socket
                //Tambien de que el Try tenga parametros es que se cierra la conexión automaticamente en caso de que el accept falle. 
                Reader streamReader = new InputStreamReader(socket.getInputStream()); //El InputStream son los bytes crudos del socket; el InputStreamReader es quien los traduce a caracteres
                BufferedReader reader = new BufferedReader(streamReader); //El BufferedReader envuelve al Reader de caracteres para poder leer linea por linea con readLine()

                String line = reader.readLine(); //Se lee la primera linea de la solicitud
                if (line == null) {
                    continue; //Si la linea esta nula, se continua con la siguiente conexión.
                }
                String[] parts = line.split(" "); //Se divide la linea en partes para obtener el metodo, la ruta y la version del protocolo HTTP
                String path = parts[1]; //Se obtiene la ruta de la solicitud

                String method = parts[0]; //Se obtiene el metodo de la solicitud
                OutputStream out = socket.getOutputStream(); //Se obtiene el OutputStream del socket para poder escribir la respuesta
                if (!method.equals("GET")) {
                    String body = buildErrorJson("Method not allowed");
                    byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

                    String errorMessage = buildHeader("405 Method Not Allowed", "application/json; charset=UTF-8", bodyBytes.length);

                    out.write(errorMessage.getBytes(StandardCharsets.UTF_8));
                    out.write(bodyBytes);
                    out.flush();
                    continue;
                }

                String routePath = extractRoutePath(path);
                String queryString = extractQueryStrings(path);
                Map<String, String> queryParams = extractQueryParams(queryString);

                if (routePath.equals("/")) {
                    path = "/index.html"; //Si la ruta es la raiz, se redirige a index.html
                } else if (routePath.equals("/health")) {
                    String body = buildHealthJson();
                    byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

                    String healthMessage = buildHeader("200 OK", "application/json; charset=UTF-8", bodyBytes.length);

                    byte[] healthMessageByte = healthMessage.getBytes(StandardCharsets.UTF_8);
                    out.write(healthMessageByte);
                    out.write(bodyBytes);
                    out.flush();
                    continue;
                } else if (routePath.equals("/server-time")) {
                    Instant now = Instant.now();
                    String body = buildServerTimeJson(now);
                    byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

                    String timeMessage = buildHeader("200 OK", "application/json; charset=UTF-8", bodyBytes.length);

                    byte[] timeMessageByte = timeMessage.getBytes(StandardCharsets.UTF_8);
                    out.write(timeMessageByte);
                    out.write(bodyBytes);
                    out.flush();
                    continue;
                } else if (routePath.equals("/greeting")) {
                    String name = queryParams.get("name");
                    if (name == null || name.isBlank()) {
                        String body = buildErrorJson("Name is required");
                        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

                        String errorMessage = buildHeader("400 Bad Request", "application/json; charset=UTF-8", bodyBytes.length);
                        out.write(errorMessage.getBytes(StandardCharsets.UTF_8));
                        out.write(bodyBytes);
                        out.flush();
                        continue;
                    }
                    String body = buildGreetingJson(name);
                    byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

                    String greetingMessage = buildHeader("200 OK", "application/json; charset=UTF-8", bodyBytes.length);

                    byte[] greetingMessageBytes = greetingMessage.getBytes(StandardCharsets.UTF_8);
                    out.write(greetingMessageBytes);
                    out.write(bodyBytes);
                    out.flush();
                    continue;
                } else if (routePath.equals("/square")) {
                    String numberStr = queryParams.get("number");

                    if (numberStr == null || numberStr.isBlank()) {
                        String body = buildErrorJson("Number is required");
                        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

                        String errorMessage = buildHeader("400 Bad Request", "application/json; charset=UTF-8", bodyBytes.length);
                        byte[] errorMessageBytes = errorMessage.getBytes(StandardCharsets.UTF_8);
                        out.write(errorMessageBytes);
                        out.write(bodyBytes);
                        out.flush();
                        continue;
                    }
                    try {
                        double number = Double.parseDouble(numberStr);
                        double square = number * number;
                        String body = buildSquareJson(number, square);
                        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

                        String squareMessage = buildHeader("200 OK", "application/json; charset=UTF-8", bodyBytes.length);
                        byte[] squareMessageBytes = squareMessage.getBytes(StandardCharsets.UTF_8);
                        out.write(squareMessageBytes);
                        out.write(bodyBytes);
                        out.flush();
                        continue;
                    } catch (NumberFormatException e) {
                        String body = buildErrorJson("Invalid number format");
                        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

                        String errorMessage = buildHeader("400 Bad Request", "application/json; charset=UTF-8", bodyBytes.length);
                        byte[] errorMessageBytes = errorMessage.getBytes(StandardCharsets.UTF_8);
                        out.write(errorMessageBytes);
                        out.write(bodyBytes);
                        out.flush();
                        continue;
                    }
                }

                System.out.println("Solicitud recibida: " + path); //Se imprime la ruta de la solicitud
                System.out.println(line); //Se imprime la primera linea de la solicitud

                // basePath = la carpeta "public" convertida a su direccion absoluta y ya normalizada (sin ".." ni cosas raras)
                Path basePath = Paths.get(PUBLIC_DIR).toAbsolutePath().normalize();
                // newPath = base + lo que pidio el cliente, ya resuelto y simplificado (aqui es donde se "cancelan" los "../")
                Path newPath = basePath.resolve(path.substring(1)).normalize();

                // Si despues de normalizar, la ruta resultante ya NO empieza dentro de basePath,
                // significa que el cliente intento salirse de la carpeta public con algo tipo "../"
                if (!isWithinBase(basePath, newPath)) {
                    String body = buildErrorJson("Path traversal attempt detected");
                    byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

                    String errorMessage = buildHeader("403 Forbidden", "application/json; charset=UTF-8", bodyBytes.length);

                    out.write(errorMessage.getBytes(StandardCharsets.UTF_8));
                    out.write(bodyBytes);
                    out.flush();
                    continue; //Se rechaza el intento de path traversal y se continua con la siguiente conexion
                }

                if (!(Files.exists(newPath))) {
                    String body = buildErrorJson("File not found");
                    byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

                    String errorMessage = buildHeader("404 Not Found", "application/json; charset=UTF-8", bodyBytes.length);

                    out.write(errorMessage.getBytes(StandardCharsets.UTF_8));
                    out.write(bodyBytes);
                    out.flush();
                    continue; //Si el archivo no existe, se envia un mensaje de error 404 y se continua con la siguiente conexión
                }

                byte[] fileByteContent = Files.readAllBytes(newPath); //Se leen todos los bytes del archivo solicitado

                int httpBodyLenght = fileByteContent.length;
                String contentType = getContentType(path);

                String headers = buildHeader("200 OK", contentType, httpBodyLenght);

                out.write(headers.getBytes(StandardCharsets.UTF_8)); //Se escriben los headers de la respuesta
                out.write(fileByteContent);
                out.flush(); //Se vacia el buffer del OutputStream
            } catch (Exception e) {
                System.out.println("Algo ha ocurrido en el sistema");
                e.printStackTrace();
            }
        }
    }

    static String buildErrorJson(String error){
        return "{\"error\" : \"" + error + "\"}";
    }

    static String buildGreetingJson(String name){
        return "{\"greeting\" :\"Hello, " + name + "!\"}";
    }

    static String buildSquareJson(double number, double square){
        return "{\"number\": " + number + ", \"square\": " + square  + "}";
    }

    static String buildHealthJson(){
        return "{\"status\": \"UP\"}";
    }

    static String buildServerTimeJson(Instant now){
        return "{\"serverTime\": \"" + now.toString() + "\"}";
    }

    static boolean isWithinBase(Path basePath, Path newPath){
        return newPath.startsWith(basePath);
    }

    static String buildHeader(String statusType, String contentType, int contentLength){
        String header = "HTTP/1.1 " + statusType + "\r\n"
                        + "Content-Type: " + contentType + "\r\n"
                        + "Content-Length: " + contentLength + "\r\n"
                        + "\r\n";
        return header;
    }

    static String extractRoutePath(String path){
        int queryIndex = path.indexOf('?');
        if (queryIndex == -1){
            return path; //Si no hay query string, se mantien la ruta original
        } else {
            return path.substring(0, queryIndex); //Esto nos ayudara con el path, porque pues hay varias querys
        }
    }

    static String extractQueryStrings(String path){
        int queryIndex = path.indexOf('?');
        if (queryIndex == -1){
            return null; //Si no hay query string, se asigna null
        } else {
            return path.substring(queryIndex + 1); //Si hay query string, se obtiene solo la parte despues del '?'
        }
    }

    static Map<String, String> extractQueryParams(String queryString) {
        Map<String, String> queryParams = new HashMap<>();
        if (queryString != null) {
            String[] params = queryString.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                    String valude = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    queryParams.put(key, valude); //Se agregan los parametros a un mapa
                        }
                    }
                }      
        return queryParams;
    }


    static String getContentType(String path) {
        String extension = path.substring(path.lastIndexOf('.') + 1);
        switch (extension) {
            case "html":
                return "text/html; charset=UTF-8";
            case "js":
                return "text/javascript; charset=UTF-8";
            case "png":
                return "image/png";
            case "jpeg":
            case "jpg":
                return "image/jpeg";
            default:
                return "application/octet-stream";
        }
    }
}
//System.out.println(System.getProperty("user.dir")); Para probar donde estoy parado y asi verificar que
//este ejecutandose el programa dentro de la carpeta raíz del pom.


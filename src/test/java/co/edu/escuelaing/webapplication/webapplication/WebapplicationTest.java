/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package co.edu.escuelaing.webapplication.webapplication;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author ander
 */
public class WebapplicationTest {
    
    public WebapplicationTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of buildErrorJson method, of class Webapplication.
     */
    @Test
    public void testBuildErrorJson() {
        System.out.println("Name is Required");
        String error = "Name is required";
        String expResult = "{\"error\" : \"Name is required\"}";        
        String result = Webapplication.buildErrorJson(error);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of buildGreetingJson method, of class Webapplication.
     */
    @Test
    public void testBuildGreetingJson() {
        System.out.println("buildGreetingJson");
        String name = "Andy";
        String expResult = "{\"greeting\" :\"Hello, Andy!\"}";
        String result = Webapplication.buildGreetingJson(name);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of buildSquareJson method, of class Webapplication.
     */
    @Test
    public void testBuildSquareJson() {
        System.out.println("buildSquareJson");
        double number = 2.0;
        double square = 4.0;
        String expResult = "{\"number\": 2.0, \"square\": 4.0}";
        String result = Webapplication.buildSquareJson(number, square);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of buildHealthJson method, of class Webapplication.
     */
    @Test
    public void testBuildHealthJson() {
        System.out.println("buildHealthJson");
        String expResult = "{\"status\": \"UP\"}";
        String result = Webapplication.buildHealthJson();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of buildServerTimeJson method, of class Webapplication.
     */
    @Test
    public void testBuildServerTimeJson() {
        System.out.println("buildServerTimeJson");
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        String expResult = "{\"serverTime\": \"2024-01-01T00:00:00Z\"}";
        String result = Webapplication.buildServerTimeJson(now);
        assertEquals(expResult, result);
    }

    /**
     * Test of isWithinBase method, of class Webapplication.
     */
    @Test
    public void testIsWithinBase() {
        System.out.println("isWithinBase");
        Path basePath = Path.of("public").toAbsolutePath().normalize();
        Path newPath = basePath.resolve("index.html").normalize();
        boolean expResult = true;
        boolean result = Webapplication.isWithinBase(basePath, newPath);
        assertEquals(expResult, result);
    }

    /**
     * Test of isWithinBase method, of class Webapplication, cuando la ruta
     * intenta salirse de la carpeta base con "..".
     */
    @Test
    public void testIsWithinBase_pathTraversal() {
        System.out.println("isWithinBase - path traversal");
        Path basePath = Path.of("public").toAbsolutePath().normalize();
        Path newPath = basePath.resolve("../pom.xml").normalize();
        boolean expResult = false;
        boolean result = Webapplication.isWithinBase(basePath, newPath);
        assertEquals(expResult, result);
    }

    /**
     * Test of buildHeader method, of class Webapplication.
     */
    @Test
    public void testBuildHeader() {
        System.out.println("buildHeader");
        String statusType = "200 OK";
        String contentType = "application/json; charset=UTF-8";
        int contentLength = 42;
        String expResult = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: application/json; charset=UTF-8\r\n"
                + "Content-Length: 42\r\n"
                + "\r\n";
        String result = Webapplication.buildHeader(statusType, contentType, contentLength);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractRoutePath method, of class Webapplication, cuando el
     * path no tiene query string.
     */
    @Test
    public void testExtractRoutePath_sinQueryString() {
        System.out.println("extractRoutePath - sin query string");
        String path = "/greeting";
        String expResult = "/greeting";
        String result = Webapplication.extractRoutePath(path);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractRoutePath method, of class Webapplication, cuando el
     * path sí tiene query string.
     */
    @Test
    public void testExtractRoutePath_conQueryString() {
        System.out.println("extractRoutePath - con query string");
        String path = "/greeting?name=Andy";
        String expResult = "/greeting";
        String result = Webapplication.extractRoutePath(path);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractQueryStrings method, of class Webapplication, cuando el
     * path no tiene query string (debe devolver null).
     */
    @Test
    public void testExtractQueryStrings_sinQueryString() {
        System.out.println("extractQueryStrings - sin query string");
        String path = "/greeting";
        String expResult = null;
        String result = Webapplication.extractQueryStrings(path);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractQueryStrings method, of class Webapplication, cuando el
     * path sí tiene query string.
     */
    @Test
    public void testExtractQueryStrings_conQueryString() {
        System.out.println("extractQueryStrings - con query string");
        String path = "/greeting?name=Andy";
        String expResult = "name=Andy";
        String result = Webapplication.extractQueryStrings(path);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractQueryParams method, of class Webapplication, cuando el
     * queryString es null (no debe fallar, debe devolver un mapa vacío).
     */
    @Test
    public void testExtractQueryParams_null() {
        System.out.println("extractQueryParams - null");
        String queryString = null;
        Map<String, String> result = Webapplication.extractQueryParams(queryString);
        assertTrue(result.isEmpty());
    }

    /**
     * Test of extractQueryParams method, of class Webapplication, con un solo
     * parámetro.
     */
    @Test
    public void testExtractQueryParams_unParametro() {
        System.out.println("extractQueryParams - un parametro");
        String queryString = "name=Andy";
        Map<String, String> result = Webapplication.extractQueryParams(queryString);
        assertEquals("Andy", result.get("name"));
    }

    /**
     * Test of extractQueryParams method, of class Webapplication, con varios
     * parámetros unidos por "&".
     */
    @Test
    public void testExtractQueryParams_variosParametros() {
        System.out.println("extractQueryParams - varios parametros");
        String queryString = "name=Andy&x=1";
        Map<String, String> result = Webapplication.extractQueryParams(queryString);
        assertEquals("Andy", result.get("name"));
        assertEquals("1", result.get("x"));
    }

    /**
     * Test of extractQueryParams method, of class Webapplication, con un
     * valor que trae espacios codificados como "%20" (URLDecoder).
     */
    @Test
    public void testExtractQueryParams_valorCodificado() {
        System.out.println("extractQueryParams - valor codificado");
        String queryString = "name=Anderson%20Garcia";
        Map<String, String> result = Webapplication.extractQueryParams(queryString);
        assertEquals("Anderson Garcia", result.get("name"));
    }

    /**
     * Test of getContentType method, of class Webapplication, para un
     * archivo .html.
     */
    @Test
    public void testGetContentType_html() {
        System.out.println("getContentType - html");
        String path = "/index.html";
        String expResult = "text/html; charset=UTF-8";
        String result = Webapplication.getContentType(path);
        assertEquals(expResult, result);
    }

    /**
     * Test of getContentType method, of class Webapplication, para un
     * archivo .js.
     */
    @Test
    public void testGetContentType_js() {
        System.out.println("getContentType - js");
        String path = "/script.js";
        String expResult = "text/javascript; charset=UTF-8";
        String result = Webapplication.getContentType(path);
        assertEquals(expResult, result);
    }

    /**
     * Test of getContentType method, of class Webapplication, para una
     * extensión desconocida (debe caer en el default).
     */
    @Test
    public void testGetContentType_desconocida() {
        System.out.println("getContentType - desconocida");
        String path = "/archivo.xyz";
        String expResult = "application/octet-stream";
        String result = Webapplication.getContentType(path);
        assertEquals(expResult, result);
    }

}

package com.weatherapp.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.weatherapp.exception.WeatherFetchException;
import com.weatherapp.util.Constants;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Cliente HTTP para comunicarse con APIs externas.
 * 
 * <p>Esta clase proporciona métodos para realizar peticiones GET a endpoints
 * externos y procesar las respuestas JSON. Incluye manejo robusto de errores,
 * timeouts y validación de respuestas.</p>
 * 
 * <p><strong>Características:</strong></p>
 * <ul>
 *   <li>Peticiones GET configurables</li>
 *   <li>Timeouts de conexión y lectura</li>
 *   <li>Manejo de excepciones específicas</li>
 *   <li>Logging detallado de operaciones</li>
 *   <li>Validación de respuestas JSON</li>
 * </ul>
 * 
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>
 * {@code
 * ApiClient client = new ApiClient();
 * JsonObject response = client.makeRequest("https://api.example.com/data");
 * String data = response.get("field").getAsString();
 * }
 * </pre>
 * 
 * @author Karen Valenzuela Landero
 * @version 1.0
 * @since 2026-04-16
 * @see com.weatherapp.exception.WeatherFetchException
 * @see com.weatherapp.util.Constants
 */
public class ApiClient {
    private static final Logger logger = Logger.getLogger(ApiClient.class.getName());
    
    /**
     * Realiza una petición GET a una URL y retorna la respuesta como JsonObject
     * @param urlString URL a la que realizar la petición
     * @return JsonObject con la respuesta parseada
     * @throws WeatherFetchException Si hay error en la petición o respuesta
     */
    public JsonObject makeRequest(String urlString) throws WeatherFetchException {
        HttpURLConnection connection = null;
        try {
            // Crear y configurar conexión
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
            connection.setReadTimeout(Constants.READ_TIMEOUT);
            
            // Validar respuesta HTTP
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                String errorMessage = String.format(
                    "Error HTTP %d al conectar con %s",
                    responseCode,
                    url.getHost()
                );
                logger.severe(errorMessage);
                throw new WeatherFetchException(errorMessage);
            }
            
            // Leer respuesta
            String responseBody = readResponse(connection);
            
            // Parsear JSON
            try {
                return JsonParser.parseString(responseBody).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                logger.severe("Respuesta JSON malformada: " + e.getMessage());
                throw new WeatherFetchException("Respuesta JSON inválida de la API", e);
            }
            
        } catch (WeatherFetchException e) {
            throw e;
        } catch (java.net.SocketTimeoutException e) {
            logger.severe("Timeout en la petición: " + e.getMessage());
            throw new WeatherFetchException("Tiempo de espera agotado al conectar con la API", e);
        } catch (java.net.UnknownHostException e) {
            logger.severe("Host desconocido: " + e.getMessage());
            throw new WeatherFetchException("No se puede conectar a la API (sin conexión a Internet)", e);
        } catch (Exception e) {
            logger.severe("Error en petición HTTP: " + e.getMessage());
            throw new WeatherFetchException("Error de conexión: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    /**
     * Lee la respuesta completa de una conexión HTTP
     */
    private String readResponse(HttpURLConnection connection) throws WeatherFetchException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            if (response.length() == 0) {
                throw new WeatherFetchException("Respuesta vacía de la API");
            }
            
            return response.toString();
            
        } catch (Exception e) {
            throw new WeatherFetchException("Error al leer respuesta: " + e.getMessage(), e);
        }
    }
}
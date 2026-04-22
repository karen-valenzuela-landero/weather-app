package com.weatherapp.service;

import com.google.gson.JsonObject;
import com.weatherapp.exception.GeocodingException;
import com.weatherapp.model.Location;
import com.weatherapp.util.Constants;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Servicio de Geocodificación que convierte nombres de ciudades a coordenadas geográficas.
 * 
 * <p>Este servicio utiliza la API de Geocodificación de Open-Meteo para obtener
 * las coordenadas geográficas (latitud y longitud) de una ciudad a partir de su nombre.
 * Incluye validación completa de entrada y manejo robusto de errores.</p>
 * 
 * <p><strong>Dependencias:</strong></p>
 * <ul>
 *   <li>{@link ApiClient} para realizar peticiones HTTP</li>
 *   <li>Open-Meteo Geocoding API</li>
 * </ul>
 * 
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>
 * {@code
 * ApiClient apiClient = new ApiClient();
 * GeocodingService geocoding = new GeocodingService(apiClient);
 * 
 * try {
 *     Location location = geocoding.getLocationByCityName("Madrid");
 *     System.out.println("Ciudad: " + location.getCity());
 *     System.out.println("País: " + location.getCountry());
 *     System.out.println("Latitud: " + location.getLatitude());
 *     System.out.println("Longitud: " + location.getLongitude());
 * } catch (GeocodingException e) {
 *     System.err.println("Error: " + e.getMessage());
 * }
 * }
 * </pre>
 * 
 * @author Karen Valenzuela Landero
 * @version 1.0
 * @since 2026-04-16
 * @see ApiClient
 * @see Location
 * @see com.weatherapp.exception.GeocodingException
 */
public class GeocodingService {
    private static final Logger logger = Logger.getLogger(GeocodingService.class.getName());
    private final ApiClient apiClient;
    
    public GeocodingService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Obtiene las coordenadas de una ciudad
     * @param cityName Nombre de la ciudad a buscar
     * @return Location con latitud, longitud y nombre de la ciudad
     * @throws GeocodingException Si la ciudad no existe o hay error en la petición
     */
    public Location getLocationByCityName(String cityName) throws GeocodingException {
        try {
            // Validar entrada
            if (cityName == null || cityName.trim().isEmpty()) {
                throw new GeocodingException("El nombre de la ciudad no puede estar vacío");
            }
            
            // Construir URL con encoding adecuado
            String encodedCity = URLEncoder.encode(cityName.trim(), StandardCharsets.UTF_8);
            String url = buildGeocodingUrl(encodedCity);
            
            logger.info("Buscando coordenadas para: " + cityName);
            
            // Realizar petición
            JsonObject response = apiClient.makeRequest(url);
            
            // Validar respuesta
            if (!response.has("results")) {
                throw new GeocodingException("Respuesta inválida de la API de geocodificación");
            }
            
            int resultCount = response.getAsJsonArray("results").size();
            if (resultCount == 0) {
                throw new GeocodingException("Ciudad no encontrada: " + cityName);
            }
            
            // Extraer y parsear datos
            JsonObject result = response.getAsJsonArray("results").get(0).getAsJsonObject();
            Location location = parseLocationFromJson(result);
            
            logger.info("Ciudad encontrada: " + location.getCity() + " (" + location.getCountry() + ")");
            
            return location;
            
        } catch (GeocodingException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Error al geocodificar: " + e.getMessage());
            throw new GeocodingException("Error al buscar la ciudad: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parsea los datos de ubicación desde JSON
     */
    private Location parseLocationFromJson(JsonObject result) throws GeocodingException {
        try {
            // Validar campos obligatorios
            if (!result.has("latitude") || !result.has("longitude") || !result.has("name")) {
                throw new GeocodingException("Campos faltantes en la respuesta de geocodificación");
            }
            
            double latitude = result.get("latitude").getAsDouble();
            double longitude = result.get("longitude").getAsDouble();
            String city = result.get("name").getAsString();
            String country = result.has("country") ? result.get("country").getAsString() : "Desconocido";
            
            // Validar rangos
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                throw new GeocodingException("Coordenadas inválidas recibidas de la API");
            }
            
            return new Location(city, country, latitude, longitude);
            
        } catch (GeocodingException e) {
            throw e;
        } catch (Exception e) {
            throw new GeocodingException("Error al parsear datos de geocodificación: " + e.getMessage(), e);
        }
    }
    
    private String buildGeocodingUrl(String encodedCity) {
        return String.format(
            "%s?name=%s&count=%d&language=%s&format=json",
            Constants.GEOCODING_API_URL,
            encodedCity,
            Constants.MAX_RESULTS,
            Constants.LANGUAGE
        );
    }
}
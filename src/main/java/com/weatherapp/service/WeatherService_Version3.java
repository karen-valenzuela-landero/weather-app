package com.weatherapp.service;

import com.google.gson.JsonObject;
import com.weatherapp.exception.WeatherFetchException;
import com.weatherapp.model.Location;
import com.weatherapp.model.Weather;
import com.weatherapp.model.WeatherRequest;
import com.weatherapp.model.WeatherResponse;
import com.weatherapp.util.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Servicio de Clima que obtiene datos meteorológicos de una o más ubicaciones geográficas.
 * 
 * <p>Este servicio utiliza la API de Pronóstico de Open-Meteo para obtener
 * los datos meteorológicos actuales de una o varias ubicaciones específicas.
 * Soporta procesamiento secuencial y paralelo para múltiples ciudades.</p>
 * 
 * <p><strong>Características:</strong></p>
 * <ul>
 *   <li>Obtiene datos para una sola ciudad</li>
 *   <li>Obtiene datos para múltiples ciudades</li>
 *   <li>Procesamiento paralelo para mejor rendimiento</li>
 *   <li>Validación exhaustiva de datos</li>
 *   <li>Manejo robusto de errores</li>
 * </ul>
 * 
 * <p><strong>Ejemplo con una ciudad:</strong></p>
 * <pre>
 * {@code
 * Location location = new Location("Madrid", "España", 40.4168, -3.7038);
 * Weather weather = weatherService.getWeatherByLocation(location);
 * }
 * </pre>
 * 
 * <p><strong>Ejemplo con múltiples ciudades:</strong></p>
 * <pre>
 * {@code
 * WeatherRequest request = new WeatherRequest();
 * request.addCity("Madrid");
 * request.addCity("Barcelona");
 * request.addCity("Valencia");
 * 
 * WeatherResponse response = weatherService.getWeatherForCities(request);
 * System.out.println("Ciudades obtenidas: " + response.getWeatherCount());
 * }
 * </pre>
 * 
 * @author Karen Valenzuela Landero
 * @version 2.0
 * @since 2026-04-16
 * @see WeatherRequest
 * @see WeatherResponse
 * @see Location
 * @see Weather
 */
public class WeatherService_Version3 {
    private static final Logger logger = Logger.getLogger(WeatherService_Version3.class.getName());
    private final ApiClient apiClient;
    private final GeocodingService geocodingService;
    
    /**
     * Constructor que inicializa el servicio con dependencias.
     * 
     * @param apiClient cliente HTTP para peticiones
     * @param geocodingService servicio de geocodificación
     * 
     * @throws IllegalArgumentException si alguna dependencia es null
     */
    public WeatherService_Version3(ApiClient apiClient, GeocodingService geocodingService) {
        if (apiClient == null || geocodingService == null) {
            throw new IllegalArgumentException("Las dependencias no pueden ser null");
        }
        this.apiClient = apiClient;
        this.geocodingService = geocodingService;
    }
    
    public Weather getWeatherByLocation(Location location) throws WeatherFetchException {
        try {
            if (location == null) {
                throw new WeatherFetchException("La ubicación no puede ser nula");
            }
            
            String url = buildWeatherUrl(location);
            
            logger.info("Obteniendo datos meteorológicos para: " + location.getCity());
            
            JsonObject response = apiClient.makeRequest(url);
            
            if (!response.has("current")) {
                throw new WeatherFetchException("Respuesta inválida de la API meteorológica");
            }
            
            Weather weather = parseWeatherFromJson(response.getAsJsonObject("current"), location);
            
            logger.info("Datos meteorológicos obtenidos exitosamente para: " + location.getCity());
            
            return weather;
            
        } catch (WeatherFetchException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Error al obtener datos meteorológicos: " + e.getMessage());
            throw new WeatherFetchException("Error al obtener datos del clima: " + e.getMessage(), e);
        }
    }
    
    public WeatherResponse getWeatherForCities(WeatherRequest request) throws WeatherFetchException {
        try {
            if (request == null || !request.hasCities()) {
                throw new IllegalArgumentException("La solicitud debe contener al menos una ciudad");
            }
            
            WeatherResponse response = new WeatherResponse();
            int totalCities = request.getCityCount();
            
            logger.info("Procesando " + totalCities + " ciudades de forma secuencial");
            
            for (int i = 0; i < totalCities; i++) {
                String cityName = request.getCities().get(i);
                try {
                    logger.info("Procesando ciudad " + (i + 1) + "/" + totalCities + ": " + cityName);
                    
                    // Geocodificar
                    Location location = geocodingService.getLocationByCityName(cityName);
                    
                    // Obtener clima
                    Weather weather = getWeatherByLocation(location);
                    
                    // Agregar a respuesta
                    response.addWeather(weather);
                    
                    logger.info("✓ " + cityName + " procesado exitosamente");
                    
                } catch (Exception e) {
                    logger.warning("✗ Error procesando " + cityName + ": " + e.getMessage());
                }
            }
            
            if (!response.hasWeather()) {
                throw new WeatherFetchException("No se pudieron obtener datos de ninguna ciudad");
            }
            
            logger.info("Procesamiento secuencial completado. Ciudades exitosas: " + response.getWeatherCount());
            
            return response;
            
        } catch (WeatherFetchException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Error en procesamiento de múltiples ciudades: " + e.getMessage());
            throw new WeatherFetchException("Error al procesar ciudades: " + e.getMessage(), e);
        }
    }
    
    public WeatherResponse getWeatherForCitiesParallel(WeatherRequest request) throws WeatherFetchException {
        try {
            if (request == null || !request.hasCities()) {
                throw new IllegalArgumentException("La solicitud debe contener al menos una ciudad");
            }
            
            WeatherResponse response = new WeatherResponse();
            int totalCities = request.getCityCount();
            
            logger.info("Procesando " + totalCities + " ciudades de forma paralela");
            
            // Crear tareas asincrónicas para cada ciudad
            List<CompletableFuture<Weather>> futures = request.getCities().stream()
                    .map(cityName -> CompletableFuture.supplyAsync(() -> {
                        try {
                            logger.info("Procesando (paralelo): " + cityName);
                            Location location = geocodingService.getLocationByCityName(cityName);
                            Weather weather = getWeatherByLocation(location);
                            logger.info("✓ " + cityName + " procesado exitosamente");
                            return weather;
                        } catch (Exception e) {
                            logger.warning("✗ Error procesando " + cityName + ": " + e.getMessage());
                            return null;
                        }
                    }))
                    .collect(Collectors.toList());
            
            // Esperar a que todas completen
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));
            
            allFutures.join();
            
            // Agregar resultados exitosos a la respuesta
            futures.stream()
                    .map(CompletableFuture::join)
                    .filter(weather -> weather != null)
                    .forEach(response::addWeather);
            
            if (!response.hasWeather()) {
                throw new WeatherFetchException("No se pudieron obtener datos de ninguna ciudad");
            }
            
            logger.info("Procesamiento paralelo completado. Ciudades exitosas: " + response.getWeatherCount());
            
            return response;
            
        } catch (WeatherFetchException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Error en procesamiento paralelo: " + e.getMessage());
            throw new WeatherFetchException("Error al procesar ciudades en paralelo: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parsea datos meteorológicos desde una respuesta JSON.
     */
    private Weather parseWeatherFromJson(JsonObject current, Location location) throws WeatherFetchException {
        try {
            String[] requiredFields = {"temperature_2m", "weather_code", "relative_humidity_2m", "wind_speed_10m"};
            for (String field : requiredFields) {
                if (!current.has(field)) {
                    throw new WeatherFetchException("Campo faltante en datos meteorológicos: " + field);
                }
            }
            
            double temperature = current.get("temperature_2m").getAsDouble();
            int weatherCode = current.get("weather_code").getAsInt();
            int humidity = current.get("relative_humidity_2m").getAsInt();
            double windSpeed = current.get("wind_speed_10m").getAsDouble();
            
            if (temperature < Constants.MIN_TEMPERATURE || temperature > Constants.MAX_TEMPERATURE) {
                logger.warning("Temperatura fuera de rango realista: " + temperature);
            }
            
            if (humidity < Constants.MIN_HUMIDITY || humidity > Constants.MAX_HUMIDITY) {
                throw new WeatherFetchException("Humedad inválida: " + humidity);
            }
            
            if (windSpeed < Constants.MIN_WIND_SPEED) {
                throw new WeatherFetchException("Velocidad del viento inválida: " + windSpeed);
            }
            
            Weather weather = new Weather();
            weather.setCity(location.getCity());
            weather.setCountry(location.getCountry());
            weather.setLatitude(location.getLatitude());
            weather.setLongitude(location.getLongitude());
            weather.setTemperature(temperature);
            weather.setWeatherCode(weatherCode);
            weather.setHumidity(humidity);
            weather.setWindSpeed(windSpeed);
            
            return weather;
            
        } catch (WeatherFetchException e) {
            throw e;
        } catch (Exception e) {
            throw new WeatherFetchException("Error al parsear datos meteorológicos: " + e.getMessage(), e);
        }
    }
    
    private String buildWeatherUrl(Location location) {
        return String.format(
            "%s?latitude=%.4f&longitude=%.4f&current=temperature_2m,weather_code,relative_humidity_2m,wind_speed_10m&timezone=auto",
            Constants.WEATHER_API_URL,
            location.getLatitude(),
            location.getLongitude()
        );
    }
}
package com.weatherapp.service;

import com.google.gson.JsonObject;
import com.weatherapp.cache.WeatherCache;
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
 * Servicio de Clima con soporte de caché.
 * 
 * <p>Versión mejorada que incluye un sistema de caché para reducir
 * peticiones a las APIs externas.</p>
 * 
 * @author Karen Valenzuela Landero
 * @version 3.0
 * @since 2026-04-16
 */
public class WeatherService {
    private static final Logger logger = Logger.getLogger(WeatherService.class.getName());
    private final ApiClient apiClient;
    private final GeocodingService geocodingService;
    private final WeatherCache cache;
    
    // Constantes de caché
    private static final long DEFAULT_CACHE_TTL = 1800; // 30 minutos
    
    /**
     * Constructor con caché usando TTL por defecto.
     */
    public WeatherService(ApiClient apiClient, GeocodingService geocodingService) {
        this(apiClient, geocodingService, DEFAULT_CACHE_TTL);
    }
    
    /**
     * Constructor con caché personalizado.
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * // Caché de 15 minutos
     * WeatherService service = new WeatherService(apiClient, geocoding, 900);
     * }
     * </pre>
     * 
     * @param apiClient cliente HTTP
     * @param geocodingService servicio de geocodificación
     * @param cacheTtlSeconds TTL del caché en segundos
     * 
     * @throws IllegalArgumentException si alguna dependencia es null
     */
    public WeatherService(ApiClient apiClient, GeocodingService geocodingService, long cacheTtlSeconds) {
        if (apiClient == null || geocodingService == null) {
            throw new IllegalArgumentException("Las dependencias no pueden ser null");
        }
        this.apiClient = apiClient;
        this.geocodingService = geocodingService;
        this.cache = new WeatherCache(cacheTtlSeconds);
    }
    
    /**
     * Obtiene datos meteorológicos con soporte de caché.
     * 
     * <p>Primero verifica el caché. Si los datos están disponibles y válidos,
     * los retorna. Si no, obtiene los datos de la API y los almacena en caché.</p>
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * Location location = new Location("Madrid", "España", 40.4168, -3.7038);
     * Weather weather = weatherService.getWeatherByLocation(location);
     * // Primera llamada: obtiene de API y almacena en caché
     * // Segunda llamada (dentro de TTL): obtiene del caché
     * }
     * </pre>
     * 
     * @param location ubicación con coordenadas
     * @return datos meteorológicos (del caché o de la API)
     * @throws WeatherFetchException si hay error
     */
    public Weather getWeatherByLocation(Location location) throws WeatherFetchException {
        try {
            if (location == null) {
                throw new WeatherFetchException("La ubicación no puede ser nula");
            }
            
            // Verificar caché primero
            Weather cached = cache.get(location.getCity());
            if (cached != null) {
                logger.info("✓ Usando datos del caché para: " + location.getCity());
                return cached;
            }
            
            logger.info("📡 Obteniendo datos de API para: " + location.getCity());
            
            String url = buildWeatherUrl(location);
            JsonObject response = apiClient.makeRequest(url);
            
            if (!response.has("current")) {
                throw new WeatherFetchException("Respuesta inválida de la API meteorológica");
            }
            
            Weather weather = parseWeatherFromJson(response.getAsJsonObject("current"), location);
            
            // Guardar en caché
            cache.put(location.getCity(), weather);
            
            return weather;
            
        } catch (WeatherFetchException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Error al obtener datos meteorológicos: " + e.getMessage());
            throw new WeatherFetchException("Error al obtener datos del clima: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene datos para múltiples ciudades con soporte de caché.
     * 
     * <p>Primero verifica qué ciudades están en caché. Para las que no están,
     * realiza peticiones geocodificación y clima.</p>
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * WeatherRequest request = new WeatherRequest();
     * request.addCities(Arrays.asList("Madrid", "Barcelona", "Valencia"));
     * 
     * WeatherResponse response = weatherService.getWeatherForCities(request);
     * // Si Madrid está en caché: se obtiene del caché (rápido)
     * // Si Barcelona no está: se obtiene de API (lento)
     * }
     * </pre>
     * 
     * @param request solicitud con ciudades
     * @return respuesta con datos meteorológicos
     * @throws WeatherFetchException si hay error
     */
    public WeatherResponse getWeatherForCities(WeatherRequest request) throws WeatherFetchException {
        try {
            if (request == null || !request.hasCities()) {
                throw new IllegalArgumentException("La solicitud debe contener al menos una ciudad");
            }
            
            WeatherResponse response = new WeatherResponse();
            int totalCities = request.getCityCount();
            
            logger.info("Procesando " + totalCities + " ciudades de forma secuencial (con caché)");
            
            for (int i = 0; i < totalCities; i++) {
                String cityName = request.getCities().get(i);
                try {
                    logger.info("Procesando ciudad " + (i + 1) + "/" + totalCities + ": " + cityName);
                    
                    // Verificar caché primero
                    Weather cached = cache.get(cityName);
                    if (cached != null) {
                        logger.info("✓ " + cityName + " obtenido del caché");
                        response.addWeather(cached);
                        continue;
                    }
                    
                    // Si no está en caché, geocodificar y obtener clima
                    Location location = geocodingService.getLocationByCityName(cityName);
                    Weather weather = getWeatherByLocation(location);
                    response.addWeather(weather);
                    
                    logger.info("✓ " + cityName + " procesado exitosamente");
                    
                } catch (Exception e) {
                    logger.warning("✗ Error procesando " + cityName + ": " + e.getMessage());
                }
            }
            
            if (!response.hasWeather()) {
                throw new WeatherFetchException("No se pudieron obtener datos de ninguna ciudad");
            }
            
            return response;
            
        } catch (WeatherFetchException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Error en procesamiento: " + e.getMessage());
            throw new WeatherFetchException("Error al procesar ciudades: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene datos para múltiples ciudades de forma paralela con caché.
     * 
     * @param request solicitud con ciudades
     * @return respuesta con datos meteorológicos
     * @throws WeatherFetchException si hay error
     */
    public WeatherResponse getWeatherForCitiesParallel(WeatherRequest request) throws WeatherFetchException {
        try {
            if (request == null || !request.hasCities()) {
                throw new IllegalArgumentException("La solicitud debe contener al menos una ciudad");
            }
            
            WeatherResponse response = new WeatherResponse();
            int totalCities = request.getCityCount();
            
            logger.info("Procesando " + totalCities + " ciudades de forma paralela (con caché)");
            
            List<CompletableFuture<Weather>> futures = request.getCities().stream()
                    .map(cityName -> CompletableFuture.supplyAsync(() -> {
                        try {
                            logger.info("Procesando (paralelo): " + cityName);
                            
                            // Verificar caché
                            Weather cached = cache.get(cityName);
                            if (cached != null) {
                                logger.info("✓ " + cityName + " obtenido del caché");
                                return cached;
                            }
                            
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
            
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));
            
            allFutures.join();
            
            futures.stream()
                    .map(CompletableFuture::join)
                    .filter(weather -> weather != null)
                    .forEach(response::addWeather);
            
            if (!response.hasWeather()) {
                throw new WeatherFetchException("No se pudieron obtener datos de ninguna ciudad");
            }
            
            return response;
            
        } catch (WeatherFetchException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Error en procesamiento paralelo: " + e.getMessage());
            throw new WeatherFetchException("Error al procesar ciudades: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene acceso al gestor de caché.
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * WeatherCache cache = weatherService.getCache();
     * cache.printStats();
     * cache.clear();
     * }
     * </pre>
     * 
     * @return gestor de caché
     */
    public WeatherCache getCache() {
        return cache;
    }
    
    private Weather parseWeatherFromJson(JsonObject current, Location location) throws WeatherFetchException {
        try {
            String[] requiredFields = {"temperature_2m", "weather_code", "relative_humidity_2m", "wind_speed_10m"};
            for (String field : requiredFields) {
                if (!current.has(field)) {
                    throw new WeatherFetchException("Campo faltante: " + field);
                }
            }
            
            double temperature = current.get("temperature_2m").getAsDouble();
            int weatherCode = current.get("weather_code").getAsInt();
            int humidity = current.get("relative_humidity_2m").getAsInt();
            double windSpeed = current.get("wind_speed_10m").getAsDouble();
            
            if (temperature < Constants.MIN_TEMPERATURE || temperature > Constants.MAX_TEMPERATURE) {
                logger.warning("Temperatura fuera de rango: " + temperature);
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
            throw new WeatherFetchException("Error al parsear datos: " + e.getMessage(), e);
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
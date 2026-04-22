package com.weatherapp.service;

import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.weatherapp.exception.WeatherFetchException;
import com.weatherapp.model.Location;
import com.weatherapp.model.Weather;
import com.weatherapp.util.Constants;

/**
 * Servicio de Clima que obtiene datos meteorológicos de una ubicación geográfica.
 * 
 * <p>Este servicio utiliza la API de Pronóstico de Open-Meteo para obtener
 * los datos meteorológicos actuales de una ubicación específica (basada en
 * latitud y longitud). Incluye validación exhaustiva de datos y manejo
 * robusto de errores.</p>
 * 
 * <p><strong>Datos obtenidos:</strong></p>
 * <ul>
 *   <li>Temperatura actual (°C)</li>
 *   <li>Código de condición meteorológica</li>
 *   <li>Humedad relativa (%)</li>
 *   <li>Velocidad del viento (km/h)</li>
 * </ul>
 * 
 * <p><strong>Dependencias:</strong></p>
 * <ul>
 *   <li>{@link ApiClient} para realizar peticiones HTTP</li>
 *   <li>Open-Meteo Forecast API</li>
 * </ul>
 * 
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>
 * {@code
 * ApiClient apiClient = new ApiClient();
 * WeatherService weatherService = new WeatherService(apiClient);
 * Location location = new Location("Madrid", "España", 40.4168, -3.7038);
 * 
 * try {
 *     Weather weather = weatherService.getWeatherByLocation(location);
 *     System.out.println("Temperatura: " + weather.getTemperature() + "°C");
 *     System.out.println("Humedad: " + weather.getHumidity() + "%");
 *     System.out.println("Viento: " + weather.getWindSpeed() + " km/h");
 * } catch (WeatherFetchException e) {
 *     System.err.println("Error: " + e.getMessage());
 * }
 * }
 * </pre>
 * 
 * @author Karen Valenzuela Landero
 * @version 1.0
 * @since 2026-04-16
 * @see ApiClient
 * @see Weather
 * @see Location
 * @see com.weatherapp.exception.WeatherFetchException
 */

public class WeatherService_Version2 {
    private static final Logger logger = Logger.getLogger(WeatherService_Version2.class.getName());
    private final ApiClient apiClient;
    
    public WeatherService_Version2(ApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Obtiene los datos meteorológicos de una ubicación
     * @param location Ubicación con coordenadas
     * @return Weather con datos meteorológicos actuales
     * @throws WeatherFetchException Si hay error en la obtención de datos
     */
    public Weather getWeatherByLocation(Location location) throws WeatherFetchException {
        try {
            if (location == null) {
                throw new WeatherFetchException("La ubicación no puede ser nula");
            }
            
            String url = buildWeatherUrl(location);
            
            logger.info("Obteniendo datos meteorológicos para: " + location.getCity());
            
            JsonObject response = apiClient.makeRequest(url);
            
            // Validar respuesta
            if (!response.has("current")) {
                throw new WeatherFetchException("Respuesta inválida de la API meteorológica");
            }
            
            Weather weather = parseWeatherFromJson(response.getAsJsonObject("current"), location);
            
            logger.info("Datos meteorológicos obtenidos exitosamente");
            
            return weather;
            
        } catch (WeatherFetchException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Error al obtener datos meteorológicos: " + e.getMessage());
            throw new WeatherFetchException("Error al obtener datos del clima: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parsea los datos meteorológicos desde JSON
     */
    private Weather parseWeatherFromJson(JsonObject current, Location location) throws WeatherFetchException {
        try {
            // Validar campos obligatorios
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
            
            // Validar rangos
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
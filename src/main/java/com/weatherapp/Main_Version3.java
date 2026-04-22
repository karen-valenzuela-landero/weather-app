package com.weatherapp;

import java.util.Scanner;
import java.util.logging.Logger;

import com.weatherapp.exception.GeocodingException;
import com.weatherapp.exception.WeatherFetchException;
import com.weatherapp.model.Location;
import com.weatherapp.model.Weather;
import com.weatherapp.service.ApiClient;
import com.weatherapp.service.GeocodingService;
import com.weatherapp.service.WeatherService_Version2;

/**
 * Aplicación principal del Clima.
 * 
 * <p>Esta es la clase principal que coordina el flujo de la aplicación del clima.
 * Maneja la entrada del usuario, coordina los servicios de geocodificación y
 * obtención de datos meteorológicos, y muestra los resultados de forma amigable.</p>
 * 
 * <p><strong>Flujo de la aplicación:</strong></p>
 * <ol>
 *   <li>Solicitar nombre de ciudad al usuario</li>
 *   <li>Validar entrada</li>
 *   <li>Usar GeocodingService para obtener coordenadas</li>
 *   <li>Usar WeatherService para obtener datos meteorológicos</li>
 *   <li>Mostrar resultados en formato legible</li>
 *   <li>Manejar errores de forma elegante</li>
 * </ol>
 * 
 * <p><strong>Ejemplo de ejecución:</strong></p>
 * <pre>
 * {@code
 * $ java Main
 * === Aplicación del Clima ===
 * Ingresa el nombre de la ciudad: Madrid
 * 
 *  Buscando ciudad...
 *  Obteniendo datos meteorológicos...
 * 
 * ====================================
 * ║     CLIMA EN MADRID              ║
 * ====================================
 * País: España
 * Coordenadas: 40.4168, -3.7038
 * 
 *   Temperatura: 22.5°C
 *  Humedad: 65%
 *  Velocidad del viento: 12.4 km/h
 *   Condición: Mayormente despejado
 * }
 * </pre>
 * 
 * <p><strong>Ciudades de ejemplo que puedes consultar:</strong></p>
 * <ul>
 *   <li>Madrid - España</li>
 *   <li>Barcelona - España</li>
 *   <li>París - Francia</li>
 *   <li>Nueva York - Estados Unidos</li>
 *   <li>Tokyo - Japón</li>
 *   <li>São Paulo - Brasil</li>
 * </ul>
 * 
 * @author Karen Valenzuela Landero
 * @version 1.0
 * @since 2026-04-16
 * @see GeocodingService
 * @see WeatherService_Version2
 * @see ApiClient
 */

public class Main_Version3 {
    private static final Logger logger = Logger.getLogger(Main_Version2.class.getName());
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        try {
            System.out.println("=== Aplicación del Clima ===");
            System.out.print("Ingresa el nombre de la ciudad: ");
            String cityName = scanner.nextLine().trim();
            
            // Validar entrada
            if (cityName.isEmpty()) {
                System.out.println(" Error: El nombre de la ciudad no puede estar vacío.");
                return;
            }
            
            // Inicializar servicios
            ApiClient apiClient = new ApiClient();
            GeocodingService geocodingService = new GeocodingService(apiClient);
            WeatherService_Version2 weatherService = new WeatherService_Version2(apiClient);
            
            // Obtener ubicación
            System.out.println("\n Buscando ciudad...");
            Location location = geocodingService.getLocationByCityName(cityName);
            
            // Obtener clima
            System.out.println(" Obteniendo datos meteorológicos...\n");
            Weather weather = weatherService.getWeatherByLocation(location);
            
            // Mostrar resultados
            displayWeather(weather);
            
        } catch (GeocodingException e) {
            System.out.println(" Error de búsqueda: " + e.getMessage());
            logger.severe(e.getMessage());
        } catch (WeatherFetchException e) {
            System.out.println(" Error al obtener el clima: " + e.getMessage());
            logger.severe(e.getMessage());
        } catch (Exception e) {
            System.out.println(" Error inesperado: " + e.getMessage());
            logger.severe("Error no manejado: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
    
    /**
     * Muestra los datos meteorológicos de forma amigable
     */
    private static void displayWeather(Weather weather) {
        System.out.println("====================================");
        System.out.println("║     CLIMA EN " + padCenter(weather.getCity().toUpperCase(), 28) + "║");
        System.out.println("====================================");
        System.out.println("País: " + weather.getCountry());
        System.out.println("Coordenadas: " + String.format("%.4f, %.4f", weather.getLatitude(), weather.getLongitude()));
        System.out.println("\n  Temperatura: " + weather.getTemperature() + "°C");
        System.out.println(" Humedad: " + weather.getHumidity() + "%");
        System.out.println(" Velocidad del viento: " + weather.getWindSpeed() + " km/h");
        System.out.println("  Condición: " + getWeatherDescription(weather.getWeatherCode()));
        System.out.println();
    }
    
    private static String getWeatherDescription(int code) {
        switch (code) {
            case 0: return "Cielo despejado";
            case 1: case 2: return "Mayormente despejado";
            case 3: return "Nublado";
            case 45: case 48: return "Niebla";
            case 51: case 53: case 55: return "Llovizna";
            case 61: case 63: case 65: return "Lluvia";
            case 71: case 73: case 75: case 77: return "Nieve";
            case 80: case 81: case 82: return "Lluvia fuerte";
            case 85: case 86: return "Aguaceros de nieve";
            case 95: case 96: case 99: return "Tormenta";
            default: return "Desconocido";
        }
    }
    
    private static String padCenter(String str, int length) {
        if (str.length() >= length) {
            return str.substring(0, length);
        }
        int totalPad = length - str.length();
        int padLeft = totalPad / 2;
        int padRight = totalPad - padLeft;
        return " ".repeat(padLeft) + str + " ".repeat(padRight);
    }
}
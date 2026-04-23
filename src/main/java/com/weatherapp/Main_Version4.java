package com.weatherapp;

import com.weatherapp.exception.GeocodingException;
import com.weatherapp.exception.WeatherFetchException;
import com.weatherapp.model.Weather;
import com.weatherapp.model.WeatherRequest;
import com.weatherapp.model.WeatherResponse;
import com.weatherapp.service.ApiClient;
import com.weatherapp.service.GeocodingService;
import com.weatherapp.service.WeatherService_Version3;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Aplicación principal del Clima - Versión Mejorada.
 * 
 * <p>Esta versión soporta la consulta de una o más ciudades con opción de
 * procesamiento secuencial o paralelo para mejor rendimiento.</p>
 * 
 * <p><strong>Menú de opciones:</strong></p>
 * <ol>
 *   <li>Consultar una ciudad</li>
 *   <li>Consultar múltiples ciudades</li>
 *   <li>Comparar temperaturas</li>
 *   <li>Salir</li>
 * </ol>
 * 
 * @author Karen Valenzuela Landero
 * @version 2.0
 * @since 2026-04-16
 */
public class Main_Version4 {
    private static final Logger logger = Logger.getLogger(Main_Version4.class.getName());
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        try {
            // Inicializar servicios
            ApiClient apiClient = new ApiClient();
            GeocodingService geocodingService = new GeocodingService(apiClient);
            WeatherService_Version3 weatherService = new WeatherService_Version3(apiClient, geocodingService);
            
            int option;
            do {
                showMainMenu();
                System.out.print("Selecciona una opción: ");
                
                option = Integer.parseInt(scanner.nextLine());

                try {
                    
                    
                    switch (option) {
                        case 1:
                            querySingleCity(scanner, weatherService);
                            break;
                        case 2:
                            queryMultipleCities(scanner, weatherService);
                            break;
                        case 3:
                            compareTemperatures(scanner, weatherService);
                            break;
                        case 4:
                            System.out.println("\n¡Hasta luego!");
                            break;
                        default:
                            System.out.println("Opción inválida. Intenta de nuevo.");
                    }
                    
                } catch (NumberFormatException e) {
                    System.out.println("Por favor ingresa un número válido.");
                }
                
            } while (option != 4);
            
        } catch (Exception e) {
            System.out.println("Error inesperado: " + e.getMessage());
            logger.severe("Error no manejado: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
    
    /**
     * Muestra el menú principal de opciones.
     */
    private static void showMainMenu() {
        System.out.println("\n======================================");
        System.out.println("║   APLICACIÓN DEL CLIMA v2.0        ║");
        System.out.println("╠====================================╣");
        System.out.println("║  1. Consultar una ciudad           ║");
        System.out.println("║  2. Consultar múltiples ciudades   ║");
        System.out.println("║  3. Comparar temperaturas          ║");
        System.out.println("║  4. Salir                          ║");
        System.out.println("======================================");
    }
    
    /**
     * Consulta el clima de una sola ciudad.
     * 
     * <p><strong>Flujo:</strong></p>
     * <ol>
     *   <li>Solicita nombre de ciudad</li>
     *   <li>Obtiene datos meteorológicos</li>
     *   <li>Muestra resultados formateados</li>
     * </ol>
     */
    private static void querySingleCity(Scanner scanner, WeatherService_Version3 weatherService) throws GeocodingException {
        System.out.print("\nIngresa el nombre de la ciudad: ");
        String cityName = scanner.nextLine().trim();
        
        if (cityName.isEmpty()) {
            System.out.println("Error: El nombre de la ciudad no puede estar vacío.");
            return;
        }
        
        try {
            System.out.println("\nBuscando ciudad...");
            System.out.println("Obteniendo datos meteorológicos...\n");
            
            WeatherRequest request = new WeatherRequest();
            request.addCity(cityName);
            
            WeatherResponse response = weatherService.getWeatherForCities(request);
            
            if (response.hasWeather()) {
                Weather weather = response.getAllWeather().get(0);
                displayWeather(weather);
            }
            
        } catch (WeatherFetchException e) {
            System.out.println("Error al obtener el clima: " + e.getMessage());
        }
    }
    
    /**
     * Consulta el clima de múltiples ciudades.
     * 
     * <p><strong>Flujo:</strong></p>
     * <ol>
     *   <li>Solicita nombres de ciudades (separadas por coma)</li>
     *   <li>Permite elegir entre procesamiento secuencial o paralelo</li>
     *   <li>Obtiene datos meteorológicos</li>
     *   <li>Muestra todos los resultados</li>
     * </ol>
     */
    private static void queryMultipleCities(Scanner scanner, WeatherService_Version3 weatherService) {
        System.out.print("\nIngresa los nombres de las ciudades (separadas por coma): ");
        String input = scanner.nextLine().trim();
        
        if (input.isEmpty()) {
            System.out.println("Error: Debes ingresar al menos una ciudad.");
            return;
        }
        
        String[] cities = input.split(",");
        if (cities.length > 10) {
            System.out.println("Se procesarán solo las primeras 10 ciudades.");
            cities = java.util.Arrays.copyOf(cities, 10);
        }
        
        System.out.println("\n¿Cómo deseas procesar las ciudades?");
        System.out.println("1. Secuencial (recomendado para pocas ciudades)");
        System.out.println("2. Paralelo (recomendado para muchas ciudades)");
        System.out.print("Selecciona: ");
        
        String processingMode = scanner.nextLine().trim();
        
        try {
            System.out.println("\nProcesando " + cities.length + " ciudades...\n");
            
            WeatherRequest request = new WeatherRequest();
            for (String city : cities) {
                request.addCity(city);
            }
            
            long startTime = System.currentTimeMillis();
            WeatherResponse response;
            
            if ("2".equals(processingMode)) {
                response = weatherService.getWeatherForCitiesParallel(request);
            } else {
                response = weatherService.getWeatherForCities(request);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            displayMultipleWeather(response, duration);
            
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (WeatherFetchException e) {
            System.out.println("Error al procesar ciudades: " + e.getMessage());
        }
    }
    
    /**
     * Compara temperaturas entre múltiples ciudades.
     * 
     * <p>Muestra estadísticas como temperatura máxima, mínima y promedio.</p>
     */
    private static void compareTemperatures(Scanner scanner, WeatherService_Version3 weatherService) {
        System.out.print("\nIngresa ciudades para comparar (separadas por coma): ");
        String input = scanner.nextLine().trim();
        
        if (input.isEmpty()) {
            System.out.println("Error: Debes ingresar al menos dos ciudades.");
            return;
        }
        
        String[] cities = input.split(",");
        if (cities.length < 2) {
            System.out.println("Error: Debes ingresar al menos dos ciudades para comparar.");
            return;
        }
        
        try {
            System.out.println("\n📊 Comparando temperaturas...\n");
            
            WeatherRequest request = new WeatherRequest();
            for (String city : cities) {
                request.addCity(city);
            }
            
            WeatherResponse response = weatherService.getWeatherForCitiesParallel(request);
            
            if (response.hasWeather()) {
                displayTemperatureComparison(response);
            }
            
        } catch (WeatherFetchException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    /**
     * Muestra los datos meteorológicos de una sola ciudad.
     */
    private static void displayWeather(Weather weather) {
        System.out.println("====================================");
        System.out.println("║     CLIMA EN " + padCenter(weather.getCity().toUpperCase(), 28) + "║");
        System.out.println("====================================");
        System.out.println("País: " + weather.getCountry());
        System.out.println("Coordenadas: " + String.format("%.4f, %.4f", weather.getLatitude(), weather.getLongitude()));
        System.out.println("\nTemperatura: " + weather.getTemperature() + "°C");
        System.out.println("Humedad: " + weather.getHumidity() + "%");
        System.out.println("Velocidad del viento: " + weather.getWindSpeed() + " km/h");
        System.out.println("Condición: " + getWeatherDescription(weather.getWeatherCode()));
        System.out.println();
    }
    
    /**
     * Muestra datos meteorológicos de múltiples ciudades.
     * 
     * @param response respuesta con múltiples datos meteorológicos
     * @param durationMs tiempo de procesamiento en milisegundos
     */
    private static void displayMultipleWeather(WeatherResponse response, long durationMs) {
        System.out.println("╔============================================================╗");
        System.out.println("║             DATOS METEOROLÓGICOS OBTENIDOS                ║");
        System.out.println("╠============================================================╣");
        
        int count = 1;
        for (Weather weather : response.getAllWeather()) {
            System.out.println(String.format("║ %d. %-52s ║", count, weather.getCity() + " (" + weather.getCountry() + ")"));
            System.out.println(String.format("║    Temp.  %d°C | Hum. %d%% | Wind. %.1f km/h             ║", 
                    (int)weather.getTemperature(), weather.getHumidity(), weather.getWindSpeed()));
            count++;
        }
        
        System.out.println("╠============================================================╣");
        System.out.println(String.format("║ Ciudades procesadas: %d | Tiempo: %dms                      ║", 
                response.getWeatherCount(), durationMs));
        System.out.println("╚============================================================\n");
    }
    
    /**
     * Muestra comparación de temperaturas entre ciudades.
     */
    private static void displayTemperatureComparison(WeatherResponse response) {
        Weather hottest = response.getHottestWeather();
        Weather coldest = response.getColdestWeather();
        double avgTemp = response.getAverageTemperature();
        
        System.out.println("╔============================================================╗");
        System.out.println("║             COMPARACIÓN DE TEMPERATURAS                   ║");
        System.out.println("╠============================================================╣");
        System.out.println(String.format("║  Más calurosa: %-35s ║", 
                hottest.getCity() + " (" + hottest.getTemperature() + "°C)"));
        System.out.println(String.format("║   Más fría:    %-35s ║", 
                coldest.getCity() + " (" + coldest.getTemperature() + "°C)"));
        System.out.println(String.format("║  Temperatura promedio: %.1f°C                          ║", avgTemp));
        System.out.println(String.format("║  Ciudades analizadas: %d                                ║", response.getWeatherCount()));
        System.out.println("============================================================\n");
    }
    
    /**
     * Convierte código numérico de clima a descripción en español.
     */
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
    
    /**
     * Centra un texto dentro de un ancho específico.
     */
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
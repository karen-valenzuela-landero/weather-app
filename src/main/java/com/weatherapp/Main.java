package com.weatherapp;

import com.weatherapp.exception.GeocodingException;
import com.weatherapp.exception.WeatherFetchException;
import com.weatherapp.model.Weather;
import com.weatherapp.model.WeatherRequest;
import com.weatherapp.model.WeatherResponse;
import com.weatherapp.service.ApiClient;
import com.weatherapp.service.GeocodingService;
import com.weatherapp.service.WeatherService;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Aplicación principal del Clima - Versión con Caché.
 * 
 * <p>Versión mejorada con sistema de caché para reducir peticiones a APIs.</p>
 * 
 * @author Karen Valenzuela Landero
 * @version 3.0
 * @since 2026-04-16
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        try {
            // Configurar TTL del caché (30 minutos por defecto)
            long cacheTtl = 1800; // segundos
            
            System.out.println("\n  Inicializando aplicación...");
            System.out.println(" Caché configurado con TTL: 30 minutos");
            
            ApiClient apiClient = new ApiClient();
            GeocodingService geocodingService = new GeocodingService(apiClient);
            WeatherService weatherService = new WeatherService(apiClient, geocodingService, cacheTtl);
            
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
                            showCacheStats(weatherService);
                            break;
                        case 5:
                            manageCacheMenu(scanner, weatherService);
                            break;
                        case 6:
                            System.out.println("\n¡Hasta luego!");
                            break;
                        default:
                            System.out.println(" Opción inválida.");
                    }
                    
                } catch (NumberFormatException e) {
                    System.out.println(" Por favor ingresa un número válido.");
                }
                
            } while (option != 6);
            
        } catch (Exception e) {
            System.out.println(" Error inesperado: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
    
    private static void showMainMenu() {
        System.out.println("\n======================================");
        System.out.println("║   APLICACIÓN DEL CLIMA v3.0 (CON CACHÉ)");
        System.out.println("╠======================================╣");
        System.out.println("║  1. Consultar una ciudad             ║");
        System.out.println("║  2. Consultar múltiples ciudades     ║");
        System.out.println("║  3. Comparar temperaturas            ║");
        System.out.println("║  4. Ver estadísticas del caché       ║");
        System.out.println("║  5. Gestionar caché                  ║");
        System.out.println("║  6. Salir                            ║");
        System.out.println("========================================");
    }
    
    private static void querySingleCity(Scanner scanner, WeatherService weatherService) throws GeocodingException {
        System.out.print("\nIngresa el nombre de la ciudad: ");
        String cityName = scanner.nextLine().trim();
        
        if (cityName.isEmpty()) {
            System.out.println(" El nombre no puede estar vacío.");
            return;
        }
        
        try {
            System.out.println("\n Buscando ciudad...");
            System.out.println(" Obteniendo datos meteorológicos...\n");
            
            WeatherRequest request = new WeatherRequest();
            request.addCity(cityName);
            
            long startTime = System.currentTimeMillis();
            WeatherResponse response = weatherService.getWeatherForCities(request);
            long duration = System.currentTimeMillis() - startTime;
            
            if (response.hasWeather()) {
                Weather weather = response.getAllWeather().get(0);
                displayWeather(weather);
                System.out.println("  Tiempo: " + duration + "ms\n");
            }
            
        } catch (WeatherFetchException e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }
    
    private static void queryMultipleCities(Scanner scanner, WeatherService weatherService) {
        System.out.print("\nIngresa ciudades (separadas por coma): ");
        String input = scanner.nextLine().trim();
        
        if (input.isEmpty()) {
            System.out.println(" Debes ingresar al menos una ciudad.");
            return;
        }
        
        String[] cities = input.split(",");
        
        System.out.println("\n¿Cómo procesar?");
        System.out.println("1. Secuencial");
        System.out.println("2. Paralelo");
        System.out.print("Selecciona: ");
        
        String processingMode = scanner.nextLine().trim();
        
        try {
            System.out.println("\n Procesando " + cities.length + " ciudades...\n");
            
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
            
        } catch (WeatherFetchException e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }
    
    private static void compareTemperatures(Scanner scanner, WeatherService weatherService) {
        System.out.print("\nIngresa ciudades para comparar (separadas por coma): ");
        String input = scanner.nextLine().trim();
        
        if (input.isEmpty()) {
            System.out.println(" Debes ingresar al menos dos ciudades.");
            return;
        }
        
        String[] cities = input.split(",");
        if (cities.length < 2) {
            System.out.println(" Debes ingresar al menos dos ciudades.");
            return;
        }
        
        try {
            System.out.println("\n Comparando temperaturas...\n");
            
            WeatherRequest request = new WeatherRequest();
            for (String city : cities) {
                request.addCity(city);
            }
            
            WeatherResponse response = weatherService.getWeatherForCitiesParallel(request);
            
            if (response.hasWeather()) {
                displayTemperatureComparison(response);
            }
            
        } catch (WeatherFetchException e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }
    
    /**
     * Muestra estadísticas del caché.
     */
    private static void showCacheStats(WeatherService weatherService) {
        weatherService.getCache().printStats();
        weatherService.getCache().printDetailedEntries();
    }
    
    /**
     * Menú para gestionar el caché.
     */
    private static void manageCacheMenu(Scanner scanner, WeatherService weatherService) {
        System.out.println("\n=======================================");
        System.out.println("║    GESTIÓN DEL CACHÉ                 ║");
        System.out.println("╠======================================╣");
        System.out.println("║  1. Ver estadísticas                 ║");
        System.out.println("║  2. Ver entradas detalladas          ║");
        System.out.println("║  3. Limpiar entradas expiradas       ║");
        System.out.println("║  4. Vaciar caché completamente       ║");
        System.out.println("║  5. Reiniciar estadísticas           ║");
        System.out.println("║  6. Volver al menú principal         ║");
        System.out.println("========================================");
        System.out.print("Selecciona una opción: ");
        
        String option = scanner.nextLine().trim();
        
        switch (option) {
            case "1":
                weatherService.getCache().printStats();
                break;
            case "2":
                weatherService.getCache().printDetailedEntries();
                break;
            case "3":
                int removed = weatherService.getCache().removeExpired();
                System.out.println("\n Se eliminaron " + removed + " entradas expiradas\n");
                break;
            case "4":
                System.out.print("¿Estás seguro? (s/n): ");
                if ("s".equalsIgnoreCase(scanner.nextLine().trim())) {
                    weatherService.getCache().clear();
                    System.out.println(" Caché vaciado\n");
                }
                break;
            case "5":
                weatherService.getCache().resetStats();
                System.out.println(" Estadísticas reiniciadas\n");
                break;
            case "6":
                break;
            default:
                System.out.println(" Opción inválida.");
        }
    }
    
    private static void displayWeather(Weather weather) {
        System.out.println("======================================");
        System.out.println("║     CLIMA EN " + padCenter(weather.getCity().toUpperCase(), 28) + "║");
        System.out.println("======================================");
        System.out.println("País: " + weather.getCountry());
        System.out.println("Coordenadas: " + String.format("%.4f, %.4f", weather.getLatitude(), weather.getLongitude()));
        System.out.println("\n  Temperatura: " + weather.getTemperature() + "°C");
        System.out.println(" Humedad: " + weather.getHumidity() + "%");
        System.out.println(" Velocidad del viento: " + weather.getWindSpeed() + " km/h");
        System.out.println("  Condición: " + getWeatherDescription(weather.getWeatherCode()));
        System.out.println();
    }
    
    private static void displayMultipleWeather(WeatherResponse response, long durationMs) {
        System.out.println("===============================================================");
        System.out.println("║             DATOS METEOROLÓGICOS OBTENIDOS                  ║");
        System.out.println("╠=============================================================╣");
        
        int count = 1;
        for (Weather weather : response.getAllWeather()) {
            System.out.println(String.format("║ %d. %-52s ║", count, weather.getCity() + " (" + weather.getCountry() + ")"));
            System.out.println(String.format("║    Temp.  %d°C | Hum. %d%% | Wind. %.1f km/h             ║", 
                    (int)weather.getTemperature(), weather.getHumidity(), weather.getWindSpeed()));
            count++;
        }
        
        System.out.println("╠=============================================================╣");
        System.out.println(String.format("║ Ciudades: %d | Tiempo: %dms                                    ║", 
                response.getWeatherCount(), durationMs));
        System.out.println("===============================================================\n");
    }
    
    private static void displayTemperatureComparison(WeatherResponse response) {
        Weather hottest = response.getHottestWeather();
        Weather coldest = response.getColdestWeather();
        double avgTemp = response.getAverageTemperature();
        
        System.out.println("===============================================================");
        System.out.println("║             COMPARACIÓN DE TEMPERATURAS                     ║");
        System.out.println("╠=============================================================╣");
        System.out.println(String.format("║  Más calurosa: %-35s ║", 
                hottest.getCity() + " (" + hottest.getTemperature() + "°C)"));
        System.out.println(String.format("║   Más fría:    %-35s ║", 
                coldest.getCity() + " (" + coldest.getTemperature() + "°C)"));
        System.out.println(String.format("║  Temperatura promedio: %.1f°C                          ║", avgTemp));
        System.out.println(String.format("║  Ciudades analizadas: %d                                ║", response.getWeatherCount()));
        System.out.println("===============================================================\n");
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
package com.weatherapp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.weatherapp.model.Weather;

public class Main_Version2 
{
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Aplicación del Clima ===");
        System.out.print("Ingresa el nombre de la ciudad: ");
        String cityName = scanner.nextLine().trim();
        
        if (cityName.isEmpty()) {
            System.out.println("Error: El nombre de la ciudad no puede estar vacío.");
            scanner.close();
            return;
        }
        
        try {
            Weather weather = getWeather(cityName);
            displayWeather(weather, cityName);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        scanner.close();
    }
    
    private static Weather getWeather(String cityName) throws Exception {
        // Paso 1: Obtener coordenadas de la ciudad
        String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" 
                      + cityName.replace(" ", "%20") + "&count=1&language=es&format=json";
        
        JsonObject geoResponse = makeRequest(geoUrl);
        
        if (!geoResponse.has("results") || geoResponse.getAsJsonArray("results").size() == 0) {
            throw new Exception("Ciudad no encontrada: " + cityName);
        }
        
        JsonObject result = geoResponse.getAsJsonArray("results").get(0).getAsJsonObject();
        double latitude = result.get("latitude").getAsDouble();
        double longitude = result.get("longitude").getAsDouble();
        String cityDisplayName = result.get("name").getAsString();
        String country = result.has("country") ? result.get("country").getAsString() : "";
        
        // Paso 2: Obtener datos meteorológicos
        String weatherUrl = String.format(
            "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&current=temperature_2m,weather_code,relative_humidity_2m,wind_speed_10m&timezone=auto",
            latitude, longitude
        );
        
        JsonObject weatherResponse = makeRequest(weatherUrl);
        JsonObject current = weatherResponse.getAsJsonObject("current");
        
        Weather weatherData = new Weather();
        weatherData.setCity(cityDisplayName);
        weatherData.setCountry(country);
        weatherData.setLatitude(latitude);
        weatherData.setLongitude(longitude);
        weatherData.setTemperature(current.get("temperature_2m").getAsDouble());
        weatherData.setWeatherCode(current.get("weather_code").getAsInt());
        weatherData.setHumidity(current.get("relative_humidity_2m").getAsInt());
        weatherData.setWindSpeed(current.get("wind_speed_10m").getAsDouble());
        
        return weatherData;
    }
    
    private static JsonObject makeRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Error en la petición HTTP: " + responseCode);
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        
        reader.close();
        connection.disconnect();
        
        return JsonParser.parseString(response.toString()).getAsJsonObject();
    }
    
    private static void displayWeather(Weather weather, String cityName) {
        System.out.println("\n====================================");
        System.out.println("║     CLIMA EN " + padCenter(weather.getCity().toUpperCase(), 28) + "║");
        System.out.println("====================================");
        System.out.println("País: " + weather.getCountry());
        System.out.println("Coordenadas: " + String.format("%.4f, %.4f", weather.getLatitude(), weather.getLongitude()));
        System.out.println("\n Temperatura: " + weather.getTemperature() + "°C");
        System.out.println(" Humedad: " + weather.getHumidity() + "%");
        System.out.println(" Velocidad del viento: " + weather.getWindSpeed() + " km/h");
        System.out.println("  Código de clima: " + getWeatherDescription(weather.getWeatherCode()));
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

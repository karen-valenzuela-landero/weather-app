package com.weatherapp.util;

/**
 * constantes a utlizar en la app.
 */
public class Constants {
    // URLs de las APIs
    public static final String GEOCODING_API_URL = 
        "https://geocoding-api.open-meteo.com/v1/search";
    public static final String WEATHER_API_URL = 
        "https://api.open-meteo.com/v1/forecast";
    
    // Parámetros
    public static final int CONNECTION_TIMEOUT = 5000;
    public static final int READ_TIMEOUT = 5000;
    public static final int MAX_RESULTS = 1;
    
    // Validación de rangos
    public static final double MIN_TEMPERATURE = -50.0;
    public static final double MAX_TEMPERATURE = 50.0;
    public static final int MIN_HUMIDITY = 0;
    public static final int MAX_HUMIDITY = 100;
    public static final double MIN_WIND_SPEED = 0.0;
    
    // Idioma
    public static final String LANGUAGE = "es";
}
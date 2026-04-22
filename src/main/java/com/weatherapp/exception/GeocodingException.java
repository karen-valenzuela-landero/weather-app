package com.weatherapp.exception;

public class GeocodingException extends WeatherAppException {
    public GeocodingException(String message) {
        super(message);
    }

    public GeocodingException(String message, Throwable cause) {
        super(message, cause);
    }
   

    
}
package com.weatherapp.exception;

public class WeatherAppException extends Exception {
    public WeatherAppException(String message) {
        super(message);
    }
    
    public WeatherAppException(String message, Throwable cause) {
        super(message, cause);
    }
}
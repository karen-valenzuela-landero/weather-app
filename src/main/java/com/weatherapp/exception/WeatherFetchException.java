package com.weatherapp.exception;

public class WeatherFetchException extends WeatherAppException {
    public WeatherFetchException(String message) {
        super(message);
    }
    
    public WeatherFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
package com.weatherapp.model;

/**
 * Representa una ubicación geográfica
 */
public class Location {
    private String city;
    private String country;
    private double latitude;
    private double longitude;
    
    public Location(String city, String country, double latitude, double longitude) {
        this.city = city;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // Getters
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    
    @Override
    public String toString() {
        return city + ", " + country + " (" + latitude + ", " + longitude + ")";
    }
}
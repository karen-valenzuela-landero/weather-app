package com.weatherapp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una solicitud de datos meteorológicos para una o más ciudades.
 * 
 * <p>Esta clase encapsula una lista de ciudades para las cuales se desea
 * obtener datos meteorológicos. Puede contener una o más ciudades y valida
 * que al menos una esté presente.</p>
 * 
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>
 * {@code
 * WeatherRequest request = new WeatherRequest();
 * request.addCity("Madrid");
 * request.addCity("Barcelona");
 * request.addCity("Valencia");
 * 
 * System.out.println("Ciudades a consultar: " + request.getCities());
 * }
 * </pre>
 * 
 * @author Karen Valenzuela Landero
 * @version 1.0
 * @since 2026-04-16
 * @see Weather
 */
public class WeatherRequest {
    private List<String> cities;
    
    /**
     * Constructor que inicializa una solicitud vacía.
     * 
     * <p>La lista de ciudades se inicializa vacía y puede poblarse
     * usando el método {@link #addCity(String)}.</p>
     */
    public WeatherRequest() {
        this.cities = new ArrayList<>();
    }
    
    /**
     * Constructor que inicializa con una lista de ciudades.
     * 
     * @param cities lista inicial de ciudades
     *               no puede ser null
     * 
     * @throws IllegalArgumentException si cities es null
     */
    public WeatherRequest(List<String> cities) {
        if (cities == null) {
            throw new IllegalArgumentException("La lista de ciudades no puede ser null");
        }
        this.cities = new ArrayList<>(cities);
    }
    
    /**
     * Agrega una ciudad a la solicitud.
     * 
     * <p>Valida que el nombre de la ciudad no esté vacío antes de agregarlo.
     * Los espacios en blanco se eliminan automáticamente.</p>
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * WeatherRequest request = new WeatherRequest();
     * request.addCity("Madrid");
     * request.addCity("  Barcelona  "); // Se trimea automáticamente
     * request.addCity("Valencia");
     * }
     * </pre>
     * 
     * @param city nombre de la ciudad a agregar
     *             no puede ser null ni estar vacío
     * 
     * @throws IllegalArgumentException si city es null o está vacío
     * 
     * @return {@code true} si la ciudad fue agregada exitosamente
     */
    public boolean addCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la ciudad no puede ser null ni estar vacío");
        }
        return this.cities.add(city.trim());
    }
    
    /**
     * Agrega múltiples ciudades a la solicitud.
     * 
     * <p>Útil para agregar varias ciudades de una sola vez.</p>
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * List<String> ciudadesEspañolas = Arrays.asList("Madrid", "Barcelona", "Valencia");
     * WeatherRequest request = new WeatherRequest();
     * request.addCities(ciudadesEspañolas);
     * }
     * </pre>
     * 
     * @param citiesList lista de ciudades a agregar
     *                   no puede ser null
     * 
     * @throws IllegalArgumentException si citiesList es null o contiene valores vacíos
     * 
     * @return {@code true} si al menos una ciudad fue agregada
     */
    public boolean addCities(List<String> citiesList) {
        if (citiesList == null) {
            throw new IllegalArgumentException("La lista de ciudades no puede ser null");
        }
        return citiesList.stream()
                .peek(city -> {
                    if (city == null || city.trim().isEmpty()) {
                        throw new IllegalArgumentException("El nombre de la ciudad no puede ser null ni estar vacío");
                    }
                })
                .map(String::trim)
                .allMatch(this.cities::add);
    }
    
    /**
     * Obtiene la lista de ciudades solicitadas.
     * 
     * @return lista inmutable de ciudades
     */
    public List<String> getCities() {
        return new ArrayList<>(this.cities);
    }
    
    /**
     * Obtiene el número de ciudades en la solicitud.
     * 
     * @return cantidad de ciudades
     */
    public int getCityCount() {
        return this.cities.size();
    }
    
    /**
     * Verifica si la solicitud tiene al menos una ciudad.
     * 
     * @return {@code true} si hay al menos una ciudad
     */
    public boolean hasCities() {
        return !this.cities.isEmpty();
    }
    
    /**
     * Verifica si una ciudad específica está en la solicitud.
     * 
     * @param city nombre de la ciudad a verificar
     * @return {@code true} si la ciudad está en la solicitud
     */
    public boolean containsCity(String city) {
        return this.cities.contains(city);
    }
    
    /**
     * Limpia todas las ciudades de la solicitud.
     */
    public void clearCities() {
        this.cities.clear();
    }
    
    @Override
    public String toString() {
        return "WeatherRequest{" +
                "cities=" + cities +
                '}';
    }
}
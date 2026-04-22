package com.weatherapp.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Representa la respuesta con datos meteorológicos para una o más ciudades.
 * 
 * <p>Esta clase contiene una lista de datos meteorológicos, cada uno
 * correspondiente a una ciudad solicitada. Proporciona métodos de acceso
 * y búsqueda convenientes.</p>
 * 
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>
 * {@code
 * WeatherResponse response = new WeatherResponse();
 * response.addWeather(weatherMadrid);
 * response.addWeather(weatherBarcelona);
 * response.addWeather(weatherValencia);
 * 
 * System.out.println("Ciudades obtenidas: " + response.getWeatherCount());
 * List<Weather> allWeather = response.getAllWeather();
 * }
 * </pre>
 * 
 * @author Karen Valenzuela Landero
 * @version 1.0
 * @since 2026-04-16
 * @see Weather
 */
public class WeatherResponse {
    private List<Weather> weatherList;
    private long timestamp;
    
    /**
     * Constructor que inicializa una respuesta vacía.
     * 
     * <p>La lista de datos meteorológicos se inicializa vacía.
     * El timestamp se registra al momento de creación.</p>
     */
    public WeatherResponse() {
        this.weatherList = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Agrega datos meteorológicos a la respuesta.
     * 
     * <p>Valida que el objeto Weather no sea null antes de agregarlo.</p>
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * WeatherResponse response = new WeatherResponse();
     * response.addWeather(weatherMadrid);
     * response.addWeather(weatherBarcelona);
     * }
     * </pre>
     * 
     * @param weather los datos meteorológicos a agregar
     *                no puede ser null
     * 
     * @throws IllegalArgumentException si weather es null
     * 
     * @return {@code true} si fue agregado exitosamente
     */
    public boolean addWeather(Weather weather) {
        if (weather == null) {
            throw new IllegalArgumentException("Los datos meteorológicos no pueden ser null");
        }
        return this.weatherList.add(weather);
    }
    
    /**
     * Agrega múltiples datos meteorológicos a la respuesta.
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * List<Weather> allWeather = Arrays.asList(weather1, weather2, weather3);
     * response.addAllWeather(allWeather);
     * }
     * </pre>
     * 
     * @param weatherList lista de datos meteorológicos a agregar
     *                    no puede ser null
     * 
     * @throws IllegalArgumentException si weatherList es null
     * 
     * @return {@code true} si al menos uno fue agregado
     */
    public boolean addAllWeather(List<Weather> weatherList) {
        if (weatherList == null) {
            throw new IllegalArgumentException("La lista de datos meteorológicos no puede ser null");
        }
        return this.weatherList.addAll(weatherList);
    }
    
    /**
     * Obtiene todos los datos meteorológicos de la respuesta.
     * 
     * @return lista inmutable de datos meteorológicos
     */
    public List<Weather> getAllWeather() {
        return Collections.unmodifiableList(this.weatherList);
    }
    
    /**
     * Obtiene datos meteorológicos de una ciudad específica.
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * Weather madrid = response.getWeatherByCity("Madrid");
     * if (madrid != null) {
     *     System.out.println("Temperatura en Madrid: " + madrid.getTemperature() + "°C");
     * }
     * }
     * </pre>
     * 
     * @param cityName nombre de la ciudad a buscar
     * 
     * @return los datos meteorológicos si se encuentra, null en caso contrario
     */
    public Weather getWeatherByCity(String cityName) {
        return this.weatherList.stream()
                .filter(w -> w.getCity().equalsIgnoreCase(cityName))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Obtiene todos los datos meteorológicos de un país específico.
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * List<Weather> españoles = response.getWeatherByCountry("España");
     * for (Weather w : españoles) {
     *     System.out.println(w.getCity() + ": " + w.getTemperature() + "°C");
     * }
     * }
     * </pre>
     * 
     * @param countryName nombre del país a filtrar
     * 
     * @return lista de datos meteorológicos del país
     */
    public List<Weather> getWeatherByCountry(String countryName) {
        return this.weatherList.stream()
                .filter(w -> w.getCountry().equalsIgnoreCase(countryName))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene el dato meteorológico con la temperatura más alta.
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * Weather hottest = response.getHottestWeather();
     * System.out.println("La ciudad más calurosa es " + hottest.getCity() + 
     *                    " con " + hottest.getTemperature() + "°C");
     * }
     * </pre>
     * 
     * @return el dato meteorológico con temperatura más alta
     *         null si la lista está vacía
     */
    public Weather getHottestWeather() {
        return this.weatherList.stream()
                .max((w1, w2) -> Double.compare(w1.getTemperature(), w2.getTemperature()))
                .orElse(null);
    }
    
    /**
     * Obtiene el dato meteorológico con la temperatura más baja.
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * Weather coldest = response.getColdestWeather();
     * System.out.println("La ciudad más fría es " + coldest.getCity() + 
     *                    " con " + coldest.getTemperature() + "°C");
     * }
     * </pre>
     * 
     * @return el dato meteorológico con temperatura más baja
     *         null si la lista está vacía
     */
    public Weather getColdestWeather() {
        return this.weatherList.stream()
                .min((w1, w2) -> Double.compare(w1.getTemperature(), w2.getTemperature()))
                .orElse(null);
    }
    
    /**
     * Obtiene la temperatura promedio de todas las ciudades.
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * double avgTemp = response.getAverageTemperature();
     * System.out.println("Temperatura promedio: " + String.format("%.1f", avgTemp) + "°C");
     * }
     * </pre>
     * 
     * @return la temperatura promedio, 0 si la lista está vacía
     */
    public double getAverageTemperature() {
        return this.weatherList.stream()
                .mapToDouble(Weather::getTemperature)
                .average()
                .orElse(0.0);
    }
    
    /**
     * Obtiene el número de datos meteorológicos en la respuesta.
     * 
     * @return cantidad de ciudades con datos
     */
    public int getWeatherCount() {
        return this.weatherList.size();
    }
    
    /**
     * Verifica si la respuesta tiene datos meteorológicos.
     * 
     * @return {@code true} si hay al menos un dato meteorológico
     */
    public boolean hasWeather() {
        return !this.weatherList.isEmpty();
    }
    
    /**
     * Obtiene el timestamp de creación de la respuesta.
     * 
     * @return timestamp en milisegundos
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Limpia todos los datos meteorológicos de la respuesta.
     */
    public void clear() {
        this.weatherList.clear();
    }
    
    @Override
    public String toString() {
        return "WeatherResponse{" +
                "weatherCount=" + weatherList.size() +
                ", timestamp=" + timestamp +
                '}';
    }
}
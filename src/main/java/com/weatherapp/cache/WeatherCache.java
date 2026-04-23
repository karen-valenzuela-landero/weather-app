package com.weatherapp.cache;

import com.weatherapp.model.Weather;
import com.weatherapp.model.WeatherResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Gestor de caché para datos meteorológicos con expiración automática.
 * 
 * <p>Este clase proporciona funciones de caché thread-safe para almacenar
 * datos meteorológicos durante un período de tiempo configurable. Soporta:</p>
 * <ul>
 *   <li>Almacenamiento de datos por ciudad</li>
 *   <li>Expiración automática basada en TTL</li>
 *   <li>Limpieza automática de datos expirados</li>
 *   <li>Estadísticas de uso del caché</li>
 *   <li>Thread-safety con ConcurrentHashMap</li>
 * </ul>
 * 
 * <p><strong>Ejemplo de uso básico:</strong></p>
 * <pre>
 * {@code
 * // Crear caché con 30 minutos de TTL
 * WeatherCache cache = new WeatherCache(1800);
 * 
 * // Almacenar datos
 * Weather weather = new Weather();
 * weather.setCity("Madrid");
 * weather.setTemperature(22.5);
 * cache.put("Madrid", weather);
 * 
 * // Recuperar datos
 * Weather cached = cache.get("Madrid");
 * if (cached != null) {
 *     System.out.println("Datos en caché: " + cached.getTemperature());
 * }
 * }
 * </pre>
 * 
 * <p><strong>Ejemplo con múltiples ciudades:</strong></p>
 * <pre>
 * {@code
 * WeatherCache cache = new WeatherCache(1800);
 *
 * // Almacenar múltiples ciudades
 * cache.putAll(List.of(madrid, barcelona, valencia));
 * 
 * // Recuperar caché completo
 * List<Weather> cached = cache.getAll();
 * System.out.println("Ciudades en caché: " + cached.size());
 * 
 * // Ver estadísticas
 * cache.printStats();
 * }
 * </pre>
 * 
 * @author Karen Valenzuela Landero
 * @version 1.0
 * @since 2026-04-16
 * @see CacheEntry
 * @see Weather
 */
public class WeatherCache {
    private static final Logger logger = Logger.getLogger(WeatherCache.class.getName());
    
    private final Map<String, CacheEntry<Weather>> cache;
    private final long defaultTtlSeconds;
    private long hits = 0;
    private long misses = 0;
    
    /**
     * Crea un gestor de caché con TTL por defecto.
     * 
     * <p><strong>TTLs recomendados:</strong></p>
     * <ul>
     *   <li>300 segundos (5 min): Datos frecuentemente actualizados</li>
     *   <li>900 segundos (15 min): Actualizaciones regulares</li>
     *   <li>1800 segundos (30 min): Uso general (recomendado)</li>
     *   <li>3600 segundos (1 hora): Datos relativamente estables</li>
     * </ul>
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * // Caché con 30 minutos de TTL
     * WeatherCache cache = new WeatherCache(1800);
     * }
     * </pre>
     * 
     * @param ttlSeconds tiempo de vida útil en segundos para todas las entradas
     *                   debe ser > 0
     * 
     * @throws IllegalArgumentException si ttlSeconds <= 0
     */
    public WeatherCache(long ttlSeconds) {
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("TTL debe ser mayor a 0 segundos");
        }
        this.cache = new ConcurrentHashMap<>();
        this.defaultTtlSeconds = ttlSeconds;
        logger.info("Caché inicializado con TTL: " + ttlSeconds + "s (" + 
                   formatTime(ttlSeconds) + ")");
    }
    
    /**
     * Almacena datos meteorológicos en el caché.
     * 
     * <p>Reemplaza cualquier entrada existente para la misma ciudad.
     * Usa el TTL por defecto configurado en el constructor.</p>
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * cache.put("Madrid", weather);
     * logger.info("Almacenado en caché: Madrid");
     * }
     * </pre>
     * 
     * @param cityName clave del caché (nombre de la ciudad)
     *                 no puede ser null ni estar vacío
     * 
     * @param weather datos meteorológicos a almacenar
     *                no puede ser null
     * 
     * @throws IllegalArgumentException si cityName es null/vacío o weather es null
     */
    public void put(String cityName, Weather weather) {
        if (cityName == null || cityName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la ciudad no puede ser null ni vacío");
        }
        if (weather == null) {
            throw new IllegalArgumentException("Los datos meteorológicos no pueden ser null");
        }
        
        String key = cityName.toLowerCase();
        cache.put(key, new CacheEntry<>(weather, defaultTtlSeconds));
        logger.info("✓ Almacenado en caché: " + cityName + " (TTL: " + defaultTtlSeconds + "s)");
    }
    
    /**
     * Almacena múltiples datos meteorológicos en el caché.
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * List<Weather> allWeather = Arrays.asList(madrid, barcelona, valencia);
     * cache.putAll(allWeather);
     * logger.info("Almacenadas " + allWeather.size() + " ciudades en caché");
     * }
     * </pre>
     * 
     * @param weatherList lista de datos meteorológicos a almacenar
     *                    no puede ser null
     * 
     * @throws IllegalArgumentException si weatherList es null
     */
    public void putAll(List<Weather> weatherList) {
        if (weatherList == null) {
            throw new IllegalArgumentException("La lista no puede ser null");
        }
        
        weatherList.forEach(weather -> {
            if (weather != null) {
                put(weather.getCity(), weather);
            }
        });
        
        logger.info("✓ Almacenadas " + weatherList.size() + " ciudades en caché");
    }
    
    /**
     * Recupera datos meteorológicos del caché.
     * 
     * <p>Verifica si la entrada ha expirado. Si está expirada, la elimina
     * automáticamente y retorna null.</p>
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * Weather cached = cache.get("Madrid");
     * if (cached != null) {
     *     System.out.println("Datos válidos en caché");
     * } else {
     *     System.out.println("No hay datos o están expirados");
     * }
     * }
     * </pre>
     * 
     * @param cityName nombre de la ciudad a recuperar
     * 
     * @return datos meteorológicos si existen y no han expirado, null en caso contrario
     */
    public Weather get(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            return null;
        }
        
        String key = cityName.toLowerCase();
        CacheEntry<Weather> entry = cache.get(key);
        
        if (entry == null) {
            misses++;
            logger.fine(" Cache MISS: " + cityName);
            return null;
        }
        
        if (entry.isExpired()) {
            cache.remove(key);
            misses++;
            logger.fine(" Cache EXPIRADO: " + cityName + " (edad: " + entry.getAgeSeconds() + "s)");
            return null;
        }
        
        hits++;
        logger.fine(" Cache HIT: " + cityName + " (edad: " + entry.getAgeSeconds() + "s)");
        return entry.getValue();
    }
    
    /**
     * Recupera todos los datos meteorológicos válidos del caché.
     * 
     * <p>Elimina automáticamente las entradas expiradas durante el proceso.</p>
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * List<Weather> allCached = cache.getAll();
     * System.out.println("Ciudades en caché: " + allCached.size());
     * 
     * for (Weather w : allCached) {
     *     System.out.println(w.getCity() + ": " + w.getTemperature() + "°C");
     * }
     * }
     * </pre>
     * 
     * @return lista de datos meteorológicos válidos (no expirados)
     *         retorna lista vacía si no hay datos válidos
     */
    public List<Weather> getAll() {
        return cache.entrySet().stream()
                .filter(entry -> !entry.getValue().isExpired())
                .map(entry -> entry.getValue().getValue())
                .collect(Collectors.toList());
    }
    
    /**
     * Verifica si un dato existe en el caché y es válido.
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * if (cache.contains("Madrid")) {
     *     System.out.println("Madrid está en caché");
     * }
     * }
     * </pre>
     * 
     * @param cityName nombre de la ciudad a verificar
     * 
     * @return {@code true} si existe y no está expirado
     */
    public boolean contains(String cityName) {
        return get(cityName) != null;
    }
    
    /**
     * Elimina un dato específico del caché.
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * cache.remove("Madrid");
     * System.out.println("Madrid eliminado del caché");
     * }
     * </pre>
     * 
     * @param cityName nombre de la ciudad a eliminar
     * 
     * @return {@code true} si fue eliminado, {@code false} si no existía
     */
    public boolean remove(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            return false;
        }
        
        String key = cityName.toLowerCase();
        boolean removed = cache.remove(key) != null;
        
        if (removed) {
            logger.info("✓ Eliminado del caché: " + cityName);
        }
        
        return removed;
    }
    
    /**
     * Limpia todos los datos del caché.
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * cache.clear();
     * System.out.println("Caché vaciado");
     * }
     * </pre>
     */
    public void clear() {
        int sizeBefore = cache.size();
        cache.clear();
        logger.info("✓ Caché vaciado (" + sizeBefore + " entradas eliminadas)");
    }
    
    /**
     * Limpia únicamente las entradas expiradas.
     * 
     * <p>Este método es útil para liberar memoria sin perder datos válidos.</p>
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * int removed = cache.removeExpired();
     * System.out.println("Eliminadas " + removed + " entradas expiradas");
     * }
     * </pre>
     * 
     * @return número de entradas expiradas eliminadas
     */
    public int removeExpired() {
        List<String> expiredKeys = cache.entrySet().stream()
                .filter(entry -> entry.getValue().isExpired())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        expiredKeys.forEach(cache::remove);
        
        logger.info("✓ Eliminadas " + expiredKeys.size() + " entradas expiradas del caché");
        
        return expiredKeys.size();
    }
    
    /**
     * Obtiene el número total de entradas en el caché (incluyendo expiradas).
     * 
     * @return cantidad de entradas
     */
    public int size() {
        return cache.size();
    }
    
    /**
     * Obtiene el número de entradas válidas (no expiradas).
     * 
     * @return cantidad de entradas válidas
     */
    public int getValidSize() {
        return (int) cache.values().stream()
                .filter(entry -> !entry.isExpired())
                .count();
    }
    
    /**
     * Verifica si el caché está vacío.
     * 
     * @return {@code true} si no hay entradas
     */
    public boolean isEmpty() {
        return cache.isEmpty();
    }
    
    /**
     * Calcula la tasa de aciertos del caché.
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * double hitRate = cache.getHitRate();
     * System.out.println("Tasa de aciertos: " + String.format("%.1f%%", hitRate));
     * }
     * </pre>
     * 
     * @return porcentaje de aciertos (0.0 a 100.0)
     */
    public double getHitRate() {
        if (hits + misses == 0) {
            return 0.0;
        }
        return (hits * 100.0) / (hits + misses);
    }
    
    /**
     * Obtiene el número total de accesos exitosos.
     * 
     * @return contador de hits
     */
    public long getHits() {
        return hits;
    }
    
    /**
     * Obtiene el número total de accesos fallidos.
     * 
     * @return contador de misses
     */
    public long getMisses() {
        return misses;
    }
    
    /**
     * Reinicia los contadores de estadísticas.
     */
    public void resetStats() {
        hits = 0;
        misses = 0;
        logger.info("✓ Estadísticas del caché reiniciadas");
    }
    
    /**
     * Obtiene información detallada sobre una entrada del caché.
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * String info = cache.getEntryInfo("Madrid");
     * System.out.println(info);
     * // Output: Madrid - Edad: 120s, TTL: 1800s, Expiración: 1680s, Uso: 6%
     * }
     * </pre>
     * 
     * @param cityName nombre de la ciudad
     * 
     * @return información formateada de la entrada, null si no existe
     */
    public String getEntryInfo(String cityName) {
        String key = cityName.toLowerCase();
        CacheEntry<Weather> entry = cache.get(key);
        
        if (entry == null) {
            return null;
        }
        
        return String.format(
            "%s - Edad: %ds, TTL: %ds, Expiración en: %ds, Uso: %d%%",
            cityName,
            entry.getAgeSeconds(),
            entry.getTtlSeconds(),
            entry.getSecondsUntilExpiration(),
            entry.getUsagePercentage()
        );
    }
    
    /**
     * Imprime estadísticas completas del caché en la consola.
     * 
     * <p><strong>Ejemplo de salida:</strong></p>
     * <pre>
     * {@code
     * 
     * ║    ESTADISTICAS DEL CACHE             ║
     * 
     * ║ Entradas totales: 5                   ║
     * ║ Entradas válidas: 4                   ║
     * ║ Entradas expiradas: 1                 ║
     * ║ Accesos exitosos (hits): 23           ║
     * ║ Accesos fallidos (misses): 7          ║
     * ║ Tasa de aciertos: 76.7%               ║
     * ║ TTL por defecto: 30 minutos           ║
     * ║ Memoria aproximada: 12.5 KB           ║
     * 
     * }
     * </pre>
     */
    public void printStats() {
        int totalEntries = cache.size();
        int validEntries = getValidSize();
        int expiredEntries = totalEntries - validEntries;
        double hitRate = getHitRate();
        long approximateMemory = estimateMemoryUsage();
        
        System.out.println("\n=========================================");
        System.out.println("║    ESTADISTICAS DEL CACHE               ║");
        System.out.println("╠=========================================╣");
        System.out.println(String.format("║ Entradas totales: %-24d ║", totalEntries));
        System.out.println(String.format("║ Entradas válidas: %-24d ║", validEntries));
        System.out.println(String.format("║ Entradas expiradas: %-21d ║", expiredEntries));
        System.out.println(String.format("║ Accesos exitosos (hits): %-16d ║", hits));
        System.out.println(String.format("║ Accesos fallidos (misses): %-15d ║", misses));
        System.out.println(String.format("║ Tasa de aciertos: %-22.1f%% ║", hitRate));
        System.out.println(String.format("║ TTL por defecto: %-23s ║", formatTime(defaultTtlSeconds)));
        System.out.println(String.format("║ Memoria aproximada: %-19s ║", formatBytes(approximateMemory)));
        System.out.println("=========================================\n");
    }
    
    /**
     * Imprime información detallada de todas las entradas en el caché.
     * 
     * <p><strong>Ejemplo de salida:</strong></p>
     * <pre>
     * {@code
     * 
     * ║              ENTRADAS EN CACHÉ DETALLADAS                            ║
     * 
     * ║ 1. Madrid - Edad: 45s, TTL: 1800s, Expiración en: 1755s, Uso: 2%   ║
     * ║ 2. Barcelona - Edad: 120s, TTL: 1800s, Expiración en: 1680s, Uso: 6% ║
     * ║ 3. Valencia - Edad: 300s, TTL: 1800s, Expiración en: 1500s, Uso: 16% ║
     * 
     * }
     * </pre>
     */
    public void printDetailedEntries() {
        System.out.println("\n==============================================================");
        System.out.println("║              ENTRADAS EN CACHÉ DETALLADAS                   ║");
        System.out.println("╠=============================================================╣");
        
        int counter = 1;
        for (Map.Entry<String, CacheEntry<Weather>> entry : cache.entrySet()) {
            String info = getEntryInfo(entry.getValue().getValue().getCity());
            String expired = entry.getValue().isExpired() ? "  EXPIRADO" : "";
            System.out.println(String.format("║ %d. %s%s", counter, info, expired));
            counter++;
        }
        
        if (cache.isEmpty()) {
            System.out.println("║ (Caché vacío)                                                         ║");
        }
        
        System.out.println("=========================================================================\n");
    }
    
    /**
     * Formatea segundos a formato legible.
     * 
     * @param seconds segundos a formatear
     * @return cadena formateada (ej: "30 minutos", "2 horas")
     */
    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + " segundos";
        } else if (seconds < 3600) {
            return (seconds / 60) + " minutos";
        } else if (seconds < 86400) {
            return (seconds / 3600) + " horas";
        } else {
            return (seconds / 86400) + " días";
        }
    }
    
    /**
     * Formatea bytes a formato legible.
     * 
     * @param bytes bytes a formatear
     * @return cadena formateada (ej: "12.5 KB", "1.2 MB")
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
    
    /**
     * Estima el uso de memoria del caché.
     * 
     * @return bytes aproximados de memoria usada
     */
    private long estimateMemoryUsage() {
        // Estimación aproximada: 100 bytes por entrada + tamaño de objetos
        return cache.size() * 150;
    }
}
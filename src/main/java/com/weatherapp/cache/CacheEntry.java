package com.weatherapp.cache;

/**
 * Representa una entrada en el caché con información de expiración.
 * 
 * <p>Esta clase almacena datos junto con un timestamp de creación para
 * permitir la expiración automática basada en un tiempo de vida útil (TTL).</p>
 * 
 * <p><strong>Características:</strong></p>
 * <ul>
 *   <li>Almacena cualquier tipo de objeto</li>
 *   <li>Registra timestamp de creación</li>
 *   <li>Permite verificar si ha expirado</li>
 *   <li>Calcula edad de la entrada</li>
 * </ul>
 * 
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>
 * {@code
 * Weather weather = new Weather();
 * CacheEntry<Weather> entry = new CacheEntry<>(weather, 3600); // 1 hora
 * 
 * if (!entry.isExpired()) {
 *     Weather cached = entry.getValue();
 * }
 * }
 * </pre>
 * 
 * @param <T> tipo de objeto almacenado en el caché
 * 
 * @author Karen Valenzuela Landero
 * @version 1.0
 * @since 2026-04-16
 */
public class CacheEntry<T> {
    private final T value;
    private final long createdAt;
    private final long ttlSeconds; // Time To Live en segundos
    
    /**
     * Crea una entrada de caché con valor y TTL.
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * // Caché que expira en 30 minutos
     * CacheEntry<Weather> entry = new CacheEntry<>(weather, 1800);
     * }
     * </pre>
     * 
     * @param value el valor a almacenar en caché
     *              puede ser null
     * 
     * @param ttlSeconds tiempo de vida útil en segundos
     *                   debe ser > 0
     * 
     * @throws IllegalArgumentException si ttlSeconds <= 0
     */
    public CacheEntry(T value, long ttlSeconds) {
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("TTL debe ser mayor a 0 segundos");
        }
        this.value = value;
        this.ttlSeconds = ttlSeconds;
        this.createdAt = System.currentTimeMillis();
    }
    
    /**
     * Obtiene el valor almacenado en la entrada.
     * 
     * @return el valor almacenado (puede ser null)
     */
    public T getValue() {
        return value;
    }
    
    /**
     * Obtiene el timestamp de creación de la entrada.
     * 
     * @return timestamp en milisegundos
     */
    public long getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Obtiene el TTL configurado para esta entrada.
     * 
     * @return TTL en segundos
     */
    public long getTtlSeconds() {
        return ttlSeconds;
    }
    
    /**
     * Verifica si la entrada ha expirado.
     * 
     * <p>Una entrada se considera expirada si el tiempo transcurrido desde
     * su creación es mayor o igual al TTL configurado.</p>
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * CacheEntry<Weather> entry = new CacheEntry<>(weather, 5); // 5 segundos
     * 
     * if (!entry.isExpired()) {
     *     System.out.println("Datos aún válidos");
     * } else {
     *     System.out.println("Datos expirados, necesita actualizar");
     * }
     * }
     * </pre>
     * 
     * @return {@code true} si la entrada ha expirado
     */
    public boolean isExpired() {
        long currentTime = System.currentTimeMillis();
        long elapsedSeconds = (currentTime - createdAt) / 1000;
        return elapsedSeconds >= ttlSeconds;
    }
    
    /**
     * Calcula cuánto tiempo falta para que expire la entrada.
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * long secondsLeft = entry.getSecondsUntilExpiration();
     * System.out.println("Expira en: " + secondsLeft + " segundos");
     * }
     * </pre>
     * 
     * @return segundos restantes hasta la expiración, 0 si ya expiró
     */
    public long getSecondsUntilExpiration() {
        long currentTime = System.currentTimeMillis();
        long elapsedSeconds = (currentTime - createdAt) / 1000;
        long remaining = ttlSeconds - elapsedSeconds;
        return Math.max(0, remaining);
    }
    
    /**
     * Obtiene la edad actual de la entrada.
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * long age = entry.getAgeSeconds();
     * System.out.println("La entrada tiene " + age + " segundos de edad");
     * }
     * </pre>
     * 
     * @return edad en segundos desde su creación
     */
    public long getAgeSeconds() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - createdAt) / 1000;
    }
    
    /**
     * Calcula el porcentaje de vida útil consumida.
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * {@code
     * int usage = entry.getUsagePercentage();
     * System.out.println("Caché al " + usage + "% de su vida útil");
     * }
     * </pre>
     * 
     * @return porcentaje de 0 a 100
     */
    public int getUsagePercentage() {
        long ageSeconds = getAgeSeconds();
        return (int) ((ageSeconds * 100) / ttlSeconds);
    }
    
    @Override
    public String toString() {
        return "CacheEntry{" +
                "value=" + value +
                ", age=" + getAgeSeconds() + "s" +
                ", ttl=" + ttlSeconds + "s" +
                ", expired=" + isExpired() +
                '}';
    }
}
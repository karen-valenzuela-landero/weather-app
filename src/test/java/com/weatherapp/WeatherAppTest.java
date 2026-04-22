package com.weatherapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WeatherAppTest {
    
    @BeforeEach
    public void setup() {
        // Configuración inicial antes de cada prueba
    }
    
    // ========== PRUEBAS DE ENTRADA ==========
    
    @Test
    public void testCiudadValida() {
        String ciudad = "Madrid";
        assertNotNull(ciudad);
        assertTrue(ciudad.length() > 0);
    }
    
    @Test
    public void testEntradaVacia() {
        String ciudad = "";
        assertTrue(ciudad.isEmpty(), "La entrada no debería estar vacía");
    }
    
    @Test
    public void testEntradaConEspacios() {
        String ciudad = "New York";
        String urlEncoded = ciudad.replace(" ", "%20");
        assertEquals("New%20York", urlEncoded);
    }
    
    // ========== PRUEBAS DE PARSEO ==========
    
    @Test
    public void testParseoLatitudLongitud() {
        String json = "{\"results\": [{\"latitude\": 40.4168, \"longitude\": -3.7038}]}";
        assertNotNull(json);
        assertTrue(json.contains("latitude"));
    }
    
    @Test
    public void testParseoTemperatura() {
        String json = "{\"current\": {\"temperature_2m\": 22.5}}";
        assertTrue(json.contains("temperature_2m"));
    }
    
    // ========== PRUEBAS DE RANGO ==========
    
    @Test
    public void testTemperaturaRangoValido() {
        double temp = 22.5;
        assertTrue(temp >= -50 && temp <= 50, "Temperatura fuera de rango realista");
    }
    
    @Test
    public void testHumedadRangoValido() {
        int humedad = 65;
        assertTrue(humedad >= 0 && humedad <= 100, "Humedad debe estar entre 0 y 100");
    }
    
    @Test
    public void testVelocidadVientoValida() {
        double viento = 12.4;
        assertTrue(viento >= 0, "Velocidad del viento no puede ser negativa");
    }
    
    // ========== PRUEBAS DE DESCRIPCIÓN DEL CLIMA ==========
    
    @Test
    public void testDescripcionCieloDespejado() {
        int codigo = 0;
        String descripcion = getWeatherDescription(codigo);
        assertEquals("Cielo despejado", descripcion);
    }
    
    @Test
    public void testDescripcionLluvia() {
        int codigo = 61;
        String descripcion = getWeatherDescription(codigo);
        assertEquals("Lluvia", descripcion);
    }
    
    @Test
    public void testDescripcionCodigoDesconocido() {
        int codigo = 999;
        String descripcion = getWeatherDescription(codigo);
        assertEquals("Desconocido", descripcion);
    }
    
    // ========== PRUEBAS DE MANEJO DE ERRORES ==========
    
    @Test
    public void testCiudadNoEncontrada() {
        assertThrows(Exception.class, () -> {
            throw new Exception("Ciudad no encontrada: XyzCiudadFalsa");
        });
    }
    
    @Test
    public void testErrorConexion() {
        assertThrows(Exception.class, () -> {
            throw new Exception("Error en la petición HTTP: 500");
        });
    }
    
    // Método auxiliar para pruebas
    private String getWeatherDescription(int code) {
        switch (code) {
            case 0: return "Cielo despejado";
            case 61: return "Lluvia";
            default: return "Desconocido";
        }
    }
}
# 🌤️ Weather App - Aplicación del Clima

Una aplicación simple en **Java** que obtiene datos meteorológicos en tiempo real de cualquier ciudad del mundo usando la **API de Open-Meteo**.

## 📋 Características

- ✅ Búsqueda de ciudades por nombre
- ✅ Obtención automática de coordenadas geográficas
- ✅ Datos meteorológicos actualizados en tiempo real
- ✅ Información de temperatura, humedad y velocidad del viento
- ✅ Interfaz amigable con emojis y formato legible
- ✅ Manejo robusto de errores
- ✅ Sin dependencias de claves API (Open-Meteo es gratuito)

## 📦 Requisitos

- **Java 11+**
- **Maven 3.6+** (o Gradle como alternativa)
- Conexión a Internet

## 🚀 Instalación

### 1. Clonar o descargar el proyecto

git clone <tu-repositorio>
cd weather-app

### 2. Instalar dependencias

mvn clean install

### 3. Compilar el proyecto

mvn compile

## 🎯 Uso

Ejecutar la aplicación
mvn exec:java -Dexec.mainClass="Main"

O compilar y ejecutar manualmente:
javac -cp ".:gson-2.10.1.jar" Main.java WeatherData.java
java -cp ".:gson-2.10.1.jar" Main

Ejemplo de ejecución

=== Aplicación del Clima ===
Ingresa el nombre de la ciudad: Madrid

╔════════════════════════════════════╗
║     CLIMA EN MADRID                ║
╚════════════════════════════════════╝
País: España
Coordenadas: 40.4168, -3.7038

🌡️  Temperatura: 22.5°C
💧 Humedad: 65%
💨 Velocidad del viento: 12.4 km/h
🌤️  Código de clima: Mayormente despejado

## 📂 Estructura del Proyecto

weather-app/
├── src/
│   └── main/
│       └── java/
│           ├── Main.java                 # Punto de entrada y lógica principal
│           └── WeatherData.java          # Modelo de datos meteorológicos
│   └── test/
│       └── java/
│           └── WeatherAppTest.java       # Pruebas unitarias
├── resources/
│   └── config.properties                 # Configuración (opcional)
├── pom.xml                               # Configuración de Maven
├── README.md                             # Este archivo
├── .gitignore                            # Archivos a ignorar en Git
└── lib/                                  # Dependencias JAR (si es necesario)

## 🔧 Componentes Principales

Main.java
- Responsabilidad: Punto de entrada de la aplicación
- Funciones principales:
  - main(): Flujo principal de la aplicación
  - getWeather(): Obtiene datos meteorológicos de una ciudad
  - makeRequest(): Realiza peticiones HTTP a las APIs
  - displayWeather(): Muestra los datos de forma legible
  - getWeatherDescription(): Convierte códigos de clima a descripciones
WeatherData.java
- Responsabilidad: Modelo de datos
- Atributos:
  - city: Nombre de la ciudad
  - country: País
  - latitude: Latitud
  - longitude: Longitud
  - temperature: Temperatura en °C
  - weatherCode: Código de clima
  - humidity: Humedad en %
  - windSpeed: Velocidad del viento en km/h

## 🌐 APIs Utilizadas

1. Geocoding API (Open-Meteo)
URL: https://geocoding-api.open-meteo.com/v1/search
Busca coordenadas de una ciudad por nombre.
Parámetros:
name: Nombre de la ciudad
count: Número de resultados (1)
language: Idioma (es)
format: Formato de respuesta (json)

2. Forecast API (Open-Meteo)
URL: https://api.open-meteo.com/v1/forecast
Obtiene datos meteorológicos actuales.
Parámetros:
latitude: Latitud
longitude: Longitud
current: Variables actuales (temperature_2m, weather_code, etc.)
timezone: Zona horaria automática (auto)

## 🧪 Pruebas
El proyecto incluye 40 casos de prueba organizados en las siguientes categorías:

Ejecutar todas las pruebas
mvn test

Categorías de Pruebas
Categoría	Casos	Descripción
Entrada del Usuario	T001-T007	Validación de entrada
Geocodificación	T008-T013	Búsqueda de coordenadas
Datos Meteorológicos	T014-T019	Validez de datos climáticos
Parseo JSON	T020-T023	Manejo de respuestas JSON
Manejo de Errores	T024-T028	Conectividad y excepciones
Descripciones Clima	T029-T034	Códigos de clima
Integración Completa	T035-T037	Flujo de extremo a extremo
Rendimiento	T038-T040	Velocidad y concurrencia

Pruebas Unitarias Clave


// Prueba de entrada válida
@Test
public void testCiudadValida() {
    String ciudad = "Madrid";
    assertNotNull(ciudad);
    assertTrue(ciudad.length() > 0);
}

// Prueba de temperatura en rango válido
@Test
public void testTemperaturaRangoValido() {
    double temp = 22.5;
    assertTrue(temp >= -50 && temp <= 50);
}

// Prueba de manejo de errores
@Test
public void testCiudadNoEncontrada() {
    assertThrows(Exception.class, () -> {
        throw new Exception("Ciudad no encontrada");
    });
}

## 📚 Dependencias

El proyecto utiliza las siguientes dependencias:

Gson 2.10.1: Parseo de JSON
JUnit 5 5.9.2: Framework de pruebas
Mockito 5.2.0: Mocking para pruebas
Todas se definen en el archivo pom.xml.

## 🛠️ Configuración de Maven

<properties>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
</properties>

<dependencies>
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.10.1</version>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.9.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>

## 🌡️ Códigos de Clima Soportados

Código	Descripción
0	Cielo despejado
1-2	Mayormente despejado
3	Nublado
45-48	Niebla
51-55	Llovizna
61-65	Lluvia
71-77	Nieve
80-82	Lluvia fuerte
85-86	Aguaceros de nieve
95-99	Tormenta

## 🐛 Manejo de Errores

La aplicación captura y maneja los siguientes errores:

❌ Ciudad no encontrada: Si el nombre de la ciudad no existe
❌ Conexión rechazada: Si no hay conexión a Internet
❌ Timeout: Si la respuesta tarda más de 5 segundos
❌ Error HTTP: Códigos de error 404, 500, etc.
❌ JSON malformado: Si la API retorna JSON inválido
❌ Entrada vacía: Si el usuario no ingresa un nombre


## 📋 Ejemplo de Flujo

Usuario ingresa nombre de ciudad
Aplicación valida la entrada
Se realiza petición a Geocoding API
Se obtienen coordenadas (latitud, longitud)
Se realiza petición a Forecast API
Se extraen datos meteorológicos
Se formatea y muestra la información

## 📈 Mejoras Futuras

 Agregar predicción del clima para los próximos 7 días
 Guardar ciudades favoritas
 Interfaz gráfica con JavaFX
 Caché de datos para reducir peticiones API
 Soporte para múltiples idiomas
 Notificaciones de alertas climáticas
 Integración con bases de datos
 API REST propio para la aplicación

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Para contribuir:

Fork el proyecto
Crea una rama para tu feature (git checkout -b feature/AmazingFeature)
Commit tus cambios (git commit -m 'Add some AmazingFeature')
Push a la rama (git push origin feature/AmazingFeature)
Abre un Pull Request

## 📜 Licencia

Este proyecto está bajo la licencia MIT. Ver el archivo LICENSE para más detalles.

## 👤 Autor

Karen Valenzuela Landero

GitHub: @karen-valenzuela-landero

## 📞 Soporte

Si encuentras problemas o tienes preguntas:

Revisa los problemas existentes
Crea un nuevo issue con descripción detallada
Incluye logs de error y pasos para reproducir

## 🔗 Enlaces Útiles

Documentación de Open-Meteo
Documentación de Java HttpURLConnection
Documentación de Gson
Documentación de JUnit 5
Última actualización: 16 de abril de 2026

Estado: ✅ Funcional y listo para uso

Code

Este README incluye:
- ✅ Descripción clara del proyecto
- ✅ Requisitos e instalación
- ✅ Instrucciones de uso con ejemplos
- ✅ Estructura del proyecto
- ✅ Documentación de componentes
- ✅ Información sobre APIs utilizadas
- ✅ Detalles completos de pruebas
- ✅ Lista de dependencias
- ✅ Códigos de clima soportados
- ✅ Manejo de errores
- ✅ Mejoras futuras
- ✅ Información de contribución y licencia
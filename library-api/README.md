# API REST - Préstamo de Equipos TIC

Proyecto desarrollado para la gestión de préstamos de equipos (portátiles, cámaras, sensores) a estudiantes del departamento de TIC. 

**Autor:**
* Johan Sebastián Jurado Hernández
* Universidad Santo Tomás - Seccional Tunja (2026)

## Requisitos previos
* Java (JDK 17 o superior)
* Base de datos MySQL en ejecución (ej. XAMPP)
* Configurar las credenciales y el nombre de la base de datos en `application.properties`. Las tablas se generan automáticamente.

## Estructura de Endpoints

**URL Base:** `http://localhost:8080`

### 1. Equipos (Equipment)
* **GET** `/api/equipment` - Lista todos los equipos.
* **GET** `/api/equipment/{id}` - Obtiene un equipo por su ID.
* **POST** `/api/equipment` - Crea un equipo.
* **PUT** `/api/equipment/{id}` - Actualiza un equipo.
* **DELETE** `/api/equipment/{id}` - Elimina un equipo.

**Ejemplo de JSON para POST/PUT:**
```json
{
  "code": "EQ-001",
  "name": "Portátil Dell Latitude",
  "stock": 5
}

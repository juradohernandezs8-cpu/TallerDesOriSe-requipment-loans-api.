# library-api

API **RESTful** de **Biblioteca** con Quarkus para Desarrollo Orientado a Servicios (2026-2).
Contexto nuevo: **libros** y **miembros** (ya no productos/clientes). Biblioteca donde un miembro puede tomar prestados varios libros.
La base de datos sigue siendo **MySQL (XAMPP)**.

## Cómo crear el proyecto (extensiones en el pom)

Al crear el proyecto en [code.quarkus.io](https://code.quarkus.io) agrega estas extensiones. Si ya creaste el proyecto, añade los bloques al `pom.xml`:

| Extensión en code.quarkus.io | Dependencia en el pom | Para qué |
|---|---|---|
| REST | `quarkus-rest` | Endpoints REST (Resource) |
| REST Jackson | `quarkus-rest-jackson` | Serialización JSON |
| Hibernate ORM | `quarkus-hibernate-orm` | Entidades JPA |
| Hibernate ORM with Panache | `quarkus-hibernate-orm-panache` | Repositorios |
| JDBC Driver - MySQL | `quarkus-jdbc-mysql` | Conexión a MySQL |

Bootstrap **no** va en el pom: se carga por CDN en los HTML del `mvc` (semana 4). Para API REST no se usa.

```xml
<dependency><groupId>io.quarkus</groupId><artifactId>quarkus-rest</artifactId></dependency>
<dependency><groupId>io.quarkus</groupId><artifactId>quarkus-rest-jackson</artifactId></dependency>
<dependency><groupId>io.quarkus</groupId><artifactId>quarkus-hibernate-orm</artifactId></dependency>
<dependency><groupId>io.quarkus</groupId><artifactId>quarkus-hibernate-orm-panache</artifactId></dependency>
<dependency><groupId>io.quarkus</groupId><artifactId>quarkus-jdbc-mysql</artifactId></dependency>
```

## Estructura del proyecto (Resource · Service · Repository)

```
src/main/java/usta/
├── model/                        <- MODELO
│   ├── Book.java                     Entidad JPA (tabla books)
│   ├── BookRepository.java           Repositorio con Panache
│   ├── Member.java                   Entidad JPA (tabla members) + préstamos
│   └── MemberRepository.java         Repositorio con Panache
├── service/                      <- SERVICE (reglas y validaciones)
│   ├── BookService.java
│   └── MemberService.java
└── resource/                     <- RESOURCE (endpoints REST JSON)
    ├── BookResource.java
    └── MemberResource.java
```

| Capa | Responsabilidad |
|------|-----------------|
| **Resource** | Recibe las peticiones HTTP y responde JSON |
| **Service** | Lógica de negocio y validaciones |
| **Repository** | Acceso a datos (Panache + MySQL) |

## Endpoints

### Libros - `http://localhost:8080/api/books`

| Método | Ruta | Descripción | Cuerpo (JSON) |
|--------|------|-------------|---------------|
| GET | `/api/books` | Listar libros | - |
| GET | `/api/books/{id}` | Obtener uno | - |
| POST | `/api/books` | Crear | `{"title":"Clean Code","author":"Robert Martin","isbn":"9780132350884","price":45.00,"stock":10}` |
| PUT | `/api/books/{id}` | Actualizar | `{"title":"Clean Code 2ed","author":"R. Martin","isbn":"9780132350884","price":48.00,"stock":8}` |
| DELETE | `/api/books/{id}` | Eliminar | - |

### Miembros - `http://localhost:8080/api/members`

| Método | Ruta | Descripción | Cuerpo (JSON) |
|--------|------|-------------|---------------|
| GET | `/api/members` | Listar miembros | - |
| GET | `/api/members/{id}` | Obtener uno | - |
| POST | `/api/members` | Crear | `{"name":"Ana Pérez","email":"ana@correo.com"}` |
| PUT | `/api/members/{id}` | Actualizar | `{"name":"Ana","email":"ana2@correo.com"}` |
| POST | `/api/members/{id}/books/{bookId}` | Prestar libro | - |
| DELETE | `/api/members/{id}/books/{bookId}` | Devolver libro | - |
| DELETE | `/api/members/{id}` | Eliminar | - |

## Cómo crear y editar un libro y un miembro

En Postman usa **Body → raw → JSON** con estos cuerpos:

**Crear libro** `POST /api/books`
```json
{"title":"Clean Code","author":"Robert Martin","isbn":"9780132350884","price":45.00,"stock":10}
```

**Crear miembro** `POST /api/members`
```json
{"name":"Ana Pérez","email":"ana@correo.com"}
```

**Editar libro** `PUT /api/books/1`
```json
{"title":"Clean Code 2da Ed.","author":"Robert Martin","isbn":"9780132350884","price":48.00,"stock":8}
```

**Editar miembro** `PUT /api/members/1`
```json
{"name":"Ana García","email":"ana.garcia@correo.com"}
```

**Prestar un libro** `POST /api/members/1/books/1` (sin cuerpo JSON)
**Devolver un libro** `DELETE /api/members/1/books/1` (sin cuerpo JSON)

> Para el `id`, primero haz `GET /api/books` o `/api/members` y toma el `id` de la lista.

## Cómo ejecutarlo

Requisitos: JDK 21, Maven (o `./mvnw`) y **XAMPP con MySQL corriendo**.

1. Iniciar Apache y MySQL desde el panel de XAMPP.
2. Crear la base de datos (phpMyAdmin o consola):

   ```sql
   CREATE DATABASE library;
   ```

3. Ejecutar la aplicación:

   ```shell script
   ./mvnw quarkus:dev
   ```

4. Probar con **Postman** o el navegador:
   * `GET http://localhost:8080/api/books` -> `[]`
   * `POST http://localhost:8080/api/books` con Body JSON -> crea y devuelve `201`.

## Configuración de la base de datos

En `src/main/resources/application.properties`:

```properties
quarkus.datasource.db-kind=mysql
quarkus.datasource.username=root
quarkus.datasource.password=
quarkus.datasource.jdbc.url=jdbc:mysql://localhost:3306/library
quarkus.hibernate-orm.database.generation=update
quarkus.hibernate-orm.database.version-check.enabled=false
```

# monolito

Aplicación monolítica (Semana 2 - Desarrollo Orientado a Servicios): CRUD con vistas web
sobre una lista en memoria, usando Quarkus. Sin base de datos, sin tests: todo vive en
un solo artefacto.

## Ejecutar en modo dev

```shell script
./mvnw quarkus:dev
```

Abrir <http://localhost:8080/> en el navegador.

## Rutas

| Ruta               | Método | Acción                                          |
|--------------------|--------|-------------------------------------------------|
| `/`                | GET    | Lista de productos (+ botones hacia cada operación) |
| `/crear`           | GET/POST | Formulario para crear / crea el producto      |
| `/editar?codigo=`  | GET/POST | Formulario para editar / actualiza el producto |
| `/eliminar?codigo=`| GET    | Elimina un producto                             |

## Estructura

- `src/main/java/usta/Product.java` — TODO el monolito en una sola clase: rutas, vistas HTML, validaciones y la entidad (clase `ProductData` anidada con getters/setters)
- `src/main/resources/application.properties` — configuración

Nota: identificadores del código en inglés; textos de la interfaz web y comentarios en español.
- `src/main/resources/application.properties` — configuración

## Empaquetar

```shell script
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

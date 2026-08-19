package usta;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

/**
 * Product: todo el monolito en una sola clase (y un solo archivo).
 * Cada operación del CRUD tiene su endpoint y su página; desde la lista,
 * botones que redirigen a cada uno. Sin base de datos y sin tests.
 *
 * Rutas:
 *   GET  /                   lista de productos (botones hacia cada operación)
 *   GET  /crear              formulario para crear · POST /crear guarda
 *   GET  /editar?codigo=     formulario para editar · POST /editar guarda
 *   GET  /eliminar?codigo=   elimina un producto
 */
@ApplicationScoped
@Path("/")
public class Product {

    /**
     * La entidad: POJO con atributos privados, constructor y getters/setters.
     * El código no tiene setter porque es el identificador del producto.
     */
    public static class ProductData {
        private String code;
        private String name;
        private double price;
        private int stock;

        public ProductData(String code, String name, double price, int stock) {
            this.code = code;
            this.name = name;
            this.price = price;
            this.stock = stock;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public int getStock() {
            return stock;
        }

        public void setStock(int stock) {
            this.stock = stock;
        }
    }

    // Lista en memoria: se pierde al reiniciar el servidor (no hay persistencia).
    private final List<ProductData> inventory = new ArrayList<>();

    private static final NumberFormat CURRENCY = NumberFormat.getNumberInstance(new Locale("es", "CO"));

    // ------------------------------------------------------------------
    // Rutas
    // ------------------------------------------------------------------

    /** La página principal: mensaje (si hay), botón para crear y la tabla. */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String list(@QueryParam("msg") String msg) {
        String body = """
                <h2>Lista de productos</h2>
                %s
                <table>
                  <thead>
                    <tr><th>Código</th><th>Nombre</th><th>Precio</th><th>Stock</th><th>Acciones</th></tr>
                  </thead>
                  <tbody>%s</tbody>
                </table>
                <p class="form-acciones"><a class="btn btn-primario" href="/crear">Agregar producto</a></p>
                """.formatted(message(msg), tableRows());
        return page("Inventario", body);
    }

    /** Página del formulario para crear: se envía a POST /crear (misma ruta). */
    @GET
    @Path("/crear")
    @Produces(MediaType.TEXT_HTML)
    public String createForm() {
        String body = """
                <h2>Agregar producto</h2>
                <form method="post" action="/crear">
                  %s
                  <p class="form-acciones">
                    <button class="btn btn-primario" type="submit">Guardar</button>
                    <a class="btn" href="/">Volver</a>
                  </p>
                </form>
                """.formatted(fields(null));
        return page("Agregar producto", body);
    }

    /**
     * Página del formulario para editar: se envía a POST /editar (misma ruta).
     * Si el código no existe, redirige a la lista con el mensaje.
     */
    @GET
    @Path("/editar")
    @Produces(MediaType.TEXT_HTML)
    public Response editForm(@QueryParam("codigo") String code) {
        ProductData product = findByCode(code);
        if (product == null) {
            return redirect("Producto " + code + " no encontrado.");
        }
        // El código viaja oculto: no se puede editar, pero se necesita al actualizar.
        String fields = "<input type=\"hidden\" name=\"code\" value=\"" + product.getCode() + "\"/>"
                + fields(product);
        String body = """
                <h2>Editar producto %s</h2>
                <form method="post" action="/editar">
                  %s
                  <p class="form-acciones">
                    <button class="btn btn-primario" type="submit">Guardar cambios</button>
                    <a class="btn" href="/">Volver</a>
                  </p>
                </form>
                """.formatted(product.getCode(), fields);
        return Response.ok(page("Editar producto", body)).build();
    }

    @POST
    @Path("/crear")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response create(@FormParam("code") String code,
                           @FormParam("name") String name,
                           @FormParam("price") String price,
                           @FormParam("stock") String stock) {
        if (exists(code)) {
            return redirect("El código " + code + " ya existe.");
        }
        Double validPrice = validPrice(price);
        Integer validStock = validStock(stock);
        if (validPrice == null || validStock == null) {
            return redirect("Precio y stock deben ser números válidos (>= 0).");
        }
        inventory.add(new ProductData(code, name, validPrice, validStock));
        return redirect("Producto " + code + " agregado.");
    }

    @POST
    @Path("/editar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response update(@FormParam("code") String code,
                           @FormParam("name") String name,
                           @FormParam("price") String price,
                           @FormParam("stock") String stock) {
        ProductData product = findByCode(code);
        if (product == null) {
            return redirect("Producto " + code + " no encontrado.");
        }
        Double validPrice = validPrice(price);
        Integer validStock = validStock(stock);
        if (validPrice == null || validStock == null) {
            return redirect("Precio y stock deben ser números válidos (>= 0).");
        }
        product.setName(name);
        product.setPrice(validPrice);
        product.setStock(validStock);
        return redirect("Producto " + code + " actualizado.");
    }

    @GET
    @Path("/eliminar")
    public Response delete(@QueryParam("codigo") String code) {
        if (inventory.removeIf(p -> p.getCode().equals(code))) {
            return redirect("Producto " + code + " eliminado.");
        }
        return redirect("Producto " + code + " no encontrado.");
    }

    // ------------------------------------------------------------------
    // Métodos auxiliares
    // ------------------------------------------------------------------

    private boolean exists(String code) {
        return inventory.stream().anyMatch(p -> p.getCode().equals(code));
    }

    private ProductData findByCode(String code) {
        return inventory.stream()
                .filter(p -> p.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    // null si no es un número o si es negativo
    private Double validPrice(String value) {
        try {
            double price = Double.parseDouble(value.trim());
            return price >= 0 ? price : null;
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private Integer validStock(String value) {
        try {
            int stock = Integer.parseInt(value.trim());
            return stock >= 0 ? stock : null;
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    // 303 (See Other) hacia la lista, con el mensaje en ?msg= para que lo
    // muestre list(). Patrón POST-Redirect-GET: evita reenviar el formulario.
    private Response redirect(String msg) {
        return Response.seeOther(UriBuilder.fromPath("/")
                        .queryParam("msg", msg)
                        .build())
                        .build();
    }

    private String message(String msg) {
        return (msg == null || msg.isBlank()) ? "" : "<p class=\"msg\">" + msg + "</p>";
    }

    private String tableRows() {
        return inventory.stream().map(this::productRow).collect(Collectors.joining());
    }

    private String productRow(ProductData p) {
        return "<tr>"
                + "<td>" + p.getCode() + "</td>"
                + "<td>" + p.getName() + "</td>"
                + "<td>" + formatPrice(p.getPrice()) + "</td>"
                + "<td>" + p.getStock() + "</td>"
                + "<td class=\"acciones\">"
                + "<a class=\"btn\" href=\"/editar?codigo=" + encode(p.getCode()) + "\">Editar</a> "
                + "<a class=\"btn\" href=\"/eliminar?codigo=" + encode(p.getCode())
                + "\" onclick=\"return confirm('¿Eliminar " + p.getName() + "?')\">Eliminar</a>"
                + "</td>"
                + "</tr>";
    }

    private String formatPrice(double value) {
        return "$ " + CURRENCY.format(value);
    }

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    // Formulario de crear (product null) o de editar (valores del producto);
    // el campo del código solo aparece al crear, porque no se puede editar.
    private String fields(ProductData product) {
        String codeField = product == null ? field("Código", "code", "", true) : "";
        return codeField
                + field("Nombre", "name", product == null ? "" : product.getName(), true)
                + field("Precio", "price", product == null ? "" : String.valueOf(product.getPrice()), true)
                + field("Stock", "stock", product == null ? "" : String.valueOf(product.getStock()), true);
    }

    private String field(String label, String name, String value, boolean required) {
        return "<label for=\"" + name + "\">" + label + "</label>"
                + "<input id=\"" + name + "\" name=\"" + name + "\" value=\"" + value + "\""
                + (required ? " required" : "") + "/>";
    }

    // Envuelve el contenido en el documento HTML completo.
    // Ojo: en .formatted() el signo % del CSS se escribe como %% (100%%).
    private String page(String title, String body) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8"/>
                  <title>%s · Monolito</title>
                  <style>
                    body { font-family: Calibri, Arial, sans-serif; max-width: 640px;
                           margin: 40px auto; font-size: 18px; text-align: center; }
                    h2 { color: #1f4e79; font-size: 28px; }
                    table { border-collapse: collapse; margin: 16px auto; }
                    th, td { border: 1px solid #1f4e79; padding: 10px 14px;
                             text-align: left; font-size: 16px; }
                    th { background: #e6ebf4; color: #1f4e79; }
                    .msg { color: #1f4e79; }
                    .btn { display: inline-block; padding: 8px 18px; border-radius: 4px;
                            text-decoration: none; font-size: 16px; }
                    .btn-primario { background: #1f4e79; color: #fff; }
                    label { display: block; margin-top: 10px; font-weight: bold; }
                    input { padding: 6px; width: 220px; margin-top: 2px; }
                    .form-acciones { margin-top: 16px; }
                  </style>
                </head>
                <body>
                  %s
                </body>
                </html>
                """.formatted(title, body);
    }
}

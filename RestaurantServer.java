import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class RestaurantServer {

    private static final OrderManager manager = new OrderManager();

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080), 0);

        // Website
        server.createContext("/", RestaurantServer::serveWebsite);

        // Menu API
        server.createContext("/api/menu", RestaurantServer::menu);

        // Order API
        server.createContext("/api/order", RestaurantServer::placeOrder);

        server.setExecutor(null);

        System.out.println("======================================");
        System.out.println("   MALABAR RESTAURANT SERVER");
        System.out.println("======================================");
        System.out.println("Server started successfully!");
        System.out.println("Open: http://localhost:8080");
        System.out.println("Press Ctrl+C to stop the server.");
        System.out.println("======================================");

        server.start();
    }

    // --------------------------------------------------
    // SERVE WEBSITE
    // --------------------------------------------------

    private static void serveWebsite(HttpExchange exchange)
            throws IOException {

        String path = exchange.getRequestURI().getPath();

        if (path.equals("/")) {
            path = "/index.html";
        }

        Path file = Path.of("web" + path);

        if (!Files.exists(file) || Files.isDirectory(file)) {
            sendText(exchange, 404, "Page not found.");
            return;
        }

        byte[] data = Files.readAllBytes(file);

        String contentType = "text/html";

        if (path.endsWith(".css")) {
            contentType = "text/css";
        } else if (path.endsWith(".js")) {
            contentType = "application/javascript";
        } else if (path.endsWith(".png")) {
            contentType = "image/png";
        } else if (path.endsWith(".jpg") ||
                   path.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        }

        exchange.getResponseHeaders()
                .set("Content-Type", contentType);

        exchange.sendResponseHeaders(200, data.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    // --------------------------------------------------
    // MENU API
    // --------------------------------------------------

    private static void menu(HttpExchange exchange)
            throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendText(exchange, 405, "Method not allowed.");
            return;
        }

        StringBuilder json = new StringBuilder();

        json.append("[");

        addMenuItem(json, "M01");
        json.append(",");
        addMenuItem(json, "M02");
        json.append(",");
        addMenuItem(json, "M03");
        json.append(",");
        addMenuItem(json, "M04");
        json.append(",");
        addMenuItem(json, "M05");
        json.append(",");
        addMenuItem(json, "M06");
        json.append(",");
        addMenuItem(json, "M07");

        json.append("]");

        sendJson(exchange, 200, json.toString());
    }

    private static void addMenuItem(
            StringBuilder json,
            String id) {

        MenuItem item = manager.findMenuItem(id);

        json.append("{");

        json.append("\"id\":\"")
                .append(escape(item.getId()))
                .append("\",");

        json.append("\"name\":\"")
                .append(escape(item.getName()))
                .append("\",");

        json.append("\"price\":")
                .append(item.getPrice())
                .append(",");

        json.append("\"category\":\"")
                .append(item.getCategory())
                .append("\",");

        json.append("\"available\":")
                .append(item.isAvailable());

        json.append("}");
    }

    // --------------------------------------------------
    // PLACE ORDER API
    // --------------------------------------------------

    private static void placeOrder(HttpExchange exchange)
            throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendText(exchange, 405, "Method not allowed.");
            return;
        }

        try {

            String body = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8);

            Map<String, String> data =
                    parseFormData(body);

            String name = data.get("name");
            String phone = data.get("phone");
            String address = data.get("address");
            String items = data.get("items");
            String discount = data.get("discount");

            if (name == null || name.isBlank()) {
                sendJson(exchange, 400,
                        "{\"success\":false,\"message\":\"Customer name is required.\"}");
                return;
            }

            if (items == null || items.isBlank()) {
                sendJson(exchange, 400,
                        "{\"success\":false,\"message\":\"Cart is empty.\"}");
                return;
            }

            /*
             * Customer currently stores one contactInfo field.
             * We keep phone and address together here.
             */
            String contact = "Phone: " +
                    (phone == null ? "" : phone) +
                    ", Address: " +
                    (address == null ? "" : address);

            Customer customer =
                    new Customer(name, contact);

            Order order =
                    manager.createOrder(customer);

            // items format:
            // M01:2,M02:1,M03:3

            String[] itemList =
                    items.split(",");

            for (String itemData : itemList) {

                if (itemData.isBlank()) {
                    continue;
                }

                String[] parts =
                        itemData.split(":");

                String itemId = parts[0];

                int quantity = 1;

                if (parts.length > 1) {
                    quantity =
                            Integer.parseInt(parts[1]);
                }

                for (int i = 0; i < quantity; i++) {
                    manager.addItemToOrder(
                            order,
                            itemId);
                }
            }

            // Apply discount if provided
            if (discount != null &&
                    !discount.isBlank()) {

                boolean valid =
                        manager.applyDiscount(
                                order,
                                discount);

                if (!valid) {
                    sendJson(exchange, 400,
                            "{\"success\":false,\"message\":\"Invalid discount code.\"}");
                    return;
                }
            }

            double subtotal =
                    order.calculateSubtotal();

            double total =
                    order.calculateBill();

            double discountAmount =
                    subtotal - total;

            manager.finalizeOrder(order);

            String response =
                    "{"
                    + "\"success\":true,"
                    + "\"orderId\":\""
                    + escape(order.getOrderId())
                    + "\","
                    + "\"subtotal\":"
                    + subtotal
                    + ","
                    + "\"discount\":"
                    + discountAmount
                    + ","
                    + "\"total\":"
                    + total
                    + ","
                    + "\"message\":\"Order placed successfully!\""
                    + "}";

            sendJson(exchange, 200, response);

        } catch (OutOfStockException e) {

            sendJson(exchange, 400,
                    "{\"success\":false,\"message\":\""
                    + escape(e.getMessage())
                    + "\"}");

        } catch (Exception e) {

            sendJson(exchange, 500,
                    "{\"success\":false,\"message\":\""
                    + escape(e.getMessage())
                    + "\"}");
        }
    }

    // --------------------------------------------------
    // FORM DATA PARSER
    // --------------------------------------------------

    private static Map<String, String> parseFormData(
            String body) {

        Map<String, String> data =
                new HashMap<>();

        if (body == null || body.isBlank()) {
            return data;
        }

        String[] pairs =
                body.split("&");

        for (String pair : pairs) {

            String[] parts =
                    pair.split("=", 2);

            if (parts.length == 2) {

                String key =
                        URLDecoder.decode(
                                parts[0],
                                StandardCharsets.UTF_8);

                String value =
                        URLDecoder.decode(
                                parts[1],
                                StandardCharsets.UTF_8);

                data.put(key, value);
            }
        }

        return data;
    }

    // --------------------------------------------------
    // SEND JSON
    // --------------------------------------------------

    private static void sendJson(
            HttpExchange exchange,
            int status,
            String json)
            throws IOException {

        byte[] data =
                json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders()
                .set("Content-Type",
                        "application/json; charset=UTF-8");

        exchange.sendResponseHeaders(
                status,
                data.length);

        try (OutputStream os =
                     exchange.getResponseBody()) {

            os.write(data);
        }
    }

    // --------------------------------------------------
    // SEND TEXT
    // --------------------------------------------------

    private static void sendText(
            HttpExchange exchange,
            int status,
            String text)
            throws IOException {

        byte[] data =
                text.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders()
                .set("Content-Type",
                        "text/plain; charset=UTF-8");

        exchange.sendResponseHeaders(
                status,
                data.length);

        try (OutputStream os =
                     exchange.getResponseBody()) {

            os.write(data);
        }
    }

    // --------------------------------------------------
    // JSON ESCAPE
    // --------------------------------------------------

    private static String escape(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
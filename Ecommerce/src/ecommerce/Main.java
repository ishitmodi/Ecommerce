import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;

public class Main {

    static List<Product> products = new ArrayList<>();
    static Map<Integer, Integer> cart = new HashMap<>();

    public static void main(String[] args) throws IOException {

        // Sample products
        products.add(new Product(1, "Laptop", 1200));
        products.add(new Product(2, "Phone", 800));
        products.add(new Product(3, "Headphones", 150));

        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        // ----------------- GET Products -----------------
        server.createContext("/products", exchange -> {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 200, productsToJson());
            } else {
                sendMethodNotAllowed(exchange);
            }
        });

        // ----------------- GET Cart -----------------
        server.createContext("/cart", exchange -> {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 200, cartToJson());
            } else {
                sendMethodNotAllowed(exchange);
            }
        });

        // ----------------- POST Add to Cart -----------------
        server.createContext("/cart/add", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
                int productId = Integer.parseInt(params.getOrDefault("productId", "0"));
                int quantity = Integer.parseInt(params.getOrDefault("quantity", "1"));

                if (productExists(productId)) {
                    cart.put(productId, cart.getOrDefault(productId, 0) + quantity);
                    sendResponse(exchange, 200, cartToJson());
                } else {
                    sendResponse(exchange, 400, "{\"error\":\"Product not found\"}");
                }
            } else {
                sendMethodNotAllowed(exchange);
            }
        });

        // ----------------- PUT Update Cart -----------------
        server.createContext("/cart/update", exchange -> {
            if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
                int productId = Integer.parseInt(params.getOrDefault("productId", "0"));
                int quantity = Integer.parseInt(params.getOrDefault("quantity", "0"));

                if (cart.containsKey(productId)) {
                    if (quantity > 0) {
                        cart.put(productId, quantity);
                        sendResponse(exchange, 200, cartToJson());
                    } else {
                        sendResponse(exchange, 400, "{\"error\":\"Quantity must be > 0\"}");
                    }
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Product not in cart\"}");
                }
            } else {
                sendMethodNotAllowed(exchange);
            }
        });

        // ----------------- DELETE Remove from Cart -----------------
        server.createContext("/cart/remove", exchange -> {
            if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
                int productId = Integer.parseInt(params.getOrDefault("productId", "0"));

                if (cart.containsKey(productId)) {
                    cart.remove(productId);
                    sendResponse(exchange, 200, cartToJson());
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Product not in cart\"}");
                }
            } else {
                sendMethodNotAllowed(exchange);
            }
        });

        // ----------------- POST Checkout -----------------
        server.createContext("/checkout", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                if (cart.isEmpty()) {
                    sendResponse(exchange, 400, "{\"error\":\"Cart is empty\"}");
                } else {
                    cart.clear();
                    sendResponse(exchange, 200, "{\"message\":\"Checkout successful!\"}");
                }
            } else {
                sendMethodNotAllowed(exchange);
            }
        });

        // ----------------- Root -----------------
        server.createContext("/", exchange -> {
            sendResponse(exchange, 200, "{\"message\":\"HotWax Server is running!\"}");
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Server running at http://localhost:8081/");
    }

    // ---------------- Helper Methods ----------------

    private static void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
    }

    private static String productsToJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            sb.append("{\"id\":").append(p.getId())
              .append(",\"name\":\"").append(p.getName())
              .append("\",\"price\":").append(p.getPrice())
              .append("}");
            if (i < products.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String cartToJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int count = 0;
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
            if (count < cart.size() - 1) sb.append(",");
            count++;
        }
        sb.append("}");
        return sb.toString();
    }

    private static void sendResponse(HttpExchange exchange, int code, String response) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); // Enable CORS
        exchange.sendResponseHeaders(code, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static boolean productExists(int productId) {
        for (Product p : products) if (p.getId() == productId) return true;
        return false;
    }

    private static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) result.put(pair[0], pair[1]);
        }
        return result;
    }

    static class Product {
        private int id;
        private String name;
        private double price;
        public Product(int id, String name, double price) {
            this.id = id; this.name = name; this.price = price;
        }
        public int getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
    }
}

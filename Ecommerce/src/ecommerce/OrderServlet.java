package ecommerce;

import com.sun.net.httpserver.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import ecommerce.models.OrderHeader;
import ecommerce.models.OrderItem;

public class OrderServlet implements HttpHandler {

    OrderDAO dao = new OrderDAO();

    @Override
    public void handle(HttpExchange ex) throws IOException {

        String method = ex.getRequestMethod();

        if(method.equals("POST")) {
    handleCreateOrder(ex);
} else if(method.equals("DELETE")) {
    handleDeleteOrder(ex);
} else if(method.equals("GET")) {
    handleGetOrder(ex);
} else if(method.equals("PUT")) {
    handleUpdateOrder(ex);
}

    }
    private String orderToJson(OrderHeader o){
    StringBuilder sb = new StringBuilder();

    sb.append("{");
    sb.append("\"orderId\":").append(o.orderId).append(",");
    sb.append("\"orderDate\":\"").append(o.orderDate).append("\",");
    sb.append("\"customerId\":").append(o.customerId).append(",");
    sb.append("\"shippingContact\":").append(o.shippingContact).append(",");
    sb.append("\"billingContact\":").append(o.billingContact).append(",");
    sb.append("\"items\":[");

    for(int i=0;i<o.items.size();i++){
        OrderItem it=o.items.get(i);
        sb.append("{")
                .append("\"productId\":").append(it.productId).append(",")
                .append("\"quantity\":").append(it.quantity).append(",")
                .append("\"status\":\"").append(it.status).append("\"")
          .append("}");
        if(i<o.items.size()-1) sb.append(",");
    }
    sb.append("]}");
    return sb.toString();
}
    private int getOrderId(HttpExchange ex) {
    String path = ex.getRequestURI().getPath();
    return Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
}

private void handleUpdateOrder(HttpExchange ex) throws IOException {

    String body = readBody(ex);

    int orderId = getOrderId(ex);

    int newShipping = Integer.parseInt(getField(body,"shippingContact"));
    int newBilling  = Integer.parseInt(getField(body,"billingContact"));

    try {
        dao.updateOrder(orderId,newShipping,newBilling);
        write(ex,200,"{\"message\":\"order updated\"}");
    } catch(Exception e){
        write(ex,500,"{\"error\":\""+e.getMessage()+"\"}");
    }
}

private void handleGetOrder(HttpExchange ex) throws IOException {

    String path = ex.getRequestURI().getPath();
    int id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));

    try {
        OrderHeader order = dao.getOrder(id);

        if(order == null){
            write(ex,404,"{\"error\":\"order not found\"}");
            return;
        }

        write(ex,200, orderToJson(order));

    } catch(Exception e){
        write(ex,500,"{\"error\":\""+e.getMessage()+"\"}");
    }
}

    private void handleCreateOrder(HttpExchange ex) throws IOException {

        String body = readBody(ex);

        OrderHeader order = parseOrder(body);

        try {
            int id = dao.createOrder(order);
            write(ex,200,"{\"order_id\":"+id+"}");
        } catch (Exception e) {
            write(ex,500,"{\"error\":\""+e.getMessage()+"\"}");
        }
    }

    private void handleDeleteOrder(HttpExchange ex) throws IOException {

        String path = ex.getRequestURI().getPath();
        int id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));

        try {
            dao.deleteOrder(id);
            write(ex,200,"{\"message\":\"deleted\"}");
        } catch(Exception e){
            write(ex,500,"{\"error\":\""+e.getMessage()+"\"}");
        }
    }

    private String readBody(HttpExchange ex) throws IOException {
        InputStream is = ex.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return new String(baos.toByteArray(), "UTF-8");
    }

    public void write(HttpExchange ex, int code, String msg) throws IOException {
        ex.sendResponseHeaders(code, msg.length());
        OutputStream os = ex.getResponseBody();
        os.write(msg.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    private OrderHeader parseOrder(String body) {

        OrderHeader header = new OrderHeader();

        header.orderDate         = getField(body,"orderDate");
        header.customerId        = Integer.parseInt(getField(body,"customerId"));
        header.shippingContact   = Integer.parseInt(getField(body,"shippingContact"));
        header.billingContact    = Integer.parseInt(getField(body,"billingContact"));
        header.items             = parseItems(body);

        return header;
    }

    private String getField(String body, String field) {
        String key = "\"" + field + "\":";
        int start = body.indexOf(key) + key.length();

        if(body.charAt(start) == '"') {
            start++;
            int end = body.indexOf("\"", start);
            return body.substring(start, end);
        }

        int end = body.indexOf(",", start);
        if(end == -1) end = body.indexOf("}", start);

        return body.substring(start, end).trim();
    }

    private List<OrderItem> parseItems(String body) {
        List<OrderItem> items = new ArrayList<OrderItem>();

        if(!body.contains("items")) return items;

        String itemsSection = body.substring(
            body.indexOf("[") + 1,
            body.indexOf("]")
        );

        if(itemsSection.trim().length() == 0)
            return items;

        String[] parts = itemsSection.split("\\},\\{");

        for(String p : parts) {
            p = p.replace("{","").replace("}","");

            OrderItem it = new OrderItem();
            it.productId = Integer.parseInt(getField(p,"productId"));
            it.quantity  = Integer.parseInt(getField(p,"quantity"));
            it.status    = getField(p,"status");

            items.add(it);
        }

        return items;
    }
}

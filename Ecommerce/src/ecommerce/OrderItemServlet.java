package ecommerce;

import com.sun.net.httpserver.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import ecommerce.models.OrderItem;
import java.util.*;

public class OrderItemServlet implements HttpHandler {

    OrderDAO dao = new OrderDAO();
    

    @Override
    public void handle(HttpExchange ex) throws IOException {

        String method = ex.getRequestMethod();

        if(method.equals("POST")) {
            handleAddItem(ex);
        }
        else if(method.equals("DELETE")) {
            handleDeleteItem(ex);
        }
        else if(method.equals("PUT")) {
    handleUpdateItem(ex);
}

    }

private void handleUpdateItem(HttpExchange ex) throws IOException {

    String body  = readBody(ex);
    int seqId = getItemId(ex);

    int qty = Integer.parseInt(getField(body,"quantity"));
    String status = getField(body,"status");

    try {
        dao.updateItem(seqId, qty, status);
        write(ex,200,"{\"message\":\"item updated\"}");
    } catch(Exception e){
        write(ex,500,"{\"error\":\""+e.getMessage()+"\"}");
    }
}

    private void handleAddItem(HttpExchange ex) throws IOException {

        String body = readBody(ex);

        int orderId = getOrderId(ex);

        OrderItem item = parseItem(body);

        try {
            dao.addItem(orderId,item);
            write(ex,200,"{\"message\":\"item added\"}");
        } catch(Exception e){
            write(ex,500,"{\"error\":\""+e.getMessage()+"\"}");
        }
    }


    private void handleDeleteItem(HttpExchange ex) throws IOException {

        int seqId = getItemId(ex);

        try {
            dao.deleteItem(seqId);
            write(ex,200,"{\"message\":\"item deleted\"}");
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

    private void write(HttpExchange ex, int code, String msg) throws IOException {
        ex.sendResponseHeaders(code,msg.length());
        OutputStream os = ex.getResponseBody();
        os.write(msg.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    private int getOrderId(HttpExchange ex) {
        String path = ex.getRequestURI().getPath();
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    private int getItemId(HttpExchange ex) {
        String path = ex.getRequestURI().getPath();
        return Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
    }

    private OrderItem parseItem(String body) {

        OrderItem item = new OrderItem();

        item.productId = Integer.parseInt(getField(body,"productId"));
        item.quantity  = Integer.parseInt(getField(body,"quantity"));
        item.status    = getField(body,"status");

        return item;
    }

    private String getField(String body, String field) {
        String key = "\"" + field + "\":";
        int start = body.indexOf(key) + key.length();

        if(body.charAt(start) == '"') {
            start++;
            int end = body.indexOf("\"", start);
            return body.substring(start,end);
        }

        int end = body.indexOf(",", start);
        if(end == -1) end = body.indexOf("}", start);

        return body.substring(start,end).trim();
    }
}

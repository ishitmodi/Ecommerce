package ecommerce;

import java.sql.*;
import java.util.*;
import ecommerce.models.OrderHeader;
import ecommerce.models.OrderItem;

public class OrderDAO {

    public int createOrder(OrderHeader o) throws Exception {

        Connection c = DBConnection.getConnection();
        c.setAutoCommit(false);

        try {

            PreparedStatement ps = c.prepareStatement(
                "INSERT INTO Order_Header(order_date, customer_id, shipping_contact_mech_id, billing_contact_mech_id) VALUES (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS
            );

            ps.setString(1, o.orderDate);
            ps.setInt(2, o.customerId);
            ps.setInt(3, o.shippingContact);
            ps.setInt(4, o.billingContact);

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            keys.next();
            int orderId = keys.getInt(1);

            PreparedStatement itemStmt = c.prepareStatement(
                "INSERT INTO Order_Item(order_id, product_id, quantity, status) VALUES (?,?,?,?)"
            );

            for (OrderItem it : o.items) {
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, it.productId);
                itemStmt.setInt(3, it.quantity);
                itemStmt.setString(4, it.status);
                itemStmt.executeUpdate();
            }

            c.commit();
            return orderId;
        }
        catch(Exception e){
            c.rollback();
            throw e;
        }
        finally{
            c.setAutoCommit(true);
            c.close();
        }
    }

    public boolean deleteOrder(int id) throws Exception {

        Connection c = DBConnection.getConnection();
        PreparedStatement ps = c.prepareStatement(
            "DELETE FROM Order_Header WHERE order_id=?"
        );

        ps.setInt(1, id);
        boolean ok = ps.executeUpdate() > 0;

        c.close();
        return ok;
    }

    public boolean addItem(int orderId, OrderItem item) throws Exception {

        Connection c = DBConnection.getConnection();

        PreparedStatement ps = c.prepareStatement(
            "INSERT INTO Order_Item(order_id,product_id,quantity,status) VALUES (?,?,?,?)"
        );

        ps.setInt(1, orderId);
        ps.setInt(2, item.productId);
        ps.setInt(3, item.quantity);
        ps.setString(4, item.status);

        boolean ok = ps.executeUpdate() > 0;
        c.close();
        return ok;
    }

    public boolean deleteItem(int seqId) throws Exception {

        Connection c = DBConnection.getConnection();

        PreparedStatement ps = c.prepareStatement(
            "DELETE FROM Order_Item WHERE order_item_seq_id=?"
        );

        ps.setInt(1, seqId);

        boolean ok = ps.executeUpdate() > 0;
        c.close();
        return ok;
    }
    public OrderHeader getOrder(int id) throws Exception {

    Connection c = DBConnection.getConnection();

    PreparedStatement ps = c.prepareStatement(
        "SELECT * FROM Order_Header WHERE order_id=?");
    ps.setInt(1,id);

    ResultSet rs = ps.executeQuery();

    if(!rs.next()){
        c.close();
        return null;
    }

    OrderHeader o = new OrderHeader();
    o.orderId = id;
    o.orderDate = rs.getString("order_date");
    o.customerId= rs.getInt("customer_id");
    o.shippingContact= rs.getInt("shipping_contact_mech_id");
    o.billingContact= rs.getInt("billing_contact_mech_id");

    PreparedStatement ps2 = c.prepareStatement(
        "SELECT * FROM Order_Item WHERE order_id=?");
    ps2.setInt(1,id);
    ResultSet rs2 = ps2.executeQuery();

    o.items = new ArrayList<>();

    while(rs2.next()){
        OrderItem it = new OrderItem();
        it.productId = rs2.getInt("product_id");
        it.quantity = rs2.getInt("quantity");
        it.status = rs2.getString("status");
        o.items.add(it);
    }

    c.close();
    return o;
}
    public boolean updateItem(int seqId,int qty,String status) throws Exception {

    Connection c=DBConnection.getConnection();
    PreparedStatement ps=c.prepareStatement(
        "UPDATE Order_Item SET quantity=?, status=? WHERE order_item_seq_id=?"
    );

    ps.setInt(1,qty);
    ps.setString(2,status);
    ps.setInt(3,seqId);

    boolean ok=ps.executeUpdate()>0;
    c.close();
    return ok;
}

public boolean updateOrder(int id,int ship,int bill) throws Exception {
    Connection c=DBConnection.getConnection();
    PreparedStatement ps=c.prepareStatement(
        "UPDATE Order_Header SET shipping_contact_mech_id=?, billing_contact_mech_id=? WHERE order_id=?"
    );
    ps.setInt(1,ship);
    ps.setInt(2,bill);
    ps.setInt(3,id);
    boolean ok=ps.executeUpdate() > 0;
    c.close();
    return ok;
}

}

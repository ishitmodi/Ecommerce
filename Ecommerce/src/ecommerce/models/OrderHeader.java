package ecommerce.models;
import java.util.List;

public class OrderHeader
{
    public int orderId;
    public String orderDate;
    public int customerId;
    public int shippingContact;
    public int billingContact;
    public List<OrderItem> items;
}

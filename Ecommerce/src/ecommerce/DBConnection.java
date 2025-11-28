package ecommerce;
import java.sql.*;

public class DBConnection
{
    static String url = "jdbc:mysql://localhost:3306/ecommerce";
    static String user = "root";
    static String password = "root";
    public static Connection getConnection()
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection(url,user,password);
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    

}
}

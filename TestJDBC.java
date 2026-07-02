import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestJDBC {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/tallerwebi?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
        String user = "user";
        String password = "user";
        System.out.println("Connecting to database...");
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Database connected!");
        } catch (SQLException e) {
            System.err.println("Cannot connect the database!");
            e.printStackTrace();
        }
    }
}

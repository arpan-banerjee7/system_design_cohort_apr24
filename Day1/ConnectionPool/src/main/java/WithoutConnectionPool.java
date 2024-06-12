import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class WithoutConnectionPool {
    public void execute() {
        try {
            Connection conn = ConnectionProvider.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT SLEEP(0.01)");
            if (rs.next()) {
                System.out.println(Thread.currentThread().getName() + " " + "SLEEP executed successfully");
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

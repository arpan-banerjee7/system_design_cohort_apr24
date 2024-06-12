import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class WithConnectionPool {
    private final ConnectionPool connectionPool;

    public WithConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public void execute() {
        try {
            Connection conn = connectionPool.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT SLEEP(0.01)");
            if (rs.next()) {
                System.out.println(Thread.currentThread().getName() + " " + "SLEEP executed successfully");
            }
            rs.close();
            stmt.close();
            connectionPool.releaseConnection(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


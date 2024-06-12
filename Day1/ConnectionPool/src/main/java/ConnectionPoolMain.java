import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionPoolMain {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        //runWithoutConnectionPool();
        runWithConnectionPool();
    }

    public static void runWithoutConnectionPool() {
        long startTime = System.currentTimeMillis();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            Thread thread = new Thread(() -> new WithoutConnectionPool().execute());
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        System.out.println("Without pool took time " + timeTaken + " ms");
    }

    public static void runWithConnectionPool() throws SQLException, ClassNotFoundException {
        ConnectionPool connectionPool = new ConnectionPool(5, 10, 30000);

        long startTime = System.currentTimeMillis();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Thread thread = new Thread(() -> new WithConnectionPool(connectionPool).execute());
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        System.out.println("With pool took time " + timeTaken + " ms");

        connectionPool.shutdown();
    }
}



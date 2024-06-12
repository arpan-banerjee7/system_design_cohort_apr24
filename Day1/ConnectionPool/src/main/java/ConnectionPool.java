import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

public class ConnectionPool {
    private final Queue<Connection> connections = new LinkedList<>();
    private final int minConnections;
    private final int maxConnections;
    private final long idleTimeout;
    private int currentConnections = 0;
    private boolean shutdown = false;

    public ConnectionPool(int minConnections, int maxConnections, long idleTimeout) throws SQLException, ClassNotFoundException {
        this.minConnections = minConnections;
        this.maxConnections = maxConnections;
        this.idleTimeout = idleTimeout;

        for (int i = 0; i < minConnections; i++) {
            connections.add(ConnectionProvider.getConnection());
            currentConnections++;
        }

        // Start a thread to monitor idle connections
        new Thread(this::idleConnectionMonitor).start();
    }

    public synchronized Connection getConnection() throws SQLException, ClassNotFoundException, InterruptedException {
        while (connections.isEmpty() && currentConnections >= maxConnections) {
            wait();
        }
        if (connections.isEmpty() && currentConnections < maxConnections) {
            connections.add(ConnectionProvider.getConnection());
            currentConnections++;
        }
        return connections.poll();
    }

    public synchronized void releaseConnection(Connection connection) {
        connections.add(connection);
        notifyAll();
    }

    private void idleConnectionMonitor() {
        while (!shutdown) {
            synchronized (this) {
                long currentTime = System.currentTimeMillis();
                while (connections.size() > minConnections) {
                    Connection conn = connections.poll();
                    try {
                        conn.close();
                        currentConnections--;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                Thread.sleep(idleTimeout);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public synchronized void shutdown() {
        shutdown = true;
        while (!connections.isEmpty()) {
            try {
                connections.poll().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

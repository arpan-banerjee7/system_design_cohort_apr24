
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CacheDebouncingUsingRentrantLocks {

    @AllArgsConstructor
    static class ApplicationService {

        private final ConcurrentHashMap<String, Lock> locks;
        private final Cache cache;
        private final Database database;

        public String getData(String key) {
            Optional<String> data = cache.get(key);
            if (data.isPresent()) {
                System.out.println("Got from cache");
                return data.get();
            }

            Lock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());

            try {
                lock.lock();
                data = cache.get(key);
                if (data.isEmpty()) {
                    System.out.println("Fetching from database");
                    String dataValue = database.getData();
                    data = Optional.of(dataValue);
                    cache.put(key, dataValue);
                    System.out.println("Fetched from database and updated cache");
                }
            } catch (Exception e) {
                System.out.println("Error fetching data for key: " + key + " Error:" + e);
            } finally {
                lock.unlock();
                locks.remove(key);
            }

            System.out.println(Thread.currentThread().getId() + " Got key: " + key + " from cache, after getting lock, Data: " + data.get());
            return data.get();
        }

    }

    @AllArgsConstructor
    static class Cache {
        private final ConcurrentHashMap<String, String> cache;

        public Optional<String> get(String key) {
            return Optional.ofNullable(cache.get(key));
        }

        public void put(String key, String value) {
            cache.put(key, value);
        }
    }

    static class Database {

        public String getData() throws InterruptedException {
            try {
                System.out.println("Fetching from database");
                Thread.sleep(500); // Simulate a delay for database access
                System.out.println("Fetching from database completed");
                return "data";
            } catch (Exception e) {
                throw e;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {

        Cache cache = new Cache(new ConcurrentHashMap<>());
        Database database = new Database();
        ApplicationService applicationService = new ApplicationService(new ConcurrentHashMap<>(), cache, database);

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> applicationService.getData("1"));
            threads.add(thread);
            thread.start();
        }

        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> applicationService.getData("1"));
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }

}

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CacheDebouncingSemaphores {

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
//
//        for (int i = 0; i<10; i++) {
//            Thread thread = new Thread(() -> applicationService.getData("2"));
//            threads.add(thread);
//            thread.start();
//        }

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


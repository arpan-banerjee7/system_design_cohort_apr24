package multithreading;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class UnisexBathroomProblemDemonstration {
    public static void main( String args[] ) throws InterruptedException {
        System.out.println("=== Original Implementation (Unfair) ===");
        UnisexBathroom.runTest();

        System.out.println("\n\n=== Fair Implementation with Gender Rotation ===");
        FairUnisexBathroom.runTest();
    }
}

class UnisexBathroom {

    static String WOMEN = "women";
    static String MEN = "men";
    static String NONE = "none";

    String inUseBy = NONE;
    int empsInBathroom = 0;
    Semaphore maxEmps = new Semaphore(3);

    void useBathroom(String name) throws InterruptedException {
        System.out.println("\n" + name + " using bathroom. Current employees in bathroom = " + empsInBathroom + " " + System.currentTimeMillis());
        Thread.sleep(3000);
        System.out.println("\n" + name + " done using bathroom " + System.currentTimeMillis());
    }

    void maleUseBathroom(String name) throws InterruptedException {
        synchronized (this) {
            while (inUseBy.equals(WOMEN)) {
                this.wait();
            }
            maxEmps.acquire();
            empsInBathroom++;
            inUseBy = MEN;
        }

        useBathroom(name);
        maxEmps.release();

        synchronized (this) {
            empsInBathroom--;

            if (empsInBathroom == 0) inUseBy = NONE;
            this.notifyAll();
        }
    }

    void femaleUseBathroom(String name) throws InterruptedException {
        synchronized (this) {
            while (inUseBy.equals(MEN)) {
                this.wait();
            }
            maxEmps.acquire();
            empsInBathroom++;
            inUseBy = WOMEN;
        }

        useBathroom(name);
        maxEmps.release();

        synchronized (this) {
            empsInBathroom--;

            if (empsInBathroom == 0) inUseBy = NONE;
            this.notifyAll();
        }
    }

    public static void runTest() throws InterruptedException {

        final UnisexBathroom unisexBathroom = new UnisexBathroom();

        Thread female1 = new Thread(new Runnable() {
            public void run() {
                try {
                    unisexBathroom.femaleUseBathroom("Lisa");
                } catch (InterruptedException ie) {

                }
            }
        });

        Thread male1 = new Thread(new Runnable() {
            public void run() {
                try {
                    unisexBathroom.maleUseBathroom("John");
                } catch (InterruptedException ie) {

                }
            }
        });

        Thread male2 = new Thread(new Runnable() {
            public void run() {
                try {
                    unisexBathroom.maleUseBathroom("Bob");
                } catch (InterruptedException ie) {

                }
            }
        });

        Thread male3 = new Thread(new Runnable() {
            public void run() {
                try {
                    unisexBathroom.maleUseBathroom("Anil");
                } catch (InterruptedException ie) {

                }
            }
        });

        Thread male4 = new Thread(new Runnable() {
            public void run() {
                try {
                    unisexBathroom.maleUseBathroom("Wentao");
                } catch (InterruptedException ie) {

                }
            }
        });

        female1.start();
        male1.start();
        male2.start();
        male3.start();
        male4.start();

        female1.join();
        male1.join();
        male2.join();
        male3.join();
        male4.join();

    }
}




// Solution 1: Fair implementation with gender rotation
class FairUnisexBathroom {
    static String WOMEN = "women";
    static String MEN = "men";
    static String NONE = "none";

    String inUseBy = NONE;
    int empsInBathroom = 0;
    Semaphore maxEmps = new Semaphore(3);

    // Fairness control
    private ReentrantLock lock = new ReentrantLock();
    private Condition womenCondition = lock.newCondition();
    private Condition menCondition = lock.newCondition();

    // Track waiting threads
    private int waitingWomen = 0;
    private int waitingMen = 0;
    private boolean womenTurn = true; // Alternate between genders
    private int consecutiveUsers = 0; // Track consecutive users of same gender
    private static final int MAX_CONSECUTIVE = 4; // Max consecutive users before switching

    void useBathroom(String name) throws InterruptedException {
        System.out.println("\n" + name + " using bathroom. Current employees in bathroom = " + empsInBathroom + " " + System.currentTimeMillis());
        Thread.sleep(1000); // Reduced time to see rotation better
        System.out.println("\n" + name + " done using bathroom " + System.currentTimeMillis());
    }

    void maleUseBathroom(String name) throws InterruptedException {
        lock.lock();
        try {
            waitingMen++;
            System.out.println(name + " wants to use bathroom. Waiting men: " + waitingMen + ", Waiting women: " + waitingWomen + ", Women's turn: " + womenTurn + ", Consecutive: " + consecutiveUsers);

            // Wait if women are using OR if it's women's turn and there are waiting women
            // OR if we've had too many consecutive users of the same gender
            while (inUseBy.equals(WOMEN) ||
                    (womenTurn && waitingWomen > 0 && inUseBy.equals(NONE)) ||
                    (inUseBy.equals(MEN) && consecutiveUsers >= MAX_CONSECUTIVE && waitingWomen > 0)) {
                System.out.println(name + " waiting... (women's turn: " + womenTurn + ", waiting women: " + waitingWomen + ", consecutive: " + consecutiveUsers + ")");
                menCondition.await();
            }

            waitingMen--;
            maxEmps.acquire();
            empsInBathroom++;
            inUseBy = MEN;
            consecutiveUsers++;
            System.out.println(name + " entering bathroom. Now men's turn: " + !womenTurn + ", consecutive: " + consecutiveUsers);
        } finally {
            lock.unlock();
        }

        useBathroom(name);
        maxEmps.release();

        lock.lock();
        try {
            empsInBathroom--;
            if (empsInBathroom == 0) {
                inUseBy = NONE;
                consecutiveUsers = 0; // Reset consecutive counter

                // Switch turn to women if there are waiting women
                if (waitingWomen > 0) {
                    womenTurn = true;
                    System.out.println("Bathroom empty. Switching to women's turn. Waiting women: " + waitingWomen);
                    womenCondition.signalAll();
                } else if (waitingMen > 0) {
                    womenTurn = false;
                    System.out.println("Bathroom empty. Staying men's turn. Waiting men: " + waitingMen);
                    menCondition.signalAll();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    void femaleUseBathroom(String name) throws InterruptedException {
        lock.lock();
        try {
            waitingWomen++;
            System.out.println(name + " wants to use bathroom. Waiting women: " + waitingWomen + ", Waiting men: " + waitingMen + ", Women's turn: " + womenTurn + ", Consecutive: " + consecutiveUsers);

            // Wait if men are using OR if it's men's turn and there are waiting men
            // OR if we've had too many consecutive users of the same gender
            while (inUseBy.equals(MEN) ||
                    (!womenTurn && waitingMen > 0 && inUseBy.equals(NONE)) ||
                    (inUseBy.equals(WOMEN) && consecutiveUsers >= MAX_CONSECUTIVE && waitingMen > 0)) {
                System.out.println(name + " waiting... (women's turn: " + womenTurn + ", waiting men: " + waitingMen + ", consecutive: " + consecutiveUsers + ")");
                womenCondition.await();
            }

            waitingWomen--;
            maxEmps.acquire();
            empsInBathroom++;
            inUseBy = WOMEN;
            consecutiveUsers++;
            System.out.println(name + " entering bathroom. Now women's turn: " + womenTurn + ", consecutive: " + consecutiveUsers);
        } finally {
            lock.unlock();
        }

        useBathroom(name);
        maxEmps.release();

        lock.lock();
        try {
            empsInBathroom--;
            if (empsInBathroom == 0) {
                inUseBy = NONE;
                consecutiveUsers = 0; // Reset consecutive counter

                // Switch turn to men if there are waiting men
                if (waitingMen > 0) {
                    womenTurn = false;
                    System.out.println("Bathroom empty. Switching to men's turn. Waiting men: " + waitingMen);
                    menCondition.signalAll();
                } else if (waitingWomen > 0) {
                    womenTurn = true;
                    System.out.println("Bathroom empty. Staying women's turn. Waiting women: " + waitingWomen);
                    womenCondition.signalAll();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public static void runTest() throws InterruptedException {
        final FairUnisexBathroom bathroom = new FairUnisexBathroom();

        System.out.println("\n=== Gender Rotation Test: 4 Consecutive Users Limit ===");
        System.out.println("Scenario: 1 female using, then 5 females + 2 males arrive");
        System.out.println("Expected: 4 consecutive females, then 2 males, then remaining females");

        // Create threads: 1 female first, then 5 more females and 2 males arrive
        Thread female1 = new Thread(() -> {
            try {
                bathroom.femaleUseBathroom("Female-1");
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        });

        // 5 more females arrive while Female-1 is using
        Thread female2 = new Thread(() -> {
            try {
                Thread.sleep(200); // Arrives while Female-1 is using
                bathroom.femaleUseBathroom("Female-2");
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        });

        Thread female3 = new Thread(() -> {
            try {
                Thread.sleep(400);
                bathroom.femaleUseBathroom("Female-3");
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        });

        Thread female4 = new Thread(() -> {
            try {
                Thread.sleep(600);
                bathroom.femaleUseBathroom("Female-4");
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        });

        Thread female5 = new Thread(() -> {
            try {
                Thread.sleep(800);
                bathroom.femaleUseBathroom("Female-5");
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        });

        Thread female6 = new Thread(() -> {
            try {
                Thread.sleep(1000);
                bathroom.femaleUseBathroom("Female-6");
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        });

        // 2 males arrive while females are using
        Thread male1 = new Thread(() -> {
            try {
                Thread.sleep(300); // Arrives while females are using
                bathroom.maleUseBathroom("Male-1");
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        });

        Thread male2 = new Thread(() -> {
            try {
                Thread.sleep(500);
                bathroom.maleUseBathroom("Male-2");
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        });

        // Start all threads
        female1.start();
        female2.start();
        female3.start();
        female4.start();
        female5.start();
        female6.start();
        male1.start();
        male2.start();

        // Wait for all threads
        female1.join();
        female2.join();
        female3.join();
        female4.join();
        female5.join();
        female6.join();
        male1.join();
        male2.join();
    }
}
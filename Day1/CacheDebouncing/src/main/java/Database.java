public class Database {
    public String getData() throws InterruptedException {
        try {
            System.out.println("fetching from database");
            Thread.sleep(500);
            System.out.println("fetching from database completed");
            return "data";
        } catch (Exception e) {
            throw e;
        }

    }
}

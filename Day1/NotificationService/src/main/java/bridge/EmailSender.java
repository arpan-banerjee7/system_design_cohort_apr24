package bridge;

public class EmailSender implements NotificationSender {

    @Override
    public void sendNotification(String message) {
        System.out.println("Sending an EMAIL notification: " + message);
    }
}

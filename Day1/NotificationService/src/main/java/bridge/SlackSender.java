package bridge;

public class SlackSender implements NotificationSender {
    @Override
    public void sendNotification(String message) {
        System.out.println("Sending a SLACK notification: " + message);
    }
}

package bridge;

public class RegularNotification extends Notification {

    public RegularNotification(NotificationSender notificationSender) {
        super(notificationSender);
    }

    @Override
    public void send(String message) {
        notificationSender.sendNotification("Regular notification: " + message);
    }
}

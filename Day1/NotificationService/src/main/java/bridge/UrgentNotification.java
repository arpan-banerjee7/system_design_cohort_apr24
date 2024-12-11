package bridge;

public class UrgentNotification extends Notification {
    public UrgentNotification(NotificationSender notificationSender) {
        super(notificationSender);
    }

    @Override
    public void send(String message) {
        notificationSender.sendNotification("URGENT notification: " + message);
    }
}

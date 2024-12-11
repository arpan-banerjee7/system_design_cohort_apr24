package bridge;

public abstract class Notification {
    NotificationSender notificationSender;

    public Notification(NotificationSender notificationSender) {
        this.notificationSender = notificationSender;
    }

    public abstract void send(String message);
}

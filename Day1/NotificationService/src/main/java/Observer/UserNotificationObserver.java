package Observer;


import bridge.Notification;

public class UserNotificationObserver implements Observer {
    private User user; // Associated user
    private Notification notificationPreference; // How the user prefers to receive notifications

    public UserNotificationObserver(User user, Notification notificationPreference) {
        this.user = user;
        this.notificationPreference = notificationPreference;
    }

    @Override
    public void update(String message) {
        notificationPreference.send(user.getName() + " is being notified: " + message);
    }
}



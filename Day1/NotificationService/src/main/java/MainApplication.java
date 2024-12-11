
import Observer.*;
import Observer.UserNotificationObserver;
import bridge.*;

/*

Class Structure:

|-- NotificationSender (Bridge Implementor Interface)
|    |-- sendNotification(String)

|-- EmailSender (Concrete Implementor for NotificationSender)
|    |-- sendNotification(String)

|-- SlackSender (Concrete Implementor for NotificationSender)
|    |-- sendNotification(String)

|-- Notification (Bridge Abstraction)
|    |-- NotificationSender notificationSender
|    |-- send(String message)

|-- RegularNotification (Concrete Abstraction extending Notification)
|    |-- send(String message)

|-- UrgentNotification (Concrete Abstraction extending Notification)
|    |-- send(String message)

|-- Subject (Observer Pattern Interface)
|    |-- register(Observer observer)
|    |-- remove(Observer observer)
|    |-- notifyObservers(String message)

|-- HolidayScheduleSubject (Concrete Subject implementing Subject)
|    |-- register(Observer observer)
|    |-- remove(Observer observer)
|    |-- notifyObservers(String message)
|    |-- newEvent(String eventMessage)

|-- FireDrillSubject (Concrete Subject implementing Subject)
|    |-- register(Observer observer)
|    |-- remove(Observer observer)
|    |-- notifyObservers(String message)
|    |-- newEvent(String eventMessage)

|-- Observer (Observer Pattern Interface)
|    |-- update(String message)

|-- UserNotificationObserver (Concrete Observer implementing Observer)
|    |-- User user
|    |-- Notification notificationPreference
|    |-- update(String message)

|-- User (Class representing a User)
|    |-- String name
|    |-- String email
*/


/*
// Manged by bridge design pattern
Notification channels/medium---> Email, slack
Notification type---> Urgent/Regular

// Managed by Observer pattern
Subject---> Interface for managing observers/subscribers, adds, removes and notifies all observers
Observer--> Concrete objects, of different types of notification events. It takes in users preferred channel and type of
notification and triggers to them.

 */
public class MainApplication {
    public static void main(String[] args) {
// USER(Observer, which has subscribed to a subject which is a type of event, lets say holiday)--->NOTIFICATION(urgent,regular)---->NOTIFICATION SENDER(email,slack)

        // bridge (notification sending medium)
        NotificationSender emailSender = new EmailSender();
        NotificationSender slackSender = new SlackSender();

        /** Example 1 **/
        // Subject (Manges list of observers and triggers their preferred notification type though their preferred notification medium
        HolidayScheduleSubject aliceHolidaySubject = new HolidayScheduleSubject();

        // Alice registers to email notification for regular holiday calendar events, slack for urgent holiday events
        User alice = new User("Alice", "alice@gmail.com");

        // bridge (notification type)
        Notification aliceRegularNotification = new RegularNotification(emailSender);
        Notification aliceUrgentNotification = new UrgentNotification(slackSender);

        // Observer (Notification preferences)
        Observer aliceRegularObserver = new UserNotificationObserver(alice, aliceRegularNotification);
        Observer aliceUrgentObserver = new UserNotificationObserver(alice, aliceUrgentNotification);

        aliceHolidaySubject.register(aliceRegularObserver);
        aliceHolidaySubject.register(aliceUrgentObserver);

        // Simulate a new event
        aliceHolidaySubject.newEvent("Holiday declared on Aug 25, 2024");

        //***********************************************************************************************************//

        /** Example 2 **/
        // Subject (Manges list of observers and triggers their preferred notification type though their preferred notification medium
        HolidayScheduleSubject bobHolidaySubject = new HolidayScheduleSubject();

        // Bob registers to Slack notifications for regular holiday calendar events, email for urgent holiday events
        User bob = new User("BOB", "bob@gmail.com");

        // bridge
        Notification bobRegularNotification = new RegularNotification(slackSender);
        Notification bobUrgentNotification = new UrgentNotification(emailSender);

        // Observer
        Observer bobRegularObserver = new UserNotificationObserver(bob, bobRegularNotification);
        Observer bobUrgentObserver = new UserNotificationObserver(bob, bobUrgentNotification);

        bobHolidaySubject.register(bobRegularObserver);
        bobHolidaySubject.register(bobUrgentObserver);

        // Simulate a new event
        bobHolidaySubject.newEvent("Holiday declared on October 12, 2025");

    }

}
package Observer;

import java.util.ArrayList;
import java.util.List;

public class HolidayScheduleSubject implements Subject {
    List<Observer> observers;

    public HolidayScheduleSubject() {
        this.observers = new ArrayList<>();
    }

    @Override
    public void register(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void remove(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }

    public void newEvent(String eventMessage) {
        System.out.println("***************");
        System.out.println("New event occurred, triggering notifications to observer: " + eventMessage);
        notifyObservers(eventMessage);
    }
}

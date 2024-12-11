package Observer;

import java.util.ArrayList;
import java.util.List;

// another type of event observer
public class FireDrillSubject implements Subject {
    List<Observer> observers;

    public FireDrillSubject() {
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
        System.out.println("New event occurred, triggering notifications to observer: " + eventMessage);
        notifyObservers(eventMessage);
    }
}

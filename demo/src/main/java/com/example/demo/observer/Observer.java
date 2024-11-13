package main.java.com.example.demo.observer;


import main.java.com.example.demo.events.Event;

public interface Observer<E extends Event> {
    void update(E e);
}
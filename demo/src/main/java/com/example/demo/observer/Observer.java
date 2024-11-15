package com.example.demo.observer;


import com.example.demo.events.Event;

public interface Observer<E extends Event> {
    void update(E e);
}
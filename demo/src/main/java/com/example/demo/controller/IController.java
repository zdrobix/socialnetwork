package com.example.demo.controller;

import com.example.demo.events.EntityChangeEvent;
import com.example.demo.observer.Observer;
import com.example.demo.service.Service;

public abstract class IController implements Observer<EntityChangeEvent> {
    abstract void setController(Service service);
}

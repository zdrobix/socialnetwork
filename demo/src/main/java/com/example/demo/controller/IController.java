package com.example.demo.controller;

import com.example.demo.service.Context;
import com.example.demo.events.EntityChangeEvent;
import com.example.demo.observer.Observer;
import com.example.demo.service.Service;

public abstract class IController implements Observer<EntityChangeEvent> {
    public Context context;

    abstract void setController(Service service);

    public void setContext (Context context_) {
        this.context = context_;
    }
}

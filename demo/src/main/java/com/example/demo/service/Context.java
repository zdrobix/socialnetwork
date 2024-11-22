package com.example.demo.service;

import com.example.demo.domain.Utilizator;

public class Context {

    private Utilizator currentUser;

    public Context () {}
    public Context (Utilizator currentUser) { this.currentUser = currentUser; }

    public Utilizator getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(Utilizator currentUser) {
        this.currentUser = currentUser;
    }
}

package com.example.demo.domain;

import com.example.demo.logs.Logger;
import com.example.demo.password.Crypter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Username extends Entity<String>{

    private final String keycryptPasswordPath = "C:\\Users\\Alex\\Desktop\\key.txt";
    private String keycryptPassword = "";

    private String username;
    private String password;

    public Username() {}

    public Username(String username, String password) {
        this.username = username;
        try {
            this.keycryptPassword = new Scanner(new File(this.keycryptPasswordPath)).nextLine();
        } catch (FileNotFoundException e) {}
        try {
            this.password = Crypter.encrypt(password, this.keycryptPassword);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }
}

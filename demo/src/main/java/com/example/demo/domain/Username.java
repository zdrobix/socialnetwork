package com.example.demo.domain;

import com.example.demo.logs.Logger;
import com.example.demo.password.Crypter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Username extends Entity<String>{

    private static String keycryptPasswordPath = "C:\\Users\\Alex\\Desktop\\key.txt";
    private static String keycryptPassword = "";

    private String username;
    private String password;
    private Long id;

    public Username() {}

    public Username(String username, String password, Long id) {
        this.username = username;
        this.id = id;
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

    public Long getIdLong () {
        return this.id;
    }
}

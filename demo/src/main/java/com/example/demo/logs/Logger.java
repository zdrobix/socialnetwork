package com.example.demo.logs;

import java.io.FileWriter;
import java.time.LocalDateTime;

public class Logger {
    private static final String logFilename = "Q:\\info\\java\\lab3\\demo\\src\\main\\java\\com\\example\\demo\\logs\\log_connect.txt";
    private static final String logModify = "Q:\\info\\java\\lab3\\demo\\src\\main\\java\\com\\example\\demo\\logs\\log_modifications.txt";
    private static final String logException = "Q:\\info\\java\\lab3\\demo\\src\\main\\java\\com\\example\\demo\\logs\\log_exceptions.txt";

    public Logger () {};

    public void LogConnection (boolean connected) {
        try {
            FileWriter fw = new FileWriter(
                    logFilename,
                    true);
            if (connected)
                fw.append("Connection established. ").append(String.valueOf(LocalDateTime.now())).append(String.valueOf('\n'));
            else {
                fw.append("Connection failed. ").append(String.valueOf(LocalDateTime.now())).append(String.valueOf('\n'));
            }
            fw.close();
        } catch (Exception ie) {
            System.out.println(ie.getMessage());
        }
    }

    public void LogModify (String operation, String parameter) {
        try {
            FileWriter fw = new FileWriter(
                    logModify,
                    true);
            switch (operation) {
                case "save": {
                    fw.append("Saving: ").append(parameter).append(String.valueOf(LocalDateTime.now())).append(String.valueOf('\n'));
                    break;
                }
                case "findOne": {
                    fw.append("Looking for: ").append(parameter).append(String.valueOf(LocalDateTime.now())).append(String.valueOf('\n'));
                    break;
                }
                case "findAll": {
                    fw.append("Looking for all: ").append(String.valueOf(LocalDateTime.now())).append(String.valueOf('\n'));
                    break;
                }
                case "delete": {
                    fw.append("Deleting: ").append(parameter).append(String.valueOf(LocalDateTime.now())).append(String.valueOf('\n'));
                    break;
                }
                default: break;
            }
            fw.close();
        } catch (Exception ie) {
            System.out.println(ie.getMessage());
        }
    }

    public static void LogException(String operation, String parameter, String message) {
        try {
            FileWriter fw = new FileWriter(
                    logException,
                    true
            );
            switch (operation) {
                case "save": {
                    fw.append("Tried to save: ").append(parameter).append(String.valueOf(LocalDateTime.now())).append(String.valueOf('\n'));
                    fw.append('\n' + message);
                    break;
                }
                case "findOne": {
                    fw.append("Tried to find for: ").append(parameter).append(String.valueOf(LocalDateTime.now())).append(String.valueOf('\n'));
                    fw.append('\n' + message);
                    break;
                }
                case "findAll": {
                    fw.append("Tried to look for all: ").append(String.valueOf(LocalDateTime.now())).append(String.valueOf('\n'));
                    fw.append('\n' + message);
                    break;
                }
                case "delete": {
                    fw.append("Tried to delete: ").append(parameter).append(String.valueOf(LocalDateTime.now())).append(String.valueOf('\n'));
                    fw.append('\n' + message);
                    break;
                }
                case "connect": {
                    fw.append("Tried to connect: ").append(String.valueOf(LocalDateTime.now())).append(String.valueOf('\n'));
                    fw.append('\n' + message);
                }
                default: break;
            }
        } catch (Exception e) {
            System.out.println("Exception unhandled. " + e.getMessage());
        }
    }
}

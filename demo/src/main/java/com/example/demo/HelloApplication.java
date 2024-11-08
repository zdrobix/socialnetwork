package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

import com.example.demo.controller.UtilizatorController;
import com.example.demo.service.Service;
import com.example.demo.repo.db.UserDatabaseRepository;
import com.example.demo.repo.db.FriendshipDatabaseRepository;

import com.example.demo.domain.validators.UtilizatorValidator;
import com.example.demo.domain.validators.PrietenieValidator;

public class HelloApplication extends Application {
    Service service;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        service = new Service(
                        new UserDatabaseRepository(
                                "jdbc:postgresql://localhost:5432/socialnetwork",
                                "postgres",
                                "parola",
                                new UtilizatorValidator()
                        ),
                        new FriendshipDatabaseRepository(
                                "jdbc:postgresql://localhost:5432/socialnetwork",
                                "postgres",
                                "parola",
                                new PrietenieValidator()
                        ));
        initView(primaryStage);
        primaryStage.setWidth(800);
        primaryStage.show();
    }

    private void initView(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/demo/UtilizatorView.fxml"));

        VBox userLayout = fxmlLoader.load();
        primaryStage.setScene(new Scene(userLayout));

        UtilizatorController userController = fxmlLoader.getController();
        userController.setUtilizatorService(service);
    }
}
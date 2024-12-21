package com.example.demo;

import com.example.demo.repo.db.*;
import com.example.demo.service.Context;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;

import com.example.demo.controller.MainMenuController;
import com.example.demo.service.Service;

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
                                "password",
                                new UtilizatorValidator()
                        ),
                        new FriendshipDatabaseRepository(
                                "jdbc:postgresql://localhost:5432/socialnetwork",
                                "postgres",
                                "password",
                                new PrietenieValidator()
                        ),
                        new FriendRequestDatabaseRepository(
                                "jdbc:postgresql://localhost:5432/socialnetwork",
                                "postgres",
                                "password",
                                new PrietenieValidator()
                        ),
                        new UserLoginDatabaseRepository(
                                "jdbc:postgresql://localhost:5432/socialnetwork",
                                "postgres",
                                "password"
                        ),
                        new MessageDatabaseRepository(
                                "jdbc:postgresql://localhost:5432/socialnetwork",
                                "postgres",
                                "password"
                        ));
        Stage stage = new Stage();
        initView(primaryStage);
        initView(stage);
        primaryStage.setWidth(800);
        stage.setWidth(800);
        stage.show();
        primaryStage.show();
    }

    private void initView(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/demo/MainMenuView.fxml"));

        HBox userLayout = fxmlLoader.load();
        primaryStage.setScene(new Scene(userLayout));

        MainMenuController menuController = fxmlLoader.getController();
        menuController.setContext(new Context(this.service.getUtilizator(1L)));
        menuController.setController(service);
        menuController.handleHome();
    }
}
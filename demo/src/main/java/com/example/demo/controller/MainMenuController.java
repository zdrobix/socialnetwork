package com.example.demo.controller;

import com.example.demo.events.EntityChangeEvent;
import com.example.demo.service.Context;
import com.example.demo.service.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainMenuController extends IController {
    private Service service;
    @FXML
    private StackPane mainContent;

    @FXML
    public void handleHome() {
        loadView("/com/example/demo/HomeView.fxml", super.context);

    }

    @FXML
    public void handleFriends() {
        if (super.context.getCurrentUser() == null)
            return;
        loadView("/com/example/demo/FriendsView.fxml", super.context);
    }

    @FXML
    public void handleAccount() {
        loadView("/com/example/demo/AccountView.fxml", super.context);
    }

    private void loadView(String fxmlFile, Context context) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            mainContent
                    .getChildren()
                    .setAll(
                            (Node) loader.load()
                    );
            IController controller = loader.getController();
            controller.setContext(context);
            controller.setController(this.service);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleAboutUs(ActionEvent actionEvent) {
    }

    public void setController (Service service_) {
        this.service = service_;
    }

    @Override
    public void update(EntityChangeEvent utilizatorEntityChangeEvent) {

    }
}

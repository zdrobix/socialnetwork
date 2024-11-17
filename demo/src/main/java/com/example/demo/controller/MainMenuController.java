package com.example.demo.controller;

import com.example.demo.events.EntityChangeEvent;
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
        loadView("/com/example/demo/HomeView.fxml");

    }

    @FXML
    public void handleFriends() {
        if (this.service.currentUser == null)
            return;
        loadView("/com/example/demo/FriendsView.fxml");
    }

    @FXML
    public void handleAccount() {
        loadView("/com/example/demo/AccountView.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            mainContent
                    .getChildren()
                    .setAll(
                            (Node) loader.load()
                    );
            IController controller = loader.getController();
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

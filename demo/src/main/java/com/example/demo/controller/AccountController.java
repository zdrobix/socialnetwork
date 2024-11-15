package com.example.demo.controller;

import com.example.demo.service.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class AccountController implements IController {

    private Service service;

    @FXML
    private VBox loginVbox;
    @FXML
    private VBox signupVbox;
    @FXML
    private Label labelLogIn, labelSignUp;

    @FXML
    private TextField usernameTextFieldLogIn;

    @FXML
    private TextField passwordTextFieldLogIn;

    @FXML
    private TextField usernameTextFieldSignUp;

    @FXML
    private TextField firstNameTextFieldSignUp;

    @FXML
    private TextField lastNameTextFieldSignUp;

    @FXML
    private TextField passwordTextFieldSignUp;

    public void handleLogin(ActionEvent actionEvent) {
        this.service.login(
                usernameTextFieldLogIn.getText(),
                passwordTextFieldLogIn.getText()
        );
    }

    public void handleSignup(ActionEvent actionEvent) {
    }

    public void showLoginVbox(MouseEvent mouseEvent) {
        signupVbox.setVisible(false);
        loginVbox.setVisible(true);
        labelLogIn.setUnderline(true);
        labelSignUp.setUnderline(false);
    }

    public void showSignupVbox(MouseEvent mouseEvent) {
        signupVbox.setVisible(true);
        loginVbox.setVisible(false);
        labelLogIn.setUnderline(false);
        labelSignUp.setUnderline(true);
    }

    public void setController (Service service_) {
        this.service = service_;
    }
}

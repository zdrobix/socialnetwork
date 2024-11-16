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
    private VBox userInfoVbox;
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

    @FXML
    private Label firstnameLabel, lastnameLabel;

    public void handleLogin(ActionEvent actionEvent) {
        if ( this.service.login(
                usernameTextFieldLogIn.getText(),
                passwordTextFieldLogIn.getText())) {
            firstnameLabel.setText("First Name: " + this.service.currentUser.getFirstName());
            lastnameLabel.setText("Last Name: " + this.service.currentUser.getLastName());
            loginVbox.setVisible(false);
            labelLogIn.setVisible(false);
            labelSignUp.setVisible(false);
            userInfoVbox.setVisible(true);
        }
        usernameTextFieldLogIn.clear();
        passwordTextFieldLogIn.clear();
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

    public void handleLogout(ActionEvent actionEvent) {
        this.service.currentUser = null;
        userInfoVbox.setVisible(false);
        this.showLoginVbox(null);
    }
}

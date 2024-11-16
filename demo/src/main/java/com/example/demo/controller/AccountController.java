package com.example.demo.controller;

import com.example.demo.events.EntityChangeEvent;
import com.example.demo.service.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class AccountController extends IController {

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
    private PasswordField passwordTextFieldLogIn;

    @FXML
    private TextField usernameTextFieldSignUp;

    @FXML
    private TextField firstNameTextFieldSignUp;

    @FXML
    private TextField lastNameTextFieldSignUp;

    @FXML
    private PasswordField passwordTextFieldSignUp;

    @FXML
    private Label firstnameLabel, lastnameLabel;

    public void handleLogin(ActionEvent actionEvent) {
        if ( this.service.login(
                usernameTextFieldLogIn.getText(),
                passwordTextFieldLogIn.getText())) {
                this.showUserInfo();
        }
        usernameTextFieldLogIn.clear();
        passwordTextFieldLogIn.clear();
    }

    public void showUserInfo() {
        firstnameLabel.setText("First Name: " + this.service.currentUser.getFirstName());
        lastnameLabel.setText("Last Name: " + this.service.currentUser.getLastName());
        loginVbox.setVisible(false);
        labelLogIn.setVisible(false);
        labelSignUp.setVisible(false);
        userInfoVbox.setVisible(true);
    }

    public void handleSignup(ActionEvent actionEvent) {
        this.service.addUtilizator(
                firstNameTextFieldSignUp.getText(),
                lastNameTextFieldSignUp.getText(),
                usernameTextFieldSignUp.getText(),
                passwordTextFieldSignUp.getText()
        );
        firstNameTextFieldSignUp.clear();
        lastNameTextFieldSignUp.clear();
        usernameTextFieldSignUp.clear();
        passwordTextFieldSignUp.clear();
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
        if (this.service.currentUser != null)
            this.showUserInfo();
        else this.showLoginVbox(null);
    }

    public void handleLogout(ActionEvent actionEvent) {
        this.service.currentUser = null;
        userInfoVbox.setVisible(false);
        this.showLoginVbox(null);
    }

    @Override
    public void update(EntityChangeEvent utilizatorEntityChangeEvent) {
        //does nothing
    }
}

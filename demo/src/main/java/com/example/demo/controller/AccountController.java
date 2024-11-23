package com.example.demo.controller;

import com.example.demo.domain.Utilizator;
import com.example.demo.events.ChangeEventType;
import com.example.demo.events.EntityChangeEvent;
import com.example.demo.service.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;


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
    private TextField firstNameTextUserInfo;
    @FXML
    private TextField lastNameTextUserInfo;

    public void handleLogin(ActionEvent actionEvent) {
        super.context.setCurrentUser(this.service.login(
                                        usernameTextFieldLogIn.getText(),
                                        passwordTextFieldLogIn.getText()
                )
        );
        if (super.context.getCurrentUser() != null)
                this.showUserInfo();
        usernameTextFieldLogIn.clear();
        passwordTextFieldLogIn.clear();
    }

    public void showUserInfo() {
        firstNameTextUserInfo.setText(super.context.getCurrentUser().getFirstName());
        lastNameTextUserInfo.setText(super.context.getCurrentUser().getLastName());
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
        labelLogIn.setVisible(true);
        labelSignUp.setVisible(true);
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

    public void handleLogout(ActionEvent actionEvent) {
        super.context.setCurrentUser(null);
        userInfoVbox.setVisible(false);
        this.showLoginVbox(null);
    }

    public void handleUpdate(ActionEvent actionEvent) {
        Utilizator user = new Utilizator(
                this.firstNameTextUserInfo.getText(),
                this.lastNameTextUserInfo.getText()
        );
        user.setId(super.context.getCurrentUser().getId());
        this.service.updateUtilizator(
            user
        );

    }

    public void handleUploadPhoto(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        Stage fileChooserStage = new Stage();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png"));

        File uploadedPhoto = fileChooser.showOpenDialog(fileChooserStage);
        if (uploadedPhoto == null)
            return;
        File destinationFolder = new File("Q:/info/java/lab3/demo/src/main/resources/user_photos");
        String fileName = super.context.getCurrentUser().getFirstName()
                        + super.context.getCurrentUser().getLastName()
                        + super.context.getCurrentUser().getId()
                        + ".png";
        File file = new File(destinationFolder, fileName);
        try {
            Files.copy(uploadedPhoto.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void setController (Service service_) {
        this.service = service_;
        if (super.context.getCurrentUser() != null)
            this.showUserInfo();
        else this.showLoginVbox(null);
        this.addListenerEnter();
    }

    @Override
    public void update(EntityChangeEvent utilizatorEntityChangeEvent) {
        if (utilizatorEntityChangeEvent.getType() == ChangeEventType.DELETE && utilizatorEntityChangeEvent.getData() instanceof Utilizator) {
            if (super.context.getCurrentUser() == utilizatorEntityChangeEvent.getData()) {
                this.handleLogout(new ActionEvent());
            }
        }
    }


    private void addListenerEnter() {
        passwordTextFieldLogIn.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin(new ActionEvent());
            }
        });
    }
}

package com.example.demo.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import com.example.demo.domain.Utilizator;
import com.example.demo.events.UtilizatorEntityChangeEvent;
import com.example.demo.observer.Observer;
import com.example.demo.service.Service;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.SQLException;


public class UtilizatorController implements Observer<UtilizatorEntityChangeEvent> {
    Service service;
    ObservableList<Utilizator> model = FXCollections.observableArrayList();

    @FXML
    private TableView<Utilizator> tableView;
    @FXML
    private TableColumn<Utilizator,String> tableColumnFirstName;
    @FXML
    private TableColumn<Utilizator,String> tableColumnLastName;
    @FXML
    private Label currentUserLabel;

    @FXML
    private TextArea logInText;
    @FXML
    private TextArea firstNameText;
    @FXML
    private TextArea lastNameText;
    @FXML
    private TextArea deleteIdText;

    public void setUtilizatorService(Service service) {
        this.service = service;
        initModel();
    }

    @FXML
    public void initialize() {
        this.tableColumnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        this.tableColumnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        this.tableView.setMaxHeight(200);
        this.tableView.setMaxWidth(200);
        this.tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void initModel() {
        this.model.clear();
        for(Utilizator utilizator : service.getAll()) {
            this.model.add(utilizator);
        }
        this.tableView.setItems(model);
    }

    @Override
    public void update(UtilizatorEntityChangeEvent utilizatorEntityChangeEvent) {

    }

    public void handleDeleteUtilizator(ActionEvent actionEvent)  {
        this.service.deleteUtilizator(
                Long.parseLong(
                    this.deleteIdText.getText().trim()
                )
        );
        this.initModel();
        this.deleteIdText.clear();
    }

    public void handleUpdateUtilizator(ActionEvent actionEvent) {
    }

    public void handleAddUtilizator(ActionEvent actionEvent) {
        this.service.addUtilizator(
            this.firstNameText.getText().trim(),
            this.lastNameText.getText().trim()
        );
        this.initModel();
        this.firstNameText.clear();
        this.lastNameText.clear();
    }

    public void handleLogInButton(ActionEvent actionEvent) {
        var splitCredentials = this.logInText
                                        .getText()
                                        .split(" ");
        if (splitCredentials.length != 2) {
            this.currentUserLabel.setText("Invalid format");
            return;
        }
        try {
            if (this.service.login(Long.parseLong(splitCredentials[0]), splitCredentials[1])) {
                this.currentUserLabel.setText("Current user: " + this.service.currentUser.getFirstName() + " " + this.service.currentUser.getLastName());
            } else this.currentUserLabel.setText("Current user: Failed to login");
        } catch (Exception ex) {
            this.currentUserLabel.setText("Invalid format for ID and NAME");
        }
        this.logInText.clear();
    }
}

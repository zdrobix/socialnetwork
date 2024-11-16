package com.example.demo.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextArea;

import com.example.demo.domain.Cerere;
import com.example.demo.domain.Utilizator;
import com.example.demo.events.EntityChangeEvent;
import com.example.demo.observer.Observer;
import com.example.demo.service.Service;

import java.time.LocalDate;

public class UtilizatorController implements Observer<EntityChangeEvent> {
    Service service;
    ObservableList<Utilizator> model = FXCollections.observableArrayList();
    ObservableList<Cerere> modelCerere = FXCollections.observableArrayList();
    Long selectedId = null;
    Long selectedRequestId = null;

    @FXML
    private TableView<Utilizator> tableViewFriends;
    @FXML
    private TableColumn<Utilizator,String> tableColumnFirstName;
    @FXML
    private TableColumn<Utilizator,String> tableColumnLastName;

    @FXML
    private TableView<Cerere> tableViewRequest;
    @FXML
    private TableColumn<Cerere,String> tableColumnRequestFrom = new TableColumn<>("From");
    @FXML
    private TableColumn<Cerere,LocalDate> tableColumnRequestDate = new TableColumn<>("Date");

    @FXML
    private Label currentUserLabel;
    @FXML
    private Label friendLabel;

    @FXML
    private TextArea logInText;
    @FXML
    private TextArea firstNameText;
    @FXML
    private TextArea lastNameText;

    public void setUtilizatorService(Service service) {
        this.service = service;
        initModel();
    }

    @FXML
    public void initialize() {
        this.tableColumnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        this.tableColumnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        this.tableViewFriends.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.tableViewFriends.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        this.selectedId = this.service
                                .getUtilizatorName(
                                        newValue.getFirstName(),
                                        newValue.getLastName()
                                )
                                .getId();
                        System.out.println(selectedId);
                    }
                });

        this.tableColumnRequestFrom.setCellValueFactory(data -> {
            Cerere cerere = data.getValue();
            var user = this.service.getUtilizator(cerere.getFrom());
            return new SimpleStringProperty(user.getFirstName() + " " + user.getLastName());
        });
        this.tableColumnRequestDate.setCellValueFactory(data -> {
            Cerere cerere = data.getValue();
            LocalDate requestDate = cerere.getDate();
            return new SimpleObjectProperty<>(requestDate);
        });
        this.tableViewRequest.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.tableViewRequest.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldVallue, newValue) -> {
                    if (newValue != null) {
                        this.selectedRequestId = newValue.getFrom();
                    }
                });
    }

    private void initModel() {
        this.model.clear();
        for(Utilizator utilizator : this.service.getAll()) {
            this.model.add(utilizator);
        }
        this.tableViewFriends.setItems(this.model);

    }

    private void initModelRequest() {
        this.modelCerere.clear();
        if (this.service.getCereriForCurrent() == null)
            return;
        for (Cerere cerere : this.service.getCereriForCurrent())
            this.modelCerere.add(cerere);

        this.tableViewRequest.setItems(this.modelCerere);
    }

    private void initLabelFriends() {
        //this.friendLabel.setText("");
        //if (this.service.currentUser == null)
        //    return;
//
        //StringBuilder labelText = new StringBuilder("Friends:\n\n");
//
        //this.service
        //        .getFriendsForCurrent()
        //        .forEach(friendId -> {
        //            Utilizator user = this.service.getUtilizator(friendId);
        //            labelText.append(user.getFirstName()).append(" ").append(user.getLastName()).append('\n');
        //        });
//
        //this.friendLabel.setText(labelText.toString());
    }

    @Override
    public void update(EntityChangeEvent utilizatorEntityChangeEvent) {

    }

    public void handleDeleteUtilizator(ActionEvent actionEvent)  {
        if (selectedId == null)
            return;
        this.service.deleteUtilizator(
                this.selectedId
        );
        this.initModel();
        this.initLabelFriends();
        this.initModelRequest();
    }

    public void handleAddUtilizator(ActionEvent actionEvent) {
        //this.service.addUtilizator(
        //    this.firstNameText.getText().trim(),
        //    this.lastNameText.getText().trim()
        //);
        //this.initModel();
        //this.firstNameText.clear();
        //this.lastNameText.clear();
    }

    public void handleLogInButton(ActionEvent actionEvent) {
        //var splitCredentials = this.logInText
        //                                .getText()
        //                                .split(" ");
        //if (splitCredentials.length != 2) {
        //    this.currentUserLabel.setText("Invalid format");
        //    return;
        //}
        //try {
        //    if (this.service.login(Long.parseLong(splitCredentials[0]))) {
        //        this.currentUserLabel.setText("Current user: " + this.service.currentUser.getFirstName() + " " + this.service.currentUser.getLastName());
        //        this.initModelRequest();
        //        this.initLabelFriends();
        //        return;
        //    } else {
        //        this.currentUserLabel.setText("Current user: Failed to login");
        //    }
        //} catch (Exception ex) {
        //    this.currentUserLabel.setText("Invalid ID format");
        //}
        //this.logInText.clear();
        //this.modelCerere.clear();
        //this.friendLabel.setText("");
    }

    public void handleSendRequest(ActionEvent actionEvent) {
        if (this.selectedId == null)
            return;
        if (this.service.currentUser == null)
            return;

        this.service.addCerere(this.service.currentUser.getId(), this.selectedId);
        this.initModelRequest();
    }

    public void handleAcceptRequest(ActionEvent actionEvent) {
        System.out.println(this.selectedRequestId + " " + this.service.currentUser.getId());
        if (this.service.currentUser == null)
            return;
        if (this.selectedRequestId == null)
            return;
        this.service.deleteCerere(this.selectedRequestId, this.service.currentUser.getId(), true);
        this.initLabelFriends();
        this.initModelRequest();
    }
}
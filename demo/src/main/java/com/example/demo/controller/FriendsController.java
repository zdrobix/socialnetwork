package com.example.demo.controller;

import com.example.demo.domain.Cerere;
import com.example.demo.domain.Prietenie;
import com.example.demo.domain.Utilizator;
import com.example.demo.events.EntityChangeEvent;
import com.example.demo.service.Service;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;

public class FriendsController extends IController{

    private Service service;
    ObservableList<Cerere> modelCereri = FXCollections.observableArrayList();
    ObservableList<Utilizator> modelFriends = FXCollections.observableArrayList();

    @FXML
    private TableView<Cerere> tableViewRequest;
    @FXML
    private TableColumn<Cerere,String> tableColumnRequestFrom;
    @FXML
    private TableColumn<Cerere, LocalDate> tableColumnRequestDate;

    @FXML
    private TableView<Utilizator> tableViewFriends;
    @FXML
    private TableColumn<Utilizator,String> nameColumn;
    @FXML
    private TableColumn<Utilizator, LocalDate> dateColumn;

    Long selectedRequestId = null;

    @Override
    public void setController(Service service) {
        this.service = service;
        service.addObserver(this);
        this.initModel();
        this.initModelRequest();
    }

    @FXML
    public void initialize() {
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
        this.tableViewRequest.setItems(modelCereri);
        this.tableViewFriends.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.nameColumn.setCellValueFactory(data -> {
            return new SimpleObjectProperty<>(data.getValue().getFirstName() + " " + data.getValue().getLastName());
        });
        this.dateColumn.setCellValueFactory(data -> {
            Prietenie prietenie = this.service.getPrietenie(this.service.currentUser.getId(), data.getValue().getId());
            return new SimpleObjectProperty<>(prietenie.getDate());
        });
        this.tableViewFriends.setItems(modelFriends);
    }

    private void initModel() {
        if (this.service.currentUser == null)
            return;
        this.modelFriends.setAll(this.service.getFriendsForCurrent());
    }

    private void initModelRequest() {
        if (this.service.currentUser == null)
            return;
        if (this.service.getCereriForCurrent() == null)
            return;
        this.service.getCereriForCurrent().forEach(x -> System.out.println(x.toString()));
        this.modelCereri.setAll(this.service.getCereriForCurrent());
    }


    @Override
    public void update(EntityChangeEvent utilizatorEntityChangeEvent) {
        switch (utilizatorEntityChangeEvent.getType()) {
            case ADD:
                break;
            case DELETE:
                this.modelCereri.remove(utilizatorEntityChangeEvent.getData());
                break;
        }
    }

    public void handleAcceptRequest(ActionEvent actionEvent) {
        if (this.selectedRequestId == null)
            return;
        this.service.deleteCerere(this.selectedRequestId, this.service.currentUser.getId(), true);
    }

    public void handleDeleteRequest(ActionEvent actionEvent) {
        if (this.selectedRequestId == null)
            return;
        this.service.deleteCerere(this.selectedRequestId, this.service.currentUser.getId(), false);
    }

    public void handleRemove(ActionEvent actionEvent) {
    }
}

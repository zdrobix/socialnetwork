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
import java.util.HashMap;
import java.util.Map;

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
    private TableColumn<Utilizator,String> nameColumnFriends;
    @FXML
    private TableColumn<Utilizator, LocalDate> dateColumnFriends;

    Long selectedRequestId = null;
    Long selectedFriendId = null;

    @Override
    public void setController(Service service) {
        this.service = service;
        service.addObserver(this);
        this.initModelFriends();
        this.initModelRequest();
        this.initializeData();
    }

    @FXML
    public void initializeData() {
        Map<Long, Utilizator> utilizatorCache = new HashMap<>();
        this.modelCereri
                .forEach(cerere ->
                                utilizatorCache.put(
                                        cerere.getFrom(),
                                        this.service.getUtilizator(
                                                cerere.getFrom()
                                        )
                                )
                );

        Map<Long, Prietenie> prietenieCache = new HashMap<>();
        this.modelFriends
                .forEach(prieten ->
                        prietenieCache.put(
                                prieten.getId(),
                                service.getPrietenie(
                                        service.currentUser.getId(),
                                        prieten.getId())
                        )
                );
        this.initTableFriends(prietenieCache);
        this.initTableRequest(utilizatorCache);
    }

    private void initTableFriends(Map<Long, Prietenie> prietenieCache) {
        this.nameColumnFriends.setCellValueFactory(
                data -> new SimpleObjectProperty<>(data.getValue().getFirstName() + " " + data.getValue().getLastName()));
        this.dateColumnFriends.setCellValueFactory(data -> {
            Prietenie prietenie = prietenieCache.get(data.getValue().getId());
            return new SimpleObjectProperty<>(prietenie.getDate());
        });
        this.tableViewFriends.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.tableViewFriends.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldVallue, newValue) -> {
                    if (newValue != null) {
                        this.selectedFriendId = newValue.getId();
                    }
                });
        this.tableViewFriends.setItems(modelFriends);
    }

    private void initTableRequest(Map<Long, Utilizator> utilizatorCache) {
        this.tableColumnRequestFrom.setCellValueFactory(data -> {
            Utilizator user = utilizatorCache.get(data.getValue().getFrom());
            return new SimpleStringProperty(user.getFirstName() + " " + user.getLastName());
        });
        this.tableColumnRequestDate.setCellValueFactory(data -> {
            LocalDate requestDate = data.getValue().getDate();
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
    }

    private void initModelFriends() {
        if (this.service.currentUser == null)
            return;
        this.modelFriends.setAll(this.service.getFriendsForCurrent());
    }

    private void initModelRequest() {
        if (this.service.currentUser == null)
            return;
        this.service.getCereriForCurrent().forEach(x -> System.out.println(x.toString()));
        this.modelCereri.setAll(this.service.getCereriForCurrent());
    }


    @Override
    public void update(EntityChangeEvent utilizatorEntityChangeEvent) {
        switch (utilizatorEntityChangeEvent.getType()) {
            case ADD:
                if (utilizatorEntityChangeEvent.getData() instanceof Cerere)
                    this.modelCereri.add((Cerere) utilizatorEntityChangeEvent.getData());
                if (utilizatorEntityChangeEvent.getData() instanceof Prietenie) {
                    Prietenie added = (Prietenie)utilizatorEntityChangeEvent.getData();
                    if (this.service.currentUser.getId() == added.getIdFriend1())
                        this.modelFriends.add(this.service.getUtilizator(added.getIdFriend2()));
                    else this.modelFriends.add(this.service.getUtilizator(added.getIdFriend1()));
                }
                break;
            case DELETE:
                if (utilizatorEntityChangeEvent.getData() instanceof Cerere)
                    this.modelCereri.remove((Cerere)utilizatorEntityChangeEvent.getData());
                if (utilizatorEntityChangeEvent.getData() instanceof Prietenie) {
                    Prietenie deletedP = (Prietenie)utilizatorEntityChangeEvent.getData();
                    if (this.service.currentUser.getId() == deletedP.getIdFriend1())
                        this.modelFriends.remove(this.service.getUtilizator(deletedP.getIdFriend2()));
                    else this.modelFriends.remove(this.service.getUtilizator(deletedP.getIdFriend1()));
                }
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
        if (this.selectedFriendId == null)
            return;
        this.service.deletePrietenie(this.selectedFriendId, this.service.currentUser.getId());
    }
}

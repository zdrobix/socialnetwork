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
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    private TableColumn<Utilizator, String> dateColumnFriends;

    Long selectedRequestId = null;
    Long selectedFriendId = null;

    Map<Long, Utilizator> utilizatorCache = new HashMap<>();
    Map<Long, Prietenie> prietenieCache = new HashMap<>();

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
        this.modelCereri
                .forEach(cerere ->
                                this.utilizatorCache.put(
                                        cerere.getFrom(),
                                        this.service.getUtilizator(
                                                cerere.getFrom()
                                        )
                                )
                );

        this.modelFriends
                .forEach(prieten ->
                        this.prietenieCache.put(
                                prieten.getId(),
                                this.service.getPrietenie(
                                        this.service.currentUser.getId(),
                                        prieten.getId())
                        )
                );
        this.initTableFriends();
        this.initTableRequest();
    }

    private void initTableFriends() {
        this.nameColumnFriends.setCellValueFactory(
                data -> new SimpleObjectProperty<>(data.getValue().getFirstName() + " " + data.getValue().getLastName()));
        this.dateColumnFriends.setCellValueFactory(data -> {
            Prietenie prietenie = this.prietenieCache.get(data.getValue().getId());
            return new SimpleObjectProperty<>(ChronoUnit.DAYS.between(prietenie.getDate(), LocalDate.now())/7 + 1 + "w");
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

    private void initTableRequest() {
        this.tableColumnRequestFrom.setCellValueFactory(data -> {
            Utilizator user = this.utilizatorCache.get(data.getValue().getFrom());
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
        this.modelFriends.setAll(this.service.getFriendsFor(this.service.currentUser.getId()));
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
                if (utilizatorEntityChangeEvent.getData() instanceof Cerere) {
                    this.modelCereri.add((Cerere) utilizatorEntityChangeEvent.getData());
                }
                if (utilizatorEntityChangeEvent.getData() instanceof Prietenie prietenie) {
                    if (Objects.equals(this.service.currentUser.getId(), prietenie.getIdFriend1())) {
                        this.prietenieCache.put(
                                prietenie.getIdFriend2(),
                                this.service.getPrietenie(
                                        this.service.currentUser.getId(),
                                        prietenie.getIdFriend2())
                        );
                        this.modelFriends.add(this.service.getUtilizator(prietenie.getIdFriend2()));
                    }
                    else {
                        this.prietenieCache.put(
                                prietenie.getIdFriend1(),
                                this.service.getPrietenie(
                                        this.service.currentUser.getId(),
                                        prietenie.getIdFriend1())
                        );
                        this.modelFriends.add(this.service.getUtilizator(prietenie.getIdFriend1()));
                    }
                }
                break;
            case DELETE:
                if (utilizatorEntityChangeEvent.getData() instanceof Cerere) {
                    this.modelCereri.remove((Cerere) utilizatorEntityChangeEvent.getData());
                }
                if (utilizatorEntityChangeEvent.getData() instanceof Prietenie prietenie) {
                    if (Objects.equals(this.service.currentUser.getId(), prietenie.getIdFriend1()))
                        this.modelFriends.remove(this.service.getUtilizator(prietenie.getIdFriend2()));
                    else this.modelFriends.remove(this.service.getUtilizator(prietenie.getIdFriend1()));
                }
                break;
        }
    }

    public void handleAcceptRequest() {
        if (this.selectedRequestId == null)
            return;
        this.service.deleteCerere(this.selectedRequestId, this.service.currentUser.getId(), true);
    }

    public void handleDeleteRequest() {
        if (this.selectedRequestId == null)
            return;
        this.service.deleteCerere(this.selectedRequestId, this.service.currentUser.getId(), false);
    }

    public void handleRemove() {
        if (this.selectedFriendId == null)
            return;
        this.service.deletePrietenie(this.selectedFriendId, this.service.currentUser.getId());
    }
}

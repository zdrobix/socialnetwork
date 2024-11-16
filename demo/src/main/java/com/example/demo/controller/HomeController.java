package com.example.demo.controller;

import com.example.demo.domain.Utilizator;
import com.example.demo.events.EntityChangeEvent;
import com.example.demo.service.Service;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class HomeController extends IController{

    private Service service;
    ObservableList<Utilizator> model = FXCollections.observableArrayList();

    @FXML
    private TableView<Utilizator> tableView;
    @FXML
    private TableColumn<Utilizator,String> tableColumnFirstName;
    @FXML
    private TableColumn<Utilizator,String> tableColumnLastName;

    @FXML
    private Label subheader;
    @FXML
    private TextField searchTextField;

    private Long selectedId = null;

    @Override
    public void setController(Service service) {
        this.service = service;
        if (this.service.currentUser == null) {
            this.subheader.setText("Welcome to SocialNetwork!\nAccess the account tab to login or sign up.");
        }
        service.addObserver(this);
        this.initModel();
    }

    @FXML
    public void initialize() {
        tableColumnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        tableColumnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        tableView.setItems(model);

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.selectedId = newValue.getId();
                System.out.println(selectedId);
            }
        });
    }

    private void initModel() {
        Iterable<Utilizator> messages = service.getAll();
        List<Utilizator> users = StreamSupport.stream(messages.spliterator(), false)
                .collect(Collectors.toList());
        model.setAll(users);
    }

    @Override
    public void update(EntityChangeEvent utilizatorEntityChangeEvent) {
        if (!(utilizatorEntityChangeEvent.getData() instanceof Utilizator))
            return;
        switch (utilizatorEntityChangeEvent.getType()) {
            case ADD:
                this.model.add(
                        (Utilizator) utilizatorEntityChangeEvent.getData()
                );
                break;
            case UPDATE:
                this.model.set(
                        this.tableView.getSelectionModel().getSelectedIndex(),
                        (Utilizator) utilizatorEntityChangeEvent.getData()
                );
                break;
            case DELETE:
                this.model.remove(utilizatorEntityChangeEvent.getData());
                break;
        }
    }

    public void handleSearch(ActionEvent actionEvent) {
        model.clear();
        if (this.searchTextField.getText().isEmpty()) {
            this.initModel();
        }
        Iterable<Utilizator> messages = service
                .getAll()
                .stream()
                .filter(
                        user ->
                                user.getFirstName().toLowerCase().startsWith(searchTextField.getText().trim().toLowerCase()) ||
                                user.getLastName().toLowerCase().startsWith(searchTextField.getText().trim().toLowerCase())
        )
                .collect(Collectors.toList());
        List<Utilizator> users = StreamSupport.stream(messages.spliterator(), false)
                .collect(Collectors.toList());
        model.setAll(users);
    }

    public void handleSendRequest(ActionEvent actionEvent) {
        if (this.selectedId == null)
            return;
        if (this.service.currentUser == null)
            return;
        this.service.addCerere(this.service.currentUser.getId(), this.selectedId);
    }
}

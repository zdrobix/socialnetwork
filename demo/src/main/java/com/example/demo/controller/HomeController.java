package com.example.demo.controller;

import com.example.demo.HelloApplication;
import com.example.demo.domain.Utilizator;
import com.example.demo.events.EntityChangeEvent;
import com.example.demo.service.Service;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
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
    private TableColumn<Utilizator, ImageView> tableColumnPhoto;

    @FXML
    private Label subheader;
    @FXML
    private TextField searchTextField;

    private Long selectedId = null;

    @Override
    public void setController(Service service) {
        this.service = service;
        if (super.context.getCurrentUser() == null) {
            this.subheader.setText("Welcome to SocialNetwork!\nAccess the account tab to login or sign up.");
        }
        service.addObserver(this);
        this.initModel();
    }

    @FXML
    public void initialize() {
        tableColumnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        tableColumnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        tableColumnPhoto.setCellValueFactory(data -> {
            String fileName = data.getValue().getFirstName()
                            + data.getValue().getLastName()
                            + data.getValue().getId()
                            + ".png";
            File file = new File("Q:/info/java/lab3/demo/src/main/resources/user_photos/" + fileName);
            if (!file.exists()) {
                file = new File("Q:/info/java/lab3/demo/src/main/resources/user_photos/userphoto.png");
            }
            Image image = new Image(file.toURI().toString());
            ImageView imageView = new ImageView(image);

            imageView.setFitHeight(20);
            imageView.setFitWidth(20);

            return new javafx.beans.property.SimpleObjectProperty<>(imageView);
        });
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
        if (super.context.getCurrentUser() == null)
            return;
        this.service.addCerere(super.context.getCurrentUser().getId(), this.selectedId);
    }

    public void handleOpenProfile(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/demo/UserInfo.fxml"));

        AnchorPane userLayout = fxmlLoader.load();
        stage.setScene(new Scene(userLayout));

        UserInfoController userInfoController = fxmlLoader.getController();
        userInfoController.setContext(super.context);
        userInfoController.setId(this.selectedId);
        userInfoController.setController(service);

        stage.setWidth(400);
        stage.setHeight(400);
        stage.show();
    }

    public void handleOpenChat(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/demo/ChatView.fxml"));

        AnchorPane userLayout = fxmlLoader.load();
        stage.setScene(new Scene(userLayout));

        ChatController userInfoController = fxmlLoader.getController();
        userInfoController.setContext(super.context);
        userInfoController.setId(this.selectedId);
        userInfoController.setController(service);

        stage.setWidth(400);
        stage.setHeight(400);
        stage.show();
    }
}

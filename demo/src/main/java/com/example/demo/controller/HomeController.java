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
import javafx.scene.control.*;
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

    @FXML
    private Button deleteButton, adminButton1, adminButton2;

    @FXML
    private Button previousPageButton, nextPageButton;

    private Long selectedId = null;

    private int pageNr = 0;
    private final int pageSize = 7;

    @Override
    public void setController(Service service) {
        this.service = service;
        if (super.context.getCurrentUser() == null)
            this.subheader.setText("Welcome to SocialNetwork!\nAccess the account tab to login or sign up.");
        service.addObserver(this);
        this.initModel();
        this.initializeAdminPanel();
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
        model.setAll(service.getAllOnPage(this.pageNr, this.pageSize, service.getAll()));
    }

    private void initializeAdminPanel() {
        deleteButton.setDisable(true);
        adminButton1.setDisable(true);
        adminButton2.setDisable(true);
        deleteButton.setVisible(false);
        adminButton1.setVisible(false);
        adminButton2.setVisible(false);

        if (super.context.getCurrentUser() == null)
            return;

        if (super.context.getCurrentUser().getId() == 35L) {
            deleteButton.setDisable(false);
            adminButton1.setDisable(false);
            adminButton2.setDisable(false);
            deleteButton.setVisible(true);
            adminButton1.setVisible(true);
            adminButton2.setVisible(true);
        }
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
                        this.model.indexOf((Utilizator) utilizatorEntityChangeEvent.getOldData()),
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
        Iterable<Utilizator> userSearch = service
                .getAll()
                .stream()
                .filter(
                        user ->
                                user.getFirstName().toLowerCase().startsWith(searchTextField.getText().trim().toLowerCase()) ||
                                user.getLastName().toLowerCase().startsWith(searchTextField.getText().trim().toLowerCase())
        )
                .collect(Collectors.toList());
        List<Utilizator> users = StreamSupport.stream(userSearch.spliterator(), false)
                .collect(Collectors.toList());
        model.setAll(this.service.getAllOnPage(1, this.pageSize, users));
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

    public void handleDelete(ActionEvent actionEvent) {
        this.service.deleteUtilizator(
                this.selectedId
        );
    }

    public void handlePreviousPage(ActionEvent actionEvent) {
        long totalPages = (this.service.getAll().size() + this.pageSize - 1) / this.pageSize;
        if (this.pageNr > 0) {
            this.previousPageButton.setDisable(false);
            this.pageNr--;
            initModel();
        }
        this.previousPageButton.setDisable(this.pageNr <= 0);
        this.nextPageButton.setDisable(this.pageNr >= totalPages - 1);
    }

    public void handleNextPage(ActionEvent actionEvent) {
        long totalPages = (this.service.getAll().size() + this.pageSize - 1) / this.pageSize;
        if (this.pageNr < totalPages - 1) {
            this.pageNr++;
            this.previousPageButton.setDisable(false);
            initModel();
        }
        this.previousPageButton.setDisable(this.pageNr <= 0);
        this.nextPageButton.setDisable(this.pageNr >= totalPages - 1);
    }
}

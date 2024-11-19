package com.example.demo.controller;

import com.example.demo.domain.Message;
import com.example.demo.domain.MessageFormatter;
import com.example.demo.events.EntityChangeEvent;
import com.example.demo.service.Service;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Comparator;

public class ChatController extends IController{
    private Service service;
    private Long idCurrent;
    private Long selectedId;

    @FXML
    private Label titleChatBox;
    @FXML
    private VBox messageHistory;
    @FXML
    private TextField messageTextField;
    @FXML
    private ScrollPane scrollPane;


    @Override
    void setController(Service service) {
        this.service = service;
        this.idCurrent = service.currentUser.getId();
        this.initialize2();
        this.initialize2();
    }

    public void setId(Long selectedId) {
        this.selectedId = selectedId;
    }

    void initialize2() {
        titleChatBox.setText("Chat with " + this.service.getUtilizator(selectedId).getFirstName());
        var sortedMessages = this.service.loadMessagesBetween(this.idCurrent, this.selectedId);
        if (sortedMessages.isEmpty()) {
            this.populateEmpty(15);
            return;
        }
        this.populateEmpty(15 - sortedMessages.size());
        sortedMessages
                .sort(Comparator.comparing(Message::getDateTime));
        sortedMessages
                .forEach(
                        message -> {
                            this.addMessageChat(
                                    message.getText(),
                                    message.getId_from(),
                                    message.getId_to()
                            ); System.out.println(message.getId());
                        }
                );
        this.addListenerSendMessage();
    }

    private void populateEmpty (int size) {
        this.messageHistory.getChildren().clear();
        for (int i = 15 - size; i > 0; i--) {
            Label empty = new Label("");
            empty.getStyleClass().add("empty");
            this.messageHistory.getChildren().add(empty);
        }
    }

    @Override
    public void update(EntityChangeEvent entityChangeEvent) {

    }

    public void handleSendMessage(ActionEvent actionEvent) {
        this.service.addMessage(
                this.selectedId,
                0L,
                this.messageTextField.getText()
        );
        this.addMessageChat(this.messageTextField.getText(), this.idCurrent, this.selectedId);
        this.messageTextField.setText("");
    }

    private void addMessageChat(String message, Long from, Long to) {

        Label messageLabel = new Label(MessageFormatter.getFormattedMessage(message, 36) + " ");
        messageLabel.setWrapText(true);
        messageLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        messageLabel.setMinWidth(Region.USE_PREF_SIZE);
        messageLabel.setPrefHeight(Region.USE_COMPUTED_SIZE);
        if (to == this.idCurrent)
            messageLabel.getStyleClass().add("message-user1");
        if (from == this.idCurrent)
             messageLabel.getStyleClass().add("message-user2");
        this.messageHistory.getChildren().add(messageLabel);

        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    private void addListenerSendMessage() {
        messageTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSendMessage(new ActionEvent());
            }
        });
    }
}

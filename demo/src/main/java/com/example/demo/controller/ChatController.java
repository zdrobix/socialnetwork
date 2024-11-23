package com.example.demo.controller;

import com.example.demo.domain.Entity;
import com.example.demo.domain.Message;
import com.example.demo.domain.MessageFormatter;
import com.example.demo.events.ChangeEventType;
import com.example.demo.events.EntityChangeEvent;
import com.example.demo.events.Sound;
import com.example.demo.service.Service;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Comparator;

public class ChatController extends IController{
    private Service service;
    private Long idCurrent;
    private Long selectedId;
    private Long selectedMessageId;

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
        this.service.addObserver(this);
        this.idCurrent = super.context.getCurrentUser().getId();
        this.initialize2();
    }

    public void setId(Long selectedId) {
        this.selectedId = selectedId;
    }

    void initialize2() {
        titleChatBox.setText("Chat with " + this.service.getUtilizator(selectedId).getFirstName());
        var sortedMessages = this.service.loadMessagesBetween(this.idCurrent, this.selectedId);
        this.messageHistory.getChildren().clear();
        this.addListenerSendMessage();

        this.populateEmpty(sortedMessages.size());

        if (sortedMessages.size() == 0)
            return;
        sortedMessages
                    .sort(Comparator.comparing(Message::getDateTime));
        sortedMessages
                    .forEach(
                            this::addMessageChat
                    );
    }

    private void populateEmpty (int size) {
        for (int i = 7 - size; i > 0; i--) {
            Label empty = new Label("");
            empty.getStyleClass().add("emptylabel"); System.out.println("da");
            this.messageHistory.getChildren().add(empty);
        }
    }

    @Override
    public void update(EntityChangeEvent entityChangeEvent) {
        if (!(entityChangeEvent.getData() instanceof Message))
            return;
        var message = (Message)entityChangeEvent.getData();
        if (entityChangeEvent.getType() == ChangeEventType.ADD) {
            this.addMessageChat(message);
        }
    }

    public void handleSendMessage(ActionEvent actionEvent) {
        if (this.selectedMessageId == null)
            this.selectedMessageId = 0L;
        this.service.addMessage(
                this.idCurrent,
                this.selectedId,
                this.selectedMessageId,
                this.messageTextField.getText()
        );
        this.messageTextField.setText("");
        this.selectedMessageId = null;
    }

    private void addMessageChat(Message message) {

        Label messageLabel = new Label(MessageFormatter.getFormattedMessage(message.getText(), 36) + " ");
        messageLabel.setWrapText(true);
        messageLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        messageLabel.setMinWidth(Region.USE_PREF_SIZE);
        messageLabel.setPrefHeight(Region.USE_COMPUTED_SIZE);
        if (message.getId_to() == context.getCurrentUser().getId()) {
            messageLabel.getStyleClass().add("message-user1");
            Button buttonReply = getButtonReply(message.getId());
            if (message.getId_reply() == 0L)
                this.messageHistory.getChildren().add(
                        new HBox(
                                messageLabel,
                                new Label(" "),
                                buttonReply
                        )
                );
            else {
                Label replyLabel = new Label(this.service.getMessage(message.getId_reply()).getText());
                replyLabel.setWrapText(true);
                replyLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
                replyLabel.setMinWidth(Region.USE_PREF_SIZE);
                replyLabel.setPrefHeight(Region.USE_COMPUTED_SIZE);
                replyLabel.getStyleClass().add("message-reply-user1");
                this.messageHistory.getChildren().add(
                        new VBox(
                                replyLabel,
                                new HBox(
                                        messageLabel,
                                        new Label(" "),
                                        buttonReply
                                )
                        )
                );
            }
        }
        if (message.getId_from() == context.getCurrentUser().getId()) {
            messageLabel.getStyleClass().add("message-user2");
            if (message.getId_reply() == 0L)
                this.messageHistory.getChildren().add(
                        messageLabel
                );
            else {
                Label replyLabel = new Label(this.service.getMessage(message.getId_reply()).getText());
                replyLabel.setWrapText(true);
                replyLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
                replyLabel.setMinWidth(Region.USE_PREF_SIZE);
                replyLabel.setPrefHeight(Region.USE_COMPUTED_SIZE);
                replyLabel.getStyleClass().add("message-reply-user2");
                this.messageHistory.getChildren().add(
                        new VBox(
                                replyLabel,
                                messageLabel
                        )
                );
            }
        }
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    private Button getButtonReply(Long idMessage) {
        Button buttonReply = new Button("R");
        buttonReply.getStyleClass().add("reply-button");
        buttonReply.setOnAction(
                event -> {
                    selectedMessageId = idMessage; System.out.println(selectedMessageId);
                }
        );
        return buttonReply;
    }

    private void addListenerSendMessage() {
        messageTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSendMessage(new ActionEvent());
            }
        });
    }
}

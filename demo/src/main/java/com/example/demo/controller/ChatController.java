package com.example.demo.controller;

import com.example.demo.domain.Message;
import com.example.demo.events.EntityChangeEvent;
import com.example.demo.service.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChatController extends IController{
    private Service service;
    private Long selectedId;

    @FXML
    public Label titleChatBox;
    @FXML
    public Label messageHistory;
    @FXML
    private TextField messageTextField;

    @FXML
    private VBox chatBox;

    @Override
    void setController(Service service) {
        this.service = service;
        this.initialize2();
    }

    public void setId(Long selectedId) {
        this.selectedId = selectedId;
    }

    void initialize2() {
        var sortedMessages = this.service.loadMessagesBetween(this.service.currentUser.getId(), this.selectedId);
        if (sortedMessages == null)
            return;
        if (sortedMessages.isEmpty())
            return;
        sortedMessages
                .sort(new Comparator<Message>() {
                    @Override
                    public int compare(Message o1, Message o2) {
                        return o1.getDateTime().compareTo(o2.getDateTime());
                    }
                });
        final String[] textMessages = {""};
        sortedMessages
                .forEach(
                        message -> {
                            textMessages[0] += message.getText();
                        }
                );
        this.messageHistory.setText(textMessages[0]);
    }

    @Override
    public void update(EntityChangeEvent entityChangeEvent) {

    }

    public void handleSendMessage(ActionEvent actionEvent) {
        new Message(0L, 0L, 0L, Timestamp.valueOf(LocalDateTime.now()), 0L, "salut, ce faci>" );
        this.service.addMessage(
                this.selectedId,
                0L,
                "Salut, ce faci?"
        );
        this.service.addMessage(
                this.selectedId,
                0L,
                this.messageTextField.getText()
        );
        this.messageHistory.setText(this.messageHistory.getText() + "\n" + this.messageTextField.getText());
    }
}

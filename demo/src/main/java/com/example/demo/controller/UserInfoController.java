package com.example.demo.controller;

import com.example.demo.domain.Utilizator;
import com.example.demo.events.EntityChangeEvent;
import com.example.demo.service.Service;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UserInfoController extends IController{
    private Service service;

    private Long selectedId;

    @FXML
    private Label nameLabel;
    @FXML
    private ImageView imageView;
    @FXML
    private Label friendsCommonLabel;

    @Override
    void setController(Service service) {
        this.service = service;
        this.initialize2();
    }

    private void initialize2() {
        this.nameLabel.setText(this.service.getUtilizator(this.selectedId).getFirstName() + " " + this.service.getUtilizator(this.selectedId).getLastName());
        this.setFriends();
        String fileName = this.service.getUtilizator(this.selectedId).getFirstName()
                + this.service.getUtilizator(this.selectedId).getLastName()
                + this.selectedId
                + ".png";
        File file = new File("Q:/info/java/lab3/demo/src/main/resources/user_photos/" + fileName);
        if (!file.exists()) {
            file = new File("Q:/info/java/lab3/demo/src/main/resources/user_photos/userphoto.png");
        }
        Image image = new Image(file.toURI().toString());

        imageView.setPreserveRatio(true);
        imageView.setFitHeight(200);
        imageView.setFitWidth(200);

        this.imageView.setImage(image);
    }

    private void setFriends() {
        if (this.service.currentUser == null)
            return;
        List<Utilizator> friendsCommon = this.service.getFriendsInCommon(this.service.currentUser.getId(), this.selectedId);
        if (friendsCommon.isEmpty()) {
            this.friendsCommonLabel.setText("No friends in common");
            return;
        }
        final String[] text = {"Friends in common: \n\n"};
        friendsCommon.forEach(
                friend -> text[0] += friend.getFirstName() + " " + friend.getLastName() + "\n"
        );
        this.friendsCommonLabel.setText(text[0]);
    }

    void setId (Long id){
        this.selectedId = id;
    }

    @Override
    public void update(EntityChangeEvent entityChangeEvent) {

    }
}

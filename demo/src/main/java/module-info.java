module main.java.com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires javafx.base;

    //requires org.controlsfx.controls;
    //requires com.dlsc.formsfx;
    //requires net.synedra.validatorfx;
    //requires org.kordamp.ikonli.javafx;
    //requires org.kordamp.bootstrapfx.core;
    //requires eu.hansolo.tilesfx;
    //requires com.almasb.fxgl.all;
    requires java.sql;

    opens main.java.com.example.demo to javafx.fxml;
    opens main.java.com.example.demo.controller to javafx.fxml;
    opens main.java.com.example.demo.domain to javafx.base;

    exports main.java.com.example.demo;
    exports main.java.com.example.demo.controller;


}
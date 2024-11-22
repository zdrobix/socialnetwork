module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    //requires org.controlsfx.controls;
    //requires com.dlsc.formsfx;
    //requires net.synedra.validatorfx;
    //requires org.kordamp.ikonli.javafx;
    //requires org.kordamp.bootstrapfx.core;
    //requires eu.hansolo.tilesfx;
    //requires com.almasb.fxgl.all;
    requires java.sql;
    requires javafx.media;
    requires java.desktop;
    //requires javafx.swt;

    opens com.example.demo to javafx.fxml;
    opens com.example.demo.controller to javafx.fxml;
    opens com.example.demo.domain to javafx.base;

    exports com.example.demo;
    exports com.example.demo.controller;


}
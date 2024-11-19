package com.example.demo.domain;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Message extends Entity<Long>{
    private Long id_to;
    private Long id_from;
    private Timestamp dateTime;
    private Long id_reply;
    private String text;

    public Message(Long id_to, Long id_from, Timestamp dateTime, Long id_reply, String text) {
        this.id_to = id_to;
        this.id_from = id_from;
        this.dateTime = dateTime;
        this.id_reply = id_reply;
        this.text = text;
        super.setId(id);
        System.out.println(this);
    }

    public Long getId_from() {
        return this.id_from;
    }

    public Long getId_to() {
        return this.id_to;
    }

    public Timestamp getDateTime() {
        return this.dateTime;
    }

    public Long getId_reply() {
        return this.id_reply;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        if (message.id == null)
            return false;
        return message.id.equals(this.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return this.id + " " + this.id_to + " " + this.id_from + " " + this.dateTime + " " + this.id_reply + " " + this.text;
    }
}
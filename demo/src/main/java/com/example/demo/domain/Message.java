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
        if (message.getId() == null)
            return false;
        return message.getId().equals(super.getId());
    }

    @Override
    public int hashCode() {
        return super.getId().hashCode();
    }

    @Override
    public String toString() {
        return super.getId() + " " + this.id_to + " " + this.id_from + " " + this.dateTime + " " + this.id_reply + " " + this.text;
    }

    public String getDateTime2() {
        return "\'" + this.dateTime + "\'";
    }

    public String getText2() {
        return "\'" + this.text + "\'";
    }
}

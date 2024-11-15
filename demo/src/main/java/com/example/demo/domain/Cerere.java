package com.example.demo.domain;

import java.time.LocalDate;

public class Cerere extends Entity<Tuple<Long, Long>> {
    LocalDate date;
    Long from;
    Long to;

    public Cerere() {
        this.date = LocalDate.now();
    }

    public Cerere(Long idFriend1_, Long idFriend2_, LocalDate date_) {
        this.from = idFriend1_;
        this.to = idFriend2_;
        this.date = date_;
        this.sedIdCerere();
    }

    public Long getFrom() {
        return this.from;
    }

    public Long getTo() {
        return this.to;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public Prietenie asPrietenie() {
        var prietenie = new Prietenie(
                this.from, this.to, this.date
        );
        prietenie.setIdPrietenie();
        return prietenie;
    }

    public void sedIdCerere() {
        super.setId(new Tuple<Long, Long>(this.from, this.to));
    }

    @Override
    public String toString() {
        return this.from + "," + this.to + "," + this.date;
    }
}

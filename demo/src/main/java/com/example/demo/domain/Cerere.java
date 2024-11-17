package com.example.demo.domain;

import java.time.LocalDate;
import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cerere cerere = (Cerere) obj;
        return this.from == cerere.from && this.to == cerere.to;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.from, this.to);
    }
}

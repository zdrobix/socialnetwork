package main.java.com.example.demo.domain;

import java.time.LocalDate;


public class Prietenie extends Entity<Tuple<Long, Long>> {

    LocalDate date;
    Long idFriend1;
    Long idFriend2;

    public Prietenie() {
        this.date = LocalDate.now();
    }

    public Prietenie(Long idFriend1_, Long idFriend2_, LocalDate date_) {
        this.idFriend1 = idFriend1_;
        this.idFriend2 = idFriend2_;
        this.date = date_;
        this.setIdPrietenie();
    }

    public Long getIdFriend1() {
        return idFriend1;
    }

    public Long getIdFriend2() {
        return idFriend2;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setIdPrietenie() {
        super.setId(new Tuple<Long, Long>(this.idFriend1, this.idFriend2));
    }

    @Override
    public String toString() {
        return this.idFriend1 + "," + this.idFriend2 + "," + this.date;
    }
}

package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.domain.validators.PrietenieValidator;
import com.example.demo.domain.validators.ValidationException;

import com.example.demo.events.ChangeEventType;
import com.example.demo.events.EntityChangeEvent;
import com.example.demo.logs.Logger;
import com.example.demo.observer.Observer;
import com.example.demo.password.Crypter;
import com.example.demo.repo.db.FriendRequestDatabaseRepository;
import com.example.demo.repo.db.UserDatabaseRepository;
import com.example.demo.repo.db.FriendshipDatabaseRepository;
import com.example.demo.repo.db.UserLoginDatabaseRepository;
import com.example.demo.observer.Observable;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Service implements Observable<EntityChangeEvent> {
    private final UserDatabaseRepository repoUseri;
    private final FriendshipDatabaseRepository repoPrieteni;
    private final FriendRequestDatabaseRepository repoCereri;
    private final UserLoginDatabaseRepository repoLogin;

    private List<Observer<EntityChangeEvent>> observers = new ArrayList<>();

    public Utilizator currentUser;

    public Service(UserDatabaseRepository repo_, FriendshipDatabaseRepository repoPrieteni_, FriendRequestDatabaseRepository repoCereri_, UserLoginDatabaseRepository repoLogin_) {
        this.repoUseri = repo_;
        this.repoPrieteni = repoPrieteni_;
        this.repoCereri = repoCereri_;
        this.repoLogin = repoLogin_;
        this.currentUser = null;
    }

    public void addUtilizator (String firstName, String lastName, String username, String password) {
        try {
            var user = new Utilizator(firstName, lastName);
            user.setId(
                    this.repoUseri.generateFirstId()
            );
            if (!this.repoUseri.save(user).isEmpty()) {
                this.repoLogin.save(new Username(username, password, user.getId()));
                EntityChangeEvent event = new EntityChangeEvent<>(ChangeEventType.ADD, user);
                notifyObservers(event);
            }
        } catch (ValidationException ex) {
            Logger.LogException("save", firstName + " " + lastName, ex.getMessage());
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        } catch (IOException ex) {

        }
    }

    public Utilizator deleteUtilizator (long id)  {
        try {
            if (this.repoUseri.findOne(id).isEmpty()) {
                Logger.LogException("delete", "null", "");
                return null;
            }
            this.repoPrieteni.findAll().forEach(
                    prietenie -> {
                        if (prietenie.getIdFriend1() == id || prietenie.getIdFriend2() == id) {
                            this.deletePrietenie(
                                    max(prietenie.getIdFriend1(), prietenie.getIdFriend2()),
                                    min(prietenie.getIdFriend1(), prietenie.getIdFriend2())
                            );
                        }
                    }
            );
            this.repoUseri.findOne(id)
                    .get()
                    .getFriends()
                    .forEach(
                            idUser -> {
                                try {
                                    this.repoUseri.findOne(idUser)
                                            .get()
                                            .removeFriend(
                                                    this.repoUseri.findOne(id)
                                                            .get()
                                            );
                                } catch (SQLException ex) {
                                    Logger.LogException("connect", "", ex.getMessage());
                                }
                            }
                    );
            this.repoCereri.findAll()
                    .forEach(
                            cerere -> {
                                if (cerere.getFrom() == id || cerere.getTo() == id) {
                                    try {
                                        this.repoCereri.delete(cerere.getId());
                                    } catch (SQLException ex) {
                                        Logger.LogException("connect", "", ex.getMessage());
                                    }
                                }
                            }
                    );
            var user = this.repoUseri.delete(id);
            if ( user.isPresent()) {
                notifyObservers(new EntityChangeEvent(ChangeEventType.DELETE, user.get()));
                return user.get();
            }
        } catch (ValidationException e) {
            Logger.LogException("delete", "" + id, "Trying to delete an invalid user");
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        }
        return null;
    }

    public void addPrietenie (long id1, long id2)
    {
        try {
            var friends = new Prietenie(id1, id2, LocalDate.now());
            PrietenieValidator.validate2(friends, this.repoUseri, this.repoPrieteni);
            this.repoPrieteni.save(friends);
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        } catch (ValidationException ex) {
            Logger.LogException("save", "null", ex.getMessage());
        }
    }

    public void deletePrietenie (long id1, long id2) {
        try {
            PrietenieValidator.validate3(id1, id2, this.repoUseri);
            var user1 = this.repoUseri.findOne(id1).get();
            var user2 = this.repoUseri.findOne(id2).get();
            if (user1 == null || user2 == null)
                Logger.LogException("delete", "null friendship", "");
            user1.removeFriend(user2);
            user2.removeFriend(user1);
            this.repoPrieteni.delete(new Tuple<>(
                    id1, id2));
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            Logger.LogException("delete", id1 + " " + id2, ex.getMessage());
        }
    }

    public void addCerere (long id1, long id2)
    {
        try {
            var cerere = new Cerere(id1, id2, LocalDate.now());
            cerere.sedIdCerere();
            if (!this.repoPrieteni.findOne(new Tuple<>(
                    max(id1, id2),
                    min(id1, id2)
            )).isEmpty())
                return;
            if (this.repoCereri.findOne(cerere.getId()).isEmpty())
                this.repoCereri.save(cerere);
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            Logger.LogException("save", "null", ex.getMessage());
        } catch (ValidationException ex) {
            Logger.LogException("save", id1 + " " + id2, ex.getMessage());
        }
    }

    public void deleteCerere (long id1, long id2, boolean accept) {
        try {
            if (accept && this.repoPrieteni.findOne(new Tuple(max(id1, id2), min(id1, id2))).isEmpty()) {
                this.addPrietenie(id1, id2);
            }
            EntityChangeEvent event = new EntityChangeEvent<>(ChangeEventType.DELETE, this.repoCereri.delete(new Tuple<>(id1, id2)));
            notifyObservers(event);
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            Logger.LogException("delete", id1 + " " + id2, ex.getMessage());
        }
    }

    public Utilizator getUtilizator(Long id) {
        try {
            return this.repoUseri.findOne(id).get();
        } catch (Exception ex) {
            Logger.LogException("findOne", id.toString(), ex.getMessage());
        }
        return null;
    }

    public Utilizator getUtilizatorName(String firstName, String lastName) {
        try {
            AtomicReference<Utilizator> result = new AtomicReference<>();
            this.repoUseri.findAll().forEach(
                    user -> {
                        if (user.getFirstName().equals(firstName) && user.getLastName().equals(lastName))
                            result.set(user);
                    }
            );
            if (result != null)
                return result.get();
        } catch (Exception ex) {
            Logger.LogException("findOne", firstName + " " + lastName, ex.getMessage());
        }
        return null;
    }

    public List<Utilizator> getAll() {
        try {
            List<Utilizator> result = new ArrayList<>();
            this.repoUseri.findAll().forEach(
                    user -> {
                        result.add(user);
                    }
            );
            return result;
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        }
        return null;
    }

    public boolean login (String username, String password) {
        try {
            AtomicReference<Utilizator> result = new AtomicReference<>();
            var login = this.repoLogin.findOne(username).get();
            if (login == null)
                return false;
            var decrypted_password = Crypter.decrypt2(
                    login.getPassword(),
                    new Scanner(
                            new File(
                                    "C:\\Users\\Alex\\Desktop\\key.txt"
                            )
                    )
                            .nextLine());
            if (!decrypted_password.equals(password))
                return false;

            Long id = login.getIdLong();
            this.repoUseri.findAll().forEach(
                    user -> {
                        if (user.getId().equals(id) ) {
                            result.set(user);
                        }
                    }
            );
            if (result.get() != null)
            {
                this.currentUser = result.get(); System.out.println(currentUser.toString());
                return true;
            }
            this.currentUser = null;
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public List<Cerere> getCereriForCurrent() {
        try {
            List<Cerere> result = new ArrayList<>();
            if (repoCereri.findAll() == null)
                return null;
            this.repoCereri.findAll().forEach(
                    cerere -> {
                        if (cerere.getTo().equals(this.currentUser.getId()))
                            result.add(cerere);
                    }
            );
            return result;
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        }
        return null;
    }

    public Prietenie getPrietenie(Long id1, Long id2) {
        try {
            return this.repoPrieteni.findOne(
                    new Tuple<>(max(id1, id2), min(id1, id2))
            ).get();
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        }
        return null;
    }

    public List<Utilizator> getFriendsForCurrent() {
        try {
            List<Utilizator> result = new ArrayList<>();
            if (repoPrieteni.findAll() == null)
                return null;
            this.repoPrieteni.findAll().forEach(
                    prieteni -> {
                        if (prieteni.getIdFriend2().equals(this.currentUser.getId()))
                            result.add(this.getUtilizator(prieteni.getIdFriend1()));
                        if (prieteni.getIdFriend1().equals(this.currentUser.getId()))
                            result.add(this.getUtilizator(prieteni.getIdFriend2()));
                    }
            );
            return result;
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        }
        return null;
    }

    @Override
    public void addObserver(Observer<EntityChangeEvent> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(Observer<EntityChangeEvent> e) {

    }

    @Override
    public void notifyObservers(EntityChangeEvent t) {
        observers.stream().forEach(x->x.update(t));
    }
}

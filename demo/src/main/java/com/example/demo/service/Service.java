package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.domain.validators.PrietenieValidator;
import com.example.demo.domain.validators.ValidationException;

import com.example.demo.events.ChangeEventType;
import com.example.demo.events.EntityChangeEvent;
import com.example.demo.logs.Logger;
import com.example.demo.observer.Observer;
import com.example.demo.password.Crypter;
import com.example.demo.repo.db.*;
import com.example.demo.observer.Observable;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Service implements Observable<EntityChangeEvent> {
    private final UserDatabaseRepository repoUseri;
    private final FriendshipDatabaseRepository repoPrieteni;
    private final FriendRequestDatabaseRepository repoCereri;
    private final UserLoginDatabaseRepository repoLogin;
    private final MessageDatabaseRepository repoMessage;

    private final List<Observer<EntityChangeEvent>> observers = new ArrayList<>();

    //public Utilizator currentUser;

    public Service(UserDatabaseRepository repo_, FriendshipDatabaseRepository repoPrieteni_, FriendRequestDatabaseRepository repoCereri_, UserLoginDatabaseRepository repoLogin_, MessageDatabaseRepository repoMessage_) {
        this.repoUseri = repo_;
        this.repoPrieteni = repoPrieteni_;
        this.repoCereri = repoCereri_;
        this.repoLogin = repoLogin_;
        this.repoMessage = repoMessage_;
        //this.currentUser = this.getUtilizator(1L);
    }

    public void addUtilizator (String firstName, String lastName, String username, String password) {
        try {
            var user = new Utilizator(firstName, lastName);
            user.setId(
                    this.repoUseri.generateFirstId()
            );
            if (this.repoUseri.save(user).isPresent()) {
                this.repoLogin.save(new Username(username, password, user.getId()));
                EntityChangeEvent event = new EntityChangeEvent<>(ChangeEventType.ADD, user);
                notifyObservers(event);
            }
        } catch (ValidationException ex) {
            Logger.LogException("save", firstName + " " + lastName, ex.getMessage());
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        } catch (IOException ex) {
            System.out.println("IOException " + ex.getMessage());
        }
    }

    public void deleteUtilizator (long id)  {
        try {
            if (this.repoUseri.findOne(id).isEmpty()) {
                Logger.LogException("delete", "null", "");
                return;
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

            this.repoMessage.findAll()
                    .forEach(
                            message -> {
                                if (message.getId_to() == id || message.getId_from() == id) {
                                    try {
                                        this.repoMessage.delete(message.getId());
                                    } catch (SQLException e) {
                                        System.out.println("IOException " + e.getMessage());
                                    } catch (IOException e) {
                                        System.out.println("IOException " + e.getMessage());
                                    }
                                }
                            }
                    );
            var user = this.repoUseri.delete(id);
            if (user.isPresent()) {
                EntityChangeEvent<Utilizator> event = new EntityChangeEvent<>(ChangeEventType.DELETE, user.get());
                notifyObservers(event);
            }
        } catch (ValidationException e) {
            Logger.LogException("delete", "" + id, "Trying to delete an invalid user");
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        } catch (IOException e) {
            System.out.println("IOException " + e.getMessage());
        }
    }

    public void updateUtilizator(Utilizator u) {
        Optional<Utilizator> oldUser;
        try {
            oldUser = this.repoUseri.findOne(u.getId());
            this.repoUseri.update(u);
            if(oldUser.isPresent()) {
                EntityChangeEvent<Utilizator> event = new EntityChangeEvent<>(ChangeEventType.UPDATE, u, oldUser.get());
                notifyObservers(event);
            }
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        }
    }


    public void addPrietenie (long id1, long id2)
    {
        try {
            var friends = new Prietenie(id1, id2, LocalDate.now());
            PrietenieValidator.validate2(friends, this.repoUseri, this.repoPrieteni);
            if (this.repoPrieteni.save(friends).isPresent()) {
                EntityChangeEvent<Prietenie> event = new EntityChangeEvent<>(ChangeEventType.ADD, friends);
                notifyObservers(event);
            }
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        } catch (ValidationException ex) {
            Logger.LogException("save", "null", ex.getMessage());
        }
    }

    public void deletePrietenie (long id1, long id2) {
        try {
            PrietenieValidator.validate3(id1, id2, this.repoUseri);
            Optional<Utilizator> user1 = this.repoUseri.findOne(id1);
            Optional<Utilizator>  user2 = this.repoUseri.findOne(id2);
            if (user1.isEmpty() || user2.isEmpty())
                Logger.LogException("delete", "null friendship", "");
            Optional<Prietenie> deleted = this.repoPrieteni.delete(new Tuple<>(
                    id1, id2));
            if (deleted.isPresent())
            {
                EntityChangeEvent<Prietenie> event = new EntityChangeEvent<>(ChangeEventType.DELETE, deleted.get());
                notifyObservers(event);
            }
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
            if (this.repoPrieteni.findOne(new Tuple<>(
                    max(id1, id2),
                    min(id1, id2)
            )).isPresent())
                return;
            if (id1 == id2)
                return;
            if (this.repoCereri.findOne(cerere.getId()).isEmpty())
                if(this.repoCereri.save(cerere).isPresent())
                {
                    EntityChangeEvent<Cerere> event = new EntityChangeEvent<>(ChangeEventType.ADD, cerere);
                    notifyObservers(event);
                }
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
            if (accept && this.repoPrieteni.findOne(new Tuple<>(max(id1, id2), min(id1, id2))).isEmpty()) {
                this.addPrietenie(id1, id2);
            }
            Optional<Cerere> cerere = this.repoCereri.delete(new Tuple<>(id1, id2));
            if (cerere.isPresent()) {
                EntityChangeEvent<Cerere> event = new EntityChangeEvent<>(ChangeEventType.DELETE, cerere.get());
                notifyObservers(event);
            }
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            Logger.LogException("delete", id1 + " " + id2, ex.getMessage());
        }
    }

    public Message addMessage(Long id_from, Long id_to, Long id_reply, String text) {
        try {
            if (id_reply != 0L && this.repoMessage.findOne(id_reply).isEmpty())
                return null;
            if (id_from == id_to)
                return null;
            Optional<Message> message = this.repoMessage.save(
                    new Message(
                            id_to,
                            id_from,
                            Timestamp.valueOf(LocalDateTime.now()),
                            id_reply,
                            text
                    )
            );
            if (message.isPresent()) {
                EntityChangeEvent<Message> event = new EntityChangeEvent<>(ChangeEventType.ADD, message.get());
                notifyObservers(event);
            }
            return message.get();
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Ioexception: " + ex.getMessage());
        }
        return null;
    }

    public List<Message> loadMessagesBetween (Long id_from, Long id_to) {
        try {
            if (Objects.equals(id_from, id_to))
                return null;
            PrietenieValidator.validate3(id_from, id_to, this.repoUseri);
            List<Message> messages = new ArrayList<>();
            this.repoMessage.findAll().forEach(
                    message -> {
                        if (Objects.equals(message.getId_from(), id_from) && Objects.equals(message.getId_to(), id_to))
                            messages.add(message);
                        if (Objects.equals(message.getId_to(), id_from) && Objects.equals(message.getId_from(), id_to))
                            messages.add(message);
                    }
            );
            return messages;
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    public Message getMessage (Long id) {
        try {
            Optional<Message> result = this.repoMessage.findOne(id);
            if (result.isEmpty())
                return null;
            return result.get();
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    public Utilizator getUtilizator(Long id) {
        try {
            return this.repoUseri.findOne(id).get();
        } catch (Exception ex) {
            Logger.LogException("findOne", id.toString(), ex.getMessage());
        }
        return null;
    }

    public List<Utilizator> getAll() {
        try {
            List<Utilizator> result = new ArrayList<>();
            this.repoUseri.findAll().forEach(
                    user -> result.add(user)
            );
            return result;
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        }
        return null;
    }

    public List<Utilizator> getAllOnPage (int pageNr, int pageSize, List<Utilizator> userList) {
        if (pageNr < 0 || pageSize <= 0 || userList == null || userList.isEmpty())
            return new ArrayList<>();
        if (pageNr * pageSize >= userList.size())
            return new ArrayList<>();
        return userList.subList(
                pageNr * pageSize,
                Math.min(pageNr * pageSize + pageSize, userList.size())
        );
    }

    public Utilizator login (String username, String password) {
        try {
            Optional<Username> loginfind = this.repoLogin.findOne(username);
            if (loginfind.isEmpty()) {
                System.out.println("invalid login");
                return null;
            }
            Username login = loginfind.get();
            var decrypted_password = Crypter.decrypt2(
                    login.getPassword(),
                    new Scanner(
                            new File(
                                    "C:\\Users\\Alex\\Desktop\\key.txt"
                            )
                    )
                            .nextLine());
            if (!decrypted_password.equals(password))
                return null;

            Long id = login.getIdLong();
            AtomicReference<Utilizator> result = new AtomicReference<>();
            this.repoUseri.findAll().forEach(
                    user -> {
                        if (user.getId().equals(id) ) {
                            result.set(user);
                        }
                    }
            );
            if (result.get() != null)
                return result.get();
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<Cerere> getCereriForUser(Long id) {
        try {
            List<Cerere> result = new ArrayList<>();
            if (repoCereri.findAll() == null)
                return null;
            this.repoCereri.findAll().forEach(
                    cerere -> {
                        if (cerere.getTo().equals(id))
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
            Optional<Prietenie> prietenie = this.repoPrieteni.findOne(
                    new Tuple<>(max(id1, id2), min(id1, id2)));
            if (prietenie.isPresent())
                return prietenie.get();
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        }
        return null;
    }

    public List<Utilizator> getFriendsFor(Long id) {
        try {
            List<Utilizator> result = new ArrayList<>();
            if (repoPrieteni.findAll() == null)
                return null;
            this.repoPrieteni.findAll().forEach(
                    prieteni -> {
                        if (prieteni.getIdFriend2().equals(id))
                            result.add(this.getUtilizator(prieteni.getIdFriend1()));
                        if (prieteni.getIdFriend1().equals(id))
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
        observers.forEach(x->x.update(t));
    }

    public List<Utilizator> getFriendsInCommon(Long id1, Long id2) {
        var id1Friends = this.getFriendsFor(id1);
        id1Friends.retainAll(this.getFriendsFor(id2));
        return id1Friends;
    }
}

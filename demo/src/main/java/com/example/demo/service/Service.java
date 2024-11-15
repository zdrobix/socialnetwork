package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.domain.validators.PrietenieValidator;
import com.example.demo.domain.validators.ValidationException;

import com.example.demo.logs.Logger;
import com.example.demo.password.Crypter;
import com.example.demo.repo.db.FriendRequestDatabaseRepository;
import com.example.demo.repo.db.UserDatabaseRepository;
import com.example.demo.repo.db.FriendshipDatabaseRepository;
import com.example.demo.repo.db.UserLoginDatabaseRepository;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Service {
    private final UserDatabaseRepository repoUseri;
    private final FriendshipDatabaseRepository repoPrieteni;
    private final FriendRequestDatabaseRepository repoCereri;
    private final UserLoginDatabaseRepository repoLogin;
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
            this.repoUseri.save(user);
            this.repoLogin.save(new Username(username, password, user.getId()));
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
            return this.repoUseri.delete(id).get();
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
            this.repoCereri.delete(new Tuple<>(
                    id1, id2));
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            Logger.LogException("delete", id1 + " " + id2, ex.getMessage());
        }
    }



    private Integer DFS(Long userId, Set<Long> visited, List<Long> userIds)  {
        try {
            visited.add(userId);
            AtomicInteger size = new AtomicInteger(1);
            userIds.add(userId);
            var list_friends = new ArrayList<Long>();
            this.repoPrieteni.findAll()
                    .forEach(
                            friendship -> {
                                if (friendship.getIdFriend1() != null && friendship.getIdFriend1() == userId)
                                    list_friends.add(friendship.getIdFriend2());
                                if (friendship.getIdFriend2() != null && friendship.getIdFriend2() == userId)
                                    list_friends.add(friendship.getIdFriend1());
                            }
                    );
            list_friends
                    .forEach(
                            friendId -> {
                                if (!visited.contains(friendId)) {
                                    size.addAndGet(DFS(friendId, visited, userIds));
                                }
                            }
                    );
            return size.get();
        } catch (Exception ex) {
            Logger.LogException("connect", "", ex.getMessage());
        }
        return 0;
    }

    public Integer numberOfCommunities() throws SQLException {
        Set<Long> visited = new HashSet<>();
        AtomicInteger count = new AtomicInteger();
        List<Long> list = new ArrayList<>();
        this.repoUseri.findAll()
                .forEach(
                        user -> {
                            Long userId = user.getId();
                            if (userId != null && !visited.contains(userId)) {
                                DFS(userId, visited, list);
                                count.getAndIncrement();
                            }
                        }
                );
        return count.get();
    }

    public List<Long> largestCommunity () throws SQLException {
        try {
            Set<Long> visited = new HashSet<>();
            List<Long> largestCommunityIds = new ArrayList<>();
            final int[] maxSize = {0};
            this.repoUseri.findAll()
                    .forEach(
                            user -> {
                                Long userId = user.getId();
                                if (userId != null && !visited.contains(userId)) {
                                    List<Long> userIds = new ArrayList<>();
                                    int size = DFS(userId, visited, userIds);
                                    if (size > maxSize[0]) {
                                        maxSize[0] = size;
                                        largestCommunityIds.clear();
                                        largestCommunityIds.addAll(userIds);
                                    }
                                }
                            }
                    );
            return largestCommunityIds;
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
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
            if (!password.equals(
                    Crypter.decrypt(
                            login.getPassword(),
                            new Scanner(
                                    new File(
                                            "C:\\Users\\Alex\\Desktop\\key.txt"
                                    )
                            )
                                    .nextLine())))
                return false;

            Long id = login.getIdLong(); System.out.println(id + " " + login.getUsername() + '\n');
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

    public List<Long> getFriendsForCurrent() {
        try {
            List<Long> result = new ArrayList<>();
            if (repoPrieteni.findAll() == null)
                return null;
            this.repoPrieteni.findAll().forEach(
                    prieteni -> {
                        if (prieteni.getIdFriend2().equals(this.currentUser.getId()))
                            result.add(prieteni.getIdFriend1());
                        if (prieteni.getIdFriend1().equals(this.currentUser.getId()))
                            result.add(prieteni.getIdFriend2());
                    }
            );
            return result;
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        }
        return null;
    }
}

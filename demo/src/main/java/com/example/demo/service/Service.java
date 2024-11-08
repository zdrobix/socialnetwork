package com.example.demo.service;

import com.example.demo.domain.Prietenie;
import com.example.demo.domain.Tuple;
import com.example.demo.domain.validators.PrietenieValidator;
import com.example.demo.domain.validators.ValidationException;
import com.example.demo.domain.Utilizator;

import com.example.demo.logs.Logger;
import com.example.demo.repo.db.UserDatabaseRepository;
import com.example.demo.repo.db.FriendshipDatabaseRepository;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Service {
    private final UserDatabaseRepository repo;
    private final FriendshipDatabaseRepository repoPrieteni;
    public Utilizator currentUser;

    public Service(UserDatabaseRepository repo_, FriendshipDatabaseRepository repoPrieteni_) {
        this.repo = repo_;
        this.repoPrieteni = repoPrieteni_;
        this.currentUser = null;
    }

    public void addUtilizator (String firstName, String lastName) {
        try {
            var user = new Utilizator(firstName, lastName);
            user.setId(
                    this.repo.generateFirstId()
            );
            this.repo.save(user);
        } catch (ValidationException ex) {
            Logger.LogException("save", firstName + " " + lastName, ex.getMessage());
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        }
    }

    public Utilizator deleteUtilizator (long id)  {
        try {
            if (this.repo.findOne(id).isEmpty()) {
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
            this.repo.findOne(id)
                    .get()
                    .getFriends()
                    .forEach(
                            idUser -> {
                                try {
                                    this.repo.findOne(idUser)
                                            .get()
                                            .removeFriend(
                                                    this.repo.findOne(id)
                                                            .get()
                                            );
                                } catch (SQLException ex) {
                                    Logger.LogException("connect", "", ex.getMessage());
                                }
                            }
                    );
            this.repo.delete(id).isEmpty();
        } catch (ValidationException | IOException e) {
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
            friends.setId(
                    new Tuple<>(
                            id1, id2));
            PrietenieValidator.validate2(friends, this.repo, this.repoPrieteni);
            this.repoPrieteni.save(friends);
        } catch (SQLException ex) {
            Logger.LogException("connect", "", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            Logger.LogException("save", "null", ex.getMessage());
        }
    }

    public void deletePrietenie (long id1, long id2) {
        try {
            PrietenieValidator.validate3(id1, id2, this.repo);
            var user1 = this.repo.findOne(id1).get();
            var user2 = this.repo.findOne(id2).get();
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
        this.repo.findAll()
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
            this.repo.findAll()
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
            return this.repo.findOne(id).get();
        } catch (Exception ex) {
            Logger.LogException("findOne", id.toString(), ex.getMessage());
        }
        return null;
    }

    public List<Utilizator> getAll() {
        try {
            List<Utilizator> result = new ArrayList<>();
            this.repo.findAll().forEach(
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

    public boolean login (Long id, String name) {
        try {
            this.currentUser = null;
            AtomicReference<Utilizator> result = new AtomicReference<>();
            this.repo.findAll().forEach(
                    user -> {
                        if (user.getId().equals(id) && user.getLastName().equals(name)) {
                            result.set(user);
                        }
                    }
            );
            if (result.get() != null)
            {
                this.currentUser = result.get();
                return true;
            }
        } catch (SQLException e) {
            Logger.LogException("connect", "", e.getMessage());
        }
        return false;
    }
}

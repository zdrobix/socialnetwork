package com.example.demo.repo.db;

import com.example.demo.domain.Utilizator;
import com.example.demo.domain.validators.Validator;
import com.example.demo.logs.Logger;
import com.example.demo.password.Crypter;
import com.example.demo.repo.Repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class UserDatabaseRepository implements Repository<Long, Utilizator> {

    private final List<String> connectionListCredentials;
    private final Validator<Utilizator> validator;
    private final static Logger logger = new Logger();

    public UserDatabaseRepository(String url, String username, String password, Validator<Utilizator> validator_)  {
        this.connectionListCredentials = Arrays.asList(url, username, password);
        this.validator = validator_;
    }

    public static Connection connectToDb (List<String> connectionCredentials)  {
        boolean connected = false;
        Connection connection = null;
        try {
            connection = DriverManager
                    .getConnection(
                            connectionCredentials.get(0),
                            connectionCredentials.get(1),
                            connectionCredentials.get(2)
                    );
            connected = true;
        } catch (SQLException e) {   System.out.println(e.getMessage());
        } finally {
            logger.LogConnection(connected);
            return connection;
        }
    }

    public Optional<Utilizator> findOne(Long id) throws SQLException {
        if (id == null)
            throw new IllegalArgumentException("id is null");
        var connection = connectToDb(this.connectionListCredentials);
        Utilizator user = null;
        if (connection == null)
            return Optional.empty();
        PreparedStatement statement = connection
                                    .prepareStatement("SELECT * FROM USERS WHERE ID = ?");

        statement.setLong(1, id);
        var result = statement.executeQuery();
        if (result.next()) {
            user = new Utilizator(
                    result.getString(2),
                    result.getString(3)
            );
            user.setId(
                    result.getLong(1));
        }
        result.close();
        connection.close();
        if (user != null )
            logger.LogModify("findOne", user.toString());
        else logger.LogModify("findOne", "null");
        return Optional.ofNullable(user);
    }

    public Iterable<Utilizator> findAll() throws SQLException {
        var connection = connectToDb(this.connectionListCredentials);
        if (connection == null)
            return null;

        var result = connection
                .prepareStatement("SELECT * FROM USERS")
                .executeQuery();

        Map<Long, Utilizator> users = new HashMap<>();
        while (result.next()) {
            var user = new Utilizator(
                    result.getString(2),
                    result.getString(3)
            );
            user.setId(result.getLong(1));
            users.putIfAbsent(
                    result.getLong(1),
                    user
            );
        }
        result.close();
        connection.close();
        logger.LogModify("findAll", "");
        return users.values();
    }

    public Optional<Utilizator> save(Utilizator user) throws SQLException {
        if (user == null)
            throw new IllegalArgumentException("user is null");
        validator.validate(user);
        var connection = connectToDb(this.connectionListCredentials);
        if (connection == null)
            return Optional.empty();

        PreparedStatement statement = connection
                .prepareStatement("INSERT INTO USERS (id, first_name, last_name) VALUES (?, ?, ?)");
        statement.setLong(1, user.getId());
        statement.setString(2, user.getFirstName());
        statement.setString(3, user.getLastName());System.out.println(statement);
        statement.executeUpdate();
        connection.close();
        logger.LogModify("save", user.toString());
        return Optional.of(user);
    }


    public Optional<Utilizator> delete(Long id) throws SQLException {
        if (id == null)
            throw new IllegalArgumentException("id is null");
        var userToDelete = this.findOne(id).get();
        if (userToDelete == null)
            return Optional.empty();
        var connection = connectToDb(this.connectionListCredentials);
        if (connection == null)
            return Optional.empty();
        PreparedStatement statement = connection
                .prepareStatement("DELETE FROM USERS WHERE ID = ?");
        statement.setLong(1, id);
        statement.executeUpdate();
        logger.LogModify("delete", userToDelete.toString());
        return Optional.of(userToDelete);
    }

    @Override
    public Optional<Utilizator> update(Utilizator user) throws SQLException {
        if(user == null)
            throw new IllegalArgumentException("entity must be not null!");
        validator.validate(user);
        String sql = "update users set first_name = ?, last_name = ? where id = ?";
        try (Connection connection = connectToDb(this.connectionListCredentials);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setLong(3, user.getId());
            if( ps.executeUpdate() > 0 )
                return Optional.empty();
            return Optional.ofNullable(user);
        } catch (SQLException e) {
            throw new SQLException();
        }
    }

    public int size() throws SQLException {
        AtomicInteger count = new AtomicInteger();
        this.findAll().forEach(
                a -> count.getAndIncrement()
        );
        return count.get();
    }

    public Long generateFirstId() throws SQLException {
        List<Long> numbers = new ArrayList<>();
        this.findAll()
                .forEach(
                        user -> numbers.add(user.getId())
                );
        Collections.sort(numbers);
        long newId = 1;
        for (var num : numbers) {
            if (num == newId)
                newId++;
            else
                if (num > newId)
                    break;
        }
        return newId;
    }
}



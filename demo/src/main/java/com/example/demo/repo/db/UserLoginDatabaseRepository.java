package com.example.demo.repo.db;

import com.example.demo.domain.Username;
import com.example.demo.domain.Utilizator;
import com.example.demo.repo.Repository;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.example.demo.repo.db.UserDatabaseRepository.connectToDb;

public class UserLoginDatabaseRepository implements Repository<String, Username> {
    private List<String> connectionListCredentials;

    public UserLoginDatabaseRepository(String url, String username, String password) {
        this.connectionListCredentials = Arrays.asList(url, username, password);
    }

    @Override
    public Optional<Username> findOne(String s) throws SQLException, IOException {
        if (s == null)
            throw new IllegalArgumentException("username is null");
        var connection = connectToDb(this.connectionListCredentials);
        Username username = null;
        if (connection == null)
            return Optional.empty();
        PreparedStatement statement = connection
                .prepareStatement("SELECT * FROM USER_LOGIN WHERE username = ?");

        statement.setString(1, s);
        var result = statement.executeQuery();
        if (result.next()) {
            username = new Username(result.getString(1), result.getString(2));
        }
        result.close();
        connection.close();
        return Optional.ofNullable(username);
    }

    @Override
    public Iterable<Username> findAll() throws SQLException, IOException {
        return null;
    }

    @Override
    public Optional<Username> save(Username entity) throws SQLException, IOException {
        if (entity == null)
            throw new IllegalArgumentException("login is null");
        var connection = connectToDb(this.connectionListCredentials);
        if (connection == null)
            return Optional.empty();

        PreparedStatement statement = connection
                .prepareStatement("INSERT INTO USER_LOGIN (username, password) VALUES (?, ?)");
        statement.setString(1, entity.getUsername());
        statement.setString(2, entity.getPassword());
        statement.executeUpdate();
        return Optional.of(entity);
    }

    @Override
    public Optional<Username> delete(String s) throws SQLException, IOException {
        if (s == null)
            throw new IllegalArgumentException("username is null");
        var userToDelete = this.findOne(s).get();
        if (userToDelete == null)
            return Optional.empty();
        var connection = connectToDb(this.connectionListCredentials);
        if (connection == null)
            return Optional.empty();
        PreparedStatement statement = connection
                .prepareStatement("DELETE FROM USER_LOGIN WHERE username = ?");
        statement.setString(1, s);
        statement.executeUpdate();
        return Optional.of(userToDelete);
    }

    @Override
    public Optional<Username> update(Username entity) throws SQLException {
        return Optional.empty();
    }
}

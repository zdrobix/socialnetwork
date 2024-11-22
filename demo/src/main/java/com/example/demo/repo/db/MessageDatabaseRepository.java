package com.example.demo.repo.db;

import com.example.demo.domain.Message;
import com.example.demo.domain.Utilizator;
import com.example.demo.logs.Logger;
import com.example.demo.repo.Repository;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import static com.example.demo.repo.db.UserDatabaseRepository.connectToDb;

public class MessageDatabaseRepository implements Repository<Long, Message> {
    private final List<String> connectionListCredentials;
    private final static Logger logger = new Logger();

    public MessageDatabaseRepository(String url, String username, String password) {
        this.connectionListCredentials = Arrays.asList(url, username, password);
    }
    @Override
    public Optional<Message> findOne(Long id) throws SQLException, IOException {
        if (id == null)
            throw new IllegalArgumentException("id is null");
        var connection = connectToDb(this.connectionListCredentials);
        Message message = null;
        if (connection == null)
            return Optional.empty();
        PreparedStatement statement = connection
                .prepareStatement("SELECT * FROM MESSAGES WHERE ID = ?");

        statement.setLong(1, id);
        var result = statement.executeQuery();
        if (result.next()) {
            message = new Message(
                    result.getLong(2),
                    result.getLong(3),
                    result.getTimestamp(4),
                    result.getLong(5),
                    result.getString(6)
            );
            message.setId(
                    result.getLong(1)
            );
        }
        result.close();
        connection.close();
        if (message != null )
            logger.LogModify("findOne", message.toString());
        else logger.LogModify("findOne", "null");
        return Optional.ofNullable(message);
    }

    @Override
    public Iterable<Message> findAll() throws SQLException, IOException {
        var connection = connectToDb(this.connectionListCredentials);
        if (connection == null)
            return null;

        var result = connection
                .prepareStatement("SELECT * FROM MESSAGES")
                .executeQuery();

        Map<Long, Message> messages = new HashMap<>();
        while (result.next()) {
            var message = new Message(
                    result.getLong(2),
                    result.getLong(3),
                    result.getTimestamp(4),
                    result.getLong(5),
                    result.getString(6)
            );
            message.setId(
                    result.getLong(1)
            );
            messages.putIfAbsent(
                    result.getLong(1),
                    message
            );
        }
        result.close();
        connection.close();
        logger.LogModify("findAll", "");
        return messages.values();
    }

    @Override
    public Optional<Message> save(Message message) throws SQLException, IOException {
        if (message == null)
            throw new IllegalArgumentException("message is null");
        var connection = connectToDb(this.connectionListCredentials);
        if (connection == null)
            return Optional.empty();
        PreparedStatement statement = connection
                .prepareStatement("INSERT INTO MESSAGES (id_to, id_from, datetime, id_reply, text) " +
                        "VALUES (?, ?, ?, ?, ?);");
        statement.setLong(1, message.getId_to());
        statement.setLong(2, message.getId_from());
        statement.setTimestamp(3, message.getDateTime());
        if (message.getId_reply() != null)
            statement.setLong(4, message.getId_reply());
        else statement.setNull(4, Types.BIGINT);
        statement.setString(5, message.getText());
        statement.executeUpdate();
        logger.LogModify("save", message.toString());
        connection.close();System.out.println("a");
        return Optional.of(message);
    }

    @Override
    public Optional<Message> delete(Long id) throws SQLException, IOException {
        //not implemented
        return Optional.empty();
    }

    @Override
    public Optional<Message> update(Message message) throws SQLException {
        //not implemented
        return Optional.empty();
    }
}

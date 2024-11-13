package main.java.com.example.demo.repo.db;

import main.java.com.example.demo.domain.Cerere;
import main.java.com.example.demo.domain.validators.Validator;
import main.java.com.example.demo.repo.Repository;
import main.java.com.example.demo.domain.Tuple;
import main.java.com.example.demo.domain.Prietenie;

import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class FriendRequestDatabaseRepository implements Repository<Tuple<Long, Long>, Cerere> {

    private List<String> connectionListCredentials;
    private Validator<Prietenie> validator;

    public FriendRequestDatabaseRepository(String url, String username, String password, Validator<Prietenie> validator_)  {
        this.connectionListCredentials = Arrays.asList(url, username, password);
        this.validator = validator_;
    }

    @Override
    public Optional<Cerere> findOne(Tuple<Long, Long> ID) throws SQLException, IllegalArgumentException {
        if (ID == null)
            throw new IllegalArgumentException("ID cannot be null");
        var connection = UserDatabaseRepository
                .connectToDb(this.connectionListCredentials);
        if (connection == null)
            return Optional.empty();

        PreparedStatement statement = connection
                .prepareStatement("SELECT * FROM FRIEND_REQUESTS WHERE (id_from = ? AND id_to = ?) OR (id_from = ? AND id_to = ?);");
        statement.setLong(1, ID.getLeft());
        statement.setLong(2, ID.getRight());
        statement.setLong(3, ID.getRight());
        statement.setLong(4, ID.getLeft());
        ResultSet result = statement.executeQuery();

        Cerere cerere = null;
        if (result.next()) {
            cerere = new Cerere(
                    max(result.getLong(1),
                            result.getLong(2)),
                    min(result.getLong(1),
                            result.getLong(2)),
                    result.getDate(3).toLocalDate()
            );
        }
        result.close();
        connection.close();
        return Optional.ofNullable(cerere);
    }

    @Override
    public Iterable<Cerere> findAll() throws SQLException {
        var connection = UserDatabaseRepository
                .connectToDb(this.connectionListCredentials);
        if (connection == null)
            return null;
        Map<Tuple<Long, Long>, Cerere> cereri = new HashMap<>();

        var result = connection
                .prepareStatement("SELECT * FROM FRIEND_REQUESTS")
                .executeQuery();

        while (result.next()) {
            var id1 = result.getLong(1);
            var id2 = result.getLong(2);
            cereri.putIfAbsent(
                    new Tuple(
                            id1,
                            id2
                    ),
                    new Cerere(
                            id1,
                            id2,
                            result.getDate(3).toLocalDate()
                    )
            );
        }
        result.close();
        connection.close();
        return cereri.values();
    }

    @Override
    public Optional<Cerere> save(Cerere cerere) throws SQLException, IllegalArgumentException {
        if (cerere == null)
            throw new IllegalArgumentException("Request cannot be null");
        var connection = UserDatabaseRepository
                .connectToDb(this.connectionListCredentials);
        if (connection == null)
            return Optional.empty();
        PreparedStatement statement = connection
                .prepareStatement("INSERT INTO FRIEND_REQUESTS (id_from, id_to, date) VALUES (?, ?, ?)");
        statement.setLong(1,
                cerere.getFrom());
        statement.setLong(2,
                cerere.getTo());
        statement.setDate(3, Date.valueOf(cerere.getDate()));
        statement.executeUpdate();
        return Optional.of(cerere);
    }

    @Override
    public Optional<Cerere> delete(Tuple<Long, Long> ID) throws SQLException, IllegalArgumentException {
        if (ID == null)
            throw new IllegalArgumentException("ID cannot be null");
        var connection = UserDatabaseRepository
                .connectToDb(this.connectionListCredentials);
        if (connection == null)
            return Optional.empty();

        Optional<Cerere> cerereOptional = this.findOne(ID);
        if (!cerereOptional.isPresent()) {
            return Optional.empty();
        }
        Cerere cerereToDelete = cerereOptional.get();

        PreparedStatement statement = connection
                .prepareStatement("DELETE FROM FRIEND_REQUESTS WHERE (id_from = ? AND id_to = ?) OR (id_from = ? AND id_to = ?);");
        statement.setLong(1, ID.getLeft());
        statement.setLong(2, ID.getRight());
        statement.setLong(3, ID.getRight());
        statement.setLong(4, ID.getLeft());
        statement.executeUpdate();

        return Optional.of(cerereToDelete);
    }

    @Override
    public Optional<Cerere> update(Cerere cerere) throws SQLException {
        if (cerere == null)
            throw new IllegalArgumentException("cerere is null");
        validator.validate(cerere.asPrietenie());
        return Optional.empty();
    }

    public int size() throws SQLException, IOException {
        AtomicInteger count = new AtomicInteger();
        this.findAll().forEach(
                a -> count.getAndIncrement()
        );
        return count.get();
    }
}

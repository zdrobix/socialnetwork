package main.java.com.example.demo.domain.validators;

import main.java.com.example.demo.domain.Prietenie;
import main.java.com.example.demo.domain.Tuple;

import main.java.com.example.demo.repo.db.UserDatabaseRepository;
import main.java.com.example.demo.repo.db.FriendshipDatabaseRepository;

import java.io.IOException;
import java.sql.SQLException;

public class PrietenieValidator implements Validator<Prietenie> {
    public void validate(Prietenie prietenie) throws ValidationException {
        String errors = "";
        if (prietenie.getIdFriend1() == prietenie.getIdFriend2())
            errors += "You can't friend yourself. ";
        if (prietenie.getDate() == null)
            errors += "The starting date of the friendship is invalid. ";
        if (!errors.equals("")) {
            throw new ValidationException(errors);
        }
    }
    public static void validate2(Prietenie prietenie, UserDatabaseRepository repo, FriendshipDatabaseRepository repo2) throws ValidationException {
        try {
            String errors = "";
            if (repo.findOne(prietenie.getIdFriend1()) == null)
                errors += "Invalid id. ";
            if (repo.findOne(prietenie.getIdFriend2()) == null)
                errors += "Invalid id. ";
            if (!repo2.findOne(new Tuple<>(prietenie.getIdFriend2(), prietenie.getIdFriend1())).isEmpty())
                errors += "Already friends. ";
            if (!repo2.findOne(prietenie.getId()).isEmpty())
                errors += "Already friends. ";
            if (!errors.equals("")) {
                throw new ValidationException(errors);
            }
        } catch (SQLException ex) {
            throw new ValidationException("Cannot acces database.");
        }
    }

    public static void validate3 (Long id1, Long id2, UserDatabaseRepository repo) throws ValidationException {
        try {
            String errors = "";
            if (repo.findOne(id1) == null)
                errors += "Invalid id. ";
            if (repo.findOne(id2) == null)
                errors += "Invalid id. ";
            if (!errors.equals("")) {
                throw new ValidationException(errors);
            }
        } catch (SQLException ex) {
            throw new ValidationException("Cannot acces database.");
        }
    }
}

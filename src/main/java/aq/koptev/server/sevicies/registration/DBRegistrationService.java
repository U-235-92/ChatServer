package aq.koptev.server.sevicies.registration;

import aq.koptev.server.models.User;
import aq.koptev.server.sevicies.dbconnect.DBConnector;
import aq.koptev.server.sevicies.dbconnect.SQLiteConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBRegistrationService implements RegistrationService {

    private final String URL = "jdbc:sqlite:src/main/resources/db/chat-db.db";
    private DBConnector connector;

    public DBRegistrationService() {
        connector = new SQLiteConnector();
    }

    @Override
    public boolean registerUser(User user) {
        String sql = "INSERT INTO Users (login, password) VALUES (?, ?)";
        try (Connection connection = connector.getConnection(URL);
             PreparedStatement preparedStatement = connector.getPreparedStatement(connection, sql)) {
            preparedStatement.setString(1, user.getLogin());
            preparedStatement.setString(2, user.getPassword());
            connection.setAutoCommit(false);
            preparedStatement.addBatch();
            preparedStatement.executeBatch();
            connection.setAutoCommit(true);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

package aq.koptev.server.sevicies.authentication;

import aq.koptev.server.models.User;
import aq.koptev.server.sevicies.dbconnect.DBConnector;
import aq.koptev.server.sevicies.dbconnect.SQLiteConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBAuthenticationService implements AuthenticationService {

    private final String URL = "jdbc:sqlite:src/main/resources/db/chat-db.db";
    private DBConnector connector;

    public DBAuthenticationService() {
        connector = new SQLiteConnector();
    }

    @Override
    public User getUser(String login, String password) {
        User user = null;
        String sql = "SELECT login, password FROM Users WHERE login = ?";
        try (Connection connection = connector.getConnection(URL);
             PreparedStatement preparedStatement = connector.getPreparedStatement(connection, sql)) {
            preparedStatement.setString(1, login);
            ResultSet resultSet = preparedStatement.executeQuery();
            String resultLogin = resultSet.getString(1).trim();
            String resultPassword = resultSet.getString(2).trim();
            if(resultLogin.equals(login)) {
                if(resultPassword.equals(password)) {
                    user = new User(resultLogin, resultPassword);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public boolean isExistUser(String login) {
        return getUser(login, "") != null;
    }

    @Override
    public String getErrorAuthenticationMessage(String login, String password) {
        String sql = "SELECT login, password FROM Users WHERE login = ?";
        try (Connection connection = connector.getConnection(URL);
             PreparedStatement preparedStatement = connector.getPreparedStatement(connection, sql)) {
            preparedStatement.setString(1, login);
            ResultSet resultSet = preparedStatement.executeQuery();
            String resultLogin = resultSet.getString(1).trim();
            String resultPassword = resultSet.getString(2).trim();
            if(resultLogin.equals(login)) {
                if(resultPassword.equals(password)) {
                    return AuthenticationMessages.EMPTY_MESSAGE.getMessage();
                } else {
                    return AuthenticationMessages.WRONG_PASSWORD.getMessage();
                }
            } else {
                return AuthenticationMessages.WRONG_LOGIN.getMessage();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
            return AuthenticationMessages.WRONG_LOGIN.getMessage();
        }
        return AuthenticationMessages.EMPTY_MESSAGE.getMessage();
    }
}

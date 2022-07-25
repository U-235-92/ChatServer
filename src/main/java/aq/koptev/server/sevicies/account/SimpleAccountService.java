package aq.koptev.server.sevicies.account;

import aq.koptev.server.sevicies.dbconnect.DBConnectURL;
import aq.koptev.server.sevicies.dbconnect.DBConnector;
import aq.koptev.server.sevicies.dbconnect.SQLiteConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SimpleAccountService implements AccountService {

    private DBConnector connector;

    public SimpleAccountService() {
        connector = new SQLiteConnector();
    }

    @Override
    public boolean changeAccountSettings(String settings) {
        String oldLogin = null;
        String newLogin = null;
        String oldPassword = null;
        String newPassword = null;
        String sqlSelectNewLogin = "SELECT login FROM Users WHERE login = ?";
        if(settings.split("\\s+", 4).length > 3) {
            oldLogin = settings.split("\\s+", 4)[0];
            newLogin = settings.split("\\s+", 4)[1];
            oldPassword = settings.split("\\s+", 4)[2];
            newPassword = settings.split("\\s+", 4)[3];
            try(Connection connection = connector.getConnection(DBConnectURL.CHAT_DB.getURL());
                    PreparedStatement preparedStatement = connector.getPreparedStatement(connection, sqlSelectNewLogin)) {
                preparedStatement.setString(1, newLogin);
                ResultSet resultSet = preparedStatement.executeQuery();
                String resultQuery = resultSet.getString(1);
                if(resultQuery == null) {
                    String sqlUpdateUser = "UPDATE Users SET login = ?, password = ? WHERE login = ?";
                    preparedStatement.setString(1, newLogin);
                    preparedStatement.setString(2, newPassword);
                    preparedStatement.setString(3, oldLogin);
                    preparedStatement.executeUpdate();
                    return true;
                } else {
                    return false;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            oldLogin = settings.split("\\s+", 3)[0];
            newLogin = settings.split("\\s+", 3)[1];
            oldPassword = settings.split("\\s+", 3)[2];
            newPassword = "";
            try(Connection connection = connector.getConnection(DBConnectURL.CHAT_DB.getURL());
                PreparedStatement preparedStatement = connector.getPreparedStatement(connection, sqlSelectNewLogin)) {
                preparedStatement.setString(1, newLogin);
                ResultSet resultSet = preparedStatement.executeQuery();
                String resultQuery = resultSet.getString(1);
                if(resultQuery == null) {
                    String sqlUpdateUser = "UPDATE Users SET login = ?, password = ? WHERE login = ?";
                    preparedStatement.setString(1, newLogin);
                    preparedStatement.setString(2, newPassword);
                    preparedStatement.setString(3, oldLogin);
                    preparedStatement.executeUpdate();
                    return true;
                } else {
                    return false;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}

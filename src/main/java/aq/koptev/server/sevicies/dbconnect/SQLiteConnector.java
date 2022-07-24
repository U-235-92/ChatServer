package aq.koptev.server.sevicies.dbconnect;

import java.sql.*;

public class SQLiteConnector implements DBConnector {

    private Connection connection;
    private PreparedStatement preparedStatement;

    @Override
    public Connection getConnection(String url) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection(url);
        return connection;
    }

    @Override
    public PreparedStatement getPreparedStatement(Connection connection, String sql) throws SQLException {
        preparedStatement = connection.prepareStatement(sql);
        return preparedStatement;
    }
}

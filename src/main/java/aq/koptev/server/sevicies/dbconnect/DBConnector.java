package aq.koptev.server.sevicies.dbconnect;

import java.sql.*;

public interface DBConnector {
    Connection getConnection(String url) throws SQLException, ClassNotFoundException;
    PreparedStatement getPreparedStatement(Connection connection, String sql) throws SQLException;
}

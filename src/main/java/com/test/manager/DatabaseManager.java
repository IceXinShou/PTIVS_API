package com.test.manager;

import java.sql.*;

public class DatabaseManager {
    public static Connection conn;

    public DatabaseManager() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:./data.db");

        Statement stmt = conn.createStatement();
        if (!stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='certificate';").next()) {
            // create a table
            stmt.executeUpdate("CREATE TABLE 'certificate' (" +
                    "client_token INT PRIMARY KEY  NOT NULL   ON CONFLICT FAIL, " +
                    "id             TEXT NOT NULL, " +
                    "pwd            TEXT NOT NULL, " +
                    "ip             TEXT NOT NULL, " +
                    "server_token    TEXT NOT NULL  " +
                    ")"
            );
        }
        stmt.close();
    }

    public void add(AccountManager accountManager) throws SQLException {
        String insert = "INSERT INTO 'certificate' VALUES (?, ?, ?, ?, ?)";
        PreparedStatement createMessage = conn.prepareStatement(insert);
        createMessage.setString(1, accountManager.clientToken);
        createMessage.setString(2, accountManager.id);
        createMessage.setString(3, accountManager.pwd);
        createMessage.setString(4, accountManager.ip);
        createMessage.setString(5, accountManager.serverToken);
        createMessage.executeUpdate();
    }

    public boolean verify(String clientToken, String ip) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    String.format("SELECT ip FROM 'certificate' WHERE client_token = '%s'", clientToken)
            );
            stmt.close();
            return rs.getString("ip").equals(ip);
        } catch (SQLException e) {
            return false;
        }
    }
}

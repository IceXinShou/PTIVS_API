package com.test.manager;

import org.jetbrains.annotations.Nullable;

import java.sql.*;

public class DatabaseManager {
    public static Connection conn;

    public DatabaseManager() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:./data.db");

        Statement stmt = conn.createStatement();
        if (!stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='certificate';").next()) {
            // create a table
            stmt.executeUpdate("CREATE TABLE 'certificate' (" +
                    "client_token INT PRIMARY KEY  NOT NULL ON CONFLICT FAIL, " +
                    "id             TEXT NOT NULL, " +
                    "pwd            TEXT NOT NULL, " +
                    "ip             TEXT NOT NULL, " +
                    "server_token   TEXT NOT NULL  " +
                    ")"
            );
        }
        stmt.close();
    }

    public void add(Account account) throws SQLException {
        String insert = "INSERT INTO 'certificate' VALUES (?, ?, ?, ?, ?)";
        PreparedStatement createMessage = conn.prepareStatement(insert);
        createMessage.setString(1, account.clientToken);
        createMessage.setString(2, account.id);
        createMessage.setString(3, account.pwd);
        createMessage.setString(4, account.ip);
        createMessage.setString(5, account.serverToken);
        createMessage.executeUpdate();
    }

    @Nullable
    public Account verify(String clientToken, String ip) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    String.format("SELECT * FROM 'certificate' WHERE client_token = '%s'", clientToken)
            );

            if (!ip.equals(rs.getString("ip"))) return null;

            return new Account(
                    rs.getString("id"),
                    rs.getString("ip"),
                    rs.getString("pwd"),
                    clientToken,
                    rs.getString("server_token")
            );
        } catch (SQLException e) {
            return null;
        }
    }
}

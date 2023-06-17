package tw.xserver.manager;

import org.jetbrains.annotations.Nullable;

import java.sql.*;

public class CertificateManager {
    public static Connection conn_cert;

    public CertificateManager() throws SQLException {
        conn_cert = DriverManager.getConnection("jdbc:sqlite:./certificate.db");

        Statement stmt = conn_cert.createStatement();
        if (!stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='certificate';").next()) {
            // create a table
            stmt.executeUpdate("CREATE TABLE 'certificate' (" +
                    "client_token TEXT PRIMARY KEY  NOT NULL ON CONFLICT FAIL, " +
                    "id           TEXT NOT NULL, " +
                    "pwd          TEXT NOT NULL, " +
                    "ip           TEXT NOT NULL, " +
                    "server_token TEXT NOT NULL  " +
                    ")"
            );
        }
        stmt.close();
    }

    public void add(Account account) throws SQLException {
        String insert = "INSERT OR REPLACE INTO 'certificate' VALUES (?, ?, ?, ?, ?)";
        PreparedStatement createMessage = conn_cert.prepareStatement(insert);
        createMessage.setString(1, account.clientToken);
        createMessage.setString(2, account.id);
        createMessage.setString(3, account.pwd);
        createMessage.setString(4, account.ip);
        createMessage.setString(5, account.serverToken);
        createMessage.executeUpdate();
        createMessage.close();
    }

    @Nullable
    public Account verify(String clientToken, String ip) {
        try {
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = conn_cert.createStatement();
                rs = stmt.executeQuery(
                        String.format("SELECT id, ip, pwd, server_token FROM 'certificate' WHERE client_token = '%s'", clientToken)
                );

                if (!ip.equals(rs.getString("ip"))) return null;

                return new Account(
                        rs.getString("id"),
                        rs.getString("ip"),
                        rs.getString("pwd"),
                        clientToken,
                        rs.getString("server_token")
                );
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            return null;
        }
    }
}

package tw.xserver.manager;

import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.Base64;

public class CertificateManager {
    public static Connection conn_cert;

    public CertificateManager() throws SQLException {
        /* 初始化 */
        /* 連線資料庫 */
        conn_cert = DriverManager.getConnection("jdbc:sqlite:./certificate.db");

        /* 初始化表格 */
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

        /* 初始化結束*/
    }

    public void add(Account account) throws SQLException {
        /* 新帳號登入 */
        /* 新增進資料庫 */
        String insert = "INSERT OR REPLACE INTO 'certificate' VALUES (?, ?, ?, ?, ?)";
        PreparedStatement createMessage = conn_cert.prepareStatement(insert);
        createMessage.setString(1, account.clientToken);
        createMessage.setString(2, account.id);
        createMessage.setString(3, Base64.getEncoder().encodeToString(account.pwd.getBytes()));
        createMessage.setString(4, account.ip);
        createMessage.setString(5, account.serverToken);
        createMessage.executeUpdate();
        createMessage.close();
    }

    @Nullable
    public Account verify(String clientToken, String ip) {
        /* 新連線驗證 */
        try {
            Statement stmt = null;
            ResultSet rs = null;
            try {
                /* 以 token 查資料庫 */
                stmt = conn_cert.createStatement();
                rs = stmt.executeQuery(
                        String.format("SELECT id, ip, pwd, server_token FROM 'certificate' WHERE client_token = '%s'", clientToken)
                );

                /* 核對 IP 身分 */
                if (!ip.equals(rs.getString("ip"))) {
                    System.out.println(ip);
                    System.out.println("ERROR");
                    return null;
                }

                /* 回傳帳號資訊 */
                return new Account(
                        rs.getString("id"),
                        rs.getString("ip"),
                        new String(Base64.getDecoder().decode(rs.getString("pwd"))),
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
            e.printStackTrace();
            return null;
        }
    }
}

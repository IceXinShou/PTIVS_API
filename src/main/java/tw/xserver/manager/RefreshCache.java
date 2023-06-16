package tw.xserver.manager;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import tw.xserver.HTML_Analyze.ReadAbsent;
import tw.xserver.HTML_Analyze.ReadRewards;
import tw.xserver.util.ErrorException;

import java.io.IOException;
import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static tw.xserver.handler.ClientHandler.getTime;
import static tw.xserver.manager.DatabaseManager.conn_cert;
import static tw.xserver.util.PageKey.ABSENT;
import static tw.xserver.util.PageKey.REWARDS;

public class RefreshCache {
    public Connection conn_cache;

    public RefreshCache() throws SQLException {
        conn_cache = DriverManager.getConnection("jdbc:sqlite:./cache.db");

        Statement stmt = conn_cache.createStatement();
        if (!stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='cache';").next()) {
            // create a table
            stmt.executeUpdate("CREATE TABLE 'cache' (" +
                    "id TEXT PRIMARY KEY  NOT NULL ON CONFLICT FAIL, " +
                    "absent          TEXT NOT NULL, " +
                    "rewards         TEXT NOT NULL" +
                    ")"
            );
        }
        stmt.close();

        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(this::refreshAllCache, 0, 30, TimeUnit.MINUTES);
    }

    private void refreshAllCache() {
        Statement stmt_cert = null;
        Statement stmt_cache = null;
        ResultSet rs_cert = null;

        try {
            stmt_cert = conn_cert.createStatement();
            stmt_cache = conn_cache.createStatement();
            rs_cert = stmt_cert.executeQuery("SELECT * FROM 'certificate';");

            System.out.println(getTime() + " starting refreshing...");
            int count = 0;
            while (rs_cert.next()) {
                count++;
                try {
                    String id = rs_cert.getString("id");
                    LoginManager login = new LoginManager(id, rs_cert.getString("pwd"));

                    JSONObject absent = ReadAbsent.readAbsent(login.fetchPageData(ABSENT));
                    JSONObject rewards = ReadRewards.readRewards(login.fetchPageData(REWARDS));

                    if (absent == null || rewards == null) continue;

                    String insert = "INSERT OR REPLACE INTO 'cache' VALUES (?, ?, ?)";
                    PreparedStatement createMessage = conn_cache.prepareStatement(insert);
                    createMessage.setString(1, id);
                    createMessage.setString(2, absent.toString());
                    createMessage.setString(3, rewards.toString());
                    createMessage.executeUpdate();
                    createMessage.close();

                } catch (ErrorException | IOException e) {
                    e.printStackTrace();
                }
            }

            System.out.println(getTime() + " refreshing successfully for " + count + " accounts!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs_cert != null) {
                try {
                    rs_cert.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (stmt_cert != null) {
                try {
                    stmt_cert.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (stmt_cache != null) {
                try {
                    stmt_cache.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void refreshCache(LoginManager login) {
        Statement stmt = null;

        try {
            stmt = conn_cache.createStatement();

            JSONObject absent = ReadAbsent.readAbsent(login.fetchPageData(ABSENT));
            JSONObject rewards = ReadRewards.readRewards(login.fetchPageData(REWARDS));

            if (absent == null || rewards == null) return;

            String insert = "INSERT OR REPLACE INTO 'cache' VALUES (?, ?, ?)";
            PreparedStatement createMessage = conn_cache.prepareStatement(insert);
            createMessage.setString(1, login.id);
            createMessage.setString(2, absent.toString());
            createMessage.setString(3, rewards.toString());
            createMessage.executeUpdate();
            createMessage.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Nullable
    public JSONObject get(String id, String type) {
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn_cache.createStatement();

            rs = stmt.executeQuery(
                    String.format("SELECT %s FROM 'cache' WHERE id = '%s'", type.toLowerCase(), id)
            );

            String data = rs.getString(type.toLowerCase());

            if (data == null)
                return null;

            return new JSONObject(data);
        } catch (Exception e) {
            e.printStackTrace();
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
        return null;
    }
}

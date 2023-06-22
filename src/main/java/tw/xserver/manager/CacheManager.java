package tw.xserver.manager;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import tw.xserver.analyze_HTML.*;
import tw.xserver.util.ErrorException;

import java.io.IOException;
import java.sql.*;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static tw.xserver.Util.getTime;
import static tw.xserver.manager.CertificateManager.conn_cert;
import static tw.xserver.util.PageKey.*;

public class CacheManager {
    public Connection conn_cache;

    public CacheManager() throws SQLException {
        /* 初始化 */
        /* 連線資料庫 */
        conn_cache = DriverManager.getConnection("jdbc:sqlite:./cache.db");

        /* 初始化表格 */
        Statement stmt = conn_cache.createStatement();
        if (!stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='cache';").next()) {
            stmt.executeUpdate("CREATE TABLE 'cache' (" +
                    "id     TEXT PRIMARY  KEY NOT NULL," +
                    "absent              TEXT NOT NULL," +
                    "rewards             TEXT NOT NULL," +
                    "history_rewards     TEXT NOT NULL," +
                    "punished_cancel_log TEXT NOT NULL," +
                    "clubs               TEXT NOT NULL," +
                    "cadres              TEXT NOT NULL," +
                    "history_score       TEXT NOT NULL," +
                    "class_table         TEXT NOT NULL " +
                    ")"
            );
        }
        stmt.close();

        /* 建立更新線程 */
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(this::refreshAllCache, 0, 30, TimeUnit.MINUTES);

        /* 初始化結束 */
    }


    private void refreshAllCache() {
        /* 自動更新 */
        /* 取得所有帳號資訊 */
        Statement stmt_cert = null;
        ResultSet rs_cert = null;

        ExecutorService executor = Executors.newFixedThreadPool(100);

        try {
            stmt_cert = conn_cert.createStatement();

            /* 取得所有帳號資訊 */
            rs_cert = stmt_cert.executeQuery("SELECT * FROM 'certificate';");

            System.out.println(getTime() + " starting updating...");
            int count = 0;
            while (rs_cert.next()) {
                count++;
                try {
                    String id = rs_cert.getString("id");
                    LoginManager login = new LoginManager(
                            id,
                            new String(Base64.getDecoder().decode(rs_cert.getString("pwd")))
                    );

                    System.out.println(getTime() + " updating cache: " + id);
                    executor.submit(() -> {
                        try {
                            PreparedStatement update = fetchSomeData(login, false);
                            update.executeUpdate();
                            update.close();
                            System.out.println(getTime() + " updated cache successfully: " + login.id);
                        } catch (Exception e) {
                            System.out.println(getTime() + " updated cache error: " + login.id);
                            e.printStackTrace();
                        }
                    });
                } catch (ErrorException | IOException e) {
                    e.printStackTrace();
                }
            }

            stmt_cert.close();
            rs_cert.close();
            executor.shutdown();

            if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
                System.out.println(getTime() + " timeout");
            } else {
                System.out.println(getTime() + " refreshing successfully for " + count + " accounts!");
            }


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
        }

        /* 等待 30 分鐘 */
    }

    public void refreshCache(LoginManager login) {
        /* 新帳號登入 */
        try {
            PreparedStatement update = fetchSomeData(login, true);
            update.executeUpdate();
            update.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public JSONObject get(String id, String type) {
        /* 資料取得 */
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

    private PreparedStatement fetchSomeData(LoginManager login, boolean all) throws IOException, ErrorException, SQLException {
        /* 取得新資料 */
        JSONObject absent;
        JSONObject history_absent = null;
        JSONObject rewards;
        JSONObject score = null;
        JSONObject history_rewards = null;
        JSONObject punished_cancel_log;
        JSONObject clubs = null;
        JSONObject cadres = null;
        JSONObject history_score = null;
        JSONObject class_table = null;

        absent = ReadAbsent.read(login.fetchPageData(ABSENT));
        rewards = ReadRewards.read(login.fetchPageData(REWARDS));
//        score = ReadScore.read(login.fetchPageData(SCORE));
        punished_cancel_log = ReadPunishedCancelLog.read(login.fetchPageData(PUNISHED_CANCEL_LOG));


        if (all) {
//            history_absent = ReadHistoryAbsent.read(login.fetchPageData(HISTORY_ABSENT));
            history_rewards = ReadHistoryRewards.read(login.fetchPageData(HISTORY_REWARDS));
            clubs = ReadClubs.read(login.fetchPageData(CLUBS));
            cadres = ReadCadres.read(login.fetchPageData(CADRES));
            history_score = ReadHistoryScore.read(login.fetchPageData(HISTORY_SCORE));
            class_table = ReadClassTable.read(login.fetchPageData(CLASS_TABLE));

            if (history_rewards == null) {
                System.out.println(login.fetchPageData(ABSENT));
                history_rewards = new JSONObject("{\"error\": \"I'll fix this bug soon (0x04)\"}");
            }
            if (clubs == null) {
                System.out.println(login.fetchPageData(ABSENT));
                clubs = new JSONObject("{\"error\": \"I'll fix this bug soon (0x06)\"}");
            }
            if (cadres == null) {
                System.out.println(login.fetchPageData(ABSENT));
                cadres = new JSONObject("{\"error\": \"I'll fix this bug soon (0x07)\"}");
            }
            if (history_score == null) {
                System.out.println(login.fetchPageData(ABSENT));
                history_score = new JSONObject("{\"error\": \"I'll fix this bug soon (0x08)\"}");
            }
            if (class_table == null) {
                System.out.println(login.fetchPageData(ABSENT));
                class_table = new JSONObject("{\"error\": \"I'll fix this bug soon (0x09)\"}");
            }
        }


        if (absent == null) {
            System.out.println(login.fetchPageData(ABSENT));
            absent = new JSONObject("{\"error\": \"I'll fix this bug soon (0x02)\"}");
        }
        if (rewards == null) {
            System.out.println(login.fetchPageData(ABSENT));
            rewards = new JSONObject("{\"error\": \"I'll fix this bug soon (0x03)\"}");
        }
        if (punished_cancel_log == null) {
            System.out.println(login.fetchPageData(ABSENT));
            punished_cancel_log = new JSONObject("{\"error\": \"I'll fix this bug soon (0x05)\"}");
        }
        PreparedStatement update;
        /* 新增進資料庫 */
        if (all) {
            String insert = "INSERT OR REPLACE INTO 'cache' VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            update = conn_cache.prepareStatement(insert);
            update.setString(1, login.id);
            update.setString(2, absent.toString());
            update.setString(3, rewards.toString());
            update.setString(4, history_rewards.toString());
            update.setString(5, punished_cancel_log.toString());
            update.setString(6, clubs.toString());
            update.setString(7, cadres.toString());
            update.setString(8, history_score.toString());
            update.setString(9, class_table.toString());
        } else {
            String insert = "UPDATE 'cache' SET absent = ?, rewards = ?, punished_cancel_log = ? WHERE id = ?";
            update = conn_cache.prepareStatement(insert);
            update.setString(1, absent.toString());
            update.setString(2, rewards.toString());
            update.setString(3, punished_cancel_log.toString());
            update.setString(4, login.id);
        }

        return update;
    }
}

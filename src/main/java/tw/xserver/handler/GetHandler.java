package tw.xserver.handler;

import tw.xserver.manager.AuthManager;
import tw.xserver.manager.JSONResponseManager;
import tw.xserver.manager.LoginManager;
import tw.xserver.util.ErrorException;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import tw.xserver.HTML_Analyze.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static tw.xserver.Main.cache;
import static tw.xserver.util.PageKey.*;

public class GetHandler {
    private final ChannelHandlerContext ctx;
    private final FullHttpRequest request;
    private final JSONResponseManager response;

    public GetHandler(ChannelHandlerContext ctx, FullHttpRequest request) throws ErrorException, IOException {
        this.ctx = ctx;
        this.request = request;
        this.response = new JSONResponseManager(ctx);

        get();
    }

    @Nullable
    public static Map<String, String> parseCookieString(@Nullable String cookieString) {
        if (cookieString == null) return null;

        Map<String, String> cookieMap = new HashMap<>();
        String[] cookies = cookieString.split("; ");
        for (String cookie : cookies) {
            String[] parts = cookie.split("=", 2);
            if (parts.length == 2) {
                cookieMap.put(parts[0], parts[1]);
            }
        }
        return cookieMap;
    }

    private void get() throws ErrorException, IOException {
        String[] args = request.uri().split("/");
        if (!args[2].equalsIgnoreCase("get"))
            return;

        HttpHeaders headers = request.headers();
        Map<String, String> cookies = parseCookieString(headers.get(HttpHeaderNames.COOKIE));
        if (cookies == null) {
            throw new ErrorException("cannot get cookie, please POST 'id' and 'pwd' to '/ptivs/login/' for login first", HttpResponseStatus.UNAUTHORIZED);
        }

        String realIP = headers.get("CF-Connecting-IP");
        if (realIP == null)
            realIP = headers.get("Host").split(":")[0];

        /* Check Cookie */
        AuthManager authManager = new AuthManager(cookies.get("token"), realIP);
        LoginManager login = authManager.loginManager;
        String id = authManager.id;
        switch (args[3].toLowerCase()) {
            case "absent" -> {
                // 學期缺曠課 010010
//                putData(readAbsent(login.fetchPageData(ABSENT)));
                putData(cache.get(id, "absent"));
            }
            case "history_absent" -> {
                // 歷年缺曠課 010030
                putData(ReadHistoryAbsent.readHistoryAbsent(login.fetchPageData(HISTORY_ABSENT)));
            }
            case "rewards" -> {
                // 學期獎懲 010040
//                putData(readRewards(login.fetchPageData(REWARDS)));
                putData(cache.get(id, "rewards"));
            }
            case "score" -> {
                // 學期成績 010090
                putData(ReadScore.readScore(login.fetchPageData(SCORE)));
            }
            case "history_rewards" -> {
                // 歷年獎懲 010050
                putData(ReadHistoryRewards.readHistoryRewards(login.fetchPageData(HISTORY_REWARDS)));
            }
            case "punished_cancel_log" -> {
                // 銷過紀錄 010060
                putData(ReadPunishedCancelLog.readPunishedCancelLog(login.fetchPageData(PUNISHED_CANCEL_LOG)));
            }
            case "clubs" -> {
                // 參與社團 010070
                putData(ReadClubs.readClubs(login.fetchPageData(CLUBS)));
            }
            case "cadres" -> {
                // 擔任幹部 010080
                putData(ReadCadres.readCadres(login.fetchPageData(CADRES)));
            }
            case "history_score" -> {
                // 歷年成績 010110
                putData(ReadHistoryScore.readHistoryScore(login.fetchPageData(HISTORY_SCORE)));
            }
            case "class_table" -> {
                // 課表 010130
                putData(ReadClassTable.readClassTable(login.fetchPageData(CLASS_TABLE)));
            }
            default -> {
                return;
            }
        }

        if (response.json.has("data")) {
            response.json.getJSONObject("data").put("profile", authManager.profile);
        }

        ctx.writeAndFlush(response.getResponse()).addListener(ChannelFutureListener.CLOSE);
    }

    private void putData(@Nullable JSONObject data) throws ErrorException {
        if (data == null) {
            throw new ErrorException("cannot parse data");
        }

        response.json.put("data", data);
    }
}


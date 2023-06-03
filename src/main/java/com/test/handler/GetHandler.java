package com.test.handler;

import com.test.manager.AuthManager;
import com.test.manager.JSONResponseManager;
import com.test.manager.LoginManager;
import com.test.util.ErrorException;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.test.util.HTML_Analyze.*;
import static com.test.util.PageKey.*;

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

        /* Check Cookie */
        AuthManager authManager = new AuthManager(cookies.get("token"), realIP);
        LoginManager login = authManager.loginManager;
        switch (args[3].toLowerCase()) {
            case "absent": {
                // 學期缺曠課 010010
                putData(readAbsent(login.fetchPageData(ABSENT)));
                break;
            }

            case "history_absent": {
                // 歷年缺曠課 010030
                putData(readHistoryAbsent(login.fetchPageData(HISTORY_ABSENT)));
                break;
            }

            case "rewards": {
                // 學期獎懲 010040
                putData(readRewards(login.fetchPageData(REWARDS)));
                break;
            }

            case "score": {
                // 學期成績 010090
                putData(readScore(login.fetchPageData(SCORE)));
                break;
            }

            case "history_rewards": {
                // 歷年獎懲 010050
                putData(readHistoryRewards(login.fetchPageData(HISTORY_REWARDS)));
                break;
            }

            case "punished_cancel_log": {
                // 銷過紀錄 010060
                putData(readPunishedCancelLog(login.fetchPageData(PUNISHED_CANCEL_LOG)));
                break;
            }

            case "clubs": {
                // 參與社團 010070
                putData(readClubs(login.fetchPageData(CLUBS)));
                break;
            }

            case "cadres": {
                // 擔任幹部 010080
                putData(readCadres(login.fetchPageData(CADRES)));
                break;
            }

            case "history_score": {
                // 歷年成績 010110
                putData(readHistoryScore(login.fetchPageData(HISTORY_SCORE)));
                break;
            }

            case "class_table": {
                // 課表 010130
                putData(readClassTable(login.fetchPageData(CLASS_TABLE)));
                break;
            }

            default: {
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


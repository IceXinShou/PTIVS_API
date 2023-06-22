package tw.xserver.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import tw.xserver.manager.AuthManager;
import tw.xserver.manager.JSONResponseManager;
import tw.xserver.util.ErrorException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static tw.xserver.Main.cache;
import static tw.xserver.Util.getTime;

public class GetHandler {
    private final ChannelHandlerContext ctx;
    private final FullHttpRequest request;
    private final JSONResponseManager response;

    public GetHandler(ChannelHandlerContext ctx, FullHttpRequest request) throws ErrorException, IOException {
        /* 初始化 */
        this.ctx = ctx;
        this.request = request;

        /* 初始回覆管理器 */
        this.response = new JSONResponseManager(ctx);

        get();
    }

    private void get() throws ErrorException, IOException {
        String[] args = request.uri().split("/");

        /* 過濾請求 */
        if (args.length > 2 && !args[2].equalsIgnoreCase("get"))
            return;

        HttpHeaders headers = request.headers();

        /* 取得 Token */
        Map<String, String> cookies = parseCookieString(headers.get(HttpHeaderNames.COOKIE));
        if (cookies == null) {
            throw new ErrorException("cannot get cookie, please POST 'id' and 'pwd' to '/ptivs/login/' for login first", HttpResponseStatus.UNAUTHORIZED);
        }
        String token = cookies.get("token");

        String realIP = headers.get("CF-Connecting-IP");
        if (realIP == null)
            realIP = headers.get("Host").split(":")[0];

        /* 驗證 Token */
        AuthManager authManager = new AuthManager(token, realIP);

        /* 添加資料進回覆 */
        String id = authManager.id;
        switch (args[3].toLowerCase()) {
            case "absent" -> {
                // 學期缺曠課 010010
                putData(cache.get(id, "absent"));
            }
            case "history_absent" -> {
                // 歷年缺曠課 010030
                putData(cache.get(id, "history_absent"));
            }
            case "rewards" -> {
                // 學期獎懲 010040
                putData(cache.get(id, "rewards"));
            }
            case "score" -> {
                // 學期成績 010090
                putData(cache.get(id, "score"));
            }
            case "history_rewards" -> {
                // 歷年獎懲 010050
                putData(cache.get(id, "history_rewards"));
            }
            case "punished_cancel_log" -> {
                // 銷過紀錄 010060
                putData(cache.get(id, "punished_cancel_log"));
            }
            case "clubs" -> {
                // 參與社團 010070
                putData(cache.get(id, "clubs"));
            }
            case "cadres" -> {
                // 擔任幹部 010080
                putData(cache.get(id, "cadres"));
            }
            case "history_score" -> {
                // 歷年成績 010110
                putData(cache.get(id, "history_score"));
            }
            case "class_table" -> {
                // 課表 010130
                putData(cache.get(id, "class_table"));
            }
            default -> {
                return;
            }
        }

        /* 補上個人資料 */
        if (response.json.has("data")) {
            response.json.getJSONObject("data").put("profile", authManager.profile);
        }

        /* 回覆並結束連線 */
        ctx.writeAndFlush(response.getResponse()).addListener(ChannelFutureListener.CLOSE);

        System.out.println(getTime() + " GET: " + args[3].toLowerCase() + ' ' + authManager.id);
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

    private void putData(@Nullable JSONObject data) throws ErrorException {
        if (data == null) {
            throw new ErrorException("cannot parse data");
        }

        response.json.put("data", data);
    }
}


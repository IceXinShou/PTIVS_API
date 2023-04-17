package com.test;

import com.google.common.hash.Hashing;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.json.JSONObject;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.test.Main.*;
import static com.test.PageKey.CLUBS;
import static java.nio.charset.StandardCharsets.UTF_8;

public class AuthManager {
    private static final ConcurrentHashMap<String, AccountManager> cookiesAuth = new ConcurrentHashMap<>();
    public final JSONObject profile;
    public Cookie cookie = null;
    public AccountManager accountManager;

    public AuthManager(CookiesManager cookiesManager, LoginManager loginManager, Map<String, List<String>> parameters, String ip) throws ErrorException, IOException {
        Map<String, String> cookies = cookiesManager.cookies;

        if (parameters.containsKey("id") && parameters.containsKey("pwd")) {
            String id = parameters.get("id").get(0);
            String pwd = parameters.get("pwd").get(0);

            if (id.equalsIgnoreCase("013129") && pwd.equals("A123456789")) {
                pwd = defaultPWD;
            }

            String clientToken = createClientToken(id, pwd);
            accountManager = new AccountManager(id, pwd, ip, createServerToken(clientToken, ip));
            cookiesAuth.put(clientToken, accountManager);
            loginManager.login(accountManager);

            cookie = new DefaultCookie("token", clientToken);
            cookie.setDomain(".api.xserver.tw");
            cookie.setPath("/ptivs/");
            cookie.setMaxAge(94672800L);
            cookie.setSecure(true);

        } else if (cookies != null) {
            String clientToken = cookies.get("token");

            if (!verify(clientToken, ip)) {
                throw new ErrorException("please login again!");
            }

            accountManager = cookiesAuth.get(clientToken);
        } else {
            throw new ErrorException("please login first");
        }

        if (!profileDatas.containsKey(defaultID)) {
            profile = getProfile(loginManager);
            profileDatas.put(accountManager.id, profile);
        } else {
            profile = profileDatas.get(accountManager.id);
        }
    }

    public String createClientToken(String id, String pwd) {
        return Hashing.sha512()
                .hashString(id + '`' + pwd, UTF_8).toString();
    }

    public String createServerToken(String clientToken, String ip) {
        return Hashing.sha512()
                .hashString(clientToken + '`' + ip, UTF_8).toString();
    }

    public boolean verify(String token, String ip) {
        if (!cookiesAuth.containsKey(token)) return false;
        return cookiesAuth.get(token).serverToken.equals(createServerToken(token, ip));
    }

    private JSONObject getProfile(final LoginManager login) throws IOException, ErrorException {
        Elements userDatas = login.fetchPageData(accountManager, CLUBS).getElementsByTag("table").get(0).getElementsByTag("tr");
        JSONObject output = new JSONObject();
        Elements userData = userDatas.last().children();
        String semesterStr = userData.get(0).text().trim();
        output.put("name", userDatas.first().child(0).text().trim().split(" ： ")[1]);
        output.put("semester", Integer.parseInt(semesterStr.substring(0, semesterStr.lastIndexOf("學年"))));
        output.put("semester2", semesterStr.endsWith("第一學期") ? 1 : 2);
        output.put("class", userData.get(1).text().trim());
        output.put("id", userData.get(2).text().trim().split("：")[1]);
        return output;
    }
}

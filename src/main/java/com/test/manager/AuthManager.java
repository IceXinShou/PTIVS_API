package com.test.manager;

import com.google.common.hash.Hashing;
import com.test.util.ErrorException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import static com.test.Main.defaultPWD;
import static com.test.Main.profileDatas;
import static com.test.util.PageKey.CLUBS;
import static java.nio.charset.StandardCharsets.UTF_8;

public class AuthManager {
    private static final ConcurrentHashMap<String, AccountManager> cookiesAuth = new ConcurrentHashMap<>();
    public final JSONObject profile;
    public Cookie cookie = null;
    private AccountManager accountManager;
    public LoginManager loginManager;

    public AuthManager(@Nullable String token, String ip) throws ErrorException, IOException {
        if (token == null) {
            throw new ErrorException("please POST 'id' and 'pwd' to '/ptivs/login/' for login first", HttpResponseStatus.UNAUTHORIZED);
        }

        if (!verify(token, ip)) {
            throw new ErrorException("please POST 'id' and 'pwd' to '/ptivs/login/' for login again", HttpResponseStatus.UNAUTHORIZED);
        }

        loginManager = new LoginManager(accountManager.id, accountManager.pwd);
        this.profile = profileDatas.get(accountManager.id);
    }

    public AuthManager(String id, String pwd, String ip) throws ErrorException, IOException {

        if (id.equalsIgnoreCase("013129") && pwd.equals("A123456789")) {
            pwd = defaultPWD;
        }

        String clientToken = createClientToken(id, pwd);
        accountManager = new AccountManager(id, pwd, ip, createServerToken(clientToken, ip));
        loginManager = new LoginManager(id, pwd);
        cookiesAuth.put(clientToken, accountManager);
        loginManager.login();

        cookie = new DefaultCookie("token", clientToken);
        cookie.setDomain(".api.xserver.tw");
        cookie.setPath("/ptivs/");
        cookie.setMaxAge(94672800L);
        cookie.setSecure(true);

        if (!profileDatas.containsKey(id)) {
            profile = getProfile(loginManager);
            profileDatas.put(accountManager.id, profile);
        } else {
            profile = profileDatas.get(accountManager.id);
        }
    }

    private String createClientToken(String id, String pwd) {
        return Hashing.sha512()
                .hashString(id + '`' + pwd, UTF_8).toString();
    }

    private String createServerToken(String clientToken, String ip) {
        return Hashing.sha512()
                .hashString(clientToken + '`' + ip, UTF_8).toString();
    }

    private boolean verify(String token, String ip) {
        if (!cookiesAuth.containsKey(token)) return false;

        AccountManager tmp = cookiesAuth.get(token);
        if (!tmp.serverToken.equals(createServerToken(token, ip))) return false;

        accountManager = tmp;
        return true;
    }

    private JSONObject getProfile(final LoginManager login) throws IOException, ErrorException {
        Elements userDatas = login.fetchPageData(CLUBS).getElementsByTag("table").get(0).getElementsByTag("tr");
        JSONObject output = new JSONObject();
        Elements userData = userDatas.last().children();
        String semesterStr = userData.get(0).text().trim();
        output.put("姓名", userDatas.first().child(0).text().trim().split(" ： ")[1]);
        output.put("學年", Integer.parseInt(semesterStr.substring(0, semesterStr.lastIndexOf("學年"))));
        output.put("學期", semesterStr.endsWith("第一學期") ? 1 : 2);
        output.put("班級", userData.get(1).text().trim());
        output.put("學號", userData.get(2).text().trim().split("：")[1]);
        return output;
    }
}

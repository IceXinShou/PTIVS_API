package tw.xserver.manager;

import com.google.common.hash.Hashing;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.jsoup.select.Elements;
import tw.xserver.util.ErrorException;
import tw.xserver.util.PageKey;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static tw.xserver.Main.*;

public class AuthManager {
    private static final ConcurrentHashMap<String, JSONObject> profileDatas = new ConcurrentHashMap<>();
    public final JSONObject profile;
    public Cookie cookie = null;
    private final Account account;
    public LoginManager loginManager;
    public String id;

    public AuthManager(@Nullable String token, String ip) throws ErrorException, IOException {
        if (token == null) {
            throw new ErrorException("please POST 'id' and 'pwd' to '/ptivs/login/' for login first", HttpResponseStatus.UNAUTHORIZED);
        }

        account = certificate.verify(token, ip);
        if (account == null) {
            throw new ErrorException("please POST 'id' and 'pwd' to '/ptivs/login/' for login again", HttpResponseStatus.UNAUTHORIZED);
        }

        id = account.id;
        loginManager = new LoginManager(id, account.pwd);

        if (!profileDatas.containsKey(id)) {
            profile = getProfile(loginManager);
            profileDatas.put(id, profile);
        } else {
            profile = profileDatas.get(id);
        }
    }

    public AuthManager(String id, String pwd, String ip) throws ErrorException, IOException, SQLException {
        if (id.equals(defaultID) && pwd.equals("A123456789")) {
            pwd = defaultPWD;
        }

        loginManager = new LoginManager(id, pwd);
        this.id = id;
        cache.refreshCache(loginManager);
        String clientToken = createClientToken(id, pwd);
        account = new Account(id, ip, pwd, clientToken, createServerToken(clientToken, ip));
        certificate.add(account);

        cookie = new DefaultCookie("token", clientToken);
        cookie.setDomain(".api.xserver.tw");
        cookie.setPath("/ptivs/");
        cookie.setMaxAge(94672800L);
        cookie.setSecure(true);

        profile = getProfile(loginManager);
        profileDatas.put(account.id, profile);
    }

    private String createClientToken(String id, String pwd) {
        return Hashing.sha512()
                .hashString(id + '`' + pwd, UTF_8).toString();
    }

    private String createServerToken(String clientToken, String ip) {
        return Hashing.sha512()
                .hashString(clientToken + '`' + ip, UTF_8).toString();
    }

    private JSONObject getProfile(final LoginManager login) throws IOException, ErrorException {
        Elements userDatas = login.fetchPageData(PageKey.CLUBS)
                .getElementsByTag("table").get(0).getElementsByTag("tr");
        JSONObject output = new JSONObject();
        Elements userData = userDatas.get(1).children();
        String semesterStr = userData.get(0).text().trim();
        output.put("姓名", userDatas.get(0).child(0).text().trim().split(" ： ")[1]);
        output.put("學年", Integer.parseInt(semesterStr.substring(0, semesterStr.lastIndexOf("學年"))));
        output.put("學期", semesterStr.endsWith("第一學期") ? 1 : 2);
        output.put("班級", userData.get(1).text().trim());
        output.put("學號", userData.get(2).text().trim().split("：")[1]);
        return output;
    }
}

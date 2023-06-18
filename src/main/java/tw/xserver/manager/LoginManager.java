package tw.xserver.manager;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import tw.xserver.util.ErrorException;
import tw.xserver.util.PageKey;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginManager {
    private static final ConcurrentHashMap<String, Map<String, String>> cookieKeeper = new ConcurrentHashMap<>(); // id, cookies
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36 Edg/114.0.1823.37";

    public final String id;
    private final String pwd;

    public LoginManager(String id, String pwd) throws IOException, ErrorException {
        this.id = id;
        this.pwd = pwd;

        login();
    }

    public void login() throws ErrorException, IOException {
        Connection.Response response = Jsoup.connect("https://sctnank.ptivs.tn.edu.tw/skyweb/main.asp")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .method(Connection.Method.POST)
                .userAgent(USER_AGENT)
                .data("txtid", id)
                .data("txtpwd", pwd)
                .data("check", "confirm")
                .execute();

        if (response.url().getPath().equals("/skyweb/main.asp")) {
            throw new ErrorException("cannot login by the provided 'id' and 'pwd'");
        }

        Jsoup.connect("https://sctnank.ptivs.tn.edu.tw/skyweb/f_head.asp")
                .method(Connection.Method.GET)
                .userAgent(USER_AGENT)
                .cookies(response.cookies())
                .execute();

        Jsoup.connect("https://sctnank.ptivs.tn.edu.tw/skyweb/f_left.asp")
                .method(Connection.Method.GET)
                .userAgent(USER_AGENT)
                .cookies(response.cookies())
                .execute();

        cookieKeeper.put(id, response.cookies());
    }

    public Document fetchPageData(PageKey pageKey) throws IOException, ErrorException {
        return fetchPageData(pageKey, false);
    }

    public Document fetchPageData(PageKey pageKey, boolean reFetch) throws IOException, ErrorException {
        Connection.Response response = Jsoup.connect("https://sctnank.ptivs.tn.edu.tw/skyweb/fnc.asp")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .method(Connection.Method.POST)
                .userAgent(USER_AGENT)
                .data("fncid", pageKey.id())
                .data("std_id", "")
                .data("local_ip", "")
                .data("contant", "")
                .cookies(cookieKeeper.get(id))
                .ignoreHttpErrors(true)
                .execute();

        // cookie expired
        if (response.url().getPath().equals("/skyweb/fnc.asp")) {
            if (reFetch)
                throw new ErrorException("wtf error (0x01)");

            fetchPageData(pageKey, true);
        }

        return response.parse();
    }
}
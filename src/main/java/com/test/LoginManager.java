package com.test;

import org.jetbrains.annotations.Nullable;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginManager {
    private static final ConcurrentHashMap<String, Map<String, String>> cookieKeeper = new ConcurrentHashMap<>(); // id, cookies
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36 Edg/112.0.1722.48";


    public void login(final AccountManager manager) throws ErrorException, IOException {
        Connection.Response response = Jsoup.connect("https://sctnank.ptivs.tn.edu.tw/skyweb/main.asp")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .method(Connection.Method.POST)
                .userAgent(USER_AGENT)
                .data("txtid", manager.id)
                .data("txtpwd", manager.pwd)
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

        cookieKeeper.put(manager.id, response.cookies());
    }

    public Document fetchPageData(final AccountManager manager, final PageKey pageKey) throws IOException, ErrorException {
        return fetchPageData(manager, pageKey, false);
    }

    public Document fetchPageData(final AccountManager manager, final PageKey pageKey, final boolean reFetch) throws IOException, ErrorException {
        Connection.Response response = Jsoup.connect("https://sctnank.ptivs.tn.edu.tw/skyweb/fnc.asp")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .method(Connection.Method.POST)
                .userAgent(USER_AGENT)
                .data("fncid", pageKey.id())
                .data("std_id", "")
                .data("local_ip", "")
                .data("contant", "")
                .cookies(cookieKeeper.get(manager.id))
                .execute();

        // cookie expired
        if (response.url().getPath().equals("/skyweb/fnc.asp")) {
            if (reFetch)
                throw new ErrorException("wtf error (0x01)");

            fetchPageData(manager, pageKey, true);
        }

        return response.parse();
    }

    private void getUrl(final String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestMethod("GET");
            conn.getContent();
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    private String streamToString(final InputStream in) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int length;
            while ((length = in.read(buff)) > 0)
                out.write(buff, 0, length);

            in.close();
            return out.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[UrlDataGetter] " + e.fillInStackTrace().getMessage());
            return null;
        }
    }
}
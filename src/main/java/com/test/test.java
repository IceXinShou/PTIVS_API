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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class test {
    private static final ConcurrentHashMap<String, String> cookieKeeper = new ConcurrentHashMap<>(); // id, cookie
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36";
    private static final String ERROR_RESPONSE = "<script language='javascript'>top.location.href='error.htm'</script>";

    public static void main(String[] args) {
        test t = new test();
        t.login("013129", args[0]);
    }

    public void login(final String id, final String password) {
        try {
            Connection.Response response = Jsoup.connect("https://sctnank.ptivs.tn.edu.tw/skyweb/main.asp")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .method(Connection.Method.POST)
                    .userAgent(USER_AGENT)
                    .data("txtid", id)
                    .data("txtpwd", URLEncoder.encode(password, StandardCharsets.UTF_8))
                    .data("check", "confirm")
                    .execute();

            String cookie = response.cookie("ASPSESSIONIDAWAATABD");
            if (cookie != null) {
                cookieKeeper.put(id, cookie);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        fetchPageData(id, PageKey.CLUBS);
    }

    public Document fetchPageData(final String id, final PageKey pageKey) {
        try {
            Document doc = Jsoup.connect("https://sctnank.ptivs.tn.edu.tw/skyweb/fnc.asp")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .method(Connection.Method.POST)
                    .userAgent(USER_AGENT)
                    .data("fncid", pageKey.id())
                    .data("std_id", "")
                    .data("local_ip", "")
                    .data("contant", "")
                    .cookie("ASPSESSIONIDAWAATABD", cookieKeeper.get(id))
                    .cookie("dataskywebfortestcookies", "HAHA%21")
                    .get();

            System.out.println(doc);

            return doc;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
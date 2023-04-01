package com.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class LoginManager {
    private final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36";

    public boolean onLogin(API_Response api, String id, String password) {
        try {
            CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(cookieManager);
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

            //get Cookie
            HttpURLConnection conn = (HttpURLConnection) new URL("https://sctnank.ptivs.tn.edu.tw/skyweb/main.asp").openConnection();
            conn.setRequestProperty("User-Agent", userAgent);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(true);
            conn.getOutputStream().write(("txtid=" + id +
                    "&txtpwd=" + URLEncoder.encode(password, StandardCharsets.UTF_8) +
                    "&check=confirm"
            ).getBytes(StandardCharsets.UTF_8));
            conn.getContent();

            if (conn.getURL().getPath().equals("/skyweb/main.asp")) {
                // not success
                api.errors.add("cannot login by the name and password");
                conn.disconnect();
                return false;
            }

            conn.disconnect();

            // Required
            getUrl("https://sctnank.ptivs.tn.edu.tw/skyweb/f_head.asp");
            getUrl("https://sctnank.ptivs.tn.edu.tw/skyweb/f_left.asp");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String fetchPageData(final String pageId) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("https://sctnank.ptivs.tn.edu.tw/skyweb/fnc.asp").openConnection();

            conn.setRequestProperty("User-Agent", userAgent);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(true);
            conn.getOutputStream().write(("fncid=" + pageId + "&std_id=&local_ip=&contant=").getBytes(StandardCharsets.UTF_8));

            String returnString = readResponse(conn.getInputStream());
            conn.disconnect();

            return returnString;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void getUrl(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", userAgent);
            conn.setRequestMethod("GET");
            conn.getContent();
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readResponse(InputStream in) {
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
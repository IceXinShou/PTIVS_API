package com.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class PtivsTn {
    private final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36";

    PtivsTn() {
        postData();
    }

    public void postData() {
        try {
            // Post Data
            CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(cookieManager);
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

            // Get Cookie
//            getUrl("https://sctnank.ptivs.tn.edu.tw/skyweb/main.asp");
//            getUrl("https://sctnank.ptivs.tn.edu.tw/skyweb/first.asp");

            // Login
            String userId = "013129";
            String userPassword = "kevin3308P@ssw0rd";
            HttpURLConnection conn = (HttpURLConnection) new URL("https://sctnank.ptivs.tn.edu.tw/skyweb/main.asp").openConnection();
            conn.setRequestProperty("User-Agent", userAgent);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(true);
            conn.getOutputStream().write(("txtid=" + userId +
                    "&txtpwd=" + URLEncoder.encode(userPassword, "UTF-8") +
                    "&check=confirm"
            ).getBytes(StandardCharsets.UTF_8));
            conn.getContent();
            System.out.println(conn.getURL());
            conn.disconnect();

            // Required
            getUrl("https://sctnank.ptivs.tn.edu.tw/skyweb/f_head.asp");
            getUrl("https://sctnank.ptivs.tn.edu.tw/skyweb/f_left.asp");
//            getUrl("https://sctnank.ptivs.tn.edu.tw/skyweb/f_right.asp");

            // Get Page
            String pageId = "010020";
            conn = (HttpURLConnection) new URL("https://sctnank.ptivs.tn.edu.tw/skyweb/fnc.asp").openConnection();
            conn.setRequestProperty("User-Agent", userAgent);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(true);
            conn.getOutputStream().write(("fncid=" + pageId + "&std_id=&local_ip=&contant=").getBytes(StandardCharsets.UTF_8));
            System.out.println(readResponse(conn.getInputStream()));
            System.out.println(conn.getURL());
            conn.disconnect();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getUrl(String url) {
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

    private String readResponse(InputStream in) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int length;
            while ((length = in.read(buff)) > 0)
                out.write(buff, 0, length);

            in.close();
            return out.toString("UTF-8");
        } catch (IOException e) {
            System.err.println("[UrlDataGetter] " + e.fillInStackTrace().getMessage());
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        new PtivsTn();
    }
}
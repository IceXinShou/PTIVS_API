package com.test;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CookiesManager {
    public Map<String, String> cookies = null;

    public CookiesManager(@Nullable String cookieString) {
        if (cookieString != null)
            cookies = parseCookieString(cookieString);
    }

    private Map<String, String> parseCookieString(String cookieString) {
        Map<String, String> cookieMap = new HashMap<>();
        String[] cookies = cookieString.split("; ");
        for (final String cookie : cookies) {
            String[] parts = cookie.split("=", 2);
            if (parts.length == 2) {
                cookieMap.put(parts[0], parts[1]);
            }
        }
        return cookieMap;
    }
}
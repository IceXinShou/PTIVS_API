package com.test.manager;

public class AccountManager {
    final String id;
    final String pwd;
    final String ip;
    final String serverToken;

    public AccountManager(String id, String pwd, String ip, String serverToken) {
        this.id = id;
        this.pwd = pwd;
        this.ip = ip;
        this.serverToken = serverToken;
    }
}

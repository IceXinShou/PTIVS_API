package com.test.manager;

public class AccountManager {
    final String id;
    final String ip;
    final String pwd;
    final String clientToken;
    final String serverToken;

    public AccountManager(String id, String ip, String pwd, String clientToken, String serverToken) {
        this.id = id;
        this.ip = ip;
        this.pwd = pwd;
        this.clientToken = clientToken;
        this.serverToken = serverToken;
    }
}

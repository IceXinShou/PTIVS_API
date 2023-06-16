package tw.xserver.manager;

public class Account {
    final String id;
    final String ip;
    final String pwd;
    final String clientToken;
    final String serverToken;

    public Account(String id, String ip, String pwd, String clientToken, String serverToken) {
        this.id = id;
        this.ip = ip;
        this.pwd = pwd;
        this.clientToken = clientToken;
        this.serverToken = serverToken;
    }
}

package net.bosselaar.seprinter.core.streamelements.messages;

public class AuthenticateMsg {
    private final String method;
    private final String token;

    public AuthenticateMsg(String method, String token) {
        this.method = method;
        this.token = token;
    }

    public String getMethod() {
        return method;
    }

    public String getToken() {
        return token;
    }
}

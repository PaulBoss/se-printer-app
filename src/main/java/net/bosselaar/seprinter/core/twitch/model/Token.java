package net.bosselaar.seprinter.core.twitch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Token {
    @JsonProperty("access_token")
    public String accessToken;
    @JsonProperty("refresh_token")
    public String refreshToken;
    @JsonProperty("expires_in")
    public long expiresIn;
    public String scope;
    @JsonProperty("token_type")
    public String tokenType;
}

package net.bosselaar.seprinter.core.twitch;

import net.bosselaar.seprinter.core.twitch.model.Token;
import net.bosselaar.seprinter.core.twitch.model.UserData;
import org.glassfish.jersey.logging.LoggingFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TwitchApi {

    private final String clientId;
    private final String clientSecret;
    private final Client client;

    public TwitchApi(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        this.client = ClientBuilder.newClient().register(new LoggingFeature(Logger.getLogger(TwitchApi.class.getName()), Level.INFO, null, null));

    }

    public Token getToken() {
        return client.target("https://id.twitch.tv/oauth2/token")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("grant_type", "client_credentials")
                .request()
                .post(null, Token.class);
    }

    public Optional<String> getUserAvatarUrl(String userName) {
        Token token = getToken();

        UserData userData = client.target("https://api.twitch.tv/helix/users")
                .queryParam("login", userName)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + token.accessToken)
                .header("Client-Id", clientId)
                .get(UserData.class);

        if (!userData.users.isEmpty()) {
            return Optional.of(userData.users.get(0).profileImageUrl);
        }
        return Optional.empty();
    }
}

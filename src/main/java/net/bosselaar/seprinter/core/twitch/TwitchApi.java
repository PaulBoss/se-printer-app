package net.bosselaar.seprinter.core.twitch;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.bosselaar.seprinter.core.twitch.model.Token;
import net.bosselaar.seprinter.core.twitch.model.UserData;
import org.glassfish.jersey.logging.LoggingFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TwitchApi {

    private final String clientId;
    private final String clientSecret;
    private final Client client;

    private Token token;

    LoadingCache<String, String> userAvatarUrls = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofDays(1))
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) {
                    return getUserAvatarUrlInternal(s);
                }
            });


    public TwitchApi(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        this.client = ClientBuilder.newClient().register(new LoggingFeature(Logger.getLogger(TwitchApi.class.getName()), Level.INFO, null, null));

        this.token = getToken();
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
        try {
            String url = userAvatarUrls.get(userName);
            if (url != null) {
                return Optional.of(url);
            }
        } catch (Exception e) {
            // Just ignore and return nothing
        }
        return Optional.empty();
    }

    private String getUserAvatarUrlInternal(String userName) {
        Invocation invocation= client.target("https://api.twitch.tv/helix/users")
                .queryParam("login", userName)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + token.accessToken)
                .header("Client-Id", clientId).buildGet();

        UserData userData = doTwitchApiCall(invocation).readEntity(UserData.class);

        if (!userData.users.isEmpty()) {
              return userData.users.get(0).profileImageUrl;
        }

        return null;
    }

    private Response doTwitchApiCall(Invocation invocation) {
        Response response = invocation.invoke();

        if (response.getStatus() == 401) {
            this.token = getToken();
            return invocation.invoke();
        } else {
            return response;
        }
    }

}

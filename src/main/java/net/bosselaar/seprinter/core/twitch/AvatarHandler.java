package net.bosselaar.seprinter.core.twitch;

import net.bosselaar.seprinter.core.twitch.model.User;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

public class AvatarHandler {

    private final String clientId;
    private final String clientSecret;
    private final Client client;


    public AvatarHandler(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = "Bearer " + clientSecret;

        this.client = ClientBuilder.newClient();

    }

    public Optional<String> getUserAvatarUrl(String userName) {
        List<User> users = client.target("https://api.twitch.tv/helix/users")
                .queryParam("login", userName)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", clientSecret)
                .header("Client-Id", clientId)
                .get(new GenericType<List<User>>(){});

        if (!users.isEmpty()) {
            return Optional.of(users.get(0).profileImageUrl);
        }
        return Optional.empty();
    }
}

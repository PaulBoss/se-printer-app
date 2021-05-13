package net.bosselaar.seprinter.core.managed;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.lifecycle.Managed;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import net.bosselaar.seprinter.config.StreamElementsConfig;
import net.bosselaar.seprinter.core.streamelements.messages.AuthenticateMsg;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class SESocketConnection implements Managed {
    private static final Logger LOGGER = LoggerFactory.getLogger(SESocketConnection.class);

    private final StreamElementsConfig config;
    private Socket socket;
    private final ObjectMapper mapper = new ObjectMapper();


    public SESocketConnection(StreamElementsConfig config) {
        this.config = config;
    }

    @Override
    public void start() {
        LOGGER.info("Opening connection");


        IO.Options options = new IO.Options();
        options.transports = new String[]{"websocket"};
        options.secure = true;

        this.socket = IO.socket(URI.create(config.socketUri), options);

        socket.on(Socket.EVENT_CONNECT, (Object... args) -> {
                LOGGER.info("Connected to SE websocket");
                 socket.emit("authenticate", new JSONObject(new AuthenticateMsg("jwt", config.jwtToken)));
        });

        socket.on("authenticated", (Object... objects) -> LOGGER.info("Successfully authenticated."));

        socket.on(Socket.EVENT_CONNECT_ERROR, (Object... objects) -> LOGGER.info("ERROR: " + objects[0]));
        socket.on(Socket.EVENT_ERROR, (Object... objects) -> LOGGER.info("ERROR: " + objects[0]));
        socket.on(Socket.EVENT_MESSAGE, (Object... objects) -> LOGGER.info("MESSAGE: " + objects[0]));

        socket.on("event", (Object... args) -> LOGGER.info("EVENT: " + args[0]));


        socket.open();
    }

    @Override
    public void stop() {
        socket.close();
    }
}

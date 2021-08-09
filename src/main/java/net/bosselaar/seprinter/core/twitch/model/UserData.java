package net.bosselaar.seprinter.core.twitch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UserData {
    @JsonProperty("data")
    public List<User> users;
}

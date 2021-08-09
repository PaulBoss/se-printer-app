package net.bosselaar.seprinter.core.twitch.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jdk.nashorn.internal.ir.annotations.Ignore;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    public String id;
    public String login;
    @JsonProperty("display_name")
    public String displayName;
    public String type;
    @JsonProperty("broadcaster_type")
    public String broadcasterType;
    public String description;
    @JsonProperty("profile_image_url")
    public String profileImageUrl;
    @JsonProperty("offline_image_url")
    public String offineImageUrl;
    @JsonProperty("view_count")
    public long viewCount;
    public String email;
    @JsonProperty("created_at")
    public Date createdAt;
}

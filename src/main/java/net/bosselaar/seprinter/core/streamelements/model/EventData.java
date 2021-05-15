package net.bosselaar.seprinter.core.streamelements.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventData {
    public String tipId;
    public String username;
    public String providerId;

    public String displayName;
    public int amount = 0;
    public int streak = 0;

    // Should preferably be an enum...
    public String tier;
    public String currency;
    public String message;
    public int quantity = 0;

    public List<Object> items;
    public String avatar;
}

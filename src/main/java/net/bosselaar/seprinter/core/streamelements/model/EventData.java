package net.bosselaar.seprinter.core.streamelements.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventData {
    public String tipId;
    public String username;
    public String providerId;

    public String displayName;

    public String sender;
    public boolean gifted = false;

    @JsonFormat(shape= JsonFormat.Shape.STRING)
    public BigDecimal amount;
    public int streak = 0;

    // Should preferably be an enum...
    public String tier;
    public String currency;
    public String message;
    public int quantity = 0;

    public List<Object> items;
    public String avatar;
}

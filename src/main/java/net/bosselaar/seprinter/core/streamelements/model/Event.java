package net.bosselaar.seprinter.core.streamelements.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Event {
    public String _id;
    public String channel;

    public EventType type;
    public EventProvider provider;
    public boolean flagged;
    public Date createdAt;
    public Date updatedAt;

    public EventData data;
}

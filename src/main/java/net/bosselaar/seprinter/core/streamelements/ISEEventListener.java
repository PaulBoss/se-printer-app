package net.bosselaar.seprinter.core.streamelements;

import net.bosselaar.seprinter.core.streamelements.model.Event;

public interface ISEEventListener {
    void handleEvent(Event event);
}

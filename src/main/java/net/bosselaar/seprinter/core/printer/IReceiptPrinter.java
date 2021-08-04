package net.bosselaar.seprinter.core.printer;

import net.bosselaar.seprinter.core.streamelements.model.Event;

public interface IReceiptPrinter {
    void start();
    void stop();
    void addJob(Event e);
}

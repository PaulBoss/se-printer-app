package net.bosselaar.seprinter.core.printer;

import net.bosselaar.seprinter.core.streamelements.ISEEventListener;
import net.bosselaar.seprinter.core.streamelements.model.Event;
import net.bosselaar.seprinter.core.streamelements.model.EventType;

public class PrinterListener implements ISEEventListener {

    private final Printer printer;

    public PrinterListener(Printer printer) {
        this.printer = printer;
    }

    @Override
    public void handleEvent(Event event) {
        if (event.type == EventType.tip)
            printer.addJob(event);
    }
}

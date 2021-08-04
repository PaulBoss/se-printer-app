package net.bosselaar.seprinter.core.printer;

import net.bosselaar.seprinter.core.streamelements.ISEEventListener;
import net.bosselaar.seprinter.core.streamelements.model.Event;
import net.bosselaar.seprinter.core.streamelements.model.EventType;

public class PrinterListener implements ISEEventListener {

    private final IReceiptPrinter printer;

    public PrinterListener(IReceiptPrinter printer) {
        this.printer = printer;
    }

    @Override
    public void handleEvent(Event event) {
        if (event.type == EventType.tip || event.type == EventType.cheer)
            printer.addJob(event);
    }
}

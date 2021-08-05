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
        // Do not print if less then 100 bits is cheered
        if (event.type == EventType.cheer && event.data.amount.longValue() < 100) {
            return;
        }

        // Only send supported items to the printer
        switch (event.type) {
            case tip:
            case cheer:
            case raid:
            case subscriber:
                printer.addJob(event);
                break;
        }
    }
}

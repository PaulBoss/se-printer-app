package net.bosselaar.seprinter.core.printer;

import net.bosselaar.seprinter.core.streamelements.ISEEventListener;
import net.bosselaar.seprinter.core.streamelements.model.Event;
import net.bosselaar.seprinter.core.streamelements.model.EventType;
import net.bosselaar.seprinter.core.twitch.TwitchApi;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

public class PrinterListener implements ISEEventListener {

    private final IReceiptPrinter printer;
    private final TwitchApi twitchApi;

    public PrinterListener(IReceiptPrinter printer, TwitchApi twitchApi) {
        this.printer = printer;
        this.twitchApi = twitchApi;
    }

    @Override
    public void handleEvent(Event event) {
        // Do not print if less then 100 bits is cheered
        if (event.type == EventType.cheer && event.data.amount.longValue() < 50) {
            return;
        }

        // Only send supported items to the printer
        switch (event.type) {
            case tip:
            case cheer:
            case raid:
            case subscriber:
                replaceDefaultAvatarUrl(event);
                printer.addJob(event);
                break;
        }
    }

    private void replaceDefaultAvatarUrl(Event event) {
        Optional<String> result = twitchApi.getUserAvatarUrl(event.data.username.toLowerCase(Locale.ROOT));
        result.ifPresent(e -> event.data.avatar = e);
    }
}

package net.bosselaar.seprinter.core.printer;

import net.bosselaar.seprinter.config.EventsConfig;
import net.bosselaar.seprinter.core.streamelements.ISEEventListener;
import net.bosselaar.seprinter.core.streamelements.model.Event;
import net.bosselaar.seprinter.core.twitch.TwitchApi;

import java.util.Locale;
import java.util.Optional;

public class PrinterListener implements ISEEventListener {

    private final EventsConfig config;
    private final IReceiptPrinter printer;
    private final TwitchApi twitchApi;

    public PrinterListener(EventsConfig config, IReceiptPrinter printer, TwitchApi twitchApi) {
        this.config = config;
        this.printer = printer;
        this.twitchApi = twitchApi;
    }

    @Override
    public void handleEvent(Event event) {
        switch (event.type) {
            case tip:
                if (!config.tips)
                    return;
                doPrint(event);
                break;
            case cheer:
                if (!config.cheers || event.data.amount.longValue() < config.cheerMinimum)
                    return;
                doPrint(event);
                break;
            case raid:
                if (!config.raids)
                    return;
                doPrint(event);
                break;
            case subscriber:
                if (!config.subscribers)
                    return;
                doPrint(event);
                break;
            case follow:
                if (!config.followers)
                    return;
                doPrint(event);
                break;
        }
    }

    private void doPrint(Event event) {
        replaceDefaultAvatarUrl(event);
        printer.addJob(event);
    }


    private void replaceDefaultAvatarUrl(Event event) {
        Optional<String> result = twitchApi.getUserAvatarUrl(event.data.username.toLowerCase(Locale.ROOT));
        result.ifPresent(e -> event.data.avatar = e);
    }
}

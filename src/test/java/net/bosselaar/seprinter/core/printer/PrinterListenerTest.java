package net.bosselaar.seprinter.core.printer;

import net.bosselaar.seprinter.config.EventsConfig;
import net.bosselaar.seprinter.core.streamelements.model.Event;
import net.bosselaar.seprinter.core.streamelements.model.EventData;
import net.bosselaar.seprinter.core.streamelements.model.EventType;
import net.bosselaar.seprinter.core.twitch.TwitchApi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PrinterListenerTest {

    @Mock
    private IReceiptPrinter receiptPrinter;
    @Mock
    private TwitchApi twitchApi;

    private Event event;
    private EventsConfig config;

    private PrinterListener pl;

    @Test
    public void shouldPrintCheer() {
        config.cheers = true;
        config.cheerMinimum = 1;

        pl.handleEvent(event);

        verify(receiptPrinter, times(1)).addJob(event);
    }

    @Test
    public void shouldNotPrintCheer() {
        event.type = EventType.cheer;
        config.cheers = false;
        pl.handleEvent(event);
        verify(receiptPrinter, times(0)).addJob(event);
    }

    @Test
    public void shouldNotPrintCheerWithLessThenConfig() {
        event.type = EventType.cheer;
        config.cheers = true;
        config.cheerMinimum = 100;

        pl.handleEvent(event);

        verify(receiptPrinter, times(0)).addJob(event);
    }

    @Test
    public void shouldPrintTip() {
        event.type = EventType.tip;
        config.tips = true;
        pl.handleEvent(event);
        verify(receiptPrinter, times(1)).addJob(event);
    }

    @Test
    public void shouldNotPrintTip() {
        event.type = EventType.tip;
        config.tips = false;
        pl.handleEvent(event);
        verify(receiptPrinter, times(0)).addJob(event);
    }

    @Test
    public void shouldPrintSub() {
        event.type = EventType.subscriber;
        config.subscribers = true;
        pl.handleEvent(event);
        verify(receiptPrinter, times(1)).addJob(event);
    }

    @Test
    public void shouldNotPrintSub() {
        event.type = EventType.subscriber;
        config.subscribers = false;
        pl.handleEvent(event);
        verify(receiptPrinter, times(0)).addJob(event);
    }

    @Test
    public void shouldPrintFollow() {
        event.type = EventType.follow;
        config.followers = true;
        pl.handleEvent(event);
        verify(receiptPrinter, times(1)).addJob(event);
    }

    @Test
    public void shouldNotPrintFollow() {
        event.type = EventType.follow;
        config.followers = false;
        pl.handleEvent(event);
        verify(receiptPrinter, times(0)).addJob(event);
    }

    @Test
    public void shouldPrintRaid() {
        event.type = EventType.raid;
        config.raids = true;
        pl.handleEvent(event);
        verify(receiptPrinter, times(1)).addJob(event);
    }

    @Test
    public void shouldNotPrintRaid() {
        event.type = EventType.raid;
        config.raids = false;
        pl.handleEvent(event);
        verify(receiptPrinter, times(0)).addJob(event);
    }

    @Test
    public void shouldNotPrintHost() {
        event.type = EventType.host;
        config.tips = false;
        pl.handleEvent(event);
        verify(receiptPrinter, times(0)).addJob(event);
    }

    @Before
    public void init() {
        event = new Event();
        event.type = EventType.cheer;
        event.data = new EventData();
        event.data.amount = new BigDecimal(1);
        event.data.username = "anothertestuser";

        config = new EventsConfig();
        config.cheers = true;
        config.cheerMinimum = 0;

        pl = new PrinterListener(config, receiptPrinter, twitchApi);
    }
}
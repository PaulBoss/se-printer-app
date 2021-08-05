package net.bosselaar.seprinter.core.streamelements.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bosselaar.seprinter.config.PrinterConfig;
import net.bosselaar.seprinter.core.printer.DefaultPrinterPrinter;
import net.bosselaar.seprinter.core.printer.IReceiptPrinter;
import net.bosselaar.seprinter.core.printer.PrinterListener;
import net.bosselaar.seprinter.core.streamelements.ISEEventListener;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

public class EventTest {

    private final static ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void canDeserializeTipTest() throws JsonProcessingException {
        Event event = MAPPER.readValue(fixture("fixtures/se-tip-event.json"), Event.class);

        assertThat(event.type).isEqualTo(EventType.tip);
        assertThat(event.data.amount).isEqualTo(BigDecimal.valueOf(1));
        assertThat(event.data.currency).isEqualTo("EUR");
        assertThat(event.data.username).isEqualTo("creepy113");
    }

    @Test
    public void canDeserializeGiftedSubTest() throws JsonProcessingException {
        Event event = MAPPER.readValue(fixture("fixtures/se-gifted-sub.json"), Event.class);

        assertThat(event.type).isEqualTo(EventType.subscriber);
        assertThat(event.data.amount).isEqualTo(BigDecimal.valueOf(2));
        assertThat(event.data.username).isEqualTo("vampiermsx");
        assertThat(event.data.sender).isEqualTo("Mortumxl");
        assertThat(event.data.gifted).isEqualTo(true);
    }

    @Test
    public void canDeserializeNonIntAmountTest() throws JsonProcessingException {
        Event event = MAPPER.readValue(fixture("fixtures/se-tip-nonint-event.json"), Event.class);

        assertThat(event.type).isEqualTo(EventType.tip);
        assertThat(event.data.amount).isEqualTo(new BigDecimal("1.33"));
        assertThat(event.data.currency).isEqualTo("EUR");
        assertThat(event.data.username).isEqualTo("creepy113");
    }

    @Test
    public void canDeserializeCheerTest() throws JsonProcessingException {
        Event event = MAPPER.readValue(fixture("fixtures/se-bits-event.json"), Event.class);
        assertThat(event.type).isEqualTo(EventType.cheer);
        assertThat(event.createdAt).isEqualTo("2021-05-16T19:47:56.388Z");
        assertThat(event.provider).isEqualTo(EventProvider.twitch);

        assertThat(event.data.amount).isEqualTo(new BigDecimal("100"));
        assertThat(event.data.currency).isNull();
        assertThat(event.data.username).isEqualTo("Anonymous");
        assertThat(event.data.displayName).isEqualTo("AnAnonymousCheerer");
        assertThat(event.data.message).isEqualTo("Anon100");
    }



    // Shortcut to quicktest the actual printing.
    public static void main(String[] args) throws IOException {
        PrinterConfig printerConfig = new PrinterConfig();
        printerConfig.rotate = true;

        IReceiptPrinter printer = new DefaultPrinterPrinter(printerConfig);

        printer.start();

        Event event = MAPPER.readValue(fixture("fixtures/se-gifted-sub.json"), Event.class);
        printer.addJob(event);

        printer.stop();
    }


}
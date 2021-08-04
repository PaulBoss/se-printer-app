package net.bosselaar.seprinter.core.streamelements.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bosselaar.seprinter.core.printer.Printer;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

public class EventTest {

    private final static ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void canDeserializeTest() throws JsonProcessingException {
        Event event = MAPPER.readValue(fixture("fixtures/se-tip-event.json"), Event.class);

        assertThat(event.type).isEqualTo(EventType.tip);
        assertThat(event.data.amount).isEqualTo(BigDecimal.valueOf(1));
        assertThat(event.data.currency).isEqualTo("EUR");
        assertThat(event.data.username).isEqualTo("creepy113");
    }

    @Test
    public void canDeserializeNonIntAmountTest() throws JsonProcessingException {
        Event event = MAPPER.readValue(fixture("fixtures/se-tip-nonint-event.json"), Event.class);

        assertThat(event.type).isEqualTo(EventType.tip);
        assertThat(event.data.amount).isEqualTo(new BigDecimal("1.33"));
        assertThat(event.data.currency).isEqualTo("EUR");
        assertThat(event.data.username).isEqualTo("creepy113");
    }


    // Shortcut to quicktest the actual printing.
    public static void main(String[] args) throws IOException {
        Printer printer = new Printer();
        printer.start();

        Event event = MAPPER.readValue(fixture("fixtures/se-tip-nonint-event.json"), Event.class);
        printer.addJob(event);
    }
}
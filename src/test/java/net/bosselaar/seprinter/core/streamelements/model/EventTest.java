package net.bosselaar.seprinter.core.streamelements.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bosselaar.seprinter.core.printer.Printer;
import org.junit.Test;

import javax.print.PrintException;
import java.io.IOException;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

public class EventTest {

    private final static ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void canDeserializeTest() throws JsonProcessingException {
        Event event = MAPPER.readValue(fixture("fixtures/se-tip-event.json"), Event.class);

        assertThat(event.type).isEqualTo(EventType.tip);
        assertThat(event.data.amount).isEqualTo(1);
        assertThat(event.data.currency).isEqualTo("EUR");
        assertThat(event.data.username).isEqualTo("creepy113");
    }

    // Shortcut to quicktest the actual printing.
    public static void main(String[] args) throws IOException {
        Printer printer = new Printer();
        printer.start();

        Event event = MAPPER.readValue(fixture("fixtures/se-tip-event.json"), Event.class);
        printer.addJob(event);
    }
}
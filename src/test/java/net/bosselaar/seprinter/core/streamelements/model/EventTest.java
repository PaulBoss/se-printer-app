package net.bosselaar.seprinter.core.streamelements.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

import static io.dropwizard.testing.FixtureHelpers.fixture;

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
}
package net.bosselaar.seprinter.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import net.bosselaar.seprinter.SEPrinterAppConfiguration;
import net.bosselaar.seprinter.core.printer.DefaultPrinterPrinter;
import net.bosselaar.seprinter.core.printer.IReceiptPrinter;
import net.bosselaar.seprinter.core.printer.PrinterListener;
import net.bosselaar.seprinter.core.streamelements.ISEEventListener;
import net.bosselaar.seprinter.core.streamelements.model.Event;
import net.bosselaar.seprinter.core.twitch.TwitchApi;

import javax.validation.Validator;
import java.io.File;

import static io.dropwizard.testing.FixtureHelpers.fixture;

public class EventPrintTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private static final Validator validator = Validators.newValidator();
    private static final YamlConfigurationFactory<SEPrinterAppConfiguration> configFactory =
            new YamlConfigurationFactory<>(SEPrinterAppConfiguration.class, validator, objectMapper, "dw");


     // Shortcut to quick test the actual printing.
    public static void main(String[] args) throws Exception {
        final File yml = new File("dev.yml");
        SEPrinterAppConfiguration config = configFactory.build(yml);

        TwitchApi twitchApi = new TwitchApi(config.twitch.clientId, config.twitch.clientSecret);

        IReceiptPrinter printer = new DefaultPrinterPrinter(config.printer);

        printer.start();

        ISEEventListener listener = new PrinterListener(printer, twitchApi);

        Event event = MAPPER.readValue(fixture("fixtures/se-tip-event.json"), Event.class);

        listener.handleEvent(event);

        printer.stop();
    }
}

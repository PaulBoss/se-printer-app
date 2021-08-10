package net.bosselaar.seprinter;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import net.bosselaar.seprinter.config.PrinterConfig;
import net.bosselaar.seprinter.config.StreamElementsConfig;
import net.bosselaar.seprinter.config.TwitchConfig;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class SEPrinterAppConfiguration extends Configuration {

    @JsonProperty("streamelements")
    @NotNull
    @Valid
    StreamElementsConfig streamElements;

    @JsonProperty("printer")
    @NotNull
    @Valid
    PrinterConfig printer;

    @JsonProperty("twitch")
    @NotNull
    @Valid
    TwitchConfig twitch;
}

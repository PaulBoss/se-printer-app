package net.bosselaar.seprinter;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.bosselaar.seprinter.config.StreamElementsConfig;
import org.hibernate.validator.constraints.*;

import javax.validation.Valid;
import javax.validation.constraints.*;

public class SEPrinterAppConfiguration extends Configuration {

    @JsonProperty("streamelements")
    @NotNull
    @Valid
    StreamElementsConfig streamElements;

}

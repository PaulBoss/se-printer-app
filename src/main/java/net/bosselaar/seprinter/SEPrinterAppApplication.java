package net.bosselaar.seprinter;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.bosselaar.seprinter.core.managed.SESocketConnection;

public class SEPrinterAppApplication extends Application<SEPrinterAppConfiguration> {

    public static void main(final String[] args) throws Exception {
        if (args.length == 0) {
            final String[] autoArgs = new String[]{"server", "dev.yml"};
            new SEPrinterAppApplication().run(autoArgs);
            return;
        }

        new SEPrinterAppApplication().run(args);
    }

    @Override
    public String getName() {
        return "SEPrinterApp";
    }

    @Override
    public void initialize(final Bootstrap<SEPrinterAppConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final SEPrinterAppConfiguration configuration,
                    final Environment environment) {
        environment.lifecycle().manage(new SESocketConnection(configuration.streamElements));
    }

}

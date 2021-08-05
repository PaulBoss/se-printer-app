package net.bosselaar.seprinter.core.printer;

import net.bosselaar.seprinter.config.PrinterConfig;
import net.bosselaar.seprinter.core.streamelements.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PngFilePrinter implements IReceiptPrinter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PngFilePrinter.class.getName());

    private final int DPI;
    private final int PRINTABLE_WIDTH;

    private final BlockingQueue<Event> jobs = new LinkedBlockingQueue<>();

    public PngFilePrinter(PrinterConfig config) {
        this.DPI = config.dpi;
        this.PRINTABLE_WIDTH = config.printableWidth;
    }

    @Override
    public void start() {
        // empty
    }

    @Override
    public void stop() {
        // empty
    }

    public void addJob(Event e) {
        try {
            printEvent(e);
        } catch (IOException ioException) {
            LOGGER.error("Could not write image");
        }
    }

    public void printEvent(Event e) throws IOException {
        byte[] imageData = ImageCreator.createEventImage(e, this.DPI, this.PRINTABLE_WIDTH, false);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));

        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ISO_DATE_TIME).replaceAll(":", "-");

        ImageIO.write(image, "PNG", new File("event-" + dateStr+ ".png"));

    }


}

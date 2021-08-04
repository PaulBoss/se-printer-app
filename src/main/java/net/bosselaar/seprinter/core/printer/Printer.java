package net.bosselaar.seprinter.core.printer;

import io.dropwizard.lifecycle.Managed;
import net.bosselaar.seprinter.core.streamelements.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.PrinterResolution;
import javax.print.attribute.standard.QueuedJobCount;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Printer implements Managed, Runnable {

    private volatile static boolean stop = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(Printer.class.getName());

    private static int DPI = 203;

    private final PrintService defaultPrinterService;
    private final BlockingQueue<Event> jobs = new LinkedBlockingQueue<>();

    public Printer() {
        this.defaultPrinterService = PrintServiceLookup.lookupDefaultPrintService();
        System.out.println("Default printer: " + defaultPrinterService.getName());
    }

    @Override
    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void stop() {
        stop = true;
    }

    public void addJob(Event e) {
        jobs.add(e);
    }

    public void printEvent(Event e) throws PrintException, IOException, InterruptedException {
        String text1 = String.format("%s", e.data.username);
        String text2= String.format("donated %.2f %s", e.data.amount, e.data.currency);

        byte[] imageData = createImage(e.data.avatar, text1, text2);

        Doc imageDoc = new SimpleDoc(imageData, DocFlavor.BYTE_ARRAY.PNG, null);

        DocPrintJob printJob = defaultPrinterService.createPrintJob();
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        pras.add(new PrinterResolution(DPI, DPI, PrinterResolution.DPI));
        pras.add(new MediaPrintableArea(5, 5, 48, 48, MediaPrintableArea.MM));
        pras.add(new Copies(1));

        PrintJobWatcher watcher = new PrintJobWatcher(printJob);
        printJob.print(imageDoc, pras);
        watcher.waitForDone();

        /*
            Somehow even after waitForDone a new job was submitted too quickly. So wait for the number of jobs
            to be zero.
         */
        while (!isPrintCompleted(defaultPrinterService)) {
            Thread.sleep(100);
            System.out.println("Waiting..");
        }
    }

    private int getTextWidth(Graphics graphics, Font font, String text) {
        FontMetrics metrics = graphics.getFontMetrics(font);
        return  metrics.stringWidth(text);
    }

    private boolean isPrintCompleted(PrintService printer) {
        return Arrays.stream(printer.getAttributes().toArray())
                .filter(att -> att instanceof QueuedJobCount)
                .map(QueuedJobCount.class::cast)
                .findFirst()
                .map(queuedJobCount -> queuedJobCount.getValue() == 0)
                .orElse(false);
    }

    private byte[] createImage(String url, String textLine1, String textLine2) throws IOException {
        final int WIDTH = 3840; // 203 DPI * 48mm
        final int HEIGHT = 3840;

        BufferedImage img = ImageIO.read(new URL(url));

        BufferedImage output = new BufferedImage(WIDTH,HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = output.createGraphics();

        g.setColor(new Color(0,0,0,0));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        int imageWidth = WIDTH - 1000;

        g.drawImage(img, 500, 0, imageWidth, imageWidth, null);
        Font font = new Font("Arial", Font.PLAIN, 256);
        g.setFont(font);
        g.setColor(Color.black);

        int textWidth = getTextWidth(g, font, textLine1);
        int xpos = (WIDTH / 2) - (textWidth / 2);
        g.drawString(textLine1, xpos, imageWidth + 300);

        drawTextCentered(g, font, textLine1, WIDTH, imageWidth + 300);
        drawTextCentered(g, font, textLine2, WIDTH, imageWidth + 600);

        g.dispose();

        return createPng(output).toByteArray();
    }

    /**
     * Draw the text in givenfont horizontal centered for the given width at the given ypos
     * @param g The grapics to drawn on
     * @param font The font to draw the text
     * @param text The text to draw
     * @param width The image width to center the text in
     * @param ypos The y position for the drawn text
     */
    private void drawTextCentered(Graphics g, Font font, String text, int width, int ypos) {
        int textWidth = getTextWidth(g, font, text);
        int xpos = (width / 2) - (textWidth / 2);
        g.drawString(text, xpos, ypos);
    }

    /**
     * Set the DPI metadata of the image. This is 72 by default which is too low for printing
     * @param metadata The image metadata which needs to be updated
     * @throws IIOInvalidTreeException ..
     */
    private void setDPI(IIOMetadata metadata) throws IIOInvalidTreeException {
        final double INCH_2_CM = 2.54;
        // for PMG, it's dots per millimeter
        final double dotsPerMilli = 1.0 * DPI / 10 / INCH_2_CM;

        IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
        horiz.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
        vert.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode dim = new IIOMetadataNode("Dimension");
        dim.appendChild(horiz);
        dim.appendChild(vert);

        IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
        root.appendChild(dim);

        metadata.mergeTree("javax_imageio_1.0", root);
    }

    /**
     * Create the PNG binary data with high DPI
     * @param image The source image
     * @return A high dpi png
     * @throws IOException ..
     */
    private ByteArrayOutputStream createPng(BufferedImage image) throws IOException {
        for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName("png"); iw.hasNext();) {
            ImageWriter writer = iw.next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
            IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
            if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
                continue;
            }

            setDPI(metadata);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final ImageOutputStream outStream = ImageIO.createImageOutputStream(bos);
            try {
                writer.setOutput(outStream);
                writer.write(metadata, new IIOImage(image, null, metadata), writeParam);
            } finally {
                outStream.close();
                bos.close();
            }
            return bos;
        }
        return null;
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                Event e = jobs.take();
                printEvent(e);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            } catch (PrintException e) {
                LOGGER.error("Error while printing", e);
            } catch (IOException e) {
                LOGGER.error("I/O error while printing", e);
            }
        }
    }


    /**
     * Small class to wait for the actuall completion of the printer
     */
    public static class PrintJobWatcher {
        private boolean done = false;

        PrintJobWatcher(DocPrintJob job) {
            job.addPrintJobListener(new PrintJobAdapter() {
                public void printJobCanceled(PrintJobEvent pje) {
                    System.out.println("Cancelled");
                    allDone();
                }
                public void printJobCompleted(PrintJobEvent pje) {
                    System.out.println("Completed");
                    allDone();
                }
                public void printJobFailed(PrintJobEvent pje) {
                    System.out.println("Failed?");
                    allDone();
                }
                public void printJobNoMoreEvents(PrintJobEvent pje) {
                    System.out.println("No more events");
                    allDone();
                }
                void allDone() {
                    synchronized (PrintJobWatcher.this) {
                        done = true;
                        System.out.println("Printing done ...");
                        PrintJobWatcher.this.notify();
                    }
                }
            });
        }
        public synchronized void waitForDone() {
            try {
                while (!done) {
                    wait();
                }
            } catch (InterruptedException e) {
                // empty
            }
        }
    }

}

package net.bosselaar.seprinter.core.printer;

import io.dropwizard.lifecycle.Managed;
import net.bosselaar.seprinter.config.PrinterConfig;
import net.bosselaar.seprinter.core.streamelements.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.PrinterResolution;
import javax.print.attribute.standard.QueuedJobCount;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DefaultPrinterPrinter implements IReceiptPrinter, Managed, Runnable {

    private volatile static boolean stop = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPrinterPrinter.class.getName());

    private final int DPI;
    private final int PRINTABLE_WIDTH;
    private final int NON_PRINT_WIDTH;
    private final int ANGLE;

    private final PrintService defaultPrinterService;
    private final BlockingQueue<Event> jobs = new LinkedBlockingQueue<>();

    public DefaultPrinterPrinter(PrinterConfig config) {
        this.defaultPrinterService = PrintServiceLookup.lookupDefaultPrintService();
        System.out.println("Default printer: " + defaultPrinterService.getName());
        this.DPI = config.dpi;
        this.PRINTABLE_WIDTH = config.printableWidth;
        this.NON_PRINT_WIDTH = config.paperWidth - config.printableWidth / 2;
        this.ANGLE = config.angle;
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
        byte[] imageData = ImageCreator.createEventImage(e, this.DPI, this.PRINTABLE_WIDTH, this.ANGLE);

        Doc imageDoc = new SimpleDoc(imageData, DocFlavor.BYTE_ARRAY.PNG, null);

        DocPrintJob printJob = defaultPrinterService.createPrintJob();
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        pras.add(new PrinterResolution(DPI, DPI, PrinterResolution.DPI));
        pras.add(new MediaPrintableArea(NON_PRINT_WIDTH, 0, PRINTABLE_WIDTH, PRINTABLE_WIDTH, MediaPrintableArea.MM));
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

    private boolean isPrintCompleted(PrintService printer) {
        return Arrays.stream(printer.getAttributes().toArray())
                .filter(att -> att instanceof QueuedJobCount)
                .map(QueuedJobCount.class::cast)
                .findFirst()
                .map(queuedJobCount -> queuedJobCount.getValue() == 0)
                .orElse(false);
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

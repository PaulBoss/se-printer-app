package net.bosselaar.seprinter;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PrinterTest {
    public static void main(String[] args) throws PrintException, InterruptedException {

        PrintService defaultPrinterService = PrintServiceLookup.lookupDefaultPrintService();
        System.out.println("Default printer: " + defaultPrinterService.getName());


        String text = "Hello\r\nworld\f";
        InputStream is = new ByteArrayInputStream(text.getBytes(StandardCharsets.US_ASCII));


        Doc doc = new SimpleDoc(is, DocFlavor.INPUT_STREAM.AUTOSENSE, null);

        DocPrintJob printJob = defaultPrinterService.createPrintJob();
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        pras.add(new Copies(1));

        PrintJobWatcher watcher = new PrintJobWatcher(printJob);
        printJob.print(doc, pras);

        Thread.sleep(10000);

        watcher.waitForDone();
        System.out.println("done");

    }

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
            }
        }
    }

}

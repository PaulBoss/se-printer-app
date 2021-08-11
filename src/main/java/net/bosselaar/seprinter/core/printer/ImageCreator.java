package net.bosselaar.seprinter.core.printer;

import net.bosselaar.seprinter.core.streamelements.model.Event;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

public class ImageCreator {

    private final static double INCH_2_CM = 2.54;

    public static byte[] createEventImage(Event e, int dpi, int printableSize, int angle) throws IOException {
        String text1;
        String text2;

        switch (e.type) {
            case tip:
                text1 = String.format("%s", e.data.username);
                text2= String.format("donated %.2f %s.", e.data.amount, e.data.currency);
                break;
            case cheer:
                text1 = String.format("%s", e.data.displayName);
                text2= String.format("cheered %.0f bits.", e.data.amount);
                break;
            case raid:
                text1 =String.format("%s raided", e.data.displayName);
                text2= String.format("with %.0f viewers!", e.data.amount);
                break;
            case subscriber:
                if (e.data.gifted) {
                    text1 = String.format("%s just gifted", e.data.sender);
                    text2 = String.format("a sub to %s!", e.data.displayName);
                } else {
                    text1 = String.format("%s subscribed!", e.data.displayName);
                    text2 = String.format("%d month streak.", e.data.amount.longValue());
                }
                break;

            default:
                text1 = "Bits/cheers/etc";
                text2= "will be printed!";
        }

        return createImage(e.data.avatar, text1, text2, dpi, printableSize, angle);
    }

    private static int getTextWidth(Graphics graphics, Font font, String text) {
        FontMetrics metrics = graphics.getFontMetrics(font);
        return  metrics.stringWidth(text);
    }


    private static byte[] createImage(String url, String textLine1, String textLine2, int dpi, int printableSize, int angle) throws IOException {
        final int WIDTH = (int)(dpi * printableSize / INCH_2_CM);
        final int HEIGHT = WIDTH;

        final BufferedImage img = ImageIO.read(new URL(url));

        final BufferedImage output = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = output.createGraphics();

        if (angle != 0) {
            final AffineTransform at = new AffineTransform();
            at.rotate(Math.toRadians(angle), WIDTH / 2.0, HEIGHT / 2.0);
            g.transform(at);
        }

        g.setColor(new Color(0,0,0,0));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        final int imageWidth = WIDTH - (WIDTH / 4);
        final int fontSize = WIDTH / 13;
        final int lineSize = fontSize / 5 + fontSize;

        g.drawImage(img, (WIDTH / 2) - (imageWidth / 2) , 0, imageWidth, imageWidth, null);
        Font font = new Font("Arial", Font.BOLD, fontSize);
        g.setFont(font);
        g.setColor(Color.black);

        drawTextCentered(g, font, textLine1, WIDTH, imageWidth + lineSize);
        drawTextCentered(g, font, textLine2, WIDTH, imageWidth + lineSize * 2);

        g.dispose();

        return createPng(output, dpi).toByteArray();
    }

    /**
     * Draw the text in givenfont horizontal centered for the given width at the given ypos
     * @param g The grapics to drawn on
     * @param font The font to draw the text
     * @param text The text to draw
     * @param width The image width to center the text in
     * @param ypos The y position for the drawn text
     */
    private static void drawTextCentered(Graphics g, Font font, String text, int width, int ypos) {
        int textWidth = getTextWidth(g, font, text);
        int xpos = (width / 2) - (textWidth / 2);
        g.drawString(text, xpos, ypos);
    }

    /**
     * Create the PNG binary data with high DPI
     * @param image The source image
     * @return A high dpi png
     * @throws IOException ..
     */
    private static ByteArrayOutputStream createPng(BufferedImage image, int dpi) throws IOException {
        for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName("png"); iw.hasNext();) {
            ImageWriter writer = iw.next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
            IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
            if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
                continue;
            }

            setDPI(metadata, dpi);

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

    /**
     * Set the DPI metadata of the image. This is 72 by default which is too low for printing
     * @param metadata The image metadata which needs to be updated
     * @throws IIOInvalidTreeException ..
     */
    private static void setDPI(IIOMetadata metadata, int dpi) throws IIOInvalidTreeException {
        // for PNG, it's dots per millimeter
        final double dotsPerMilli = 1.0 * dpi / 10 / INCH_2_CM;

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
}

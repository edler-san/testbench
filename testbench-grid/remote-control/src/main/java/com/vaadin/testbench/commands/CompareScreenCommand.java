package com.vaadin.testbench.commands;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.mortbay.log.LogFactory;
import org.openqa.selenium.server.RobotRetriever;
import org.openqa.selenium.server.commands.Command;

import com.vaadin.testbench.util.ImageComparisonUtil;
import com.vaadin.testbench.util.ReferenceImageRepresentation;

/**
 * This command gets a list of 16x16 pixel blocks generated from a reference
 * image, takes a screen shot of the browser window, generates a similar list of
 * 16x16 pixel blocks and compares the two. If they are similar enough, the
 * command returns "OK" and all is well. If the images differ more than the
 * allowed tolerance, the screen shot is sent back to the caller (base64
 * encoded) and the caller can decide what to do with the data.
 * 
 * Parameters (Strings): <br>
 * - A representation of reference image calculated as the average sum of RGB
 * values over 16x16 blocks <br>
 * - The tolerance for error (0..768) <br>
 * - The maximum number of retries <br>
 * - The X position of the canvas <br>
 * - The Y position of the canvas <br>
 * - The width of the canvas <br>
 * - The height of the canvas
 * 
 * @author Jonatan Kronqvist / Vaadin Ltd
 */
public class CompareScreenCommand extends Command {

    private static final Log LOGGER = LogFactory
            .getLog(CompareScreenCommand.class);

    private ReferenceImageRepresentation referenceImage;
    private final float tolerance;
    private final int maxRetries;
    private final int canvasX;
    private final int canvasY;
    private final int canvasWidth;
    private final int canvasHeight;

    private BufferedImage screenshot;

    private int numScreenShotsTaken = 0;

    public CompareScreenCommand(Vector<String> parameters) {
        parseBlockParameter(parameters.get(0));
        tolerance = Float.valueOf(parameters.get(1));
        maxRetries = Integer.valueOf(parameters.get(2));
        canvasX = Integer.valueOf(parameters.get(3));
        canvasY = Integer.valueOf(parameters.get(4));
        canvasWidth = Integer.valueOf(parameters.get(5));
        canvasHeight = Integer.valueOf(parameters.get(6));
    }

    /**
     * Parses the 16x16 pixel block data from a Base64 encoded representation.
     * 
     * @param blockParam
     *            the block data string.
     */
    private void parseBlockParameter(String blockParam) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(
                    Base64.decodeBase64(blockParam.getBytes()));
            ObjectInputStream objIn = new ObjectInputStream(
                    new GZIPInputStream(in));
            Object obj = objIn.readObject();
            objIn.close();
            if (obj instanceof ReferenceImageRepresentation) {
                referenceImage = (ReferenceImageRepresentation) obj;
            } else {
                LOGGER.error("Unexpected class in serialized data.");
            }
        } catch (IOException e) {
            LOGGER.error("Could not load reference image data", e);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Could not load reference image data", e);
        }
    }

    @Override
    public String execute() {
        try {
            if (grabAndCompareScreenshot()) {
                return "OK," + numScreenShotsTaken;
            } else {
                return "OK," + numScreenShotsTaken + ","
                        + getBase64EncodedScreenshot();
            }
        } catch (InterruptedException e) {
            LOGGER.error("Problem comparing screenshots", e);
            return "ERROR: Problem comparing screenshots: " + e.getMessage();
        } catch (ExecutionException e) {
            LOGGER.error("Problem comparing screenshots", e);
            return "ERROR: Problem comparing screenshots: " + e.getMessage();
        } catch (TimeoutException e) {
            LOGGER.error("Problem comparing screenshots", e);
            return "ERROR: Problem comparing screenshots: " + e.getMessage();
        } catch (IOException e) {
            LOGGER.error("Problem comparing screenshots", e);
            return "ERROR: Problem comparing screenshots: " + e.getMessage();
        }
    }

    private String getBase64EncodedScreenshot() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ImageIO.write(screenshot, "png", outStream);
        return new String(Base64.encodeBase64(outStream.toByteArray()));
    }

    /**
     * Takes a screen shot and compares it to the reference returning true if
     * equal. If the comparison fails we retry maxRetries times and if it still
     * fails we return false.
     * 
     * @return true if the screen shot is equal to the reference.
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    private boolean grabAndCompareScreenshot() throws InterruptedException,
            ExecutionException, TimeoutException {
        numScreenShotsTaken = 0;
        for (int i = 0; i < maxRetries; i++) {
            screenshot = grabScreenshot();
            numScreenShotsTaken++;
            int[] shotBlocks = ImageComparisonUtil
                    .generateImageBlocks(screenshot);
            if (ImageComparisonUtil.blocksEqual(referenceImage, shotBlocks,
                    tolerance)) {
                return true;
            }
            CommandUtil.pause(250);
        }
        return false;
    }

    private BufferedImage grabScreenshot() throws InterruptedException,
            ExecutionException, TimeoutException {
        return RobotRetriever.getRobot().createScreenCapture(
                new Rectangle(canvasX, canvasY, canvasWidth, canvasHeight));
    }

}
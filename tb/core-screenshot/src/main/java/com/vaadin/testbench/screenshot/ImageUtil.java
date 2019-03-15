/**
 * Copyright (C) 2012 Vaadin Ltd
 * <p>
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * <p>
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * <p>
 * You should have received a copy of the license along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 */
package com.vaadin.testbench.screenshot;

import static com.vaadin.testbench.screenshot.ScreenshotProperties.IMAGE_FILE_NAME_ENDING;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import com.vaadin.frp.functions.CheckedFunction;

/**
 * These image utility functions are for internal use only.
 */
public class ImageUtil {

  /**
   * Contains ImageUtil-internal information about an image. Used by
   * getBlock() method.
   */
  public static final class ImageProperties {
    private BufferedImage image = null;
    private Raster raster = null;
    private boolean alpha = false;
    private boolean fallback = false;
    private int width = 0;
    private int height = 0;
  }

  /**
   * Encodes target image to a Base64 string
   *
   * @return Base64 encoded String of image
   */
  public static CheckedFunction<BufferedImage, String> encodeImageToBase64() {
    return (image) -> {
      Base64 encoder = new Base64();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(image , IMAGE_FILE_NAME_ENDING , baos);
      baos.flush();
      byte[] encodedBytes = encoder.encode(baos.toByteArray());
      baos.close();
      return new String(encodedBytes);
    };
  }

  /**
   * Get luminance value for the given rgb value.
   *
   * @return
   */
  public static Function<Integer, Double> rgbToLuminance() {
    return (rgb) -> {
      int r = ((rgb >> 16) & 0xFF);
      int g = ((rgb >> 8) & 0xFF);
      int b = (rgb & 0xFF);
      return .299 * r + .587 * g + .114 * b;
    };
  }

  /**
   * Check canvas sizes and resize images to same size
   *
   * @return true/false
   */
  public static boolean imagesSameSize(BufferedImage image1 ,
                                       BufferedImage image2) {
    return (image1.getWidth() == image2.getWidth()
            && image1.getHeight() == image2.getHeight());
  }

  /**
   * Resize images to be same size. The size is determined by the minimum
   * height and minimum width of the images.
   *
   * @param image1 an image.
   * @param image2 an image.
   * @return a list containing two images with the same dimensions
   */
  public static List<BufferedImage> cropToBeSameSize(BufferedImage image1 ,
                                                     BufferedImage image2) {
    if (imagesSameSize(image1 , image2)) {
      return Arrays.asList(image1 , image2);
    }

    int minHeight = Math.min(image1.getHeight() , image2.getHeight());
    int minWidth = Math.min(image1.getWidth() , image2.getWidth());

    BufferedImage cropped1 = cropImage(image1 , minWidth , minHeight);
    BufferedImage cropped2 = cropImage(image2 , minWidth , minHeight);
    return Arrays.asList(cropped1 , cropped2);
  }

  /**
   * Crops the image to the given size starting at (0,0)
   *
   * @param image  The image to crop
   * @param width  width in pixels
   * @param height height in pixels
   */
  private static BufferedImage cropImage(BufferedImage image ,
                                         int width ,
                                         int height) {

    if (image.getWidth() == width && image.getHeight() == height) {
      return image;
    }
    return image.getSubimage(0 , 0 , width , height);
  }

  /**
   * Create a 16x16 sample buffer with space for 4 color bands
   *
   * @return
   */
  public static int[] createSampleBuffer() {
    return new int[16 * 16 * 4];
  }

  /**
   * Extract magical image properties used by the getBlock function.
   *
   * @return an ImageProperties descriptor object
   */
  public static Function<BufferedImage, ImageProperties> extractImageProperties() {
    return (image) -> {
      final int imageType = image.getType();
      ImageProperties p = new ImageProperties();
      p.image = image;
      p.raster = image.getRaster();
      p.alpha = imageType == TYPE_INT_ARGB
                || imageType == BufferedImage.TYPE_4BYTE_ABGR;
      boolean rgb = imageType == TYPE_INT_ARGB || imageType == TYPE_INT_RGB;
      boolean bgr = imageType == BufferedImage.TYPE_INT_BGR
                    || imageType == BufferedImage.TYPE_3BYTE_BGR
                    || imageType == BufferedImage.TYPE_4BYTE_ABGR;
      p.width = image.getWidth();
      p.height = image.getHeight();
      p.fallback = ! (rgb || bgr);
      return p;
    };
  }

  /**
   * Returns the 16x16 RGB block starting at (x,y) from the given image
   *
   * @param properties The properties of the image (image + metadata)
   * @param x          The x coordinate of the block (in pixels)
   * @param y          The y coordinate of the block (in pixels)
   * @param result     A sample buffer (32 bits per pixel) for storing the resulting
   *                   block, or null (a new buffer will be created)
   * @param sample     A sample buffer for storing intermediate values, or null (a
   *                   new buffer will be created). This parameter is provided mainly
   *                   for speed (providing it eliminates unnecessary block
   *                   allocations).
   * @return An array of RGB values for the block
   */
  public static int[] getBlock(final ImageProperties properties ,
                               int x , int y ,
                               int[] result ,
                               int[] sample) {

    final int width;
    final int height;

    if (result == null) result = new int[16 * 16];

    if (sample == null) sample = new int[16 * 16 * 4];

    if (x + 16 >= properties.width) width = properties.width - x;
    else width = 16;

    if (y + 16 >= properties.height) height = properties.height - y;
    else height = 16;

    final int l = width * height;

    if (properties.fallback) {

      properties.image.getRGB(x , y , width , height , result , 0 , width);

    } else {

      // NOTE: Apparently raster.getPixels() standardises channel
      // order in the retrieved sample.
      // NOTE: if this is NOT the case, provide system info and
      // relevant screenshot & reference for new integration test
      properties.raster.getPixels(x , y , width , height , sample);

      if (properties.alpha) {

        int p = 0;
        for (int i = 0; i < l; ++ i) {
          result[i] = (sample[p + 3] << 24) // A
                      | (sample[p] << 16) // R
                      | (sample[p + 1] << 8) // G
                      | sample[p + 2]; // B
          p += 4;
        }

      } else {

        int p = 0;
        for (int i = 0; i < l; ++ i) {
          result[i] = (255 << 24) // A
                      | (sample[p] << 16) // R
                      | (sample[p + 1] << 8) // G
                      | sample[p + 2]; // B
          p += 3;
        }

      }

    }

    // Fill the rest with zeros
    for (int i = l, max = result.length; i < max; ++ i) {
      result[i] = 0;
    }

    return result;
  }

  /**
   * Clones the given BufferedImage
   *
   * @return A copy of sourceImage
   */
  public static Function<BufferedImage, BufferedImage> cloneImage() {
    return (sourceImage) -> {
      // This method could likely be optimized but the gain is probably
      // small
      final int w = sourceImage.getWidth();
      final int h = sourceImage.getHeight();

      BufferedImage newImage = new BufferedImage(w , h , TYPE_INT_RGB);

      Graphics2D g = (Graphics2D) newImage.getGraphics();
      g.drawImage(sourceImage , 0 , 0 , w , h , null);
      g.dispose();

      return newImage;
    };

  }

}
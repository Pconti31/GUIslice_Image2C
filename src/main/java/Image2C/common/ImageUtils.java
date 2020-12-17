/**
 *
 * The MIT License
 *
 * Copyright 2020 Paul Conti
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package Image2C.common;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.imageio.ImageIO;


import com.pump.image.pixel.quantize.BiasedMedianCutColorQuantization;
import com.pump.image.pixel.quantize.ColorLUT;
import com.pump.image.pixel.quantize.ColorQuantization;
import com.pump.image.pixel.quantize.ColorSet;
import com.pump.image.pixel.quantize.ImageQuantization;
import com.pump.image.pixel.quantize.MedianCutColorQuantization;

import java.io.IOException;

@SuppressWarnings("unused")
public class ImageUtils {
  /** The instance. */
  private static ImageUtils instance  = null;
  
  private Color transparentColor;
  
  public final static int MOST_DIFFUSION     = 0;
  public final static int MEDIUM_DIFFUSION   = 1;
  public final static int SIMPLEST_DIFFUSION = 2;
  public final static int NEAREST_NEIGHBOR   = 3;
  
  public final static int BIASED             = 0;
  public final static int MEDIAN             = 1;

  private int hfType;           // File type "BM"
  private int hfSize;           // File size
  private short hfReserved1;    // Reserved for apps
  private short hfReserved2;
  private int hfOffset;         // Offset to pixel data

  // Header info
  private int bmpHdrSz;         // DIB Header 2 size (40)
  private int bmpWidth;         // Width in pixels
  private int bmpHeight;        // Height in pixels
  private short bmpPlanes;      // Number of planes
  private short bmpBpp;         // Number bits per pixel
  private int bmpCompression;
  private int bmpImgSz;         // Image size
  private int bmpXRes;          // Horizontal image resolution (pixels per meter)
  private int bmpYRes;          // Vertical image resolution   (pixels per meter)
  private int bmpClrUsed;       // Number colors in palette (0 to n)
  private int bmpClrImportant;  // Number colors that are important (0 is all) 
//  private int[] bmpClrIndex;    // bpp <=8 use color index for each color
//  private int bmpClrSize;       // Size of bmpClrIndex;
  
  // Image's pixel data
  private short[] bmpImage;
  private int[] decodedImage;
  private long nOriginalColors;
  private long nCurrentColors;
  
  // RLE data
  boolean bFlip;
  byte lastByte;
  int  matchCount;
  
  /**
   * getInstance() - get our Singleton Object.
   *
   * @return instance
   */
  public static synchronized ImageUtils getInstance() {
    if (instance == null) {
      instance = new ImageUtils();
    }
    return instance;
  }

  /**
   * empty constructor.
   */
  public ImageUtils() {
    // For GUIslice the default value is 'pink' for the transparent pixel color.
    // can be overridden by calling setTransparentPixelColor()
    transparentColor = new Color(255,0,255); // Color MAGENTA;
  }
  
  public BufferedImage convertTo24(BufferedImage inputImage, boolean bTransparent) {
    BufferedImage outputImage = null;
    if(inputImage.getType() != BufferedImage.TYPE_INT_RGB && !bTransparent) {
      // most incoming BufferedImage that went through some ImageTools operation are ARGB
      // saving ARGB to bmp will not fail, but pixel color gets distorted
      // need to convert to RGB 3-channel before saving as non-alpha format, BMP 24,16 ot JPEG
      // https://stackoverflow.com/a/46460009/1124509
      // https://stackoverflow.com/questions/9340569/jpeg-image-with-wrong-colors

      //Get the height and width of the image
      int width = inputImage.getWidth();
      int height = inputImage.getHeight();
  
      //Get the pixels of the image to an int array 
      int [] pixels=inputImage.getRGB(0, 0,width,height,null,0,width);

      //Create a new buffered image without an alpha channel. (TYPE_INT_RGB)
      BufferedImage copy = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

      //Set the pixels of the original image to the new image
      copy.setRGB(0, 0,width,height,pixels,0,width);
      inputImage = copy;
    }
    ColorSet inputColors = new ColorSet();
    inputColors.addColors(inputImage);
    nOriginalColors = inputColors.getColorCount();
    // creates output image as 24 bit color
    bmpBpp = 24;
    outputImage = new BufferedImage(inputImage.getWidth(),
        inputImage.getHeight(), BufferedImage.TYPE_INT_RGB);
    // scales the input image to the output image
    Graphics2D g2d = outputImage.createGraphics();
    if (bTransparent) 
      g2d.setColor(transparentColor);
    else
      g2d.setColor(Color.WHITE);
    g2d.fillRect(0, 0, inputImage.getWidth(), inputImage.getHeight());
    g2d.drawImage(inputImage, 0, 0, inputImage.getWidth(),  inputImage.getHeight(), null);
    g2d.dispose();
    ColorSet outputColors = new ColorSet();
    outputColors.addColors(outputImage);
    nCurrentColors = outputColors.getColorCount();
    return outputImage;
  }
  
  public BufferedImage convertTo8(BufferedImage inputImage) {
    BufferedImage outputImage = null;
    ColorSet inputColors = new ColorSet();
    inputColors.addColors(inputImage);
    outputImage = colorQuantizer(inputImage, inputColors, 256, 
        ImageUtils.MOST_DIFFUSION, ImageUtils.BIASED);
    ColorSet outputColors = new ColorSet();
    outputColors.addColors(outputImage);
    int nColors = outputColors.getColorCount();
    // Success?
    if (nColors <= 256) {
      nCurrentColors = nColors;
      bmpBpp = 8;
    } else { 
      // failure, do not return anything
      outputImage = null;
    }
    return outputImage;
  }
  
  public BufferedImage convertTo4(BufferedImage inputImage) {
    BufferedImage outputImage = null;
    ColorSet inputColors = new ColorSet();
    inputColors.addColors(inputImage);
    BufferedImage tempImage = colorQuantizer(inputImage, inputColors, 16, 
        ImageUtils.MOST_DIFFUSION, ImageUtils.BIASED);
    ColorSet outputColors = new ColorSet();
    outputColors.addColors(tempImage);
    int nColors = outputColors.getColorCount();

    // Success?
    if (nColors <= 16) {
      // Create the BufferedImage
      int w = inputImage.getWidth(), h = inputImage.getHeight();
      int length = (w * h);

      byte[] data = new byte[length];
      DataBuffer db = new DataBufferByte(data, length);
      WritableRaster wr = Raster.createPackedRaster(db, w, h, 4, null);
      outputImage = new BufferedImage(outputColors.createIndexColorModel(false, false), 
          wr, false, null);

      // packs the input image to the output image
      Graphics2D g2d = outputImage.createGraphics();
      g2d.drawImage(inputImage, 0, 0, inputImage.getWidth(),  inputImage.getHeight(), null);
      g2d.dispose();

      nCurrentColors = nColors;
      bmpBpp = 4;
    } else { 
      // failure, do not return anything
      outputImage = null;
    }
    return outputImage;
  }
  
  public BufferedImage convertTo1(BufferedImage inputImage, float bwfactor) {
    
    // our colorQuantizer does a poor job with 1 Bpp
    // so I use a more brute force method
    BufferedImage dst = new BufferedImage(
        inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_INT_RGB);
    float[] scales = {bwfactor, bwfactor, bwfactor};
    float[] offsets = new float[4];
    RescaleOp rop = new RescaleOp(scales, offsets, null);

    Graphics2D g = dst.createGraphics();
    g.drawImage(inputImage, rop, 0, 0);
    g.dispose();
    
    byte[] bw = {(byte) 0xff, (byte) 0};
    IndexColorModel blackAndWhite = new IndexColorModel(
    1, // One bit per pixel
    2, // Two values in the component arrays
    bw, // Red Components
    bw, // Green Components
    bw);// Blue Components
    
    // Create the BufferedImage
    int w = dst.getWidth(), h = dst.getHeight();
    int length = ((w + 7) * h) / 8;

    byte[] data = new byte[length];
    DataBuffer db = new DataBufferByte(data, length);
    WritableRaster wr = Raster.createPackedRaster(db, w, h, 1, null);
    BufferedImage outputImage = new BufferedImage(blackAndWhite, wr, false, null);
    
    // packs the input image to the output image as 1 bit
    Graphics2D g2d = outputImage.createGraphics();
    g2d.drawImage(dst, 0, 0, dst.getWidth(),  dst.getHeight(), null);
    g2d.dispose();

    bmpBpp = 1;
    nCurrentColors = 2;
    return outputImage;
  }
  
  public BufferedImage grayscale(BufferedImage inputImage) {
    int width = inputImage.getWidth();
    int height = inputImage.getHeight();

    // Create the BufferedImage
    BufferedImage outputImage = new BufferedImage(width, height, 
        inputImage.getType());
    
    int rgbValue;
    int B=0,G=0,R=0;
    int intensity;
    int gray;
    
    for (int row = 0; row < width; row++) {
      for (int col = 0; col < height; col++) {
        rgbValue = inputImage.getRGB(row, col);
        R = (rgbValue & 0x00ff0000) >> 16;
        G = (rgbValue & 0x0000ff00) >> 8;
        B = rgbValue & 0x000000ff;
        // take conversion up to one single value by calculating pixel intensity.
        intensity = (int) (0.2126 * R + 0.7152 * G + 0.0722 * B);
        gray = (intensity<<16) | (intensity<<8) | intensity; 
        outputImage.setRGB(row, col, gray);
      }
    }
    ColorSet outputColors = new ColorSet();
    outputColors.addColors(outputImage);
    nCurrentColors = outputColors.getColorCount();
    if (nCurrentColors <= 2)
      bmpBpp = 1;
    else if (nCurrentColors <= 16)
      bmpBpp = 4;
    else if (nCurrentColors <= 256)
      bmpBpp = 8;
    else
      bmpBpp = 16;
    return outputImage;
  }
  
  public long getCurrentColors() {
    return nCurrentColors;
  }
  
  public long getOriginalColors() {
    return nOriginalColors;
  }
  
  public int getBitDepth() {
    return bmpBpp;
  }
  
  /**
   * Set the value of transparent pixel colors
   *
   * @param r
   *        the red value
   * @param g
   *        the green value
   * @param b
   *        the blue value
   *
   */
  public void setTransparentPixelColor(Color transparentColor) {
    this.transparentColor = transparentColor;
  }
  
  public BufferedImage clone(BufferedImage inputImage) {
    BufferedImage outputImage = new BufferedImage(inputImage.getWidth(),
        inputImage.getHeight(), inputImage.getType());
    // scales the input image to the output image
    Graphics2D g2d = outputImage.createGraphics();
    g2d.drawImage(inputImage, 0, 0, inputImage.getWidth(),  inputImage.getHeight(), null);
    g2d.dispose();
    return outputImage;
  }

  public BufferedImage colorQuantizer(BufferedImage image, ColorSet ic, int nColors,
      int algorithm, int q) {
     ColorQuantization cq = null;
     if (algorithm == ImageUtils.BIASED)
         cq = new BiasedMedianCutColorQuantization(.1f);
     else
       cq = new MedianCutColorQuantization();
     ColorSet reducedImageColors = cq.createReducedSet(ic, nColors, true);
     IndexColorModel icm = reducedImageColors.createIndexColorModel(false, false);
     ColorLUT lut = new ColorLUT(icm);
     BufferedImage reducedImage = null;
     switch (q) {
       case ImageUtils.MOST_DIFFUSION:
         reducedImage = ImageQuantization.MOST_DIFFUSION.createImage(image, lut);
         break;
       case ImageUtils.MEDIUM_DIFFUSION:
         reducedImage = ImageQuantization.MEDIUM_DIFFUSION.createImage(image, lut);
         break;
       case ImageUtils.SIMPLEST_DIFFUSION:
         reducedImage = ImageQuantization.SIMPLEST_DIFFUSION.createImage(image, lut);
         break;
       case ImageUtils.NEAREST_NEIGHBOR:
         reducedImage = ImageQuantization.NEAREST_NEIGHBOR.createImage(image, lut);
         break;
     }
     return reducedImage;
  }
  
  public BufferedImage imageResize(BufferedImage inputImage, int scaledWidth, int scaledHeight) {
    // creates output image
    BufferedImage outputImage = new BufferedImage(scaledWidth,
            scaledHeight, inputImage.getType());

    // scales the input image to the output image
    Graphics2D g2d = outputImage.createGraphics();
    g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
    g2d.dispose();
    
    return outputImage;
  }

  public void image2C_Array(BufferedImage image, String bmpFileName, File file, String arrayName, 
      String ext, boolean bUseLittleEndian, boolean bCArrayFlash) throws IOException
  {
    // First thing is to convert our image to a BMP 24 bit memory based image
    // for processing.
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    // writes to the output image in specified format
    boolean result = ImageIO.write(image, "bmp", outputStream);
    if (!result) {
      throw new IOException("ImageIO.write failed");
    }
    // needs to close the streams
    outputStream.flush();
    outputStream.close();
    
    // Now read our memory based BMP into variables so we can later do our ouput
    byte[] bmpArray = outputStream.toByteArray();
    
    LittleEndianDataInputStream fIn = 
        new LittleEndianDataInputStream(new ByteArrayInputStream(bmpArray));
    hfType = fIn.readShort();
    if (hfType != 0x4D42) {  // test for "BM"
      fIn.close();
      throw new IOException("hfType != 0x4D42");
    }
    hfSize = fIn.readInt();
    hfReserved1 = fIn.readShort();     
    hfReserved2 = fIn.readShort();     
    hfOffset = fIn.readInt();

    //Read Header Info
    bmpHdrSz = fIn.readInt();
    bmpWidth = fIn.readInt();
    bmpHeight = fIn.readInt();
    bmpPlanes = fIn.readShort();     
    bmpBpp = fIn.readShort();
    bmpCompression = fIn.readInt();
    if (bmpCompression == 0 && bmpBpp != 24) {
      fIn.close();
      throw new IOException("corrupted BMP");
    }
/*
    if (((bmpCompression == 0 && bmpBpp == 1)    || // MONO 
          (bmpCompression == 3 && bmpBpp == 16)  || // RGB565
          (bmpCompression == 0 && bmpBpp == 16)  || // RGB555
          (bmpCompression == 0 && bmpBpp == 8)  || 
          (bmpCompression == 0 && bmpBpp == 4)) && 
          (bmpPlanes == 1)) {
*/
    bmpImgSz = fIn.readInt();
    bmpXRes = fIn.readInt();
    bmpYRes = fIn.readInt();
    bmpClrUsed = fIn.readInt();
    bmpClrImportant = fIn.readInt();
/*
      if (bmpClrUsed == 0 && bmpClrImportant > 0)
        bmpClrUsed = bmpClrImportant;
      bmpClrIndex = null;
      bmpClrSize = 0;
      if (hfOffset > 54) { 
        bmpClrSize = (hfOffset-54)/4;
        if (bmpClrSize > 256) {
          fIn.close();
          return false;
        }
        // copy the color palette
        bmpClrIndex = new int[bmpClrSize];
        for (int i=0; i<bmpClrSize; i++) {
          bmpClrIndex[i] = fIn.readInt();
        }
      }
*/
/*
    String hexType = Integer.toHexString(hfType & 0xffff);
    System.out.println("hfType: " + hexType);
    System.out.println("hfSize: " + hfSize);
    System.out.println("hfOffset: " + hfOffset);
    System.out.println("bmpHdrSz: " + bmpHdrSz);
    System.out.println("bmpWidth: " + bmpWidth);
    System.out.println("bmpHeight: " + bmpHeight);
    System.out.println("bmpBpp: " + bmpBpp);
    System.out.println("bmpCompression: " + bmpCompression);
    System.out.println("bmpImgSz: " + bmpImgSz);
    System.out.println("bmpXRes: " + bmpXRes);
    System.out.println("bmpYRes: " + bmpYRes);
    System.out.println("bmpClrUsed: " + bmpClrUsed);
    System.out.println("bmpClrImportant: " + bmpClrImportant);
*/
      // display the color palette
//  System.out.println("bmpClrSize: " + bmpClrSize);
//      if (bmpClrSize > 0) {
//        for (int i=0; i<bmpClrSize; i++) {
//          String hex = String.format("0x%08X",bmpClrIndex[i]);
//          System.out.println("bmpClrIndex[" + i + "]: " + hex);
//        }
//      }
    bFlip = true;
    if (bmpHeight < 0) {
      bFlip = false;
      bmpHeight = -bmpHeight;
    }
    bmpImage = new short[bmpImgSz];
    int x;
    for(x=0; x<bmpImgSz; x++)
    {
      bmpImage[x] = (short) (fIn.readByte() & 0xFF);
    }
    fIn.close();
    
    // Everything is now stored in variables but our bitmap image needs further processing
    decodeBitmap(bUseLittleEndian);
    
    // We can now output the BMP
    // Start with the Header info
    int arraySz =  bmpWidth * bmpHeight; // image as ushorts
    String line;
    CArrayOutputStream fOut = new CArrayOutputStream(file, bUseLittleEndian);
    
    // output our boiler plate
    fOut.writeString("//------------------------------------------------------------------------------\n");
    fOut.writeString("// File Generated by image2c\n");
    fOut.writeString("//------------------------------------------------------------------------------\n");
    line = String.format("// Generated from   : %s%s\n",bmpFileName, ext);
    fOut.writeString(line);
    line = String.format("// Dimensions       : %dx%d pixels\n",bmpWidth,bmpHeight);
    fOut.writeString(line);
    line = String.format("// Bits Per Pixel   : %d Bits\n",16);
    fOut.writeString(line); 
    line = String.format("// Memory Size      : %d Bytes\n",arraySz*2);
    fOut.writeString(line); 
    line = String.format("// Little Endian    : %s uint16_t\n",bUseLittleEndian);
    fOut.writeString(line);
    fOut.writeString("//------------------------------------------------------------------------------\n");
    fOut.writeString("\n");
    
    fOut.writeString("// For details on how to generate this file please refer to\n");
    fOut.writeString("// https://github.com/ImpulseAdventure/GUIslice/wiki/Display-Images-from-FLASH\n");
    fOut.writeString("\n");
    fOut.writeString("#include \"GUIslice.h\"\n");
    fOut.writeString("#include \"GUIslice_config.h\"\n");
    fOut.writeString("\n");
    if (bCArrayFlash) {
      fOut.writeString("#if (GSLC_USE_PROGMEM)\n");
      fOut.writeString("  #if defined(__AVR__)\n");
      fOut.writeString("    #include <avr/pgmspace.h>\n");
      fOut.writeString("  #else\n");
      fOut.writeString("    #include <pgmspace.h>\n");
      fOut.writeString("  #endif\n");
      fOut.writeString("#endif\n");
      fOut.writeString("\n");
      line = String.format("const unsigned short %s[%d+2] GSLC_PMEM = {\n", arrayName, arraySz);
    } else {
      line = String.format("const unsigned short %s[%d+2] = {\n", arrayName, arraySz);
    }
    fOut.writeString(line);
    line = String.format("%d, // Height of image\n", bmpHeight);
    fOut.writeString(line);
    line = String.format("%d, // Width of image\n", bmpWidth);
    fOut.writeString(line);
    
    // Our header is completed so now do the bitmap image
    fOut.streamArray(decodedImage);
    fOut.close();
  }

  /*
   * decodeBitmap converts the image to RGB565 colors
   * Remember: scan lines are inverted in a bitmap file!
   */
  private void decodeBitmap(boolean bUseLittleEndian) {
    int nColRaw;
    decodedImage = new int[bmpWidth * bmpHeight];
    int idx = 0;
    int pos;
    // BMP rows are padded (if needed) to 4-byte boundary
    int rowSize = (bmpWidth * 3 + 3) & ~3;
    int r, g, b;
    for (int row = 0; row < bmpHeight; row++) { // For each scanline...

      // position to start of scan line.
      if (bFlip) {
        // Bitmap is stored bottom-to-top order (normal BMP)
        pos = (bmpHeight - 1 - row) * rowSize;
      } else {
        // Bitmap is stored top-to-bottom
        pos = row * rowSize;
      }
      for (int col = 0; col < bmpWidth; col++) { // For each pixel...
        // Convert pixel from BMP to TFT format, push to display
        b = bmpImage[pos++];
        g = bmpImage[pos++];
        r = bmpImage[pos++];
        // Default to DRV_COLORMODE_RGB565

        nColRaw  = (((r & 0xF8) >> 3) << 11); // Mask: 1111 1000 0000 0000
        nColRaw |= (((g & 0xFC) >> 2) <<  5); // Mask: 0000 0111 1110 0000
        nColRaw |= (((b & 0xF8) >> 3) <<  0); // Mask: 0000 0000 0001 1111
/*
    nColRaw |= (((b & 0xF8) >> 3) << 11); // Mask: 1111 1000 0000 0000
    nColRaw |= (((g & 0xFC) >> 2) <<  5); // Mask: 0000 0111 1110 0000
    nColRaw |= (((r & 0xF8) >> 3) <<  0); // Mask: 0000 0000 0001 1111
*/
        decodedImage[idx++] = (short) (nColRaw);
      } // end pixel
    } // end scanline
  }


  public void image2File(BufferedImage image, File file) throws IOException {
    BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
    newImage.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);
    ImageIO.write(newImage, "BMP", file);
  }
}

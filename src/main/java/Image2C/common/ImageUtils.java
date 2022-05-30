/**
 *
 * The MIT License
 *
 * Copyright 2018-2022 Paul Conti
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
package image2C.common;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
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
  
  private Color[] colors;
  private Color colMonochrome;
  private Color colCurrentFG;
  private Color colTransparent;
  private int   nCurrentFG_idx;
  
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
  private short[] bmpShortArray;
  private byte[]  bmpByteArray;
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
    colTransparent = new Color(255,0,255); // Color MAGENTA
    colors = null;
    colMonochrome = new Color(255,255,255); // Color WHITE
    colCurrentFG = new Color(255,255,255); // Color WHITE
  }
  
  public BufferedImage convertForegroundColor(BufferedImage inputImage, Color newColor) {
    BufferedImage newImage = null;
    if (inputImage.getType() == BufferedImage.TYPE_BYTE_BINARY) {
      if (inputImage.getColorModel() instanceof IndexColorModel) {
        IndexColorModel colorModelOriginal = (IndexColorModel) inputImage.getColorModel();
        int[] palette = new int[2];
        colorModelOriginal.getRGBs(palette);
        if (palette[0] == colCurrentFG.getRGB())
          palette[0] = newColor.getRGB();
        else
          palette[1] = newColor.getRGB();
        IndexColorModel colorModelNew = new IndexColorModel(
            1,         // bits per pixel
            2,         // size of color component array
            palette,   // color map
            0,         // offset in the map
            DataBuffer.TYPE_BYTE,
            null);
        WritableRaster raster = inputImage.getRaster();
        // Create and return the new BufferedImage
        newImage = new BufferedImage(inputImage.getWidth(),inputImage.getHeight(),inputImage.getType(), colorModelNew);
        newImage.setData(raster);
      } 
    } else {
      int nWidth = inputImage.getWidth();
      int nHeight = inputImage.getHeight();
      // create our two indexed color map
      int[] palette = new int[2];
      palette[0] = Color.BLACK.getRGB();
      palette[1] = newColor.getRGB();
      IndexColorModel colorMap = new IndexColorModel(
          1,         // bits per pixel
          2,         // size of color component array
          palette,   // color map
          0,         // start offset into color map
          DataBuffer.TYPE_BYTE,
          null);
      // Create a buffered image 
      newImage = new BufferedImage(nWidth, nHeight, BufferedImage.TYPE_BYTE_BINARY, colorMap);
      WritableRaster outRaster = newImage.getRaster();

      int[] rgb = new int[4];
      for (int y = 0; y < nHeight ; y++) {
        for (int x = 0; x < nWidth; x++) {
          int inColor = inputImage.getRGB(x, y);
          if (inColor == colCurrentFG.getRGB()) {
            outRaster.setPixel(x, y, new int[] { newColor.getRed(), 
                newColor.getGreen(), 
                newColor.getBlue(), 
                0xFF});
          }
        } // end x < nWidth
        
      } // end y < nHeight
    }
    colCurrentFG = new Color(colMonochrome.getRGB());
    return newImage;
  }
  
  public BufferedImage convertTo1(BufferedImage inputImage) {

    int nWidth = inputImage.getWidth();
    int nHeight = inputImage.getHeight();
    // create our two indexed color map
    Color colTransparent;
    int[] palette = new int[2];
    if (colCurrentFG.getRGB() == Color.WHITE.getRGB()) {
      colTransparent = Color.BLACK;
      palette[0] = Color.BLACK.getRGB();
      palette[1] = colCurrentFG.getRGB();
    } else {
      colTransparent = Color.WHITE;
      palette[0] = Color.WHITE.getRGB();
      palette[1] = colCurrentFG.getRGB();
    }
    
    IndexColorModel colorMap = new IndexColorModel(
        1,         // bits per pixel
        2,         // size of color component array
        palette,   // color map
        0,         // start offset into color map
        DataBuffer.TYPE_BYTE,
        null);
   
    int w = inputImage.getWidth(), h = inputImage.getHeight();
    int length = (w * h);

//    byte[] data = new byte[length];
//    DataBuffer db = new DataBufferByte(data, length);
//    WritableRaster wr = Raster.createPackedRaster(db, w, h, 1, null);
//    BufferedImage blackwhiteImage = new BufferedImage(colorMap, wr, false, null);
    BufferedImage blackwhiteImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY, colorMap);
//    BufferedImage blackwhiteImage = new BufferedImage(w, h, BufferedImage.CUSTOM);
    ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
      op.filter(inputImage, blackwhiteImage);

    // packs the input image to the output image
    Graphics2D g2d = blackwhiteImage.createGraphics();
    g2d.drawImage(inputImage, 0, 0, colTransparent, null);
//    g2d.drawImage(inputImage, 0, 0, null);
    g2d.dispose();

    return blackwhiteImage;
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
  
  public BufferedImage convertTo24(BufferedImage inputImage) {
    BufferedImage outputImage = null;
    nOriginalColors = getNumberOfColors(inputImage);
    // creates output image as 24 bit color
    bmpBpp = 24;
    outputImage = new BufferedImage(inputImage.getWidth(),
        inputImage.getHeight(), BufferedImage.TYPE_INT_RGB);
    // scales the input image to the output image
    Graphics2D g2d = outputImage.createGraphics();
    g2d.drawImage(inputImage, 0, 0, inputImage.getWidth(),  inputImage.getHeight(), colTransparent, null);
    g2d.dispose();
    ColorSet outputColors = new ColorSet();
    outputColors.addColors(outputImage);
    nCurrentColors = outputColors.getColorCount();
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
  
  public long getNumberOfColors(BufferedImage inputImage) {
    colors = null;
    colCurrentFG = new Color(255,255,255); // Color WHITE
    ColorSet inputColors = new ColorSet();
    inputColors.addColors(inputImage);
    nOriginalColors = inputColors.getColorCount();
    nCurrentColors = nOriginalColors;
    if (nCurrentColors < 3) {
      colors = inputColors.getColors();
      colCurrentFG = colors[0];
      nCurrentFG_idx = 0;
    }
    return nOriginalColors;
  }
  
  public Color swapFG() {
    if (nCurrentColors == 2) {
      nCurrentFG_idx ^= 1;
      colCurrentFG = colors[nCurrentFG_idx];
    }
    return colCurrentFG;
  }
  
  public long getOriginalColors() {
    return nOriginalColors;
  }
  
  public long getCurrentColors() {
    return nCurrentColors;
  }
  
  public int getBitDepth() {
    return bmpBpp;
  }
  
  /**
   * Set the value of transparent pixel color
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
    this.colTransparent = transparentColor;
  }
  
  /**
   * Set the value of monochrome pixel color
   *
   * @param color
   *        the color value
   */
  public void setMonochromeColor(Color color) {
    this.colMonochrome = color;
  }
  
  /**
   * get the value of monochrome pixel color
   * @return
   */
  public Color getMonochromeColor() {
    return colMonochrome;
  }
  
  /**
   * setBackgroundColor
   * @param color
   */
  public void setFGColor(Color color) {
    this.colCurrentFG = color;
  }
  
  public Color getFGColor() {
    return colCurrentFG;
  }
  
  public BufferedImage clone(BufferedImage inputImage) {
    BufferedImage outputImage = null;
    ColorModel cm = inputImage.getColorModel();
    boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
    WritableRaster raster = inputImage.copyData(inputImage.getRaster().createCompatibleWritableRaster());
    outputImage= new BufferedImage(cm, raster, isAlphaPremultiplied, null);
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

  public void image2C_Array(BufferedImage inputImage, String bmpFileName, File file, String arrayName, 
      String ext, boolean bUseLittleEndian, boolean bCArrayFlash, boolean bIsTransparent) throws IOException
  {
    int arraySz;
    try {

      if( nCurrentColors < 3 && inputImage.getType() != BufferedImage.TYPE_BYTE_BINARY) {
       BufferedImage blackWhite = convertTo1(inputImage);
       DataBufferByte data = (DataBufferByte) blackWhite.getRaster().getDataBuffer();
       bmpByteArray = data.getData();
       bmpBpp = 1;
       bmpWidth = inputImage.getWidth();
       bmpHeight = inputImage.getHeight();
       bmpImgSz = bmpByteArray.length;
       arraySz = bmpImgSz;
    } else if(inputImage.getType() == BufferedImage.TYPE_BYTE_BINARY) {
      
      BufferedImage blackWhite = 
         new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
      ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
      op.filter(inputImage, blackWhite);
      DataBufferByte data = (DataBufferByte) blackWhite.getRaster().getDataBuffer();
      bmpByteArray = data.getData();
      bmpBpp = 1;
      bmpWidth = inputImage.getWidth();
      bmpHeight = inputImage.getHeight();
      bmpImgSz = bmpByteArray.length;
      arraySz = bmpImgSz;
//      System.out.println("bmpByteArray len: " + bmpImgSz);
    } else {
      // First thing is to convert our image to a BMP 24 bit memory based image
      // for processing.
      BufferedImage convertedImage = convertTo24(inputImage);
      
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      // writes to the output image in specified format
      boolean result = ImageIO.write(convertedImage, "bmp", outputStream);
      if (!result) {
        throw new IOException("ImageIO.write failed");
      }
      // needs to close the streams
      outputStream.flush();
      outputStream.close();
      // Now read our memory based BMP into variables so we can later do our output
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
      // display the color palette
      System.out.println("bmpClrSize: " + bmpClrSize);
      if (bmpClrSize > 0) {
        for (int i=0; i<bmpClrSize; i++) {
          String hex = String.format("0x%08X",bmpClrIndex[i]);
          System.out.println("bmpClrIndex[" + i + "]: " + hex);
         }
       }
   * 
   */
      bFlip = true;
      if (bmpHeight < 0) {
        bFlip = false;
        bmpHeight = -bmpHeight;
      }
      
      bmpShortArray = new short[bmpImgSz];
      for(int x=0; x<bmpImgSz; x++)
      {
        bmpShortArray[x] = (short) (fIn.readByte() & 0xFF);
      }
      arraySz =  bmpWidth * bmpHeight * 2; 
      fIn.close();
    }
      
    // We can now output the BMP
    // Start with the Header info
    String line;
    CArrayOutputStream fOut = new CArrayOutputStream(file, bUseLittleEndian);
    
    // output our boiler plate
    fOut.writeString("//------------------------------------------------------------------------------\n");
    fOut.writeString("// File Generated by GUIslice_Image2C\n");
    fOut.writeString("//------------------------------------------------------------------------------\n");
    line = String.format("// Generated from   : %s%s\n",bmpFileName, ext);
    fOut.writeString(line);
    line = String.format("// Dimensions       : %dx%d pixels\n",bmpWidth,bmpHeight);
    fOut.writeString(line);
    if (bmpBpp == 1) {
      line = String.format("// Bits Per Pixel   : %d Bits\n",1);
    } else {
      line = String.format("// Bits Per Pixel   : %d Bits\n",16);
    }
    fOut.writeString(line); 
    line = String.format("// Memory Size      : %d Bytes\n",arraySz);
    fOut.writeString(line); 
    line = String.format("// Little Endian    : %s\n",bUseLittleEndian);
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
      if (bmpBpp == 1) {
        line = String.format("const unsigned char %s[%d+7] GSLC_PMEM = {\n", arrayName, arraySz);
      } else {
        line = String.format("const unsigned short %s[%d+2] GSLC_PMEM = {\n", arrayName, arraySz/2);
      }
    } else {
      if (bmpBpp == 1) {
        line = String.format("const unsigned char %s[%d+5] = {\n", arrayName, arraySz);
      } else {
        line = String.format("const unsigned short %s[%d+2] = {\n", arrayName, arraySz/2);
      }
    }
    fOut.writeString(line);
    
    if (bmpBpp == 1) {
      // output Height and width as two bytes in big endian format
      int byteHi = (bmpHeight >>> 8) & 0xFF;
      int byteLo = bmpHeight & 0xFF;
      line = String.format("0x%02X, // Height of image\n", byteHi);
      fOut.writeString(line);
      line = String.format("0x%02X,\n", byteLo);
      fOut.writeString(line);
      byteHi = (bmpWidth >>> 8) & 0xFF;
      byteLo = bmpWidth & 0xFF;
      line = String.format("0x%02X, // Width of image\n", byteHi);
      fOut.writeString(line);
      line = String.format("0x%02X,\n", byteLo);
      fOut.writeString(line);
      /* for 1 bit pixel imahes GUIslice expects 
       * the foreground color to be output as
       * red, green, and blue
       */
      line = String.format("%3d, // red color\n",colMonochrome.getRed());
      fOut.writeString(line);
      line = String.format("%3d, // green color\n",colMonochrome.getGreen());
      fOut.writeString(line);
      line = String.format("%3d, // blue color\n",colMonochrome.getBlue());
      fOut.writeString(line);
      // Our header is completed so now do the bitmap image
      fOut.streamArray(bmpByteArray);
    }  else {  
      line = String.format("%d, // Height of image\n", bmpHeight);
      fOut.writeString(line);
      line = String.format("%d, // Width of image\n", bmpWidth);
      fOut.writeString(line);
      // Our header is completed
      // Everything is now stored in variables but our bitmap image needs further processing
      decodeBitmap(bUseLittleEndian);
      fOut.streamArray(decodedImage);
    }
    fOut.close();
    } catch (Exception e) {
      System.out.println(e.toString());
    }
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
        b = bmpShortArray[pos++];
        g = bmpShortArray[pos++];
        r = bmpShortArray[pos++];
        // Default to DRV_COLORMODE_RGB565

        nColRaw  = (((r & 0xF8) >> 3) << 11); // Mask: 1111 1000 0000 0000
        nColRaw |= (((g & 0xFC) >> 2) <<  5); // Mask: 0000 0111 1110 0000
        nColRaw |= (((b & 0xF8) >> 3) <<  0); // Mask: 0000 0000 0001 1111
        decodedImage[idx++] = (short) (nColRaw);
      } // end pixel
    } // end scanline
  }

  /**
   * fromRGB565
   * Convert RGB565 color to Java Color object
   * 
   * @param n RGB565 as integer
   * @return Color object
   */
  public static Color fromRGB565(int n) {
    int r = (n & 0xFF0000) >> 16;
    int g = (n & 0xFF00) >> 8;
    int b = (n & 0xFF);
    return new Color(r,g,b);
  }
  
  /**
   * toRGB565
   * Convert Java Color to RGB565 color
   *
   * @param the color to convert
   * @return RGB565 format color
   */
  public static int toRGB565(Color color) {
    //RGB888
    int r = color.getRed();
    int g = color.getGreen();
    int b = color.getBlue();
    
    //Converting to RGB565
    int nColRaw  = (((r & 0xF8) >> 3) << 11); // Mask: 1111 1000 0000 0000
    nColRaw |= (((g & 0xFC) >> 2) <<  5); // Mask: 0000 0111 1110 0000
    nColRaw |= (((b & 0xF8) >> 3) <<  0); // Mask: 0000 0000 0001 1111

    return nColRaw;
  }
  
  public void image2File(BufferedImage image, File file, boolean bMonochrome) throws IOException {
    if (!bMonochrome) {
      BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
      newImage.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);
      ImageIO.write(newImage, "BMP", file);
    } else {
      ImageIO.write(image, "BMP", file);
    }
  }
}

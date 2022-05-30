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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * The Class CArrayOutputStream.
 * This class will output byte data as literal hexadecimal constants.
 * The output can be either little or big endian format and will
 * look something like a hexdump of a binary file.
 * 
 * You can write normal strings using writeString() function.
 * 
 * @author Paul Conti
 * 
 */
public class CArrayOutputStream {
  
  /** The Constant MAXBYTES. */
  private final static int MAXITEMS = 16;
  
  /** The fw. */
  private FileWriter  fw;
  
  /** The pw. */
  private PrintWriter pw;
  
  /** The line len. */
  private int lineLen;
  
  /** The pos. */
  private int pos;
  
  /** The b little endian. */
  private boolean bLittleEndian;
  
  ShortBuffer byteBuffer;
  
  /**
   * Instantiates a new c array output stream.
   *
   * @param file
   *          the file
   * @param bLittleEndian
   *          the b little endian
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public CArrayOutputStream(File file, boolean bLittleEndian) throws IOException {
    fw = new FileWriter(file);
    pw = new PrintWriter(fw);
    this.bLittleEndian = bLittleEndian;
    lineLen = 0;
    pos = 0;
  }
  
  /**
   * Stream array.
   *
   * @param data
   *          the data
   */
  public void streamArray(int[] data) {
    for(int i=0; i<data.length; i++)
    {
      writeShort(data[i], (i+1==data.length));
    }
    flush();
  }

  /**
   * Stream array.
   *
   * @param data
   *          the data
   */
  public void streamArray(byte[] data) {
    int x;
    for(x=0; x<data.length; x++)
    {
      writeByte(data[x], (x+1==data.length));
    }
    flush();
  }

  /**
   * Stream byte.
   *
   * @param b
   *          the b
   */
  private void writeByte(byte b, boolean bLast) {
    if (bLast) {
      pw.printf("0x%02X  ",b);
    } else {
      pw.printf("0x%02X, ",b);
    }
    lineLen++;
    pos++;
    if (lineLen >= MAXITEMS) {
      pw.printf("    // 0x%04X (%d) pixels\n",pos,pos);
      lineLen = 0;
    }
  }

  /**
   * Write string.
   *
   * @param s
   *          the s
   */
  public void writeString(String s) {
    pw.printf("%s", s);
  }

  /**
   * Write short.
   *
   * @param v
   *          the v
   */
  public void writeShort(int data, boolean bLast) {
    short v = Integer.valueOf(data).shortValue();
    if (!bLittleEndian) {
      v = convertShortBigEndian(v);
    }
    if (bLast) {
      pw.printf("0x%04X  ",v);
    } else {
      pw.printf("0x%04X, ",v);
    }
    lineLen++;
    pos++;
    if (lineLen >= MAXITEMS) {
      pw.printf("    // 0x%04X (%d) pixels\n",pos,pos);
      lineLen = 0;
    }
  }

  private short convertShortBigEndian (short inValue) {
    byte tmpValue [] = shortToArray(inValue);
    return shortFromArray(tmpValue);
 }

  public short shortFromArray(byte[] payload){
    ByteBuffer buffer = ByteBuffer.wrap(payload);
    buffer.order(ByteOrder.BIG_ENDIAN);
    return buffer.getShort();
  }

  public byte[] shortToArray(short value){
      ByteBuffer buffer = ByteBuffer.allocate(2);
      buffer.order(ByteOrder.BIG_ENDIAN);
      buffer.putShort(value);
      buffer.flip();
      return buffer.array();
  }
  
  /**
   * Flush.
   *
   * @param b
   *          the b
   */
  public void flush() {
    if (lineLen != 0) {
      int leftOver = MAXITEMS - lineLen;
      for (int i=0; i<leftOver; i++)
        pw.printf("        ");
      pw.printf("    // 0x%04X (%d) pixels\n",pos,pos);
    }
    pw.printf("};\n");
  }
  
  /**
   * Close.
   *
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void close() throws IOException {
    pw.close();
    fw.close();
  }

}

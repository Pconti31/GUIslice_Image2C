/**
 *
 * The MIT License
 *
 * Copyright 2018-2020 Paul Conti
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

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The Class LittleEndianDataOutputStream.
 * 
 * @author Paul Conti
 * 
 */
public class LittleEndianDataOutputStream extends OutputStream implements DataOutput {

  /** The out. */
  private OutputStream out;
  
  /** The d. */
  private DataOutputStream d;

  /**
   * Instantiates a new little endian data output stream.
   *
   * @param out
   *          the out
   */
  public LittleEndianDataOutputStream(OutputStream out) {
    this.out = out;
    this.d = new DataOutputStream(out);
  }
 
  /**
   * writeBoolean
   *
   * @see java.io.DataOutput#writeBoolean(boolean)
   */
  @Override
  public void writeBoolean(boolean v) throws IOException {
    d.writeBoolean(v);
  }

  /**
   * writeByte
   *
   * @see java.io.DataOutput#writeByte(int)
   */
  @Override
  public void writeByte(int v) throws IOException {
    d.writeByte(v);
  }

  /**
   * writeShort
   *
   * @see java.io.DataOutput#writeShort(int)
   */
  @Override
  public void writeShort(int v) throws IOException {
    d.writeByte(0xFF & v);
    d.writeByte(0xFF & (v >> 8));
  }

  /**
   * writeChar
   *
   * @see java.io.DataOutput#writeChar(int)
   */
  @Override
  public void writeChar(int v) throws IOException {
    d.writeByte(0xFF & v);
    d.writeByte(0xFF & (v >> 8));
  }

  /**
   * writeInt
   *
   * @see java.io.DataOutput#writeInt(int)
   */
  @Override
  public void writeInt(int v) throws IOException {
    d.writeByte(0xff & v);
    d.writeByte(0xff & (v >> 8));
    d.writeByte(0xff & (v >> 16));
    d.writeByte(0xff & (v >> 24));
  }

  /**
   * writeLong
   *
   * @see java.io.DataOutput#writeLong(long)
   */
  @Override
  public void writeLong(long v) throws IOException {
    d.writeByte((int) (0xff & v));
    d.writeByte((int) (0xff & (v << 8)));
    d.writeByte((int) (0xff & (v << 16)));
    d.writeByte((int) (0xff & (v << 24)));
    d.writeByte((int) (0xff & (v << 32)));
    d.writeByte((int) (0xff & (v << 40)));
    d.writeByte((int) (0xff & (v << 48)));
    d.writeByte((int) (v << 56)); 
  }

  /**
   * writeFloat
   *
   * @see java.io.DataOutput#writeFloat(float)
   */
  @Override
  public void writeFloat(float v) throws IOException {
    writeInt(Float.floatToIntBits(v));
  }

  /**
   * writeDouble
   *
   * @see java.io.DataOutput#writeDouble(double)
   */
  @Override
  public void writeDouble(double v) throws IOException {
    writeLong(Double.doubleToLongBits(v));
  }

  /**
   * writeBytes
   *
   * @see java.io.DataOutput#writeBytes(java.lang.String)
   */
  @Override
  public void writeBytes(String s) throws IOException {
    d.writeBytes(s);
  }

  /**
   * writeChars
   *
   * @see java.io.DataOutput#writeChars(java.lang.String)
   */
  @Override
  public void writeChars(String s) throws IOException {
    for (int i = 0; i < s.length(); i++) {
      writeChar(s.charAt(i));
    }
  }

  /**
   * writeUTF
   *
   * @see java.io.DataOutput#writeUTF(java.lang.String)
   */
  @Override
  public void writeUTF(String s) throws IOException {
    d.writeUTF(s);
  }

  /**
   * write
   *
   * @see java.io.OutputStream#write(int)
   */
  @Override
  public void write(int b) throws IOException {
    out.write(b);
  }
  
  /**
   * close
   *
   * @see java.io.OutputStream#close()
   */
  @Override
  public void close() {
    try {
      this.d.close();
      out.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}

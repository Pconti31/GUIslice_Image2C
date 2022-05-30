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

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The Class LittleEndianDataInputStream.
 */
public class LittleEndianDataInputStream extends InputStream implements DataInput {

  /** The in. */
  private InputStream in;
  
  /** The d. */
  private DataInputStream d;
  
  /** The w. */
  private byte[] w;

  /**
   * Instantiates a new little endian data input stream.
   *
   * @param in
   *          the in
   */
  public LittleEndianDataInputStream(InputStream in) {
    this.in = in;
    this.d = new DataInputStream(in);
    w = new byte[8];
  }
 
  /**
   * available
   *
   * @see java.io.InputStream#available()
   */
  public int available() throws IOException {
    return d.available();
  }
 
  /**
   * readFully
   *
   * @see java.io.DataInput#readFully(byte[])
   */
  @Override
  public void readFully(byte[] b) throws IOException {
    d.readFully(b, 0, b.length);
  }

  /**
   * readFully
   *
   * @see java.io.DataInput#readFully(byte[], int, int)
   */
  @Override
  public void readFully(byte[] b, int off, int len) throws IOException {
    d.readFully(b, off, len);
  }

  /**
   * skipBytes
   *
   * @see java.io.DataInput#skipBytes(int)
   */
  @Override
  public int skipBytes(int n) throws IOException {
    return d.skipBytes(n);
  }

  /**
   * readBoolean
   *
   * @see java.io.DataInput#readBoolean()
   */
  @Override
  public boolean readBoolean() throws IOException {
    return d.readBoolean();
  }

  /**
   * readByte
   *
   * @see java.io.DataInput#readByte()
   */
  @Override
  public byte readByte() throws IOException {
    return d.readByte();
  }

  /**
   * readUnsignedByte
   *
   * @see java.io.DataInput#readUnsignedByte()
   */
  @Override
  public int readUnsignedByte() throws IOException {
    return d.readUnsignedByte();
  }

  /**
   * readShort
   *
   * @see java.io.DataInput#readShort()
   */
  @Override
  public short readShort() throws IOException {
    d.readFully(w, 0, 2);
    return (short)(
        (w[1]&0xff) << 8 |
        (w[0]&0xff));
  }

  /**
   * readUnsignedShort
   *
   * @see java.io.DataInput#readUnsignedShort()
   */
  @Override
  public int readUnsignedShort() throws IOException {
    d.readFully(w, 0, 2);
    return (
        (w[1]&0xff) << 8 |
        (w[0]&0xff));
  }

  /**
   * readChar
   *
   * @see java.io.DataInput#readChar()
   */
  @Override
  public char readChar() throws IOException {
    d.readFully(w, 0, 2);
    return (char) (
        (w[1]&0xff) << 8 |
        (w[0]&0xff));
  }

  /**
   * readInt
   *
   * @see java.io.DataInput#readInt()
   */
  @Override
  public int readInt() throws IOException {
    d.readFully(w, 0, 4);
    return
    (w[3])      << 24 |
    (w[2]&0xff) << 16 |
    (w[1]&0xff) <<  8 |
    (w[0]&0xff);
  }

  /**
   * readLong
   *
   * @see java.io.DataInput#readLong()
   */
  @Override
  public long readLong() throws IOException {
    d.readFully(w, 0, 8);
    return
    (long)(w[7])      << 56 | 
    (long)(w[6]&0xff) << 48 |
    (long)(w[5]&0xff) << 40 |
    (long)(w[4]&0xff) << 32 |
    (long)(w[3]&0xff) << 24 |
    (long)(w[2]&0xff) << 16 |
    (long)(w[1]&0xff) <<  8 |
    (long)(w[0]&0xff);
  }

  /**
   * readFloat
   *
   * @see java.io.DataInput#readFloat()
   */
  @Override
  public float readFloat() throws IOException {
    return Float.intBitsToFloat(readInt());
  }

  /**
   * readDouble
   *
   * @see java.io.DataInput#readDouble()
   */
  @Override
  public double readDouble() throws IOException {
    return Double.longBitsToDouble(readLong());
  }

  /**
   * readLine
   *
   * @see java.io.DataInput#readLine()
   */
  @SuppressWarnings("deprecation")
  @Override
  public String readLine() throws IOException {
    return d.readLine();
  }

  /**
   * readUTF
   *
   * @see java.io.DataInput#readUTF()
   */
  @Override
  public String readUTF() throws IOException {
    return d.readUTF();
  }

  /**
   * read
   *
   * @see java.io.InputStream#read()
   */
  @Override
  public int read() throws IOException {
    return in.read();
  }

  /**
   * close
   *
   * @see java.io.InputStream#close()
   */
  public final void close() throws IOException {
    d.close();
  }
}

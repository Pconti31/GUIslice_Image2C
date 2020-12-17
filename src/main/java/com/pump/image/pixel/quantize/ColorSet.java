/**
 * This software is released as part of the Pumpernickel project.
 * 
 * All com.pump resources in the Pumpernickel project are distributed under the
 * MIT License:
 * https://raw.githubusercontent.com/mickleness/pumpernickel/master/License.txt
 * 
 * More information about the Pumpernickel project is available here:
 * https://mickleness.github.io/pumpernickel/
 *
 * Modified by Paul Conti for the GUIslice project 2019-04-06
 * See: 
 * https://github.com/ImpulseAdventure/GUIslice
 *
 * The particular changes:
 * - removed unsupported bit depths of 2,3,5,6,7,
 */
package com.pump.image.pixel.quantize;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.pump.image.pixel.BufferedImageIterator;
import com.pump.image.pixel.IntARGBConverter;

/**
 * This class stores information about the frequency of colors. This assumes all
 * colors are opaque.
 * 
 */
public class ColorSet implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Sort RGB values in order of frequency. This has to be relative to a
	 * particular <code>ColorSet</code>.
	 */
	public static class FrequencyComparator implements Comparator<Integer> {
		ColorSet set;
		boolean ascending;

		/**
		 * 
		 * @param set
		 *            the color set to sort.
		 * @param ascending
		 *            if true then this will sort in increasing frequency.
		 *            (Surprisingly: this yields the best results for gif color
		 *            tables and LZW compression.)
		 */
		protected FrequencyComparator(ColorSet set, boolean ascending) {
			this.set = set;
			this.ascending = ascending;
		}

		public int compare(Integer rgb1, Integer rgb2) {
			int p1 = set.tree.get(rgb1);
			int p2 = set.tree.get(rgb2);
			if (ascending) {
				if (p1 < p2) {
					return -1;
				} else if (p1 > p2) {
					return 1;
				}
			} else {
				if (p1 > p2) {
					return -1;
				} else if (p1 < p2) {
					return 1;
				}
			}
			return rgb1 - rgb2;
		}
	}

	/**
	 * Sorts RGB values in order of increasing red, green or blue masks.
	 */
	public static class RGBChannelComparator implements Comparator<Integer> {
		int offset1, offset2, offset3;

		/**
		 * 
		 * @param offset1
		 *            the first offset to sort by. For RGB values, the red
		 *            channel has an offset of 16, the green has an offset of 8,
		 *            and the blue has an offset of zero.
		 * @param offset2
		 *            If the first channels match: this is the second criteria
		 *            this comparator evaluates with.
		 * @param offset3
		 *            If the first two channels match: this is the third
		 *            criteria this comparator evaluates with.
		 */
		public RGBChannelComparator(int offset1, int offset2, int offset3) {
			this.offset1 = offset1;
			this.offset2 = offset2;
			this.offset3 = offset3;
		}

		public int compare(Integer o1, Integer o2) {
			int t = doCompare(o1, o2, offset1);
			if (t == 0)
				t = doCompare(o1, o2, offset2);
			if (t == 0)
				t = doCompare(o1, o2, offset3);
			if (t != 0)
				return t;

			// they should be equal at this point,
			// so it would also make sense to return zero.

			// But just to be extra safe:
			return o2 - o1;
		}

		private int doCompare(int i1, int i2, int mask) {
			i1 = (i1 >> mask) & 0xff;
			i2 = (i2 >> mask) & 0xff;
			if (i1 < i2) {
				return -1;
			} else if (i1 > i2) {
				return 1;
			}
			return 0;
		}
	}

	private static Comparator<Integer> redComparator = new RGBChannelComparator(
			16, 8, 0);

	/** Map RGB values to the number of occurrences. */
	TreeMap<Integer, Integer> tree = new TreeMap<Integer, Integer>(
			redComparator);

	/**
	 * A cached value of the number of pixels. This is equivalent to the sum of
	 * all the values in <code>tree</code>.
	 */
	private long pixelCount = 0;

	/** Create an empty ColorSet. */
	public ColorSet() {
	}

	/**
	 * Create a ColorSet that records every pixel in the argument provided.
	 * 
	 * @param i
	 *            the image to add to this ColorSet.
	 */
	public ColorSet(BufferedImage i) {
		this();
		addColors(i);
	}

	/**
	 * Reconstitute a ColorSet from a String.
	 * 
	 * @param s
	 *            a serialized ColorSet from <code>colorSet.toString()</code>.
	 */
	public ColorSet(String s) {
		if (s.startsWith(ColorSet.class.getName() + "[") == false) {
			throw new IllegalArgumentException(
					"This set did not have the correct header.");
		}
		int i = 23;
		int l = s.length();
		int i2, i3;
		int r, g, b, count;
		while (i < l) {
			i2 = s.indexOf('(', i) + 1;
			i3 = s.indexOf(',', i2);
			r = Integer.parseInt(s.substring(i2, i3));
			i2 = i3 + 1;
			i3 = s.indexOf(',', i2);
			g = Integer.parseInt(s.substring(i2, i3));
			i2 = i3 + 1;
			i3 = s.indexOf(')', i2);
			b = Integer.parseInt(s.substring(i2, i3));
			if (s.charAt(i3 + 1) == 'x') {
				i2 = i3 + 2;
				i3 = s.indexOf(',', i2);
				if (i3 == -1) {
					i3 = s.indexOf(']', i2);
				}
				try {
					count = Integer.parseInt(s.substring(i2, i3));
				} catch (RuntimeException e) {
					System.err.println("i2=" + i2 + " i3=" + i3 + " l=" + l);
					throw e;
				}
			} else {
				count = 1;
			}
			int rgb = r << 16 + (g << 8) + b;
			tree.put(rgb, count);
			i = i3 + 1;
		}
	}

	/**
	 * @return a map of RGB colors to the number of occurrences of each color.
	 * 
	 * @param cloneData
	 *            if true then the inner data in this object will be safely
	 *            cloned. If false then this will return a pointer to the data
	 *            that backs this object. This may help the efficiency of some
	 *            algorithms, but of course it needs to be done with caution.
	 */
	public Map<Integer, Integer> getRGBtoFrequencyMap(boolean cloneData) {
		if (cloneData) {
			TreeMap<Integer, Integer> newCopy = new TreeMap<Integer, Integer>(
					redComparator);
			newCopy.putAll(tree);
			return newCopy;
		}
		return tree;
	}

	/**
	 * @param c
	 *            the color to search for.
	 * @return the number of times this color is represented.
	 */
	public int getOccurrences(Color c) {
		return getOccurrences(c.getRed(), c.getGreen(), c.getBlue());
	}

	/**
	 * @param red
	 *            the red component of the color to search for.
	 * @param green
	 *            the green component of the color to search for.
	 * @param blue
	 *            the blue component of the color to search for.
	 * @return the number of times this color is represented.
	 */
	public int getOccurrences(int red, int green, int blue) {
		int rgb = (red << 16) + (green << 8) + blue;
		Integer count = tree.get(rgb);
		if (count == null)
			return 0;
		return count;
	}

	public boolean equals(Object obj) {
		if ((obj instanceof ColorSet) == false)
			return false;
		return equals((ColorSet) obj, false);
	}

	/**
	 * This checks to see if this <code>ColorSet</code> is equal to another.
	 * 
	 * @param l
	 *            another ColorSet to compare against
	 * @param compareColorsOnly
	 *            if this is <code>true</code> then this method <i>only</i>
	 *            checks to see that two <code>ColorSets</code> contain the same
	 *            colors, not necessarily the same pixel count.
	 * @return true if two sets are equal
	 */
	public synchronized boolean equals(ColorSet l, boolean compareColorsOnly) {
		synchronized (l) {
			if (l.tree.size() != tree.size())
				return false;
			if (compareColorsOnly) {
				return tree.keySet().equals(l.tree.keySet());
			}
			return tree.equals(l.tree);
		}
	}

	public String toString() {
		StringWriter s = new StringWriter(tree.size() * 18);
		try {
			write(s);
		} catch (IOException e) {
			// this is impossible, yes?
			throw new RuntimeException(e);
		}
		return s.toString();
	}

	public synchronized void write(Writer out) throws IOException {
		out.write(ColorSet.class.getName() + "[");
		Iterator<Integer> keyIter = tree.keySet().iterator();
		while (keyIter.hasNext()) {
			int rgb = keyIter.next();
			int r = (rgb >> 16) & 0xff;
			int g = (rgb >> 8) & 0xff;
			int b = (rgb >> 0) & 0xff;
			int count = tree.get(rgb);

			out.write('(');
			out.write(r + "");
			out.write(',');
			out.write(g + "");
			out.write(',');
			out.write(b + "");
			out.write(')');
			if (count > 1) {
				out.write('x');
				out.write(count + "");
			}
			if (keyIter.hasNext()) {
				out.write(',');
			}
		}
		out.write(']');
	}

	/**
	 * A a color to this set.
	 * 
	 * @param r
	 *            the red component of this color from [0, 255]
	 * @param g
	 *            the green component of this color from [0, 255]
	 * @param b
	 *            the blue component of this color from [0, 255]
	 * @param occurrence
	 *            the number of occurrences of this color (must be greater than
	 *            zero)
	 */
	public synchronized void addColor(int r, int g, int b, int occurrence) {
		if (occurrence <= 0)
			throw new IllegalArgumentException(
					"occurrence must be greater than 0");

		if (r < 0 || r > 255)
			throw new IllegalArgumentException("r must be between 0 and 255");
		if (g < 0 || g > 255)
			throw new IllegalArgumentException("g must be between 0 and 255");
		if (b < 0 || b > 255)
			throw new IllegalArgumentException("b must be between 0 and 255");
		int rgb = (r << 16) + (g << 8) + b;

		Integer count = tree.get(rgb);
		if (count == null) {
			tree.put(rgb, occurrence);
		} else {
			tree.put(rgb, count + occurrence);
		}

		pixelCount += occurrence;
	}

	/**
	 * @return the number of colors represented in this <code>ColorSet</code>.
	 */
	public synchronized int getColorCount() {
		return tree.size();
	}

	/**
	 * @return the number of pixels represented in this <code>ColorSet</code>.
	 */
	public synchronized long getPixelCount() {
		return pixelCount;
	}
	
	/**
	 * This creates an <code>IndexColorModel</code>. This method does not
	 * perform any color reduction: it just converts this data set to a
	 * <code>IndexColorModel</code>.
	 * <P>
	 * This method will throw an exception if this set represents over 256
	 * colors (or if this set is over 255 colors and
	 * <code>includeTransparentPixel</code> is <code>true</code>.)
	 * 
	 * @param includeTransparentPixel
	 *            if this is true, then the first color in this model will be a
	 *            transparent pixel.
	 * @param optimizeForGifs
	 *            if this is true, then the set will be sorted in a way that
	 *            generally helps GIF compression (the most frequently used
	 *            colors have the most 0's in them, and if possible the color
	 *            model is populated with extra shades of the most important
	 *            colors). If this is false, then this set is simply sorted by
	 *            descending frequency of color.
	 *            <P>
	 *            Regardless of this variable, if
	 *            <code>includeTransparentPixel</code> is true, then this the
	 *            first index is always the transparent pixel.
	 * @return a new <code>IndexColorModel</code> representing this set.
	 */
	public synchronized IndexColorModel createIndexColorModel(
			boolean includeTransparentPixel, boolean optimizeForGifs) {
		int colorCount = getColorCount();
		if (colorCount > 255 && includeTransparentPixel)
			throw new IllegalStateException(
					"There are too many colors ("
							+ colorCount
							+ ") to make an IndexColorModel (max is 255 + 1 transparent pixel)");
		if (colorCount > 256)
			throw new IllegalStateException("There are too many colors ("
					+ colorCount + ") to make an IndexColorModel (max is 256)");

		Integer[] allColors = tree.keySet().toArray(new Integer[tree.size()]);

		int offset = includeTransparentPixel ? 1 : 0;
		byte[] r;
		byte[] g;
		byte[] b;
		r = new byte[colorCount + offset];
		g = new byte[colorCount + offset];
		b = new byte[colorCount + offset];

		for (int a = 0; a < allColors.length; a++) {
			r[a + offset] = (byte) ((int) ((allColors[a] >> 16) & 0xff));
			g[a + offset] = (byte) ((int) ((allColors[a] >> 8) & 0xff));
			b[a + offset] = (byte) ((int) ((allColors[a] >> 0) & 0xff));
		}
		if (includeTransparentPixel) {
			return new IndexColorModel(8, allColors.length + 1, r, g, b, 0);
		} else {
		  return new IndexColorModel(8, allColors.length, r, g, b);
		}
	}

	/**
	 * Add all the pixels in this iterator that are more than 50% opaque. This
	 * does not account for premultiplied alpha.
	 * 
	 * @param i
	 *            the image to process.
	 */
	public void addColors(BufferedImage i) {
		IntARGBConverter g = new IntARGBConverter(BufferedImageIterator.get(i));
		addColors(g);
	}

	/**
	 * Add all the pixels in this iterator that are more than 50% opaque. This
	 * does not account for premultiplied alpha.
	 * 
	 * @param i
	 *            the pixel data to process.
	 */
	public void addColors(IntARGBConverter i) {

		int w = i.getWidth();

		int r, g, b, alpha;
		int[] t = new int[i.getMinimumArrayLength()];
		while (i.isDone() == false) {
			i.next(t);
			for (int a = 0; a < w; a++) {
				alpha = ((t[a] >> 24) & 0xff);
				if (alpha > 128) {
					r = ((t[a] >> 16) & 0xff);
					g = ((t[a] >> 8) & 0xff);
					b = ((t[a]) & 0xff);
					addColor(r, g, b, 1);
				}
			}
		}
	}

	/**
	 * Add one occurrence of this of this color.
	 * 
	 * @param c
	 *            the color to add
	 */
	public void addColor(Color c) {
		addColor(c.getRed(), c.getGreen(), c.getBlue(), 1);
	}

	/**
	 * Add one occurrence of this of this color.
	 * 
	 * @param r
	 *            the red component of the color to add.
	 * @param g
	 *            the green component of the color to add.
	 * @param b
	 *            the blue component of the color to add.
	 */
	public void addColor(int r, int g, int b) {
		addColor(r, g, b, 1);
	}

	/**
	 * Add all the color data from the argument into this ColorSet.
	 * 
	 * @param l
	 *            the incoming ColorSet to add to this set.
	 */
	public synchronized void addColors(ColorSet l) {
		for (Integer rgb : l.tree.keySet()) {
			int count = l.tree.get(rgb);
			int r = (rgb >> 16) & 0xff;
			int g = (rgb >> 8) & 0xff;
			int b = (rgb >> 0) & 0xff;
			addColor(r, g, b, count);
		}
	}

	/**
	 * @return all the colors in this <code>ColorSet</code>.
	 * 
	 * @param prependTransparentPixel
	 *            true if the first color returned should be fully transparent.
	 */
	public synchronized Color[] getColors(boolean prependTransparentPixel) {
		int extra = prependTransparentPixel ? 1 : 0;
		Color[] array = new Color[tree.size() + extra];
		int ctr = 0;
		if (prependTransparentPixel) {
			array[ctr++] = new Color(0, 0, 0, 0);
		}
		for (Integer rgb : tree.keySet()) {
			int r = (rgb >> 16) & 0xff;
			int g = (rgb >> 8) & 0xff;
			int b = (rgb >> 0) & 0xff;
			array[ctr++] = new Color(r, g, b);
		}

		return array;
	}

	/**
	 * @return all the colors in this <code>ColorSet</code>.
	 */
	public Color[] getColors() {
		return getColors(false);
	}
}
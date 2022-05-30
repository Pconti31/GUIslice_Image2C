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

package image2C.views;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 * <p>
 * ColoredThumbSliderUI A Custom Java L{@literal &}F implementation 
 * of SliderUI with a Colored thumb control.
 * </p>
 * <p>
 * This code has circulated on various forums for at least 12 years.
 * I have no idea who is the original author.
 * </p>
 *
 */
public class ColoredThumbSliderUI extends BasicSliderUI {
  /** Slider <code>JSlider</code>. */
  private JSlider slider;

  /** thumb color <code>Color</code>. */
  private Color thumbColor;

  /**
   * Instantiates a new colored thumb slider UI.
   *
   * @param slider
   *          the slider
   * @param color
   *          the color
   */
  public ColoredThumbSliderUI(JSlider slider, Color color) {
    super(slider);
    this.slider = slider;
    thumbColor = color;
  }

  /**
   * paint
   *
   * @see javax.swing.plaf.basic.BasicSliderUI#paint(java.awt.Graphics, javax.swing.JComponent)
   */
  @Override
  public void paint(Graphics g, JComponent c) {
    recalculateIfInsetsChanged();
    recalculateIfOrientationChanged();
    Rectangle clip = g.getClipBounds();

    if (slider.getPaintTrack() && clip.intersects(trackRect)) {
      paintTrack(g);
    }
    if (slider.getPaintTicks() && clip.intersects(tickRect)) {
      paintTicks(g);
    }
    if (slider.getPaintLabels() && clip.intersects(labelRect)) {
      paintLabels(g);
    }
    if (slider.hasFocus() && clip.intersects(focusRect)) {
      paintFocus(g);
    }
    if (clip.intersects(thumbRect)) {
      Color savedColor = slider.getBackground();
      slider.setBackground(thumbColor);
      paintThumb(g);
      slider.setBackground(savedColor);
    }
  }

}

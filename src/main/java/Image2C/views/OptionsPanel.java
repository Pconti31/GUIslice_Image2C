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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.text.DefaultFormatter;
import javax.swing.UIManager;

public class OptionsPanel extends JPanel {
  private static final long serialVersionUID = 1L;

  private final JLabel lblWidth = new JLabel("Width:");
  private final JLabel lblColors = new JLabel("# of Colors:");
  final JLabel lblTransparentPixelColor = new JLabel("Change Transparent Pixels to:");
  final JLabel lblCurrentForeground = new JLabel("Current Foreground Color:");
  final JLabel lblNewForeground = new JLabel("Monochrome Output:");
  JButton btnTransColor;
  JButton btnNewForeground;
  JButton btnCurrentForeground;
  JTextField txtOutputFile;
  JTextField txtCArray;
  JFormattedTextField txtWidth;
  JFormattedTextField txtHeight;
  JFormattedTextField txtNumColors;
  JCheckBox cbLittleEndian;
  JCheckBox cbCArrayFlash;
  private NumberFormat amountFormat;

  /**
   * Create the panel.
   */
  public OptionsPanel() {
    setPreferredSize(new Dimension(250, 500));
    
    amountFormat = NumberFormat.getIntegerInstance();

    JLabel lblFileOptions = new JLabel("File Options");
    lblFileOptions.setFont(new Font("SansSerif", Font.BOLD, 14));
    
    JLabel lblOutputFile = new JLabel("Output File");
    lblOutputFile.setFont(new Font("SansSerif", Font.PLAIN, 12));
    
    txtOutputFile = new JFormattedTextField(new DefaultFormatter());
    txtOutputFile.setFont(new Font("SansSerif", Font.PLAIN, 12));
    txtOutputFile.setColumns(12);
    txtOutputFile.setActionCommand("outputfile");
        
   
    JLabel lblCArray = new JLabel("C Array Name");
    lblCArray.setFont(new Font("SansSerif", Font.PLAIN, 12));
    
    txtCArray = new JFormattedTextField(new DefaultFormatter());
    txtCArray.setFont(new Font("SansSerif", Font.PLAIN, 12));
    txtCArray.setColumns(12);
    txtCArray.setActionCommand("carray");
    
    cbCArrayFlash = new JCheckBox("C Array as PROGMEM Storage?");
    cbCArrayFlash.setFont(new Font("SansSerif", Font.PLAIN, 12));
    
    cbLittleEndian = new JCheckBox("Output Little Endian?");
    cbLittleEndian.setFont(new Font("SansSerif", Font.PLAIN, 12));
    
    JLabel lblBMPDetails = new JLabel("Image Details");
    lblBMPDetails.setFont(new Font("SansSerif", Font.BOLD, 14));
    lblWidth.setFont(new Font("SansSerif", Font.PLAIN, 12));
    
    txtWidth = new JFormattedTextField(amountFormat);
    txtWidth.setEditable(false);
    txtWidth.setFont(new Font("SansSerif", Font.PLAIN, 12));
    txtWidth.setColumns(6);
    
    JLabel lblHeight = new JLabel("Height:");
    lblHeight.setFont(new Font("SansSerif", Font.PLAIN, 12));
    
    txtHeight = new JFormattedTextField(amountFormat);
    txtHeight.setEditable(false);
    txtHeight.setFont(new Font("SansSerif", Font.PLAIN, 12));
    txtHeight.setColumns(6);
    lblColors.setFont(new Font("SansSerif", Font.PLAIN, 12));
    
    txtNumColors = new JFormattedTextField(amountFormat);
    txtNumColors.setEditable(false);
    txtNumColors.setFont(new Font("SansSerif", Font.PLAIN, 12));
    txtNumColors.setColumns(6);
    
    JLabel lblImageOptions = new JLabel("Image Options");
    lblImageOptions.setFont(new Font("SansSerif", Font.BOLD, 14));

    lblTransparentPixelColor.setFont(new Font("SansSerif", Font.PLAIN, 12));
    lblTransparentPixelColor.setVisible(false);
    
    btnTransColor = new JButton("");
    btnTransColor.setBackground(ImageApp.getTransparentColor());
    btnTransColor.setPreferredSize(new Dimension(30, 30));
    btnTransColor.setBorder(UIManager.getBorder("Button.border"));
    btnTransColor.setActionCommand("transcolor");
    btnTransColor.setEnabled(false);
    btnTransColor.setVisible(false);
    
    lblCurrentForeground.setFont(new Font("SansSerif", Font.PLAIN, 12));
    lblCurrentForeground.setVisible(false);
    
    btnCurrentForeground = new JButton("");
    btnCurrentForeground.setBackground(ImageApp.getCurrentForeground());
    btnCurrentForeground.setPreferredSize(new Dimension(30, 30));
    btnCurrentForeground.setBorder(UIManager.getBorder("Button.border"));
    btnCurrentForeground.setActionCommand("current_color");
    btnCurrentForeground.setVisible(false);
    
    lblNewForeground.setFont(new Font("SansSerif", Font.PLAIN, 12));
    lblNewForeground.setVisible(false);
    
    btnNewForeground = new JButton("");
    btnNewForeground.setBackground(ImageApp.getNewForeground());
    btnNewForeground.setPreferredSize(new Dimension(30, 30));
    btnNewForeground.setBorder(UIManager.getBorder("Button.border"));
    btnNewForeground.setActionCommand("new_color");
    btnNewForeground.setEnabled(false);
    btnNewForeground.setVisible(false);
    
    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
      groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
          .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
            .addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
              .addGap(10)
              .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addComponent(lblFileOptions)
                .addComponent(cbCArrayFlash)
                .addComponent(cbLittleEndian)
                .addGroup(groupLayout.createSequentialGroup()
                  .addPreferredGap(ComponentPlacement.RELATED)
                  .addComponent(lblImageOptions))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                    .addComponent(lblCArray)
                    .addGroup(groupLayout.createSequentialGroup()
                      .addPreferredGap(ComponentPlacement.RELATED)
                      .addComponent(lblOutputFile)))
                  .addPreferredGap(ComponentPlacement.UNRELATED)
                  .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                    .addComponent(txtOutputFile, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCArray, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addGroup(groupLayout.createSequentialGroup()
                  .addPreferredGap(ComponentPlacement.RELATED)
                  .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                    .addGroup(groupLayout.createSequentialGroup()
                      .addComponent(lblColors)
                      .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                      .addComponent(txtNumColors, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(groupLayout.createSequentialGroup()
                      .addComponent(lblWidth)
                      .addPreferredGap(ComponentPlacement.RELATED, 111, Short.MAX_VALUE)
                      .addComponent(txtWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
                .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
                  .addGroup(groupLayout.createSequentialGroup()
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(lblCurrentForeground)
                    .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCurrentForeground, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                  .addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
                    .addComponent(lblTransparentPixelColor)
                    .addPreferredGap(ComponentPlacement.UNRELATED)
                    .addComponent(btnTransColor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))))
            .addGroup(groupLayout.createSequentialGroup()
              .addContainerGap()
              .addComponent(lblHeight)
              .addGap(113)
              .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addComponent(btnNewForeground, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(txtHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
          .addContainerGap(16, Short.MAX_VALUE))
        .addGroup(groupLayout.createSequentialGroup()
          .addContainerGap()
          .addComponent(lblBMPDetails)
          .addContainerGap(146, Short.MAX_VALUE))
        .addGroup(groupLayout.createSequentialGroup()
          .addContainerGap()
          .addComponent(lblNewForeground)
          .addContainerGap(145, Short.MAX_VALUE))
    );
    groupLayout.setVerticalGroup(
      groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
          .addGap(12)
          .addComponent(lblFileOptions)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblOutputFile)
            .addComponent(txtOutputFile, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addGap(9)
          .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblCArray)
            .addComponent(txtCArray, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addGap(10)
          .addComponent(cbCArrayFlash)
          .addGap(3)
          .addComponent(cbLittleEndian)
          .addGap(10)
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
              .addComponent(lblNewForeground)
              .addGap(18)
              .addComponent(lblBMPDetails))
            .addComponent(btnNewForeground, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
            .addComponent(txtHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(lblHeight))
          .addGap(18)
          .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
            .addComponent(txtWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(lblWidth))
          .addGap(18)
          .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
            .addComponent(txtNumColors, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(lblColors))
          .addGap(18)
          .addComponent(lblImageOptions)
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addComponent(lblTransparentPixelColor)
            .addComponent(btnTransColor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addGap(21)
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addComponent(btnCurrentForeground, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(lblCurrentForeground))
          .addContainerGap(47, Short.MAX_VALUE))
    );
    setLayout(groupLayout);
  }
  
  /**
   * Adds the listeners.
   *
   * @param al
   *          the object that implements ActionListener
   */
  public void addListeners(ActionListener al) {
    btnTransColor.addActionListener(al);
    btnNewForeground.addActionListener(al);
    btnCurrentForeground.addActionListener(al);
    txtOutputFile.addActionListener(al);
    
  }
}

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
package Image2C.views;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
//import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import java.awt.Dimension;

import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.NumberFormatter;

import Image2C.common.ImageUtils;

import javax.swing.JFormattedTextField;
import javax.swing.JFileChooser;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

/**
 * The Class ImageApp is the main program for Image2C.
 * 
 * This is a tool for converting images to C Arrays
 * so images can be stored at compile time in RAM or Flash
 * based storage.  
 * 
 * Images can be png, jpg, bmp, or gif formats.
 * They will be converted to bmp files in either 16,8,4,or 1 bit depths.
 * Images can also be converted to gray scale and be resized.
 * 
 * To create the C Array file use the export function.
 * To simply save the image as a bmp file choose the save option.
 * If you get into trouble press the undo button or simply reload the image.
 * 
 * @author Paul Conti
 * 
 */
public class ImageApp implements ActionListener {

  /** The Constant PROGRAM_TITLE. */
  public static final String PROGRAM_TITLE = "GUIslice Image2C";
  
  /** The Constant VERSION is our application version number. */
  public static final String VERSION = "1.04";

  /** The Constant VERSION_NO is for save and restore of user preferences. */
  public static final String VERSION_NO = "-1";
  
  /** The image file. */
  private File imageFile;
  
  /** The image utils. */
  private ImageUtils imageUtils = null;
  
  /** The fc. */
  private JFileChooser fc=null;
  
  /** The fc 2. */
  private JFileChooser fc2=null;
  
  /** The bi original image. */
  private BufferedImage biOriginalImage=null;
  
  /** The bi input image. */
  private BufferedImage biInputImage=null;
  
  /** The bi converted image. */
  private BufferedImage biConvertedImage=null;
  
  /** The input name minus extension. */
  private String sInputName;
  
  /** The input file's extenstion */
  private String sInputExt;
  
  /** The s output name. */
  private String sOutputName;
  
  /** The s output path. */
  private String sOutputPath;
  
  /** The s C array name. */
  private String sCArrayName;
  
  /** The orig width. */
  private int origWidth;
  
  /** The orig height. */
  private int origHeight;
  
  /** The scaled width. */
  private int scaledWidth;
  
  /** The scaled height. */
  private int scaledHeight;
  
  /** The bpp. */
  private int bpp;
  
  /** The b is transparent. */
  private boolean bIsTransparent;
  
  /** The btn trans color. */
  private JButton btnTransColor;
  
  /** The transparent color. */
  private Color transparentColor;

  /** The frame. */
  private JFrame frame;
  
  /** The image pane. */
  private JPanel imagePane;
  
  /** The properties pane. */
  private JPanel propertiesPane;
  
  /** The txt output file. */
  private JTextField txtOutputFile;
  
  /** The txt C array. */
  private JTextField txtCArray;
  
  /** The txt width. */
  private JFormattedTextField txtWidth;
  
  /** The txt height. */
  private JFormattedTextField txtHeight;
  
  /** The txt num colors. */
  private JFormattedTextField txtNumColors;
  
  /** The txt image size. */
  private JFormattedTextField txtImageSize;
  
  /** The cb little endian. */
  private JCheckBox cbLittleEndian;
  
  /** The cb target guislice. */
//  private JCheckBox cbTargetGuislice;
  
  /** The cb C array flash. */
  private JCheckBox cbCArrayFlash;
  
  /** The cb transparent. */
  private JCheckBox cbTransparent;
  
  /** The cb gray scale. */
  private JCheckBox cbGrayScale;
  
  /* B&W Factor for converting to 1 bit format */
  private JFormattedTextField txtBwFactor;
  
  private Float bwfactor;
  
  /** The cc. */
  private ColorChooser cc;
  
  /** The amount format. */
  private NumberFormat amountFormat;

  /**
   * Launch the application.
   *
   * @param args
   *          the arguments
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          ThemeManager.loadThemes();
          ThemeManager.getDefaultLookAndFeel();
          ImageApp window = new ImageApp();
          window.frame.setLocationRelativeTo(null);
          window.frame.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Create the application.
   */
  public ImageApp() {
    // grab our BMP Utilities
    imageUtils = ImageUtils.getInstance();
    // Start our UI
    initialize();
  }

  /**
   * Initialize the contents of the frame.
   */
  private void initialize() {
    frame = new JFrame(PROGRAM_TITLE);
    frame.setBounds(100, 100, 740, 600);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    cc = new ColorChooser(frame);
    transparentColor = new Color(255,0,255);

    JMenuBar menuBar = new JMenuBar();
    frame.setJMenuBar(menuBar);
    
    JMenu mbFile = new JMenu("File");
    menuBar.add(mbFile);
    
    JMenuItem openMenuItem = new JMenuItem("Open",
        new ImageIcon(ImageApp.class.getResource("/resources/open.png")));
    openMenuItem.setToolTipText("Open image file");
    openMenuItem.addActionListener(this);
    openMenuItem.setActionCommand("open");
    mbFile.add(openMenuItem);
    
    JMenuItem SaveMenuItem = new JMenuItem("Save",
        new ImageIcon(ImageApp.class.getResource("/resources/save.png")));
    SaveMenuItem.setToolTipText("Save image as BMP file");
    SaveMenuItem.setActionCommand("save");
    SaveMenuItem.addActionListener(this);
    mbFile.add(SaveMenuItem);
    
    JMenuItem exportMenuItem = new JMenuItem("Export",
        new ImageIcon(ImageApp.class.getResource("/resources/export.png")));
    exportMenuItem.setToolTipText("Export file with image stored as C Array");
    exportMenuItem.setActionCommand("export");
    exportMenuItem.addActionListener(this);
    mbFile.add(exportMenuItem);
    
    JMenuItem exitMenuItem = new JMenuItem("Exit",
        new ImageIcon(ImageApp.class.getResource("/resources/logout.png")));
    exitMenuItem.setToolTipText("Exit Program");
    exitMenuItem.setActionCommand("exit");
    exitMenuItem.addActionListener(this);
    mbFile.add(exitMenuItem);
    
    JMenu mnOptions = new JMenu("Options");
    menuBar.add(mnOptions);
    
    JMenuItem meniItemThemes = new JMenuItem("Themes");
    meniItemThemes.setActionCommand("themes");
    meniItemThemes.addActionListener(this);
    mnOptions.add(meniItemThemes);
    menuBar.add(mnOptions);
    
    JMenu mnHelp = new JMenu("Help");
    menuBar.add(mnHelp);
    
    JMenuItem meniItemAbout = new JMenuItem("About");
    meniItemAbout.setIcon(new ImageIcon(ImageApp.class.getResource("/resources/about.png")));
    meniItemAbout.setActionCommand("about");
    meniItemAbout.addActionListener(this);
    mnHelp.add(meniItemAbout);
    
    JToolBar toolBar = new JToolBar();
    frame.getContentPane().add(toolBar, BorderLayout.NORTH);
    
    JButton btnOpen = new JButton(new ImageIcon(ImageApp.class.getResource("/resources/open.png")));
    btnOpen.setToolTipText("Open image file");
    btnOpen.setActionCommand("open");
    btnOpen.addActionListener(this);
    toolBar.add(btnOpen);
    
    JButton btnSave = new JButton(new ImageIcon(ImageApp.class.getResource("/resources/save.png")));
    btnSave.setToolTipText("Save as BMP file");
    btnSave.setActionCommand("save");
    btnSave.addActionListener(this);
    toolBar.add(btnSave);

    toolBar.addSeparator();

    JButton btnUndo = new JButton(new ImageIcon(ImageApp.class.getResource("/resources/undo.png")));
    btnUndo.setToolTipText("Undo all operations and go back to original file");
    btnUndo.setActionCommand("undo");
    btnUndo.addActionListener(this);
    toolBar.add(btnUndo);

    toolBar.addSeparator();

    JButton btnExport = new JButton(new ImageIcon(ImageApp.class.getResource("/resources/export.png")));
    btnExport.setToolTipText("Export file with image stored as a C Array");
    btnExport.setActionCommand("export");
    btnExport.addActionListener(this);
    toolBar.add(btnExport);
    
    toolBar.addSeparator();

    JButton btnExit = new JButton(new ImageIcon(ImageApp.class.getResource("/resources/logout.png")));
    btnExit.setToolTipText("Exit Program");
    btnExit.setActionCommand("exit");
    btnExit.addActionListener(this);
    toolBar.add(btnExit);
    
    imagePane = new JPanel() {
      private static final long serialVersionUID = 1L;

      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (biConvertedImage != null) {
          Graphics2D g2d = (Graphics2D) g.create();
//          int x = (getWidth() / 2) - (bfOutputImage.getWidth() / 2);
//          int y = (getHeight() / 2) - (bfOutputImage.getHeight() / 2);
          g2d.drawImage(biConvertedImage, 0, 0, this);
          g2d.dispose();
        }
      }
    };
    imagePane.setPreferredSize(new Dimension(1000, 1000));
    frame.getContentPane().add(imagePane, BorderLayout.CENTER);
    imagePane.setLayout(null);
    
    JScrollPane scrollPane = new JScrollPane(imagePane);
    scrollPane.setPreferredSize(new Dimension(1000, 1000));
//    scrollPane.setBounds(0, 0, 2, 2);
    frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
    
    amountFormat = NumberFormat.getIntegerInstance();
    
    propertiesPane = new JPanel();
    frame.getContentPane().add(propertiesPane, BorderLayout.EAST);
    propertiesPane.setPreferredSize(new Dimension(250, 500));
    
    JLabel lblFileOptions = new JLabel("File Options");
    lblFileOptions.setFont(new Font("SansSerif", Font.BOLD, 14));
    
    JLabel lblBMPOptions = new JLabel("BMP Options");
    lblBMPOptions.setFont(new Font("SansSerif", Font.BOLD, 14));
    
    JLabel lblOutputFile = new JLabel("Output File:");
    lblOutputFile.setFont(new Font("SansSerif", Font.PLAIN, 12));
    
    txtOutputFile = new JFormattedTextField(new DefaultFormatter());
    txtOutputFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sOutputName = txtOutputFile.getText();
      }
    });
    txtOutputFile.setFont(new Font("SansSerif", Font.PLAIN, 12));
    txtOutputFile.setColumns(10);
    
    JLabel lblCArray = new JLabel("C Array:");
    lblCArray.setFont(new Font("SansSerif", Font.PLAIN, 12));
    
    txtCArray = new JFormattedTextField(new DefaultFormatter());
    txtCArray.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sCArrayName = txtCArray.getText();
        sCArrayName = sCArrayName.replaceAll("[^A-Za-z0-9()\\[\\]]", "_");
        txtCArray.setText(sCArrayName);
      }
    });
    txtCArray.setFont(new Font("SansSerif", Font.PLAIN, 12));
    txtCArray.setColumns(10);
    
    cbLittleEndian = new JCheckBox("Output Little Endian");
    cbLittleEndian.setFont(new Font("SansSerif", Font.PLAIN, 12));
    
//    cbTargetGuislice = new JCheckBox("Target GUIslice?");
//    cbTargetGuislice.setFont(new Font("SansSerif", Font.PLAIN, 12));
    
    cbCArrayFlash = new JCheckBox("C Array as PROGMEM Storage?");
    cbCArrayFlash.setFont(new Font("SansSerif", Font.PLAIN, 12));
    
    JLabel lblSize = new JLabel("Image Size in Bytes:");
    lblSize.setFont(new Font("SansSerif", Font.PLAIN, 12));
    
    JLabel lblDepth = new JLabel("Bit Depth:");
    lblDepth.setFont(new Font("SansSerif", Font.PLAIN, 12));
/*    
    rb1Bit = new JRadioButton("1 Bit");
    rb1Bit.setFont(new Font("SansSerif", Font.PLAIN, 12));
    rb1Bit.setActionCommand("convert1bit");
    rb1Bit.addActionListener(this);
    buttonGroup.add(rb1Bit);
    
    rb4Bit = new JRadioButton("4 Bit");
    rb4Bit.setFont(new Font("SansSerif", Font.PLAIN, 12));
    rb4Bit.setActionCommand("convert4bit");
    rb4Bit.addActionListener(this);
    buttonGroup.add(rb4Bit);
    
    rb8Bit = new JRadioButton("8 Bit");
    rb8Bit.setFont(new Font("SansSerif", Font.PLAIN, 12));
    rb8Bit.setActionCommand("convert8bit");
    rb8Bit.addActionListener(this);
    buttonGroup.add(rb8Bit);
    
    rb16Bit = new JRadioButton("16 Bit");
    rb16Bit.setFont(new Font("SansSerif", Font.PLAIN, 12));
    rb16Bit.setActionCommand("convert16bit");
    rb16Bit.addActionListener(this);
    buttonGroup.add(rb16Bit);
*/  
    JLabel lblDummy = new JLabel("      ");
    cbGrayScale = new JCheckBox("Gray Scale");
    cbGrayScale.setFont(new Font("SansSerif", Font.PLAIN, 12));
    cbGrayScale.setActionCommand("grayscale");
    cbGrayScale.addActionListener(this);
    
    JLabel lblWidth = new JLabel("Width:");
    lblWidth.setFont(new Font("SansSerif", Font.PLAIN, 12));
    
    JLabel lblHeight = new JLabel("Height:");
    lblHeight.setFont(new Font("SansSerif", Font.PLAIN, 12));
    
    txtWidth = new JFormattedTextField(amountFormat);
    txtWidth.setColumns(5);
    txtWidth.setFont(new Font("SansSerif", Font.PLAIN, 12));
    txtWidth.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        scaledWidth = ((Long) txtWidth.getValue()).intValue();
        double ratio = (double)origWidth / (double)scaledWidth;
        scaledHeight = (int)((double)origHeight/ratio);
        txtHeight.setValue(scaledHeight);
        origWidth = scaledWidth;
        origHeight = scaledHeight;
        biConvertedImage = imageUtils.imageResize(biConvertedImage, scaledWidth, scaledHeight);
        biInputImage = imageUtils.clone(biConvertedImage);
        updateProperties();
      }
    });
    
    txtHeight = new JFormattedTextField(amountFormat);
    txtHeight.setColumns(5);
    txtHeight.setFont(new Font("SansSerif", Font.PLAIN, 12));
    txtHeight.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        scaledHeight = ((Long) txtHeight.getValue()).intValue();
        double ratio = (double)origHeight / (double)scaledHeight;
        scaledWidth = (int)((double)origWidth/ratio);
        txtWidth.setValue(scaledWidth);
        origWidth = scaledWidth;
        origHeight = scaledHeight;
        biConvertedImage = imageUtils.imageResize(biConvertedImage, scaledWidth, scaledHeight);
        biInputImage = imageUtils.clone(biConvertedImage);
        updateProperties();
      }
    });
    
    txtImageSize = new JFormattedTextField(amountFormat);
    txtImageSize.setColumns(10);
    txtImageSize.setFont(new Font("SansSerif", Font.PLAIN, 12));
    txtImageSize.setEditable(false);
    
    JLabel lblColors = new JLabel("# Colors:");
    lblColors.setFont(new Font("SansSerif", Font.PLAIN, 12));
    
    txtNumColors = new JFormattedTextField(amountFormat);
    txtNumColors.setColumns(5);
    txtNumColors.setFont(new Font("SansSerif", Font.PLAIN, 12));
    
    cbTransparent = new JCheckBox("Transparent Pixels");
    cbTransparent.setActionCommand("transparent");
    cbTransparent.addActionListener(this);
    cbTransparent.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
    NumberFormatter intf = new NumberFormatter();
    intf.setMinimum(Integer.valueOf(0));
    intf.setMaximum(Integer.valueOf(255));
    
    JLabel lblTransparentPixelColor = new JLabel("Transparent Pixel Color:");
    lblTransparentPixelColor.setFont(new Font("SansSerif", Font.PLAIN, 12));
    
    btnTransColor = new JButton();
    btnTransColor.setBorderPainted(false);
    btnTransColor.setBackground(transparentColor);
    btnTransColor.setActionCommand("transcolor");
    btnTransColor.addActionListener(this);
    
    JLabel lblBwBit = new JLabel("B&W Factor:");
    lblBwBit.setFont(new Font("SansSerif", Font.PLAIN, 12));
    
    NumberFormat factorFormat = DecimalFormat.getInstance();
    factorFormat.setMinimumFractionDigits(2);
    factorFormat.setMaximumFractionDigits(2);
    factorFormat.setRoundingMode(RoundingMode.HALF_UP);
    
    txtBwFactor = new JFormattedTextField(factorFormat);
    txtBwFactor.setValue(new Float(0.55));
    txtBwFactor.setFont(new Font("SansSerif", Font.PLAIN, 12));
    txtBwFactor.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (biInputImage != null && bpp == 1) {
          try {
            ((JFormattedTextField) txtBwFactor).commitEdit();
          } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }
          bwfactor = (Float) ((Double)txtBwFactor.getValue()).floatValue();
          biConvertedImage = imageUtils.convertTo1(biInputImage, bwfactor);
          updateProperties();
        }
      }
    });
    
    GroupLayout gl_propertiesPane = new GroupLayout(propertiesPane);
    gl_propertiesPane.setHorizontalGroup(
      gl_propertiesPane.createParallelGroup(Alignment.LEADING)
        .addGroup(gl_propertiesPane.createSequentialGroup()
          .addContainerGap()
          .addGroup(gl_propertiesPane.createParallelGroup(Alignment.LEADING)
            .addComponent(cbCArrayFlash)
            .addComponent(cbLittleEndian)
            .addGroup(gl_propertiesPane.createSequentialGroup()
              .addGroup(gl_propertiesPane.createParallelGroup(Alignment.LEADING)
                .addComponent(lblCArray, GroupLayout.PREFERRED_SIZE, 74, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblOutputFile, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE))
              .addPreferredGap(ComponentPlacement.RELATED)
              .addGroup(gl_propertiesPane.createParallelGroup(Alignment.LEADING, false)
                .addComponent(txtOutputFile)
                .addComponent(txtCArray, GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)))
            .addGroup(gl_propertiesPane.createParallelGroup(Alignment.TRAILING)
              .addComponent(lblBMPOptions)
              .addComponent(lblDummy)))
          .addGap(97))
        .addGroup(gl_propertiesPane.createSequentialGroup()
          .addGap(30)
          .addComponent(lblFileOptions)
          .addContainerGap(204, Short.MAX_VALUE))
        .addGroup(gl_propertiesPane.createSequentialGroup()
          .addContainerGap()
          .addGroup(gl_propertiesPane.createParallelGroup(Alignment.LEADING)
            .addComponent(lblBwBit)
            .addGroup(gl_propertiesPane.createSequentialGroup()
              .addGroup(gl_propertiesPane.createParallelGroup(Alignment.TRAILING, false)
                .addComponent(lblWidth, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblHeight, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                .addComponent(lblColors, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
              .addPreferredGap(ComponentPlacement.UNRELATED)
              .addGroup(gl_propertiesPane.createParallelGroup(Alignment.LEADING)
                .addComponent(txtWidth, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
                .addComponent(txtHeight, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
                .addComponent(txtNumColors, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE))))
          .addGap(68))
        .addGroup(gl_propertiesPane.createSequentialGroup()
          .addContainerGap()
          .addGroup(gl_propertiesPane.createParallelGroup(Alignment.LEADING)
            .addComponent(lblSize)
            .addComponent(cbTransparent)
            .addComponent(cbGrayScale)
            .addGroup(gl_propertiesPane.createSequentialGroup()
              .addComponent(lblTransparentPixelColor)
              .addGap(4)
              .addGroup(gl_propertiesPane.createParallelGroup(Alignment.LEADING, false)
                .addComponent(btnTransColor, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtImageSize, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)))
//            .addComponent(lblDepth)
            .addGroup(gl_propertiesPane.createSequentialGroup()
              .addComponent(lblBwBit)
              .addPreferredGap(ComponentPlacement.UNRELATED)
              .addComponent(txtBwFactor)
              .addPreferredGap(ComponentPlacement.RELATED)
              .addGroup(gl_propertiesPane.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_propertiesPane.createSequentialGroup()
                  .addComponent(lblDummy)
                  .addPreferredGap(ComponentPlacement.RELATED)
                  .addComponent(lblDummy))
                .addGroup(gl_propertiesPane.createParallelGroup(Alignment.TRAILING, false)
//                  .addComponent(txtBwFactor, Alignment.LEADING)
                  .addComponent(txtNumColors, Alignment.LEADING, 72, 72, Short.MAX_VALUE)))))
          .addContainerGap(19, Short.MAX_VALUE))
    );
    gl_propertiesPane.setVerticalGroup(
      gl_propertiesPane.createParallelGroup(Alignment.LEADING)
        .addGroup(gl_propertiesPane.createSequentialGroup()
          .addGap(7)
          .addComponent(lblFileOptions)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addGroup(gl_propertiesPane.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblOutputFile)
            .addComponent(txtOutputFile, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addGroup(gl_propertiesPane.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblCArray)
            .addComponent(txtCArray, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(cbCArrayFlash)
          .addGap(3)
          .addComponent(cbLittleEndian)
          .addPreferredGap(ComponentPlacement.RELATED)
          .addComponent(lblDummy)
          .addGap(17)
          .addComponent(lblBMPOptions)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addGroup(gl_propertiesPane.createParallelGroup(Alignment.TRAILING)
            .addGroup(gl_propertiesPane.createParallelGroup(Alignment.BASELINE)
              .addComponent(lblHeight)
              .addComponent(txtHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addGroup(gl_propertiesPane.createSequentialGroup()
              .addGroup(gl_propertiesPane.createParallelGroup(Alignment.BASELINE)
                .addComponent(lblWidth)
                .addComponent(txtWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
              .addGap(27)))
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(gl_propertiesPane.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblColors)
            .addComponent(txtNumColors, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//          .addPreferredGap(ComponentPlacement.UNRELATED)
//          .addGroup(gl_propertiesPane.createParallelGroup(Alignment.BASELINE)
//            .addComponent(lblBwBit, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
//            .addGap(30)
//           .addComponent(txtBwFactor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//          .addPreferredGap(ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
//          .addComponent(lblBwBit)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addGroup(gl_propertiesPane.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblBwBit)
            .addGap(30)
            .addComponent(txtBwFactor))
//            .addComponent(lblDummy)
 //           .addComponent(lblDummy))
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(cbGrayScale)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(cbTransparent)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addGroup(gl_propertiesPane.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblTransparentPixelColor)
            .addComponent(btnTransColor, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(gl_propertiesPane.createParallelGroup(Alignment.LEADING)
            .addComponent(lblSize)
            .addComponent(txtImageSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addGap(58))
    );
    propertiesPane.setLayout(gl_propertiesPane);

  }
  
  /**
   * Show image dialog.
   *
   * @return the <code>file</code> object
   */
  public File showImageDialog() {
    File file = null;
    if (fc == null) {
      fc = new JFileChooser(".");
      fc.setDialogTitle("Choose your Image");
      fc.setAcceptAllFileFilterUsed(false);
      fc.addChoosableFileFilter(new FileFilter() {
        public String getDescription() {
            return "Images: *.bmp,*.gif,*.jpg,*.png";
        }
  
        public boolean accept(File f) {
          if (f.isDirectory()) {
            return true;
          } else {
            if ((f.getName().toLowerCase().endsWith(".bmp")) ||
                (f.getName().toLowerCase().endsWith(".gif")) ||
                (f.getName().toLowerCase().endsWith(".jpg")) ||
                (f.getName().toLowerCase().endsWith(".png")) )
              return true;
            return false;
          }
        }
      });
      ImagePreviewPanel preview = new ImagePreviewPanel();
      fc.setAccessory(preview);
      fc.addPropertyChangeListener(preview);
    }
    int option = fc.showDialog(new JFrame(), "Select");
    if (option == JFileChooser.APPROVE_OPTION) {
      file = fc.getSelectedFile();
    } 
    return file;
  }
  
  /**
   * Save image dialog.
   *
   * @return the <code>file</code> object
   */
  public boolean saveImageDialog() {
    File file = null;
    if (fc2 == null) {
      fc2 = new JFileChooser(sOutputPath);
      File tmpFile = new File(sInputName+".bmp");
      fc2.setSelectedFile(tmpFile);
      fc2.setDialogTitle("Save your Image as a BMP file");
      fc2.setAcceptAllFileFilterUsed(false);
      fc2.addChoosableFileFilter(new FileFilter() {
        public String getDescription() {
            return "Images: *.bmp";
        }
  
        public boolean accept(File f) {
          if (f.isDirectory()) {
            return true;
          } else {
            if ((f.getName().toLowerCase().endsWith(".bmp")))
              return true;
            return false;
          }
        }
      });
      ImagePreviewPanel preview = new ImagePreviewPanel();
      fc2.setAccessory(preview);
      fc2.addPropertyChangeListener(preview);
    }
    boolean result=false;
    int option = fc2.showDialog(new JFrame(), "Save");
    if (option == JFileChooser.APPROVE_OPTION) {
      String name = fc2.getSelectedFile().toString();
      if (!(name.toLowerCase().endsWith(".bmp")))
        name = name + ".bmp";
      file = new File(name);
      try {
        imageUtils.image2File(biConvertedImage, file);
        result = true;
        JOptionPane.showMessageDialog(null, "Successful save of " + sOutputName + ".bmp", 
            "Save BMP", JOptionPane.INFORMATION_MESSAGE);
      } catch (IOException e) {
        JOptionPane.showMessageDialog(null, "Unsuccessful save of " + sOutputName + ".bmp", 
            "Save BMP", JOptionPane.ERROR_MESSAGE);
      }
    } 
    return result;
  }
 
  /**
   * Read image.
   */
  public void readImage() {
    cbLittleEndian.setSelected(true);
//    cbTargetGuislice.setSelected(true);
    cbCArrayFlash.setSelected(true);
    cbGrayScale.setSelected(false);
    cbTransparent.setSelected(false);
    
    bIsTransparent = false;
    buildNames(imageFile.getName());
    txtOutputFile.setText(sOutputName);
    sCArrayName = sOutputName.replaceAll("[^A-Za-z0-9()\\[\\]]", "_");
    txtCArray.setText(sCArrayName);
    try {
      biOriginalImage = ImageIO.read(imageFile);
      biConvertedImage = imageUtils.convertTo24(biOriginalImage, bIsTransparent);
      cbTransparent.setEnabled(true);
      cbGrayScale.setEnabled(true);
      origWidth = biOriginalImage.getWidth();
      origHeight = biOriginalImage.getHeight();
      scaledWidth = origWidth;
      scaledHeight = origHeight;
      updateProperties();
      // save this image and use as base for all other conversions
      biInputImage = imageUtils.clone(biConvertedImage);  
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  /**
   * Builds the names.
   *
   * @param fileName
   *          the file name
   */
  public void buildNames(String fileName) {
    int n = fileName.indexOf(".bmp");
    if (n == -1)
      n = fileName.indexOf(".png");
    if (n == -1)
      n = fileName.indexOf(".jpg");
    if (n == -1)
      n = fileName.indexOf(".gif");
    if (n == -1) {
      // can't get here but just in case
      sInputExt = new String(" ");
      sOutputName = fileName;
    } else {
      sInputExt = new String(fileName.substring(n));
      sOutputName = new String(fileName.substring(0,n));
    }
    sInputName = new String(sOutputName);
    n = imageFile.getPath().indexOf(fileName);
    sOutputPath = new String(imageFile.getPath().substring(0,n));
  }
  
  /**
   * Update properties.
   */
  public void updateProperties() {
    long nCols = imageUtils.getCurrentColors();
    txtNumColors.setValue(nCols);
    txtWidth.setValue(Integer.valueOf(biConvertedImage.getWidth()));
    txtHeight.setValue(Integer.valueOf(biConvertedImage.getHeight()));
    bpp = imageUtils.getBitDepth();
/*
    switch (bpp) {
      case 1:
        rb1Bit.setSelected(true);
        break;
      case 4:
        rb4Bit.setSelected(true);
        break;
      case 8:
        rb8Bit.setSelected(true);
        break;
      case 16:
        rb16Bit.setSelected(true);
        break;
    }
*/
    ByteArrayOutputStream tmp = new ByteArrayOutputStream();
    try {
      ImageIO.write(biConvertedImage, "bmp", tmp);
      tmp.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Integer contentLength = tmp.size();
    txtImageSize.setValue(contentLength);

    imagePane.repaint();
  }
  
  /**
   * actionPerformed.
   *
   * @param e
   *          the e
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    String command = ((AbstractButton) e.getSource()).getActionCommand();
    String frameTitle = null;
//    System.out.println("command: " + command);
    switch(command) {
      case "about":
        JOptionPane.showMessageDialog(null, "Program to convert Images to C Arrays\n" 
        + " and/or save as BMP in format supported by GUIslice API\n\n"
        + PROGRAM_TITLE
        + " ver " + VERSION +"\n" 
        + "Copyright (c) 2020 Paul Conti\n"
        + "GUIslice CopyRight (c) Calvin Hass 2016-2020"
        , "About Image2C", JOptionPane.INFORMATION_MESSAGE);
        break;
    
      case "open":
        imageFile = showImageDialog();
        if (imageFile == null) break;
        frameTitle = PROGRAM_TITLE + " - " + imageFile.getName();
        frame.setTitle(frameTitle);
        readImage();
        break;
/*        
      case "convert1bit":
        if (biInputImage != null) {
          try {
            ((JFormattedTextField) txtBwFactor).commitEdit();
          } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }
          bwfactor = (Float) ((Double)txtBwFactor.getValue()).floatValue();
          biConvertedImage = imageUtils.convertTo1(biInputImage, bwfactor);
          updateProperties();
        }
        break;
      
      case "convert4bit":
        if (biInputImage != null) {
          tmpImage = imageUtils.convertTo4(biInputImage);
          if (tmpImage != null) {
            biConvertedImage = tmpImage;
          } else {
            JOptionPane.showMessageDialog(null, "Sorry, unable to reduce image further.", 
                "Failure", JOptionPane.WARNING_MESSAGE);
          }
          updateProperties();
        }
        break;
      
      case "convert8bit":
        if (biInputImage != null) {
          tmpImage = imageUtils.convertTo8(biInputImage);
          if (tmpImage != null) {
            biConvertedImage = tmpImage;
          } else {
            JOptionPane.showMessageDialog(null, "Sorry, unable to reduce image further.", 
                "Failure", JOptionPane.WARNING_MESSAGE);
          }
          updateProperties();
        }
        break;
      
      case "convert16bit":
        if (biInputImage != null) {
          biConvertedImage = imageUtils.convertTo16(biInputImage, bIsTransparent);
          updateProperties();
        }
        break;
*/      
      case "exit":
        System.exit(0);
        break;
        
      case "export":
        try {
          ((JFormattedTextField) txtOutputFile).commitEdit();
          sOutputName = txtOutputFile.getText();
          ((JFormattedTextField) txtCArray).commitEdit();
          sCArrayName = txtCArray.getText();
          sCArrayName = sCArrayName.replaceAll("[^A-Za-z0-9()\\[\\]]", "_");
          String sName = sOutputPath + sOutputName;
          if (!(sName.toLowerCase().endsWith(".c")))
            sName = sName + ".c";
          File file = new File(sName);
          boolean bUseLittleEndian = cbLittleEndian.isSelected();
          boolean bCArrayFlash = cbCArrayFlash.isSelected();
          imageUtils.image2C_Array(biConvertedImage, sInputName, file, sCArrayName, sInputExt,
              bUseLittleEndian, bCArrayFlash);
          JOptionPane.showMessageDialog(null, "Successful export of " + sOutputName + ".c", 
              "Export Image", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        } catch (ParseException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
        break;
        
      case "grayscale":
        if (biInputImage != null) {
          biConvertedImage = imageUtils.grayscale(biInputImage);
          updateProperties();
        }
        break;
        
      case "save":
        if (biConvertedImage != null) {
          if (!saveImageDialog()) {
            JOptionPane.showMessageDialog(null, "Failed to save BMP file.", 
                "Failure", JOptionPane.ERROR_MESSAGE);
          } 
        }
        break;

      case "themes":
        String[] themes = ThemeManager.getOptions();
        JComboBox<String> comboBox = new JComboBox<String>(themes); 
        comboBox.setSelectedIndex(ThemeManager.defLafIndex);
        Object[] message = {
            "Choose Theme:", comboBox,
          };
        int option = JOptionPane.showConfirmDialog(null, message, "Theme", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) {
          return;
        }
        String name = (String)comboBox.getSelectedItem();
        ThemeManager.saveLookAndFeel(name);
        ThemeManager.setLookAndFeel(name);
        SwingUtilities.updateComponentTreeUI(frame);
        break;
        
      case "transcolor":
        transparentColor = cc.showDialog(transparentColor);
        btnTransColor.setBackground(transparentColor);
        propertiesPane.repaint();
        break;
        
      case "transparent":
        if (cbTransparent.isSelected()) {
          bIsTransparent = true;
          imageUtils.setTransparentPixelColor(transparentColor);
        } else {
          bIsTransparent = false;
        }
        if (biOriginalImage != null) {
          biConvertedImage = imageUtils.convertTo24(biOriginalImage, bIsTransparent);
          updateProperties();
        }
        break;
        
      case "undo":
        if (biOriginalImage != null) {
          // restore to the original image user opened
          biConvertedImage = imageUtils.convertTo24(biOriginalImage, bIsTransparent);
          // save this image and use as base for all other conversions
          biInputImage = imageUtils.clone(biConvertedImage);  
          updateProperties();
        }
        
        break;

      default:
        throw new IllegalArgumentException("Invalid menu item: " + command);
    }

  }
}

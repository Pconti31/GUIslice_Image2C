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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;

import java.awt.Dimension;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.filechooser.FileFilter;

import image2C.common.ImageUtils;

import javax.swing.JFormattedTextField;
import javax.swing.JFileChooser;
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
  public static final String VERSION = "2.01";

  /** The Constant VERSION_NO is for save and restore of user preferences. */
  public static final String VERSION_NO = "-1";
  
  /** The image file. */
  private File imageFile;
  
  /** The image utils. */
  static private ImageUtils imageUtils = null;
  
  /** The fc. */
  private JFileChooser fc=null;
  
  /** The fc 2. */
  private JFileChooser fc2=null;
  
  /** The bi original image. */
  private BufferedImage biOriginalImage=null;
  
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
  
  /** The b is transparent. */
  private boolean bTransparentChange;
  
  /** The transparent color. */
  static Color transparentColor;

  /** The number of colors in the image */
  private long numColors;
  
  /** The frame. */
  private JFrame frame;
  
  /** The image pane. */
  private JPanel imagePane;
  
  /** The options pane. */
  private OptionsPanel optionsPane;
  
  /** Our foreground color */
  private Color nColFG;
  
  /** The cc. */
  private ColorChooser cc;
  
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
          window.frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
              String title = "Confirm Dialog";
              String message = "You're about to quit the application -- are you sure?";
              int answer = JOptionPane.showConfirmDialog(null,message,title, JOptionPane.YES_NO_OPTION); 
              if(answer == JOptionPane.YES_OPTION) {
                System.exit(0);
              } else
                window.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            }
          });
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
    
/*
 * I've decided to not support this any longer and stick with just C Arrays
 */
/*
    JMenuItem SaveMenuItem = new JMenuItem("Save",
        new ImageIcon(ImageApp.class.getResource("/resources/save.png")));
    SaveMenuItem.setToolTipText("Save image as BMP file");
    SaveMenuItem.setActionCommand("save");
    SaveMenuItem.addActionListener(this);
    mbFile.add(SaveMenuItem);
*/    
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
    
    /*
     * I've decided to not support this any longer and stick with just C Arrays
     */
/*
    JButton btnSave = new JButton(new ImageIcon(ImageApp.class.getResource("/resources/save.png")));
    btnSave.setToolTipText("Save as BMP file");
    btnSave.setActionCommand("save");
    btnSave.addActionListener(this);
    toolBar.add(btnSave);
*/
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
    
    
    optionsPane = new OptionsPanel();
    frame.getContentPane().add(optionsPane, BorderLayout.EAST);
    optionsPane.addListeners(this);
    optionsPane.setPreferredSize(new Dimension(250, 500));
    
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
        imageUtils.image2File(biConvertedImage, 
            file, 
            (biOriginalImage.getType() == BufferedImage.TYPE_BYTE_BINARY));
        result = true;
        JOptionPane.showMessageDialog(null, "Successful save of " + name, 
            "Save BMP", JOptionPane.INFORMATION_MESSAGE);
      } catch (IOException e) {
        JOptionPane.showMessageDialog(null, "Unsuccessful save of " + name, 
            "Save BMP", JOptionPane.ERROR_MESSAGE);
      }
    } 
    return result;
  }
 
  /**
   * Read image.
   */
  public void readImage() {
    optionsPane.cbLittleEndian.setSelected(true);
    optionsPane.cbCArrayFlash.setSelected(true);
    
    bTransparentChange = false;
    buildNames(imageFile.getName());
    optionsPane.txtOutputFile.setText(sOutputName);
    sCArrayName = sOutputName.replaceAll("[^A-Za-z0-9()\\[\\]]", "_");
    optionsPane.txtCArray.setText(sCArrayName);
    try {
      biOriginalImage = ImageIO.read(imageFile);
      biConvertedImage = imageUtils.clone(biOriginalImage);
      numColors = imageUtils.getNumberOfColors(biConvertedImage);
      if(biOriginalImage.getType() == BufferedImage.TYPE_BYTE_BINARY
          || numColors == 2 || numColors == 1) {
        optionsPane.btnNewForeground.setEnabled(true);
        optionsPane.btnNewForeground.setVisible(true);
        optionsPane.lblNewForeground.setVisible(true);
        nColFG = imageUtils.getFGColor();
        optionsPane.btnNewForeground.setBackground(nColFG);
        optionsPane.btnCurrentForeground.setBackground(nColFG);
        imageUtils.setMonochromeColor(nColFG);
        optionsPane.lblCurrentForeground.setVisible(true);
        optionsPane.btnCurrentForeground.setEnabled(true);
        optionsPane.btnCurrentForeground.setVisible(true);
      } else {
        optionsPane.btnTransColor.setEnabled(true);
        optionsPane.btnTransColor.setVisible(true);
        optionsPane.lblTransparentPixelColor.setVisible(true);
      }
      updateProperties();
    } catch (Exception e) {
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
    numColors = imageUtils.getNumberOfColors(biConvertedImage);
    optionsPane.txtNumColors.setValue(numColors);
    optionsPane.txtWidth.setValue(Integer.valueOf(biConvertedImage.getWidth()));
    optionsPane.txtHeight.setValue(Integer.valueOf(biConvertedImage.getHeight()));

    imagePane.repaint();
  }
  
  /**
   * getTransparentColor
   * @return
   */
  public static Color getTransparentColor() {
    return transparentColor;
  }
  
  /**
   * getCurrentForeground
   * @return
   */
  public static Color getCurrentForeground() {
    return imageUtils.getFGColor();
  }
  
  /**
   * getNewForeground
   * @return
   */
  public static Color getNewForeground() {
    return imageUtils.getMonochromeColor();
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
        + "Copyright (c) 2021-2022 Paul Conti\n"
        + "GUIslice CopyRight (c) Calvin Hass 2016-2022"
        , "About Image2C", JOptionPane.INFORMATION_MESSAGE);
        break;
        
      case "carray":
        sCArrayName = optionsPane.txtCArray.getText();
        sCArrayName = sCArrayName.replaceAll("[^A-Za-z0-9()\\[\\]]", "_");
        optionsPane.txtCArray.setText(sCArrayName);
        break;
    
      case "open":
        imageFile = showImageDialog();
        if (imageFile == null) break;
        frameTitle = PROGRAM_TITLE + " - " + imageFile.getName();
        frame.setTitle(frameTitle);
        optionsPane.lblTransparentPixelColor.setVisible(false);
        optionsPane.btnTransColor.setEnabled(false);
        optionsPane.btnTransColor.setVisible(false);
        optionsPane.lblCurrentForeground.setVisible(false);
        optionsPane.btnCurrentForeground.setEnabled(false);
        optionsPane.btnCurrentForeground.setVisible(false);
        optionsPane.lblNewForeground.setVisible(false);
        optionsPane.btnNewForeground.setEnabled(false);
        optionsPane.btnNewForeground.setVisible(false);

        readImage();
        break;
        
      case "outputfile":
        sOutputName = optionsPane.txtOutputFile.getText();
        break;

      case "exit":
        System.exit(0);
        break;
        
      case "export":
        try {
          ((JFormattedTextField) optionsPane.txtOutputFile).commitEdit();
          sOutputName = optionsPane.txtOutputFile.getText();
          ((JFormattedTextField) optionsPane.txtCArray).commitEdit();
          sCArrayName = optionsPane.txtCArray.getText();
          sCArrayName = sCArrayName.replaceAll("[^A-Za-z0-9()\\[\\]]", "_");
          String sName = sOutputPath + sOutputName;
          if (!(sName.toLowerCase().endsWith(".c")))
            sName = sName + ".c";
          File file = new File(sName);
          boolean bUseLittleEndian = optionsPane.cbLittleEndian.isSelected();
          boolean bCArrayFlash = optionsPane.cbCArrayFlash.isSelected();
          imageUtils.image2C_Array(biConvertedImage, sInputName, file, sCArrayName, sInputExt,
              bUseLittleEndian, bCArrayFlash, bTransparentChange);
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
        imageUtils.setTransparentPixelColor(transparentColor);
        optionsPane.btnTransColor.setBackground(transparentColor);
        optionsPane.repaint();
        break;
        
      case "current_color":
        nColFG = imageUtils.swapFG();
        optionsPane.btnCurrentForeground.setBackground(nColFG);
        optionsPane.btnNewForeground.setBackground(nColFG);
        imageUtils.setFGColor(nColFG);
        imageUtils.setMonochromeColor(nColFG);
        optionsPane.repaint();
        break;

      case "new_color":
        nColFG = cc.showDialog(getNewForeground());
        optionsPane.btnNewForeground.setBackground(nColFG);
        imageUtils.setMonochromeColor(nColFG);
        optionsPane.repaint();
/*
        if (biOriginalImage != null) {
          biConvertedImage = imageUtils.convertForegroundColor(biOriginalImage, imageUtils.getNewForegroundColor());
          updateProperties();
        }
*/
        break;
/*        
      case "transparent":
        if (optionsPane.cbTransparent.isSelected()) {
          bTransparentChange = true;
          imageUtils.setTransparentPixelColor(transparentColor);
        } else {
          bTransparentChange = false;
        }
        if (biOriginalImage != null) {
          biConvertedImage = imageUtils.convertTo24(biOriginalImage, bTransparentChange);
          updateProperties();
        }
        break;
*/        
      case "undo":
        if (biOriginalImage != null) {
          // restore to the original image user opened
          biConvertedImage = imageUtils.clone(biOriginalImage);
          updateProperties();
        }
        
        break;

      default:
        throw new IllegalArgumentException("Invalid menu item: " + command);
    }

  }
}

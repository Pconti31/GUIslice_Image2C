# GUIslice image2c

### guislice image2c Contents
- `guislice_image2c-win-x.y.z.zip`: Builder Executable for Windows (in zip format)
- `guislice_image2c-linux-x.y.z.tar.gz`: Builder Executable for Linux (tar gzip format)
- `guislice_image2c-osx-x.y.z.zip`: Builder Executable for MacOS (in zip format)

### Brief Overview
<p>The GUIslice image2c is a standalone desktop application that is designed to generate memory based images 
for GUIslice API.</p>

<p>Input images can be in BMP, PNG, JPG, or GIF formats.</p>

<p>However, the utility cannot support BMP files using 32 bit format due to issues with the Java runtime. </p>

<p>The generated output code (.c) will contain the imported image as a C Array that can be included in your 
projects. These files may be used directly by the GUIslice_Builder for inclusion into your projects. 
You can refer to GUIslice/examples/arduino/ex28_ard_btn_img_flash for a sample application using C arrays 
for image buttons.</p>

#### Supported Image Output

<p>GUIslice API and therefore this utility only supports two output formats for C Arrays. The most common 
is 16 bit color per pixel and the other is Monochrome 1 bit per pixel. </p>

<p>The typical TFT screens we hobbist use only support 16 bit pixels, RGB565 format, which requires the 
alpha channel (transparency) to be removed along with a corresponding reduction in the number of supported 
colors compared to 24 or 32 bit colors per pixel.
</p>

<p>
The RGB888 format (24 bit color) allocates 8 bits to red, green and blue colors; While RGB565 allocates
only 5 bits for red, 6 bits for green, and 5 bits for blue.  Moreover, RGB888 would require storing and transmitting 3 bytes
per pixel consuming more RAM memory and which would need more time to transmit. RGB565 format is more efficient in this regard
since it only requires two bytes per pixel.
</p>

#### Transparent Images

<p>Transparent images are only supported by PNG files. The typical TFT screens we hobbist use only support 
16 bit pixels which requires the Alpha channel (transparency) to be removed along with a reduction 
in the possible number of supported colors.</p>

<p>Transparent pixels will be converted automatically to GUIslice API's transparent indicator of the Pink (Magenta) color. 
Unless your image only contains 1 color with a transparent backgound, in which case, the C array will automatically 
be exported in 1 bit format not 16 bit format greatly reducing the memory requirements for your image.</p>

<p>If you have configured GUIslice API to use another color instead of Magenta you may select the colored Pink box 
on the lower right of the display and choose another color to match the one you actually configured. 
Changing this color requires you to modify your GUIslice/config/*.h file's variable GSLC_BMP_TRANS_RGB:</p>
```
  // Enable for bitmap transparency and definition of color to use
  #define GSLC_BMP_TRANS_EN     1               // 1 = enabled, 0 = disabled
  #define GSLC_BMP_TRANS_RGB    0xFF,0x00,0xFF  // RGB color (default: MAGENTA)
```

#### Monochrome Images

<p>It should be noted that while BMP's that use 1 bit pixels do support two colors, one for 
background and one for foreground. GUIslice only support the foreground color. 
The background is treated as transparent. GUIslice_Image2C will allow you to change the 
foreground color and output the new color to your C array. It will also allow you to overwrite 
the current foreground color with a new one. Simply press the New Foreground Color button and 
a popup color chooser menu will appear.</p>

<p>For Monochrome images only the utility needs to know the color of your foreground to accurately generate your C array.
However, it turns out that there is no way to determine programmatically which color is foreground and
which is background. This program will first determine the number of colors used in your image.</p>

<p>If only one is found (most likely a PNG image with one color and a transparent background) that color will be marked
as your Foreground color. Nothing more for you to do.</p>

<p>If two colors are found it will grab the first one it finds and make it the foreground and present it to you in 
the Foreground color box.  If we guessed incorrectly or you want an inverse image then simply click the 
foreground color button and it will be exchanged with the second color found.</>

<P> Again only for Monochrome images the default color output for your C array will be white. This will be shown in the
Monochrome Output button. You may override this color by pressing this button and a color chooser will appear.
This is only needed when or if you are going to override the current color.</p>

#### WARNING

<p>You should also realize that 8 bit MCU like Arduino UNO and Mega can only address within a 16-bit 
range so arrays larger then 65kb will not load.  There likely will be size limitations with other MCUs.</P>

### Enhancement for 2.00

<P>The number of available Themes has be greatly increased for this version. Checkout options->themes for a list.</p>

<p>Previously Monochrome images (binary Bitmap images that only used 1 bit per pixel) were converted 
to 16 bit color.  They still displayed properly but took up 16 times the RAM.  Now they are 
correctly output to C arrays as unsigned char with a dramatic reduction in RAM.</p> 

<p>The default Monochrome image is assumed to have a white foreground and a black background.</p>

<p>Version 1.0 of GUIslice_Image2C would show Trandsparent pixels as black causing some images to appear as a 
filled in black rectangle.  This is fixed in version 2.0</p>

### Acknowledgements

The Non-Built in Java Themes are supported by the FlatLaf project. 

The FlatLaf project is on GitHub: 
<https://github.com/JFormDesigner/FlatLaf>

Some Imaging is provided by the Pumpernickel project.
https://mickleness.github.io/pumpernickel/

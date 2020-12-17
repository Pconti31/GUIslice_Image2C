# GUIslice image2c

### Brief Overview
The GUIslice image2c is a standalone desktop application that is designed to convert images to C Array memory based images for GUIslice.

Input images can be in BMP, PNG, JPG, or GIF formats. 

Note that the utility can also convert various formats to BMP files for use with SD Cards and GUIslice. 

The generated output code (.c) will contain the imported image as a C Array that can be included in your 
projects. You can refer to GUIslice/examples/arduino/ex28_ard_btn_img_flash for a sample application.

If you open a transparent image, say a PNG file you will notice that the transparent pixels have been 
converted to Black.  The typical TFT screens we hobbist use only support 16bit pixels which requires 
the Alpha channel (transparency) to be removed along with a reduction in the possible number of supported 
colors.

you can convert the transparent pixels to GUIslice API's 
transparent indicator of the Pink (Magenta) color. You do this by checking the "Transparent checkbox" on 
the righthand side Panel.

If you have configured GUIslice API to use a different color you may select the colored Pink box on the 
lower right of the display and choose another color to match the one you 
actually configured.

Note that the "Transparent Pixels" checkbox only works if your image contains transparent pixels.  

You should also realize that 8 bit MCU like Arduino UNO and Mega can only address within a 16-bit 
range so arrays larger then 65kb will not load.  There likely will be size limitations with other MCUs.

### Acknowledgements

The Non-Built in Java Themes are supported by the FlatLaf project. 

The FlatLaf project is on GitHub: 
<https://github.com/JFormDesigner/FlatLaf>

Some Imaging is provided by the Pumpernickel project.
https://mickleness.github.io/pumpernickel/

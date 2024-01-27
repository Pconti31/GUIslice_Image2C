/**
 * @author Paul Conti
 *
 */
module image2C {
  requires transitive java.desktop;
  requires transitive java.prefs;
  requires com.formdev.flatlaf;
  requires com.formdev.flatlaf.intellijthemes;
  exports image2C.common;
  exports image2C.views;
  exports com.pump.image.pixel;
  exports com.pump.image.pixel.quantize;
}
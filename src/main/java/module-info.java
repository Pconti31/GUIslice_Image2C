/**
 * @author Paul Conti
 *
 */
module image2C {
  requires transitive java.desktop;
  requires transitive java.prefs;
  requires flatlaf;
  requires flatlaf.intellij.themes;
  exports image2C.common;
  exports image2C.views;
  exports com.pump.image.pixel;
  exports com.pump.image.pixel.quantize;
}
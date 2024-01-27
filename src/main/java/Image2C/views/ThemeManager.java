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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.util.SystemInfo;

import com.formdev.flatlaf.intellijthemes.FlatAllIJThemes;
import com.formdev.flatlaf.intellijthemes.FlatAllIJThemes.FlatIJLookAndFeelInfo;

import image2C.common.ThemeInfo;

public class ThemeManager {

  /** The themes. */
  public static List<ThemeInfo> themes;

  /** The prefs. */
  static Preferences fPrefs;
  
  /** Key to use when looking up user preferences for desired theme */
  public static final String THEME_KEY = "THEME_KEY";

  /** The Constant MY_NODE. */
  public static final String MY_NODE = "com/impulseadventure/utilities_3";
  
  public static String defLafClassName;
  public static int    defLafIndex;

  public static String[] getOptions() {
    String[] options = new String[themes.size()];
    int i=0;
    for(ThemeInfo ti : themes) {
      options[i++] = ti.name;
    }
    return options;
  }

  public static void loadThemes() {
    // get rid of the bugged Preferences warning - not needed in Java 9 and above
    System.setErr(new PrintStream(new OutputStream() {
        public void write(int b) throws IOException {}
    }));
    String prefNode = MY_NODE + ImageApp.VERSION_NO;
    fPrefs = Preferences.userRoot().node(prefNode);
    System.setErr(System.err);  
    if( SystemInfo.isMacOS ) {
      defLafClassName = fPrefs.get(THEME_KEY, "Flat Mac Dark");
    } else {
      defLafClassName = fPrefs.get(THEME_KEY, "Flat Dark");
    }
    themes = new ArrayList<ThemeInfo>();
    
    // add system look an feels
    for (LookAndFeelInfo look_and_feel : UIManager.getInstalledLookAndFeels()) {
      JFrame.setDefaultLookAndFeelDecorated( false );
      JDialog.setDefaultLookAndFeelDecorated( false );
      themes.add(new ThemeInfo(look_and_feel.getName(),
        null, look_and_feel.getClassName()));
    }

    // add Flat LAF 
    themes.add( new ThemeInfo( "Flat Light"   , null, FlatLightLaf.class.getName() ) );
    themes.add( new ThemeInfo( "Flat Dark"    , null, FlatDarkLaf.class.getName() ) );
    themes.add( new ThemeInfo( "Flat Mac Light"   , null, FlatMacLightLaf.class.getName() ) );
    themes.add( new ThemeInfo( "Flat Mac Dark"    , null, FlatMacDarkLaf.class.getName() ) );

    themes.add( new ThemeInfo( "Flat IntelliJ", null, FlatIntelliJLaf.class.getName() ) );
    themes.add( new ThemeInfo( "Flat Darcula" , null, FlatDarculaLaf.class.getName() ) );
   
    // add intellij themes next
    for (FlatIJLookAndFeelInfo info : FlatAllIJThemes.INFOS) {
      themes.add( new ThemeInfo( info.getName() , null, info.getClassName()) );
    }
    
  }

  public static void getDefaultLookAndFeel() {
    setLookAndFeel(defLafClassName);
    defLafIndex = 0;
    int n = 0;
    for (ThemeInfo ti : themes) {
       if (ti.name.equals(defLafClassName)) {
         defLafIndex = n;
         break;
       }
       n++;
    }
  }

  public static void saveLookAndFeel(String selectedLaf) {
    fPrefs.put(THEME_KEY, selectedLaf);
    defLafClassName = selectedLaf;
    defLafIndex = 0;
    int n = 0;
    for (ThemeInfo ti : themes) {
       if (ti.name.equals(defLafClassName)) {
         defLafIndex = n;
         break;
       }
       n++;
    }
  }
  
  public static void setLookAndFeel(String selectedLaf) {
    try {
      // scan themes for a match
      ThemeInfo themeInfo = null;
      for (ThemeInfo ti : themes) {
         if (ti.name.equals(selectedLaf)) {
           themeInfo = ti;
         }
      }
      if (themeInfo != null) {
        if( themeInfo.lafClassName != null ) {
          UIManager.setLookAndFeel( themeInfo.lafClassName );
          return;
        }
      }
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ex) {
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  } // end setLookAndFeel
  
}

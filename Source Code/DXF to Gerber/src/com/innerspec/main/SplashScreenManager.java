package com.innerspec.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.ResourceBundle;

/**
 * Manages the display of text to the splash screen.
 *
 * @author Zachary Geier - zdgeier@gmail.com - Innerspec Technologies Inc.
 */
public class SplashScreenManager {
    private static java.awt.SplashScreen mySplash;
    private static Graphics2D splashGraphics;
    private static Font font;
    private static Rectangle2D splashNameTextArea;
    
    /** Contains the two major revision numbers (i.e. 2.9, 3.5, ...) */
    private final static String currentMajorVersion = "3.0";
    
    private static final int TIME_ACTIVE = 4000;

    /** 
     * Returns the version number of the build.
     * Version number is handled in build.xml with the -pre-jar target and
     * output to the version.property file in the resource package.
     * Version number is currently incremented by one every time it is built. 
     * 
     * @param propToken     The token of the property to be read from
     *                      com.innerspec.resources.version. This program 
     *                      expects the value to be "BUILD".
     * 
     * @return Minor version number (ex: 1 in 2.0.1)
     */
    private static String getToken(String propToken) { 
        ResourceBundle rb 
                = ResourceBundle.getBundle("com.innerspec.resources.version"); 
       
        //Returns property with version number
        return rb.getString(propToken); 
    } 
    
    /**
     * Prepare the global variables for the other splash functions.
     */
    public static void splashInit()
    {        
        mySplash = java.awt.SplashScreen.getSplashScreen();
        
        //Problems displaying the screen will result in mySplash equaling null
        if (mySplash != null)
        {   
            Dimension dim = mySplash.getSize();
            int height = dim.height;
            int width = dim.width;
            
            // Reserves area to write in
            splashNameTextArea = 
                    new Rectangle2D.Double(15., height*0.88, width * .45, 32.);
            
            //Creates graphics environment and sets font
            splashGraphics = mySplash.createGraphics();
            font = new Font("Arial", Font.BOLD, 15);
            splashGraphics.setFont(font);
            
            //Anti-aliasing (makes text look better)
            splashGraphics.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

            //Draws version number
            splashText(SplashScreenManager.getVersion(), 520, 15);      
            
            try {  Thread.sleep(TIME_ACTIVE);  }
            catch (InterruptedException e) { /* Ignore */ }
        }
    }
    
    /**
     * Displays a string at a location on the splash screen.
     * 
     * @param str   String to be displayed.
     * @param x     X coordinate of the text.
     * @param y     Y coordinate of the text.
     */
    private static void splashText(String str, int x, int y)
    {
        //Checks if splash is really being displayed
        if (mySplash != null && mySplash.isVisible())
        {
            // draw the text
            splashGraphics.setPaint(Color.WHITE);
            splashGraphics.drawString(str, 
                    (int)(splashNameTextArea.getX() + x),
                    (int)(splashNameTextArea.getY() + y));

            // Makes sure text is displayed
            mySplash.update();
        }
    }
    
    /**
     * Returns the version string with a leading v. This is output directly
     * to the splash screen.
     * 
     * @return String with version information.
     */
    public static String getVersion(){
        //
        return "v" + currentMajorVersion + "." + getToken("BUILD");
    }
}

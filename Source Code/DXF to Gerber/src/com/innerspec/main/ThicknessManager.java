
package com.innerspec.main;

import java.util.ArrayList;

/** 
 *  Contains the main methods for handling thicknesses for files.
 *  This is used to contain the list of thicknesses as well as to generate the 
 *  thickness header for each Gerber file.
 * 
 * @author Zachary Geier - zdgeier@gmail.com - Innerspec Technologies Inc.
 */
public class ThicknessManager {
    /**Contains the list of thicknesses in their string format for gbr files. */
    private final static ArrayList<String> thicknessArrayOutput 
            = new ArrayList<String>();
    
    /**Contains the list of the numeric thicknesses in the dxf/gbr files. */
    private final static ArrayList<Double> thicknesses 
            = new ArrayList<Double>();
    
    /** 
     * Adds a thickness to the list of thicknesses and the Gerber output list.
     * 
     * @param thickness     The decimal form of the desired thickness
     */
    public static void addThickness(double thickness){
        boolean isUsed = false;
        
        thickness = (double)Math.round(thickness * 1000) / 1000;
        
        for(double t : thicknesses){
            if(t == thickness){
                isUsed = true;
            }
        }
        
        if(isUsed == false){
            thicknessArrayOutput.add("%ADD" + (thicknesses.size() + 10)
                + "C," + thickness + "*%");
            thicknesses.add(thickness);
        }
    }
    
    /**
     * Uses thicknessArrayOutput to create a list of thicknesses separated into 
     * lines for output to Gerber file headers.
     * 
     * @return      output      Thickness code header
     */
    public static String getThicknessArrayOutput(){
        String output = "";
        for(String o : thicknessArrayOutput){
            output = output.concat(o + "\n");
        }
        return output;
    }
    
    /** 
     * Returns the thickness code (starting with 10) for a thickness in the 
     * thicknesses array.
     * Used by the entity classes (line, arc, and circle) to output the tool or
     * thickness that should be used.
     * 
     * Returns 0 if thickness does not exist.
     * 
     * @param thickness     Thickness of the desired code.
     * @return              Thickness code of the parameter.
     */
    public static int getThicknessCode(double thickness){
        for(int k = 0; k < thicknesses.size(); k++){
            if(thickness == thicknesses.get(k)){
                return k + 10;
            }
        }
        
        System.out.println("ERROR: Thickness does not exist:\t" + thickness);
        return 0;
    }

    public static ArrayList<Double> getThicknesses(){
        return thicknesses;
    }
}

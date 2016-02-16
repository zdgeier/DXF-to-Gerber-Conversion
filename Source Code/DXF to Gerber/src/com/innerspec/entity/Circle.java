
package com.innerspec.entity;

import com.innerspec.main.GerberFileManager;
import com.innerspec.main.LayerManager;
import com.innerspec.main.Layer;
import com.innerspec.main.GerberFile;
import com.innerspec.main.PrimaryFileSelectorFrame;
import com.innerspec.main.ThicknessManager;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/** 
 * Handles the processing of single circle entities in a Gerber file. 
 * 
 * @author Zachary Geier - zdgeier@gmail.com - Innerspec Technologies Inc.
 */
public class Circle {
    private double xPosCenter;
    private double yPosCenter;
    private double radius;
    private double xPosRight;
    private double yPosRight;
    private double xPosUp;
    private double yPosUp;
    private double xPosLeft;
    private double yPosLeft;
    private double xPosDown;
    private double yPosDown;
    private int thicknessCode;
    private String toolSelect;
    
    private GerberFile currentGerberFile;    
    private String currentLayerName;
    private Layer currentLayer;    
    
    private ArrayList<GerberFile> gerberFiles;
    private ArrayList<Layer> layers;
    private BufferedReader scan;
    
    /**
     * Sets up, finds, and executes the output of single circle entity data to 
     * its Gerber file.
     * 
     * @param scan The BufferedReader that has buffered the location at which 
     *             the entity is found.
     */
    public Circle(BufferedReader scan){
        this.scan   = scan;
        
        layers = LayerManager.getLayers();
        gerberFiles = GerberFileManager.getGerberFiles();
        
        /** Searches DXF file for data needed to generate the circle */
        currentLayerName = fileSearch("  8");
        xPosCenter    = Double.parseDouble(fileSearch(" 10"));
        yPosCenter    = Double.parseDouble(fileSearch(" 20"));
        radius        = Double.parseDouble(fileSearch(" 40"));
        
        setPositions();        
        
        //Finds the layer and gerber file that this entity is on
        for(Layer l : layers){
            if(l.getName().equals(currentLayerName)){
                currentLayer = l;
                currentGerberFile = gerberFiles.get(currentLayer.getFileNum());
            }
        }
        
        //Quits if layer does not exist
        if(currentLayer == null)return;
        
        //Needs positions to be set
        if(isNegative()){
            GerberFileManager.addEntityOutOfBounds(1);
            return;
        }
        else if(isZeroSize()){
            GerberFileManager.addZeroLengthEntity(1);
            return;
        }
        
        //Special handleing is needed for circles flashed by the user
        if(currentLayer.isFlashed()){
            //Rounds to the third decimal place
            double diameter = (double)Math.round(radius * 2 * 1000) / 1000;
            
            //Diameter is used as a thickness instead of a user preset
            thicknessCode = ThicknessManager.getThicknessCode(diameter);
            
            toolSelect = getToolSelect();
            if (toolSelect != null) currentGerberFile.addOutputln(toolSelect);
            
            writeFlashedCircle();
        } 
        else {            
            thicknessCode = ThicknessManager.getThicknessCode(currentLayer.getThickness());
            
            toolSelect = getToolSelect();
            if (toolSelect != null) currentGerberFile.addOutputln(toolSelect);
            
            writeCircle();
        }
        
        currentGerberFile.addEntity();
    }
    
    /** 
     * Outputs a regular circle, writing the right, up, left, down, and end 
     * positions.
     */
    private void writeCircle(){
        currentGerberFile.addOutputln(getRightPositionString());
        currentGerberFile.addOutputln(getUpPositionString());
        currentGerberFile.addOutputln(getLeftPositionString());
        currentGerberFile.addOutputln(getDownPositionString());
        currentGerberFile.addOutputln(getEndPositionString());
    }
    
    /**
     * Outputs a flashed circle, writing the center position and a D03 code.
     */
    private void writeFlashedCircle(){
        currentGerberFile.addOutputln(getCenterPositionString());
        currentGerberFile.addOutputln("D03*");
    }
    
    /**
     * Returns the tool used to write the entity. If the tool is the same as the
     * previous tool, then there is no need to change. If the tool does need to
     * change then the aperture heading is referenced to find the number of the
     * tool with the correct thickness.
     * 
     * @return A string with the thickness code that can be output to the file,
     *         null if the thickness code is already active.
     */
    private String getToolSelect(){
        //Prints tool select
        if(currentGerberFile.getOldThicknessCode() != 0 
                && currentGerberFile.getOldThicknessCode() == thicknessCode){
            return null;
        } 
        else {
            currentGerberFile.setOldThicknessCode(thicknessCode);
            return "G54D" + thicknessCode + "*";
        }
    }
    
    /**
     * Sets the up, down, left, and right positions of the circle
     */
    private void setPositions(){
        xPosRight = xPosCenter + radius;
        yPosRight = yPosCenter;
        xPosUp    = xPosCenter;
        yPosUp    = yPosCenter + radius;
        xPosLeft  = xPosCenter - radius;
        yPosLeft  = yPosCenter;
        xPosDown  = xPosCenter;
        yPosDown  = yPosCenter - radius;
    }
    
    /** 
     * Returns the center position of the circle.
     */
    private String getCenterPositionString(){
        return "X" + padSixPlaces(xPosCenter) 
             + "Y" + padSixPlaces(yPosCenter) + "D02*";
    }
    
    /**
     * Returns the right position of the circle. Gerber format requires that
     * position intervals must be written every 90 degrees, even though the 
     * plotter may not stop.
     */
    private String getRightPositionString(){
        return "G01X" + padSixPlaces(xPosRight) 
                + "Y" + padSixPlaces(yPosRight) + "D02*";
    }
    
    /**
     * Returns the right position of the circle. Gerber format requires that
     * position intervals must be written every 90 degrees, even though the 
     * plotter may not stop.
     */
    private String getUpPositionString(){
        return "G03X" + padSixPlaces(xPosUp) 
                + "Y" + padSixPlaces(yPosUp) 
                + "I" + padSixPlaces(radius) + "D01*";
    }
    
    /**
     * Returns the right position of the circle. Gerber format requires that
     * position intervals must be written every 90 degrees, even though the 
     * plotter may not stop.
     */
    private String getLeftPositionString(){
        return "X" + padSixPlaces(xPosLeft) 
             + "Y" + padSixPlaces(yPosLeft) 
             + "J" + padSixPlaces(radius) + "D01*";
    }
    
    /**
     * Returns the right position of the circle. Gerber format requires that
     * position intervals must be written every 90 degrees, even though the 
     * plotter may not stop.
     */
    private String getDownPositionString(){
        return "X" + padSixPlaces(xPosDown) 
             + "Y" + padSixPlaces(yPosDown) 
             + "I" + padSixPlaces(radius) + "D01*";
    }
    
    /**
     * Returns the end position of the plotter on the circle. 
     */
    private String getEndPositionString(){
        return "X" + padSixPlaces(xPosRight) 
             + "Y" + padSixPlaces(yPosRight)
             + "J" + padSixPlaces(radius) + "D01*";
    }
    
    /** 
     * Rounds and formats a value to six integer places. If the value does not
     * take up all six places, then zeros are added onto the front.
     * 
     * @param input The number to be padded
     * @return 
     */
    private String padSixPlaces(double input){
        //if < 6 add 0's on to front
        double x = (input * 100000);
        int y = (int)Math.round(x);
        return String.format("%06d", y);
    }
    
   
    /**
     * Searches the DXF file for a Search Code. Once this code is reached the
     * next line is returned unless another entity or the end of the section 
     * is reached. 
     * 
     * @param searchCode The DXF code that signals an element of the DXF file or
     *                   entity. The code index can be found in many places such
     *                   as http://www.autodesk.com/techpubs/autocad/acad2000/
     *                   dxf/group_codes_in_numerical_order_dxf_01.htm
     * 
     * @return The line proceeding the search code, or null if the code is not
     *         found.
     */
    private String fileSearch(String searchCode){
        //Search for searchCode
        String readLine;
        
        try {
            while((readLine = scan.readLine()) != null){                
                if(readLine.equals(searchCode)){
                    return scan.readLine();
                }
                else if(readLine.equals("ENDSEC") || readLine.equals("  0")){
                    break;
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        }
        
        return null;
    }
    
    /** 
     * Checks if the entity draws in any negative coordinates (which cannot be
     * mapped in the Gerber format).
     * 
     * @return True if the entity contains a negative coordinate, false if the
     *         entity is fully positive.
     */
    private boolean isNegative(){
        return xPosUp < 0 || yPosUp < 0 || xPosDown < 0 || yPosDown < 0 ||
               xPosLeft < 0 || yPosLeft < 0 || xPosRight < 0 || yPosRight < 0;
    }
    
    /**
     * Checks if the entity does not have a size equal to zero. This is to
     * prevent users from accidentally missing an invisible entity, that
     * plotters might handle differently
     * 
     * @return True if the entity has a zero size, false if the entity has
     *         valid dimensions
     */
    private boolean isZeroSize(){
        return radius <= 0;
    }
    
    /**
     * Searches for circles on flashed layers and adds their diameters to the
     * thicknesses list. 
     * 
     * @throws IOException Error reading file.
     */
    public static void addFlashedCircleThicknesses() throws IOException{
        
        String line;
        String layerName;
        Double flashDiameter;
        BufferedReader reader;
        boolean inEntitySection = false;
        
        ArrayList<Layer> layers = LayerManager.getLayers();
        
        reader = new BufferedReader(
                new FileReader(PrimaryFileSelectorFrame.getDxfLocation()));

        while((line = reader.readLine()) != null){

            //Only looks for entities in entity section
            if(line.compareTo("ENTITIES") == 0) inEntitySection = true;

            if(inEntitySection == true && line.equals("  0")){
                //Reads the name of the entity after the "  0" code
                line = reader.readLine();
                
                //Checks if the entity is a circle
                if (line.equals("CIRCLE")) {
                    //Searches the file for the layer name
                    layerName = flashFileSearch("  8", reader);

                    //Finds the radius/diameter using the " 40" code
                    flashDiameter = 2 * Double.parseDouble(flashFileSearch(" 40", reader));
                    
                    //Rounds to the nearest thosandths place
                    flashDiameter = (double)Math.round(flashDiameter * 1000) / 1000;
                    
                    //Finds layer object that holds the entity and adds its
                    //thickness.
                    for (Layer layer: layers){
                        if (layer.isFlashed() && 
                                layerName.equals(layer.getName())){

                            ThicknessManager.addThickness(flashDiameter);
                        }
                    }
                }
                //Once ENDSEC is reached, the loop increments out of the file
                else if (line.equals("ENDSEC")){
                    inEntitySection = false;
                }	
            }
        }
        
        reader.close();
    }
    
    /**
     * Searches the DXF file within a flashed circle to find the value of a 
     * DXF code.
     * 
     * @param searchCode The The DXF code that signals an element of the DXF file or
     *                   entity. The code index can be found in many places such
     *                   as http://www.autodesk.com/techpubs/autocad/acad2000/
     *                   dxf/group_codes_in_numerical_order_dxf_01.htm
     * 
     * @param reader     The BufferedReader that holds the location in the file
     *                   that is incremented and searched through.
     * 
     * @return              Returns the value of the line after the search code.
     * @throws IOException  Error reading file.
     */
    private static String flashFileSearch(String searchCode, BufferedReader reader) throws IOException{
        String line;
        
        //Reads until another entity
        while((line = reader.readLine()) != null){
            if(line.equals(searchCode)){
                return reader.readLine();
            }
            else if(line.equals("  0")){
                break;
            }
        }
        
        return null;
    }
}


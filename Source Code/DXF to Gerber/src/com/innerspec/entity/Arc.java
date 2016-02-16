
package com.innerspec.entity;

import com.innerspec.main.GerberFileManager;
import com.innerspec.main.LayerManager;
import com.innerspec.main.Layer;
import com.innerspec.main.GerberFile;
import com.innerspec.main.ThicknessManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

/** 
 * Handles the processing of single arc entities in a Gerber file. 
 * 
 * @author Zachary Geier - zdgeier@gmail.com - Innerspec Technologies Inc.
 */
public class Arc {
    private double xPosCenter;
    private double yPosCenter;
    private double xPosStart;
    private double yPosStart;
    private double xPosEnd;
    private double yPosEnd;
    private double radius;
    private double startAngle;
    private double endAngle;
    
    private double xPosRight;
    private double yPosRight;
    private double xPosUp;
    private double yPosUp;
    private double xPosLeft;
    private double yPosLeft;
    private double xPosDown;
    private double yPosDown; 
    
    private double xDistanceToCenter;
    private double yDistanceToCenter;
    
    private double extrusionDirection = 1; //Controls which way the arc faces
    
    private boolean isCircularCodeUsed;
    private String circularCode = "G03";
    private String toolSelect; 
    private int thicknessCode;  
    private GerberFile currentGerberFile;
    private Layer currentLayer;
    private String currentLayerName;
    
    private ArrayList<Layer> layers;
    private ArrayList<GerberFile> gerberFiles;
    private BufferedReader scan;
    
    /**
     * Sets up, finds, and executes the output of single arc entity data to 
     * its Gerber file.
     * 
     * @param scan The BufferedReader that has buffered the location at which 
     *             the entity is found.
     */
    public Arc(BufferedReader scan){
        this.scan = scan;
        
        gerberFiles = GerberFileManager.getGerberFiles();
        layers      = LayerManager.getLayers();
        
        /** Searches DXF file for data needed to generate the arc */
        currentLayerName   = fileSearch("  8");
        xPosCenter         = Double.parseDouble(fileSearch(" 10"));
        yPosCenter         = Double.parseDouble(fileSearch(" 20"));
        radius             = Double.parseDouble(fileSearch(" 40"));
        extrusionDirection = Double.parseDouble(fileSearchExtrusionDirection());
        startAngle         = Double.parseDouble(fileSearch(" 50"));
        endAngle           = Double.parseDouble(fileSearch(" 51"));

        setPositions();
        setPosStartAndEnd();
        
        //Finds the layer and gerber file that this entity is on
        for(Layer l : layers){
            if(l.getName().equals(currentLayerName)){
                currentLayer = l;
                currentGerberFile = gerberFiles.get(currentLayer.getFileNum());
                break;
            }
        }
        
        //Quits if layer does not exist
        if(currentLayer == null) return;
        
        //Needs start and end positions to be set
        if(isNegative()){            
            GerberFileManager.addEntityOutOfBounds(2);
            return;
        } 
        else if (isZeroSize()){
            GerberFileManager.addZeroLengthEntity(2);
            return;
        }
        
        //Gets the thickness code of the current layer from the manager
        thicknessCode = 
                ThicknessManager.getThicknessCode(currentLayer.getThickness());
        
        //Tool select uses the thickness code to get the tool command
        toolSelect = getToolSelect();
        if (toolSelect != null) currentGerberFile.addOutputln(toolSelect);
        
        currentGerberFile.addOutputln(getStartPos());
        
        addArcPiesOutput();

        if(isCircularCodeUsed == false){
            currentGerberFile.addOutputln("G03" + getEndPos());
        } else {
            currentGerberFile.addOutputln(getEndPos());
        }
        
        currentGerberFile.addEntity();
    }
    
    /**
     * Sets the up, down, left, and right coordinates for the arc.
     */
    private void setPositions(){
        xPosCenter = extrusionDirection * xPosCenter;
        xPosRight  = xPosCenter + radius;
        yPosRight  = yPosCenter;
        xPosUp     = xPosCenter;
        yPosUp     = yPosCenter + radius;
        xPosLeft   = xPosCenter - radius;
        yPosLeft   = yPosCenter;
        xPosDown   = xPosCenter;
        yPosDown   = yPosCenter - radius;
    }
    
    /**
     * Calculates and sets the start and end positions of the arc.
     */
    private void setPosStartAndEnd(){
        if(extrusionDirection == -1){
            //Switches angle in opposite direction
            
            if(startAngle < 180) startAngle = 180 - startAngle;
                else startAngle = 540 - startAngle;
            
            if(endAngle < 180) endAngle = 180 - endAngle;
                else endAngle = 540 - endAngle;
            
            //Switches the values of endAngle and startAngle
            double tempEndAngle   = endAngle;
            double tempStartAngle = startAngle;
            
            startAngle = tempEndAngle;
            endAngle   = tempStartAngle;
        }

        //Converts polar coordinates to rectangular coordinates
        
        xPosStart = xPosCenter + radius 
                        * Math.cos(startAngle/360 * (2 * Math.PI));
        yPosStart = yPosCenter + radius 
                        * Math.sin(startAngle/360 * (2 * Math.PI));
        xPosEnd   = xPosCenter + radius 
                        * Math.cos(endAngle/360 * (2 * Math.PI));
        yPosEnd   = yPosCenter + radius 
                        * Math.sin(endAngle/360 * (2 * Math.PI));
    }

    /**
     * Adds 90 degree position intervals to the current Gerber file. Circular
     * arcs are made every 90 degrees of the whole angle
     */
    private void addArcPiesOutput(){
        if(endAngle < startAngle){
            endAngle = endAngle + 360;
        }
        
        for(int k = 0; k <= 720; k += 90){
            if(((startAngle - k) < 0) && ((k - endAngle) <= 0)){
                isCircularCodeUsed = true;
                
                if(k == 0)
                    currentGerberFile.addOutputln(circularCode + getRightPos());
                if(k == 90)
                    currentGerberFile.addOutputln(circularCode + getUpPos());
                if(k == 180)
                    currentGerberFile.addOutputln(circularCode + getLeftPos());
                if(k == 270)
                    currentGerberFile.addOutputln(circularCode + getDownPos());
                if(k == 360)
                    currentGerberFile.addOutputln(circularCode + getRightPos());
                if(k == 450)
                    currentGerberFile.addOutputln(circularCode + getUpPos());
                if(k == 540)
                    currentGerberFile.addOutputln(circularCode + getLeftPos());
                if(k == 630)
                    currentGerberFile.addOutputln(circularCode + getDownPos());
                if(k == 720)
                    currentGerberFile.addOutputln(circularCode + getRightPos());
                
                circularCode = "";
            }
        }
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
     * Returns the start position of the arc.
     * 
     * Additionally, it sets the x and y distance to the center
     */
    private String getStartPos(){
        String output = "G01X" + padSixPlaces(xPosStart) 
                           + "Y" + padSixPlaces(yPosStart) + "D02*";
        
        xDistanceToCenter = radius * Math.cos(startAngle/360 * (2 * Math.PI));
        yDistanceToCenter = radius * Math.sin(startAngle/360 * (2 * Math.PI));
        return output;
    }
    
    /**
     * Returns the right position of the arc. Gerber format requires that
     * position intervals must be written every 90 degrees, even though the 
     * plotter may not stop.
     * 
     * Additionally, it sets the x and y distance to the center.
     */
    private String getRightPos(){
        String output = "X" + padSixPlaces(xPosRight) 
                      + "Y" + padSixPlaces(yPosRight) 
                      + "I" + padSixPlaces(xDistanceToCenter) 
                      + "J" + padSixPlaces(yDistanceToCenter) + "D01*";
        
        xDistanceToCenter = radius;
        yDistanceToCenter = 0.0;
        return output;
    }
    
    /**
     * Returns the up position of the arc. Gerber format requires that
     * position intervals must be written every 90 degrees, even though the 
     * plotter may not stop.
     * 
     * Additionally, it sets the x and y distance to the center.
     */
    private String getUpPos(){
        String output = "X" + padSixPlaces(xPosUp) 
                      + "Y" + padSixPlaces(yPosUp) 
                      + "I" + padSixPlaces(xDistanceToCenter) 
                      + "J" + padSixPlaces(yDistanceToCenter) + "D01*";
        
        xDistanceToCenter = 0.0;
        yDistanceToCenter = radius;
        return output;
    }
    
    /**
     * Returns the left position of the arc. Gerber format requires that
     * position intervals must be written every 90 degrees, even though the 
     * plotter may not stop.
     * 
     * Additionally, it sets the x and y distance to the center.
     */
    private String getLeftPos(){
        String output = "X" + padSixPlaces(xPosLeft) 
                      + "Y" + padSixPlaces(yPosLeft) 
                      + "I" + padSixPlaces(xDistanceToCenter) 
                      + "J" + padSixPlaces(yDistanceToCenter) + "D01*";
        
        xDistanceToCenter = radius;
        yDistanceToCenter = 0.0;
        return output;
    }
    
    /**
     * Returns the down position of the arc. Gerber format requires that
     * position intervals must be written every 90 degrees, even though the 
     * plotter may not stop.
     * 
     * Additionally, it sets the x and y distance to the center.
     */
    private String getDownPos(){
        String output = "X" + padSixPlaces(xPosDown) 
                      + "Y" + padSixPlaces(yPosDown) 
                      + "I" + padSixPlaces(xDistanceToCenter)
                      + "J" + padSixPlaces(yDistanceToCenter) + "D01*";
        
        xDistanceToCenter = 0.0;
        yDistanceToCenter = radius;
        return output;
    }
    
    /**
     * Returns the end position of the arc.
     */
    private String getEndPos(){
        String output = "X" + padSixPlaces(xPosEnd)
                      + "Y" + padSixPlaces(yPosEnd) 
                      + "I" + padSixPlaces(xDistanceToCenter) 
                      + "J" + padSixPlaces(yDistanceToCenter) + "D01*";
        
        return output;
    }
    
    /** 
     * Rounds and formats a value to six integer places. If the value does not
     * take up all six places, then zeros are added onto the front.
     * 
     * @param input The number to be padded
     * @return 
     */
    private String padSixPlaces(double input){
        int x = (int)(Math.abs(input) * 100000);
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
     * Searches the file for a Search Code until an Immediate Code or Secondary
     * Code is reached. The immediate code is only used to check the line
     * directly after the current line, while the secondary code is checked in
     * all subsequent lines.
     * 
     * A special method is needed for the extrusion direction in order to
     * account for different DXF versions and the case that a extrusion
     * direction might not exist.
     * 
     * This function searches for the "230" code which signals an extrusion
     * direction. Otherwise, if the code is not found then the " 50" (start 
     * angle) or "100" codes will be reached and the searching stopped.
     * 
     * @return The extrusion direction of the arc, either 1.0 or -1.0
     */
    private String fileSearchExtrusionDirection(){
        
        try {
            //Search for searchCode
            String readLine;
            
            //Checks if immediate code is equal to the next line
            //The reader is reset to its previous line afterwards
            scan.mark(50);
            
            if(scan.readLine().equals(" 50")){
                scan.reset();                
                return "1.0";
            }
            scan.reset();
            
            while((readLine = scan.readLine()) != null){
                if(readLine.equals("230")){
                    return scan.readLine();
                }
                else if(readLine.equals("100") 
                        || readLine.equals("ENDSEC") 
                        || readLine.equals("  0")){
                    break;
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        }
        return "1.0";
    }
    
    /** 
     * Checks if the entity draws in any negative coordinates (which cannot be
     * mapped in the Gerber format).
     * 
     * @return True if the entity contains a negative coordinate, false if the
     *         entity is fully positive.
     */
    private boolean isNegative(){
        return xPosStart < 0 || yPosStart < 0 || xPosEnd < 0 || yPosEnd < 0;
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
        return (xPosStart - xPosEnd == 0 && yPosStart - yPosEnd == 0) 
                || (radius <= 0);
    }
}

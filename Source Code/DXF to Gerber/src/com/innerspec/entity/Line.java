
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
 * Handles the processing of single line entities in a Gerber file. 
 * 
 * @author Zachary Geier - zdgeier@gmail.com - Innerspec Technologies Inc.
 */
public class Line {
    private double xPosOne;
    private double yPosOne;
    private double xPosTwo;
    private double yPosTwo;
    
    private String toolSelect;
    private int thicknessCode;
    private GerberFile currentGerberFile;
    private Layer currentLayer = null;
    private String currentLayerName;
    
    private ArrayList<Layer> layers;
    private ArrayList<GerberFile> gerberFiles;
    private BufferedReader scan;   
    
    public Line(BufferedReader scan){
        this.scan = scan;
        
        gerberFiles = GerberFileManager.getGerberFiles();
        layers = LayerManager.getLayers();
    
        // Searches DXF file for data needed to generate the circle 
        currentLayerName = fileSearch("  8");
        xPosOne          = Double.parseDouble(fileSearch(" 10"));
        yPosOne          = Double.parseDouble(fileSearch(" 20"));
        xPosTwo          = Double.parseDouble(fileSearch(" 11"));
        yPosTwo          = Double.parseDouble(fileSearch(" 21"));
        
        //Finds the layer and gerber file that this entity is on
        for(Layer l : layers){
            if(l.getName().equals(currentLayerName)){
                currentLayer = l;
                currentGerberFile = gerberFiles.get(currentLayer.getFileNum());
                break;
            }
        }
        
        
        //Quits if layer does not exist
        if(currentLayer == null) {
            return;
        }
        
        if(isNegative()){
            GerberFileManager.addEntityOutOfBounds(0);
            return;
        }
        else if(isZeroSize()){
            GerberFileManager.addZeroLengthEntity(0);
            return;
        }

        //Gets the thickness code of the current layer
        thicknessCode = 
                ThicknessManager.getThicknessCode(currentLayer.getThickness());

        toolSelect = getToolSelect();
        if (toolSelect != null) currentGerberFile.addOutputln(toolSelect);
        
        currentGerberFile.addOutputln(getStartPos());
        currentGerberFile.addOutputln(getEndPos());
        
        currentGerberFile.addEntity();
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
     * Returns the start position of the line.
     */    
    private String getStartPos(){
        return "G01X" + (int)(xPosOne * 100000) 
                + "Y" + (int)(yPosOne * 100000) + "D02*";
    }
    
    /**
     * Returns the end position of the line.
     */
    private String getEndPos(){
        return "X" + (int)(xPosTwo * 100000) 
             + "Y" + (int)(yPosTwo * 100000) + "D01*";
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
    private boolean isZeroSize(){
        return xPosOne - xPosTwo == 0 && yPosOne - yPosTwo == 0;
    }
    
    /**
     * Checks if the entity does not have a size equal to zero. This is to
     * prevent users from accidentally missing an invisible entity, that
     * plotters might handle differently
     * 
     * @return True if the entity has a zero size, false if the entity has
     *         valid dimensions
     */
    private boolean isNegative(){
        return xPosOne < 0 || xPosTwo < 0 || yPosOne < 0 || yPosOne < 0;
    } 
}

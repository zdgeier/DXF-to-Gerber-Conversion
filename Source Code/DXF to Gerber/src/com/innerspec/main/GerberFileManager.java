
package com.innerspec.main;

import com.innerspec.entity.Line;
import com.innerspec.entity.Arc;
import com.innerspec.entity.Circle;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.io.FileReader;

/** 
 * Handles the creation and writing of Gerber file data.
 * 
 * @author Zachary Geier - zdgeier@gmail.com - Innerspec Technologies Inc.
 */
public class GerberFileManager {
    private static ArrayList<Layer> layers 
            = LayerManager.getLayers();
    
    private static ArrayList<GerberFile> gerberFiles 
            = new ArrayList<GerberFile>();
    
    private static ProgressFrame progressFrame;
    
    private static BufferedReader scan;
    private static int fileNum = 0;    
    private static int entityQuantity = 0; 
    
    private static ArrayList<String> otherEntityNames       = new ArrayList<String>();
    private static ArrayList<Integer> otherEntityQuantities = new ArrayList<Integer>(); 
    
    private static int[] entitiesOutOfBounds = {0,0,0};
    private static int[] zeroLengthEntities = {0,0,0};
    private static int otherEntitiesTotal = 0;
    
    /** Starts process to create and write to Gerber files.
     * 
     * Loops through all layers, calling createGerberFile() to create all 
     * unique Gerber files.
     * Afterwards, searchAndWriteEntities() is called to write all entities to 
     * the files. 
     */
    public static void startWriting() {
        
        progressFrame = PrimaryFileSelectorFrame.getProgressFrame();
        
        //creates files and thicknesses
        //Nested for loop used to repeat 
        //for (Layer primary : layers) {
            for (Layer l : layers) {
                createGerberFile(l);
            }
        //}
        
        searchAndWriteEntities();
        writeProgressOutput();
    }
    
    /** Creates a Gerber file for a layer or adds it to the file. Synchronizes
     * the fileNum of the layer and Gerber file to which a layer belongs to.
     * 
     * @param   layer     The layer of which a Gerber file will be created or 
     *                    added to.
     */
    private static void createGerberFile(Layer layer) {
        /*
        * First checks to see if the gerber file array is empty.
        *
        * Creates a Gerber file from the layer or adds the layer to a gerber 
        * file if its file already exists.
        */
        
        boolean isUsed = false;
        
        if (gerberFiles.isEmpty()) {
            //Avoids adding to fileNum which starts from 0
            GerberFile gerberFile = new GerberFile(layer);
            
            layer.setFileNum(gerberFile.getFileNumber());
            gerberFiles.add(gerberFile);
        } else {
            for (GerberFile gerberFile : gerberFiles) {
                if (gerberFile.getGerberFilePath().equals(layer.getFilePath())){
                    isUsed = true;
                    layer.setFileNum(gerberFile.getFileNumber());
                    gerberFile.addLayer(layer);
                }
            }
            if (isUsed == false) {
                fileNum++;
                GerberFile gerberFile = new GerberFile(layer); 
                
                gerberFile.setFileNumber(fileNum);
                layer.setFileNum(gerberFile.getFileNumber());
                gerberFiles.add(gerberFile);
            }
        }
    }

    /**
     * Searches the dxf file for each entity (line, arc, or circle) and writes 
     * to a Gerber file using the startEntityWriting() method.
     * 
     * Once a NullPointerException is thrown (reaching the end of the dxf file), 
     * the end code is written to all Gerber files.
     */
    private static void searchAndWriteEntities() {
        String line = "";
        
        try {
            scan = new BufferedReader(
                    new FileReader(PrimaryFileSelectorFrame.getDxfLocation()));
            
            boolean inEntitySection = false;
            
            //changed second br.readLine() to line
            while((line = scan.readLine()) != null){
                
                //Only looks for entities in entity section
                if(line.compareTo("ENTITIES") == 0) inEntitySection = true;
                
                if(inEntitySection == true){
                    if(line.equals("  0")){
                        //Reads the line after the "  0" code
                        line = scan.readLine();
                        
                        if (line.equals("LINE")) {
                            Line l = new Line(scan);
                            entityQuantity++;
                        }
                        else if (line.equals("CIRCLE")) {
                            Circle c = new Circle(scan);
                            entityQuantity++;
                        }
                        else if (line.equals("ARC")) {
                            Arc a = new Arc(scan);
                            entityQuantity++;
                        }
                        else if (line.equals("ENDSEC")){
                            inEntitySection = false;
                        } 
                        else {
                            //Unrecognized entity
                            addOtherEntity(line);
                            otherEntitiesTotal++;
                        }
                    }		
                }
            }
            
            scan.close();
            
        } catch(IOException e){
            e.printStackTrace();
        } catch(NullPointerException n){
            n.printStackTrace();
        }
        
        for(GerberFile g : gerberFiles){
            //Prints the end code in all gerber files
            g.addOutputln("M02*");
            g.printLines();
        }
    }
    
    private static void addOtherEntity(String name){
        int index = 0;
        boolean isFound = false;
        
        for(String existingName: otherEntityNames){
            if(name.equals(existingName)){
                isFound = true;
                break;
            }
            
            index++;
        }
        
        if(isFound == false){
            otherEntityNames.add(name);
            otherEntityQuantities.add(1);
        }
        else {
            otherEntityQuantities.set(index, otherEntityQuantities.get(index) + 1);
        }
    }
    
    private static void writeProgressOutput(){
        progressFrame.addProgressEvent("DXF Input File: \n"
                + "\t" + PrimaryFileSelectorFrame.getDxfLocation() + "\n", 0);
        
        progressFrame.addProgressEvent(entityQuantity 
                + " Entities Processed", 0);
        
        boolean hasWrittenFiles = false;
        
        /* 
         * Prints written gerber file paths and checks that a file has been 
         *  changed
         */
        for(GerberFile g: gerberFiles){
            if(g.canWrite()){
                hasWrittenFiles = true;
            }
        }
        
        /* Do not write error messages if no files have been written to */
        if(hasWrittenFiles) {
            int index = 0;
            String s;
            
            if(otherEntityNames.size() > 0){
                s = "\n" + otherEntitiesTotal
                        + " Unsupported Entities Found:";

                progressFrame.addProgressEvent(s, 0);

                for(String name: otherEntityNames){
                    if(otherEntityQuantities.get(index) == 1){
                        s = "\t" + otherEntityQuantities.get(index) 
                            + " " + name + " Entity Found";
                    }
                    else {
                        s = "\t" + otherEntityQuantities.get(index) 
                            + " " + name + " Entities Found";
                    }

                    progressFrame.addProgressEvent(s, 0);

                    index++;
                }
            }
            else {
                progressFrame.addProgressEvent("\n0 Unsupported Entities "
                        + "Found.", index);
            }
            
            /* Writes entities that are out of bounds ( having negative coords ) */
        
            String outOfBoundsError = "";
            int totalBoundsErrors = 0;

            for(int i = 0; i < 3; i++){
                if(i == 0){
                    outOfBoundsError = outOfBoundsError + "\t" 
                            + entitiesOutOfBounds[i] 
                            + " Line(s)\n";
                }
                else if (i == 1){
                    outOfBoundsError = outOfBoundsError + "\t" 
                            + entitiesOutOfBounds[i] 
                            + " Circle(s)\n";
                }
                else if (i == 2){
                    outOfBoundsError = outOfBoundsError + "\t" 
                            + entitiesOutOfBounds[i] 
                            + " Arc(s)\n";
                }

                totalBoundsErrors += entitiesOutOfBounds[i];
            }
            
            if(totalBoundsErrors > 0){
                //Writes error message if there are any bounds errors
                progressFrame.addProgressEvent("\n" + totalBoundsErrors 
                        + " Entities Out of Bounds:", 0);

                progressFrame.addProgressEvent(outOfBoundsError, 0);
            }
            else{
                progressFrame.addProgressEvent("\n0 Entities Out of Bounds.", 0);
            }
            
            /* Writes entities that are of zero length or size */
            
            String zeroLengthError = "";
            int totalZeroLengthErrors = 0;

            for(int i = 0; i < 3; i++){
                if(i == 0){
                    zeroLengthError = zeroLengthError + "\t" 
                            + zeroLengthEntities[i] 
                            + " Line(s)\n";
                }
                else if (i == 1){
                    zeroLengthError = zeroLengthError + "\t" 
                            + zeroLengthEntities[i] 
                            + " Circle(s)\n";
                }
                else if (i == 2){
                    zeroLengthError = zeroLengthError + "\t" 
                            + zeroLengthEntities[i] 
                            + " Arc(s)";
                }

                totalZeroLengthErrors += zeroLengthEntities[i];
            }
            
            if(totalZeroLengthErrors > 0){
                //Writes error message if there are any bounds errors
                progressFrame.addProgressEvent("\n" + totalZeroLengthErrors 
                        + " Zero Size Entities Found:", 0);

                progressFrame.addProgressEvent(zeroLengthError, 0);
            }
            else{
                progressFrame.addProgressEvent("\n0 Zero Size Entities.", 0);
            }
            
            progressFrame.addProgressEvent("\nGerber Files Created:", 100);
            
            for(GerberFile g: gerberFiles){
                if(g.canWrite()){
                    progressFrame.addProgressEvent("\t" + g.getGerberFilePath(), 0);
                }
            }
        }
        else {
            progressFrame.addProgressEvent("\nNo Gerber Files Written!", 100);
        }
        
        progressFrame.setDoneWriting(true);
    }
    
    public static ArrayList getGerberFiles() {
        return gerberFiles;
    }
    
    public static ArrayList<String> getOtherEntities(){
        return otherEntityNames;
    }
    
    /**
     * 
     * @param entityType 0 for line, 1 for circle, and 2 for arc 
     */
    public static void addEntityOutOfBounds(int entityType){
        entitiesOutOfBounds[entityType]++;
    }
    
    public static void addZeroLengthEntity(int entityType){
        zeroLengthEntities[entityType]++;
    }
}

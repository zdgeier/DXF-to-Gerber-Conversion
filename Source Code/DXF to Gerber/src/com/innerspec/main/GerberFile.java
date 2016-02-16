
package com.innerspec.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/** 
 * A data type containing information about each Gerber file. 
 * 
 * @author Zachary Geier - zdgeier@gmail.com - Innerspec Technologies Inc.
 */
public class GerberFile {
    
    public static boolean canOverwriteAll = false;
    
    /** Contains all layers within the Gerber file. */
    public  ArrayList<Layer> fileLayers = new ArrayList<Layer>();
    private ArrayList<String> fileOutputLines = new ArrayList<String>();
    
    private String filePath;
    private String fileName;
    private int layerQuantity = 0;
    private int entityQuantity = 0;
    private int oldThicknessCode = 0;
    
    private boolean canWrite;
    
    /** Used to denote which file a layer belongs to. */
    private int fileNumber = 0;
    
    
    /** 
     * The main output string that contains all the lines that are going to
     * be written to the Gerber file
     */
    private String fileOutput = "";
 
    /**
     * Creates a Gerber file using the first layer's file path (and name)
     * that is added to it.
     * 
     * @param layer     The primary layer that is used to for its filePath.
     */
    public GerberFile(Layer layer){
        this.filePath = layer.getFilePath();
        this.fileName = layer.getFileName();
        
        //Creates gerber file with filepath
        try {
            File file = new File(filePath);
            if (file.createNewFile() == false){
                if(canOverwriteAll){
                    file.delete();
                    file.createNewFile();
                    canWrite = true;
                }
                else {
                    /*  
                     *  Uses yes/no/cancel dialog to check if the user only 
                     *  wants to overwrite or not overwrite one file or 
                     *  overwrite all the files.   
                     */
                    Object[] options = {"Yes", "No", "Overwrite All"};
                
                    JFrame fileExistsFrame = new JFrame();
                    
                    int n = JOptionPane.showOptionDialog(fileExistsFrame,
                        this.fileName + " already exists.\n" + "Overwrite?",
                        "File Already Exists",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[2]);
                    
                    
                    if(n == JOptionPane.YES_OPTION){
                        file.delete();
                        file.createNewFile();
                        canWrite = true;
                    }
                    else if(n == JOptionPane.NO_OPTION){
                        canWrite = false;
                    }
                    //"Overwrite All" replaces the cancel option
                    else if(n == JOptionPane.CANCEL_OPTION){
                        file.delete();
                        file.createNewFile();
                        canOverwriteAll = true;
                        canWrite = true;
                    }
                    else {
                        canWrite = false;
                    }
                }
            }
            else {
                canWrite = true;
            }
         } 
         catch (IOException e) {
             e.printStackTrace();
         }

        //Prints gerber header        
        addOutputln ("*%FSLAX25Y25*%");
        addOutputln ("G70*");
        addOutputln ("%IPPOS*%");
        addOutputln (ThicknessManager.getThicknessArrayOutput());

        addLayer(layer);    
    }
    
    public String getGerberFilePath(){
        return filePath;
    }
    
    public void addLayer(Layer l){        
        fileLayers.add(l);
        layerQuantity++;
    }
    
    public int getLayerNumber(){
        return layerQuantity;
    }
    
    public void addEntity(){
        entityQuantity++;
    }
    
    public int getEntityQuantity(){
        return entityQuantity;
    }
    
    public ArrayList getFileLayers(){
        return fileLayers;
    }
    
    public String getFileName(){
        return fileName;
    }
    
    public int getFileNumber(){
        return fileNumber;
    }
    
    public void setFileNumber(int fileNumber){
        this.fileNumber = fileNumber + 0;
    }
    
    public void setOldThicknessCode(int thicknessCode){
        this.oldThicknessCode = thicknessCode;
    }
    
    public int getOldThicknessCode(){
        return oldThicknessCode;
    }
    
    public void printLines(){
        if (canWrite == true) {
            File gbrFile;

            try {
                gbrFile = new File(filePath);

                BufferedWriter writer = 
                        new BufferedWriter(new FileWriter(gbrFile, true));

                for(String line: fileOutputLines){
                    if(line != null)
                        writer.write(line);
                    else
                        writer.newLine();
                }

                writer.close();
            } 
            catch(NullPointerException e) {
                System.out.println("ERROR: gbrPrintLn");
            }
            catch(IOException e){
                System.out.println("ERROR: FILEPRINTLN");
                e.printStackTrace();
            }
        }
    }
    
    public void addOutputln(String line){
        fileOutputLines.add(line);
        fileOutputLines.add(null);
    }
    
    public void addOutput(String line) {
        fileOutputLines.add(line);
    }
    
    public boolean canWrite(){
        return canWrite;
    }
}

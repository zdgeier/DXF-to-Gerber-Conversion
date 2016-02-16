package com.innerspec.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/** 
 * LayerManager contains methods to populate and return the layers array. 
 * 
 * @author Zachary Geier - zdgeier@gmail.com - Innerspec Technologies Inc.
 */

public class LayerManager {
    /** Contains the unique layers of the selected dxf file */
    private static ArrayList<Layer> layers = new ArrayList<Layer>();
    
    /** 
     *  Parses the selected file for layers.
     *  Once a layer is found, add layer is used to add it to the "layers" array
     */
    public static void populateLayersArray(){
 
        try{
            BufferedReader scan = new BufferedReader(
                    new FileReader(PrimaryFileSelectorFrame.getDxfLocation()));
            
            String line; 
            boolean inEntitySection = false;

            while((line = scan.readLine()) != null){
                
                //Only looks for entities in entity section
                if(line.compareTo("ENTITIES") == 0) inEntitySection = true;
                
                if(inEntitySection == true){	//loops in entity section
                    if(line.compareTo("  8") == 0) {
                        line = scan.readLine();
                        addLayer(line);
                    }
                    else if (line.compareTo("ENDSEC") == 0){
                        break;
                    }
                }
                
            }
            
            scan.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }       
        catch(NullPointerException e){
            //When end of file is reached, a null pointer exception will occur.
            //This is caught, but ignored.
        }
    }
   
    /** This method adds a layer name to the "layers" ArrayList, while checking 
     *  to see if the layer is valid and does not already exist.
     * 
     *  Only used by the populateLayersArray() method
     * 
     * @param layerName         Name of the layer to add         
     */
    private static void addLayer(String layerName){
        boolean layerAlreadyExists = false;
        
        for(Layer l: layers){   
            if(l.getName().equals(layerName)){
                layerAlreadyExists = true;
            }
        }
        
        if(layerAlreadyExists == false && layerName != null){
            layers.add(new Layer(layerName));
        }
    }
    
    /** Returns the "layers" Arraylist which contains all unique layers in the
     * dxf file.
     * 
     * @return layers
     */
    public static ArrayList getLayers(){
        return layers;
    }
}
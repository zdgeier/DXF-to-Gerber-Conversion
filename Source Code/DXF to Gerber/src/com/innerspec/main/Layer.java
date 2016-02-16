//creates layer object to set name, thickness, and filename ( used in layerList array)
package com.innerspec.main;

/** 
 * A data type containing all the information related to each layer. 
 * 
 * @author Zachary Geier - zdgeier@gmail.com - Innerspec Technologies Inc.
 */
public class Layer {
    private String name;
    private String fileName;
    private String filePath;
    
    private double thickness;
    private int thicknessCode;
    
    private boolean isActive = true;
    private boolean isFlashed = false;
    
    /** 
     *  The Gerber file number that denotes which Gerber file the layer will 
     *  be written to.
     */
    private int fileNum;
    
    public Layer(String name){
        this.name = name;
    }
    
    public void setLayer(String n, double t, String f){
        name = n;
        thickness = t;
        fileName = f;
    }
    
    public void setFileName(String fileName){
        this.fileName = fileName;
    }
    
    public void setName(String name){
        this.name = name;
    }
        
    public void setThickness(double thickness){
        this.thickness = thickness;
    }
    
    public void setThicknessCode(int thicknessCode){
        this.thicknessCode = thicknessCode;
    }
    
    /**
    * Replaces the Dxf file name with the correct output gbr name for the layer.
    * 
    * @param    dxfPath     The path of the selected dxf file
    */
    public void setFilePath(String dxfPath){
        //replaces last '/' with null(cutting path to primary folder)
        
        String os = System.getProperty("os.name").toLowerCase();
        
        //Builds file path pertaining to the operating system on which it is run
        //This is to compensate for the change in / or \ in file names
        if (os.indexOf("win") >= 0){
            filePath = dxfPath.replace
                    (dxfPath.substring(dxfPath.lastIndexOf("\\")), "");
            filePath = filePath.concat("\\" + getFileName() + ".gbr");
        } else {
            filePath = dxfPath.replace
                (dxfPath.substring(dxfPath.lastIndexOf("/")), "");
            filePath = filePath.concat("/" + getFileName() + ".gbr");
        }        
    }
    
    public void setFileNum(int fileNum){
        this.fileNum = fileNum;
    }
    
    public void setIsActive (boolean isActive){
        this.isActive = isActive;
    }
    
    public void setIsFlashed (boolean isFlashed){
        this.isFlashed = isFlashed;        
    }
    
    public String getFileName(){
        return fileName;
    }
    
    public String getName(){
        return name;
    }
    
    public double getThickness(){
        return thickness;
    }
    
    public int getThicknessCode(){
        return thicknessCode;
    }
    
    public String getFilePath(){
        return filePath;
    }
    
    public int getFileNum(){
        return fileNum;
    }
    
    public boolean isActive () {
        return isActive;
    }
    
    public boolean isFlashed(){
        return isFlashed;
    }
}

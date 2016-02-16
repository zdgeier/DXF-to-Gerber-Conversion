
package com.innerspec.main;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/** 
 * A table model that handles the data population of the table in DataFrame.
 * Also, it allows for the acquisition of user input to change the thickness or 
 * file name of each layer. 
 * 
 * @author Zachary Geier - zdgeier@gmail.com - Innerspec Technologies Inc.
 */
public class DataFrameModel extends AbstractTableModel{
    ArrayList<Layer> layers = LayerManager.getLayers();
    
    @Override
    public int getRowCount(){
        return layers.size();
    }
    
    @Override
    public int getColumnCount(){
        return 5;
    }
    
    /** Returns the value of a cell in the table. 
     * 
     * @param row       The row of the cell.
     * @param column    The column of the cell.
     * @return          The value of a cell or null if column is out-of-bounds 
     *                  (not 0,1, or 2)
     */
    @Override
    public Object getValueAt(int row, int column){
        Layer layer = layers.get(row);
        
        if(column == 0){
            return layer.isActive();
        }
        else if (column == 1){
            return layer.isFlashed();
        }
        else if (column == 2){
            return layer.getName();
        }
        else if (column == 3){
            //Format to 3 decimal places      
            NumberFormat formatter = new DecimalFormat("#0.000");     
            return (formatter.format(layer.getThickness()));
        }
        else if (column == 4){
            return layer.getFileName();
        }
        
        return null;
        
    }
    
    @Override
    public String getColumnName(int column){
        switch (column){
            case 0:  return "Active";
            case 1:  return "Flash";
            case 2:  return "Layer";
            case 3:  return "Line Width"; //Line Width = thickness
            case 4:  return "File Name";
            default: return null;
        }                
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex){
        if(columnIndex == 0 || columnIndex == 1){
            return Boolean.class;
        } else {
            return getValueAt(0, columnIndex).getClass();
        }      
    }
    
    /** 
     * Returns whether a cell is editable by the user. Makes the first column in
     * the table not editable by the user. The user can, however, modify the 
     * thickness and file name of each layer.
     * 
     * @param row       The row of the cell.
     * @param column    The column of the cell.
     * @return          False if cell is not editable (first column).
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        if(column != 2) return true;
        return false;
    }
    
    /** Sets the value of a cell in the table.
     * 
     * @param name      The value (in this case, in string form) of the cell.
     * @param row       The row of the cell.
     * @param column    The column of the cell.
     */
    @Override
    public void setValueAt(Object name, int row, int column){
        Layer layer = layers.get(row);
        
        if(column == 0){
            layer.setIsActive(Boolean.valueOf(name.toString()));
        }
        else if(column == 1){
            layer.setIsFlashed(Boolean.valueOf(name.toString()));
        }
        else if(column == 2){
            layer.setName(name.toString());
        }
        else if(column == 3){
            NumberFormat formatter = new DecimalFormat("#0.000");
            name = formatter.format(Double.valueOf(name.toString()));
            layer.setThickness(Double.parseDouble(name.toString()));
        }
        else if(column == 4){
            layer.setFileName(String.valueOf(name));
        }
        
        layers.set(row, layer);
    }
}


package com.innerspec.main;

import com.innerspec.entity.Circle;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.ImageIcon;

/** 
 * Displays a table of layer names, thicknesses, and file names.
 *  The thicknesses and file names columns are modified by the user to specify
 *  the desired thickness and file name of each layer. Layers can be combined
 *  into one file by having the same file name.
 *
 * @author Zachary Geier - zdgeier@gmail.com - Innerspec Technologies Inc.
 */
public class DataFrame extends javax.swing.JFrame {
    private ArrayList<Layer> layers = LayerManager.getLayers();
    
    private final double MIN_THICKNESS = 0.001;
    private final double MAX_THICKNESS = 1.0;         
    
    /**
     * Creates new form DataFrame
     */
    public DataFrame() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable = new javax.swing.JTable();
        nextButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Modify Layer Attributes");

        DataFrameModel d = new DataFrameModel();
        jTable.setModel(d);
        //CUSTOM CODE

        URL iconURL = getClass().getResource("/com/innerspec/resources/InnerspecLogo.png");
        // iconURL is null when not found
        ImageIcon icon = new ImageIcon(iconURL);
        this.setIconImage(icon.getImage());

        jTable.getColumnModel().getColumn(3)
        .setCellEditor(new DoubleTableEditor(MIN_THICKNESS,MAX_THICKNESS));

        jTable.getColumnModel().getColumn(0).setMaxWidth(50);
        jTable.getColumnModel().getColumn(1).setMaxWidth(50);

        String layerName;
        String stringTemp;
        String fileName;
        Double thicknessMil;
        Double doubleTemp;
        Boolean isFlashed;

        for(int k = 0; k < jTable.getRowCount(); k++){
            layerName = jTable.getValueAt(k, 2).toString();
            isFlashed = false;

            thicknessMil = 0.005;   //Default
            fileName = layerName;

            if(layerName.contains("MIL")
                && layerName.lastIndexOf("MIL") > layerName.lastIndexOf("_")){
                stringTemp = layerName.substring(0, layerName.lastIndexOf("MIL"));
                stringTemp = stringTemp.substring(stringTemp.lastIndexOf("_") + 1, stringTemp.length());

                try {
                    doubleTemp = Double.valueOf(stringTemp);

                    thicknessMil = doubleTemp / 1000;
                    fileName = layerName.substring(0, layerName.indexOf("_"));
                }
                catch(NumberFormatException e){
                    //Happens when the value between _ and MIL cannot be converted
                    //EX: TOP_AMIL
                }
            }

            jTable.setValueAt(isFlashed, k, 1);
            jTable.setValueAt(thicknessMil, k, 3);
            jTable.setValueAt(fileName, k, 4);
        }
        jTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTable);

        nextButton.setText("Next >");
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(6, 6, 6)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 688, Short.MAX_VALUE)
                .add(6, 6, 6))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(cancelButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(nextButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(6, 6, 6)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nextButton)
                    .add(cancelButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    /** 
     * Grabs all table data and updates layer and thickness values.
     * 
     *  Calls disposes of the window and GerberFileManager.startWriting() when 
  finished.
     *  
     * @param evt       User presses the "ENTER" button
     */
    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        String name;
        String fileName;
        double thickness;
        boolean isActive;
        
        for(int k = 0; k < layers.size(); k++){ 
            Layer layer = layers.get(k); 
            
            name = layer.getName();
            fileName = jTable.getValueAt(k, 4).toString();
            thickness = Double.parseDouble(jTable.getValueAt(k, 3).toString());
            isActive = layer.isActive();
            
            
            
            /*
            * Makes sure layer, fileName, and thickness are valid.
            * Changes existing layer values to those in the table.
            * Removes invalid or empty layers.
            */
            if(name != null
                    && fileName  != null 
                    && thickness != 0.0 
                    && isActive){
                layer.setFileName(fileName); 
                layer.setThickness(thickness);
                ThicknessManager.addThickness(thickness);
            }
            else {
                layers.remove(k);
                k--;
            }
        }
        
        try {
            Circle.addFlashedCircleThicknesses();
        } catch (IOException e){
            e.printStackTrace();
        }
        
        for(Layer l : layers){
            l.setFilePath(PrimaryFileSelectorFrame.getDxfLocation());
        }
        
        //Hide this frame and present progress frame
        setVisible(false);
        PrimaryFileSelectorFrame.getProgressFrame().setVisible(true);
        PrimaryFileSelectorFrame.getProgressFrame().initializeGerberManager();
        dispose();
    }//GEN-LAST:event_nextButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        System.exit(0);
    }//GEN-LAST:event_cancelButtonActionPerformed

    /** 
     * Sets up and displays the DataFrame window.
     */
    public void main(String args[]) {
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        //Sets up window in OS theme
        try {
            javax.swing.UIManager.setLookAndFeel(
                javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DataFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DataFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DataFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DataFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DataFrame().setVisible(true);
            }
        });
    }
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable;
    private javax.swing.JButton nextButton;
    // End of variables declaration//GEN-END:variables
}

/*
* Open-Source tuning tools
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License along
* with this program; if not, write to the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

package com.vgi.mafscaling;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

public class PrimaryOpenLoopFuelingTable implements ActionListener {
	class RestrictedFileSystemView extends FileSystemView {
	    private final File rootDirectory;
	    RestrictedFileSystemView(File rootDirectory) {
	        this.rootDirectory = rootDirectory;
	    }
	    @Override
	    public File createNewFolder(File dir) {
			return null;
		}
	    public File getParentDirectory(File dir) {
	    	return new File(".");
	    }
	    @Override
	    public File[] getRoots() {
	        return new File[] {rootDirectory};
	    }
	    @Override
	    public boolean isRoot(File file) {
	    	if (file.equals(rootDirectory))
	    		return true;
	        return false;
	    }
	}
    private static final Logger logger = Logger.getLogger(PrimaryOpenLoopFuelingTable.class);
    private final static int ColumnWidth = 45;
    private ExcelAdapter excelAdapter = new ExcelAdapter();
    private JFileChooser fileChooser = null;
    private JTable fuelingTable = null;
    private JTable tempFuelingTable = null;
    private JButton btnSetDefault = null;
    private JComboBox<String> loadList = null;
    private String fileName = Config.getDefaultPOLFueling();
    private String tempFileName = "";
    
    public PrimaryOpenLoopFuelingTable() {
    	btnSetDefault = new JButton("Set Default");
    	File appdir = new File(".");
    	FileSystemView fsv = new RestrictedFileSystemView(appdir);
    	fileChooser = new JFileChooser(fsv);
    	fileChooser.setCurrentDirectory(appdir);
    	if (!fileName.isEmpty()) {
    		fuelingTable = loadPolFueling(fuelingTable, fileName);
    		if (fuelingTable == null) {
    			Config.setDefaultPOLFueling("");
                String[] files = Config.getPOLFuelingFiles().split(",");
                String fs = "";
                for (String fn : files) {
                	if (fn.equals(fileName) || fn.isEmpty())
                		continue;
                	fs += ("," + fn);
                }
                Config.setPOLFuelingFiles(fs);
    			fileName = "";
    		}
    		else {
	        	if (Config.getDefaultPOLFueling().equals(fileName))
	        		btnSetDefault.setText("Unset Default");
    		}
    	}
        else if (Config.getDefaultPOLFueling().equals(fileName))
    		btnSetDefault.setText("Unset Default");
    }
    
    public boolean getSetUserFueling() {
        JPanel fuelingPanel = new JPanel();
        GridBagLayout gbl_dataPanel = new GridBagLayout();
        gbl_dataPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
        gbl_dataPanel.rowHeights = new int[] {0, 0};
        gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0};
        gbl_dataPanel.rowWeights = new double[]{0.0, 0.0};
        fuelingPanel.setLayout(gbl_dataPanel);
        
        loadList = new JComboBox<String>(Config.getPOLFuelingFiles().split(","));
        GridBagConstraints gbc_loadList = new GridBagConstraints();
        gbc_loadList.anchor = GridBagConstraints.EAST;
        gbc_loadList.insets = new Insets(1, 5, 1, 1);
        gbc_loadList.gridx = 0;
        gbc_loadList.gridy = 0;
        loadList.setSelectedItem(fileName);
        loadList.setActionCommand("polselected");
        loadList.addActionListener(this);
        loadList.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        fuelingPanel.add(loadList, gbc_loadList);
        
        Component horizontalGlue = Box.createHorizontalGlue();
        GridBagConstraints gbc_horizontalGlue = new GridBagConstraints();
        gbc_horizontalGlue.fill = GridBagConstraints.HORIZONTAL;
        gbc_horizontalGlue.insets = new Insets(1, 5, 1, 1);
        gbc_horizontalGlue.gridx = 1;
        gbc_horizontalGlue.gridy = 0;
        fuelingPanel.add(horizontalGlue, gbc_horizontalGlue);
        
        JButton btnSave = new JButton("Save");
        GridBagConstraints gbc_btnSave = new GridBagConstraints();
        gbc_btnSave.anchor = GridBagConstraints.EAST;
        gbc_btnSave.insets = new Insets(1, 5, 1, 1);
        gbc_btnSave.gridx = 2;
        gbc_btnSave.gridy = 0;
        btnSave.setActionCommand("save");
        btnSave.addActionListener(this);
        fuelingPanel.add(btnSave, gbc_btnSave);
        
        GridBagConstraints gbc_btnSetDefault = new GridBagConstraints();
        gbc_btnSetDefault.anchor = GridBagConstraints.EAST;
        gbc_btnSetDefault.insets = new Insets(1, 5, 1, 1);
        gbc_btnSetDefault.gridx = 3;
        gbc_btnSetDefault.gridy = 0;
        btnSetDefault.setActionCommand("setdefault");
        btnSetDefault.addActionListener(this);
        fuelingPanel.add(btnSetDefault, gbc_btnSetDefault);
        
        JButton btnClearData = new JButton("Clear");
        GridBagConstraints gbc_btnClearData = new GridBagConstraints();
        gbc_btnClearData.anchor = GridBagConstraints.EAST;
        gbc_btnClearData.insets = new Insets(1, 5, 1, 1);
        gbc_btnClearData.gridx = 4;
        gbc_btnClearData.gridy = 0;
        btnClearData.setActionCommand("clear");
        btnClearData.addActionListener(this);
        fuelingPanel.add(btnClearData, gbc_btnClearData);
        
        JButton btnValidateData = new JButton("Validate");
        GridBagConstraints gbc_btnValidateData = new GridBagConstraints();
        gbc_btnValidateData.anchor = GridBagConstraints.EAST;
        gbc_btnValidateData.insets = new Insets(1, 5, 1, 1);
        gbc_btnValidateData.gridx = 5;
        gbc_btnValidateData.gridy = 0;
        btnValidateData.setActionCommand("check");
        btnValidateData.addActionListener(this);
        fuelingPanel.add(btnValidateData, gbc_btnValidateData);

        tempFuelingTable = createFuelingTable();
        tempFileName = fileName;

        GridBagConstraints gbc_fuelingTable = new GridBagConstraints();
        gbc_fuelingTable.insets = new Insets(5, 0, 0, 0);
        gbc_fuelingTable.fill = GridBagConstraints.BOTH;
        gbc_fuelingTable.weightx = 1.0;
        gbc_fuelingTable.weighty = 1.0;
        gbc_fuelingTable.gridx = 0;
        gbc_fuelingTable.gridy = 1;
        gbc_fuelingTable.gridwidth = 6;
        
        JScrollPane scrollPane = new JScrollPane(tempFuelingTable);
        scrollPane.setViewportBorder(new TitledBorder(null, "Primary Open Loop Fueling", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        fuelingPanel.add(scrollPane, gbc_fuelingTable);
        fuelingPanel.setPreferredSize(new Dimension(620, 500));

        JComponent[] inputs = new JComponent[] { fuelingPanel };
        do {
            if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null, inputs, "Fueling settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                excelAdapter.removeTable(tempFuelingTable);
                Utils.clearTable(tempFuelingTable);
                tempFuelingTable = null;
                tempFileName = "";
                return false;
            }
        }
        while (!validateFuelingData(tempFuelingTable));
        if (tempFuelingTable != null) {
            if (fuelingTable != null)
                excelAdapter.removeTable(fuelingTable);
            if (Utils.isTableEmpty(tempFuelingTable)) {
	            fuelingTable = null;
                tempFuelingTable = null;
                tempFileName = "";
	            fileName = "";
            }
            else {
	            fuelingTable = tempFuelingTable;
	            fileName = tempFileName;
            }
        }
        return true;
    }
    
    /**
     * Method clears cells and resets background color
     */
    public void clear() {
        Utils.clearTable(tempFuelingTable);
        tempFileName = "";
        loadList.setSelectedIndex(0);
    }
    
    /**
     * Method validates that fueling table exists and all data is populated.
     * @return
     */
    public boolean validate() {
        if (fuelingTable == null || !validateFuelingData(fuelingTable)) {
            JOptionPane.showMessageDialog(null, "Please set fueling data", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    /**
     * Method returns true if fueling table is set, false otherwise.
     * @return
     */
    public boolean isSet() {
        if (fuelingTable == null || !validateFuelingData(fuelingTable))
            return false;
        return true;
    }
    
    /**
     * Method sets values for the row.
     * If current table row count is less than specified row, a new row is added.
     * If current table column count is less than number of columns in elements array, a new column is added.
     * @param row
     * @param elements
     */
    public void setValueAtRow(int row, String[] elements) {
    	fuelingTable = setValueAtRow(fuelingTable, row, elements);
    }

    /**
     * Method sets values for the row.
     * If current table row count is less than specified row, a new row is added.
     * If current table column count is less than number of columns in elements array, a new column is added.
     * @param fuelingTable
     * @param row
     * @param elements
     * @return
     */
    private JTable setValueAtRow(JTable fuelingTable, int row, String[] elements) {
        if (fuelingTable == null)
            fuelingTable = createFuelingTable();
        Utils.ensureRowCount(row + 1, fuelingTable);
        Utils.ensureColumnCount(elements.length - 1, fuelingTable);
        for (int i = 0; i < elements.length - 1; ++i)
            fuelingTable.setValueAt(elements[i], row, i);
        return fuelingTable;
    }
    
    public Object getValueAt(int row, int column) {
        return fuelingTable.getValueAt(row, column);
    }
    
    /**
     * Method return row count
     * @return row count
     */
    public int getRowCount() {
        return fuelingTable.getRowCount();
    }
    
    /**
     * Method return row count
     * @return column count
     */
    public int getColumnCount() {
        return fuelingTable.getColumnCount();
    }
    
    /**
     * Method write data in current primary open loop fueling table to provide file handler
     * @param out, file handler
     * @throws IOException 
     */
    public void write(FileWriter out) throws IOException {
    	write(fuelingTable, out);
    }

    
    /**
     * Method write data in current primary open loop fueling table to provide file handler
     * @param fuelingTable
     * @param out, file handler
     * @throws IOException 
     */
    private void write(JTable fuelingTable, FileWriter out) throws IOException {
        if (fuelingTable != null) {
            int i, j;
            for (i = 0; i < fuelingTable.getRowCount(); ++i) {
                for (j = 0; j < fuelingTable.getColumnCount(); ++j)
                    out.write(fuelingTable.getValueAt(i, j).toString() + ",");
                out.write("\n");
            }
        }
    }
    
    /**
     * Function creates new fueling table
     * @return
     */
    private JTable createFuelingTable() {
        JTable table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setColumnSelectionAllowed(true);
        table.setCellSelectionEnabled(true);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setTableHeader(null);
        if (fuelingTable != null) {
            table.setModel(new DefaultTableModel(fuelingTable.getRowCount(), fuelingTable.getColumnCount()));
            Utils.initializeTable(table, ColumnWidth);
            for (int i = 0; i < fuelingTable.getRowCount(); ++i) {
                for (int j = 0; j < fuelingTable.getColumnCount(); ++j) {
                    if (fuelingTable.getValueAt(i, j) != null)
                        table.setValueAt(fuelingTable.getValueAt(i, j).toString(), i, j);
                }
            }
            Utils.colorTable(table);
        }
        else {
            table.setModel(new DefaultTableModel(20, 15));
            Utils.initializeTable(table, ColumnWidth);
        }
        Format[][] formatMatrix = { { new DecimalFormat("#"), new DecimalFormat("0.00") } };
        NumberFormatRenderer renderer = (NumberFormatRenderer)table.getDefaultRenderer(Object.class);
        renderer.setFormats(formatMatrix);
        excelAdapter.addTable(table, true, true);
        return table;
    }
    
    /**
     * Method validates that provided table exists and all data is populated.
     * @param fuelingTable, table to be cecked
     * @return
     */
    private boolean validateFuelingData(JTable fuelingTable) {
        if (fuelingTable == null)
            return false;
        // check if table is empty
        if (Utils.isTableEmpty(fuelingTable))
        	return true;
        // check paste format
        if (!fuelingTable.getValueAt(0, 0).toString().equalsIgnoreCase("[table3d]") &&
            !((fuelingTable.getValueAt(0, 0).toString().equals("")) &&
              Pattern.matches(Utils.fpRegex, fuelingTable.getValueAt(0, 1).toString()) &&
              Pattern.matches(Utils.fpRegex, fuelingTable.getValueAt(1, 0).toString()))) {
            JOptionPane.showMessageDialog(null, "Invalid data in POL Fueling table!\n\nPlease post 'Primary Open Loop Fueling' table into first cell", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (fuelingTable.getValueAt(0, 0).toString().equalsIgnoreCase("[table3d]")) {
            // realign if paste is from RomRaider
            if (fuelingTable.getValueAt(0, 1).toString().equals("")) {
                Utils.removeRow(0, fuelingTable);
                for (int i = fuelingTable.getColumnCount() - 2; i >= 0; --i)
                    fuelingTable.setValueAt(fuelingTable.getValueAt(0, i), 0, i + 1);
                fuelingTable.setValueAt("", 0, 0);
            }
            // paste is probably from excel, just blank out the first cell
            else
                fuelingTable.setValueAt("", 0, 0);
        }
        // remove extra rows
        for (int i = fuelingTable.getRowCount() - 1; i >= 0 && fuelingTable.getValueAt(i, 0).toString().equals(""); --i)
            Utils.removeRow(i, fuelingTable);
        // remove extra columns
        for (int i = fuelingTable.getColumnCount() - 1; i >= 0 && fuelingTable.getValueAt(0, i).toString().equals(""); --i)
            Utils.removeColumn(i, fuelingTable);
        // validate all cells are numeric
        for (int i = 0; i < fuelingTable.getRowCount(); ++i) {
            for (int j = 0; j < fuelingTable.getColumnCount(); ++j) {
                if (i == 0 && j == 0)
                    continue;
                if (!Pattern.matches(Utils.fpRegex, fuelingTable.getValueAt(i, j).toString())) {
                    JOptionPane.showMessageDialog(null, "Invalid value at row " + (i + 1) + " column " + (j + 1), "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        Utils.colorTable(fuelingTable);
        return true;
    }
    
    /**
     * Function to load POL Fueling table from file
     */
    private JTable loadPolFueling(JTable fuelingTable, String fileName) {
    	if (fileName.isEmpty())
    		return null;
        File file = new File("./" + fileName);
        BufferedReader br = null;
        try {
        	br = new BufferedReader(new FileReader(file));
        	int i = 0;
            String line = br.readLine();
            while (line != null) {
            	fuelingTable = setValueAtRow(fuelingTable, i++, line.split(",", -1));
            	line = br.readLine();
            }
            if (i > 0 && validateFuelingData(fuelingTable))
            	return fuelingTable;
        }
        catch (FileNotFoundException e) {
            logger.error(e);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
        finally {
        	if (br != null) {
                try {
                	br.close();
                }
                catch (IOException e) {
                    logger.error(e);
                }
        	}
        }
        return null;
    }
    
    /**
     * Function sets current POL table as default. If the current POL table not saved - it saves it.
     * @param fuelingTable
     * @return
     */
    private void setDefault(JTable fuelingTable) {
    	if (btnSetDefault.getText().equals("Unset Default")) {
    		Config.setDefaultPOLFueling("");
    		btnSetDefault.setText("Set Default");
    	}
    	else {
	    	if (!tempFileName.isEmpty())
	    		save(fuelingTable);
	    	Config.setDefaultPOLFueling(tempFileName);
    		btnSetDefault.setText("Unset Default");
    	}
    }
    
    /**
     * Function save POL table.
     * @param fuelingTable
     * @return
     */
    private void save(JTable fuelingTable) {
    	if (!validateFuelingData(fuelingTable))
    		return;
    	if (tempFileName.isEmpty()) {
            if (JFileChooser.APPROVE_OPTION != fileChooser.showSaveDialog(null))
                return;
            File file = fileChooser.getSelectedFile();
            tempFileName = file.getName();
            FileWriter out = null;
            try {
            	out = new FileWriter(file);
                write(fuelingTable, out);
            }
            catch (Exception e) {
                e.printStackTrace();
                logger.error(e);
            }
            finally {
            	if (out != null) {
                    try {
                    	out.close();
                    }
                    catch (IOException e) {
                        logger.error(e);
                    }
            	}
            }
            String[] files = Config.getPOLFuelingFiles().split(",");
            String fs = "";
            for (String fn : files) {
            	if (fn.equals(fileName) || fn.isEmpty())
            		continue;
            	fs += ("," + fn);
            }
        	fs += ("," + tempFileName);
            Config.setPOLFuelingFiles(fs);
            loadList.addItem(tempFileName);
            loadList.setSelectedItem(tempFileName);
    	}
    }
    
    /**
     * Loads POL fueling table from file.
     * @param fuelingTable
     */
    private void load() {
        String fileName = (String)loadList.getSelectedItem();
        clear();
        btnSetDefault.setText("Set Default");
        if (!fileName.isEmpty()) {
        	if (loadPolFueling(tempFuelingTable, fileName) != null) {
	        	tempFileName = fileName;
	        	loadList.setSelectedItem(fileName);
	        	if (Config.getDefaultPOLFueling().equals(fileName))
	        		btnSetDefault.setText("Unset Default");
        	}
        	else {
            	loadList.removeItem(fileName);
            	loadList.setSelectedItem("");
            	if (!fileName.isEmpty()) {
                    String[] files = Config.getPOLFuelingFiles().split(",");
                    String fs = "";
                    for (String fn : files) {
                    	if (fn.equals(fileName) || fn.isEmpty())
                    		continue;
                    	fs += ("," + fn);
                    }
                    Config.setPOLFuelingFiles(fs);
            	}
        	}
        }
        else if (Config.getDefaultPOLFueling().equals(fileName))
    		btnSetDefault.setText("Unset Default");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("clear".equals(e.getActionCommand()))
            clear();
        else if ("check".equals(e.getActionCommand()))
            validateFuelingData(tempFuelingTable);
        else if ("setdefault".equals(e.getActionCommand()))
            setDefault(tempFuelingTable);
        else if ("save".equals(e.getActionCommand()))
        	save(tempFuelingTable);
        else if ("polselected".equals(e.getActionCommand()))
        	load();
    }
}

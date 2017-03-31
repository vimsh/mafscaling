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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.Format;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
    private static final Logger logger = Logger.getLogger(PrimaryOpenLoopFuelingTable.class);
    private final static int ColumnWidth = 45;
    private ExcelAdapter excelAdapter = new ExcelAdapter();
    private JFileChooser fileChooser = null;
    private JTable fuelingTable = null;
    private JTable tempFuelingTable = null;
    private JButton btnSetDefault = null;
    private JComboBox<String> loadList = null;
    private String fileName = "";
    private String tempFileName = "";
    private JCheckBox checkBoxMap = null;
    private boolean isPolfMap = false;
    
    public PrimaryOpenLoopFuelingTable() {
        checkBoxMap = new JCheckBox("This fueling table has MAP (psi abs) axis");
        File appdir = new File(".");
        FileSystemView fsv = new RestrictedFileSystemView(appdir);
        fileChooser = new JFileChooser(fsv);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileName = Config.getDefaultPOLFueling();
        btnSetDefault = new JButton("Set Default");
        btnSetDefault.addActionListener(this);
        if (!fileName.isEmpty()) {
            fuelingTable = loadPolFueling(fuelingTable, fileName);
            if (fuelingTable != null)
                btnSetDefault.setText("Unset Default");
            else {
                Config.setDefaultPOLFueling("");
                resetPOLFuelingFiles(fileName, "");
            }
        }
    }
    
    public boolean getSetUserFueling() {
        JPanel fuelingPanel = new JPanel();
        GridBagLayout gbl_dataPanel = new GridBagLayout();
        gbl_dataPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
        gbl_dataPanel.rowHeights = new int[] {0, 0, 0};
        gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0};
        gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0};
        fuelingPanel.setLayout(gbl_dataPanel);
        
        loadList = new JComboBox<String>(Config.getPOLFuelingFiles().split(Utils.fileFieldSplitter));
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
        if (btnSetDefault.getText().equals("Unset Default") && !Config.getDefaultPOLFueling().equals(tempFileName))
            btnSetDefault.setText("Set Default");

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

        GridBagConstraints gbc_checkBoxMap = new GridBagConstraints();
        gbc_checkBoxMap.anchor = GridBagConstraints.WEST;
        gbc_checkBoxMap.insets = new Insets(5, 0, 0, 0);
        gbc_checkBoxMap.gridx = 0;
        gbc_checkBoxMap.gridy = 2;
        fuelingPanel.add(checkBoxMap, gbc_checkBoxMap);

        JComponent[] inputs = new JComponent[] { fuelingPanel };
        do {
            if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null, inputs, "Fueling settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                excelAdapter.removeTable(tempFuelingTable);
                Utils.clearTable(tempFuelingTable);
                tempFuelingTable = null;
                tempFileName = "";
                checkBoxMap.setSelected(isPolfMap);
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
                checkBoxMap.setSelected(isPolfMap);
            }
            else {
                fuelingTable = tempFuelingTable;
                fileName = tempFileName;
                isPolfMap = checkBoxMap.isSelected();
            }
        }
        return true;
    }
    
    /**
     * Method resets POL Fueling files list to encounter for removed files
     */
    public void resetPOLFuelingFiles(String remFileName, String addFileName) {
        String[] files = Config.getPOLFuelingFiles().split(Utils.fileFieldSplitter);
        String fs = "";
        for (String fn : files) {
            if (fn.equals(remFileName) || fn.isEmpty())
                continue;
            fs += ("," + fn);
        }
        if (!addFileName.isEmpty())
            fs += ("," + addFileName);
        Config.setPOLFuelingFiles(fs);
    }
    
    /**
     * Method clears cells and resets background color
     */
    public void clear() {
        Utils.clearTable(tempFuelingTable);
        tempFileName = "";
        loadList.setSelectedIndex(0);
        resetMapState();
    }
    
    /**
     * Method validates that fueling table exists and all data is populated.
     * @return
     */
    public boolean validate() {
        if (fuelingTable == null || !validateFuelingData(fuelingTable))
            return false;
        return true;
    }

    /**
     * Method returns true if fueling table is set, false otherwise.
     * @return
     */
    public boolean isSet() {
        if (fuelingTable == null || Utils.isTableEmpty(fuelingTable) || !validateFuelingData(fuelingTable))
            return false;
        return true;
    }
    
    /**
     * Method returns true if fueling table has MAP axis, false otherwise.
     * @return
     */
    public boolean isMap() {
        return isPolfMap;
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
    public void write(Writer out) throws IOException {
        write(fuelingTable, out);
    }

    /**
     * Method resets the MAP checkbox and flag states.
     * @return
     */
    private void resetMapState() {
        checkBoxMap.setSelected(false);
        checkBoxMap.setEnabled(true);
        isPolfMap = false;
    }
    
    /**
     * Method write data in current primary open loop fueling table to provide file handler
     * @param fuelingTable
     * @param out, file handler
     * @throws IOException 
     */
    private void write(JTable fuelingTable, Writer out) throws IOException {
        if (fuelingTable != null) {
            if (checkBoxMap.isSelected())
                out.write("MAP\n");
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
        table.putClientProperty("terminateEditOnFocusLost", true);
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
     * @param fuelingTable, table to be checked
     * @return
     */
    private boolean validateFuelingData(JTable fuelingTable) {
        if (fuelingTable != null && Utils.isTableEmpty(fuelingTable))
            return true;
        if (!Utils.validateTable(fuelingTable))
            return false;
        validateMapLoadFuelingData(fuelingTable);
        return true;
    }
    
    /**
     * Method validates that proper selection of MAP or Load POLF is made.
     * @param fuelingTable, table to be checked
     * @return
     */
    private void validateMapLoadFuelingData(JTable fuelingTable) {
        final double maxLoad = 10.0;
        double firstRowLastColumnValue = Double.parseDouble(fuelingTable.getValueAt(0, fuelingTable.getColumnCount() - 1).toString());
        if (checkBoxMap.isSelected() && firstRowLastColumnValue < maxLoad) {
            JOptionPane.showMessageDialog(null, "The data looks like it's Load based POLF table. Unsetting the checkbox.", "Warning", JOptionPane.WARNING_MESSAGE);
            checkBoxMap.setSelected(false);
        }
        else if (!checkBoxMap.isSelected() && firstRowLastColumnValue >= maxLoad) {
            JOptionPane.showMessageDialog(null, "This data looks like it's MAP (psi) based POLF table. Setting the checkbox.", "Warning", JOptionPane.WARNING_MESSAGE);
            checkBoxMap.setSelected(true);
        }
    }
    
    /**
     * Function to load POL Fueling table from file
     */
    private JTable loadPolFueling(JTable fuelingTable, String fileName) {
        File file = new File("./" + fileName);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsoluteFile()), Config.getEncoding()));
            int i = 0;
            String line = br.readLine();
            while (line != null) {
                if (0 == i && line.equals("MAP")) {
                    checkBoxMap.setSelected(true);
                    checkBoxMap.setEnabled(false);
                    isPolfMap = true;
                }
                else
                    fuelingTable = setValueAtRow(fuelingTable, i++, line.split(Utils.fileFieldSplitter, -1));
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
        else if (!save(fuelingTable).isEmpty()) {
            Config.setDefaultPOLFueling(tempFileName);
            btnSetDefault.setText("Unset Default");
        }
    }
    
    /**
     * Function save POL table.
     * @param fuelingTable
     * @return
     */
    private String save(JTable fuelingTable) {
        if (fuelingTable == null || Utils.isTableEmpty(fuelingTable) || !validateFuelingData(fuelingTable))
            return "";
        File file = null;
        if (tempFileName.isEmpty()) {
            if (JFileChooser.APPROVE_OPTION != fileChooser.showSaveDialog(null))
                return "";
            file = fileChooser.getSelectedFile();
            tempFileName = file.getName();
        }
        else
            file = new File(tempFileName);
        Writer out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Config.getEncoding()));
            write(fuelingTable, out);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to save file " + tempFileName + ": " + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            tempFileName = "";
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
        if (!tempFileName.isEmpty()) {
            resetPOLFuelingFiles(tempFileName, tempFileName);
            if (((DefaultComboBoxModel<String>)loadList.getModel()).getIndexOf(tempFileName) == -1)
                loadList.addItem(tempFileName);
            loadList.setSelectedItem(tempFileName);
        }
        return tempFileName;
    }
    
    /**
     * Loads POL fueling table from file.
     * @param fuelingTable
     */
    private void load() {
        Utils.clearTable(tempFuelingTable);
        tempFileName = (String)loadList.getSelectedItem();
        btnSetDefault.setText("Set Default");
        resetMapState(); // needed even when tempFileName == "" (blank table selected) so load log will display columns correctly
        if (!tempFileName.isEmpty()) {
            if (loadPolFueling(tempFuelingTable, tempFileName) != null) {
                loadList.setSelectedItem(tempFileName);
                if (Config.getDefaultPOLFueling().equals(tempFileName))
                    btnSetDefault.setText("Unset Default");
            }
            else {
                loadList.removeItem(tempFileName);
                loadList.setSelectedItem("");
                resetPOLFuelingFiles(tempFileName, "");
            }
        }
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

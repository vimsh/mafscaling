package com.vgi.mafscaling;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class PrimaryOpenLoopFuelingTable implements ActionListener {
    private final static int ColumnWidth = 40;
    private JTable fuelingTable = null;
    private JTable tempFuelingTable = null;
    private ExcelAdapter excelAdapter = null;
    private DoubleFormatRenderer intRenderer = null;
    
    public PrimaryOpenLoopFuelingTable() {
        intRenderer = new DoubleFormatRenderer();
        intRenderer.setFormatter(new DecimalFormat("#"));
    }

    public void setExcelAdapter(ExcelAdapter excelAdapter) {
        this.excelAdapter = excelAdapter;
    }
    
    public boolean getSetUserFueling() {
        JPanel fuelingPanel = new JPanel();
        GridBagLayout gbl_dataPanel = new GridBagLayout();
        gbl_dataPanel.columnWidths = new int[]{0, 0, 0};
        gbl_dataPanel.rowHeights = new int[] {0, 0};
        gbl_dataPanel.columnWeights = new double[]{1.0, 0.0, 0.0};
        gbl_dataPanel.rowWeights = new double[]{0.0, 0.0};
        
        Component horizontalGlue = Box.createHorizontalGlue();
        GridBagConstraints gbc_horizontalGlue = new GridBagConstraints();
        gbc_horizontalGlue.fill = GridBagConstraints.HORIZONTAL;
        gbc_horizontalGlue.insets = new Insets(1, 5, 1, 1);
        gbc_horizontalGlue.gridx = 0;
        gbc_horizontalGlue.gridy = 0;
        fuelingPanel.add(horizontalGlue, gbc_horizontalGlue);
        
        JButton btnClearData = new JButton("Clear");
        GridBagConstraints gbc_btnClearData = new GridBagConstraints();
        gbc_btnClearData.anchor = GridBagConstraints.EAST;
        gbc_btnClearData.insets = new Insets(1, 5, 1, 1);
        gbc_btnClearData.gridx = 1;
        gbc_btnClearData.gridy = 0;
        btnClearData.setActionCommand("clear");
        btnClearData.addActionListener(this);
        fuelingPanel.add(btnClearData, gbc_btnClearData);
        fuelingPanel.setLayout(gbl_dataPanel);
        
        JButton btnValidateData = new JButton("Validate");
        GridBagConstraints gbc_btnValidateData = new GridBagConstraints();
        gbc_btnValidateData.anchor = GridBagConstraints.EAST;
        gbc_btnValidateData.insets = new Insets(1, 5, 1, 1);
        gbc_btnValidateData.gridx = 2;
        gbc_btnValidateData.gridy = 0;
        btnValidateData.setActionCommand("check");
        btnValidateData.addActionListener(this);
        fuelingPanel.add(btnValidateData, gbc_btnValidateData);

        tempFuelingTable = createFuelingTable();

        GridBagConstraints gbc_fuelingTable = new GridBagConstraints();
        gbc_fuelingTable.insets = new Insets(5, 0, 0, 0);
        gbc_fuelingTable.fill = GridBagConstraints.BOTH;
        gbc_fuelingTable.weightx = 1.0;
        gbc_fuelingTable.weighty = 1.0;
        gbc_fuelingTable.gridx = 0;
        gbc_fuelingTable.gridy = 1;
        gbc_fuelingTable.gridwidth = 3;
        
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
                return false;
            }
        }
        while (!validateFuelingData(tempFuelingTable));
        if (tempFuelingTable != null) {
            if (fuelingTable != null)
                excelAdapter.removeTable(fuelingTable);
            fuelingTable = tempFuelingTable;
        }
        return true;
    }
    
    /**
     * Method clears cells and resets background color
     */
    public void clear() {
        Utils.clearTable(tempFuelingTable);
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
     * Method sets values for the row.
     * If current table row count is less than specified row, a new row is added.
     * If current table column count is less than number of columns in elements array, a new column is added.
     * @param row
     * @param elements
     */
    public void setValueAtRow(int row, String[] elements) {
        if (fuelingTable == null)
            fuelingTable = createFuelingTable();
        Utils.ensureRowCount(row + 1, fuelingTable);
        Utils.ensureColumnCount(elements.length - 1, fuelingTable);
        for (int i = 0; i < elements.length - 1; ++i)
            fuelingTable.setValueAt(elements[i], row, i);
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
            table.getColumnModel().getColumn(0).setCellRenderer(intRenderer);
            Utils.colorTable(table);
        }
        else {
            table.setModel(new DefaultTableModel(20, 15));
            table.getColumnModel().getColumn(0).setCellRenderer(intRenderer);
            Utils.initializeTable(table, ColumnWidth);
        }
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
        fuelingTable.getColumnModel().getColumn(0).setCellRenderer(intRenderer);
        Utils.colorTable(fuelingTable);
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("clear".equals(e.getActionCommand()))
            clear();
        else if ("check".equals(e.getActionCommand()))
            validateFuelingData(tempFuelingTable);
    }
}

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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

public class LogStatsFixedAxis implements ActionListener {
    private static final Logger logger = Logger.getLogger(LogStatsFixedAxis.class);
	private enum Axis {XAXIS, YAXIS};
    private final static int ColumnWidth = 40;
    private final static int AxisColumnCount = 15;
    private ExcelAdapter excelAdapter = new ExcelAdapter();
    private JTable xAxisTable = null;
    private JTable yAxisTable = null;
    private JTable templateTable = null;
    private JComboBox<String> xAxisList = null;
    private JComboBox<String> yAxisList = null;
    private ArrayList<Double> xAxisArray;
    private ArrayList<Double> yAxisArray;
    
    public LogStatsFixedAxis(ArrayList<Double> xAxis, ArrayList<Double> yAxis) {
    	xAxisArray = xAxis;
    	yAxisArray = yAxis;
    	initialize();
    }
    
    private void initialize() {
        JPanel dataPanel = new JPanel();
        GridBagLayout gbl_dataPanel = new GridBagLayout();
        gbl_dataPanel.columnWidths = new int[]{0};
        gbl_dataPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0};
        gbl_dataPanel.columnWeights = new double[]{0.0};
        gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        dataPanel.setLayout(gbl_dataPanel);
        dataPanel.setPreferredSize(new Dimension(620, 500));

        JLabel note1Label = new JLabel("You can manually enter axis values or paste them into tables below");
        note1Label.setForeground(Color.BLUE);
        GridBagConstraints gbc_note1Label = new GridBagConstraints();
        gbc_note1Label.anchor = GridBagConstraints.WEST;
        gbc_note1Label.fill = GridBagConstraints.HORIZONTAL;
        gbc_note1Label.insets = new Insets(3, 3, 3, 3);
        gbc_note1Label.gridx = 0;
        gbc_note1Label.gridy = 0;
        dataPanel.add(note1Label, gbc_note1Label);
        
        createAxisPanel(dataPanel);
        
        JLabel note2Label = new JLabel("You can select existing axis if you have saved them before");
        note2Label.setForeground(Color.BLUE);
        GridBagConstraints gbc_note2Label = new GridBagConstraints();
        gbc_note2Label.anchor = GridBagConstraints.WEST;
        gbc_note2Label.fill = GridBagConstraints.HORIZONTAL;
        gbc_note2Label.insets = new Insets(3, 3, 3, 3);
        gbc_note2Label.gridx = 0;
        gbc_note2Label.gridy = 2;
        dataPanel.add(note2Label, gbc_note2Label);
        
        createControlPanel(dataPanel);
        
        JLabel note3Label = new JLabel("You can paste a table below and first row and first column will be used as axis");
        note3Label.setForeground(Color.BLUE);
        GridBagConstraints gbc_note3Label = new GridBagConstraints();
        gbc_note3Label.anchor = GridBagConstraints.WEST;
        gbc_note3Label.fill = GridBagConstraints.HORIZONTAL;
        gbc_note3Label.insets = new Insets(3, 3, 3, 3);
        gbc_note3Label.gridx = 0;
        gbc_note3Label.gridy = 4;
        dataPanel.add(note3Label, gbc_note3Label);
        
        createDataTablePanel(dataPanel);

    	setAxisTables();
        
        JComponent[] inputs = new JComponent[] { dataPanel };
        do {
            if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null, inputs, "Fixed size axis", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE))
                return;
        }
        while (!validateAxisData());
    }

    private void createAxisPanel(JPanel dataPanel) {
        JPanel axisPanel = new JPanel();
        GridBagConstraints gbc_axisPanel = new GridBagConstraints();
        gbc_axisPanel.insets = new Insets(3, 3, 3, 3);
        gbc_axisPanel.anchor = GridBagConstraints.NORTHWEST;
        gbc_axisPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_axisPanel.weightx = 1.0;
        gbc_axisPanel.gridx = 0;
        gbc_axisPanel.gridy = 1;
        dataPanel.add(axisPanel, gbc_axisPanel);
        
        GridBagLayout gbl_cntlPanel = new GridBagLayout();
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0, 0, 0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0};
        gbl_cntlPanel.rowWeights = new double[]{0.0, 0.0, 1.0};
        axisPanel.setLayout(gbl_cntlPanel);

    	xAxisTable = new JTable();
        xAxisTable.setColumnSelectionAllowed(true);
        xAxisTable.setCellSelectionEnabled(true);
        xAxisTable.setBorder(new LineBorder(Color.GRAY));
        xAxisTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        xAxisTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        xAxisTable.setModel(new DefaultTableModel(1, AxisColumnCount));
        xAxisTable.setTableHeader(null);
        Utils.initializeTable(xAxisTable, ColumnWidth);
        excelAdapter.addTable(xAxisTable, false, false, false, false, true, false, true, false, true);
        
        JScrollPane xAxisScrollPane = new JScrollPane(xAxisTable);
        xAxisScrollPane.setViewportBorder(new TitledBorder(null, "X-Axis", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        xAxisScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        xAxisScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        GridBagConstraints gbc_xAxisScrollPane = new GridBagConstraints();
        gbc_xAxisScrollPane.insets = new Insets(0, 0, 0, 0);
        gbc_xAxisScrollPane.anchor = GridBagConstraints.NORTH;
        gbc_xAxisScrollPane.fill = GridBagConstraints.HORIZONTAL;
        gbc_xAxisScrollPane.ipady = 17;
        gbc_xAxisScrollPane.weightx = 1.0;
        gbc_xAxisScrollPane.gridx = 0;
        gbc_xAxisScrollPane.gridy = 0;
        axisPanel.add(xAxisScrollPane, gbc_xAxisScrollPane);

        JButton btnClearXAxis = new JButton("Clear");
        GridBagConstraints gbc_btnClearXAxis = new GridBagConstraints();
        gbc_btnClearXAxis.anchor = GridBagConstraints.EAST;
        gbc_btnClearXAxis.insets = new Insets(1, 5, 1, 1);
        gbc_btnClearXAxis.gridx = 1;
        gbc_btnClearXAxis.gridy = 0;
        btnClearXAxis.setActionCommand("clearxaxis");
        btnClearXAxis.addActionListener(this);
        axisPanel.add(btnClearXAxis, gbc_btnClearXAxis);

        JButton btnSaveXAxis = new JButton("Save");
        GridBagConstraints gbc_btnSaveXAxis = new GridBagConstraints();
        gbc_btnSaveXAxis.anchor = GridBagConstraints.EAST;
        gbc_btnSaveXAxis.insets = new Insets(1, 5, 1, 1);
        gbc_btnSaveXAxis.gridx = 2;
        gbc_btnSaveXAxis.gridy = 0;
        btnSaveXAxis.setActionCommand("savexaxis");
        btnSaveXAxis.addActionListener(this);
        axisPanel.add(btnSaveXAxis, gbc_btnSaveXAxis);

    	yAxisTable = new JTable();
        yAxisTable.setColumnSelectionAllowed(true);
        yAxisTable.setCellSelectionEnabled(true);
        yAxisTable.setBorder(new LineBorder(Color.GRAY));
        yAxisTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        yAxisTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        yAxisTable.setModel(new DefaultTableModel(1, AxisColumnCount));
        yAxisTable.setTableHeader(null);
        Utils.initializeTable(yAxisTable, ColumnWidth);
        excelAdapter.addTable(yAxisTable, false, false, false, false, true, false, true, false, true);
        
        JScrollPane yAxisScrollPane = new JScrollPane(yAxisTable);
        yAxisScrollPane.setViewportBorder(new TitledBorder(null, "Y-Axis", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        yAxisScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        yAxisScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        GridBagConstraints gbc_yAxisScrollPane = new GridBagConstraints();
        gbc_yAxisScrollPane.anchor = GridBagConstraints.PAGE_START;
        gbc_yAxisScrollPane.weightx = 1.0;
        gbc_yAxisScrollPane.insets = new Insets(0, 0, 0, 0);
        gbc_yAxisScrollPane.fill = GridBagConstraints.HORIZONTAL;
        gbc_yAxisScrollPane.ipady = 17;
        gbc_yAxisScrollPane.gridx = 0;
        gbc_yAxisScrollPane.gridy = 1;
        axisPanel.add(yAxisScrollPane, gbc_yAxisScrollPane);

        JButton btnClearYAxis = new JButton("Clear");
        GridBagConstraints gbc_btnClearYAxis = new GridBagConstraints();
        gbc_btnClearYAxis.anchor = GridBagConstraints.EAST;
        gbc_btnClearYAxis.insets = new Insets(1, 5, 1, 1);
        gbc_btnClearYAxis.gridx = 1;
        gbc_btnClearYAxis.gridy = 1;
        btnClearYAxis.setActionCommand("clearyaxis");
        btnClearYAxis.addActionListener(this);
        axisPanel.add(btnClearYAxis, gbc_btnClearYAxis);

        JButton btnSaveYAxis = new JButton("Save");
        GridBagConstraints gbc_btnSaveYAxis = new GridBagConstraints();
        gbc_btnSaveYAxis.anchor = GridBagConstraints.EAST;
        gbc_btnSaveYAxis.insets = new Insets(1, 5, 1, 1);
        gbc_btnSaveYAxis.gridx = 2;
        gbc_btnSaveYAxis.gridy = 1;
        btnSaveYAxis.setActionCommand("saveyaxis");
        btnSaveYAxis.addActionListener(this);
        axisPanel.add(btnSaveYAxis, gbc_btnSaveYAxis);
    }
    
    private void createControlPanel(JPanel dataPanel) {
    	String templNames;
        JPanel cntlPanel = new JPanel();
        GridBagConstraints gbc_ctrlPanel = new GridBagConstraints();
        gbc_ctrlPanel.insets = new Insets(3, 3, 3, 3);
        gbc_ctrlPanel.anchor = GridBagConstraints.WEST;
        gbc_ctrlPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_ctrlPanel.gridx = 0;
        gbc_ctrlPanel.gridy = 3;
        dataPanel.add(cntlPanel, gbc_ctrlPanel);
        
        GridBagLayout gbl_cntlPanel = new GridBagLayout();
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0};
        gbl_cntlPanel.rowWeights = new double[]{0.0};
        cntlPanel.setLayout(gbl_cntlPanel);

        JLabel xAxisLabel = new JLabel("Saved X-Axis");
        GridBagConstraints gbc_xAxisLabel = new GridBagConstraints();
        gbc_xAxisLabel.anchor = GridBagConstraints.WEST;
        gbc_xAxisLabel.insets = new Insets(3, 0, 3, 0);
        gbc_xAxisLabel.gridx = 0;
        gbc_xAxisLabel.gridy = 0;
        cntlPanel.add(xAxisLabel, gbc_xAxisLabel);

        templNames = Config.getXAxisTemplates();
        if (templNames.isEmpty())
        	templNames = ",";
        xAxisList = new JComboBox<String>(templNames.split(","));
        GridBagConstraints gbc_xAxisList = new GridBagConstraints();
        gbc_xAxisList.anchor = GridBagConstraints.WEST;
        gbc_xAxisList.insets = new Insets(1, 5, 1, 1);
        gbc_xAxisList.gridx = 1;
        gbc_xAxisList.gridy = 0;
        xAxisList.setActionCommand("xaxis");
        xAxisList.addActionListener(this);
        xAxisList.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXX");
        cntlPanel.add(xAxisList, gbc_xAxisList);

        JButton btnRemXAxisTempl = new JButton("Remove");
        GridBagConstraints gbc_btnRemXAxisTempl = new GridBagConstraints();
        gbc_btnRemXAxisTempl.anchor = GridBagConstraints.EAST;
        gbc_btnRemXAxisTempl.insets = new Insets(1, 5, 1, 1);
        gbc_btnRemXAxisTempl.gridx = 2;
        gbc_btnRemXAxisTempl.gridy = 0;
        btnRemXAxisTempl.setActionCommand("remxtempl");
        btnRemXAxisTempl.addActionListener(this);
        cntlPanel.add(btnRemXAxisTempl, gbc_btnRemXAxisTempl);
        
        JLabel yAxisLabel = new JLabel("Saved Y-Axis");
        GridBagConstraints gbc_yAxisLabel = new GridBagConstraints();
        gbc_yAxisLabel.anchor = GridBagConstraints.EAST;
        gbc_yAxisLabel.insets = new Insets(3, 3, 3, 0);
        gbc_yAxisLabel.gridx = 3;
        gbc_yAxisLabel.gridy = 0;
        cntlPanel.add(yAxisLabel, gbc_yAxisLabel);

        templNames = Config.getYAxisTemplates();
        if (templNames.isEmpty())
        	templNames = ",";
        yAxisList = new JComboBox<String>(templNames.split(","));
        GridBagConstraints gbc_yAxisList = new GridBagConstraints();
        gbc_yAxisList.anchor = GridBagConstraints.WEST;
        gbc_yAxisList.insets = new Insets(1, 5, 1, 1);
        gbc_yAxisList.gridx = 4;
        gbc_yAxisList.gridy = 0;
        yAxisList.setActionCommand("yaxis");
        yAxisList.addActionListener(this);
        yAxisList.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXX");
        cntlPanel.add(yAxisList, gbc_yAxisList);

        JButton btnRemYAxisTempl = new JButton("Remove");
        GridBagConstraints gbc_btnRemYAxisTempl = new GridBagConstraints();
        gbc_btnRemYAxisTempl.anchor = GridBagConstraints.EAST;
        gbc_btnRemYAxisTempl.insets = new Insets(1, 5, 1, 1);
        gbc_btnRemYAxisTempl.gridx = 5;
        gbc_btnRemYAxisTempl.gridy = 0;
        btnRemYAxisTempl.setActionCommand("remytempl");
        btnRemYAxisTempl.addActionListener(this);
        cntlPanel.add(btnRemYAxisTempl, gbc_btnRemYAxisTempl);
    }
    
    public void createDataTablePanel(JPanel dataPanel) {    	
        JPanel tblPanel = new JPanel();
        GridBagConstraints gbc_tblPanel = new GridBagConstraints();
        gbc_tblPanel.insets = new Insets(3, 3, 3, 3);
        gbc_tblPanel.anchor = GridBagConstraints.NORTHWEST;
        gbc_tblPanel.fill = GridBagConstraints.BOTH;
        gbc_tblPanel.gridx = 0;
        gbc_tblPanel.gridy = 5;
        dataPanel.add(tblPanel, gbc_tblPanel);
        
        GridBagLayout gbl_cntlPanel = new GridBagLayout();
        gbl_cntlPanel.columnWidths = new int[]{0,0};
        gbl_cntlPanel.rowHeights = new int[]{0, 0};
        gbl_cntlPanel.columnWeights = new double[]{1.0,0.0};
        gbl_cntlPanel.rowWeights = new double[]{0.0, 1.0};
        tblPanel.setLayout(gbl_cntlPanel);
        
        JButton btnClearData = new JButton("Clear");
        GridBagConstraints gbc_btnClearData = new GridBagConstraints();
        gbc_btnClearData.anchor = GridBagConstraints.EAST;
        gbc_btnClearData.insets = new Insets(1, 5, 1, 1);
        gbc_btnClearData.gridx = 0;
        gbc_btnClearData.gridy = 0;
        btnClearData.setActionCommand("cleartempl");
        btnClearData.addActionListener(this);
        tblPanel.add(btnClearData, gbc_btnClearData);

        JButton btnValidateData = new JButton("Validate");
        GridBagConstraints gbc_btnValidateData = new GridBagConstraints();
        gbc_btnValidateData.anchor = GridBagConstraints.EAST;
        gbc_btnValidateData.insets = new Insets(1, 5, 1, 1);
        gbc_btnValidateData.gridx = 1;
        gbc_btnValidateData.gridy = 0;
        btnValidateData.setActionCommand("validate");
        btnValidateData.addActionListener(this);
        tblPanel.add(btnValidateData, gbc_btnValidateData);

        templateTable = new JTable();
        templateTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        templateTable.setColumnSelectionAllowed(true);
        templateTable.setCellSelectionEnabled(true);
        templateTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        templateTable.setTableHeader(null);
        templateTable.setModel(new DefaultTableModel(20, 15));
        Utils.initializeTable(templateTable, ColumnWidth);
        Format[][] formatMatrix = { { new DecimalFormat("#"), new DecimalFormat("0.00") } };
        NumberFormatRenderer renderer = (NumberFormatRenderer)templateTable.getDefaultRenderer(Object.class);
        renderer.setFormats(formatMatrix);
        excelAdapter.addTable(templateTable, true, true);
        
        GridBagConstraints gbc_templTable = new GridBagConstraints();
        gbc_templTable.insets = new Insets(5, 0, 0, 0);
        gbc_templTable.fill = GridBagConstraints.BOTH;
        gbc_templTable.weightx = 1.0;
        gbc_templTable.weighty = 1.0;
        gbc_templTable.gridx = 0;
        gbc_templTable.gridy = 1;
        gbc_templTable.gridwidth = 2;
        
        JScrollPane scrollPane = new JScrollPane(templateTable);
        tblPanel.add(scrollPane, gbc_templTable);
    }

    /**
     * Methods sets the axis table from axis array if they have been set before
     */
    private void setAxisTables() {
    	try {
    	    if (xAxisArray.size() > 0) {
		        Utils.ensureColumnCount(xAxisArray.size(), xAxisTable);
		        Utils.clearTable(xAxisTable);
		    	for (int i = 0; i < xAxisArray.size(); ++i)
		    		xAxisTable.setValueAt(xAxisArray.get(i), 0, i);
		        for (int i = xAxisTable.getColumnCount() - 1; i >= 0 && xAxisTable.getValueAt(0, i).toString().equals(""); --i)
		            Utils.removeColumn(i, xAxisTable);
    	    }
    	    if (yAxisArray.size() > 0) {
		        Utils.ensureColumnCount(yAxisArray.size(), yAxisTable);
		        Utils.clearTable(yAxisTable);
		    	for (int i = 0; i < yAxisArray.size(); ++i)
		    		yAxisTable.setValueAt(yAxisArray.get(i), 0, i);
		        for (int i = yAxisTable.getColumnCount() - 1; i >= 0 && yAxisTable.getValueAt(0, i).toString().equals(""); --i)
		            Utils.removeColumn(i, yAxisTable);
    	    }
    	}
    	catch (Exception e) {
            logger.error(e);
    	}
    }
    
    /**
     * Method validates that all data is populated and valid.
     * @param fuelingTable, table to be checked
     * @return
     */
    private boolean validateTemplateData() {
    	try {
	        // check if table is empty
	        if (Utils.isTableEmpty(templateTable))
	        	return true;
	        // check paste format
	        if (!templateTable.getValueAt(0, 0).toString().equalsIgnoreCase("[table3d]") &&
	            !((templateTable.getValueAt(0, 0).toString().equals("")) &&
	              Pattern.matches(Utils.fpRegex, templateTable.getValueAt(0, 1).toString()) &&
	              Pattern.matches(Utils.fpRegex, templateTable.getValueAt(1, 0).toString()))) {
	            JOptionPane.showMessageDialog(null, "Invalid data in table.\n\nPlease post data into first cell", "Error", JOptionPane.ERROR_MESSAGE);
	            return false;
	        }
	        if (templateTable.getValueAt(0, 0).toString().equalsIgnoreCase("[table3d]")) {
	            // realign if paste is from RomRaider
	            if (templateTable.getValueAt(0, 1).toString().equals("")) {
	                Utils.removeRow(0, templateTable);
	                for (int i = templateTable.getColumnCount() - 2; i >= 0; --i)
	                	templateTable.setValueAt(templateTable.getValueAt(0, i), 0, i + 1);
	                templateTable.setValueAt("", 0, 0);
	            }
	            // paste is probably from excel, just blank out the first cell
	            else
	            	templateTable.setValueAt("", 0, 0);
	        }
	        // remove extra rows
	        for (int i = templateTable.getRowCount() - 1; i >= 0 && templateTable.getValueAt(i, 0).toString().equals(""); --i)
	            Utils.removeRow(i, templateTable);
	        // remove extra columns
	        for (int i = templateTable.getColumnCount() - 1; i >= 0 && templateTable.getValueAt(0, i).toString().equals(""); --i)
	            Utils.removeColumn(i, templateTable);
	        // validate row/column headers cells are numeric
	        for (int i = 1; i < templateTable.getRowCount(); ++i) {
                if (!Pattern.matches(Utils.fpRegex, templateTable.getValueAt(i, 0).toString())) {
                    JOptionPane.showMessageDialog(null, "Invalid value at row " + (i + 1) + " column 1", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
	        }
            for (int i = 1; i < templateTable.getColumnCount(); ++i) {
                if (!Pattern.matches(Utils.fpRegex, templateTable.getValueAt(0, i).toString())) {
                    JOptionPane.showMessageDialog(null, "Invalid value at row 1 column " + (i + 1), "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
	        Utils.colorTableHeaders(templateTable);
	        // copy axis
	        Utils.clearTable(xAxisTable);
	        Utils.clearTable(yAxisTable);
	        Utils.ensureColumnCount(templateTable.getColumnCount() - 1, xAxisTable);
	        Utils.ensureColumnCount(templateTable.getRowCount() - 1, yAxisTable);
	        for (int i = 1; i < templateTable.getColumnCount(); ++i)
	        	xAxisTable.setValueAt(templateTable.getValueAt(0, i), 0, i - 1);
	        for (int i = 1; i < templateTable.getRowCount(); ++i)
	        	yAxisTable.setValueAt(templateTable.getValueAt(i, 0), 0, i - 1);
	        // remove extra columns
	        for (int i = xAxisTable.getColumnCount() - 1; i >= 0 && xAxisTable.getValueAt(0, i).toString().equals(""); --i)
	            Utils.removeColumn(i, xAxisTable);
	        for (int i = yAxisTable.getColumnCount() - 1; i >= 0 && yAxisTable.getValueAt(0, i).toString().equals(""); --i)
	            Utils.removeColumn(i, yAxisTable);
	        return true;
    	}
    	catch (Exception e) {
            logger.error(e);
            JOptionPane.showMessageDialog(null, e, "Error validation table data", JOptionPane.ERROR_MESSAGE);
    	}
    	return false;
    }
    
    /**
     * Methods saves axis table into config and adds a menu item in the template dropdown
     * @param type
     */
    private void save(Axis type) {
    	try {
	    	JTable axisTable = xAxisTable;
	    	JComboBox<String> axisList = xAxisList;
	    	if (type == Axis.YAXIS) {
	    		axisTable = yAxisTable;
	    		axisList = yAxisList;
	    	}
	        if (Utils.isTableEmpty(axisTable)) {
	        	JOptionPane.showMessageDialog(null, "Table is empty or not properly populated", "Error", JOptionPane.ERROR_MESSAGE);
	        	return;
	        }
	        String axisName = "";
	        JTextField nameTextField = new JTextField();
	        JComponent[] inputs = new JComponent[] { new JLabel("Set unique name"), nameTextField };
	        do {
	        	if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null, inputs, "Axis template name", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE))
	        		return;
	        	axisName = nameTextField.getText().trim();
	        	boolean found = false;
	        	for (int i = 0; i < xAxisList.getItemCount() && !found; ++i) {
	        		if (xAxisList.getItemAt(i).toString().equals(axisName))
	        			found = true;
	        	}
	        	if (found) {
	        		JOptionPane.showMessageDialog(null, "This name is being used for X-Axis template", "Error", JOptionPane.ERROR_MESSAGE);
	        		axisName = "";
	        	}
	        	else {
		        	for (int i = 0; i < yAxisList.getItemCount() && !found; ++i) {
		        		if (yAxisList.getItemAt(i).toString().equals(axisName))
		        			found = true;
		        	}
		        	if (found) {
		        		JOptionPane.showMessageDialog(null, "This name is being used for Y-Axis template", "Error", JOptionPane.ERROR_MESSAGE);
		        		axisName = "";
		        	}
	        	}
	        }
	        while (axisName.isEmpty());
	        
	        String axisValues = "";
	    	for (int i = 0; i < axisTable.getColumnCount(); ++i) {
		        if (!Pattern.matches(Utils.fpRegex, axisTable.getValueAt(0, i).toString())) {
		        	JOptionPane.showMessageDialog(null, "Invalid data in table, column " + (i + 1), "Error", JOptionPane.ERROR_MESSAGE);
		            return;
		        }
		        if (i > 0)
		        	axisValues += ",";
		        axisValues += axisTable.getValueAt(0, i).toString();
	    	}
        	if (type == Axis.XAXIS) {
        		String s = Config.getXAxisTemplates();
        		if (s.endsWith(","))
            		Config.setXAxisTemplates(s + axisName);
        		else
        			Config.setXAxisTemplates(s + "," + axisName);
        	}
        	else {
        		String s = Config.getYAxisTemplates();
        		if (s.endsWith(","))
        			Config.setYAxisTemplates(s + axisName);
        		else
        			Config.setYAxisTemplates(s + "," + axisName);
        	}
        	Config.setProperty(axisName, axisValues);
        	if (axisList.getItemCount() == 0)
        		axisList.addItem("");
    		axisList.addItem(axisName);
    		axisList.setSelectedItem(axisName);
    	}
    	catch (Exception e) {
            logger.error(e);
            JOptionPane.showMessageDialog(null, e, "Error saving axis", JOptionPane.ERROR_MESSAGE);
    	}
    }
    
    /**
     * Method removes current axis template from templates dropdown and config
     * @param type
     */
    private void remove(Axis type) {
    	try {
    		String newNames = "";
    		String[] axisNames = Config.getXAxisTemplates().split(",");
	    	JComboBox<String> axisList = xAxisList;
	    	if (type == Axis.YAXIS) {
	    		axisList = yAxisList;
	    		axisNames = Config.getYAxisTemplates().split(",");
	    	}
	    	if (axisList.getSelectedItem() == null)
	    		return;
	    	String axisName = axisList.getSelectedItem().toString();
	    	if (axisName.isEmpty())
	    		return;
        	if (axisList.getItemCount() == 0)
        		axisList.addItem("");
	    	axisList.setSelectedItem("");
	    	axisList.removeItem(axisName);
	    	Config.removeProperty(axisName);
    		for (String s : axisNames) {
    			if (s.isEmpty() || s.equals(axisName))
    				continue;
    			newNames += ("," + s);
    		}
    		if (newNames.isEmpty())
    			newNames = ",";
	    	if (type == Axis.XAXIS)
	    		Config.setXAxisTemplates(newNames);
	    	else
	    		Config.setYAxisTemplates(newNames);
    	}
    	catch (Exception e) {
            logger.error(e);
            JOptionPane.showMessageDialog(null, e, "Error removing axis template", JOptionPane.ERROR_MESSAGE);
    	}
    }
    
    /**
     * Method loads from config selected axis template into axis table
     * @param type
     */
    private void load(Axis type) {
    	try {
	    	JTable axisTable = xAxisTable;
	    	JComboBox<String> axisList = xAxisList;
	    	if (type == Axis.YAXIS) {
	    		axisTable = yAxisTable;
	    		axisList = yAxisList;
	    	}
	    	if (axisList.getSelectedItem() == null)
	    		return;
	    	String axisName = axisList.getSelectedItem().toString();
	    	if (axisName.isEmpty())
	    		return;
	    	String[] values = Config.getProperty(axisName).split(",");
	        Utils.ensureColumnCount(values.length, axisTable);
	        Utils.clearTable(axisTable);
	    	for (int i = 0; i < values.length; ++i)
	    		axisTable.setValueAt(values[i], 0, i);
	        for (int i = axisTable.getColumnCount() - 1; i >= 0 && axisTable.getValueAt(0, i).toString().equals(""); --i)
	            Utils.removeColumn(i, axisTable);
    	}
    	catch (Exception e) {
            logger.error(e);
            JOptionPane.showMessageDialog(null, e, "Error load axis template", JOptionPane.ERROR_MESSAGE);
    	}
    }
    
    /**
     * Methods validates axis table and makes sure at least one axes is specified
     * @return
     */
    private boolean validateAxisData() {
    	try {
	        if (Utils.isTableEmpty(xAxisTable) && Utils.isTableEmpty(yAxisTable)) {
	        	JOptionPane.showMessageDialog(null, "Both axis tables are either empty or not properly populated", "Error", JOptionPane.ERROR_MESSAGE);
	        	return false;
	        }
	        ArrayList<Double> tempArray = new ArrayList<Double>();
	        if (!Utils.isTableEmpty(xAxisTable)) {
		    	for (int i = 0; i < xAxisTable.getColumnCount(); ++i) {
			        if (!Pattern.matches(Utils.fpRegex, xAxisTable.getValueAt(0, i).toString())) {
			        	JOptionPane.showMessageDialog(null, "Invalid data in X-Axis table, column " + (i + 1), "Error", JOptionPane.ERROR_MESSAGE);
			            return false;
			        }
			        tempArray.add(Double.valueOf(xAxisTable.getValueAt(0, i).toString()));
		    	}
		    	xAxisArray.clear();
		    	for (Double d : tempArray)
		    		xAxisArray.add(d);
		    	Collections.sort(xAxisArray);
	        }
	        tempArray.clear();
	        if (!Utils.isTableEmpty(yAxisTable)) {
		    	for (int i = 0; i < yAxisTable.getColumnCount(); ++i) {
			        if (!Pattern.matches(Utils.fpRegex, yAxisTable.getValueAt(0, i).toString())) {
			        	JOptionPane.showMessageDialog(null, "Invalid data in Y-Axis table, column " + (i + 1), "Error", JOptionPane.ERROR_MESSAGE);
			            return false;
			        }
			        tempArray.add(Double.valueOf(yAxisTable.getValueAt(0, i).toString()));
		    	}
		    	yAxisArray.clear();
		    	for (Double d : tempArray)
		    		yAxisArray.add(d);
		    	Collections.sort(yAxisArray);
	        }
	        return true;
    	}
    	catch (Exception e) {
            logger.error(e);
            JOptionPane.showMessageDialog(null, e, "Error setting axis", JOptionPane.ERROR_MESSAGE);
    	}
    	return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("clearxaxis".equals(e.getActionCommand())) {
            Utils.clearTable(xAxisTable);
            xAxisList.setSelectedIndex(-1);
        }
        else if ("clearyaxis".equals(e.getActionCommand())) {
        	Utils.clearTable(yAxisTable);
            yAxisList.setSelectedIndex(-1);
        }
        else if ("cleartempl".equals(e.getActionCommand()))
        	Utils.clearTable(templateTable);
        else if ("validate".equals(e.getActionCommand()))
            validateTemplateData();
        else if ("savexaxis".equals(e.getActionCommand()))
        	save(Axis.XAXIS);
        else if ("saveyaxis".equals(e.getActionCommand()))
        	save(Axis.YAXIS);
        else if ("remxtempl".equals(e.getActionCommand()))
        	remove(Axis.XAXIS);
        else if ("remytempl".equals(e.getActionCommand()))
        	remove(Axis.YAXIS);
        else if ("xaxis".equals(e.getActionCommand()))
        	load(Axis.XAXIS);
        else if ("yaxis".equals(e.getActionCommand()))
        	load(Axis.YAXIS);
    }
}

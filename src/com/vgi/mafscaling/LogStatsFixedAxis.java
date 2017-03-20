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
import java.awt.Component;
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
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;

public class LogStatsFixedAxis implements ActionListener {
    private static final Logger logger = Logger.getLogger(LogStatsFixedAxis.class);
    private enum Axis {XAXIS, YAXIS};
    private final static int ColumnWidth = 55;
    private final static int AxisColumnCount = 25;
    private ExcelAdapter excelAdapter = new ExcelAdapter();
    private JTable xAxisTable = null;
    private JTable yAxisTable = null;
    private JTable templateTable = null;
    private JPanel dataPanel = null;
    private JSpinner distanceSpinner = null;
    private JComboBox<String> xAxisList = null;
    private JComboBox<String> yAxisList = null;
    private ArrayList<Double> xAxisArray;
    private ArrayList<Double> yAxisArray;
    private ArrayList<Integer> distance;
    private Insets insets0 = new Insets(0, 0, 0, 0);
    private Insets insets3 = new Insets(3, 3, 3, 3);
    
    public LogStatsFixedAxis(ArrayList<Integer> distancePct, ArrayList<Double> xAxis, ArrayList<Double> yAxis) {
        xAxisArray = xAxis;
        yAxisArray = yAxis;
        distance = distancePct;
        initialize();
    }
    
    private void initialize() {
        String templNames;
        
        dataPanel = new JPanel();
        GridBagLayout gbl_dataPanel = new GridBagLayout();
        gbl_dataPanel.columnWidths = new int[]{0, 0, 0, 0, 0};
        gbl_dataPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_dataPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0};
        gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        dataPanel.setLayout(gbl_dataPanel);
        dataPanel.setPreferredSize(new Dimension(620, 550));
        
        GridBagConstraints gbc_fullWidth = new GridBagConstraints();
        gbc_fullWidth.anchor = GridBagConstraints.PAGE_START;
        gbc_fullWidth.fill = GridBagConstraints.HORIZONTAL;
        gbc_fullWidth.insets = insets3;
        gbc_fullWidth.gridwidth = 5;
        gbc_fullWidth.gridx = 0;
        gbc_fullWidth.gridy = 0;

        JLabel noteLabel = new JLabel("Select saved, manually enter, or paste axis values into tables below");
        noteLabel.setForeground(Color.BLUE);
        dataPanel.add(noteLabel, gbc_fullWidth);
        
        // add controls for x-axis
        templNames = Config.getXAxisTemplates();
        if (templNames.isEmpty())
            templNames = ",";
        gbc_fullWidth.gridy++;
        addLabel(gbc_fullWidth.gridy, 0, "Saved X-Axis");
        xAxisList = addComboBox(gbc_fullWidth.gridy, 1, templNames, "xaxis");
        addButton(gbc_fullWidth.gridy, 2, "Clear", "clearxaxis");
        addButton(gbc_fullWidth.gridy, 3, "Save", "savexaxis");
        addButton(gbc_fullWidth.gridy, 4, "Remove", "remxtempl");
        
        // add x-axis table
        gbc_fullWidth.gridy++;
        xAxisTable = createAxisPanel(gbc_fullWidth.gridy, "X-Axis");

        gbc_fullWidth.gridy++;
        dataPanel.add(new JLabel(" "), gbc_fullWidth);
        
        // add controls for y-axis
        templNames = Config.getYAxisTemplates();
        if (templNames.isEmpty())
            templNames = ",";
        gbc_fullWidth.gridy++;
        addLabel(gbc_fullWidth.gridy, 0, "Saved Y-Axis");
        yAxisList = addComboBox(gbc_fullWidth.gridy, 1, templNames, "yaxis");
        addButton(gbc_fullWidth.gridy, 2, "Clear", "clearyaxis");
        addButton(gbc_fullWidth.gridy, 3, "Save", "saveyaxis");
        addButton(gbc_fullWidth.gridy, 4, "Remove", "remytempl");
        
        // add y-axis table
        gbc_fullWidth.gridy++;
        yAxisTable = createAxisPanel(gbc_fullWidth.gridy, "Y-Axis");

        gbc_fullWidth.gridy++;
        noteLabel = new JLabel("You can paste a table below and first row / first column will be used as axis");
        noteLabel.setForeground(Color.BLUE);
        dataPanel.add(noteLabel, gbc_fullWidth);

        gbc_fullWidth.gridy++;
        addButton(gbc_fullWidth.gridy, 3, "Clear", "cleartempl");
        addButton(gbc_fullWidth.gridy, 4, "Validate", "validate");
        
        gbc_fullWidth.gridy++;
        createAxisTablePanel(gbc_fullWidth.gridy);

        gbc_fullWidth.gridy++;
        noteLabel = new JLabel("<html>You can set specific distance % to get data that falls as close the the axis points as possible (where 50% is the mid-point between two axis points)<html>");
        noteLabel.setForeground(Color.BLUE);
        dataPanel.add(noteLabel, gbc_fullWidth);

        gbc_fullWidth.gridy++;
        addLabel(gbc_fullWidth.gridy, 0, "Distance %");
        distanceSpinner = addSpinnerFilter(gbc_fullWidth.gridy, 1, distance.get(0).intValue(), 1, 50, 1);

        setAxisTables();
        
        JComponent[] inputs = new JComponent[] { dataPanel };
        do {
            if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null, inputs, "Fixed size axis", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE))
                return;
        }
        while (!validateAxisData());
    }

    private JTable createAxisPanel(int row, String tableName) {
        JTable axisTable = new JTable() {
            private static final long serialVersionUID = -731761682656923981L;
            public Component prepareEditor(TableCellEditor editor, int row, int column) {
                Component c = super.prepareEditor(editor, row, column);
                if (c instanceof JTextComponent)
                    ((JTextComponent) c).selectAll();
                return c;
            }
        };
        axisTable.setColumnSelectionAllowed(true);
        axisTable.setCellSelectionEnabled(true);
        axisTable.setBorder(new LineBorder(Color.GRAY));
        axisTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        axisTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        axisTable.setModel(new DefaultTableModel(1, AxisColumnCount));
        axisTable.setTableHeader(null);
        Utils.initializeTable(axisTable, ColumnWidth);
        excelAdapter.addTable(axisTable, false, false, false, false, true, false, true, false, true);
        
        JScrollPane axisScrollPane = new JScrollPane(axisTable);
        axisScrollPane.setViewportBorder(new TitledBorder(null, tableName, TitledBorder.LEADING, TitledBorder.TOP, null, null));
        axisScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        axisScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        GridBagConstraints gbc_axisScrollPane = new GridBagConstraints();
        gbc_axisScrollPane.insets = insets0;
        gbc_axisScrollPane.anchor = GridBagConstraints.NORTH;
        gbc_axisScrollPane.fill = GridBagConstraints.HORIZONTAL;
        gbc_axisScrollPane.weightx = 1.0;
        gbc_axisScrollPane.gridx = 0;
        gbc_axisScrollPane.gridy = row;
        gbc_axisScrollPane.gridwidth = 5;
        gbc_axisScrollPane.ipady = 10;
        dataPanel.add(axisScrollPane, gbc_axisScrollPane);
        
        return axisTable;
    }

    public void createAxisTablePanel(int row) {
        templateTable = new JTable() {
            private static final long serialVersionUID = -5292434981612251526L;
            public Component prepareEditor(TableCellEditor editor, int row, int column) {
                Component c = super.prepareEditor(editor, row, column);
                if (c instanceof JTextComponent)
                    ((JTextComponent) c).selectAll();
                return c;
            }
        };
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
        gbc_templTable.gridy = row;
        gbc_templTable.gridwidth = 5;
        
        JScrollPane scrollPane = new JScrollPane(templateTable);
        dataPanel.add(scrollPane, gbc_templTable);
    }
    
    private void addLabel(int row, int column, String text) {
        JLabel label = new JLabel(text);
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.anchor = GridBagConstraints.EAST;
        gbc_label.insets = new Insets(3, 0, 3, 0);
        gbc_label.gridx = column;
        gbc_label.gridy = row;
        dataPanel.add(label, gbc_label);
    }
    
    private JComboBox<String> addComboBox(int row, int column, String names, String action) {
        JComboBox<String> combo = new JComboBox<String>(names.split(","));
        combo.setActionCommand(action);
        combo.addActionListener(this);
        combo.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXX");
        GridBagConstraints gbc_combo = new GridBagConstraints();
        gbc_combo.anchor = GridBagConstraints.WEST;
        gbc_combo.insets = new Insets(1, 5, 1, 1);
        gbc_combo.gridx = column;
        gbc_combo.gridy = row;
        dataPanel.add(combo, gbc_combo);
        return combo;
    }

    private JSpinner addSpinnerFilter(int row, int column, int value, int min, int max, int step) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        GridBagConstraints gbc_spinner = new GridBagConstraints();
        gbc_spinner.anchor = GridBagConstraints.WEST;
        gbc_spinner.insets = new Insets(1, 5, 1, 1);
        gbc_spinner.gridx = column;
        gbc_spinner.gridy = row;
        dataPanel.add(spinner, gbc_spinner);
        return spinner;
    }
    
    private void addButton(int row, int column, String name, String action) {
        JButton button = new JButton(name);
        GridBagConstraints gbc_button = new GridBagConstraints();
        gbc_button.anchor = GridBagConstraints.EAST;
        gbc_button.insets = new Insets(1, 5, 1, 1);
        gbc_button.gridx = column;
        gbc_button.gridy = row;
        button.setActionCommand(action);
        button.addActionListener(this);
        dataPanel.add(button, gbc_button);
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
     * @return
     */
    private boolean validateTemplateData() {
        try {
            if (!Utils.validateTableHeader(templateTable))
                return false;
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
            String val;
            int i = 0;
            for (; i < axisTable.getColumnCount(); ++i) {
                val = axisTable.getValueAt(0, i).toString();
                if (val.isEmpty())
                    break;
                else if (!Pattern.matches(Utils.fpRegex, val)) {
                    JOptionPane.showMessageDialog(null, "Invalid data in table, column " + (i + 1), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (i > 0)
                    axisValues += ",";
                axisValues += val;
            }
            for (; i < axisTable.getColumnCount(); ++i)
                axisTable.setValueAt("", 0, i);
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
            String val;
            int i = 0;
            ArrayList<Double> tempArray = new ArrayList<Double>();
            if (!Utils.isTableEmpty(xAxisTable)) {
                for (i = 0; i < xAxisTable.getColumnCount(); ++i) {
                    val = xAxisTable.getValueAt(0, i).toString();
                    if (val.isEmpty())
                        break;
                    else if (!Pattern.matches(Utils.fpRegex, val)) {
                        JOptionPane.showMessageDialog(null, "Invalid data in X-Axis table, column " + (i + 1), "Error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    tempArray.add(Double.valueOf(val));
                }
                for (; i < xAxisTable.getColumnCount(); ++i)
                    xAxisTable.setValueAt("", 0, i);
                xAxisArray.clear();
                for (Double d : tempArray)
                    xAxisArray.add(d);
                Collections.sort(xAxisArray);
            }
            tempArray.clear();
            if (!Utils.isTableEmpty(yAxisTable)) {
                for (i = 0; i < yAxisTable.getColumnCount(); ++i) {
                    val = yAxisTable.getValueAt(0, i).toString();
                    if (val.isEmpty())
                        break;
                    if (!Pattern.matches(Utils.fpRegex, val)) {
                        JOptionPane.showMessageDialog(null, "Invalid data in Y-Axis table, column " + (i + 1), "Error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    tempArray.add(Double.valueOf(val));
                }
                for (; i < yAxisTable.getColumnCount(); ++i)
                    yAxisTable.setValueAt("", 0, i);
                yAxisArray.clear();
                for (Double d : tempArray)
                    yAxisArray.add(d);
                Collections.sort(yAxisArray);
            }
            distance.set(0, (Integer)distanceSpinner.getValue());
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
            xAxisTable.setModel(new DefaultTableModel(1, AxisColumnCount));
            Utils.initializeTable(xAxisTable, ColumnWidth);
            xAxisList.setSelectedIndex(-1);
        }
        else if ("clearyaxis".equals(e.getActionCommand())) {
            yAxisTable.setModel(new DefaultTableModel(1, AxisColumnCount));
            Utils.initializeTable(yAxisTable, ColumnWidth);
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

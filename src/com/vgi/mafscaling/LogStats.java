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
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.apache.log4j.Logger;
import org.math.plot.Plot3DPanel;
import org.math.plot.plots.HistogramPlot3D;

public class LogStats extends FCTabbedPane implements ActionListener {
    private enum Statistics {COUNT, MINIMUM, MAXIMUM, MEAN, MEDIAN, MODE, RANGE, VARIANCE, STDDEV};
    private enum Plot3D {GRID, HIST, BAR, LINE, SCATTER};
    private enum DataFilter {NONE, LESS, LESS_EQUAL, EQUAL, GREATER_EQUAL, GREATER};
    private static final long serialVersionUID = -7486851151646396168L;
    private static final Logger logger = Logger.getLogger(LogStats.class);
    private static final int ColumnWidth = 50;
    private static final int DataTableRowCount = 50;
    private static final int DataTableColumnCount = 25;
    private static final String SetFilters = "<html><center>Set<br>Filters</center></html>";
    private static final String ViewFilters = "<html><center>View<br>Filters</center></html>";
    
    public class JFmtTextField extends JFormattedTextField {
        private static final long serialVersionUID = -8767848259312656943L;
        public JFmtTextField(NumberFormat format) {
            super(format);  
        }
        @Override  
        protected void processFocusEvent(final FocusEvent e) {  
            if (e.isTemporary()) {
                return;
            }
      
            if (e.getID() == FocusEvent.FOCUS_LOST) {
                if (getText() == null || getText().isEmpty()) {
                    setValue(null);
                }
            }
            super.processFocusEvent(e);  
        }  
    };

    private File[] logFiles = null;
    private ArrayList<File> logFilesTodo = null;
    private JComboBox<String> xAxisColumn = null;
    private JComboBox<String> yAxisColumn = null;
    private JMultiSelectionBox dataColumn = null;
    private JComboBox<String> statistics = null;
    private JFormattedTextField xAxisRoundTextBox = null;
    private JFormattedTextField yAxisRoundTextBox = null;
    private JFormattedTextField hitCountTextBox = null;
    private ArrayList<JButton> filterButtonList = null;
    private ArrayList<JComboBox<String>> filterComboBoxList = null;
    private ArrayList<JComboBox<String>> filterColumnList = null;
    private ArrayList<JFormattedTextField> filterTextBoxList = null;
    private JTable dataTable = null;
    private JPanel cntlPanel = null;
    private JLabel labelFileName = null;
    private JButton btnSetFiltersButton = null;
    private ExcelAdapter excelAdapter = null;
    private HashMap<Double, HashMap<Double, ArrayList<Double>>> xData = null;
    private Plot3DPanel plot = null;
    private ButtonGroup rbGroup = new ButtonGroup();
    private ArrayList<Double> xAxisArray;
    private ArrayList<Double> yAxisArray;
    private int distance = 50;
    private Insets insets0 = new Insets(0, 0, 0, 0);
    private Insets insets3 = new Insets(3, 3, 3, 3);
    private UIDefaults buttonInsets6 = new UIDefaults();
    private UIDefaults buttonInsets10 = new UIDefaults();

    public LogStats(int tabPlacement) {
        super(tabPlacement);
        initialize();
    }

    private void initialize() {
        excelAdapter = new ExcelAdapter();
        xAxisArray = new ArrayList<Double>();
        yAxisArray = new ArrayList<Double>();
        for (String s : Config.getLastXAxis().split(","))
            if (!s.isEmpty()) xAxisArray.add(Double.valueOf(s));
        for (String s : Config.getLastYAxis().split(","))
            if (!s.isEmpty()) yAxisArray.add(Double.valueOf(s));
        filterButtonList = new ArrayList<JButton>();
        filterComboBoxList = new ArrayList<JComboBox<String>>();
        filterColumnList = new ArrayList<JComboBox<String>>();
        filterTextBoxList = new ArrayList<JFormattedTextField>();
        createDataTab();
        createGraghTab();
        createUsageTab();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // DATA TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void createDataTab() {
        JPanel dataPanel = new JPanel();
        add(dataPanel, "<html><div style='text-align: center;'>D<br>a<br>t<br>a</div></html>");
        GridBagLayout gbl_dataPanel = new GridBagLayout();
        gbl_dataPanel.columnWidths = new int[] {0};
        gbl_dataPanel.rowHeights = new int[] {0, 0};
        gbl_dataPanel.columnWeights = new double[]{0.0};
        gbl_dataPanel.rowWeights = new double[]{0.0, 1.0};
        dataPanel.setLayout(gbl_dataPanel);

        createControlPanel(dataPanel);
        createDataPanel(dataPanel);
    }
    
    private void createControlPanel(JPanel dataPanel) {
        try {
            NumberFormat doubleFmt = NumberFormat.getNumberInstance();
            doubleFmt.setGroupingUsed(false);
            doubleFmt.setMaximumFractionDigits(2);
            doubleFmt.setMinimumFractionDigits(1);
            doubleFmt.setRoundingMode(RoundingMode.HALF_UP);
            
            NumberFormat doubleFmtFilters = NumberFormat.getNumberInstance();
            doubleFmtFilters.setGroupingUsed(false);
            doubleFmtFilters.setMaximumFractionDigits(6);
            doubleFmtFilters.setMinimumFractionDigits(0);
            doubleFmtFilters.setRoundingMode(RoundingMode.HALF_UP);
            
            buttonInsets6.put("Button.contentMargins", new Insets(6, 6, 6, 6));
            buttonInsets10.put("Button.contentMargins", new Insets(6, 10, 6, 10));
            
            cntlPanel = new JPanel();
            GridBagConstraints gbc_ctrlPanel = new GridBagConstraints();
            gbc_ctrlPanel.insets = insets3;
            gbc_ctrlPanel.anchor = GridBagConstraints.PAGE_START;
            gbc_ctrlPanel.fill = GridBagConstraints.HORIZONTAL;
            gbc_ctrlPanel.weightx = 1.0;
            gbc_ctrlPanel.gridx = 0;
            gbc_ctrlPanel.gridy = 0;
            dataPanel.add(cntlPanel, gbc_ctrlPanel);
            
            GridBagLayout gbl_cntlPanel = new GridBagLayout();
            gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            gbl_cntlPanel.rowHeights = new int[]{0, 0, 0};
            gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.2, 0.0, 1.0};
            gbl_cntlPanel.rowWeights = new double[]{0.0, 0.0, 0.0};
            cntlPanel.setLayout(gbl_cntlPanel);
            
            addLabel(2, 1, "CellHit Min Cnt");
            hitCountTextBox = addTextField(2, 2, 3, NumberFormat.getIntegerInstance(), true);
            
            JButton selectLogButton = addButton(0, 0, 2, "<html><center>Select<br>Log</center></html>", "selectlog");
            selectLogButton.setMargin(new Insets(3, 5, 3, 5));
            selectLogButton.putClientProperty("Nimbus.Overrides", buttonInsets6);
    
            addLabel(0, 1, "X-Axis");
            xAxisColumn = addComboBox(0, 2, null);
            
            addLabel(1, 1, "Y-Axis");
            yAxisColumn = addComboBox(1, 2, null);
            
            addLabel(0, 3, "Data");
            dataColumn = new JMultiSelectionBox();
            GridBagConstraints gbc_dataColumn = new GridBagConstraints();
            gbc_dataColumn.anchor = GridBagConstraints.WEST;
            gbc_dataColumn.insets = new Insets(3, 0, 3, 3);
            gbc_dataColumn.gridx = 4;
            gbc_dataColumn.gridy = 0;
            dataColumn.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXX");
            cntlPanel.add(dataColumn, gbc_dataColumn);

            addLabel(1, 3, "Stats");
            statistics = addComboBox(1, 4, new String[] {"Count", "Minimum", "Maximum", "Mean", "Median", "Mode", "Range", "Variance", "Standard Deviation"});
            
            addLabel(0, 5, "X-Step");
            xAxisRoundTextBox = addTextField(0, 6, 5, doubleFmt, true);
    
            addLabel(1, 5, "Y-Step");
            yAxisRoundTextBox = addTextField(1, 6, 5, doubleFmt, true);
    
            addLabel(0, 7, "or");

            JButton btnSetAxisButton = addButton(0, 8, 2, "<html><center>Set<br>Axis</center></html>", "setaxis");
            btnSetAxisButton.setMargin(new Insets(3, 9, 3, 9));
            btnSetAxisButton.putClientProperty("Nimbus.Overrides", buttonInsets10);

            btnSetFiltersButton = addButton(0, 9, 2, SetFilters, "setfilters");
            btnSetFiltersButton.setMargin(new Insets(3, 5, 3, 5));
            btnSetFiltersButton.putClientProperty("Nimbus.Overrides", buttonInsets6);

            JButton clearFiltersButton = addButton(0, 10, 2, "<html><center>Clear<br>Filters</center></html>", "clearfilters");
            clearFiltersButton.setMargin(new Insets(3, 5, 3, 5));
            clearFiltersButton.putClientProperty("Nimbus.Overrides", buttonInsets6);

            addButton(0, 11, 2, "GO", "go");

            labelFileName = new JLabel("");
            labelFileName.setHorizontalAlignment(SwingConstants.LEFT);
            GridBagConstraints gbc_label = new GridBagConstraints();
            gbc_label.anchor = GridBagConstraints.WEST;
            gbc_label.fill = GridBagConstraints.HORIZONTAL;
            gbc_label.insets = insets3;
            gbc_label.gridx = 3;
            gbc_label.gridy = 2;
            gbc_label.gridwidth = gbl_cntlPanel.columnWidths.length - gbc_label.gridx;
            cntlPanel.add(labelFileName, gbc_label);
        }
        catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }
    
    private void createDataPanel(JPanel dataPanel) {
        TableColumnModel dataTableModel = new DefaultTableColumnModel();
        dataTableModel.addColumn(new TableColumn(0, 250));
        
        dataTable = new JTable() {
            private static final long serialVersionUID = 6526901361175099297L;
            public boolean isCellEditable(int row, int column) { return false; };
        };
        dataTable.setColumnSelectionAllowed(true);
        dataTable.setCellSelectionEnabled(true);
        dataTable.setBorder(new LineBorder(new Color(0, 0, 0)));
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        dataTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        dataTable.setModel(new DefaultTableModel(DataTableRowCount, DataTableColumnCount));
        dataTable.setTableHeader(null);
        Utils.addTableHeaderHighlight(dataTable);
        Utils.initializeTable(dataTable, ColumnWidth);
        
        Format[][] formatMatrix = { { new DecimalFormat("0.00"), new DecimalFormat("0.00") } };
        NumberFormatRenderer renderer = (NumberFormatRenderer)dataTable.getDefaultRenderer(Object.class);
        renderer.setFormats(formatMatrix);
        
        GridBagConstraints gbc_dataTable = new GridBagConstraints();
        gbc_dataTable.insets = insets3;
        gbc_dataTable.anchor = GridBagConstraints.PAGE_START;
        gbc_dataTable.fill = GridBagConstraints.BOTH;
        gbc_dataTable.weightx = 1.0;
        gbc_dataTable.weighty = 1.0;
        gbc_dataTable.gridx = 0;
        gbc_dataTable.gridy = 1;
        gbc_dataTable.gridwidth = 14;

        JScrollPane scrollPane = new JScrollPane(dataTable);
        dataPanel.add(scrollPane, gbc_dataTable);
        excelAdapter.addTable(dataTable, false, false, true, false, true, true, true, true, true);
    }
    
    private JButton addButton(int row, int column, int rowspan, String name, String action) {
        JButton button = new JButton(name);
        button.setActionCommand(action);
        button.addActionListener(this);
        GridBagConstraints gbc_button = new GridBagConstraints();
        gbc_button.anchor = GridBagConstraints.EAST;
        gbc_button.insets = insets3;
        gbc_button.gridx = column;
        gbc_button.gridy = row;
        gbc_button.gridheight = rowspan;
        cntlPanel.add(button, gbc_button);
        return button;
    }
    
    private void addLabel(int row, int column, String text) {
        JLabel label = new JLabel(text);
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.anchor = GridBagConstraints.EAST;
        gbc_label.insets = insets3;
        gbc_label.gridx = column;
        gbc_label.gridy = row;
        if (text.equals("or")) {
            gbc_label.gridheight = 2;
            gbc_label.insets = insets0;
        }
        cntlPanel.add(label, gbc_label);
    }
    
    private JComboBox<String> addComboBox(int row, int column, String[] values) {
        JComboBox<String> combo;
        if (values != null)
            combo = new JComboBox<String>(values);
        else {
            combo = new JComboBox<String>();
            combo.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXX");
        }
        GridBagConstraints gbc_combo = new GridBagConstraints();
        gbc_combo.anchor = GridBagConstraints.WEST;
        gbc_combo.insets = new Insets(3, 0, 3, 3);
        gbc_combo.gridx = column;
        gbc_combo.gridy = row;
        cntlPanel.add(combo, gbc_combo);
        return combo;
    }
    
    private JFormattedTextField addTextField(int row, int column, int numColumns, NumberFormat format, boolean fill) {
        JFormattedTextField textField = new JFormattedTextField(format);
        textField.setColumns(numColumns);
        textField.setMinimumSize(textField.getPreferredSize());
        textField.setMaximumSize(textField.getPreferredSize());
        GridBagConstraints gbc_textField = new GridBagConstraints();
        gbc_textField.anchor = GridBagConstraints.WEST;
        if (fill)
            gbc_textField.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField.insets = new Insets(3, 0, 3, 3);
        //gbc_textField.weightx = 1;
        gbc_textField.gridx = column;
        gbc_textField.gridy = row;
        cntlPanel.add(textField, gbc_textField);
        return textField;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE CHART TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void createGraghTab() {
        JPanel plotPanel = new JPanel();
        add(plotPanel, "<html><div style='text-align: center;'>3<br>D<br> <br>C<br>h<br>a<br>r<br>t</div></html>");
        GridBagLayout gbl_plotPanel = new GridBagLayout();
        gbl_plotPanel.columnWidths = new int[] {0};
        gbl_plotPanel.rowHeights = new int[] {0, 0};
        gbl_plotPanel.columnWeights = new double[]{1.0};
        gbl_plotPanel.rowWeights = new double[]{0.0, 1.0};
        plotPanel.setLayout(gbl_plotPanel);

        JPanel cntlPanel = new JPanel();
        GridBagConstraints gbl_ctrlPanel = new GridBagConstraints();
        gbl_ctrlPanel.insets = insets3;
        gbl_ctrlPanel.anchor = GridBagConstraints.NORTH;
        gbl_ctrlPanel.weightx = 1.0;
        gbl_ctrlPanel.fill = GridBagConstraints.HORIZONTAL;
        gbl_ctrlPanel.gridx = 0;
        gbl_ctrlPanel.gridy = 0;
        plotPanel.add(cntlPanel, gbl_ctrlPanel);
        
        GridBagLayout gbl_cntlPanel = new GridBagLayout();
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);

        addRadioButton(cntlPanel, 0, "Grid", "grid");
        addRadioButton(cntlPanel, 1, "Histogram", "hist");
        addRadioButton(cntlPanel, 2, "Bar", "bar");
        addRadioButton(cntlPanel, 3, "Line", "line");
        addRadioButton(cntlPanel, 4, "Scatter", "scatter");
        
        plot = new Plot3DPanel("SOUTH") {
            private static final long serialVersionUID = 7914951068593204419L;
            public void addPlotToolBar(String location) {
                super.addPlotToolBar(location);
                super.plotToolBar.remove(7);
                super.plotToolBar.remove(5);
                super.plotToolBar.remove(4);
            }            
        };
        plot.setAutoBounds();
        plot.setAutoscrolls(true);
        plot.setEditable(false);
        plot.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        plot.setForeground(Color.BLACK);
        plot.getAxis(0).setColor(Color.BLACK);
        plot.getAxis(1).setColor(Color.BLACK);
        plot.getAxis(2).setColor(Color.BLACK);
        
        GridBagConstraints gbl_chartPanel = new GridBagConstraints();
        gbl_chartPanel.anchor = GridBagConstraints.CENTER;
        gbl_chartPanel.insets = insets3;
        gbl_chartPanel.weightx = 1.0;
        gbl_chartPanel.weighty = 1.0;
        gbl_chartPanel.fill = GridBagConstraints.BOTH;
        gbl_chartPanel.gridx = 0;
        gbl_chartPanel.gridy = 1;
        plotPanel.add(plot, gbl_chartPanel);
    }
    
    protected void addRadioButton(JPanel panel, int column, String name, String action) {
        JRadioButton button = new JRadioButton(name);
        button.setActionCommand(action);
        button.addActionListener(this);
        rbGroup.add(button);
        GridBagConstraints gbc_button = new GridBagConstraints();
        gbc_button.anchor = GridBagConstraints.WEST;
        gbc_button.insets = new Insets(0, 0, 3, 3);
        gbc_button.gridx = column;
        gbc_button.gridy = 0;
        panel.add(button, gbc_button);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE USAGE TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void createUsageTab() {
        JTextPane  usageTextArea = new JTextPane();
        usageTextArea.setMargin(new Insets(10, 10, 10, 10));
        usageTextArea.setContentType("text/html");
        usageTextArea.setText(usage());
        usageTextArea.setEditable(false);
        usageTextArea.setCaretPosition(0);

        JScrollPane textScrollPane = new JScrollPane(usageTextArea);
        textScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        textScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(textScrollPane, "<html><div style='text-align: center;'>U<br>s<br>a<br>g<br>e</div></html>");
    }
    
    private String usage() {
        ResourceBundle bundle;
        bundle = ResourceBundle.getBundle("com.vgi.mafscaling.logstats");
        return bundle.getString("usage"); 
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // WORK FUNCTIONS
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void setAxis() {
        ArrayList<Integer> distancePct = new ArrayList<Integer>();
        distancePct.add(distance);
        new LogStatsFixedAxis(distancePct, xAxisArray, yAxisArray);
        Config.setLastXAxis(xAxisArray.toString().replaceAll("\\[|\\]", ""));
        Config.setLastYAxis(yAxisArray.toString().replaceAll("\\[|\\]", ""));
        if (xAxisArray.size() > 0)
            xAxisRoundTextBox.setValue(null);
        if (yAxisArray.size() > 0)
            yAxisRoundTextBox.setValue(null);
        distance = distancePct.get(0);
    }
    
    private void setFilters() {
        String[] colNames = new String[xAxisColumn.getItemCount()];
        for (int i = 0; i < xAxisColumn.getItemCount(); ++i)
            colNames[i] = xAxisColumn.getItemAt(i);
        new LogStatsFilters(colNames, filterButtonList, filterColumnList, filterComboBoxList, filterTextBoxList);
        if (filterButtonList.size() > 0)
            btnSetFiltersButton.setText(ViewFilters);
        else
            btnSetFiltersButton.setText(SetFilters);
    }
    
    private void clearFilters() {
        filterButtonList.clear();
        filterColumnList.clear();
        filterComboBoxList.clear();
        filterTextBoxList.clear();
        btnSetFiltersButton.setText(SetFilters);
    }

    private void selectLogColumns() {
        fileChooser.setMultiSelectionEnabled(true);
        if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(this))
            return;
        getLogColumns();
    }

    private void getLogColumns() {
        Object xAxis = xAxisColumn.getSelectedItem();
        Object yAxis = yAxisColumn.getSelectedItem();
        List<String> data = dataColumn.getSelectedItems();
        xAxisColumn.removeAllItems();
        yAxisColumn.removeAllItems();
        dataColumn.removeAllItems();
        dataColumn.setText("");
        labelFileName.setText("");
        Object[] filterArr = new Object[filterButtonList.size()];
        for (int k = 0; k < filterButtonList.size(); ++k) {
            filterArr[k] = filterColumnList.get(k).getSelectedItem();
            filterColumnList.get(k).removeAllItems();
            filterColumnList.get(k).addItem("");
        }
        logFiles = fileChooser.getSelectedFiles();
        logFilesTodo = new ArrayList<File>();
        BufferedReader br = null;
        TreeSet<String> columns = new TreeSet<String>();
        
        for (File logFile : logFiles) {
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(logFile.getAbsoluteFile()), Config.getEncoding()));
                String line = null;
                String [] elements = null;
                while ((line = br.readLine()) != null && (elements = line.trim().split(Utils.fileFieldSplitter, -1)) != null && elements.length < 2)
                    continue;
                if (logFilesTodo.size() == 0) {
                    logFilesTodo.add(logFile);
                    for (String item : elements)
                        columns.add(item);
                }
                else {
                    HashSet<String> columnsCurrent = new HashSet<String>();
                    for (String item : elements) {
                        if (!columns.contains(item))
                            JOptionPane.showMessageDialog(null, "File " + logFile.getName() + " has extra column '" + item + "'\nThis columns will be ignored", "Extra column", JOptionPane.WARNING_MESSAGE);
                        else
                            columnsCurrent.add(item);
                    }
                    for (Iterator<String> it = columns.iterator(); it.hasNext();) {
                        String item = it.next();
                        if (!columnsCurrent.contains(item)) {
                            JOptionPane.showMessageDialog(null, "File " + logFile.getName() + " is missing column '" + item + "'\nThis columns will be ignored", "Missing column", JOptionPane.WARNING_MESSAGE);
                            it.remove();
                        }
                    }
                    logFilesTodo.add(logFile);
                }
            }
            catch (Exception e) {
                logger.error(e);
                JOptionPane.showMessageDialog(null, e, "Error opening file", JOptionPane.ERROR_MESSAGE);
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
        }
        for (String item : columns) {
            xAxisColumn.addItem(item);
            yAxisColumn.addItem(item);
            dataColumn.addItem(item);
            for (int k = 0; k < filterButtonList.size(); ++k)
                filterColumnList.get(k).addItem(item);
        }
        if (xAxis != null)
            xAxisColumn.setSelectedItem(xAxis);
        if (yAxis != null)
            yAxisColumn.setSelectedItem(yAxis);
        if (data != null)
            dataColumn.setSelectedItems(data);
        for (int k = filterButtonList.size() - 1; k >= 0 ; --k) {
            if (filterArr[k] != null) {
                filterColumnList.get(k).setSelectedItem(filterArr[k]);
                if (filterColumnList.get(k).getSelectedIndex() == -1 || filterColumnList.get(k).getSelectedItem().equals("")) {
                    filterButtonList.remove(k);
                    filterComboBoxList.remove(k);
                    filterColumnList.remove(k);
                    filterTextBoxList.remove(k);
                }
            }
        }
        if (filterButtonList.size() > 0) {
            filterButtonList.set(0, null);
            btnSetFiltersButton.setText(ViewFilters);
        }
        else
            btnSetFiltersButton.setText(SetFilters);
            
        StringBuffer sb = new StringBuffer("");
        for (File logFile : logFilesTodo)
            sb.append(logFile.getName()).append(", ");
        if (sb.length() >= 2)
            sb.delete(sb.length() - 2, sb.length());
        labelFileName.setText("[" + sb.toString() + "]");
    }
    
    private Statistics getStatId() {
        if (statistics.getSelectedItem() == null)
            return Statistics.MEAN;
        String name = (String)statistics.getSelectedItem();
        if ("Count".equals(name))
            return Statistics.COUNT;
        if ("Minimum".equals(name))
            return Statistics.MINIMUM;
        if ("Maximum".equals(name))
            return Statistics.MAXIMUM;
        if ("Mean".equals(name))
            return Statistics.MEAN;
        if ("Median".equals(name))
            return Statistics.MEDIAN;
        if ("Mode".equals(name))
            return Statistics.MODE;
        if ("Range".equals(name))
            return Statistics.RANGE;
        if ("Variance".equals(name))
            return Statistics.VARIANCE;
        if ("Standard Deviation".equals(name))
            return Statistics.STDDEV;
        return Statistics.MEAN;
    }
    
    private DataFilter getType(JComboBox<String> filter) {
        if (filter.getSelectedItem() == null)
            return DataFilter.NONE;
        String name = (String)filter.getSelectedItem();
        if ("<".equals(name))
            return DataFilter.LESS;
        if ("<=".equals(name))
            return DataFilter.LESS_EQUAL;
        if ("=".equals(name))
            return DataFilter.EQUAL;
        if (">=".equals(name))
            return DataFilter.GREATER_EQUAL;
        if (">".equals(name))
            return DataFilter.GREATER;
        return DataFilter.NONE;
    }
    
    private int getFilterRounding(JFormattedTextField filterTextBox) {
        int rounding = 0;
        String filterString = filterTextBox.getText();
        if (filterString.indexOf('.') != -1) {
            filterString = filterString.substring(filterString.indexOf('.'));
            rounding = filterString.length() - 1;
        }
        return rounding;
    }
    
    private boolean checkAgainstFilter(double value, DataFilter type, double filter, int rounding) {
        boolean ret = true;
        switch (type) {
        case LESS:
            if (value >= filter)
                ret = false;
            break;
        case LESS_EQUAL:
            if (value > filter)
                ret = false;
            break;
        case EQUAL:
            double rndVal = value;
            if (rounding > 0) {
                double multiplier = Math.pow(10.0, rounding);
                rndVal = Math.round(value * multiplier) / multiplier;
            }
            else
                rndVal = Math.round(value);
            if (!Utils.equals(rndVal, filter))
                ret = false;
            break;
        case GREATER_EQUAL:
            if (value < filter)
                ret = false;
            break;
        case GREATER:
            if (value <= filter)
                ret = false;
            break;
        default:
            break;
        }
        return ret;
    }

    private void processLog() {
        // Get and validate required fields
        Utils.clearTable(dataTable);
        int xAxisArraySize = xAxisArray.size();
        int yAxisArraySize = yAxisArray.size();
        if (xAxisColumn.getSelectedItem() == null || yAxisColumn.getSelectedItem() == null || dataColumn.getSelectedItems() == null) {
            JOptionPane.showMessageDialog(null, "Please select X-Axis, Y-Axis, and Data columns.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (xAxisRoundTextBox.getValue() == null && xAxisArraySize == 0) {
            JOptionPane.showMessageDialog(null, "X-Axis scaling is not set. Please set 'Step' or X-Axis values.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (yAxisRoundTextBox.getValue() == null && yAxisArraySize == 0) {
            JOptionPane.showMessageDialog(null, "Y-Axis scaling is not set. Please set 'Step' or Y-Axis values.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double xRound = Double.NaN;
        if (xAxisRoundTextBox.getValue() != null) {
            xRound = (((Number)xAxisRoundTextBox.getValue()).doubleValue());
            if (xRound < 0.01) {
                JOptionPane.showMessageDialog(null, "Incorrect X-Axis scaling, minimum allowed is 0.01", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        double yRound = Double.NaN;
        if (yAxisRoundTextBox.getValue() != null) {
            yRound = (((Number)yAxisRoundTextBox.getValue()).doubleValue());
            if (yRound < 0.01) {
                JOptionPane.showMessageDialog(null, "Incorrect Y-Axis scaling, minimum allowed is 0.01", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Statistics statid = getStatId();
        String xAxisColName = (String)xAxisColumn.getSelectedItem();
        String yAxisColName = (String)yAxisColumn.getSelectedItem();
        List<String> dataColNames = dataColumn.getSelectedItems();
        
        // Get filters
        DataFilter[] filterTypeArr = new DataFilter[filterButtonList.size()];
        String[] filterColNameArr = new String[filterButtonList.size()];
        double[] filterArr = new double[filterButtonList.size()];
        int[] filterRoundingArr = new int[filterButtonList.size()];
        boolean[] useFilterArr = new boolean[filterButtonList.size()];
        boolean[] isFilterAndArr = new boolean[filterButtonList.size()];
        
        for (int k = 0; k < filterButtonList.size(); ++k) {
            filterTypeArr[k] = getType(filterComboBoxList.get(k));
            if (filterColumnList.get(k).getSelectedItem() != null)
                filterColNameArr[k] = (String)filterColumnList.get(k).getSelectedItem();
            filterArr[k] = (filterTextBoxList.get(k).getValue() != null ? (((Number)filterTextBoxList.get(k).getValue()).doubleValue()) : Double.NaN);
            filterRoundingArr[k] = getFilterRounding(filterTextBoxList.get(k));
            if (!filterColNameArr[k].isEmpty() && filterTypeArr[k] != DataFilter.NONE && !Double.isNaN(filterArr[k])) {
                useFilterArr[k] = true;
                isFilterAndArr[k] = (k > 0 && "or".equals(filterButtonList.get(k).getText()) ? false : true);
            }
        }
        
        TreeSet<Double> xVals = new TreeSet<Double>();
        TreeSet<Double> yVals = new TreeSet<Double>();
        // data struct where first hash set is X-Axis containing second hash set which is Y-Axis containing array of values
        xData = new HashMap<Double, HashMap<Double, ArrayList<Double>>>();

        // Process log files
        for (File logFile : logFilesTodo) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(logFile.getAbsoluteFile()), Config.getEncoding()));
                String line = null;
                String [] elements = null;
                while ((line = br.readLine()) != null && (elements = line.trim().split(Utils.fileFieldSplitter, -1)) != null && elements.length < 2)
                    continue;
                ArrayList<String> columns = new ArrayList<String>(Arrays.asList(elements));
                int xColIdx = columns.indexOf(xAxisColName);
                int yColIdx = columns.indexOf(yAxisColName);
                ArrayList<Integer> vColIdxArray = new ArrayList<Integer>();
                for (String dataColName : dataColNames)
                    vColIdxArray.add(columns.indexOf(dataColName));
                int[] fltrColIdxArr = new int[filterButtonList.size()];
                double fltrVal;
                for (int k = 0; k < filterButtonList.size(); ++k)
                    fltrColIdxArr[k] = (useFilterArr[k] ? columns.indexOf(filterColNameArr[k]) : -1);
                double x, y, val, dst, totdst;
                double distratio = distance / 100.0;
                int idx = 0;
                int lastXIdx = xAxisArraySize - 1;
                int lastYIdx = yAxisArraySize - 1;
                int i = 2;
                boolean skip;
                int tmColIdx = -1;
                HashMap<Double, ArrayList<Double>> yData;
                ArrayList<Double> data;
                try {
                    line = br.readLine();
                    while (line != null) {
                        elements = line.trim().split(Utils.fileFieldSplitter, -1);
                        if (i == 2) {
                            boolean found = false;
                            for (int k = 0; !found && k < filterButtonList.size(); ++k) {
                                if (useFilterArr[k] && elements[fltrColIdxArr[k]].matches(Utils.tmRegex))
                                    tmColIdx = fltrColIdxArr[k];
                                    found = true;
                            }
                            if (!found) {
                                if (elements[xColIdx].matches(Utils.tmRegex))
                                    tmColIdx = xColIdx;
                                else if (elements[yColIdx].matches(Utils.tmRegex))
                                    tmColIdx = yColIdx;
                            }
                            if (tmColIdx >= 0)
                                Utils.resetBaseTime(elements[tmColIdx]);
                        }
                        if (tmColIdx >= 0)
                            elements[tmColIdx] = String.valueOf(Utils.parseTime(elements[tmColIdx]));
                        // Check if value passes all filters
                        boolean passFilters = true;
                        for (int k = 0; k < filterButtonList.size() && useFilterArr[k]; ++k) {
                            if (!Pattern.matches(Utils.fpRegex, elements[fltrColIdxArr[k]])) {
                                JOptionPane.showMessageDialog(null, "Invalid value for Filter " + (k + 1) + ", file " + logFile.getName() + ", column " + (fltrColIdxArr[k] + 1) + ", row " + (i + 1), "Invalid value", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            fltrVal = Utils.parseValue(elements[fltrColIdxArr[k]]);
                            if (isFilterAndArr[k])
                                passFilters = (passFilters && checkAgainstFilter(fltrVal, filterTypeArr[k], filterArr[k], filterRoundingArr[k]));
                            else
                                passFilters = (passFilters || checkAgainstFilter(fltrVal, filterTypeArr[k], filterArr[k], filterRoundingArr[k]));
                        }
                        // Value should be processed
                        if (passFilters) {
                            if (!Pattern.matches(Utils.fpRegex, elements[xColIdx]) && !Pattern.matches(Utils.onOffRegex, elements[xColIdx])) {
                                JOptionPane.showMessageDialog(null, "Invalid value for X-Axis, file " + logFile.getName() + ", column " + (xColIdx + 1) + ", row " + (i + 1), "Invalid value", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            if (!Pattern.matches(Utils.fpRegex, elements[yColIdx]) && !Pattern.matches(Utils.onOffRegex, elements[yColIdx])) {
                                JOptionPane.showMessageDialog(null, "Invalid value for Y-Axis, file " + logFile.getName() + ", column " + (yColIdx + 1) + ", row " + (i + 1), "Invalid value", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            skip = false;
                            if (Double.isNaN(xRound)) {
                                val = Utils.parseValue(elements[xColIdx]);
                                idx = Utils.closestValueIndex(val, xAxisArray);
                                x = xAxisArray.get(idx);
                                if (distance < 50 && xAxisArraySize > 1) {
                                    if (val < x) {
                                        dst = x - val;
                                        if (idx == 0)
                                            totdst = xAxisArray.get(idx + 1) - x;
                                        else
                                            totdst = x - xAxisArray.get(idx - 1);
                                    }
                                    else {
                                        dst = val - x;
                                        if (idx == lastXIdx)
                                            totdst = x - xAxisArray.get(idx - 1);
                                        else
                                            totdst = xAxisArray.get(idx + 1) - x;
                                    }
                                    if (distratio < dst / totdst)
                                        skip = true;
                                }
                            }
                            else {
                                x = Utils.round(Utils.parseValue(elements[xColIdx]), xRound);
                                xVals.add(x);
                            }
                            if (Double.isNaN(yRound)) {
                                val = Utils.parseValue(elements[yColIdx]);
                                idx = Utils.closestValueIndex(val, yAxisArray);
                                y = yAxisArray.get(idx);
                                if (distance < 50 && yAxisArraySize > 1) {
                                    if (val < y) {
                                        dst = y - val;
                                        if (idx == 0)
                                            totdst = yAxisArray.get(idx + 1) - y;
                                        else
                                            totdst = y - yAxisArray.get(idx - 1);
                                    }
                                    else {
                                        dst = val - y;
                                        if (idx == lastYIdx)
                                            totdst = y - yAxisArray.get(idx - 1);
                                        else
                                            totdst = yAxisArray.get(idx + 1) - y;
                                    }
                                    if (distratio < dst / totdst)
                                        skip = true;
                                }
                            }
                            else {
                                y = Utils.round(Utils.parseValue(elements[yColIdx]), yRound);
                                yVals.add(y);
                            }
                            val = 0;
                            for (Integer vColIdx : vColIdxArray) {
                                if (!Pattern.matches(Utils.fpRegex, elements[vColIdx]) && !Pattern.matches(Utils.onOffRegex, elements[vColIdx])) {
                                    JOptionPane.showMessageDialog(null, "Invalid value for Data, file " + logFile.getName() + ", column " + (vColIdx + 1) + ", row " + (i + 1), "Invalid value", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                val += Utils.parseValue(elements[vColIdx]);
                            }
                            yData = xData.get(x);
                            if (yData == null) {
                                yData = new HashMap<Double, ArrayList<Double>>();
                                xData.put(x, yData);
                            }
                            data = yData.get(y);
                            if (data == null) {
                                data = new ArrayList<Double>();
                                yData.put(y, data);
                            }
                            if (!skip)
                                data.add(val);
                        }
                        line = br.readLine();
                        i += 1;
                    }
                }
                catch (Exception e) {
                    logger.error(e);
                    JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            catch (Exception e) {
                logger.error(e);
                JOptionPane.showMessageDialog(null, e, "Error opening file", JOptionPane.ERROR_MESSAGE);
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
        }
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        if (xVals.size() > 0) {
            xAxisArray.clear();
            xAxisArray.addAll(xVals);
        }
        if (yVals.size() > 0) {
            yAxisArray.clear();
            yAxisArray.addAll(yVals);
        }
        processData(statid);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    
    public void processData(Statistics id) {
        try {
            Utils.ensureColumnCount(xAxisArray.size() + 1, dataTable);
            Utils.ensureRowCount(yAxisArray.size() + 1, dataTable);
            int x = 0;
            int y = 0;
            double val = 0;
            HashMap<Double, ArrayList<Double>> xentry;
            ArrayList<Double> yentry;
            int hitCount = hitCountTextBox.getValue() != null ? (((Number)hitCountTextBox.getValue()).intValue()) : 0; 
            for (double xval : xAxisArray) {
                dataTable.setValueAt(xval, 0, ++x);
                xentry = xData.get(xval);
                if (xentry == null)
                    continue;
                y = 0;
                for (double yval : yAxisArray) {
                    dataTable.setValueAt(yval, ++y, 0);
                    yentry = xentry.get(yval);
                    if (yentry == null || yentry.size() <= hitCount)
                        continue;
                    switch (id) {
                    case COUNT:
                        val = yentry.size();
                        break;
                    case MINIMUM:
                        val = Collections.min(yentry);
                        break;
                    case MAXIMUM:
                        val = Collections.max(yentry);
                        break;
                    case MEAN:
                        val = Utils.mean(yentry);
                        break;
                    case MEDIAN:
                        val = Utils.median(yentry);
                        break;
                    case MODE:
                        val = Utils.mode(yentry);
                        break;
                    case RANGE:
                        val = Utils.range(yentry);
                        break;
                    case VARIANCE:
                        val = Utils.variance(yentry);
                        break;
                    case STDDEV:
                        val = Utils.standardDeviation(yentry);
                        break;
                    }
                    dataTable.setValueAt(Double.isNaN(val) ? "" : val, y, x);
                    
                }
            }
            if (xData.size() > 0) {
                int i;
                // remove extra rows
                for (i = dataTable.getRowCount() - 1; i >= 0 && dataTable.getValueAt(i, 0).toString().equals(""); --i)
                    Utils.removeRow(i, dataTable);
                // remove extra columns
                for (i = dataTable.getColumnCount() - 1; i >= 0 && dataTable.getValueAt(0, i).toString().equals(""); --i)
                    Utils.removeColumn(i, dataTable);
                Utils.colorTable(dataTable);
            }
            else {
                Utils.clearTable(dataTable);
            }
            JRadioButton button = (JRadioButton) rbGroup.getElements().nextElement();
            button.setSelected(true);
            display3D(Plot3D.GRID);
        }
        catch (Exception e) {
            logger.error(e);
            JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addGridPlot() {
        if (xData == null || xData.size() == 0)
            return;
        TreeMap<Double, Integer> xAxisMap = new TreeMap<Double, Integer>();
        TreeMap<Double, Integer> yAxisMap = new TreeMap<Double, Integer>();
        for (Map.Entry<Double, HashMap<Double, ArrayList<Double>>> xentry : xData.entrySet()) {
            xAxisMap.put(xentry.getKey(), 0);
            for (Map.Entry<Double, ArrayList<Double>> yentry : xentry.getValue().entrySet())
                yAxisMap.put(yentry.getKey(), 0);
        }
        double[] x = new double[xAxisMap.size()];
        int i = 0;
        for (Double key : xAxisMap.keySet())
            x[i++] = key;
        double[] y = new double[yAxisMap.size()];
        i = 0;
        for (Double key : yAxisMap.keySet())
            y[i++] = key;
        double[][] z = Utils.doubleZArray(dataTable, x, y);
        Color[][] colors = Utils.doubleColorArray(dataTable, x, y);
        plot.addGridPlot(dataColumn.getSelectedItemsString() + " " + statistics.getSelectedItem().toString(), colors, x, y, z);
    }
    
    private void addHistogramPlot(Color[] colors, double[][] xyzArray) {
        double minXDiff = Double.NaN;
        double minYDiff = Double.NaN;
        double diff;
        for (int i = 1; i < dataTable.getColumnCount() - 1; ++i) {
            diff = Double.valueOf(dataTable.getValueAt(0, i + 1).toString()) - Double.valueOf(dataTable.getValueAt(0, i).toString());
            if (diff < minXDiff || Double.isNaN(minXDiff))
                minXDiff = diff;
        }
        for (int i = 1; i < dataTable.getRowCount() - 1; ++i) {
            diff = Double.valueOf(dataTable.getValueAt(i + 1, 0).toString()) - Double.valueOf(dataTable.getValueAt(i, 0).toString());
            if (diff < minYDiff || Double.isNaN(minYDiff))
                minYDiff = diff;
        }
        plot.addPlot(new HistogramPlot3D(dataColumn.getSelectedItemsString() + " " + statistics.getSelectedItem().toString(), colors, xyzArray, minXDiff, minYDiff));
    }

    private void addBarLineScatterPlot(Plot3D type) {
        if (xData == null || xData.size() == 0)
            return;
        int k = 0;
        String val;
        double X, Y;
        double[][] array = new double[dataTable.getColumnCount() * dataTable.getRowCount()][3];

        BgColorFormatRenderer renderer = (BgColorFormatRenderer)dataTable.getDefaultRenderer(Object.class);
        ArrayList<Color> colors = new ArrayList<Color>();
        for (int i = 1; i < dataTable.getColumnCount(); ++i) {
            val = dataTable.getValueAt(0, i).toString();
            if (val.isEmpty())
                break;
            X = Double.valueOf(val.toString());
            for (int j = 1; j < dataTable.getRowCount(); ++j) {
                val = dataTable.getValueAt(j, 0).toString();
                if (val.isEmpty())
                    break;
                Y = Double.valueOf(val.toString());
                val = dataTable.getValueAt(j, i).toString();
                if (!val.isEmpty()) {
                    colors.add(renderer.getColorAt(j, i));
                    array[k][0] = X;
                    array[k][1] = Y;
                    array[k++][2] = Double.valueOf(val);
                }
            }
        }
        double[][] xyzArray = new double[k][3];
        for (k = 0; k < xyzArray.length; ++k)
            System.arraycopy(array[k], 0, xyzArray[k], 0, 3);
        switch (type) {
        case BAR:
            plot.addBarPlot(dataColumn.getSelectedItemsString() + " " + statistics.getSelectedItem().toString(), colors.toArray(new Color[colors.size()]), xyzArray);
            break;
        case HIST:
            addHistogramPlot(colors.toArray(new Color[colors.size()]), xyzArray);
            break;
        case LINE:
            plot.addLinePlot(dataColumn.getSelectedItemsString() + " " + statistics.getSelectedItem().toString(), colors.toArray(new Color[colors.size()]), xyzArray);
            break;
        case SCATTER:
            plot.addScatterPlot(dataColumn.getSelectedItemsString() + " " + statistics.getSelectedItem().toString(), colors.toArray(new Color[colors.size()]), xyzArray);
        default:
            break;
        }
    }

    private void display3D(Plot3D type) {
        plot.removeAllPlots();
        switch (type) {
        case GRID:
            addGridPlot();
            break;
        case HIST:
            addBarLineScatterPlot(type);
            break;
        case BAR:
            addBarLineScatterPlot(type);
            break;
        case LINE:
            addBarLineScatterPlot(type);
            break;
        case SCATTER:
            addBarLineScatterPlot(type);
            break;
        default:
            return;
        }
        plot.setAxisLabel(0, xAxisColumn.getSelectedItem().toString());
        plot.setAxisLabel(1, yAxisColumn.getSelectedItem().toString());
        plot.setAxisLabel(2, dataColumn.getSelectedItemsString());
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if ("selectlog".equals(e.getActionCommand()))
            selectLogColumns();
        else if ("go".equals(e.getActionCommand()))
            processLog();
        else if ("setaxis".equals(e.getActionCommand()))
            setAxis();
        else if ("setfilters".equals(e.getActionCommand()))
            setFilters();
        else if ("clearfilters".equals(e.getActionCommand()))
            clearFilters();
        else if ("grid".equals(e.getActionCommand())) {
            if (xData != null)
                display3D(Plot3D.GRID);
        }
        else if ("hist".equals(e.getActionCommand())) {
            if (xData != null)
                display3D(Plot3D.HIST);
        }
        else if ("bar".equals(e.getActionCommand())) {
            if (xData != null)
                display3D(Plot3D.BAR);
        }
        else if ("line".equals(e.getActionCommand())) {
            if (xData != null)
                display3D(Plot3D.LINE);
        }
        else if ("scatter".equals(e.getActionCommand())) {
            if (xData != null)
                display3D(Plot3D.SCATTER);
        }
    }
    
    protected void onDroppedFiles(List<File> files) {
        if (files.size() > 0 && getSelectedIndex() == 0) {
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setCurrentDirectory(files.get(0));
            fileChooser.setSelectedFiles((File[])files.toArray());
            fileChooser.approveSelection();
            getLogColumns();
        }
    }
}

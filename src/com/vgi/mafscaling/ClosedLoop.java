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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import org.apache.log4j.Logger;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.function.Function2D;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYSeries;

public class ClosedLoop extends AMafScaling {
    private static final long serialVersionUID = 2988105467764335997L;
    private static final Logger logger = Logger.getLogger(ClosedLoop.class);

    private static final ArrayList<String> logDataTableHeaders = new ArrayList<String>(Arrays.asList("Time", "Load", "RPM", "MafV", "AFR", "STFT", "LTFT", "dV/dt", "IAT", "MAP"));
    private static final String SaveDataFileHeader = "[closed_loop run data]";
    private static final String Afr1TableName = "AFR Average";
    private static final String Afr2TableName = "AFR Cell Hit Count";
    private static final String Y2AxisName = "Total Correction (%)";
    private static final String dvdtAxisName = "dV / dt";
    private static final String iatAxisName = "IAT";
    private static final String trpmAxisName = "Trims / Rpm";
    private static final String mnmdAxisName = "Mean / Mode";
    private static final String mnmd2AxisName = "Trims / MafV";
    private static final String timeAxisName = "Time";
    private static final String rpmAxisName = "RPM";
    private static final String totalCorrectionDataName = "Total Correction";
    private static final int ColumnCount = logDataTableHeaders.size();
    private static final int AfrTableColumnCount = 15;
    private static final int AfrTableRowCount = 25;
    private static final int LogDataRowCount = 200;
    private int clValue = Config.getClOlStatusValue();
    private int minCellHitCount = Config.getCLMinCellHitCount();
    private double afrMin = Config.getAfrMinimumValue();
    private double afrMax = Config.getAfrMaximumValue();
    private double minLoad = Config.getLoadMinimumValue();
    private double maxDvDt = Config.getDvDtMaximumValue();
    private double maxMafV = Config.getMafVMaximumValue();
    private double maxIat = Config.getIatMaximumValue();

    private int logClOlStatusColIdx = -1;
    private int logAfLearningColIdx = -1;
    private int logAfCorrectionColIdx = -1;
    private int logAfrColIdx = -1;
    private int logRpmColIdx = -1;
    private int logLoadColIdx = -1;
    private int logTimeColIdx = -1;
    private int logMafvColIdx = -1;
    private int logIatColIdx = -1;
    private int logMapColIdx = -1;

    private JTable logDataTable = null;
    private JTable afr1Table = null;
    private JTable afr2Table = null;
    private JCheckBox checkBoxDvdtData = null;
    private JCheckBox checkBoxIatData = null;
    private JCheckBox checkBoxTrpmData = null;
    private JCheckBox checkBoxMnmdData = null;
    private ArrayList<Double> trimArray = new ArrayList<Double>();
    private ArrayList<Double> timeArray = new ArrayList<Double>();
    private ArrayList<Double> iatArray = new ArrayList<Double>();
    private ArrayList<Double> dvdtArray = new ArrayList<Double>();
    private ArrayList<Double> correctionMeanArray = new ArrayList<Double>();
    private ArrayList<Double> correctionModeArray = new ArrayList<Double>();

    public ClosedLoop(int tabPlacement, PrimaryOpenLoopFuelingTable table, MafCompare comparer) {
        super(tabPlacement, table, comparer);
        runData = new XYSeries(totalCorrectionDataName);
        initialize();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // DATA TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    protected void createRunPanel(JPanel dataPanel) {
        JPanel runPanel = new JPanel();
        GridBagConstraints gbc_runPanel = new GridBagConstraints();
        gbc_runPanel.fill = GridBagConstraints.BOTH;
        gbc_runPanel.weightx = 1.0;
        gbc_runPanel.weighty = 1.0;
        gbc_runPanel.gridx = 0;
        gbc_runPanel.gridy = 3;
        dataPanel.add(runPanel, gbc_runPanel);

        GridBagLayout gbl_runPanel = new GridBagLayout();
        gbl_runPanel.columnWidths = new int[]{0, 0};
        gbl_runPanel.rowHeights = new int[] {0};
        gbl_runPanel.columnWeights = new double[]{0.0, 0.0};
        gbl_runPanel.rowWeights = new double[]{0.0};
        runPanel.setLayout(gbl_runPanel);
        
        JScrollPane dataScrollPane = new JScrollPane();
        createLogDataTable(dataScrollPane);
        GridBagConstraints gbc_dataScrollPane = new GridBagConstraints();
        gbc_dataScrollPane.fill = GridBagConstraints.BOTH;
        gbc_dataScrollPane.ipadx = ColumnWidth * (ColumnCount - 1 /* don't calculate MAP width in */ );
        gbc_dataScrollPane.weighty = 1.0;
        gbc_dataScrollPane.gridx = 0;
        gbc_dataScrollPane.gridy = 0;
        runPanel.add(dataScrollPane, gbc_dataScrollPane);

        JScrollPane aprScrollPane = new JScrollPane();
        createAfrDataTables(aprScrollPane);
        GridBagConstraints gbc_aprScrollPane = new GridBagConstraints();
        gbc_aprScrollPane.weightx = 1.0;
        gbc_aprScrollPane.weighty = 1.0;
        gbc_aprScrollPane.anchor = GridBagConstraints.PAGE_START;
        gbc_aprScrollPane.fill = GridBagConstraints.BOTH;
        gbc_aprScrollPane.gridx = 1;
        gbc_aprScrollPane.gridy = 0;
        runPanel.add(aprScrollPane, gbc_aprScrollPane);
    }

    private void createLogDataTable(JScrollPane dataScrollPane) {
        JPanel dataRunPanel = new JPanel();
        dataScrollPane.setViewportView(dataRunPanel);
        GridBagLayout gbl_dataRunPanel = new GridBagLayout();
        gbl_dataRunPanel.columnWidths = new int[]{0};
        gbl_dataRunPanel.rowHeights = new int[] {0};
        gbl_dataRunPanel.columnWeights = new double[]{0.0};
        gbl_dataRunPanel.rowWeights = new double[]{0.0};
        dataRunPanel.setLayout(gbl_dataRunPanel);

        logDataTable = new JTable();
        logDataTable.getTableHeader().setReorderingAllowed(false);
        logDataTable.setModel(new DefaultTableModel(LogDataRowCount, ColumnCount));
        logDataTable.setColumnSelectionAllowed(true);
        logDataTable.setCellSelectionEnabled(true);
        logDataTable.setBorder(new LineBorder(new Color(0, 0, 0)));
        logDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        logDataTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        for (int i = 0; i < logDataTableHeaders.size(); i++) { logDataTable.getColumnModel().getColumn(i).setHeaderValue(logDataTableHeaders.get(i)); }
        // hide MAP column from displaying
        logDataTable.getColumnModel().getColumn(logDataTableHeaders.indexOf("MAP")).setMinWidth(0);
        logDataTable.getColumnModel().getColumn(logDataTableHeaders.indexOf("MAP")).setMaxWidth(0);
        
        Utils.initializeTable(logDataTable, ColumnWidth);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.gridx = 0;
        gbc.gridy = 0;

        dataRunPanel.add(logDataTable.getTableHeader(), gbc);
        gbc.gridy = 1;
        dataRunPanel.add(logDataTable, gbc);

        excelAdapter.addTable(logDataTable, true, false);
    }

    private void createAfrDataTables(JScrollPane aprScrollPane) {
        JPanel aprRunPanel = new JPanel();
        aprScrollPane.setViewportView(aprRunPanel);
        GridBagLayout gbl_aprRunPanel = new GridBagLayout();
        gbl_aprRunPanel.columnWidths = new int[]{0, 0};
        gbl_aprRunPanel.rowHeights = new int[] {0, 0, 0, 0, 0};
        gbl_aprRunPanel.columnWeights = new double[]{0.0, 1.0};
        gbl_aprRunPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0};
        aprRunPanel.setLayout(gbl_aprRunPanel);

        afr1Table = createAfrDataTable(aprRunPanel, Afr1TableName, 0);
        afr2Table = createAfrDataTable(aprRunPanel, Afr2TableName, 2);
    }

    private JTable createAfrDataTable(JPanel panel, String tableName, int gridy) {
        final JTable afrTable = new JTable() {
            private static final long serialVersionUID = 6526901361175099297L;
            public boolean isCellEditable(int row, int column) { return false; };
        };
        DefaultTableColumnModel afrModel = new DefaultTableColumnModel();
        final TableColumn afrColumn = new TableColumn(0, 250);
        afrColumn.setHeaderValue(tableName);
        afrModel.addColumn(afrColumn);
        JTableHeader lblAfrTableName = afrTable.getTableHeader();
        lblAfrTableName.setColumnModel(afrModel);
        lblAfrTableName.setReorderingAllowed(false);
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) lblAfrTableName.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);

        GridBagConstraints gbc_lblAfrTableName = new GridBagConstraints();
        gbc_lblAfrTableName.insets = new Insets((gridy == 0 ? 0 : 5),0,0,0);
        gbc_lblAfrTableName.anchor = GridBagConstraints.PAGE_START;
        gbc_lblAfrTableName.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblAfrTableName.gridx = 0;
        gbc_lblAfrTableName.gridy = gridy;
        panel.add(lblAfrTableName, gbc_lblAfrTableName);

        afrTable.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                afrColumn.setWidth(afrTable.getWidth());
            }
        });
        afrTable.getTableHeader().setReorderingAllowed(false);
        afrTable.setColumnSelectionAllowed(true);
        afrTable.setCellSelectionEnabled(true);
        afrTable.setBorder(new LineBorder(new Color(0, 0, 0)));
        afrTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        afrTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        afrTable.setModel(new DefaultTableModel(AfrTableRowCount, AfrTableColumnCount));
        Utils.initializeTable(afrTable, ColumnWidth);

        if (tableName.equals(Afr1TableName)) {
	        Format[][] formatMatrix = { { new DecimalFormat("#"), new DecimalFormat("0.00") } };
	        NumberFormatRenderer renderer = (NumberFormatRenderer)afrTable.getDefaultRenderer(Object.class);
	        renderer.setFormats(formatMatrix);
        }
        else if (tableName.equals(Afr2TableName)) {
	        Format[][] formatMatrix = { { new DecimalFormat("#"), new DecimalFormat("0.00") }, { new DecimalFormat("#"), new DecimalFormat("#") } };
	        NumberFormatRenderer renderer = (NumberFormatRenderer)afrTable.getDefaultRenderer(Object.class);
	        renderer.setFormats(formatMatrix);
        }
        
        GridBagConstraints gbc_afrTable = new GridBagConstraints();
        gbc_afrTable.insets = new Insets(0, 0, 0, 0);
        gbc_afrTable.anchor = GridBagConstraints.PAGE_START;
        gbc_afrTable.gridx = 0;
        gbc_afrTable.gridy = gridy + 1;
        panel.add(afrTable, gbc_afrTable);
        
        excelAdapter.addTable(afrTable, true, false);

        return afrTable;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE CHART TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    protected void createGraghTab() {
        JPanel cntlPanel = new JPanel();
        JPanel plotPanel = createGraphPlotPanel(cntlPanel);
        add(plotPanel, "<html><div style='text-align: center;'>C<br>h<br>a<br>r<br>t</div></html>");
        
        GridBagLayout gbl_cntlPanel = new GridBagLayout();
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0, 0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);
        
        GridBagConstraints gbc_check = new GridBagConstraints();
        gbc_check.insets = insets2;
        gbc_check.gridx = 0;
        gbc_check.gridy = 0;
        
        checkBoxDvdtData = new JCheckBox("dV/dt");
        checkBoxDvdtData.setActionCommand("dvdt");
        checkBoxDvdtData.addActionListener(this);
        cntlPanel.add(checkBoxDvdtData, gbc_check);
        
        gbc_check.gridx++;
        checkBoxIatData = new JCheckBox("IAT");
        checkBoxIatData.setActionCommand("iat");
        checkBoxIatData.addActionListener(this);
        cntlPanel.add(checkBoxIatData, gbc_check);

        gbc_check.gridx++;
        checkBoxTrpmData = new JCheckBox("Trims/RPM");
        checkBoxTrpmData.setActionCommand("trpm");
        checkBoxTrpmData.addActionListener(this);
        cntlPanel.add(checkBoxTrpmData, gbc_check);

        gbc_check.gridx++;
        checkBoxMnmdData = new JCheckBox("Mean/Mode");
        checkBoxMnmdData.setActionCommand("mnmd");
        checkBoxMnmdData.addActionListener(this);
        cntlPanel.add(checkBoxMnmdData, gbc_check);

        gbc_check.gridx++;
        checkBoxRunData = new JCheckBox("Total Correction");
        checkBoxRunData.setActionCommand("corrdata");
        checkBoxRunData.addActionListener(this);
        cntlPanel.add(checkBoxRunData, gbc_check);

        gbc_check.gridx++;
        createGraphCommonControls(cntlPanel, gbc_check.gridx);
        
        createChart(plotPanel, Y2AxisName);
        createMafSmoothingPanel(plotPanel);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // COMMON ACTIONS RELATED FUNCTIONS
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("loadlog".equals(e.getActionCommand()) && !polfTable.validate()) { return; }
        if (checkActionPerformed(e)) { return; }
    	if ("dvdt".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected()) {
            	clearNotRunDataCheckboxes();
            	clearRunDataCheckboxes();
                if (plotDvdtData())
                    checkBox.setSelected(true);
            }
            else
                runData.clear();
            setRanges();
        }
        else if ("iat".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected()) {
            	clearNotRunDataCheckboxes();
            	clearRunDataCheckboxes();
                if (plotIatData())
                    checkBox.setSelected(true);
            }
            else
                runData.clear();
            setRanges();
        }
        else if ("trpm".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected()) {
            	clearNotRunDataCheckboxes();
            	clearRunDataCheckboxes();
                if (plotTrimRpmData())
                    checkBox.setSelected(true);
            }
            else {
                runData.clear();
                currMafData.clear();
            }
            setRanges();
        }
        else if ("mnmd".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected()) {
            	clearNotRunDataCheckboxes();
            	clearRunDataCheckboxes();
                if (plotMeanModeData())
                    checkBox.setSelected(true);
            }
            else {
                currMafData.clear();
                corrMafData.clear();
                runData.clear();
            }
            setRanges();
        }
        else if ("corrdata".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected()) {
            	clearRunDataCheckboxes();
                if (!plotCorrectionData())
                    checkBox.setSelected(false);
            }
            else
                runData.clear();
            setRanges();
        }
        else if ("current".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected()) {
            	clearRunDataCheckboxes();
                if (!plotCurrentMafData())
                    checkBox.setSelected(false);
            }
            else
                currMafData.clear();
            setRanges();
        }
        else if ("corrected".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected()) {
            	clearRunDataCheckboxes();
                if (!setCorrectedMafData())
                    checkBox.setSelected(false);
            }
            else
                corrMafData.clear();
            setRanges();
        }
        else if ("smoothed".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected()) {
            	clearRunDataCheckboxes();
                if (!setSmoothedMafData())
                    checkBox.setSelected(false);
            }
            else
                smoothMafData.clear();
            setRanges();
        }
        else if ("smoothing".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected())
                enableSmoothingView(true);
            else
                enableSmoothingView(false);
            setRanges();
        }
    }
    
    private void clearRunDataCheckboxes() {
    	if (checkBoxDvdtData.isSelected()) {
    		checkBoxDvdtData.setSelected(false);
    		runData.clear();
    	}
    	if (checkBoxIatData.isSelected()) {
    		checkBoxIatData.setSelected(false);
    		runData.clear();
    	}
    	if (checkBoxTrpmData.isSelected()) {
    		checkBoxTrpmData.setSelected(false);
    		currMafData.clear();
    		runData.clear();
    	}
    	if (checkBoxMnmdData.isSelected()) {
    		checkBoxMnmdData.setSelected(false);
    		currMafData.clear();
    		corrMafData.clear();
    		runData.clear();
    	}
    }
    
    protected void clearRunTables() {
    	clearLogDataTables();
    	clearAfrDataTables();
    }
    
    private void clearLogDataTables() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
    	try {
	        while (LogDataRowCount < logDataTable.getRowCount())
	            Utils.removeRow(LogDataRowCount, logDataTable);
	        Utils.clearTable(logDataTable);
    	}
    	finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    	}
    }
    
    private void clearAfrDataTables() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
    	try {
	        clearAfrDataTable(afr1Table);
	        clearAfrDataTable(afr2Table);
    	}
    	finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    	}
    }
    
    private void clearAfrDataTable(JTable table) {
        while (AfrTableRowCount < table.getRowCount())
            Utils.removeRow(AfrTableRowCount, table);
        while (AfrTableColumnCount < table.getColumnCount())
            Utils.removeColumn(AfrTableColumnCount, table);
        Utils.clearTable(table);
    }

    protected void clearData() {
    	super.clearData();
    	trimArray.clear();
    	timeArray.clear();
    	iatArray.clear();
    	dvdtArray.clear();
        Utils.clearTable(afr1Table);
        Utils.clearTable(afr2Table);
    }

    protected void clearChartCheckBoxes() {
    	super.clearChartCheckBoxes();
    	checkBoxDvdtData.setSelected(false);
    	checkBoxIatData.setSelected(false);
    }
    
    protected void calculateMafScaling() {
        if (!polfTable.validate())
            return;
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            clearData();
            clearChartData();
            clearChartCheckBoxes();

            if (!getMafTableData(voltArray, gsArray))
                return;
            calculateCorrectedGS();
            setCorrectedMafData();
            
            smoothGsArray.addAll(gsCorrected);
            checkBoxCorrectedMaf.setSelected(true);
            
            setXYTable(mafSmoothingTable, voltArray, smoothGsArray);
            
            setRanges();
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            JOptionPane.showMessageDialog(null, "Error: " + e, "Error", JOptionPane.ERROR_MESSAGE);
        }
        finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private void calculateCorrectedGS() {
        double corr;
        double val1;
        double val2;
        int closestMafIdx;
        int closestRmpIdx;
        int closestColIdx;
        int i;
        String tableName = "Log Data";
        ArrayList<Integer> temp = new ArrayList<Integer>(gsArray.size());
        correctionMeanArray = new ArrayList<Double>(gsArray.size());
        correctionModeArray = new ArrayList<Double>(gsArray.size());

        ArrayList<HashMap<Double, Integer>> modeCalcArray = new ArrayList<HashMap<Double, Integer>>();
        for (i = 0; i < gsArray.size(); ++i) {
            temp.add(0);
            correctionMeanArray.add(0.0);
            correctionModeArray.add(0.0);
            modeCalcArray.add(new HashMap<Double, Integer>());
        }
        ArrayList<Double> afrRpmArray = new ArrayList<Double>();
        for (i = 1; i < polfTable.getRowCount(); ++i) {
            afrRpmArray.add(Double.valueOf(polfTable.getValueAt(i, 0).toString()));
            Utils.ensureRowCount(i + 1, afr1Table);
            Utils.ensureRowCount(i + 1, afr2Table);
            afr1Table.setValueAt(polfTable.getValueAt(i, 0), i, 0);
            afr2Table.setValueAt(polfTable.getValueAt(i, 0), i, 0);
        }
        ArrayList<Double> afrColArray = new ArrayList<Double>(); // Load or MAP
        for (i = 1; i < polfTable.getColumnCount(); ++i) {
            afrColArray.add(Double.valueOf(polfTable.getValueAt(0, i).toString()));
            Utils.ensureColumnCount(i + 1, afr1Table);
            Utils.ensureColumnCount(i + 1, afr2Table);
            afr1Table.setValueAt(polfTable.getValueAt(0, i), 0, i);
            afr2Table.setValueAt(polfTable.getValueAt(0, i), 0, i);
        }

        HashMap<String, Double> row = new HashMap<String, Double>();
        Integer val;
        HashMap<Double, Integer> modeCountMap;
        final int mapHeaderIdx = logDataTableHeaders.indexOf("MAP");
        for (i = 0; i < logDataTable.getRowCount(); ++i) {
            // validate cells
            for (int j = 0; j < logDataTable.getColumnCount(); ++j)
            {
                String strVal = logDataTable.getValueAt(i, j).toString();
                if (j != mapHeaderIdx || polfTable.isMap())
                {
                    if (strVal.isEmpty()) { break; }
                    if (!Utils.validateDouble(strVal, i, j, tableName)) { return; }
                }
                row.put(logDataTableHeaders.get(j), Double.valueOf(strVal));
            }

            corr = row.get("LTFT") + row.get("STFT");
            trimArray.add(corr);
            rpmArray.add(row.get("RPM"));
            timeArray.add(row.get("Time"));
            iatArray.add(row.get("IAT"));
            mafvArray.add(row.get("MafV"));
            dvdtArray.add(row.get("dV/dt"));
            
            closestMafIdx = Utils.closestValueIndex(row.get("Load") * row.get("RPM") / 60.0, gsArray);
            correctionMeanArray.set(closestMafIdx, (correctionMeanArray.get(closestMafIdx) * temp.get(closestMafIdx) + corr) / (temp.get(closestMafIdx) + 1));
            temp.set(closestMafIdx, temp.get(closestMafIdx) + 1);
            modeCountMap = modeCalcArray.get(closestMafIdx);
            double roundedCorr = ((double)Math.round(corr * 10.0)) / 10.0;
            val = modeCountMap.get(roundedCorr);
            if (val == null)
            	modeCountMap.put(roundedCorr, 1);
            else
            	modeCountMap.put(roundedCorr, val + 1);
            
            closestRmpIdx = Utils.closestValueIndex(row.get("RPM"), afrRpmArray) + 1;
            closestColIdx = Utils.closestValueIndex(polfTable.isMap() ? row.get("MAP") : row.get("Load"), afrColArray) + 1;
            val1 = (afr1Table.getValueAt(closestRmpIdx, closestColIdx).toString().isEmpty()) ? 0 : Double.valueOf(afr1Table.getValueAt(closestRmpIdx, closestColIdx).toString());
            val2 = (afr2Table.getValueAt(closestRmpIdx, closestColIdx).toString().isEmpty()) ? 0 : Double.valueOf(afr2Table.getValueAt(closestRmpIdx, closestColIdx).toString());
            afr1Table.setValueAt((val1 * val2 + row.get("AFR")) / (val2 + 1.0), closestRmpIdx, closestColIdx);
            afr2Table.setValueAt(val2 + 1.0, closestRmpIdx, closestColIdx);
        }
        
        for (i = 0; i < modeCalcArray.size(); ++i) {
        	modeCountMap = modeCalcArray.get(i);
        	if (modeCountMap.size() > 0) {
	            int maxValueInMap=(Collections.max(modeCountMap.values()));
	            double sum = 0;
	            int count = 0;
	            for (Entry<Double, Integer> entry : modeCountMap.entrySet()) {
	                if (entry.getValue() == maxValueInMap) {
	                	sum += entry.getKey();
	                	count += 1;
	                }
	            }
	            correctionModeArray.set(i, sum / count);
        	}
        }
        	
        int size = afrRpmArray.size() + 1;
        while (size < afr1Table.getRowCount())
            Utils.removeRow(size, afr1Table);
        while (size < afr2Table.getRowCount())
            Utils.removeRow(size, afr2Table);
        Utils.colorTable(afr1Table);
        Utils.colorTable(afr2Table);
        int firstCorrIndex = 0;
        double firstCorr = 1;
        for (i = 0; i < correctionMeanArray.size(); ++i) {
            corr = 1;
            if (temp.get(i) > minCellHitCount) {
                corr = 1.0 + (correctionMeanArray.get(i) + correctionModeArray.get(i)) / 200.00;
                if (firstCorrIndex == 0) {
                	firstCorrIndex = i;
                	firstCorr = corr;
                }
            }
            gsCorrected.add(i, gsArray.get(i) * corr);
        }
        for (i = firstCorrIndex - 1; i > 0; --i)
        	gsCorrected.set(i, gsArray.get(i) * firstCorr);
    }

    private boolean plotDvdtData() {
        return setXYSeries(runData, timeArray, dvdtArray);
    }

    private boolean plotIatData() {
        return setXYSeries(runData, timeArray, iatArray);
    }

    private boolean plotTrimRpmData() {
        if (setXYSeries(runData, rpmArray, trimArray)) {
	        double[] ols = Regression.getOLSRegression(mafChartPanel.getChartPanel().getChart().getXYPlot().getDataset(1), 0);
	        Function2D curve = new LineFunction2D(ols[0], ols[1]);
	        currMafData.clear();
	        currMafData.add(runData.getMinX(), curve.getValue(runData.getMinX()));
	        currMafData.add(runData.getMaxX(), curve.getValue(runData.getMaxX()));
	        return true;
        }
        return false;
    }

    private boolean plotMeanModeData() {
        return (setXYSeries(runData, mafvArray, trimArray) && setXYSeries(currMafData, voltArray, correctionMeanArray) && setXYSeries(corrMafData, voltArray, correctionModeArray));
    }

    private boolean plotCorrectionData() {
        ArrayList<Double> yarr = new ArrayList<Double>();
        for (int i = 0; i < correctionMeanArray.size(); ++i)
            yarr.add((correctionMeanArray.get(i) + correctionModeArray.get(i)) / 2.0);
        return setXYSeries(runData, voltArray, yarr);
    }
    
    private void setRanges() {
    	double paddingX;
    	double paddingY;
        currMafData.setDescription(currentDataName);
        corrMafData.setDescription(correctedDataName);
        smoothMafData.setDescription(smoothedDataName);
        XYPlot plot = mafChartPanel.getChartPanel().getChart().getXYPlot();
    	plot.getDomainAxis(0).setLabel(XAxisName);
    	plot.getRangeAxis(0).setLabel(Y1AxisName);
    	plot.getRangeAxis(1).setLabel(Y2AxisName);
    	plot.getRangeAxis(0).setVisible(true);
        plot.getRenderer(0).setSeriesVisible(0, true);
        plot.getRenderer(0).setSeriesVisible(1, true);
        plot.getRenderer(0).setSeriesVisible(2, true);
        if (checkBoxDvdtData.isSelected() && checkBoxDvdtData.isEnabled()) {
        	paddingX = runData.getMaxX() * 0.05;
        	paddingY = runData.getMaxY() * 0.05;
        	plot.getDomainAxis(0).setRange(runData.getMinX() - paddingX, runData.getMaxX() + paddingX);
	    	plot.getRangeAxis(1).setRange(runData.getMinY() - paddingY, runData.getMaxY() + paddingY);
	    	plot.getRangeAxis(0).setVisible(false);
	    	plot.getRangeAxis(1).setLabel(dvdtAxisName);
	    	plot.getDomainAxis(0).setLabel(timeAxisName);
	        plot.getRenderer(0).setSeriesVisible(0, false);
	        plot.getRenderer(0).setSeriesVisible(1, false);
	        plot.getRenderer(0).setSeriesVisible(2, false);
        }
        else if (checkBoxIatData.isSelected() && checkBoxIatData.isEnabled()) {
        	paddingX = runData.getMaxX() * 0.05;
        	paddingY = runData.getMaxY() * 0.05;
        	plot.getDomainAxis(0).setRange(runData.getMinX() - paddingX, runData.getMaxX() + paddingX);
	    	plot.getRangeAxis(1).setRange(runData.getMinY() - paddingY, runData.getMaxY() + paddingY);
	    	plot.getRangeAxis(0).setVisible(false);
	    	plot.getRangeAxis(1).setLabel(iatAxisName);
	    	plot.getDomainAxis(0).setLabel(timeAxisName);
	        plot.getRenderer(0).setSeriesVisible(0, false);
	        plot.getRenderer(0).setSeriesVisible(1, false);
	        plot.getRenderer(0).setSeriesVisible(2, false);
        }
        else if (checkBoxTrpmData.isSelected() && checkBoxTrpmData.isEnabled()) {
        	paddingX = runData.getMaxX() * 0.05;
        	paddingY = runData.getMaxY() * 0.05;
        	plot.getDomainAxis(0).setRange(runData.getMinX() - paddingX, runData.getMaxX() + paddingX);
	    	plot.getRangeAxis(1).setRange(runData.getMinY() - paddingY, runData.getMaxY() + paddingY);
	    	plot.getRangeAxis(0).setRange(runData.getMinY() - paddingY, runData.getMaxY() + paddingY);
	    	plot.getRangeAxis(0).setVisible(false);
	    	plot.getRangeAxis(1).setLabel(trpmAxisName);
	    	plot.getDomainAxis(0).setLabel(rpmAxisName);
	        plot.getRenderer(0).setSeriesVisible(0, false);
	        plot.getRenderer(0).setSeriesVisible(1, false);
	        currMafData.setDescription("Trend");
        }
        else if (checkBoxMnmdData.isSelected() && checkBoxMnmdData.isEnabled()) {
        	paddingX = runData.getMaxX() * 0.05;
        	paddingY = runData.getMaxY() * 0.05;
        	plot.getDomainAxis(0).setRange(runData.getMinX() - paddingX, runData.getMaxX() + paddingX);
	    	plot.getRangeAxis(1).setRange(runData.getMinY() - paddingY, runData.getMaxY() + paddingY);
	    	plot.getRangeAxis(0).setRange(runData.getMinY() - paddingY, runData.getMaxY() + paddingY);
	    	plot.getRangeAxis(0).setLabel(mnmdAxisName);
	    	plot.getRangeAxis(1).setLabel(mnmd2AxisName);
	    	plot.getDomainAxis(0).setLabel(XAxisName);
	        plot.getRenderer(0).setSeriesVisible(0, false);
	        currMafData.setDescription("Mean");
	        corrMafData.setDescription("Mode");
        }
        else if (checkBoxRunData.isSelected() && checkBoxRunData.isEnabled() &&
        	     !checkBoxCurrentMaf.isSelected() && !checkBoxCorrectedMaf.isSelected() && !checkBoxSmoothedMaf.isSelected()) {
        	paddingX = runData.getMaxX() * 0.05;
        	paddingY = runData.getMaxY() * 0.05;
        	plot.getDomainAxis(0).setRange(runData.getMinX() - paddingX, runData.getMaxX() + paddingX);
	    	plot.getRangeAxis(1).setRange(runData.getMinY() - paddingY, runData.getMaxY() + paddingY);
        }
        else if (checkBoxSmoothing.isSelected()) {
        	double maxY = Collections.max(Arrays.asList(new Double[] { currMafData.getMaxY(), smoothMafData.getMaxY(), corrMafData.getMaxY() }));
        	double minY = Collections.max(Arrays.asList(new Double[] { currMafData.getMinY(), smoothMafData.getMinY(), corrMafData.getMinY() }));
        	paddingX = smoothMafData.getMaxX() * 0.05;
        	paddingY = maxY * 0.05;
        	plot.getDomainAxis(0).setRange(smoothMafData.getMinX() - paddingX, smoothMafData.getMaxX() + paddingX);
	    	plot.getRangeAxis(0).setRange(minY - paddingY, maxY + paddingY);
            corrMafData.setDescription(mafCurveDataName);
            currMafData.setDescription(currentSlopeDataName);
            smoothMafData.setDescription(smoothedSlopeDataName);
        }
        else if ((checkBoxCurrentMaf.isSelected() && checkBoxCurrentMaf.isEnabled()) ||
            	 (checkBoxCorrectedMaf.isSelected() && checkBoxCorrectedMaf.isEnabled()) ||
            	 (checkBoxSmoothedMaf.isSelected() && checkBoxSmoothedMaf.isEnabled())) {
        	paddingX = voltArray.get(voltArray.size() - 1) * 0.05;
        	paddingY = gsCorrected.get(gsCorrected.size() - 1) * 0.05;
        	plot.getDomainAxis(0).setRange(voltArray.get(0) - paddingX, voltArray.get(voltArray.size() - 1) + paddingX);
	    	plot.getRangeAxis(0).setRange(gsCorrected.get(0) - paddingY, gsCorrected.get(gsCorrected.size() - 1) + paddingY);
	    	if (checkBoxRunData.isSelected()) {
	        	paddingX = runData.getMaxX() * 0.05;
	        	paddingY = runData.getMaxY() * 0.05;
	    		plot.getRangeAxis(1).setRange(runData.getMinY() - paddingY, runData.getMaxY() + paddingY);
	    	}
        }
        else {
	    	plot.getRangeAxis(0).setAutoRange(true);
        	plot.getDomainAxis(0).setAutoRange(true);
        }
    }
    
    protected void onEnableSmoothingView(boolean flag) {
        checkBoxDvdtData.setEnabled(!flag);
        checkBoxIatData.setEnabled(!flag);
        checkBoxTrpmData.setEnabled(!flag);
        checkBoxMnmdData.setEnabled(!flag);
        if (flag == false) {
            if (checkBoxDvdtData.isSelected())
            	plotDvdtData();
            else if (checkBoxIatData.isSelected())
            	plotIatData();
            else if (checkBoxTrpmData.isSelected())
            	plotTrimRpmData();
            else if (checkBoxMnmdData.isSelected())
            	plotMeanModeData();
            else {
	            if (checkBoxRunData.isSelected())
	                plotCorrectionData();
	            if (checkBoxCurrentMaf.isSelected())
	                plotCurrentMafData();
	            if (checkBoxCorrectedMaf.isSelected())
	                setCorrectedMafData();
	            if (checkBoxSmoothedMaf.isSelected())
	                setSmoothedMafData();
            }
        }
    }
    
    protected void onSmoothReset() {
	    if (!checkBoxDvdtData.isEnabled() || !checkBoxDvdtData.isSelected() ||
	    	!checkBoxTrpmData.isEnabled() || !checkBoxTrpmData.isSelected() || 
	    	!checkBoxMnmdData.isEnabled() || !checkBoxMnmdData.isSelected() || 
	    	!checkBoxIatData.isEnabled() || !checkBoxIatData.isSelected())
	    	setCorrectedMafData();
	    if (checkBoxSmoothing.isSelected())
	        plotSmoothingLineSlopes();
	    else if (checkBoxSmoothedMaf.isSelected())
	        setSmoothedMafData();
    }
   
    public void saveData() {
        if (JFileChooser.APPROVE_OPTION != fileChooser.showSaveDialog(this))
            return;
        File file = fileChooser.getSelectedFile();
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        int i, j;
        FileWriter out = null;
        try {
        	out = new FileWriter(file);
            // write string identifier
            out.write(SaveDataFileHeader + "\n");
            // write maf data
            for (i = 0; i < mafTable.getRowCount(); ++i) {
                for (j = 0; j < mafTable.getColumnCount(); ++j)
                    out.write(mafTable.getValueAt(i, j).toString() + ",");
                out.write("\n");
            }
            // write log data
            for (i = 0; i < logDataTable.getRowCount(); ++i) {
                for (j = 0; j < logDataTable.getColumnCount(); ++j)
                    out.write(logDataTable.getValueAt(i, j).toString() + ",");
                out.write("\n");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
        finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        	if (out != null) {
                try {
                    out.close();
                }
                catch (IOException e) {
                    logger.error(e);
                }
        	}
        }
    }
   
    public void loadData() {
    	fileChooser.setMultiSelectionEnabled(false);
        if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(this))
            return;
        File file = fileChooser.getSelectedFile();
        int i, j;
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        BufferedReader br = null;
        try {
        	br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
            String line = br.readLine();
            if (line == null || !line.equals(SaveDataFileHeader)) {
                JOptionPane.showMessageDialog(null, "Invalid saved data file!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            line = br.readLine();
            String[] elements;
            i = 0;
            int offset = 0;
            boolean isLogData = false;
            while (line != null) {
                elements = line.split("\\s*,\\s*", -1);
                switch (i) {
                case 0:
                    Utils.ensureColumnCount(elements.length - 1, mafTable);
                    for (j = 0; j < elements.length - 1; ++j)
                        mafTable.setValueAt(elements[j], i, j);
                    break;
                case 1:
                    Utils.ensureColumnCount(elements.length - 1, mafTable);
                    for (j = 0; j < elements.length - 1; ++j)
                        mafTable.setValueAt(elements[j], i, j);
                    break;
                default:
                    if (elements.length - 1 == logDataTable.getColumnCount()) {
                    	if (!isLogData) {
                    		offset = i;
                    		isLogData = true;
                    	}
                        Utils.ensureRowCount(i - offset + 1, logDataTable);
                        for (j = 0; j < elements.length - 1; ++j)
                            logDataTable.setValueAt(elements[j], i - offset, j);
                    }
                }
                i += 1;
                line = br.readLine();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
        finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
    
    private int getLogTableEmptyRow() {
    	if (logDataTable.getValueAt(0, 0) != null && logDataTable.getValueAt(0, 0).toString().isEmpty())
    		return 0;
    	if (logDataTable.getValueAt(logDataTable.getRowCount() - 1, 0) != null && !logDataTable.getValueAt(0, 0).toString().isEmpty())
    		return logDataTable.getRowCount();
    	for (int i = logDataTable.getRowCount() - 2; i > 0; --i) {
    		if (logDataTable.getValueAt(i, 0) != null && !logDataTable.getValueAt(i, 0).toString().isEmpty())
    			return i + 1;
    	}
    	return 0;
    }
    
    private boolean getColumnsFilters(String[] elements) {
    	boolean ret = true;
        ArrayList<String> columns = new ArrayList<String>(Arrays.asList(elements));
        String logClOlStatusColName = Config.getClOlStatusColumnName();
        String logAfLearningColName = Config.getAfLearningColumnName();
        String logAfCorrectionColName = Config.getAfCorrectionColumnName();
        String logAfrColName = Config.getAfrColumnName();
        String logRpmColName = Config.getRpmColumnName();
        String logLoadColName = Config.getLoadColumnName();
        String logTimeColName = Config.getTimeColumnName();
        String logMafvColName = Config.getMafVoltageColumnName();
        String logIatColName = Config.getIatColumnName();
        String logMapColName = Config.getMapColumnName();
        logClOlStatusColIdx = columns.indexOf(logClOlStatusColName);
        logAfLearningColIdx = columns.indexOf(logAfLearningColName);
        logAfCorrectionColIdx = columns.indexOf(logAfCorrectionColName);
        logAfrColIdx = columns.indexOf(logAfrColName);
        logRpmColIdx = columns.indexOf(logRpmColName);
        logLoadColIdx = columns.indexOf(logLoadColName);
        logTimeColIdx = columns.indexOf(logTimeColName);
        logMafvColIdx = columns.indexOf(logMafvColName);
        logIatColIdx = columns.indexOf(logIatColName);
        logMapColIdx = columns.indexOf(logMapColName);
        if (logClOlStatusColIdx == -1)   { Config.setClOlStatusColumnName(Config.NO_NAME);   ret = false; }
        if (logAfLearningColIdx == -1)   { Config.setAfLearningColumnName(Config.NO_NAME);   ret = false; }
        if (logAfCorrectionColIdx == -1) { Config.setAfCorrectionColumnName(Config.NO_NAME); ret = false; }
        if (logAfrColIdx == -1)          { Config.setAfrColumnName(Config.NO_NAME);          ret = false; }
        if (logRpmColIdx == -1)          { Config.setRpmColumnName(Config.NO_NAME);          ret = false; }
        if (logLoadColIdx == -1)         { Config.setLoadColumnName(Config.NO_NAME);         ret = false; }
        if (logTimeColIdx == -1)         { Config.setTimeColumnName(Config.NO_NAME);         ret = false; }
        if (logMafvColIdx == -1)         { Config.setMafVoltageColumnName(Config.NO_NAME);   ret = false; }
        if (logIatColIdx == -1)          { Config.setIatColumnName(Config.NO_NAME);          ret = false; }
        if (logMapColIdx == -1)          { Config.setMapColumnName(Config.NO_NAME);          if (polfTable.isMap()) ret = false; }
        clValue = Config.getClOlStatusValue();
        minCellHitCount = Config.getCLMinCellHitCount();
        afrMin = Config.getAfrMinimumValue();
        afrMax = Config.getAfrMaximumValue();
        minLoad = Config.getLoadMinimumValue();
        maxDvDt = Config.getDvDtMaximumValue();
        maxMafV = Config.getMafVMaximumValue();
        maxIat = Config.getIatMaximumValue();
        return ret;
    }
    
    protected void loadLogFile() {
        boolean displayDialog = true;
        File[] files = fileChooser.getSelectedFiles();
        for (File file : files) {
	        BufferedReader br = null;
	        try {
	            br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
	            String line = null;
	            String [] elements = null;
	            while ((line = br.readLine()) != null && (elements = line.split("\\s*,\\s*", -1)) != null && elements.length < 2)
	            	continue;
                getColumnsFilters(elements);
                boolean resetColumns = false;

                if (logClOlStatusColIdx >= 0 || logAfLearningColIdx >= 0 || logAfCorrectionColIdx >= 0 || logAfrColIdx >= 0 ||
                	logRpmColIdx >= 0 || logLoadColIdx >=0 || logTimeColIdx >=0 || logMafvColIdx >= 0 || logIatColIdx >= 0  ||
                	logMapColIdx >= 0 ) {
                	if (displayDialog) {
	                    int rc = JOptionPane.showOptionDialog(null, "Would you like to reset column names or filter values?", "Columns/Filters Reset", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, optionButtons, optionButtons[0]);
	                    if (rc == 0)
	                    	resetColumns = true;
	                    else if (rc == 2)
	                    	displayDialog = false;
                	}
                }

                if (resetColumns || logClOlStatusColIdx < 0 || logAfLearningColIdx < 0 || logAfCorrectionColIdx < 0 || logAfrColIdx < 0 ||
                	logRpmColIdx < 0 || logLoadColIdx < 0 || logTimeColIdx < 0 || logMafvColIdx < 0 || logIatColIdx < 0 || logMapColIdx < 0 && polfTable.isMap() ) {
                	ColumnsFiltersSelection selectionWindow = new CLColumnsFiltersSelection(polfTable.isSet(), polfTable.isMap());
                	if (!selectionWindow.getUserSettings(elements) || !getColumnsFilters(elements))
                		return;
                }
                
                String[] flds;
                line = br.readLine();
                int clol;
                int i = 2;
                int row = getLogTableEmptyRow();
                long time = 0;
                long prevTime = 0;
                double afr = 0;
                double dVdt = 0;
                double pmafv = 0;
                double mafv = 0;
                double load;
                double iat;
                double map;
                setCursor(new Cursor(Cursor.WAIT_CURSOR));

                while (line != null) {
                    flds = line.split("\\s*,\\s*", -1);
                    try {
                    	// Calculate dV/dt
                    	prevTime = time;
                    	if (prevTime == 0)
                    		Utils.resetBaseTime(flds[logTimeColIdx]);
                    	time = Utils.parseTime(flds[logTimeColIdx]);
                    	pmafv = mafv;
                    	mafv = Double.valueOf(flds[logMafvColIdx]);
                    	if ((time - prevTime) == 0)
                    		dVdt = 100.0;
                    	else
                    		dVdt = Math.abs(((mafv - pmafv) / (time - prevTime)) * 1000.0);
                        clol = (int)Utils.parseValue(flds[logClOlStatusColIdx]);
                        if (clol == clValue) {
                            // Filters
                        	afr = Double.valueOf(flds[logAfrColIdx]);
                        	load = Double.valueOf(flds[logLoadColIdx]);
                        	iat = Double.valueOf(flds[logIatColIdx]);
                        	map = polfTable.isMap() ? Double.valueOf(flds[logMapColIdx]) : 0.0;
                        	if (afrMin <= afr && afr <= afrMax && minLoad <= load && dVdt <= maxDvDt && maxMafV >= mafv && maxIat >= iat) {
                                Utils.ensureRowCount(row + 1, logDataTable);
                                logDataTable.setValueAt(time, row, 0);
                                logDataTable.setValueAt(load, row, 1);
                                logDataTable.setValueAt(Double.valueOf(flds[logRpmColIdx]), row, 2);
                                logDataTable.setValueAt(mafv, row, 3);
                                logDataTable.setValueAt(afr, row, 4);
                                logDataTable.setValueAt(Double.valueOf(flds[logAfCorrectionColIdx]), row, 5);
                                logDataTable.setValueAt(Double.valueOf(flds[logAfLearningColIdx]), row, 6);
                                logDataTable.setValueAt(dVdt, row, 7);
                                logDataTable.setValueAt(iat, row, 8);
                                logDataTable.setValueAt(map, row, 9);
                                row += 1;
                        	}
                        }
                    }
                    catch (NumberFormatException e) {
                        logger.error(e);
                        JOptionPane.showMessageDialog(null, "Error parsing number at " + file.getName() + " line " + i + ": " + e, "Error processing file", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    line = br.readLine();
                    i += 1;
                }
	        }
	        catch (Exception e) {
	            logger.error(e);
	            JOptionPane.showMessageDialog(null, e, "Error opening file", JOptionPane.ERROR_MESSAGE);
	        }
	        finally {
	        	setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
    }
    
    protected String usage() {
        ResourceBundle bundle;
        bundle = ResourceBundle.getBundle("com.vgi.mafscaling.closed_loop");
        return bundle.getString("usage"); 
    }
}

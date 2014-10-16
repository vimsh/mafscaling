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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYSeriesLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.function.Function2D;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;
import org.math.plot.Plot3DPanel;

public class LoadComp extends JTabbedPane implements ActionListener, IMafChartHolder {
    private static final long serialVersionUID = 2988105467764335997L;
    private static final Logger logger = Logger.getLogger(LoadComp.class);

    private static final String MpTableName = "Current MP table";
    private static final String NewMpTableName = "New MP table";
    private static final String MpCorrTableName = "MP Correction (%) table";
    private static final String MpCorrCountTableName = "MP Correction Count table";
    private static final String dvdtAxisName = "dV / dt";
    private static final String iatAxisName = "IAT";
    private static final String trpmAxisName = "Trims";
    private static final String timeAxisName = "Time";
    private static final String rpmAxisName = "RPM";
    private static final String mafvAxisName = "MAF Voltage";
    private static final String x3dAxisName = "MP";
    private static final String y3dAxisName = "RPM";
    private static final String z3dAxisName = "Avg Error %";
    private static final int ColumnWidth = 50;
    private static final int ColumnCount = 9;
    private static final int MpTableRowCount = 20;
    private static final int LogDataRowCount = 200;
    private boolean isLoadCompInRatio = Config.getIsLoadCompInRatio();
    private int thrtlMaxChange = Config.getThrottleChangeMaxValue();
    private int minCellHitCount = Config.getLCMinCellHitCount();
    private double afrMin = Config.getLCAfrMinimumValue();
    private double afrMax = Config.getLCAfrMaximumValue();
    private double rpmMax = Config.getRPMMaximumValue();
    private double rpmMin = Config.getRPMMinimumValue();
    private double dvDtMax = Config.getLCDvDtMaximumValue();
    private double iatMax = Config.getLCIatMaximumValue();
    private double cruiseValue = Config.getCruiseStatusValue();
    private int corrApplied = Config.getLCCorrectionAppliedValue();
    
    private int logThrottleAngleColIdx = -1;
    private int logAfLearningColIdx = -1;
    private int logAfCorrectionColIdx = -1;
    private int logAfrColIdx = -1;
    private int logRpmColIdx = -1;
    private int logTimeColIdx = -1;
    private int logMafvColIdx = -1;
    private int logIatColIdx = -1;
    private int logMpColIdx = -1;
    private int logCruiseStatusColIdx = -1;

    private JTable mpTable = null;
    private JTable newMpTable = null;
    private JTable mpCorrTable = null;
    private JTable mpCorrCountTable = null;
    private JTable logDataTable = null;
    private JCheckBox compareTableCheckBox = null;
    private ButtonGroup rbGroup = null;
    private ChartPanel chartPanel = null;
    private JScrollPane dataScrollPane = null;

    private ExcelAdapter mpExcelAdapter = null;
    private ArrayList<ExcelAdapter> excelAdapterList = new ArrayList<ExcelAdapter>();
    private ArrayList<Double> trimArray = new ArrayList<Double>();
    private ArrayList<Double> rpmArray = new ArrayList<Double>();
    private ArrayList<Double> mafvArray = new ArrayList<Double>();
    private ArrayList<Double> timeArray = new ArrayList<Double>();
    private ArrayList<Double> iatArray = new ArrayList<Double>();
    private ArrayList<Double> dvdtArray = new ArrayList<Double>();
    private final JFileChooser fileChooser = new JFileChooser();
    private final XYSeries runData = new XYSeries(dvdtAxisName);
    private final XYSeries trendData = new XYSeries("Trend");
    private List<XYSeries> corrData = new ArrayList<XYSeries>();
    HashMap<Double, HashMap<Double, ArrayList<Double>>> xData = null;
    private Plot3DPanel plot3d = null;
    private ArrayList<Double> xAxisArray = null;
    private ArrayList<Double> yAxisArray = null;
    private ArrayList<ArrayList<Double>> savedNewMpTable = new ArrayList<ArrayList<Double>>();

    public LoadComp(int tabPlacement) {
        super(tabPlacement);
        initialize();
    }

    private void initialize() {
        mpExcelAdapter = new ExcelAdapter() {
    	    protected void onPaste(JTable table, boolean extendRows, boolean extendCols) {
    	    	super.onPaste(table, extendRows, extendCols);
    	    	validateTable(table);
    	    	clearRunMpTables();
    	    }
    	};
        createDataTab();
        createGraghTab();
        create3dGraghTab();
        createUsageTab();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // DATA TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void createDataTab() {
    	fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setCurrentDirectory(new File("."));
        
        JPanel dataPanel = new JPanel();
        add(dataPanel, "<html><div style='text-align: center;'>D<br>a<br>t<br>a</div></html>");
        GridBagLayout gbl_dataPanel = new GridBagLayout();
        gbl_dataPanel.columnWidths = new int[] {0, 0};
        gbl_dataPanel.rowHeights = new int[] {0, 0};
        gbl_dataPanel.columnWeights = new double[]{0.0, 0.0};
        gbl_dataPanel.rowWeights = new double[]{0.0, 1.0};
        dataPanel.setLayout(gbl_dataPanel);

        JPanel cntlPanel = new JPanel();
        GridBagConstraints gbl_ctrlPanel = new GridBagConstraints();
        gbl_ctrlPanel.insets = new Insets(3, 3, 3, 3);
        gbl_ctrlPanel.anchor = GridBagConstraints.PAGE_START;
        gbl_ctrlPanel.weightx = 1.0;
        gbl_ctrlPanel.fill = GridBagConstraints.HORIZONTAL;
        gbl_ctrlPanel.gridx = 0;
        gbl_ctrlPanel.gridy = 0;
        gbl_ctrlPanel.gridwidth = 2;
        dataPanel.add(cntlPanel, gbl_ctrlPanel);
        
        GridBagLayout gbl_cntlPanel = new GridBagLayout();
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);
        
        JButton btnLoadDataButton = new JButton("Load Log");
        GridBagConstraints gbc_btnLoadDataButton = new GridBagConstraints();
        gbc_btnLoadDataButton.insets = new Insets(0, 0, 5, 5);
        gbc_btnLoadDataButton.gridx = 0;
        gbc_btnLoadDataButton.gridy = 0;
        btnLoadDataButton.setActionCommand("loadlog");
        btnLoadDataButton.addActionListener(this);
        cntlPanel.add(btnLoadDataButton, gbc_btnLoadDataButton);
        
        JButton btnMpClearButton = new JButton("Clear MP Data");
        GridBagConstraints gbc_btnMpClearButton = new GridBagConstraints();
        gbc_btnMpClearButton.anchor = GridBagConstraints.PAGE_START;
        gbc_btnMpClearButton.insets = new Insets(0, 0, 5, 5);
        gbc_btnMpClearButton.gridx = 1;
        gbc_btnMpClearButton.gridy = 0;
        btnMpClearButton.setActionCommand("clearmp");
        btnMpClearButton.addActionListener(this);
        cntlPanel.add(btnMpClearButton, gbc_btnMpClearButton);
        
        JButton btnRunClearButton = new JButton("Clear Run Data");
        GridBagConstraints gbc_btnRunClearButton = new GridBagConstraints();
        gbc_btnRunClearButton.insets = new Insets(0, 0, 5, 5);
        gbc_btnRunClearButton.gridx = 2;
        gbc_btnRunClearButton.gridy = 0;
        btnRunClearButton.setActionCommand("clearlog");
        btnRunClearButton.addActionListener(this);
        cntlPanel.add(btnRunClearButton, gbc_btnRunClearButton);
        
        JButton btnAllClearButton = new JButton("Clear All");
        GridBagConstraints gbc_btnAllClearButton = new GridBagConstraints();
        gbc_btnAllClearButton.insets = new Insets(0, 0, 5, 5);
        gbc_btnAllClearButton.gridx = 3;
        gbc_btnAllClearButton.gridy = 0;
        btnAllClearButton.setActionCommand("clearall");
        btnAllClearButton.addActionListener(this);
        cntlPanel.add(btnAllClearButton, gbc_btnAllClearButton);
        
        JCheckBox hideLogTableCheckBox = new JCheckBox("Hide Log Table");
        GridBagConstraints gbc_hideLogTableCheckBox = new GridBagConstraints();
        gbc_hideLogTableCheckBox.insets = new Insets(0, 0, 3, 3);
        gbc_hideLogTableCheckBox.gridx = 4;
        gbc_hideLogTableCheckBox.gridy = 0;
        hideLogTableCheckBox.setActionCommand("hidelogtable");
        hideLogTableCheckBox.addActionListener(this);
        cntlPanel.add(hideLogTableCheckBox, gbc_hideLogTableCheckBox);
        
        compareTableCheckBox = new JCheckBox("Compare Tables");
        GridBagConstraints gbc_compareTableCheckBox = new GridBagConstraints();
        gbc_compareTableCheckBox.insets = new Insets(0, 0, 3, 3);
        gbc_compareTableCheckBox.gridx = 5;
        gbc_compareTableCheckBox.gridy = 0;
        compareTableCheckBox.setActionCommand("comparetables");
        compareTableCheckBox.addActionListener(this);
        cntlPanel.add(compareTableCheckBox, gbc_compareTableCheckBox);
        
        JButton btnGoButton = new JButton("GO");
        GridBagConstraints gbc_btnGoButton = new GridBagConstraints();
        gbc_btnGoButton.anchor = GridBagConstraints.EAST;
        gbc_btnGoButton.insets = new Insets(0, 0, 5, 0);
        gbc_btnGoButton.gridx = 6;
        gbc_btnGoButton.gridy = 0;
        btnGoButton.setActionCommand("go");
        btnGoButton.addActionListener(this);
        cntlPanel.add(btnGoButton, gbc_btnGoButton);

        dataScrollPane = new JScrollPane();
        GridBagConstraints gbc_dataScrollPane = new GridBagConstraints();
        gbc_dataScrollPane.weightx = 0;
        gbc_dataScrollPane.weighty = 1.0;
        gbc_dataScrollPane.fill = GridBagConstraints.BOTH;
        gbc_dataScrollPane.ipadx = ColumnWidth * ColumnCount;
        gbc_dataScrollPane.gridx = 0;
        gbc_dataScrollPane.gridy = 1;
        dataPanel.add(dataScrollPane, gbc_dataScrollPane);

        JPanel dataRunPanel = new JPanel();
        dataScrollPane.setViewportView(dataRunPanel);
        GridBagLayout gbl_dataRunPanel = new GridBagLayout();
        gbl_dataRunPanel.columnWidths = new int[]{0};
        gbl_dataRunPanel.rowHeights = new int[] {0};
        gbl_dataRunPanel.columnWeights = new double[]{0.0};
        gbl_dataRunPanel.rowWeights = new double[]{0.0};
        dataRunPanel.setLayout(gbl_dataRunPanel);

        JScrollPane aprScrollPane = new JScrollPane();
        GridBagConstraints gbc_aprScrollPane = new GridBagConstraints();
        gbc_aprScrollPane.weightx = 1.0;
        gbc_aprScrollPane.weighty = 1.0;
        gbc_aprScrollPane.anchor = GridBagConstraints.PAGE_START;
        gbc_aprScrollPane.fill = GridBagConstraints.BOTH;
        gbc_aprScrollPane.gridx = 1;
        gbc_aprScrollPane.gridy = 1;
        dataPanel.add(aprScrollPane, gbc_aprScrollPane);
        
        JPanel MpTablesPanel = new JPanel();
        aprScrollPane.setViewportView(MpTablesPanel);
        GridBagLayout gbl_MpTablesPanel = new GridBagLayout();
        gbl_MpTablesPanel.columnWidths = new int[]{0, 0};
        gbl_MpTablesPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
        gbl_MpTablesPanel.columnWeights = new double[]{0.0, 1.0};
        gbl_MpTablesPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        MpTablesPanel.setLayout(gbl_MpTablesPanel);

        createLogDataTable(dataRunPanel);
        createMpDataTables(MpTablesPanel);
    }
    
    private void createLogDataTable(JPanel panel) {
            logDataTable = new JTable();
            logDataTable.getTableHeader().setReorderingAllowed(false);
            logDataTable.setModel(new DefaultTableModel(LogDataRowCount, ColumnCount));
            logDataTable.setColumnSelectionAllowed(true);
            logDataTable.setCellSelectionEnabled(true);
            logDataTable.setBorder(new LineBorder(new Color(0, 0, 0)));
            logDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
            logDataTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            logDataTable.getColumnModel().getColumn(0).setHeaderValue("Time");
            logDataTable.getColumnModel().getColumn(1).setHeaderValue("RPM");
            logDataTable.getColumnModel().getColumn(2).setHeaderValue("IAT");
            logDataTable.getColumnModel().getColumn(3).setHeaderValue("STFT");
            logDataTable.getColumnModel().getColumn(4).setHeaderValue("LTFT");
            logDataTable.getColumnModel().getColumn(5).setHeaderValue("MP");
            logDataTable.getColumnModel().getColumn(6).setHeaderValue("MafV");
            logDataTable.getColumnModel().getColumn(7).setHeaderValue("Trims");
            logDataTable.getColumnModel().getColumn(8).setHeaderValue("dV/dt");
            Utils.initializeTable(logDataTable, ColumnWidth);

            JTableHeader header = logDataTable.getTableHeader();
            GridBagConstraints gbc_header = new GridBagConstraints();
            gbc_header.insets = new Insets(0, 0, 0, 0);
            gbc_header.gridx = 0;
            gbc_header.gridy = 0;
            panel.add(header, gbc_header);

            GridBagConstraints gbc_table = new GridBagConstraints();
            gbc_table.insets = new Insets(0, 0, 0, 0);
            gbc_table.anchor = GridBagConstraints.PAGE_START;
            gbc_table.gridx = 0;
            gbc_table.gridy = 1;
            panel.add(logDataTable, gbc_table);

	        ExcelAdapter excelAdapter = new ExcelAdapter();
            excelAdapter.addTable(logDataTable, true, false);
            excelAdapterList.add(excelAdapter);
    }
    
    private void createMpDataTables(JPanel panel) {
        mpTable = createMpDataTable(panel, MpTableName, 0);
        newMpTable = createMpDataTable(panel, NewMpTableName, 2);
        mpCorrTable = createMpDataTable(panel, MpCorrTableName, 4);
        mpCorrCountTable = createMpDataTable(panel, MpCorrCountTableName, 6);
    }
    
    private JTable createMpDataTable(JPanel panel, String tableName, int gridy) {
    	DefaultTableColumnModel afrModel = new DefaultTableColumnModel();
        final TableColumn afrColumn = new TableColumn(0, 250);
        afrColumn.setHeaderValue(tableName);
        afrModel.addColumn(afrColumn);
        JTableHeader lblAfrTableName = new JTableHeader();
        lblAfrTableName.setColumnModel(afrModel);
        lblAfrTableName.setReorderingAllowed(false);
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) lblAfrTableName.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        
        GridBagConstraints gbc_lblAfrTableName = new GridBagConstraints();
        gbc_lblAfrTableName.insets = new Insets((gridy == 0 ? 0 : 5),0,0,0);
        gbc_lblAfrTableName.anchor = GridBagConstraints.WEST;
        gbc_lblAfrTableName.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblAfrTableName.gridx = 0;
        gbc_lblAfrTableName.gridy = gridy;
        panel.add(lblAfrTableName, gbc_lblAfrTableName);
        
        final JTable table;
        if (tableName.equals(MpTableName)) {
	        table = new JTable() {
				private static final long serialVersionUID = 3218402382894083287L;
				public boolean isCellEditable(int row, int column) { return false; };
	        };
	        mpExcelAdapter.addTable(table, false, true, false, false, true, true, false, true, true);
        }
        else {
        	table = new JTable() {
				private static final long serialVersionUID = -3754572906310312568L;
	        };
	        ExcelAdapter excelAdapter = new ExcelAdapter();
	        excelAdapter.addTable(table, false, true, false, false, true, true, false, true, true);
            excelAdapterList.add(excelAdapter);
        }
        table.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                afrColumn.setWidth(table.getWidth());
            }
        });
        table.setName(tableName);
        table.getTableHeader().setReorderingAllowed(false);
        table.setColumnSelectionAllowed(true);
        table.setCellSelectionEnabled(true);
        table.setBorder(new LineBorder(new Color(0, 0, 0)));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setModel(new DefaultTableModel(MpTableRowCount, MpTableRowCount));
        table.setTableHeader(null);
        Utils.initializeTable(table, ColumnWidth);
        
        formatMpTable(table);
        
        GridBagConstraints gbc_table = new GridBagConstraints();
        gbc_table.insets = new Insets(0, 0, 0, 0);
        gbc_table.anchor = GridBagConstraints.PAGE_START;
        gbc_table.gridx = 0;
        gbc_table.gridy = gridy + 1;
        panel.add(table, gbc_table);

        return table;
    }
    
    private void formatMpTable(JTable table) {
        if (table.getName().equals(MpCorrCountTableName)) {
	        Format[][] formatMatrix = { { new DecimalFormat("#"), new DecimalFormat("0.00") }, { new DecimalFormat("#"), new DecimalFormat("#") } };
	        NumberFormatRenderer renderer = (NumberFormatRenderer)table.getDefaultRenderer(Object.class);
	        renderer.setFormats(formatMatrix);
        }
        else {
	        Format[][] formatMatrix = { { new DecimalFormat("#"), new DecimalFormat("0.00") } };
	        NumberFormatRenderer renderer = (NumberFormatRenderer)table.getDefaultRenderer(Object.class);
	        renderer.setFormats(formatMatrix);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE CHART TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void createGraghTab() {
    	rbGroup = new ButtonGroup();
        JPanel plotPanel = new JPanel();
        add(plotPanel, "<html><div style='text-align: center;'>C<br>h<br>a<br>r<br>t</div></html>");

        GridBagLayout gbl_plotPanel = new GridBagLayout();
        gbl_plotPanel.columnWidths = new int[] {0};
        gbl_plotPanel.rowHeights = new int[] {0, 0};
        gbl_plotPanel.columnWeights = new double[]{1.0};
        gbl_plotPanel.rowWeights = new double[]{0.0, 1.0};
        plotPanel.setLayout(gbl_plotPanel);

        JPanel cntlPanel = new JPanel();
        GridBagConstraints gbl_ctrlPanel = new GridBagConstraints();
        gbl_ctrlPanel.insets = new Insets(3, 3, 3, 3);
        gbl_ctrlPanel.anchor = GridBagConstraints.NORTH;
        gbl_ctrlPanel.weightx = 1.0;
        gbl_ctrlPanel.fill = GridBagConstraints.HORIZONTAL;
        gbl_ctrlPanel.gridx = 0;
        gbl_ctrlPanel.gridy = 0;
        plotPanel.add(cntlPanel, gbl_ctrlPanel);
        
        GridBagLayout gbl_cntlPanel = new GridBagLayout();
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0, 0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);
        
        JRadioButton radioButtonDvdtData = new JRadioButton("dV/dt");
        GridBagConstraints gbc_radioButtonDvdtData = new GridBagConstraints();
        gbc_radioButtonDvdtData.anchor = GridBagConstraints.WEST;
        gbc_radioButtonDvdtData.insets = new Insets(0, 0, 3, 3);
        gbc_radioButtonDvdtData.gridx = 0;
        gbc_radioButtonDvdtData.gridy = 0;
        radioButtonDvdtData.setActionCommand("dvdt");
        radioButtonDvdtData.addActionListener(this);
        rbGroup.add(radioButtonDvdtData);
        cntlPanel.add(radioButtonDvdtData, gbc_radioButtonDvdtData);
        
        JRadioButton radioButtonIatData = new JRadioButton("IAT");
        GridBagConstraints gbc_radioButtonIatData = new GridBagConstraints();
        gbc_radioButtonIatData.anchor = GridBagConstraints.WEST;
        gbc_radioButtonIatData.insets = new Insets(0, 0, 3, 3);
        gbc_radioButtonIatData.gridx = 1;
        gbc_radioButtonIatData.gridy = 0;
        radioButtonIatData.setActionCommand("iat");
        radioButtonIatData.addActionListener(this);
        rbGroup.add(radioButtonIatData);
        cntlPanel.add(radioButtonIatData, gbc_radioButtonIatData);
        
        JRadioButton radioButtonTrpmData = new JRadioButton("Trims/RPM");
        GridBagConstraints gbc_radioButtonTrpmData = new GridBagConstraints();
        gbc_radioButtonTrpmData.anchor = GridBagConstraints.WEST;
        gbc_radioButtonTrpmData.insets = new Insets(0, 0, 3, 3);
        gbc_radioButtonTrpmData.gridx = 2;
        gbc_radioButtonTrpmData.gridy = 0;
        radioButtonTrpmData.setActionCommand("trpm");
        radioButtonTrpmData.addActionListener(this);
        rbGroup.add(radioButtonTrpmData);
        cntlPanel.add(radioButtonTrpmData, gbc_radioButtonTrpmData);
        
        JRadioButton radioButtonTrmafvData = new JRadioButton("Trims/MAFv");
        GridBagConstraints gbc_radioButtonTrmafvData = new GridBagConstraints();
        gbc_radioButtonTrmafvData.anchor = GridBagConstraints.WEST;
        gbc_radioButtonTrmafvData.insets = new Insets(0, 0, 3, 3);
        gbc_radioButtonTrmafvData.gridx = 3;
        gbc_radioButtonTrmafvData.gridy = 0;
        radioButtonTrmafvData.setActionCommand("trmafv");
        radioButtonTrmafvData.addActionListener(this);
        rbGroup.add(radioButtonTrmafvData);
        cntlPanel.add(radioButtonTrmafvData, gbc_radioButtonTrmafvData);
        
        JRadioButton radioButtonCorrData = new JRadioButton("Corrections");
        GridBagConstraints gbc_radioButtonCorrData = new GridBagConstraints();
        gbc_radioButtonCorrData.anchor = GridBagConstraints.WEST;
        gbc_radioButtonCorrData.insets = new Insets(0, 0, 3, 3);
        gbc_radioButtonCorrData.gridx = 4;
        gbc_radioButtonCorrData.gridy = 0;
        radioButtonCorrData.setActionCommand("corr");
        radioButtonCorrData.addActionListener(this);
        rbGroup.add(radioButtonCorrData);
        cntlPanel.add(radioButtonCorrData, gbc_radioButtonCorrData);
        
        JFreeChart chart = ChartFactory.createScatterPlot(null, null, null, null, PlotOrientation.VERTICAL, false, true, false);
        chart.setBorderVisible(true);

        chartPanel = new ChartPanel(chart, true, true, true, true, true);
		chartPanel.setAutoscrolls(true);
		chartPanel.setMouseZoomable(false);
        
        GridBagConstraints gbl_chartPanel = new GridBagConstraints();
        gbl_chartPanel.anchor = GridBagConstraints.CENTER;
        gbl_chartPanel.insets = new Insets(3, 3, 3, 3);
        gbl_chartPanel.weightx = 1.0;
        gbl_chartPanel.weighty = 1.0;
        gbl_chartPanel.fill = GridBagConstraints.BOTH;
        gbl_chartPanel.gridx = 0;
        gbl_chartPanel.gridy = 1;
        plotPanel.add(chartPanel, gbl_chartPanel);

        XYDotRenderer dotRenderer = new XYDotRenderer();
        dotRenderer.setSeriesPaint(0, new Color(0, 51, 102));
        dotRenderer.setDotHeight(3);
        dotRenderer.setDotWidth(3);

        XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer();
        lineRenderer.setUseFillPaint(true);
        lineRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator( 
                StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, 
                new DecimalFormat("0.00"), new DecimalFormat("0.00")));
        
        Stroke stroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, null, 0.0f);
        lineRenderer.setSeriesStroke(0, stroke);
        lineRenderer.setSeriesPaint(0, Color.RED);
        lineRenderer.setSeriesShape(0, ShapeUtilities.createDiamond((float) 2.5));

        lineRenderer.setLegendItemLabelGenerator(
        		new StandardXYSeriesLabelGenerator() {
					private static final long serialVersionUID = 7593430826693873496L;
					public String generateLabel(XYDataset dataset, int series) {
						XYSeries xys = ((XYSeriesCollection)dataset).getSeries(series);
						return xys.getDescription();
					}
        		}
        );

        NumberAxis xAxis = new NumberAxis(timeAxisName);
        xAxis.setAutoRangeIncludesZero(false);
        NumberAxis yAxis = new NumberAxis(dvdtAxisName);
        yAxis.setAutoRangeIncludesZero(false);
        
        XYSeriesCollection scatterDataset = new XYSeriesCollection(runData);
        XYSeriesCollection lineDataset = new XYSeriesCollection();

        trendData.setDescription("Trend");
        lineDataset.addSeries(trendData);
        
        XYPlot plot = chart.getXYPlot();
        plot.setRangePannable(true);
        plot.setDomainPannable(true);
        plot.setDomainGridlinePaint(Color.DARK_GRAY);
        plot.setRangeGridlinePaint(Color.DARK_GRAY);
        plot.setBackgroundPaint(new Color(224, 224, 224));

        plot.setDataset(0, lineDataset);
        plot.setRenderer(0, lineRenderer);
        plot.setDomainAxis(0, xAxis);
        plot.setRangeAxis(0, yAxis);
        plot.mapDatasetToDomainAxis(0, 0);
        plot.mapDatasetToRangeAxis(0, 0);

        plot.setDataset(1, scatterDataset);
        plot.setRenderer(1, dotRenderer);
        plot.mapDatasetToDomainAxis(1, 0);
        plot.mapDatasetToRangeAxis(1, 0);
        
        LegendTitle legend = new LegendTitle(plot.getRenderer()); 
        legend.setItemFont(new Font("Arial", 0, 10));
        legend.setPosition(RectangleEdge.TOP);
        chart.addLegend(legend);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE 3D CHART TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void create3dGraghTab() {
        JPanel plotPanel = new JPanel();
        add(plotPanel, "<html><div style='text-align: center;'>3<br>D<br><br>C<br>h<br>a<br>r<br>t</div></html>");
        GridBagLayout gbl_plotPanel = new GridBagLayout();
        gbl_plotPanel.columnWidths = new int[] {0};
        gbl_plotPanel.rowHeights = new int[] {0};
        gbl_plotPanel.columnWeights = new double[]{1.0};
        gbl_plotPanel.rowWeights = new double[]{1.0};
        plotPanel.setLayout(gbl_plotPanel);
                
        plot3d = new Plot3DPanel("SOUTH") {
			private static final long serialVersionUID = 7914951068593204419L;
			public void addPlotToolBar(String location) {
				super.addPlotToolBar(location);
        		super.plotToolBar.remove(7);
        		super.plotToolBar.remove(5);
        		super.plotToolBar.remove(4);
        	}        	
        };
        plot3d.setAutoBounds();
        plot3d.setAutoscrolls(true);
        plot3d.setEditable(false);
        plot3d.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        plot3d.setForeground(Color.BLACK);
        plot3d.getAxis(0).setColor(Color.BLACK);
        plot3d.getAxis(1).setColor(Color.BLACK);
        plot3d.getAxis(2).setColor(Color.BLACK);
        plot3d.setAxisLabel(0, x3dAxisName);
        plot3d.setAxisLabel(1, y3dAxisName);
        plot3d.setAxisLabel(2, z3dAxisName);
        
        GridBagConstraints gbl_chartPanel = new GridBagConstraints();
        gbl_chartPanel.anchor = GridBagConstraints.CENTER;
        gbl_chartPanel.insets = new Insets(3, 3, 3, 3);
        gbl_chartPanel.fill = GridBagConstraints.BOTH;
        gbl_chartPanel.gridx = 0;
        gbl_chartPanel.gridy = 0;
        plotPanel.add(plot3d, gbl_chartPanel);
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE USAGE TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void createUsageTab() {
    	final Desktop desktop = Desktop.getDesktop(); 
        JTextPane usageTextArea = new JTextPane();
        usageTextArea.setMargin(new Insets(10, 10, 10, 10));
        usageTextArea.setContentType("text/html");
        usageTextArea.setText(usage());
        usageTextArea.setEditable(false);
        usageTextArea.setCaretPosition(0);
        usageTextArea.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (HyperlinkEvent.EventType.ACTIVATED == e.getEventType()) {
                    try {
                    	desktop.browse(new URI(e.getURL().toString()));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        JScrollPane textScrollPane = new JScrollPane(usageTextArea);
        textScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        textScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(textScrollPane, "<html><div style='text-align: center;'>U<br>s<br>a<br>g<br>e</div></html>");
    }
    
    private String usage() {
        ResourceBundle bundle;
        bundle = ResourceBundle.getBundle("com.vgi.mafscaling.load_comp");
        return bundle.getString("usage"); 
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // COMMOMN ACTIONS RELATED FUNCTIONS
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void clearMpTables() {
    	clearMpTable(mpTable);
    	clearRunMpTables();
    }
    
    private void clearRunMpTables() {
    	clearMpTable(newMpTable);
    	clearMpTable(mpCorrTable);
    	clearMpTable(mpCorrCountTable);
    	savedNewMpTable.clear();
    	compareTableCheckBox.setSelected(false);
        clear2dChartData();
        plot3d.removeAllPlots();
    }
    
    private void clearMpTable(JTable table) {
        if (table.getName().equals(MpTableName))
        	table.setModel(new DefaultTableModel(newMpTable.getRowCount(), newMpTable.getColumnCount()));
        else
        	table.setModel(new DefaultTableModel(mpTable.getRowCount(), mpTable.getColumnCount()));
        Utils.initializeTable(table, ColumnWidth);
        formatMpTable(table);
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
        String logThrottleAngleColName = Config.getThrottleAngleColumnName();
        String logAfLearningColName = Config.getAfLearningColumnName();
        String logAfCorrectionColName = Config.getAfCorrectionColumnName();
        String logAfrColName = Config.getAfrColumnName();
        String logRpmColName = Config.getRpmColumnName();
        String logTimeColName = Config.getTimeColumnName();
        String logMafvColName = Config.getMafVoltageColumnName();
        String logIatColName = Config.getIatColumnName();
        String logMpColName = Config.getMpColumnName();
        String logCruiseStatusColName = Config.getCruiseStatusColumnName();
        logThrottleAngleColIdx = columns.indexOf(logThrottleAngleColName);
        logAfLearningColIdx = columns.indexOf(logAfLearningColName);
        logAfCorrectionColIdx = columns.indexOf(logAfCorrectionColName);
        logAfrColIdx = columns.indexOf(logAfrColName);
        logRpmColIdx = columns.indexOf(logRpmColName);
        logTimeColIdx = columns.indexOf(logTimeColName);
        logMafvColIdx = columns.indexOf(logMafvColName);
        logIatColIdx = columns.indexOf(logIatColName);
        logMpColIdx = columns.indexOf(logMpColName);
        logCruiseStatusColIdx = columns.indexOf(logCruiseStatusColName);
        if (logThrottleAngleColIdx == -1){ Config.setThrottleAngleColumnName(Config.NO_NAME);ret = false; }
        if (logAfLearningColIdx == -1)   { Config.setAfLearningColumnName(Config.NO_NAME);   ret = false; }
        if (logAfCorrectionColIdx == -1) { Config.setAfCorrectionColumnName(Config.NO_NAME); ret = false; }
        if (logAfrColIdx == -1)          { Config.setAfrColumnName(Config.NO_NAME);          ret = false; }
        if (logRpmColIdx == -1)          { Config.setRpmColumnName(Config.NO_NAME);          ret = false; }
        if (logTimeColIdx == -1)         { Config.setTimeColumnName(Config.NO_NAME);         ret = false; }
        if (logMafvColIdx == -1)         { Config.setMafVoltageColumnName(Config.NO_NAME);   ret = false; }
        if (logIatColIdx == -1)          { Config.setIatColumnName(Config.NO_NAME);          ret = false; }
        if (logMpColIdx == -1)           { Config.setMpColumnName(Config.NO_NAME);           ret = false; }
        if (logCruiseStatusColIdx == -1) { Config.setCommandedAfrColumnName(Config.NO_NAME); }
        isLoadCompInRatio = Config.getIsLoadCompInRatio();
        thrtlMaxChange = Config.getThrottleChangeMaxValue();
        minCellHitCount = Config.getLCMinCellHitCount();
        afrMin = Config.getLCAfrMinimumValue();
        afrMax = Config.getLCAfrMaximumValue();
        rpmMax = Config.getRPMMaximumValue();
        rpmMin = Config.getRPMMinimumValue();
        dvDtMax = Config.getLCDvDtMaximumValue();
        iatMax = Config.getLCIatMaximumValue();
        cruiseValue = Config.getCruiseStatusValue();
        corrApplied = Config.getLCCorrectionAppliedValue();
        return ret;
    }
    
    private void loadLogFile() {
        if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(this))
            return;
        File[] files = fileChooser.getSelectedFiles();
        for (File file : files) {
	        BufferedReader br = null;
	        try {
	            br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
	            String line = br.readLine();
	            if (line != null) {
	                String [] elements = line.split(",", -1);
	                getColumnsFilters(elements);
	
	                boolean resetColumns = false;
	                if (logThrottleAngleColIdx >= 0 || logAfLearningColIdx >= 0 || logAfCorrectionColIdx >= 0 || logAfrColIdx >= 0 ||
	                	logRpmColIdx >= 0 || logTimeColIdx >=0 || logMafvColIdx >= 0 || logIatColIdx >= 0 || logMpColIdx >= 0) {
	                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Would you like to reset column names or filter values?", "Columns/Filters Reset", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE))
	                    	resetColumns = true;
	                }
	
	                if (resetColumns || logThrottleAngleColIdx < 0 || logAfLearningColIdx < 0 || logAfCorrectionColIdx < 0 || logAfrColIdx < 0 ||
	                	logRpmColIdx < 0 || logTimeColIdx < 0 || logMafvColIdx < 0 || logIatColIdx < 0 || logMpColIdx < 0) {
	                	ColumnsFiltersSelection selectionWindow = new LCColumnsFiltersSelection(false);
	                	if (!selectionWindow.getUserSettings(elements) || !getColumnsFilters(elements))
	                		return;
	                }
	                
	                String[] flds;
	                line = br.readLine();
	                boolean removed = false;
	                int i = 2;
	                int row = getLogTableEmptyRow();
	                double cruise = -1;
	                double thrtlMaxChange2 = thrtlMaxChange * 2.0;
	                double throttle = 0;
	                double pThrottle = 0;
	                double ppThrottle = 0;
	                double trims = 0;
	                double stft = 0;
	                double ltft = 0;
	                double afr = 0;
	                double dVdt = 0;
	                double prevTime = 0;
	                double time = 0;
	                double timeMultiplier = 1.0;
	                double pmafv = 0;
	                double mafv = 0;
	                double iat;
	                double rpm;
	                clearChartData();
	                setCursor(new Cursor(Cursor.WAIT_CURSOR));
	                try {
	                	if (-1 == logCruiseStatusColIdx)
	                		cruiseValue = -1;
		                while (line != null) {
		                    flds = line.split(",", -1);
		                    ppThrottle = pThrottle;
		                    pThrottle = throttle;
		                    try {
		                    	throttle = Double.valueOf(flds[logThrottleAngleColIdx]);
	                        	// Calculate dV/dt
                            	prevTime = time;
                            	time = Double.valueOf(flds[logTimeColIdx]);
                            	if (timeMultiplier == 1.0 && (int)time - time < 0) {
                            		timeMultiplier = 1000.0;
                            		prevTime *= timeMultiplier;
                            	}
                        		time *= timeMultiplier;
                            	pmafv = mafv;
                            	mafv = Double.valueOf(flds[logMafvColIdx]);
                            	if ((time - prevTime) == 0.0)
                            		dVdt = 100.0;
                            	else
                            		dVdt = Math.abs(((mafv - pmafv) / (time - prevTime)) * 1000.0);
		                    	if (row > 1 && Math.abs(pThrottle - throttle) > thrtlMaxChange) {
		                    		if (!removed)
		                    			Utils.removeRow(row--, logDataTable);
		                    		removed = true;
		                    	}
		                    	else if (row <= 2 || Math.abs(ppThrottle - throttle) <= thrtlMaxChange2) {
		                            // Filters
		                        	afr = Double.valueOf(flds[logAfrColIdx]);
	                                rpm = Double.valueOf(flds[logRpmColIdx]);
		                        	iat = Double.valueOf(flds[logIatColIdx]);
		                        	stft = Double.valueOf(flds[logAfCorrectionColIdx]);
		                        	ltft = Double.valueOf(flds[logAfLearningColIdx]);
		                        	trims = stft + ltft;
		                        	if (cruiseValue != -1)
		                        		cruise = Double.valueOf(flds[logCruiseStatusColIdx]);
		                        	if (afrMin <= afr && afr <= afrMax && rpmMin <= rpm && rpm <= rpmMax && dVdt <= dvDtMax && iat <= iatMax && cruise == cruiseValue) {
			                    		removed = false;
		                                Utils.ensureRowCount(row + 1, logDataTable);
		                                logDataTable.setValueAt(time, row, 0);
		                                logDataTable.setValueAt(rpm, row, 1);
		                                logDataTable.setValueAt(iat, row, 2);
		                                logDataTable.setValueAt(stft, row, 3);
		                                logDataTable.setValueAt(ltft, row, 4);
		                                logDataTable.setValueAt(Double.valueOf(flds[logMpColIdx]), row, 5);
		                                logDataTable.setValueAt(mafv, row, 6);
		                                logDataTable.setValueAt(trims, row, 7);
		                                logDataTable.setValueAt(dVdt, row, 8);
		                                trimArray.add(trims);
		                                rpmArray.add(rpm);
		                                mafvArray.add(mafv);
		                                timeArray.add(time);
		                                iatArray.add(iat);
		                                dvdtArray.add(dVdt);
		                                row += 1;
		                        	}
		                        	else
			                    		removed = true;
		                    	}
		                    	else
		                    		removed = true;
		                    }
		                    catch (NumberFormatException e) {
		                        logger.error(e);
		                        JOptionPane.showMessageDialog(null, "Error parsing number at " + file.getName() + " line " + i + ": " + e, "Error processing file", JOptionPane.ERROR_MESSAGE);
		                        return;
		                    }
		                    line = br.readLine();
		                    i += 1;
		                }
		    	        JRadioButton button = (JRadioButton) rbGroup.getElements().nextElement();
		    	        button.setSelected(true);
		    	        plotDvdtData();
	                }
	                finally {
	                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	                }
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
    }
    
    private boolean validateTable(JTable table) {
        if (table == null)
            return false;
        // check if table is empty
        if (Utils.isTableEmpty(table))
        	return true;
        // check paste format
        if (!table.getValueAt(0, 0).toString().equalsIgnoreCase("[table3d]") &&
            !((table.getValueAt(0, 0).toString().equals("")) &&
              Pattern.matches(Utils.fpRegex, table.getValueAt(0, 1).toString()) &&
              Pattern.matches(Utils.fpRegex, table.getValueAt(1, 0).toString()))) {
            JOptionPane.showMessageDialog(null, "Pasted data doesn't seem to be a valid table with row/column headers.\n\nPlease post 'Engine Load Compensation' table into first cell", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (table.getValueAt(0, 0).toString().equalsIgnoreCase("[table3d]")) {
            // realign if paste is from RomRaider
            if (table.getValueAt(0, 1).toString().equals("")) {
                Utils.removeRow(0, table);
                for (int i = table.getColumnCount() - 2; i >= 0; --i)
                	table.setValueAt(table.getValueAt(0, i), 0, i + 1);
                table.setValueAt("", 0, 0);
            }
            // paste is probably from excel, just blank out the first cell
            else
            	table.setValueAt("", 0, 0);
        }
        // remove extra rows
        for (int i = table.getRowCount() - 1; i >= 0 && table.getValueAt(i, 0).toString().equals(""); --i)
            Utils.removeRow(i, table);
        // remove extra columns
        for (int i = table.getColumnCount() - 1; i >= 0 && table.getValueAt(0, i).toString().equals(""); --i)
            Utils.removeColumn(i, table);
        // validate all cells are numeric
        String val;
        for (int i = 0; i < table.getRowCount(); ++i) {
            for (int j = 0; j < table.getColumnCount(); ++j) {
                if (i == 0 && j == 0)
                    continue;
                val = table.getValueAt(i, j).toString();
                if (val.equals(""))
                	table.setValueAt("0", i, j);
                else if (!Pattern.matches(Utils.fpRegex, val)) {
                    JOptionPane.showMessageDialog(null, "Invalid value at row " + (i + 1) + " column " + (j + 1), "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        Utils.colorTable(table);
        return true;
    }
    
    private boolean getAxisData() {
    	try {
    		if (Utils.isTableEmpty(mpTable)) {
                JOptionPane.showMessageDialog(null, "PLease paste current Engine Load Compensation table into top grid", "Error getting Engine Load Compensation table headers", JOptionPane.ERROR_MESSAGE);
                return false;
    		}
    		xAxisArray = new ArrayList<Double>();
    		yAxisArray = new ArrayList<Double>();
	        for (int i = 1; i < mpTable.getColumnCount(); ++i)
	        	xAxisArray.add(Double.valueOf(mpTable.getValueAt(0, i).toString()));
	        for (int i = 1; i < mpTable.getRowCount(); ++i)
	        	yAxisArray.add(Double.valueOf(mpTable.getValueAt(i, 0).toString()));
	        if (xAxisArray.size() > 0 && yAxisArray.size() > 0)
	        	return true;
    	}
    	catch (Exception e) {
            logger.error(e);
            JOptionPane.showMessageDialog(null, "Invalid value in Engine Load Compensation table: " + e, "Error getting Engine Load Compensation table headers", JOptionPane.ERROR_MESSAGE);
    	}
    	return false;
    }

    private boolean processLog() {
        try {
            double x, y, val;
            int cnt;
            String xStr, yStr, valStr;
            xData = new HashMap<Double, HashMap<Double, ArrayList<Double>>>();
            HashMap<Double, ArrayList<Double>> yData;
            ArrayList<Double> data;
            for (int i = 0; i < logDataTable.getRowCount(); ++i) {
            	xStr = logDataTable.getValueAt(i, 5).toString();
            	yStr = logDataTable.getValueAt(i, 1).toString();
            	valStr = logDataTable.getValueAt(i, 7).toString();
                if (!Pattern.matches(Utils.fpRegex, xStr)) {
                    JOptionPane.showMessageDialog(null, "Invalid value for MP, row " + i + 1, "Invalid value", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (!Pattern.matches(Utils.fpRegex, yStr)) {
                    JOptionPane.showMessageDialog(null, "Invalid value for RPM, row " + i, "Invalid value", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (!Pattern.matches(Utils.fpRegex, valStr)) {
                    JOptionPane.showMessageDialog(null, "Invalid value for Trims, row " + i, "Invalid value", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                x = xAxisArray.get(Utils.closestValueIndex(Double.valueOf(xStr), xAxisArray));
                y = yAxisArray.get(Utils.closestValueIndex(Double.valueOf(yStr), yAxisArray));
                val = Double.valueOf(valStr);

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
                data.add(val);
            }

            Color[][] colorMatrix = new Color[mpCorrTable.getRowCount()][mpCorrTable.getColumnCount()];
	        for (int i = 1; i < xAxisArray.size() + 1; ++i) {
	        	newMpTable.setValueAt(mpTable.getValueAt(0, i), 0, i);
	        	mpCorrTable.setValueAt(mpTable.getValueAt(0, i), 0, i);
	        	mpCorrCountTable.setValueAt(mpTable.getValueAt(0, i), 0, i);
	        	for (int j = 1; j < yAxisArray.size() + 1; ++j) {
	        		if (i == 1) {
	        			newMpTable.setValueAt(mpTable.getValueAt(j, 0), j, 0);
	        			mpCorrTable.setValueAt(mpTable.getValueAt(j, 0), j, 0);
	        			mpCorrCountTable.setValueAt(mpTable.getValueAt(j, 0), j, 0);
	        		}
        			x = xAxisArray.get(i - 1);
        			y = yAxisArray.get(j - 1);
                    yData = xData.get(x);
                    if (yData == null)
                    	newMpTable.setValueAt(mpTable.getValueAt(j, i), j, i);
                    else {
                    	data = yData.get(y);
	                    if (data == null)
	                    	newMpTable.setValueAt(mpTable.getValueAt(j, i), j, i);
	                    else {
	    	        		cnt = data.size();
    		        		val = (Utils.mean(data) + Utils.mode(data)) / 2.0;
	    	        		mpCorrTable.setValueAt(val, j, i);
	    	        		mpCorrCountTable.setValueAt(cnt, j, i);
	    	        		if (cnt > minCellHitCount) {
	    		        		val = val / 100.0 * corrApplied;
	    		        		val = 1 + val / 100.0;
	    		        		if (isLoadCompInRatio)
	    		        			newMpTable.setValueAt(val * Double.valueOf(mpTable.getValueAt(j, i).toString()), j, i);
	    		        		else
	    		        			newMpTable.setValueAt(val * (100.0 + Double.valueOf(mpTable.getValueAt(j, i).toString())) - 100.0, j, i);
	    		            	colorMatrix[j][i] = Color.PINK;
	    	        		}
	    	        		else
	    	        			newMpTable.setValueAt(mpTable.getValueAt(j, i), j, i);
	                    }
                    }
	        	}
	        }
	        Utils.colorTable(newMpTable);
	        
            for (int i = 0; i < colorMatrix.length; ++i)
                colorMatrix[i][0] = Color.LIGHT_GRAY;
            for (int i = 0; i < colorMatrix[0].length; ++i)
                colorMatrix[0][i] = Color.LIGHT_GRAY;
            ((BgColorFormatRenderer)mpCorrTable.getDefaultRenderer(Object.class)).setColors(colorMatrix);
            ((BgColorFormatRenderer)mpCorrCountTable.getDefaultRenderer(Object.class)).setColors(colorMatrix);

	        plot3dCorrection();
            
	        return true;
        }
        catch (Exception e) {
            logger.error(e);
            JOptionPane.showMessageDialog(null, e, "Error processing data", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    
    private void clearChartData() {
        trimArray.clear();
        rpmArray.clear();
        mafvArray.clear();
        timeArray.clear();
        iatArray.clear();
        dvdtArray.clear();
        runData.clear();
        trendData.clear();
        clear2dChartData();
        plot3d.removeAllPlots();
    }
    
    private void calculateLoad() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
	    	clearRunMpTables();
            if (!getAxisData() || !processLog())
                return;
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
    
    private boolean plotDvdtData() {
    	clear2dChartData();
        if (setXYSeries(runData, timeArray, dvdtArray)) {
	        double paddingX = runData.getMaxX() * 0.05;
	        double paddingY = runData.getMaxY() * 0.05;
	        XYPlot plot = chartPanel.getChart().getXYPlot();
	    	plot.getDomainAxis(0).setRange(runData.getMinX() - paddingX, runData.getMaxX() + paddingX);
	    	plot.getRangeAxis(0).setRange(runData.getMinY() - paddingY, runData.getMaxY() + paddingY);
	    	plot.getDomainAxis(0).setLabel(timeAxisName);
	    	plot.getRangeAxis(0).setLabel(dvdtAxisName);
	        plot.getRenderer(0).setSeriesVisible(0, false);
	        return true;
        }
        return false;
    }

    private boolean plotIatData() {
    	clear2dChartData();
    	if (setXYSeries(runData, timeArray, iatArray)) {
	    	double paddingX = runData.getMaxX() * 0.05;
	    	double paddingY = runData.getMaxY() * 0.05;
	        XYPlot plot = chartPanel.getChart().getXYPlot();
	    	plot.getDomainAxis(0).setRange(runData.getMinX() - paddingX, runData.getMaxX() + paddingX);
	    	plot.getRangeAxis(0).setRange(runData.getMinY() - paddingY, runData.getMaxY() + paddingY);
	    	plot.getDomainAxis(0).setLabel(timeAxisName);
	    	plot.getRangeAxis(0).setLabel(iatAxisName);
	        plot.getRenderer(0).setSeriesVisible(0, false);
	        return true;
    	}
        return false;
    }

    private boolean plotTrimRpmData() {
    	clear2dChartData();
        if (setXYSeries(runData, rpmArray, trimArray)) {
	        double[] ols = Regression.getOLSRegression(chartPanel.getChart().getXYPlot().getDataset(1), 0);
	        Function2D curve = new LineFunction2D(ols[0], ols[1]);
	        trendData.clear();
	        trendData.add(runData.getMinX(), curve.getValue(runData.getMinX()));
	        trendData.add(runData.getMaxX(), curve.getValue(runData.getMaxX()));
	        double paddingX = runData.getMaxX() * 0.05;
	        double paddingY = runData.getMaxY() * 0.05;
	        XYPlot plot = chartPanel.getChart().getXYPlot();
        	plot.getDomainAxis(0).setRange(runData.getMinX() - paddingX, runData.getMaxX() + paddingX);
	    	plot.getRangeAxis(0).setRange(runData.getMinY() - paddingY, runData.getMaxY() + paddingY);
	    	plot.getDomainAxis(0).setLabel(rpmAxisName);
	    	plot.getRangeAxis(0).setLabel(trpmAxisName);
	        plot.getRenderer(0).setSeriesVisible(0, true);
	        return true;
        }
        return false;
    }

    private boolean plotTrimMafvData() {
    	clear2dChartData();
        if (setXYSeries(runData, mafvArray, trimArray)) {
	        double[] ols = Regression.getOLSRegression(chartPanel.getChart().getXYPlot().getDataset(1), 0);
	        Function2D curve = new LineFunction2D(ols[0], ols[1]);
	        trendData.clear();
	        trendData.add(runData.getMinX(), curve.getValue(runData.getMinX()));
	        trendData.add(runData.getMaxX(), curve.getValue(runData.getMaxX()));
	        double paddingX = runData.getMaxX() * 0.05;
	        double paddingY = runData.getMaxY() * 0.05;
	        XYPlot plot = chartPanel.getChart().getXYPlot();
        	plot.getDomainAxis(0).setRange(runData.getMinX() - paddingX, runData.getMaxX() + paddingX);
	    	plot.getRangeAxis(0).setRange(runData.getMinY() - paddingY, runData.getMaxY() + paddingY);
	    	plot.getDomainAxis(0).setLabel(mafvAxisName);
	    	plot.getRangeAxis(0).setLabel(trpmAxisName);
	        plot.getRenderer(0).setSeriesVisible(0, true);
	        return true;
        }
        return false;
    }

    private boolean plotCorrectionData() {
    	XYSeries series;
        runData.clear();
        trendData.clear();
        clear2dChartData();
        XYPlot plot = chartPanel.getChart().getXYPlot();
    	XYSeriesCollection lineDataset = (XYSeriesCollection) plot.getDataset(0);
    	DecimalFormat df = new DecimalFormat(".00");
    	String val;
    	int i = 0;
    	int j = 0;
    	if (!Utils.isTableEmpty(mpCorrTable)) {
    		try {
		    	for (i = 1; i < mpCorrTable.getColumnCount(); ++i) {
		    		val = mpCorrTable.getValueAt(0, i).toString();
		    		series = new XYSeries(df.format(Double.valueOf(val)));
		    		for (j = 1; j < mpCorrTable.getRowCount(); ++j) { 
		    			if (mpCorrTable.getValueAt(j, i) != null) {
		    				val = mpCorrTable.getValueAt(j, i).toString();
		    				if (!val.isEmpty())
		    					series.add(Double.valueOf(mpCorrTable.getValueAt(j, 0).toString()), Double.valueOf(val));
		    			}
		    		}
		    		if (series.getItemCount() > 0) {
		    			corrData.add(series);
		    			series.setDescription(series.getKey().toString());
		    	        lineDataset.addSeries(series);
		    		}
		    	}
		    	plot.getDomainAxis(0).setAutoRange(true);
		    	plot.getRangeAxis(0).setAutoRange(true);
		    	plot.getDomainAxis(0).setLabel(rpmAxisName);
		    	plot.getRangeAxis(0).setLabel(trpmAxisName);
		        plot.getRenderer(0).setSeriesVisible(0, false);
            }
            catch (NumberFormatException e) {
                logger.error(e);
                JOptionPane.showMessageDialog(null, "Error parsing number from " + MpCorrTableName + " table, cell(" + i + " : " + j + "): " + e, "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
    	}
        return true;
    }
    
    private void plot3dCorrection() {
        plot3d.removeAllPlots();
    	if (!Utils.isTableEmpty(mpCorrTable)) {
    		try {
		        double[] x = new double[xAxisArray.size()];
		        int i = 0;
		        for (i = 0; i < xAxisArray.size(); ++i)
		        	x[i] = xAxisArray.get(i);
		        double[] y = new double[yAxisArray.size()];
		        for (i = 0; i < yAxisArray.size(); ++i)
		        	y[i] = yAxisArray.get(i);
		        double[][] z = Utils.doubleZArray(mpCorrTable, x, y);
		        Color[][] colors = Utils.generateTableColorMatrix(mpCorrTable, 1, 1);
		        plot3d.addGridPlot("Average Error % Plot", colors, x, y, z);
            }
            catch (Exception e) {
                logger.error(e);
                JOptionPane.showMessageDialog(null, "Error: " + e, "Error", JOptionPane.ERROR_MESSAGE);
            }
    	}
    }

    private void clear2dChartData() {
        XYPlot plot = chartPanel.getChart().getXYPlot();
    	XYSeriesCollection lineDataset = (XYSeriesCollection) plot.getDataset(0);
    	for (XYSeries series : corrData)
    		lineDataset.removeSeries(series);
    	corrData.clear();
    }

    private boolean setXYSeries(XYSeries series, ArrayList<Double> xarr, ArrayList<Double> yarr) {
        if (xarr.size() == 0 || xarr.size() != yarr.size())
            return false;
        series.clear();
        for (int i = 0; i < xarr.size(); ++i)
            series.add(xarr.get(i), yarr.get(i), false);
        series.fireSeriesChanged();
        return true;
    }
    
    private boolean setCompareTables() {
    	if (mpTable.getColumnCount() != newMpTable.getColumnCount() || mpTable.getRowCount() != newMpTable.getRowCount())
    		return false;
    	ArrayList<Double> row;
    	double v1, v2;
    	Utils.clearTableColors(newMpTable);
    	savedNewMpTable.clear();
    	for (int i = 1; i < mpTable.getRowCount(); ++i) {
    		row = new ArrayList<Double>();
    		savedNewMpTable.add(row);
    		for (int j = 1; j < mpTable.getColumnCount(); ++j) {
    			try {
    				v1 = Double.valueOf(mpTable.getValueAt(i, j).toString());
    				v2 = Double.valueOf(newMpTable.getValueAt(i, j).toString());
    				row.add(v2);
    				v2 = v2 - v1;
    				if (v2 == 0)
        				newMpTable.setValueAt("", i, j);
    				else
    					newMpTable.setValueAt(v2, i, j);
    			}
    			catch (Exception e) {
    				unsetCompareTables();
    				return false;
    			}
    		}
    	}
    	Utils.colorTable(newMpTable);
    	return true;
    }
    
    private void unsetCompareTables() {
    	Utils.clearTableColors(newMpTable);
    	ArrayList<Double> row;
    	for (int i = 0; i < savedNewMpTable.size(); ++i) {
    		row = savedNewMpTable.get(i);
    		for (int j = 0; j < row.size(); ++j)
    			newMpTable.setValueAt(row.get(j), i + 1, j + 1);
    	}
    	Utils.colorTable(newMpTable);
    	savedNewMpTable.clear();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("clearmp".equals(e.getActionCommand())) {
	    	clearMpTables();
        }
        else if ("clearlog".equals(e.getActionCommand())) {
            clearLogDataTables();
            clearRunMpTables();
        }
        else if ("clearall".equals(e.getActionCommand())) {
            clearLogDataTables();
	    	clearMpTables();
        }
        else if ("go".equals(e.getActionCommand())) {
        	calculateLoad();
        }
        else if ("loadlog".equals(e.getActionCommand())) {
            loadLogFile();
        }
        else if ("hidelogtable".equals(e.getActionCommand())) {
        	JCheckBox checkBox = (JCheckBox)e.getSource();
        	if (checkBox.isSelected())
        		dataScrollPane.setVisible(false);
        	else
        		dataScrollPane.setVisible(true);
        	fireStateChanged();
        }
        else if ("comparetables".equals(e.getActionCommand())) {
        	JCheckBox checkBox = (JCheckBox)e.getSource();
        	if (checkBox.isSelected()) {
        		if (!setCompareTables())
        			checkBox.setSelected(false);
        	}
        	else
        		unsetCompareTables();
        	fireStateChanged();
        }
        else if ("dvdt".equals(e.getActionCommand())) {
        	JRadioButton radioButton = (JRadioButton)e.getSource();
            if (radioButton.isSelected()) {
                if (!plotDvdtData())
                	radioButton.setSelected(false);
            }
            else
                runData.clear();
        }
        else if ("iat".equals(e.getActionCommand())) {
        	JRadioButton radioButton = (JRadioButton)e.getSource();
            if (radioButton.isSelected()) {
                if (!plotIatData())
                	radioButton.setSelected(false);
            }
            else
                runData.clear();
        }
        else if ("trpm".equals(e.getActionCommand())) {
        	JRadioButton radioButton = (JRadioButton)e.getSource();
            if (radioButton.isSelected()) {
                if (!plotTrimRpmData())
                	radioButton.setSelected(false);
            }
            else {
                runData.clear();
                trendData.clear();
            }
        }
        else if ("trmafv".equals(e.getActionCommand())) {
        	JRadioButton radioButton = (JRadioButton)e.getSource();
            if (radioButton.isSelected()) {
                if (!plotTrimMafvData())
                	radioButton.setSelected(false);
            }
            else {
                runData.clear();
                trendData.clear();
            }
        }
        else if ("corr".equals(e.getActionCommand())) {
        	JRadioButton radioButton = (JRadioButton)e.getSource();
            if (radioButton.isSelected()) {
                if (!plotCorrectionData())
                	radioButton.setSelected(false);
            }
            else
                clear2dChartData();
        }
    }
        
    public void onMovePoint(int itemIndex, double valueX, double valueY) {
    }
}
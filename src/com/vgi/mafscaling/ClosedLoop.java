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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYSeriesLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.function.Function2D;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;

public class ClosedLoop extends JTabbedPane implements ActionListener, IMafChartHolder {
    private static final long serialVersionUID = 2988105467764335997L;
    private static final Logger logger = Logger.getLogger(ClosedLoop.class);

    private static final String SaveDataFileHeader = "[closed_loop run data]";
    private static final String MafTableName = "Current MAF Scaling";
    private static final String Afr1TableName = "AFR Average";
    private static final String Afr2TableName = "AFR Cell Hit Count";
    private static final String XAxisName = "MAF Sensor (Voltage)";
    private static final String Y1AxisName = "Mass Airflow (g/s)";
    private static final String Y2AxisName = "Total Correction (%)";
    private static final String dvdtAxisName = "dV / dt";
    private static final String iatAxisName = "IAT";
    private static final String trpmAxisName = "Trims / Rpm";
    private static final String mnmdAxisName = "Mean / Mode";
    private static final String mnmd2AxisName = "Trims / MafV";
    private static final String timeAxisName = "Time";
    private static final String rpmAxisName = "RPM";
    private static final String totalCorrectionDataName = "Total Correction";
    private static final String currentDataName = "Current";
    private static final String correctedDataName = "Corrected";
    private static final String smoothedDataName = "Smoothed";
    private static final String mafCurveDataName = "Smoothed Maf Curve";
    private static final String currentSlopeDataName = "Current Maf Slope";
    private static final String smoothedSlopeDataName = "Smoothed Maf Slope";
    private static final int MinimumDataSampleCount = 30;
    private static final int ColumnWidth = 50;
    private static final int ColumnCount = 9;
    private static final int MafTableColumnCount = 50;
    private static final int AfrTableColumnCount = 15;
    private static final int AfrTableRowCount = 25;
    private static final int LogDataRowCount = 200;
    private int clValue = Config.getClOlStatusValue();
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

    private JTable mafTable = null;
    private JTable mafSmoothingTable = null;
    private JTable logDataTable = null;
    private JTable afr1Table = null;
    private JTable afr2Table = null;
    private MafChartPanel mafChartPanel = null;
    private JCheckBox checkBoxDvdtData = null;
    private JCheckBox checkBoxIatData = null;
    private JCheckBox checkBoxTrpmData = null;
    private JCheckBox checkBoxMnmdData = null;
    private JCheckBox checkBoxRunData = null;
    private JCheckBox checkBoxCurrentMaf = null;
    private JCheckBox checkBoxCorrectedMaf = null;
    private JCheckBox checkBoxSmoothedMaf = null;
    private JCheckBox checkBoxSmoothing = null;
    private JComboBox<String> smoothComboBox = null;
    private JButton btnCompareButton = null;
    private JButton btnSmoothButton = null;
    private JButton btnResetSmoothButton = null;
    private JButton btnPlusButton = null;
    private JButton btnMinusButton = null;
    private JLabel lblMafIncDec = null;
    private JFormattedTextField mafIncDecTextField = null;

    private ArrayList<Double> trimArray = new ArrayList<Double>();
    private ArrayList<Double> rpmArray = new ArrayList<Double>();
    private ArrayList<Double> timeArray = new ArrayList<Double>();
    private ArrayList<Double> iatArray = new ArrayList<Double>();
    private ArrayList<Double> mafvArray = new ArrayList<Double>();
    private ArrayList<Double> dvdtArray = new ArrayList<Double>();
    private ArrayList<Double> voltArray = new ArrayList<Double>();
    private ArrayList<Double> gsArray = new ArrayList<Double>();
    private ArrayList<Double> gsCorrected = new ArrayList<Double>();
    private ArrayList<Double> smoothGsArray = new ArrayList<Double>();
    private ArrayList<Double> correctionMeanArray = new ArrayList<Double>();
    private ArrayList<Double> correctionModeArray = new ArrayList<Double>();
    private final ExcelAdapter excelAdapter = new ExcelAdapter();
    private final JFileChooser fileChooser = new JFileChooser();
    private final XYSeries runData = new XYSeries(totalCorrectionDataName);
    private final XYSeries currMafData = new XYSeries(currentDataName);
    private final XYSeries corrMafData = new XYSeries(correctedDataName);
    private final XYSeries smoothMafData = new XYSeries(smoothedDataName);
    private PrimaryOpenLoopFuelingTable polfTable = null;
    private MafCompare mafCompare = null;

    public ClosedLoop(int tabPlacement, PrimaryOpenLoopFuelingTable table, MafCompare comparer) {
        super(tabPlacement);
    	polfTable = table;
    	mafCompare = comparer;
        initialize();
    }

    private void initialize() {
        createDataTab();
        createGraghTab();
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
        gbl_dataPanel.rowHeights = new int[] {0, 0, 0, 0};
        gbl_dataPanel.columnWeights = new double[]{0.0, 0.0};
        gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0};
        dataPanel.setLayout(gbl_dataPanel);

        JPanel cntlPanel = new JPanel();
        GridBagConstraints gbl_ctrlPanel = new GridBagConstraints();
        gbl_ctrlPanel.insets = new Insets(3, 3, 3, 3);
        gbl_ctrlPanel.anchor = GridBagConstraints.NORTH;
        gbl_ctrlPanel.weightx = 1.0;
        gbl_ctrlPanel.fill = GridBagConstraints.HORIZONTAL;
        gbl_ctrlPanel.gridx = 0;
        gbl_ctrlPanel.gridy = 0;
        gbl_ctrlPanel.gridwidth = 2;
        dataPanel.add(cntlPanel, gbl_ctrlPanel);
        
        GridBagLayout gbl_cntlPanel = new GridBagLayout();
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0, 0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);
        
        JButton btnMafClearButton = new JButton("Clear Maf Data");
        GridBagConstraints gbc_btnMafClearButton = new GridBagConstraints();
        gbc_btnMafClearButton.insets = new Insets(0, 0, 5, 5);
        gbc_btnMafClearButton.gridx = 0;
        gbc_btnMafClearButton.gridy = 0;
        btnMafClearButton.setActionCommand("clearmaf");
        btnMafClearButton.addActionListener(this);
        cntlPanel.add(btnMafClearButton, gbc_btnMafClearButton);
        
        JButton btnRunClearButton = new JButton("Clear Run Data");
        GridBagConstraints gbc_btnRunClearButton = new GridBagConstraints();
        gbc_btnRunClearButton.insets = new Insets(0, 0, 5, 5);
        gbc_btnRunClearButton.gridx = 1;
        gbc_btnRunClearButton.gridy = 0;
        btnRunClearButton.setActionCommand("clearlog");
        btnRunClearButton.addActionListener(this);
        cntlPanel.add(btnRunClearButton, gbc_btnRunClearButton);
        
        JButton btnAllClearButton = new JButton("Clear All");
        GridBagConstraints gbc_btnAllClearButton = new GridBagConstraints();
        gbc_btnAllClearButton.insets = new Insets(0, 0, 5, 5);
        gbc_btnAllClearButton.gridx = 2;
        gbc_btnAllClearButton.gridy = 0;
        btnAllClearButton.setActionCommand("clearall");
        btnAllClearButton.addActionListener(this);
        cntlPanel.add(btnAllClearButton, gbc_btnAllClearButton);
        
        JButton btnLoadDataButton = new JButton("Load");
        GridBagConstraints gbc_btnLoadDataButton = new GridBagConstraints();
        gbc_btnLoadDataButton.insets = new Insets(0, 0, 5, 5);
        gbc_btnLoadDataButton.gridx = 3;
        gbc_btnLoadDataButton.gridy = 0;
        btnLoadDataButton.setActionCommand("load");
        btnLoadDataButton.addActionListener(this);
        cntlPanel.add(btnLoadDataButton, gbc_btnLoadDataButton);
        
        JButton btnSaveDataButton = new JButton("Save");
        GridBagConstraints gbc_btnSaveDataButton = new GridBagConstraints();
        gbc_btnSaveDataButton.insets = new Insets(0, 0, 5, 5);
        gbc_btnSaveDataButton.gridx = 4;
        gbc_btnSaveDataButton.gridy = 0;
        btnSaveDataButton.setActionCommand("save");
        btnSaveDataButton.addActionListener(this);
        cntlPanel.add(btnSaveDataButton, gbc_btnSaveDataButton);
        
        Component horizontalGlue = Box.createHorizontalGlue();
        GridBagConstraints gbc_horizontalGlue = new GridBagConstraints();
        gbc_horizontalGlue.weightx = 1.0;
        gbc_horizontalGlue.fill = GridBagConstraints.HORIZONTAL;
        gbc_horizontalGlue.insets = new Insets(0, 0, 5, 5);
        gbc_horizontalGlue.gridx = 5;
        gbc_horizontalGlue.gridy = 0;
        cntlPanel.add(horizontalGlue, gbc_horizontalGlue);
        
        JButton btnGoButton = new JButton("GO");
        GridBagConstraints gbc_btnGoButton = new GridBagConstraints();
        gbc_btnGoButton.anchor = GridBagConstraints.EAST;
        gbc_btnGoButton.insets = new Insets(0, 0, 5, 0);
        gbc_btnGoButton.gridx = 6;
        gbc_btnGoButton.gridy = 0;
        btnGoButton.setActionCommand("go");
        btnGoButton.addActionListener(this);
        cntlPanel.add(btnGoButton, gbc_btnGoButton);
        
        JScrollPane mafScrollPane = new JScrollPane();
        mafScrollPane.setViewportBorder(new TitledBorder(null, MafTableName, TitledBorder.LEADING, TitledBorder.TOP, null, null));
        mafScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        mafScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        GridBagConstraints gbc_mafScrollPane = new GridBagConstraints();
        gbc_mafScrollPane.ipady = 28;
        gbc_mafScrollPane.anchor = GridBagConstraints.PAGE_START;
        gbc_mafScrollPane.weightx = 1.0;
        gbc_mafScrollPane.insets = new Insets(0, 0, 0, 0);
        gbc_mafScrollPane.fill = GridBagConstraints.HORIZONTAL;
        gbc_mafScrollPane.gridx = 0;
        gbc_mafScrollPane.gridy = 1;
        gbc_mafScrollPane.gridwidth = 2;
        dataPanel.add(mafScrollPane, gbc_mafScrollPane);
        
        JPanel dataMafPanel = new JPanel();
        mafScrollPane.setViewportView(dataMafPanel);
        GridBagLayout gbl_dataMafPanel = new GridBagLayout();
        gbl_dataMafPanel.columnWidths = new int[]{0, 0, 0};
        gbl_dataMafPanel.rowHeights = new int[] {0, 0};
        gbl_dataMafPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gbl_dataMafPanel.rowWeights = new double[]{0.0, 0.0};
        dataMafPanel.setLayout(gbl_dataMafPanel);
        
        JLabel lblVoltage = new JLabel("volt");
        GridBagConstraints gbc_lblVoltage = new GridBagConstraints();
        gbc_lblVoltage.anchor = GridBagConstraints.PAGE_START;
        gbc_lblVoltage.insets = new Insets(1, 1, 1, 5);
        gbc_lblVoltage.weightx = 0;
        gbc_lblVoltage.weighty = 0;
        gbc_lblVoltage.gridx = 0;
        gbc_lblVoltage.gridy = 0;
        dataMafPanel.add(lblVoltage, gbc_lblVoltage);
        
        JLabel lblGS = new JLabel(" g/s");
        GridBagConstraints gbc_lblGS = new GridBagConstraints();
        gbc_lblGS.anchor = GridBagConstraints.PAGE_START;
        gbc_lblGS.insets = new Insets(1, 1, 1, 5);
        gbc_lblGS.weightx = 0;
        gbc_lblGS.weighty = 0;
        gbc_lblGS.gridx = 0;
        gbc_lblGS.gridy = 1;
        dataMafPanel.add(lblGS, gbc_lblGS);

        mafTable = new JTable();
        mafTable.setColumnSelectionAllowed(true);
        mafTable.setCellSelectionEnabled(true);
        mafTable.setBorder(new LineBorder(new Color(0, 0, 0)));
        mafTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        mafTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        mafTable.setModel(new DefaultTableModel(2, MafTableColumnCount));
        mafTable.setTableHeader(null);
        Utils.initializeTable(mafTable, ColumnWidth);
        GridBagConstraints gbc_mafTable = new GridBagConstraints();
        gbc_mafTable.insets = new Insets(0, 0, 0, 0);
        gbc_mafTable.fill = GridBagConstraints.BOTH;
        gbc_mafTable.weightx = 1.0;
        gbc_mafTable.weighty = 1.0;
        gbc_mafTable.gridx = 1;
        gbc_mafTable.gridy = 0;
        gbc_mafTable.gridheight = 2;
        dataMafPanel.add(mafTable, gbc_mafTable);
        excelAdapter.addTable(mafTable, false, false, false, false, true, false, true, false, true);

        JPanel dataRunButtonPanel = new JPanel();
        dataRunButtonPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        GridBagConstraints gbc_dataRunButtonPanel = new GridBagConstraints();
        gbc_dataRunButtonPanel.anchor = GridBagConstraints.PAGE_START;
        gbc_dataRunButtonPanel.weightx = 1.0;
        gbc_dataRunButtonPanel.insets = new Insets(0, 0, 0, 0);
        gbc_dataRunButtonPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_dataRunButtonPanel.gridx = 0;
        gbc_dataRunButtonPanel.gridy = 2;
        gbc_dataRunButtonPanel.gridwidth = 2;
        dataPanel.add(dataRunButtonPanel, gbc_dataRunButtonPanel);
        
        JButton btnOpFuelingButton = new JButton("POL Fueling");
        btnOpFuelingButton.setActionCommand("fueling");
        btnOpFuelingButton.addActionListener(this);
        dataRunButtonPanel.add(btnOpFuelingButton);
        
        JButton btnLoadLogButton = new JButton("Load Log");
        btnLoadLogButton.setActionCommand("loadlog");
        btnLoadLogButton.addActionListener(this);
        dataRunButtonPanel.add(btnLoadLogButton);

        JScrollPane dataScrollPane = new JScrollPane();
        GridBagConstraints gbc_dataScrollPane = new GridBagConstraints();
        gbc_dataScrollPane.weightx = 0;
        gbc_dataScrollPane.weighty = 1.0;
        gbc_dataScrollPane.fill = GridBagConstraints.BOTH;
        gbc_dataScrollPane.ipadx = ColumnWidth * ColumnCount;
        gbc_dataScrollPane.gridx = 0;
        gbc_dataScrollPane.gridy = 3;
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
        gbc_aprScrollPane.gridy = 3;
        dataPanel.add(aprScrollPane, gbc_aprScrollPane);

        JPanel aprRunPanel = new JPanel();
        aprScrollPane.setViewportView(aprRunPanel);
        GridBagLayout gbl_aprRunPanel = new GridBagLayout();
        gbl_aprRunPanel.columnWidths = new int[]{0, 0};
        gbl_aprRunPanel.rowHeights = new int[] {0, 0, 0, 0, 0};
        gbl_aprRunPanel.columnWeights = new double[]{0.0, 1.0};
        gbl_aprRunPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0};
        aprRunPanel.setLayout(gbl_aprRunPanel);

        createLogDataTable(dataRunPanel);
        createAfrDataTables(aprRunPanel);
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
            logDataTable.getColumnModel().getColumn(1).setHeaderValue("Load");
            logDataTable.getColumnModel().getColumn(2).setHeaderValue("RPM");
            logDataTable.getColumnModel().getColumn(3).setHeaderValue("MafV");
            logDataTable.getColumnModel().getColumn(4).setHeaderValue("AFR");
            logDataTable.getColumnModel().getColumn(5).setHeaderValue("STFT");
            logDataTable.getColumnModel().getColumn(6).setHeaderValue("LTFT");
            logDataTable.getColumnModel().getColumn(7).setHeaderValue("dV/dt");
            logDataTable.getColumnModel().getColumn(8).setHeaderValue("IAT");
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

            excelAdapter.addTable(logDataTable, true, false);
    }
    
    private void createAfrDataTables(JPanel panel) {
        afr1Table = createAfrDataTable(panel, Afr1TableName, 0);
        afr2Table = createAfrDataTable(panel, Afr2TableName, 2);
    }
    
    private JTable createAfrDataTable(JPanel panel, String tableName, int gridy) {
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
        gbc_lblAfrTableName.anchor = GridBagConstraints.PAGE_START;
        gbc_lblAfrTableName.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblAfrTableName.gridx = 0;
        gbc_lblAfrTableName.gridy = gridy;
        panel.add(lblAfrTableName, gbc_lblAfrTableName);
        
        final JTable afrTable = new JTable() {
            private static final long serialVersionUID = 6526901361175099297L;
            public boolean isCellEditable(int row, int column) { return false; };
        };
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
        afrTable.setTableHeader(null);
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
    
    private void createGraghTab() {
        JPanel plotPanel = new JPanel();
        add(plotPanel, "<html><div style='text-align: center;'>C<br>h<br>a<br>r<br>t</div></html>");

        GridBagLayout gbl_plotPanel = new GridBagLayout();
        gbl_plotPanel.columnWidths = new int[] {0};
        gbl_plotPanel.rowHeights = new int[] {0, 0, 0};
        gbl_plotPanel.columnWeights = new double[]{1.0};
        gbl_plotPanel.rowWeights = new double[]{0.0, 1.0, 0.0};
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
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0, 0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);
        
        checkBoxDvdtData = new JCheckBox("dV/dt");
        GridBagConstraints gbc_checkBoxDvdtData = new GridBagConstraints();
        gbc_checkBoxDvdtData.insets = new Insets(0, 0, 3, 3);
        gbc_checkBoxDvdtData.gridx = 0;
        gbc_checkBoxDvdtData.gridy = 0;
        checkBoxDvdtData.setActionCommand("dvdt");
        checkBoxDvdtData.addActionListener(this);
        cntlPanel.add(checkBoxDvdtData, gbc_checkBoxDvdtData);
        
        checkBoxIatData = new JCheckBox("IAT");
        GridBagConstraints gbc_checkBoxIatData = new GridBagConstraints();
        gbc_checkBoxIatData.insets = new Insets(0, 0, 3, 3);
        gbc_checkBoxIatData.gridx = 1;
        gbc_checkBoxIatData.gridy = 0;
        checkBoxIatData.setActionCommand("iat");
        checkBoxIatData.addActionListener(this);
        cntlPanel.add(checkBoxIatData, gbc_checkBoxIatData);
        
        checkBoxTrpmData = new JCheckBox("Trims/RPM");
        GridBagConstraints gbc_checkBoxTrpmData = new GridBagConstraints();
        gbc_checkBoxTrpmData.insets = new Insets(0, 0, 3, 3);
        gbc_checkBoxTrpmData.gridx = 2;
        gbc_checkBoxTrpmData.gridy = 0;
        checkBoxTrpmData.setActionCommand("trpm");
        checkBoxTrpmData.addActionListener(this);
        cntlPanel.add(checkBoxTrpmData, gbc_checkBoxTrpmData);
        
        checkBoxMnmdData = new JCheckBox("Mean/Mode");
        GridBagConstraints gbc_checkBoxMnmdData = new GridBagConstraints();
        gbc_checkBoxMnmdData.insets = new Insets(0, 0, 3, 3);
        gbc_checkBoxMnmdData.gridx = 3;
        gbc_checkBoxMnmdData.gridy = 0;
        checkBoxMnmdData.setActionCommand("mnmd");
        checkBoxMnmdData.addActionListener(this);
        cntlPanel.add(checkBoxMnmdData, gbc_checkBoxMnmdData);
        
        checkBoxRunData = new JCheckBox("Total Correction");
        GridBagConstraints gbc_checkBoxRunData = new GridBagConstraints();
        gbc_checkBoxRunData.insets = new Insets(0, 0, 3, 3);
        gbc_checkBoxRunData.gridx = 4;
        gbc_checkBoxRunData.gridy = 0;
        checkBoxRunData.setActionCommand("corrdata");
        checkBoxRunData.addActionListener(this);
        cntlPanel.add(checkBoxRunData, gbc_checkBoxRunData);
        
        checkBoxCurrentMaf = new JCheckBox("Current");
        GridBagConstraints gbc_checkBoxCurrentMaf = new GridBagConstraints();
        gbc_checkBoxCurrentMaf.insets = new Insets(0, 0, 3, 3);
        gbc_checkBoxCurrentMaf.gridx = 5;
        gbc_checkBoxCurrentMaf.gridy = 0;
        checkBoxCurrentMaf.setActionCommand("current");
        checkBoxCurrentMaf.addActionListener(this);
        cntlPanel.add(checkBoxCurrentMaf, gbc_checkBoxCurrentMaf);
        
        checkBoxCorrectedMaf = new JCheckBox("Corrected");
        GridBagConstraints gbc_checkBoxCorrectedMaf = new GridBagConstraints();
        gbc_checkBoxCorrectedMaf.insets = new Insets(0, 0, 3, 3);
        gbc_checkBoxCorrectedMaf.gridx = 6;
        gbc_checkBoxCorrectedMaf.gridy = 0;
        checkBoxCorrectedMaf.setActionCommand("corrected");
        checkBoxCorrectedMaf.addActionListener(this);
        cntlPanel.add(checkBoxCorrectedMaf, gbc_checkBoxCorrectedMaf);

        checkBoxSmoothedMaf = new JCheckBox("Smoothed");
        GridBagConstraints gbc_checkBoxSmoothedMaf = new GridBagConstraints();
        gbc_checkBoxSmoothedMaf.insets = new Insets(0, 0, 3, 3);
        gbc_checkBoxSmoothedMaf.gridx = 7;
        gbc_checkBoxSmoothedMaf.gridy = 0;
        checkBoxSmoothedMaf.setActionCommand("smoothed");
        checkBoxSmoothedMaf.addActionListener(this);
        cntlPanel.add(checkBoxSmoothedMaf, gbc_checkBoxSmoothedMaf);

        btnCompareButton = new JButton("Compare");
        GridBagConstraints gbc_btnCompareButton = new GridBagConstraints();
        gbc_btnCompareButton.anchor = GridBagConstraints.CENTER;
        gbc_btnCompareButton.insets = new Insets(0, 0, 3, 3);
        gbc_btnCompareButton.weightx = 1.0;
        gbc_btnCompareButton.gridx = 8;
        gbc_btnCompareButton.gridy = 0;
        btnCompareButton.setActionCommand("compare");
        btnCompareButton.addActionListener(this);
        cntlPanel.add(btnCompareButton, gbc_btnCompareButton);

        checkBoxSmoothing = new JCheckBox("Smoothing:");
        GridBagConstraints gbc_checkBoxSmoothing = new GridBagConstraints();
        gbc_checkBoxSmoothing.anchor = GridBagConstraints.CENTER;
        gbc_checkBoxSmoothing.insets = new Insets(0, 0, 3, 3);
        gbc_checkBoxSmoothing.gridx = 9;
        gbc_checkBoxSmoothing.gridy = 0;
        checkBoxSmoothing.setActionCommand("smoothing");
        checkBoxSmoothing.addActionListener(this);
        cntlPanel.add(checkBoxSmoothing, gbc_checkBoxSmoothing);
        
        String[] smoothingDegree = { " 3 ", " 5 ", " 7 " };
        smoothComboBox = new JComboBox<String>(smoothingDegree);
        smoothComboBox.setSelectedIndex(0);
        smoothComboBox.setEnabled(false);
        GridBagConstraints gbc_smoothComboBox = new GridBagConstraints();
        gbc_smoothComboBox.anchor = GridBagConstraints.CENTER;
        gbc_smoothComboBox.insets = new Insets(0, 0, 3, 3);
        gbc_smoothComboBox.gridx = 10;
        gbc_smoothComboBox.gridy = 0;
        cntlPanel.add(smoothComboBox, gbc_smoothComboBox);

        btnSmoothButton = new JButton("Apply");
        btnSmoothButton.setEnabled(false);
        GridBagConstraints gbc_btnSmoothButton = new GridBagConstraints();
        gbc_btnSmoothButton.anchor = GridBagConstraints.CENTER;
        gbc_btnSmoothButton.insets = new Insets(0, 0, 3, 3);
        gbc_btnSmoothButton.gridx = 11;
        gbc_btnSmoothButton.gridy = 0;
        btnSmoothButton.setActionCommand("smooth");
        btnSmoothButton.addActionListener(this);
        cntlPanel.add(btnSmoothButton, gbc_btnSmoothButton);

        btnResetSmoothButton = new JButton("Reset");
        GridBagConstraints gbc_btnResetSmoothButton = new GridBagConstraints();
        gbc_btnResetSmoothButton.anchor = GridBagConstraints.CENTER;
        gbc_btnResetSmoothButton.insets = new Insets(0, 0, 3, 3);
        gbc_btnResetSmoothButton.gridx = 12;
        gbc_btnResetSmoothButton.gridy = 0;
        btnResetSmoothButton.setActionCommand("smoothreset");
        btnResetSmoothButton.addActionListener(this);
        cntlPanel.add(btnResetSmoothButton, gbc_btnResetSmoothButton);
        
        JFreeChart chart = ChartFactory.createScatterPlot(null, null, null, null, PlotOrientation.VERTICAL, false, true, false);
        chart.setBorderVisible(true);
        mafChartPanel = new MafChartPanel(chart, this);
        
        GridBagConstraints gbl_chartPanel = new GridBagConstraints();
        gbl_chartPanel.anchor = GridBagConstraints.CENTER;
        gbl_chartPanel.insets = new Insets(3, 3, 3, 3);
        gbl_chartPanel.weightx = 1.0;
        gbl_chartPanel.weighty = 1.0;
        gbl_chartPanel.fill = GridBagConstraints.BOTH;
        gbl_chartPanel.gridx = 0;
        gbl_chartPanel.gridy = 1;
        plotPanel.add(mafChartPanel.getChartPanel(), gbl_chartPanel);

        XYDotRenderer dotRenderer = new XYDotRenderer();
        dotRenderer.setSeriesPaint(0, new Color(0, 51, 102));
        dotRenderer.setDotHeight(3);
        dotRenderer.setDotWidth(3);

        XYSplineRenderer lineRenderer = new XYSplineRenderer(3);
        lineRenderer.setUseFillPaint(true);
        lineRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator( 
                StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, 
                new DecimalFormat("0.00"), new DecimalFormat("0.00")));
        
        Stroke stroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, null, 0.0f);
        lineRenderer.setSeriesStroke(0, stroke);
        lineRenderer.setSeriesStroke(1, stroke);
        lineRenderer.setSeriesStroke(2, stroke);
        lineRenderer.setSeriesPaint(0, new Color(34, 139, 34));
        lineRenderer.setSeriesPaint(1, new Color(0, 0, 255));
        lineRenderer.setSeriesPaint(2, new Color(201, 0, 0));
        lineRenderer.setSeriesShape(0, ShapeUtilities.createDiamond((float) 2.5));
        lineRenderer.setSeriesShape(1, ShapeUtilities.createUpTriangle((float) 2.5));
        lineRenderer.setSeriesShape(2, ShapeUtilities.createDownTriangle((float) 2.5));
        mafChartPanel.enablePointsDrag(0);

        lineRenderer.setLegendItemLabelGenerator(
        		new StandardXYSeriesLabelGenerator() {
					private static final long serialVersionUID = 7593430826693873496L;
					public String generateLabel(XYDataset dataset, int series) {
						XYSeries xys = ((XYSeriesCollection)dataset).getSeries(series);
						return xys.getDescription();
					}
        		}
        );

        ValueAxis mafvDomain = new NumberAxis(XAxisName);
        ValueAxis mafgsRange = new NumberAxis(Y1AxisName);
        ValueAxis afrerrRange = new NumberAxis(Y2AxisName);
        
        XYSeriesCollection scatterDataset = new XYSeriesCollection(runData);
        XYSeriesCollection lineDataset = new XYSeriesCollection();

        currMafData.setDescription(currentDataName);
        corrMafData.setDescription(correctedDataName);
        smoothMafData.setDescription(smoothedDataName);
        
        lineDataset.addSeries(smoothMafData);
        lineDataset.addSeries(corrMafData);
        lineDataset.addSeries(currMafData);
        
        XYPlot plot = chart.getXYPlot();
        plot.setRangePannable(true);
        plot.setDomainPannable(true);
        plot.setDomainGridlinePaint(Color.DARK_GRAY);
        plot.setRangeGridlinePaint(Color.DARK_GRAY);
        plot.setBackgroundPaint(new Color(224, 224, 224));

        plot.setDataset(0, lineDataset);
        plot.setRenderer(0, lineRenderer);
        plot.setDomainAxis(0, mafvDomain);
        plot.setRangeAxis(0, mafgsRange);
        plot.mapDatasetToDomainAxis(0, 0);
        plot.mapDatasetToRangeAxis(0, 0);

        plot.setDataset(1, scatterDataset);
        plot.setRenderer(1, dotRenderer);
        plot.setRangeAxis(1, afrerrRange);
        plot.mapDatasetToDomainAxis(1, 0);
        plot.mapDatasetToRangeAxis(1, 1);
        
        LegendTitle legend = new LegendTitle(plot.getRenderer()); 
        legend.setItemFont(new Font("Arial", 0, 10));
        legend.setPosition(RectangleEdge.TOP);
        chart.addLegend(legend);
        
        JPanel mafSmoothingPanel = new JPanel();
        mafSmoothingPanel.setMinimumSize(new Dimension(50, 50));
        GridBagLayout gbl_mafSmoothingPanel = new GridBagLayout();
        gbl_mafSmoothingPanel.columnWidths = new int[]{0, 0, 0};
        gbl_mafSmoothingPanel.rowHeights = new int[] {0, 0};
        gbl_mafSmoothingPanel.columnWeights = new double[]{0.0, 0.0, 1.0};
        gbl_mafSmoothingPanel.rowWeights = new double[]{0.0, 0.0};
        GridBagConstraints gbc_mafSmoothingPanel = new GridBagConstraints();
        gbc_mafSmoothingPanel.ipady = 3;
        gbc_mafSmoothingPanel.anchor = GridBagConstraints.SOUTH;
        gbc_mafSmoothingPanel.weightx = 1.0;
        gbc_mafSmoothingPanel.insets = new Insets(0, 0, 5, 0);
        gbc_mafSmoothingPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_mafSmoothingPanel.gridx = 0;
        gbc_mafSmoothingPanel.gridy = 2;
        mafSmoothingPanel.setLayout(gbl_mafSmoothingPanel);
        plotPanel.add(mafSmoothingPanel, gbc_mafSmoothingPanel);
        
        lblMafIncDec = new JLabel("Inc/Dec");
        GridBagConstraints gbc_lblMafIncDec = new GridBagConstraints();
        gbc_lblMafIncDec.anchor = GridBagConstraints.PAGE_START;
        gbc_lblMafIncDec.insets = new Insets(0, 0, 0, 0);
        gbc_lblMafIncDec.weightx = 0;
        gbc_lblMafIncDec.weighty = 0;
        gbc_lblMafIncDec.gridx = 0;
        gbc_lblMafIncDec.gridy = 0;
        mafSmoothingPanel.add(lblMafIncDec, gbc_lblMafIncDec);
        
        NumberFormat doubleFmt = NumberFormat.getNumberInstance();
        doubleFmt.setMaximumFractionDigits(2);
        mafIncDecTextField = new JFormattedTextField(doubleFmt);
        mafIncDecTextField.setValue(new Double(0.1));
        mafIncDecTextField.setColumns(5);
        mafIncDecTextField.setPreferredSize(new Dimension(40, 24));
        GridBagConstraints gbc_mafIncDecTextField = new GridBagConstraints();
        gbc_mafIncDecTextField.anchor = GridBagConstraints.PAGE_START;
        gbc_mafIncDecTextField.insets = new Insets(0, 0, 0, 0);
        gbc_mafIncDecTextField.weightx = 0;
        gbc_mafIncDecTextField.weighty = 0;
        gbc_mafIncDecTextField.gridx = 0;
        gbc_mafIncDecTextField.gridy = 1;
        mafSmoothingPanel.add(mafIncDecTextField, gbc_mafIncDecTextField);
        
        btnPlusButton = new JButton("<html><div style='text-align: center; font-weight: bold;'>+</div></html>");
        btnPlusButton.setActionCommand("plus");
        btnPlusButton.addActionListener(this);
        btnPlusButton.setPreferredSize(new Dimension(40, 25));
        GridBagConstraints gbc_btnPlusButton = new GridBagConstraints();
        gbc_btnPlusButton.anchor = GridBagConstraints.PAGE_START;
        gbc_btnPlusButton.insets = new Insets(0, 0, 0, 0);
        gbc_btnPlusButton.weightx = 0;
        gbc_btnPlusButton.weighty = 0;
        gbc_btnPlusButton.gridx = 1;
        gbc_btnPlusButton.gridy = 0;
        mafSmoothingPanel.add(btnPlusButton, gbc_btnPlusButton);

        btnMinusButton = new JButton("<html><div style='text-align: center; font-weight: bold;'>-</div></html>");
        btnMinusButton.setActionCommand("minus");
        btnMinusButton.addActionListener(this);
        btnMinusButton.setPreferredSize(new Dimension(40, 25));
        GridBagConstraints gbc_btnMinusButton = new GridBagConstraints();
        gbc_btnMinusButton.anchor = GridBagConstraints.PAGE_START;
        gbc_btnMinusButton.insets = new Insets(0, 0, 0, 0);
        gbc_btnMinusButton.weightx = 0;
        gbc_btnMinusButton.weighty = 0;
        gbc_btnMinusButton.gridx = 1;
        gbc_btnMinusButton.gridy = 1;
        mafSmoothingPanel.add(btnMinusButton, gbc_btnMinusButton);

        mafSmoothingTable = new JTable() {
            private static final long serialVersionUID = 6526901361175099297L;
            public boolean isCellEditable(int row, int column) { return false; };
        };
        
        mafSmoothingTable.setColumnSelectionAllowed(true);
        mafSmoothingTable.setCellSelectionEnabled(true);
        mafSmoothingTable.setBorder(new LineBorder(new Color(0, 0, 0)));
        mafSmoothingTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        mafSmoothingTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        mafSmoothingTable.setModel(new DefaultTableModel(2, mafTable.getColumnCount()));
        mafSmoothingTable.setTableHeader(null);
        Utils.initializeTable(mafSmoothingTable, ColumnWidth);
        GridBagConstraints gbc_mafTable = new GridBagConstraints();
        gbc_mafTable.anchor = GridBagConstraints.SOUTH;
        gbc_mafTable.insets = new Insets(0, 0, 0, 0);
        gbc_mafTable.fill = GridBagConstraints.HORIZONTAL;
        gbc_mafTable.weightx = 1.0;
        gbc_mafTable.gridx = 2;
        gbc_mafTable.gridy = 0;
        gbc_mafTable.gridheight = 2;
        
        JScrollPane mafSmoothingPane = new JScrollPane(mafSmoothingTable);
        mafSmoothingPane.setPreferredSize(new Dimension(52, 52));
        mafSmoothingPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        
        mafSmoothingPanel.add(mafSmoothingPane, gbc_mafTable);
        excelAdapter.addTable(mafSmoothingTable, false, true, true, false, false, true, false, false, true);
        lblMafIncDec.setVisible(false);
        mafIncDecTextField.setVisible(false);
        btnPlusButton.setVisible(false);
        btnMinusButton.setVisible(false);
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

    //////////////////////////////////////////////////////////////////////////////////////
    // COMMOMN ACTIONS RELATED FUNCTIONS
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("clearmaf".equals(e.getActionCommand())) {
            clearMafTable();
        }
        else if ("clearlog".equals(e.getActionCommand())) {
            clearLogDataTables();
            clearAfrDataTables();
        }
        else if ("clearall".equals(e.getActionCommand())) {
            clearMafTable();
            clearLogDataTables();
            clearAfrDataTables();
        }
        else if ("load".equals(e.getActionCommand())) {
            loadData();
        }
        else if ("save".equals(e.getActionCommand())) {
            saveData();
        }
        else if ("go".equals(e.getActionCommand())) {
            calculateMafScaling();
        }
        else if ("compare".equals(e.getActionCommand())) {
        	mafCompare.setVisible(true);
        }
        else if ("smooth".equals(e.getActionCommand())) {
            smoothCurve();
        }
        else if ("smoothreset".equals(e.getActionCommand())) {
            smoothReset();
        }
        else if ("plus".equals(e.getActionCommand())) {
            changeMafSmoothingCellValue(true);
        }
        else if ("minus".equals(e.getActionCommand())) {
            changeMafSmoothingCellValue(false);
        }
        else if ("fueling".equals(e.getActionCommand())) {
            polfTable.getSetUserFueling();
        }
        if ("loadlog".equals(e.getActionCommand())) {
            loadLogFile();
        }
        else if ("dvdt".equals(e.getActionCommand())) {
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
    
    private void clearNotRunDataCheckboxes() {
    	if (checkBoxRunData.isSelected()) {
    		checkBoxRunData.setSelected(false);
    		runData.clear();
    	}
    	if (checkBoxCurrentMaf.isSelected()) {
    		checkBoxCurrentMaf.setSelected(false);
    		currMafData.clear();
    	}
    	if (checkBoxCorrectedMaf.isSelected()) {
    		checkBoxCorrectedMaf.setSelected(false);
    		corrMafData.clear();
    	}
    	if (checkBoxSmoothedMaf.isSelected()) {
    		checkBoxSmoothedMaf.setSelected(false);
    		smoothMafData.clear();
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
    
    private void clearMafTable() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
    	try {
	        while (MafTableColumnCount < mafTable.getColumnCount())
	            Utils.removeColumn(MafTableColumnCount, mafTable);
	        Utils.clearTable(mafTable);
    	}
    	finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    	}
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
    
    private void clearData() {
    	trimArray.clear();
    	rpmArray.clear();
    	timeArray.clear();
    	iatArray.clear();
    	mafvArray.clear();
    	dvdtArray.clear();
        voltArray.clear();
        gsArray.clear();
        gsCorrected.clear();
        smoothGsArray.clear();
        Utils.clearTable(afr1Table);
        Utils.clearTable(afr2Table);
    }
    
    private void clearChartData() {
        runData.clear();
        currMafData.clear();
        corrMafData.clear();
        smoothMafData.clear();
    }
    
    private void clearChartCheckBoxes() {
    	checkBoxDvdtData.setSelected(false);
    	checkBoxIatData.setSelected(false);
        checkBoxRunData.setSelected(false);
        checkBoxCurrentMaf.setSelected(false);
        checkBoxCorrectedMaf.setSelected(false);
        checkBoxSmoothedMaf.setSelected(false);
    }
    
    private void calculateMafScaling() {
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
        double time;
        double load;
        double rpm;
        double dvdt;
        double afr;
        double mafv;
        double stft;
        double ltft;
        double iat;
        double corr;
        double val1;
        double val2;
        String timeStr;
        String loadStr;
        String rpmStr;
        String mafvStr;
        String afrStr;
        String stftStr;
        String ltftStr;
        String dvdtStr;
        String iatStr;
        int closestMafIdx;
        int closestRmpIdx;
        int closestLoadIdx;
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
        ArrayList<Double> afrLoadArray = new ArrayList<Double>();
        for (i = 1; i < polfTable.getColumnCount(); ++i) {
            afrLoadArray.add(Double.valueOf(polfTable.getValueAt(0, i).toString()));
            Utils.ensureColumnCount(i + 1, afr1Table);
            Utils.ensureColumnCount(i + 1, afr2Table);
            afr1Table.setValueAt(polfTable.getValueAt(0, i), 0, i);
            afr2Table.setValueAt(polfTable.getValueAt(0, i), 0, i);
        }
        Integer val;
        HashMap<Double, Integer> modeCountMap;
        for (i = 0; i < logDataTable.getRowCount(); ++i) {
            timeStr = logDataTable.getValueAt(i, 0).toString();
            loadStr = logDataTable.getValueAt(i, 1).toString();
            rpmStr = logDataTable.getValueAt(i, 2).toString();
            mafvStr = logDataTable.getValueAt(i, 3).toString();
            afrStr = logDataTable.getValueAt(i, 4).toString();
            stftStr = logDataTable.getValueAt(i, 5).toString();
            ltftStr = logDataTable.getValueAt(i, 6).toString();
            dvdtStr = logDataTable.getValueAt(i, 7).toString();
            iatStr = logDataTable.getValueAt(i, 8).toString();
            if (timeStr.isEmpty() || loadStr.isEmpty() || rpmStr.isEmpty() || mafvStr.isEmpty() ||
            	afrStr.isEmpty() || stftStr.isEmpty() || ltftStr.isEmpty() || dvdtStr.isEmpty() || iatStr.isEmpty())
                break;
            if (!Utils.validateDouble(timeStr, i, 0, tableName) ||
            	!Utils.validateDouble(loadStr, i, 1, tableName) ||
                !Utils.validateDouble(rpmStr, i, 2, tableName) ||
                !Utils.validateDouble(mafvStr, i, 3, tableName) ||
                !Utils.validateDouble(afrStr, i, 4, tableName) ||
                !Utils.validateDouble(stftStr, i, 5, tableName) ||
                !Utils.validateDouble(ltftStr, i, 6, tableName) ||
                !Utils.validateDouble(dvdtStr, i, 7, tableName) ||
                !Utils.validateDouble(iatStr, i, 8, tableName))
                return;
            time = Double.valueOf(timeStr);
            load = Double.valueOf(loadStr);
            rpm = Double.valueOf(rpmStr);
            mafv = Double.valueOf(mafvStr);
            afr = Double.valueOf(afrStr);
            stft = Double.valueOf(stftStr);
            ltft = Double.valueOf(ltftStr);
            dvdt = Double.valueOf(dvdtStr);
            iat = Double.valueOf(iatStr);
            corr = ltft + stft;
            trimArray.add(corr);
            rpmArray.add(rpm);
            timeArray.add(time);
            iatArray.add(iat);
            mafvArray.add(mafv);
            dvdtArray.add(dvdt);
            
            closestMafIdx = Utils.closestValueIndex(load * rpm / 60.0, gsArray);
            correctionMeanArray.set(closestMafIdx, (correctionMeanArray.get(closestMafIdx) * temp.get(closestMafIdx) + corr) / (temp.get(closestMafIdx) + 1));
            temp.set(closestMafIdx, temp.get(closestMafIdx) + 1);
            modeCountMap = modeCalcArray.get(closestMafIdx);
            double roundedCorr = ((double)Math.round(corr * 10.0)) / 10.0;
            val = modeCountMap.get(roundedCorr);
            if (val == null)
            	modeCountMap.put(roundedCorr, 1);
            else
            	modeCountMap.put(roundedCorr, val + 1);
            
            closestRmpIdx = Utils.closestValueIndex(rpm, afrRpmArray) + 1;
            closestLoadIdx = Utils.closestValueIndex(load, afrLoadArray) + 1;
            val1 = (afr1Table.getValueAt(closestRmpIdx, closestLoadIdx).toString().isEmpty()) ? 0 : Double.valueOf(afr1Table.getValueAt(closestRmpIdx, closestLoadIdx).toString());
            val2 = (afr2Table.getValueAt(closestRmpIdx, closestLoadIdx).toString().isEmpty()) ? 0 : Double.valueOf(afr2Table.getValueAt(closestRmpIdx, closestLoadIdx).toString());
            afr1Table.setValueAt((val1 * val2 + afr) / (val2 + 1.0), closestRmpIdx, closestLoadIdx);
            afr2Table.setValueAt(val2 + 1.0, closestRmpIdx, closestLoadIdx);
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
            if (temp.get(i) > MinimumDataSampleCount) {
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
    
    private boolean getMafTableData(ArrayList<Double> voltArray, ArrayList<Double> gsArray) {
        String value;
        for (int i = 0; i < mafTable.getColumnCount(); ++i) {
            for (int j = 0; j < mafTable.getRowCount(); ++j) {
                value = mafTable.getValueAt(j, i).toString();
                if (value.isEmpty() && i > 0)
                    return true;
                if (!Utils.validateDouble(value, j, i, MafTableName))
                    return false;
            }
            voltArray.add(Double.parseDouble(mafTable.getValueAt(0, i).toString()));
            gsArray.add(Double.parseDouble(mafTable.getValueAt(1, i).toString()));
        }
        if (voltArray.size() != gsArray.size()) {
            JOptionPane.showMessageDialog(null, "Data sets (volt/gs) in  " + MafTableName + " have different length", "Invalid Data", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
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
    
    private boolean plotCurrentMafData() {
        ArrayList<Double> xarr = voltArray;
        ArrayList<Double> yarr = gsArray;
        if (xarr.size() == 0 || yarr.size() == 0 || xarr.size() != yarr.size()) {
            xarr = new ArrayList<Double>();
            yarr = new ArrayList<Double>();
            if (mafTable.getValueAt(0, 0).toString().trim().isEmpty())
                return false;
            if (!getMafTableData(xarr, yarr))
                return false;
        }
        return setXYSeries(currMafData, xarr, yarr);
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

    private boolean setXYTable(JTable table, ArrayList<Double> xarr, ArrayList<Double> yarr) {
        if (xarr.size() == 0 || yarr.size() == 0 || xarr.size() != yarr.size())
            return false;
        for (int i = 0; i < xarr.size(); ++i) {
            Utils.ensureColumnCount(i + 1, table);
            table.setValueAt(xarr.get(i), 0, i);
            table.setValueAt(smoothGsArray.get(i), 1, i);
        }
        return true;
    }
    
    private boolean setCorrectedMafData() {
        return setXYSeries(corrMafData, voltArray, gsCorrected);
    }
    
    private boolean setSmoothedMafData() {
        return setXYSeries(smoothMafData, voltArray, smoothGsArray);
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
    
    private void plotLineSlope(XYSeries series, ArrayList<Double> xarr, ArrayList<Double> yarr) {
        if (xarr.size() == 0 || yarr.size() == 0 || xarr.size() != yarr.size())
            return;
        for (int j = 0; j < xarr.size() - 1; ++j)
            series.add((double)xarr.get(j + 1), (double)((yarr.get(j + 1) - yarr.get(j)) / (xarr.get(j + 1) - xarr.get(j))));
    }
    
    private void plotSmoothingLineSlopes() {
        currMafData.clear();
        smoothMafData.clear();
        plotLineSlope(currMafData, voltArray, gsArray);
        plotLineSlope(smoothMafData, voltArray, smoothGsArray);
        setXYTable(mafSmoothingTable, voltArray, smoothGsArray);
    }
    
    public void onMovePoint(int itemIndex, double valueX, double valueY) {
        ArrayList<Double> xarr = voltArray;
        ArrayList<Double> yarr = smoothGsArray;
        XYSeries series = smoothMafData;
        if (checkBoxSmoothing.isSelected() == false)
            yarr.set(itemIndex, valueY);
        else {
            double X1 = xarr.get(itemIndex);
            double Y1 = yarr.get(itemIndex);
            double X2 = xarr.get(itemIndex + 1);
            double Y2 = X2 * valueY - X1 * valueY + Y1;
            yarr.set(itemIndex + 1, Y2);
            mafSmoothingTable.setValueAt(Y2, 1, itemIndex + 1);
            corrMafData.updateByIndex(itemIndex + 1, Y2);
            if (itemIndex + 1 < series.getItemCount()) {
                X1 = X2;
                X2 = xarr.get(itemIndex + 2);
                Y1 = Y2;
                Y2 = yarr.get(itemIndex + 2);
                valueY = (Y2 - Y1) / (X2 - X1);
                series.updateByIndex(itemIndex + 1, valueY);
            }
        }
    }
    
    private void enableSmoothingView(boolean flag) {
        if (smoothGsArray.size() == 0) {
            checkBoxSmoothing.setSelected(false);
            return;
        }
        smoothComboBox.setEnabled(flag);
        btnSmoothButton.setEnabled(flag);
        checkBoxDvdtData.setEnabled(!flag);
        checkBoxIatData.setEnabled(!flag);
        checkBoxTrpmData.setEnabled(!flag);
        checkBoxMnmdData.setEnabled(!flag);
        checkBoxRunData.setEnabled(!flag);
        checkBoxCurrentMaf.setEnabled(!flag);
        checkBoxCorrectedMaf.setEnabled(!flag);
        checkBoxSmoothedMaf.setEnabled(!flag);
        clearChartData();
        if (flag == true) {
            lblMafIncDec.setVisible(true);
            mafIncDecTextField.setVisible(true);
            btnPlusButton.setVisible(true);
            btnMinusButton.setVisible(true);
            setCorrectedMafData();
            plotSmoothingLineSlopes();
            corrMafData.setDescription(mafCurveDataName);
            currMafData.setDescription(currentSlopeDataName);
            smoothMafData.setDescription(smoothedSlopeDataName);
        }
        else {
            lblMafIncDec.setVisible(false);
            mafIncDecTextField.setVisible(false);
            btnPlusButton.setVisible(false);
            btnMinusButton.setVisible(false);
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
            currMafData.setDescription(currentDataName);
            corrMafData.setDescription(correctedDataName);
            smoothMafData.setDescription(smoothedDataName);
        }
    }
    
    private void smoothReset() {
        if (gsCorrected.size() == 0)
            return;
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
	        smoothGsArray.clear();
	        smoothGsArray.addAll(gsCorrected);
	        corrMafData.clear();
	        if (!checkBoxDvdtData.isEnabled() || !checkBoxDvdtData.isSelected() ||
	        	!checkBoxTrpmData.isEnabled() || !checkBoxTrpmData.isSelected() || 
	        	!checkBoxMnmdData.isEnabled() || !checkBoxMnmdData.isSelected() || 
	        	!checkBoxIatData.isEnabled() || !checkBoxIatData.isSelected())
	        	setCorrectedMafData();
	        if (checkBoxSmoothing.isSelected())
	            plotSmoothingLineSlopes();
	        else if (checkBoxSmoothedMaf.isSelected())
	            setSmoothedMafData();
	        setXYTable(mafSmoothingTable, voltArray, smoothGsArray);
        }
        finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    public void changeMafSmoothingCellValue(boolean add) {
        if (!Pattern.matches(Utils.fpRegex, mafIncDecTextField.getText())) {
            JOptionPane.showMessageDialog(null, "Invalid Maf Scaling increment/decrement value", "Invalid value", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double smoothingIncrement = Double.valueOf(mafIncDecTextField.getText());
        
        int[] rows = mafSmoothingTable.getSelectedRows();
        int[] cols = mafSmoothingTable.getSelectedColumns();
        if (rows == null || cols == null) {
            JOptionPane.showMessageDialog(null, "Please select MAF table cell(s) to apply increment/decrement.", "Invalid selection", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        double val;
        for (int col : cols) {
            if (!Pattern.matches(Utils.fpRegex, mafSmoothingTable.getValueAt(1, col).toString()))
                continue;
            val = (Double)(mafSmoothingTable.getValueAt(1, col)) + (add == true ? smoothingIncrement : smoothingIncrement * -1.0); 
            mafSmoothingTable.setValueAt(val, 1, col);
            smoothGsArray.set(col, val);
            corrMafData.updateByIndex(col, val);
            if (col == 0)
                smoothMafData.updateByIndex(col, (smoothGsArray.get(col + 1) - smoothGsArray.get(col)) / (voltArray.get(col + 1) - voltArray.get(col)));
            else if (col == smoothGsArray.size() - 1)
                smoothMafData.updateByIndex(col - 1, (smoothGsArray.get(col) - smoothGsArray.get(col - 1)) / (voltArray.get(col) - voltArray.get(col - 1)));
            else {
                smoothMafData.updateByIndex(col, (smoothGsArray.get(col + 1) - smoothGsArray.get(col)) / (voltArray.get(col + 1) - voltArray.get(col)));
                smoothMafData.updateByIndex(col - 1, (smoothGsArray.get(col) - smoothGsArray.get(col - 1)) / (voltArray.get(col) - voltArray.get(col - 1)));
            }
        }
    }
    
    private void smoothCurve() {
        if (!checkBoxSmoothing.isSelected() || voltArray.size() == 0 || smoothGsArray.size() == 0)
            return;

        int degree = Integer.parseInt(smoothComboBox.getSelectedItem().toString().trim());
        int[] rows = mafSmoothingTable.getSelectedRows();
        int[] cols = mafSmoothingTable.getSelectedColumns();
        if (rows == null || cols == null || cols.length < degree) {
            JOptionPane.showMessageDialog(null, "Please select MAF Scaling table cells range to apply smoothing. Number of selected columns must be greater than the smoothing degree value.", "Invalid selection", JOptionPane.ERROR_MESSAGE);
            return;
        }        
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        int start = cols[0];
        int end = cols[cols.length - 1];
        try {
	        int movWind;
	        int i;
	        ArrayList<Double> tmpArray = new ArrayList<Double>();
	        if (degree == 3) {
	            movWind = 1;
	            for (i = movWind + start; i + movWind <= end; ++i)
	                tmpArray.add(0.25 * smoothGsArray.get(i - 1) + 
	                             0.5  * smoothGsArray.get(i) + 
	                             0.25 * smoothGsArray.get(i + 1));
	            for (i = 0; i < tmpArray.size(); ++i)
	                smoothGsArray.set(i + movWind + start, tmpArray.get(i));
	        }
	        if (degree == 5) {
	            movWind = 2;
	            for (i = movWind + start; i + movWind <= end; ++i)
	                tmpArray.add(0.1 * smoothGsArray.get(i - 2) + 
	                             0.2 * smoothGsArray.get(i - 1) + 
	                             0.4 * smoothGsArray.get(i) + 
	                             0.2 * smoothGsArray.get(i + 1) +
	                             0.1 * smoothGsArray.get(i + 2));
	            for (i = 0; i < tmpArray.size(); ++i)
	                smoothGsArray.set(i + movWind + start, tmpArray.get(i));
	        }
	        if (degree == 7) {
	            movWind = 3;
	            for (i = movWind + start; i + movWind <= end; ++i)
	                tmpArray.add(0.05 * smoothGsArray.get(i - 3) +
	                             0.1  * smoothGsArray.get(i - 2) + 
	                             0.15 * smoothGsArray.get(i - 1) + 
	                             0.4  * smoothGsArray.get(i) + 
	                             0.15 * smoothGsArray.get(i + 1) +
	                             0.1  * smoothGsArray.get(i + 2) +
	                             0.05 * smoothGsArray.get(i + 3));
	            for (i = 0; i < tmpArray.size(); ++i)
	                smoothGsArray.set(i + movWind + start, tmpArray.get(i));
	        }
	        plotSmoothingLineSlopes();
        }
        finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
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
                elements = line.split(",", -1);
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
        logClOlStatusColIdx = columns.indexOf(logClOlStatusColName);
        logAfLearningColIdx = columns.indexOf(logAfLearningColName);
        logAfCorrectionColIdx = columns.indexOf(logAfCorrectionColName);
        logAfrColIdx = columns.indexOf(logAfrColName);
        logRpmColIdx = columns.indexOf(logRpmColName);
        logLoadColIdx = columns.indexOf(logLoadColName);
        logTimeColIdx = columns.indexOf(logTimeColName);
        logMafvColIdx = columns.indexOf(logMafvColName);
        logIatColIdx = columns.indexOf(logIatColName);
        if (logClOlStatusColIdx == -1)   { Config.setClOlStatusColumnName(Config.NO_NAME);   ret = false; }
        if (logAfLearningColIdx == -1)   { Config.setAfLearningColumnName(Config.NO_NAME);   ret = false; }
        if (logAfCorrectionColIdx == -1) { Config.setAfCorrectionColumnName(Config.NO_NAME); ret = false; }
        if (logAfrColIdx == -1)          { Config.setAfrColumnName(Config.NO_NAME);          ret = false; }
        if (logRpmColIdx == -1)          { Config.setRpmColumnName(Config.NO_NAME);          ret = false; }
        if (logLoadColIdx == -1)         { Config.setLoadColumnName(Config.NO_NAME);         ret = false; }
        if (logTimeColIdx == -1)         { Config.setTimeColumnName(Config.NO_NAME);         ret = false; }
        if (logMafvColIdx == -1)         { Config.setMafVoltageColumnName(Config.NO_NAME);   ret = false; }
        if (logIatColIdx == -1)          { Config.setIatColumnName(Config.NO_NAME);          ret = false; }
        clValue = Config.getClOlStatusValue();
        afrMin = Config.getAfrMinimumValue();
        afrMax = Config.getAfrMaximumValue();
        minLoad = Config.getLoadMinimumValue();
        maxDvDt = Config.getDvDtMaximumValue();
        maxMafV = Config.getMafVMaximumValue();
        maxIat = Config.getIatMaximumValue();
        return ret;
    }
    
    private void loadLogFile() {
        if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(this))
            return;
        boolean isPolSet = polfTable.isSet();
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
	                if (logClOlStatusColIdx >= 0 || logAfLearningColIdx >= 0 || logAfCorrectionColIdx >= 0 || logAfrColIdx >= 0 ||
	                	logRpmColIdx >= 0 || logLoadColIdx >=0 || logTimeColIdx >=0 || logMafvColIdx >= 0 || logIatColIdx >= 0 ) {
	                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Would you like to reset column names or filter values?", "Columns/Filters Reset", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE))
	                    	resetColumns = true;
	                }
	
	                if (resetColumns || logClOlStatusColIdx < 0 || logAfLearningColIdx < 0 || logAfCorrectionColIdx < 0 || logAfrColIdx < 0 ||
	                	logRpmColIdx < 0 || logLoadColIdx < 0 || logTimeColIdx < 0 || logMafvColIdx < 0 || logIatColIdx < 0 ) {
	                	ColumnsFiltersSelection selectionWindow = new ColumnsFiltersSelection(ColumnsFiltersSelection.TaskTab.CLOSED_LOOP, isPolSet);
	                	if (!selectionWindow.getUserSettings(elements) || !getColumnsFilters(elements))
	                		return;
	                }
	                
	                String[] flds;
	                line = br.readLine();
	                int clol;
	                int i = 2;
	                int row = getLogTableEmptyRow();
	                double afr = 0;
	                double dVdt = 0;
	                double prevTime = 0;
	                double time = 0;
	                double timeMultiplier = 1.0;
	                double pmafv = 0;
	                double mafv = 0;
	                double load;
	                double iat;
	                setCursor(new Cursor(Cursor.WAIT_CURSOR));

	                while (line != null) {
	                    flds = line.split(",", -1);
	                    try {
	                        clol = Integer.valueOf(flds[logClOlStatusColIdx]);
	                        if (clol == clValue) {
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
	                            // Filters
	                        	afr = Double.valueOf(flds[logAfrColIdx]);
	                        	load = Double.valueOf(flds[logLoadColIdx]);
	                        	iat = Double.valueOf(flds[logIatColIdx]);
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
	                                row += 1;
	                        	}
	                        }
	                    }
	                    catch (NumberFormatException e) {
	                        logger.error(e);
	                        JOptionPane.showMessageDialog(null, "Error parsing number at line " + i + ": " + e, "Error processing file", JOptionPane.ERROR_MESSAGE);
	                        return;
	                    }
	                    line = br.readLine();
	                    i += 1;
	                }
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
    
    private String usage() {
        ResourceBundle bundle;
        bundle = ResourceBundle.getBundle("com.vgi.mafscaling.closed_loop");
        return bundle.getString("usage"); 
    }
}
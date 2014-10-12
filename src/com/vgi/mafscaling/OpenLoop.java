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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.TreeMap;
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
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

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
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;

public class OpenLoop extends JTabbedPane implements ActionListener, IMafChartHolder {
    private static final long serialVersionUID = 2988105467764335997L;
    private static final Logger logger = Logger.getLogger(OpenLoop.class);
    private static final String SaveDataFileHeader = "[open_loop run data]";
    private static final String MafTableName = "Current MAF Scaling";
    private static final String RunTableName = "Run ";
    private static final String XAxisName = "MAF Sensor (Voltage)";
    private static final String Y1AxisName = "Mass Airflow (g/s)";
    private static final String Y2AxisName = "AFR Error (%)";
    private static final String rpmAxisName = "RPM";
    private static final String runDataName = "AFR Error";
    private static final String currentDataName = "Current";
    private static final String correctedDataName = "Corrected";
    private static final String smoothedDataName = "Smoothed";
    private static final String mafCurveDataName = "Smoothed Maf Curve";
    private static final String currentSlopeDataName = "Current Maf Slope";
    private static final String smoothedSlopeDataName = "Smoothed Maf Slope";
    private static final int ColumnWidth = 50;
    private static final int RunCount = 12;
    private static final int MafTableColumnCount = 50;
    private static final int RunRowsCount = 200;
    private double minMafV = Config.getMafVMinimumValue();
    private double afrErrPrct = Config.getWidebandAfrErrorPercentValue();
    private double minWotEnrichment = Config.getWOTEnrichmentValue();
    private int wotPoint = Config.getWOTStationaryPointValue();
    private int afrRowOffset = Config.getWBO2RowOffset();
    private int skipRowsOnTransition = Config.getOLCLTransitionSkipRows();
    private int logThtlAngleColIdx = -1;
    private int logAfLearningColIdx = -1;
    private int logAfCorrectionColIdx = -1;
    private int logMafvColIdx = -1;
    private int logAfrColIdx = -1;
    private int logRpmColIdx = -1;
    private int logLoadColIdx = -1;
    private int logCommandedAfrCol = -1;

    private JTable mafTable = null;
    private JTable mafSmoothingTable = null;
    private MafChartPanel mafChartPanel = null;
    private JCheckBox checkBoxMafRpmData = null;
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

    private ArrayList<Double> rpmArray = new ArrayList<Double>();
    private ArrayList<Double> mafvArray = new ArrayList<Double>();
    private ArrayList<Double> afrArray = new ArrayList<Double>();
    private ArrayList<Double> voltArray = new ArrayList<Double>();
    private ArrayList<Double> gsArray = new ArrayList<Double>();
    private ArrayList<Double> gsCorrected = new ArrayList<Double>();
    private ArrayList<Double> smoothGsArray = new ArrayList<Double>();

    private final ExcelAdapter excelAdapter = new ExcelAdapter();
    private final JTable[] runTables = new JTable[RunCount];
    private final JFileChooser fileChooser = new JFileChooser();
    private final XYSeries runData = new XYSeries(runDataName);
    private final XYSeries currMafData = new XYSeries(currentDataName);
    private final XYSeries corrMafData = new XYSeries(correctedDataName);
    private final XYSeries smoothMafData = new XYSeries(smoothedDataName);
    private PrimaryOpenLoopFuelingTable polfTable = null;
    private MafCompare mafCompare = null;

    public OpenLoop(int tabPlacement, PrimaryOpenLoopFuelingTable table, MafCompare comparer) {
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
        gbl_dataPanel.columnWidths = new int[] {0};
        gbl_dataPanel.rowHeights = new int[] {0, 0, 0, 0};
        gbl_dataPanel.columnWeights = new double[]{1.0};
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
        btnRunClearButton.setActionCommand("clearrun");
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
        gbc_dataScrollPane.weightx = 1.0;
        gbc_dataScrollPane.weighty = 1.0;
        gbc_dataScrollPane.fill = GridBagConstraints.BOTH;
        gbc_dataScrollPane.gridx = 0;
        gbc_dataScrollPane.gridy = 3;
        dataPanel.add(dataScrollPane, gbc_dataScrollPane);
        
        JPanel dataRunPanel = new JPanel();
        dataScrollPane.setViewportView(dataRunPanel);
        GridBagLayout gbl_dataRunPanel = new GridBagLayout();
        gbl_dataRunPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_dataRunPanel.rowHeights = new int[] {0};
        gbl_dataRunPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        gbl_dataRunPanel.rowWeights = new double[]{0.0};
        dataRunPanel.setLayout(gbl_dataRunPanel);

        createRunTables(dataRunPanel);
    }
    
    private void createRunTables(JPanel dataRunPanel) {
        for (int i = 0; i < RunCount; ++i) {
            runTables[i] = new JTable();
            JTable table = runTables[i];
            table.getTableHeader().setReorderingAllowed(false);
            table.setModel(new DefaultTableModel(RunRowsCount, 3));
            table.setColumnSelectionAllowed(true);
            table.setCellSelectionEnabled(true);
            table.setBorder(new LineBorder(new Color(0, 0, 0)));
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
            table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            table.getColumnModel().getColumn(0).setHeaderValue("<html><center>Engine<br>Speed<br>(RPM)<br></center></html>");
            table.getColumnModel().getColumn(1).setHeaderValue("<html><center>MAF<br>Sensor<br>Voltage<br></center></html>");
            table.getColumnModel().getColumn(2).setHeaderValue("<html><center>AFR<br>Error<br>%<br></center></html>");
            Utils.initializeTable(table, ColumnWidth);
            
            JTableHeader header = table.getTableHeader();
            GridBagConstraints gbc_header = new GridBagConstraints();
            gbc_header.insets = new Insets(0, 0, 0, 5);
            gbc_header.gridx = i;
            gbc_header.gridy = 0;
            dataRunPanel.add(header, gbc_header);

            GridBagConstraints gbc_table = new GridBagConstraints();
            gbc_table.insets = new Insets(0, 0, 0, 5);
            gbc_table.anchor = GridBagConstraints.PAGE_START;
            gbc_table.gridx = i;
            gbc_table.gridy = 1;
            dataRunPanel.add(table, gbc_table);
            
            excelAdapter.addTable(table, true, false);
        }
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
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0, 0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);
        
        checkBoxMafRpmData = new JCheckBox("MafV/RPM");
        GridBagConstraints gbc_checkBoxMafRpmData = new GridBagConstraints();
        gbc_checkBoxMafRpmData.insets = new Insets(0, 0, 3, 3);
        gbc_checkBoxMafRpmData.gridx = 0;
        gbc_checkBoxMafRpmData.gridy = 0;
        checkBoxMafRpmData.setActionCommand("mafrpm");
        checkBoxMafRpmData.addActionListener(this);
        cntlPanel.add(checkBoxMafRpmData, gbc_checkBoxMafRpmData);
        
        checkBoxRunData = new JCheckBox("AFR Error");
        GridBagConstraints gbc_checkBoxData = new GridBagConstraints();
        gbc_checkBoxData.insets = new Insets(0, 0, 3, 3);
        gbc_checkBoxData.gridx = 1;
        gbc_checkBoxData.gridy = 0;
        checkBoxRunData.setActionCommand("rdata");
        checkBoxRunData.addActionListener(this);
        cntlPanel.add(checkBoxRunData, gbc_checkBoxData);
        
        checkBoxCurrentMaf = new JCheckBox("Current");
        GridBagConstraints gbc_checkBoxCurrentMaf = new GridBagConstraints();
        gbc_checkBoxCurrentMaf.insets = new Insets(0, 0, 3, 3);
        gbc_checkBoxCurrentMaf.gridx = 2;
        gbc_checkBoxCurrentMaf.gridy = 0;
        checkBoxCurrentMaf.setActionCommand("current");
        checkBoxCurrentMaf.addActionListener(this);
        cntlPanel.add(checkBoxCurrentMaf, gbc_checkBoxCurrentMaf);
        
        checkBoxCorrectedMaf = new JCheckBox("Corrected");
        GridBagConstraints gbc_checkBoxCorrectedMaf = new GridBagConstraints();
        gbc_checkBoxCorrectedMaf.insets = new Insets(0, 0, 3, 3);
        gbc_checkBoxCorrectedMaf.gridx = 3;
        gbc_checkBoxCorrectedMaf.gridy = 0;
        checkBoxCorrectedMaf.setActionCommand("corrected");
        checkBoxCorrectedMaf.addActionListener(this);
        cntlPanel.add(checkBoxCorrectedMaf, gbc_checkBoxCorrectedMaf);

        checkBoxSmoothedMaf = new JCheckBox("Smoothed");
        GridBagConstraints gbc_checkBoxSmoothedMaf = new GridBagConstraints();
        gbc_checkBoxSmoothedMaf.insets = new Insets(0, 0, 3, 3);
        gbc_checkBoxSmoothedMaf.gridx = 4;
        gbc_checkBoxSmoothedMaf.gridy = 0;
        checkBoxSmoothedMaf.setActionCommand("smoothed");
        checkBoxSmoothedMaf.addActionListener(this);
        cntlPanel.add(checkBoxSmoothedMaf, gbc_checkBoxSmoothedMaf);

        btnCompareButton = new JButton("Compare");
        GridBagConstraints gbc_btnCompareButton = new GridBagConstraints();
        gbc_btnCompareButton.anchor = GridBagConstraints.CENTER;
        gbc_btnCompareButton.insets = new Insets(0, 0, 3, 3);
        gbc_btnCompareButton.weightx = 1.0;
        gbc_btnCompareButton.gridx = 5;
        gbc_btnCompareButton.gridy = 0;
        btnCompareButton.setActionCommand("compare");
        btnCompareButton.addActionListener(this);
        cntlPanel.add(btnCompareButton, gbc_btnCompareButton);

        checkBoxSmoothing = new JCheckBox("Smoothing:");
        GridBagConstraints gbc_checkBoxSmoothing = new GridBagConstraints();
        gbc_checkBoxSmoothing.anchor = GridBagConstraints.CENTER;
        gbc_checkBoxSmoothing.insets = new Insets(0, 0, 3, 3);
        gbc_checkBoxSmoothing.gridx = 6;
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
        gbc_smoothComboBox.gridx = 7;
        gbc_smoothComboBox.gridy = 0;
        cntlPanel.add(smoothComboBox, gbc_smoothComboBox);

        btnSmoothButton = new JButton("Apply");
        btnSmoothButton.setEnabled(false);
        GridBagConstraints gbc_btnSmoothButton = new GridBagConstraints();
        gbc_btnSmoothButton.anchor = GridBagConstraints.CENTER;
        gbc_btnSmoothButton.insets = new Insets(0, 0, 3, 3);
        gbc_btnSmoothButton.gridx = 8;
        gbc_btnSmoothButton.gridy = 0;
        btnSmoothButton.setActionCommand("smooth");
        btnSmoothButton.addActionListener(this);
        cntlPanel.add(btnSmoothButton, gbc_btnSmoothButton);

        btnResetSmoothButton = new JButton("Reset");
        GridBagConstraints gbc_btnResetSmoothButton = new GridBagConstraints();
        gbc_btnResetSmoothButton.anchor = GridBagConstraints.CENTER;
        gbc_btnResetSmoothButton.insets = new Insets(0, 0, 3, 3);
        gbc_btnResetSmoothButton.gridx = 9;
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
					private static final long serialVersionUID = -4045338273187150888L;
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
            private static final long serialVersionUID = -7484222189491449568L;
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
    	final Desktop desktop = Desktop.getDesktop();
        JTextPane  usageTextArea = new JTextPane();
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

    //////////////////////////////////////////////////////////////////////////////////////
    // COMMOMN ACTIONS RELATED FUNCTIONS
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("clearmaf".equals(e.getActionCommand())) {
            clearMafTable();
        }
        else if ("clearrun".equals(e.getActionCommand())) {
            clearRunTables();
        }
        else if ("clearall".equals(e.getActionCommand())) {
            clearMafTable();
            clearRunTables();
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
        else if ("mafrpm".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected()) {
            	clearNotRunDataCheckboxes();
                if (!plotMafVRpmData())
                    checkBox.setSelected(false);
            }
            else
                runData.clear();
            setRanges();
        }
        else if ("rdata".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected()) {
            	if (checkBoxMafRpmData.isSelected()) {
            		checkBoxMafRpmData.setSelected(false);
            		runData.clear();
            	}
                if (!plotRunData())
                    checkBox.setSelected(false);
            }
            else
                runData.clear();
            setRanges();
        }
        else if ("current".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected()) {
            	if (checkBoxMafRpmData.isSelected()) {
            		checkBoxMafRpmData.setSelected(false);
            		runData.clear();
            	}
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
            	if (checkBoxMafRpmData.isSelected()) {
            		checkBoxMafRpmData.setSelected(false);
            		runData.clear();
            	}
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
            	if (checkBoxMafRpmData.isSelected()) {
            		checkBoxMafRpmData.setSelected(false);
            		runData.clear();
            	}
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
    
    private void clearRunTables() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
	        for (int i = 0; i < runTables.length; ++i) {
	            while (RunRowsCount < runTables[i].getRowCount())
	                Utils.removeRow(RunRowsCount, runTables[i]);
	            Utils.clearTable(runTables[i]);
	        }
        }
        finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    private void clearData() {
    	rpmArray.clear();
    	mafvArray.clear();
    	afrArray.clear();
        voltArray.clear();
        gsArray.clear();
        gsCorrected.clear();
        smoothGsArray.clear();
    }
    
    private void clearChartData() {
        runData.clear();
        currMafData.clear();
        corrMafData.clear();
        smoothMafData.clear();
    }
    
    private void clearChartCheckBoxes() {
    	checkBoxMafRpmData.setSelected(false);
        checkBoxRunData.setSelected(false);
        checkBoxCurrentMaf.setSelected(false);
        checkBoxCorrectedMaf.setSelected(false);
        checkBoxSmoothedMaf.setSelected(false);
    }
    
    private void calculateMafScaling() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            clearData();
            clearChartData();
            clearChartCheckBoxes();

            TreeMap<Integer, ArrayList<Double>> result = new TreeMap<Integer, ArrayList<Double>>();
            if (!getMafTableData(voltArray, gsArray))
                return;
            if (!sortRunData(result) || result.isEmpty())
                return;
            calculateCorrectedGS(result);
            setCorrectedMafData();
            
            smoothGsArray.addAll(gsCorrected);
            checkBoxCorrectedMaf.setSelected(true);
            
            setXYTable(mafSmoothingTable, voltArray, smoothGsArray);

            setRanges();
            setSelectedIndex(1);
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
    
    private boolean sortRunData(TreeMap<Integer, ArrayList<Double>> result) {
        int closestVoltIdx;
        double rpm;
        double voltage;
        double error;
        ArrayList<Double> closestVolatageArray;
        for (int i = 0; i < runTables.length; ++i) {
            JTable table = runTables[i];
            String tableName = RunTableName + (i + 1);
            String rpmValue;
            String mafvValue;
            String afrValue;
            for (int j = 0; j < table.getRowCount(); ++j) {
                rpmValue = table.getValueAt(j, 0).toString();
                mafvValue = table.getValueAt(j, 1).toString();
                afrValue = table.getValueAt(j, 2).toString();
                if (rpmValue.isEmpty() || mafvValue.isEmpty() || afrValue.isEmpty())
                    continue;
                if (!Utils.validateDouble(rpmValue, j, 0, tableName) ||
                	!Utils.validateDouble(mafvValue, j, 1, tableName) ||
                	!Utils.validateDouble(afrValue, j, 2, tableName))
                    return false;
                rpm = Double.parseDouble(rpmValue);
                voltage = Double.parseDouble(mafvValue);
                error = Double.parseDouble(afrValue);
                rpmArray.add(rpm);
                mafvArray.add(voltage);
                afrArray.add(error);
                closestVoltIdx = Utils.closestValueIndex(voltage, voltArray);
                closestVolatageArray = result.get(closestVoltIdx);
                if (closestVolatageArray == null) {
                    closestVolatageArray = new ArrayList<Double>();
                    result.put(closestVoltIdx, closestVolatageArray);
                }
                closestVolatageArray.add(error);
            }
        }
        return true;
    }
    
    private void calculateCorrectedGS(TreeMap<Integer, ArrayList<Double>> result) {
        ArrayList<Double> closestVolatageArray;
        double gs = 0;
        double avgError = 0;
        int lastErrIndex = 0;
        int i;
        gsCorrected.addAll(gsArray);
        for (i = 0; i < gsCorrected.size(); ++i) {
            gs = gsCorrected.get(i);
            avgError = 0;
            closestVolatageArray = result.get(i);
            if (closestVolatageArray != null) {
                for (int j = 0; j < closestVolatageArray.size(); ++j)
                    avgError += closestVolatageArray.get(j);
                avgError /= closestVolatageArray.size();
                lastErrIndex = i;
            }
            gsCorrected.set(i, gs * (1 + avgError/100.0));
        }
        avgError = 0;
        ArrayList<Double> sortedAfrArray = result.get(lastErrIndex);
        Collections.sort(sortedAfrArray, Collections.reverseOrder());
        for (i = 0; i < 10 && i < sortedAfrArray.size(); ++i)
        	avgError += sortedAfrArray.get(i);
        if (i > 0)
        	avgError /= i;
        for (i = lastErrIndex + 1; i < gsCorrected.size(); ++i) {
            gs = gsCorrected.get(i);
            gsCorrected.set(i, gs +(gs * 0.01 * avgError));
        }
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

    private boolean plotMafVRpmData() {
    	return setXYSeries(runData, rpmArray, mafvArray);
    }

    private boolean plotRunData() {
    	return setXYSeries(runData, mafvArray, afrArray);
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
    	XYPlot plot = mafChartPanel.getChartPanel().getChart().getXYPlot();
    	plot.getDomainAxis(0).setLabel(XAxisName);
    	plot.getRangeAxis(1).setLabel(Y2AxisName);
    	plot.getRangeAxis(0).setVisible(true);
    	if (checkBoxMafRpmData.isSelected() && checkBoxMafRpmData.isEnabled()) {
        	paddingX = runData.getMaxX() * 0.05;
        	paddingY = runData.getMaxY() * 0.05;
        	plot.getDomainAxis(0).setRange(runData.getMinX() - paddingX, runData.getMaxX() + paddingX);
        	plot.getRangeAxis(1).setRange(runData.getMinY() - paddingY, runData.getMaxY() + paddingY);
        	plot.getRangeAxis(0).setVisible(false);
        	plot.getRangeAxis(1).setLabel(XAxisName);
        	plot.getDomainAxis(0).setLabel(rpmAxisName);
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
        	double minY = Collections.min(Arrays.asList(new Double[] { currMafData.getMinY(), smoothMafData.getMinY(), corrMafData.getMinY() }));
        	paddingX = smoothMafData.getMaxX() * 0.05;
        	paddingY = maxY * 0.05;
        	plot.getDomainAxis(0).setRange(smoothMafData.getMinX() - paddingX, smoothMafData.getMaxX() + paddingX);
        	plot.getRangeAxis(0).setRange(minY - paddingY, maxY + paddingY);
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
        checkBoxMafRpmData.setEnabled(!flag);
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
            if (checkBoxMafRpmData.isSelected())
            	plotMafVRpmData();
            if (checkBoxRunData.isSelected())
                plotRunData();
            if (checkBoxCurrentMaf.isSelected())
                plotCurrentMafData();
            if (checkBoxCorrectedMaf.isSelected())
                setCorrectedMafData();
            if (checkBoxSmoothedMaf.isSelected())
                setSmoothedMafData();
            currMafData.setDescription(currentDataName);
            corrMafData.setDescription(correctedDataName);
            smoothMafData.setDescription(smoothedDataName);
        }
    }
    
    private void smoothReset() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
    	try {
	        if (gsCorrected.size() == 0)
	            return;
	        smoothGsArray.clear();
	        smoothGsArray.addAll(gsCorrected);
	        setXYTable(mafSmoothingTable, voltArray, smoothGsArray);
	        corrMafData.clear();
	        if (!checkBoxMafRpmData.isEnabled() || !checkBoxMafRpmData.isSelected())
	        	setCorrectedMafData();
	        if (checkBoxSmoothing.isSelected())
	            plotSmoothingLineSlopes();
	        else if (checkBoxSmoothedMaf.isSelected())
	            setSmoothedMafData();
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
            // write run data
            for (int t = 0; t < runTables.length; ++t) {
                for (i = 0; i < runTables[t].getColumnCount(); ++i) {
                    for (j = 0; j < runTables[t].getRowCount(); ++j)
                        out.write(runTables[t].getValueAt(j, i).toString() + ",");
                    out.write("\n");
                }
            }
        }
        catch (Exception e) {
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
        int i, j, k, l;
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
            JTable table = null;
            i = k = l = 0;
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
                    int offset = runTables.length * 3 + mafTable.getRowCount();
                    if (i > 1 && i < offset) {
                        if (l == 0 )
                            table = runTables[k++];
                        Utils.ensureRowCount(elements.length - 1, table);
                        for (j = 0; j < elements.length - 1; ++j)
                                table.setValueAt(elements[j], j, l);
                        l += 1;
                        if (l == 3)
                        	l = 0;
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
    
    private boolean getColumnsFilters(String[] elements, boolean isPolSet) {
    	boolean ret = true;
        ArrayList<String> columns = new ArrayList<String>(Arrays.asList(elements));
        String logThtlAngleColName = Config.getThrottleAngleColumnName();
        String logAfLearningColName = Config.getAfLearningColumnName();
        String logAfCorrectionColName = Config.getAfCorrectionColumnName();
        String logMafvColName = Config.getMafVoltageColumnName();
        String logAfrColName = Config.getWidebandAfrColumnName();
        String logRpmColName = Config.getRpmColumnName();
        String logLoadColName = Config.getLoadColumnName();
        String logCommandedAfrColName = Config.getCommandedAfrColumnName();
        logThtlAngleColIdx = columns.indexOf(logThtlAngleColName);
        logAfLearningColIdx = columns.indexOf(logAfLearningColName);
        logAfCorrectionColIdx = columns.indexOf(logAfCorrectionColName);
        logMafvColIdx = columns.indexOf(logMafvColName);
        logAfrColIdx = columns.indexOf(logAfrColName);
        logRpmColIdx = columns.indexOf(logRpmColName);
        logLoadColIdx = columns.indexOf(logLoadColName);
        logCommandedAfrCol = columns.indexOf(logCommandedAfrColName);
        if (logThtlAngleColIdx == -1)    { Config.setThrottleAngleColumnName(Config.NO_NAME); ret = false; }
        if (logAfLearningColIdx == -1)   { Config.setAfLearningColumnName(Config.NO_NAME);    ret = false; }
        if (logAfCorrectionColIdx == -1) { Config.setAfCorrectionColumnName(Config.NO_NAME);  ret = false; }
        if (logMafvColIdx == -1)         { Config.setMafVoltageColumnName(Config.NO_NAME);    ret = false; }
        if (logAfrColIdx == -1)          { Config.setWidebandAfrColumnName(Config.NO_NAME);   ret = false; }
        if (logRpmColIdx == -1)          { Config.setRpmColumnName(Config.NO_NAME);           ret = false; }
        if (logLoadColIdx == -1)         { Config.setLoadColumnName(Config.NO_NAME);          ret = false; }
        if (logCommandedAfrCol == -1)    { Config.setCommandedAfrColumnName(Config.NO_NAME);  if (!isPolSet) ret = false; }
        wotPoint = Config.getWOTStationaryPointValue();
        minMafV = Config.getMafVMinimumValue();
        afrErrPrct = Config.getWidebandAfrErrorPercentValue();
        minWotEnrichment = Config.getWOTEnrichmentValue();
        afrRowOffset = Config.getWBO2RowOffset();
        skipRowsOnTransition = Config.getOLCLTransitionSkipRows();
        return ret;
    }
    
    private void loadLogFile() {
        if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(this))
            return;
        boolean isPolSet = polfTable.isSet();
        File[] files = fileChooser.getSelectedFiles();
        for (File file : files) {
	        BufferedReader br = null;
	        ArrayDeque<String[]> buffer = new ArrayDeque<String[]>();
	        try {
	            br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
	            String line = br.readLine();
	            if (line != null) {
	                String [] elements = line.split(",", -1);
	                getColumnsFilters(elements, false);
	
	                boolean resetColumns = false;
	                if (logThtlAngleColIdx >= 0 || logAfLearningColIdx >= 0 || logAfCorrectionColIdx >= 0 || logMafvColIdx >= 0 ||
	                	logAfrColIdx >= 0 || logRpmColIdx >= 0 || logLoadColIdx >= 0 || logCommandedAfrCol >= 0) {
	                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Would you like to reset column names or filter values?", "Columns/Filters Reset", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE))
	                    	resetColumns = true;
	                }
	                
	                if (resetColumns || logThtlAngleColIdx < 0 || logAfLearningColIdx < 0 || logAfCorrectionColIdx < 0 || logMafvColIdx < 0 ||
	                    	logAfrColIdx < 0 || logRpmColIdx < 0 || logLoadColIdx < 0 || (logCommandedAfrCol < 0 && !isPolSet)) {
	                	ColumnsFiltersSelection selectionWindow = new OLColumnsFiltersSelection(isPolSet);
	                	if (!selectionWindow.getUserSettings(elements) || !getColumnsFilters(elements, isPolSet))
	                		return;
	                }
	                
	                String[] flds;
	                String[] afrflds;
	                boolean wotFlag = true;
	                boolean foundWot = false;
	                double throttle;
	                double stft;
	                double ltft;
	                double afr;
	                double rpm;
	                double load;
	                double mafv;
	                double cmdafr = 0;
	                double afrErr = 0;
	                int skipRowCount = 0;
	                int row = 0;
	                int i = 0;
	                int j = 0;
	                for (; i < runTables.length; ++i) {
	                    if (runTables[i].getValueAt(0, 0).toString().isEmpty())
	                        break;
	                }
	                if (i == runTables.length)
	                    return;
	                setCursor(new Cursor(Cursor.WAIT_CURSOR));
	                for (int k = 0; k <= afrRowOffset && line != null; ++k) {
	                	line = br.readLine();
	                	if (line != null)
	                		buffer.addFirst(line.split(",", -1));
	                }
	                while (line != null && buffer.size() > afrRowOffset) {
	                    afrflds = buffer.getFirst();
	                    flds = buffer.removeLast();
	                    line = br.readLine();
	                	if (line != null)
	                		buffer.addFirst(line.split(",", -1));

	                    try {
		                    throttle = Double.valueOf(flds[logThtlAngleColIdx]);
		                    if (row == 0 && throttle < 99)
		                        wotFlag = false;
		                    if (throttle < wotPoint) {
		                        if (wotFlag == true) {
		                            wotFlag = false;
		                            skipRowCount = 0;
		                            j -= 1;
		                            while (j > 0 && skipRowCount < skipRowsOnTransition) {
			                            runTables[i].setValueAt("", j, 0);
			                            runTables[i].setValueAt("", j, 1);
			                            runTables[i].setValueAt("", j, 2);
			                            skipRowCount += 1;
			                            j -= 1;
		                            }
		                            skipRowCount = 0;
		                        }
		                    }
		                    else {
		                        if (wotFlag == false) {
		                            wotFlag = true;
		                            skipRowCount = 0;
		                            if (foundWot) {
		                                i += 1;
		                                if (i == runTables.length)
		                                    return;
		                            }
		                            if (row > 0)
		                                j = 0;
		                        }
		                        if (skipRowCount >= skipRowsOnTransition) {
			                        mafv = Double.valueOf(flds[logMafvColIdx]);
			                        if (minMafV <= mafv) {
			                            foundWot = true;
			                            stft = Double.valueOf(flds[logAfCorrectionColIdx]);
			                            ltft = Double.valueOf(flds[logAfLearningColIdx]);
			                            afr = Double.valueOf(afrflds[logAfrColIdx]);
			                            rpm = Double.valueOf(flds[logRpmColIdx]);
			                            load = Double.valueOf(flds[logLoadColIdx]);
			
			                            afr = afr / ((100.0 - (ltft + stft)) / 100.0);
			                            
			                            if (logCommandedAfrCol >= 0)
			                            	cmdafr = Double.valueOf(flds[logCommandedAfrCol]);
			                            else if (isPolSet)
			                            	cmdafr = Utils.calculateCommandedAfr(rpm, load, minWotEnrichment, polfTable);
			                            else {
			                            	JOptionPane.showMessageDialog(null, "Please set either \"Commanded AFR\" column or \"Primary Open Loop Fueling\" table", "Error", JOptionPane.ERROR_MESSAGE);
			                            	return;
			                            }

		                            	afrErr = (afr - cmdafr) / cmdafr * 100.0;
			                            if (Math.abs(afrErr) <= afrErrPrct) {
			                            	Utils.ensureRowCount(j + 1, runTables[i]);
				                            runTables[i].setValueAt(rpm, j, 0);
				                            runTables[i].setValueAt(mafv, j, 1);
				                            runTables[i].setValueAt(afrErr, j, 2);
				                            j += 1;
			                            }
			                        }
		                        }
		                        skipRowCount += 1;
		                    }
	                    }
	                    catch (NumberFormatException e) {
	                        logger.error(e);
	                        JOptionPane.showMessageDialog(null, "Error parsing number at " + file.getName() + " line " + (row + 1) + ": " + e, "Error processing file", JOptionPane.ERROR_MESSAGE);
	                        return;
	                    }
	                    row += 1;
	                }

	                if (!foundWot) {
	                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	                    JOptionPane.showMessageDialog(null, "Sorry, no WOT pulls were found in the log file", "No WOT data", JOptionPane.INFORMATION_MESSAGE);
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
        bundle = ResourceBundle.getBundle("com.vgi.mafscaling.open_loop");
        return bundle.getString("usage"); 
    }
}
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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.regex.Pattern;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;

public class BadNoodle extends JTabbedPane implements ActionListener, IMafChartHolder {
    private static final long serialVersionUID = 2988105467764335997L;
    private final static String SaveDataFileHeader = "[badnoodle run data]";
    private final static String MafTableName = "Current MAF Scaling";
    private final static String RunTableName = "Run ";
    private final static String XAxisName = "MAF Sensor (Voltage)";
    private final static String Y1AxisName = "Mass Airflow (g/s)";
    private final static String Y2AxisName = "AFR Error (%)";
    private final static int ColumnWidth = 50;
    private final static int RunCount = 12;
    private final static double SmoothingIncrement = 0.10;
    private int MafTableColumnCount = 50;
    private int RunRowsCount = 200;
    private int logThtlAngleColIdx = -1;
    private int logMafvColIdx = -1;
    private int logAfrColIdx = -1;
    private int logRpmColIdx = -1;
    private int logLoadColIdx = -1;

    private JPanel mafSmoothingPanel = null;
    private JTable mafTable = null;
    private JTable mafSmoothingTable = null;
    private ChartPanel chartPanel = null;
    private JCheckBox checkBoxRunData = null;
    private JCheckBox checkBoxCurrentMaf = null;
    private JCheckBox checkBoxCorrectedMaf = null;
    private JCheckBox checkBoxSmoothedMaf = null;
    private JCheckBox checkBoxSmoothing = null;
    private JComboBox<String> smoothComboBox = null;
    private JButton btnSmoothButton = null;
    private JButton btnResetSmoothButton = null;

    private ArrayList<Double> voltArray = new ArrayList<Double>();
    private ArrayList<Double> gsArray = new ArrayList<Double>();
    private ArrayList<Double> gsCorrected = new ArrayList<Double>();
    private ArrayList<Double> smoothGsArray = new ArrayList<Double>();

    private PrimaryOpenLoopFuelingTable polfTable = new PrimaryOpenLoopFuelingTable();
    private final ExcelAdapter excelAdapter = new ExcelAdapter();
    private final JTable[] runTables = new JTable[RunCount];
    private final JFileChooser fileChooser = new JFileChooser();
    private final XYSeries runData = new XYSeries("AFR Error");
    private final XYSeries currMafData = new XYSeries("Current");
    private final XYSeries corrMafData = new XYSeries("Corrected");
    private final XYSeries smoothMafData = new XYSeries("Smoothed");

    public BadNoodle(int tabPlacement) {
        super(tabPlacement);
        initialize();
    }

    private void initialize() {
        polfTable.setExcelAdapter(excelAdapter);
        createDataTab();
        createGraghTab();
        createUsageTab();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // DATA TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void createDataTab() {
        URL location = BadNoodle.class.getProtectionDomain().getCodeSource().getLocation();
        System.out.println(location.getPath());

        fileChooser.setCurrentDirectory(new File(location.getPath()));
        
        JPanel dataPanel = new JPanel();
        add(dataPanel, "<html><div style='text-align: center;'>D<br>a<br>t<br>a</div></html>");
        GridBagLayout gbl_dataPanel = new GridBagLayout();
        gbl_dataPanel.columnWidths = new int[] {0};
        gbl_dataPanel.rowHeights = new int[] {0, 0, 0, 0};
        gbl_dataPanel.columnWeights = new double[]{1.0};
        gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0,0, 1.0};
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
        excelAdapter.addTable(mafTable, false, true);

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
        gbl_dataRunPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_dataRunPanel.rowHeights = new int[] {0};
        gbl_dataRunPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_dataRunPanel.rowWeights = new double[]{0.0};
        dataRunPanel.setLayout(gbl_dataRunPanel);

        createRunTables(dataRunPanel);
    }
    
    private void createRunTables(JPanel dataRunPanel) {
        for (int i = 0; i < RunCount; ++i) {
            runTables[i] = new JTable();
            JTable table = runTables[i];
            table.getTableHeader().setReorderingAllowed(false);
            table.setModel(new DefaultTableModel(RunRowsCount, 2));
            table.setColumnSelectionAllowed(true);
            table.setCellSelectionEnabled(true);
            table.setBorder(new LineBorder(new Color(0, 0, 0)));
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
            table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            table.getColumnModel().getColumn(0).setHeaderValue("<html><center>Mass<br>Airflow<br>Sensor<br>Voltage</b></center></html>");
            table.getColumnModel().getColumn(1).setHeaderValue("<html><center>AFR<br>Error<br>%</b></center></html>");
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
        gbl_plotPanel.rowHeights = new int[] {0};
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
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0, 0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);
        
        checkBoxRunData = new JCheckBox("AFR Error");
        GridBagConstraints gbc_checkBoxData = new GridBagConstraints();
        gbc_checkBoxData.insets = new Insets(0, 0, 3, 3);
        gbc_checkBoxData.gridx = 0;
        gbc_checkBoxData.gridy = 0;
        checkBoxRunData.setActionCommand("rdata");
        checkBoxRunData.addActionListener(this);
        cntlPanel.add(checkBoxRunData, gbc_checkBoxData);
        
        checkBoxCurrentMaf = new JCheckBox("Current");
        GridBagConstraints gbc_checkBoxCurrentMaf = new GridBagConstraints();
        gbc_checkBoxCurrentMaf.insets = new Insets(0, 0, 3, 3);
        gbc_checkBoxCurrentMaf.gridx = 1;
        gbc_checkBoxCurrentMaf.gridy = 0;
        checkBoxCurrentMaf.setActionCommand("current");
        checkBoxCurrentMaf.addActionListener(this);
        cntlPanel.add(checkBoxCurrentMaf, gbc_checkBoxCurrentMaf);
        
        checkBoxCorrectedMaf = new JCheckBox("Corrected");
        GridBagConstraints gbc_checkBoxCorrectedMaf = new GridBagConstraints();
        gbc_checkBoxCorrectedMaf.insets = new Insets(0, 0, 3, 3);
        gbc_checkBoxCorrectedMaf.gridx = 2;
        gbc_checkBoxCorrectedMaf.gridy = 0;
        checkBoxCorrectedMaf.setActionCommand("corrected");
        checkBoxCorrectedMaf.addActionListener(this);
        cntlPanel.add(checkBoxCorrectedMaf, gbc_checkBoxCorrectedMaf);

        checkBoxSmoothedMaf = new JCheckBox("Smoothed");
        GridBagConstraints gbc_checkBoxSmoothedMaf = new GridBagConstraints();
        gbc_checkBoxSmoothedMaf.insets = new Insets(0, 0, 3, 3);
        gbc_checkBoxSmoothedMaf.gridx = 3;
        gbc_checkBoxSmoothedMaf.gridy = 0;
        checkBoxSmoothedMaf.setActionCommand("smoothed");
        checkBoxSmoothedMaf.addActionListener(this);
        cntlPanel.add(checkBoxSmoothedMaf, gbc_checkBoxSmoothedMaf);

        Component horizontalGlue = Box.createHorizontalGlue();
        GridBagConstraints gbc_horizontalGlue = new GridBagConstraints();
        gbc_horizontalGlue.weightx = 1.0;
        gbc_horizontalGlue.fill = GridBagConstraints.HORIZONTAL;
        gbc_horizontalGlue.insets = new Insets(0, 0, 3, 3);
        gbc_horizontalGlue.gridx = 4;
        gbc_horizontalGlue.gridy = 0;
        cntlPanel.add(horizontalGlue, gbc_horizontalGlue);

        checkBoxSmoothing = new JCheckBox("Smoothing:");
        GridBagConstraints gbc_checkBoxSmoothing = new GridBagConstraints();
        gbc_checkBoxSmoothing.anchor = GridBagConstraints.CENTER;
        gbc_checkBoxSmoothing.insets = new Insets(0, 0, 3, 3);
        gbc_checkBoxSmoothing.gridx = 5;
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
        gbc_smoothComboBox.gridx = 6;
        gbc_smoothComboBox.gridy = 0;
        cntlPanel.add(smoothComboBox, gbc_smoothComboBox);

        btnSmoothButton = new JButton("Apply");
        btnSmoothButton.setEnabled(false);
        GridBagConstraints gbc_btnSmoothButton = new GridBagConstraints();
        gbc_btnSmoothButton.anchor = GridBagConstraints.CENTER;
        gbc_btnSmoothButton.insets = new Insets(0, 0, 3, 3);
        gbc_btnSmoothButton.gridx = 7;
        gbc_btnSmoothButton.gridy = 0;
        btnSmoothButton.setActionCommand("smooth");
        btnSmoothButton.addActionListener(this);
        cntlPanel.add(btnSmoothButton, gbc_btnSmoothButton);

        btnResetSmoothButton = new JButton("Reset");
        GridBagConstraints gbc_btnResetSmoothButton = new GridBagConstraints();
        gbc_btnResetSmoothButton.anchor = GridBagConstraints.CENTER;
        gbc_btnResetSmoothButton.insets = new Insets(0, 0, 3, 3);
        gbc_btnResetSmoothButton.gridx = 8;
        gbc_btnResetSmoothButton.gridy = 0;
        btnResetSmoothButton.setActionCommand("smoothreset");
        btnResetSmoothButton.addActionListener(this);
        cntlPanel.add(btnResetSmoothButton, gbc_btnResetSmoothButton);
        
        JFreeChart chart = ChartFactory.createScatterPlot(null, null, null, null, PlotOrientation.VERTICAL, false, true, false);
        chartPanel = new MafChartPanel(chart, this);
        
        GridBagConstraints gbl_chartPanel = new GridBagConstraints();
        gbl_chartPanel.anchor = GridBagConstraints.PAGE_START;
        gbl_chartPanel.insets = new Insets(3, 3, 3, 3);
        gbl_chartPanel.weightx = 1.0;
        gbl_chartPanel.fill = GridBagConstraints.BOTH;
        gbl_chartPanel.gridx = 0;
        gbl_chartPanel.gridy = 1;
        plotPanel.add(chartPanel, gbl_chartPanel);

        XYDotRenderer dotRenderer = new XYDotRenderer();
        dotRenderer.setSeriesPaint(0, new Color(0, 51, 102));
        dotRenderer.setDotHeight(3);
        dotRenderer.setDotWidth(3);

         XYSplineRenderer lineRenderer = new XYSplineRenderer(3);
        lineRenderer.setUseFillPaint(true);
        lineRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator( 
                StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, 
                new DecimalFormat("0.00"), new DecimalFormat("0.00"))); 
        
        lineRenderer.setSeriesPaint(0, new Color(34, 139, 34));
        lineRenderer.setSeriesPaint(1, new Color(0, 0, 255));
        lineRenderer.setSeriesPaint(2, new Color(201, 0, 0));
        lineRenderer.setSeriesShape(0, ShapeUtilities.createDiamond((float) 2.5));
        lineRenderer.setSeriesShape(1, ShapeUtilities.createUpTriangle((float) 2.5));
        lineRenderer.setSeriesShape(2, ShapeUtilities.createDownTriangle((float) 2.5));

        ValueAxis mafvDomain = new NumberAxis(XAxisName);
        ValueAxis mafgsRange = new NumberAxis(Y1AxisName);
        ValueAxis afrerrRange = new NumberAxis(Y2AxisName);        
        
        XYSeriesCollection scatterDataset = new XYSeriesCollection(runData);
        XYSeriesCollection lineDataset = new XYSeriesCollection();
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
        
        mafSmoothingPanel = new JPanel();
        GridBagLayout gbl_mafSmoothingPanel = new GridBagLayout();
        gbl_mafSmoothingPanel.columnWidths = new int[]{0, 0};
        gbl_mafSmoothingPanel.rowHeights = new int[] {0, 0};
        gbl_mafSmoothingPanel.columnWeights = new double[]{0.0, 1.0};
        gbl_mafSmoothingPanel.rowWeights = new double[]{0.0, 0.0};
        GridBagConstraints gbc_mafSmoothingPanel = new GridBagConstraints();
        gbc_mafSmoothingPanel.ipady = 3;
        gbc_mafSmoothingPanel.anchor = GridBagConstraints.PAGE_START;
        gbc_mafSmoothingPanel.weightx = 1.0;
        gbc_mafSmoothingPanel.insets = new Insets(0, 0, 5, 0);
        gbc_mafSmoothingPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_mafSmoothingPanel.gridx = 0;
        gbc_mafSmoothingPanel.gridy = 2;
        mafSmoothingPanel.setLayout(gbl_mafSmoothingPanel);
        plotPanel.add(mafSmoothingPanel, gbc_mafSmoothingPanel);
        
        JButton btnPlusButton = new JButton("<html><div style='text-align: center; font-weight: bold;'>+</div></html>");
        btnPlusButton.setActionCommand("plus");
        btnPlusButton.addActionListener(this);
        btnPlusButton.setMinimumSize(new Dimension(40, 25));
        GridBagConstraints gbc_btnPlusButton = new GridBagConstraints();
        gbc_btnPlusButton.anchor = GridBagConstraints.PAGE_START;
        gbc_btnPlusButton.insets = new Insets(0, 0, 0, 0);
        gbc_btnPlusButton.weightx = 0;
        gbc_btnPlusButton.weighty = 0;
        gbc_btnPlusButton.gridx = 0;
        gbc_btnPlusButton.gridy = 0;
        mafSmoothingPanel.add(btnPlusButton, gbc_btnPlusButton);

        JButton btnMinusButton = new JButton("<html><div style='text-align: center; font-weight: bold;'>-</div></html>");
        btnMinusButton.setActionCommand("minus");
        btnMinusButton.addActionListener(this);
        btnMinusButton.setMinimumSize(new Dimension(40, 25));
        GridBagConstraints gbc_btnMinusButton = new GridBagConstraints();
        gbc_btnMinusButton.anchor = GridBagConstraints.PAGE_START;
        gbc_btnMinusButton.insets = new Insets(0, 0, 0, 0);
        gbc_btnMinusButton.weightx = 0;
        gbc_btnMinusButton.weighty = 0;
        gbc_btnMinusButton.gridx = 0;
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
        gbc_mafTable.insets = new Insets(0, 0, 0, 0);
        gbc_mafTable.fill = GridBagConstraints.BOTH;
        gbc_mafTable.weightx = 1.0;
        gbc_mafTable.weighty = 1.0;
        gbc_mafTable.gridx = 1;
        gbc_mafTable.gridy = 0;
        gbc_mafTable.gridheight = 2;
        
        JScrollPane mafSmoothingPane = new JScrollPane(mafSmoothingTable);
        mafSmoothingPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        
        mafSmoothingPanel.add(mafSmoothingPane, gbc_mafTable);
        mafSmoothingPanel.setVisible(false);
        excelAdapter.addTable(mafSmoothingTable, false, true, true, true);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE USAGE TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void createUsageTab() {
        JTextPane  usageTextArea = new JTextPane();
        usageTextArea.setContentType("text/html");
        usageTextArea.setText(usage());
        usageTextArea.setEditable(false);

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
        else if ("rdata".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected()) {
                if (!plotRunData())
                    checkBox.setSelected(false);
            }
            else
                runData.clear();
        }
        else if ("current".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected()) {
                if (!plotCurrentMafData())
                    checkBox.setSelected(false);
            }
            else
                currMafData.clear();
        }
        else if ("corrected".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected()) {
                if (!setCorrectedMafData())
                    checkBox.setSelected(false);
            }
            else
                corrMafData.clear();
        }
        else if ("smoothed".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected()) {
                if (!setSmoothedMafData())
                    checkBox.setSelected(false);
            }
            else
                smoothMafData.clear();
        }
        else if ("smoothing".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected())
                enableSmoothingView(true);
            else
                enableSmoothingView(false);
        }
    }
    
    private void clearMafTable() {
        while (MafTableColumnCount < mafTable.getColumnCount())
            Utils.removeColumn(MafTableColumnCount, mafTable);
        Utils.clearTable(mafTable);
    }
    
    private void clearRunTables() {
        for (int i = 0; i < runTables.length; ++i) {
            while (RunRowsCount < runTables[i].getRowCount())
                Utils.removeRow(RunRowsCount, runTables[i]);
            Utils.clearTable(runTables[i]);
        }
    }
    
    private void clearData() {
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
        checkBoxRunData.setSelected(false);
        checkBoxCurrentMaf.setSelected(false);
        checkBoxCorrectedMaf.setSelected(false);
        checkBoxSmoothedMaf.setSelected(false);
    }
    
    private void calculateMafScaling() {
        chartPanel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            clearData();
            clearChartData();
            clearChartCheckBoxes();

            TreeMap<Integer, ArrayList<Double>> result = new TreeMap<Integer, ArrayList<Double>>();
            if (!getMafTableData(voltArray, gsArray))
                return;
            if (!sortRunData(result))
                return;
            calculateCorrectedGS(result);
            setCorrectedMafData();
            
            smoothGsArray.addAll(gsCorrected);
            checkBoxCorrectedMaf.setSelected(true);

            setSelectedIndex(1);
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);    
        }
        finally {
            chartPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    private boolean sortRunData(TreeMap<Integer, ArrayList<Double>> result) {
        int closestVoltIdx;
        double voltage;
        double error;
        ArrayList<Double> closestVolatageArray;
        for (int i = 0; i < runTables.length; ++i) {
            JTable table = runTables[i];
            String tableName = RunTableName + (i + 1);
            String mafvValue;
            String afrValue;
            for (int j = 0; j < table.getRowCount(); ++j) {
                mafvValue = table.getValueAt(j, 0).toString();
                afrValue = table.getValueAt(j, 1).toString();
                if (mafvValue.isEmpty() || afrValue.isEmpty())
                    continue;
                if (!Utils.validateDouble(mafvValue, j, 0, tableName) || !Utils.validateDouble(afrValue, j, 1, tableName))
                    return false;
                voltage = Double.parseDouble(mafvValue);
                error = Double.parseDouble(afrValue);
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
        double gs;
        double avgError;
        gsCorrected.addAll(gsArray);
        for (int i = 0; i < gsCorrected.size(); ++i) {
            gs = gsCorrected.get(i);
            avgError = 0;
            closestVolatageArray = result.get(i);
            if (closestVolatageArray != null) {
                for (int j = 0; j < closestVolatageArray.size(); ++j)
                    avgError += closestVolatageArray.get(j);
                avgError /= closestVolatageArray.size();
            }
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

    private boolean plotRunData() {
        ArrayList<Double> xarr = new ArrayList<Double>();
        ArrayList<Double> yarr = new ArrayList<Double>();
        for (int i = 0; i < runTables.length; ++i) {
            JTable table = runTables[i];
            String tableName = RunTableName + (i + 1);
            String mafvValue;
            String afrValue;
            for (int j = 0; j < table.getRowCount(); ++j) {
                mafvValue = table.getValueAt(j, 0).toString();
                afrValue = table.getValueAt(j, 1).toString();
                if (mafvValue.isEmpty() || afrValue.isEmpty())
                    continue;
                if (!Utils.validateDouble(mafvValue, j, 0, tableName) || !Utils.validateDouble(afrValue, j, 1, tableName))
                    return false;
                xarr.add(Double.parseDouble(mafvValue));
                yarr.add(Double.parseDouble(afrValue));
            }
        }
        return setXYSeries(runData, xarr, yarr);
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
        currMafData.clear();
        for (int i = 0; i < xarr.size(); ++i)
            currMafData.add(xarr.get(i), yarr.get(i));
        return true;
    }

    private boolean setXYSeries(XYSeries series, ArrayList<Double> xarr, ArrayList<Double> yarr) {
        if (xarr.size() == 0 || yarr.size() == 0 || xarr.size() != yarr.size())
            return false;
        series.clear();
        for (int i = 0; i < xarr.size(); ++i)
            series.add(xarr.get(i), yarr.get(i));
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
    
    public void onMovePoint(int itemIndex, double valueY) {
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
        checkBoxRunData.setEnabled(!flag);
        checkBoxCurrentMaf.setEnabled(!flag);
        checkBoxCorrectedMaf.setEnabled(!flag);
        checkBoxSmoothedMaf.setEnabled(!flag);
        clearChartData();
        if (flag == true) {
            mafSmoothingPanel.setVisible(true);
            plotSmoothingLineSlopes();
        }
        else {
            mafSmoothingPanel.setVisible(false);
            if (checkBoxRunData.isSelected())
                plotRunData();
            if (checkBoxCurrentMaf.isSelected())
                plotCurrentMafData();
            if (checkBoxCorrectedMaf.isSelected())
                setCorrectedMafData();
            if (checkBoxSmoothedMaf.isSelected())
                setSmoothedMafData();
        }
    }
    
    private void smoothReset() {
        if (gsCorrected.size() == 0)
            return;
        smoothGsArray.clear();
        smoothGsArray.addAll(gsCorrected);
        if (checkBoxSmoothing.isSelected())
            plotSmoothingLineSlopes();
        else if (checkBoxSmoothedMaf.isSelected())
            setSmoothedMafData();
    }
    
    public void changeMafSmoothingCellValue(boolean add) {
        if (mafSmoothingTable.getSelectedRows() == null || mafSmoothingTable.getSelectedColumns() == null)
            return;
        int[] rows = mafSmoothingTable.getSelectedRows();
        if (rows.length == 1 && rows[0] == 0)
            return;
        int[] cols = mafSmoothingTable.getSelectedColumns();
        double val;
        for (int col : cols) {
            if (!Pattern.matches(Utils.fpRegex, mafSmoothingTable.getValueAt(1, col).toString()))
                continue;
            val = (Double)(mafSmoothingTable.getValueAt(1, col)) + (add == true ? SmoothingIncrement : SmoothingIncrement * -1.0); 
            mafSmoothingTable.setValueAt(val, 1, col);
            smoothGsArray.set(col, val);
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
        int movWind;
        ArrayList<Double> tmpArray = new ArrayList<Double>();
        int degree = Integer.parseInt(smoothComboBox.getSelectedItem().toString().trim());
        if (degree == 3) {
            movWind = 1;
            for (int i = movWind; i + movWind < smoothGsArray.size(); ++i)
                tmpArray.add(0.25 * smoothGsArray.get(i - 1) + 
                             0.5  * smoothGsArray.get(i) + 
                             0.25 * smoothGsArray.get(i + 1));
            for (int i = 0; i < tmpArray.size(); ++i)
                smoothGsArray.set(i + movWind, tmpArray.get(i));
        }
        if (degree == 5) {
            movWind = 2;
            for (int i = movWind; i + movWind < smoothGsArray.size(); ++i)
                tmpArray.add(0.1 * smoothGsArray.get(i - 2) + 
                             0.2 * smoothGsArray.get(i - 1) + 
                             0.4 * smoothGsArray.get(i) + 
                             0.2 * smoothGsArray.get(i + 1) +
                             0.1 * smoothGsArray.get(i + 2));
            for (int i = movWind; i < tmpArray.size(); ++i)
                smoothGsArray.set(i + movWind, tmpArray.get(i));
        }
        if (degree == 7) {
            movWind = 3;
            for (int i = movWind; i + movWind < smoothGsArray.size(); ++i)
                tmpArray.add(0.05 * smoothGsArray.get(i - 3) +
                             0.1  * smoothGsArray.get(i - 2) + 
                             0.15 * smoothGsArray.get(i - 1) + 
                             0.4  * smoothGsArray.get(i) + 
                             0.15 * smoothGsArray.get(i + 1) +
                             0.1  * smoothGsArray.get(i + 2) +
                             0.05 * smoothGsArray.get(i + 3));
            for (int i = movWind; i < tmpArray.size(); ++i)
                smoothGsArray.set(i + movWind, tmpArray.get(i));
        }
        plotSmoothingLineSlopes();
    }
   
    public void saveData() {
        if (JFileChooser.APPROVE_OPTION != fileChooser.showSaveDialog(this))
            return;
        File file = fileChooser.getSelectedFile();
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        int i, j;
        try {
            FileWriter out = new FileWriter(file);
            try {
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
                // write fueling data
                polfTable.write(out);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    out.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
   
    public void loadData() {
        if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(this))
            return;
        File file = fileChooser.getSelectedFile();
        int i, j, k;
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            BufferedReader br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
            try {
                String line = br.readLine();
                if (line == null || !line.equals(SaveDataFileHeader)) {
                    JOptionPane.showMessageDialog(null, "Invalid saved data file!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                line = br.readLine();
                String[] elements;
                JTable table = null;
                i = k = 0;
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
                        int offset = runTables.length * 2 + mafTable.getRowCount();
                        if (i > 1 && i < offset) {
                            if ( (i & 1) == 0 ) {
                                table = runTables[k++];
                                Utils.ensureRowCount(elements.length - 1, table);
                                for (j = 0; j < elements.length - 1; ++j)
                                    table.setValueAt(elements[j], j, 0);
                            }
                            else  {
                                Utils.ensureRowCount(elements.length - 1, table);
                                for (j = 0; j < elements.length - 1; ++j)
                                    table.setValueAt(elements[j], j, 1);
                            }
                        }
                        else {
                            polfTable.setValueAtRow(i - offset, elements);
                        }
                    }
                    i += 1;
                    line = br.readLine();
                }
                polfTable.validate();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    br.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadLogFile() {
        if (!polfTable.validate())
            return;
        if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(this))
            return;
        File file = fileChooser.getSelectedFile();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
            try {
                String line = br.readLine();
                if (line != null) {
                    String [] elements = line.split(",", -1);
                    JTable table = new JTable() {
                        private static final long serialVersionUID = 1L;
                        public boolean isCellEditable(int row, int column) { return false; };
                    };
                    table.setColumnSelectionAllowed(false);
                    table.setCellSelectionEnabled(true);
                    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    table.setBorder(new LineBorder(new Color(0, 0, 0)));
                    table.setTableHeader(null);
                    table.setModel(new DefaultTableModel(elements.length, 1));
                    for (int i = 0; i < elements.length; ++i)
                        table.setValueAt(elements[i], i, 0);
                    JComponent[] inputs = new JComponent[] { new JScrollPane(table) };
                    
                    if (logThtlAngleColIdx >= 0)
                        table.changeSelection(logThtlAngleColIdx, 0, false, false);
                    else
                    	table.clearSelection();
                    if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null, inputs, "Select Throttle Angle % Column", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE))
                        return;
                    logThtlAngleColIdx = table.getSelectedRow();
                    if (logThtlAngleColIdx == -1) {
                        JOptionPane.showMessageDialog(null, "Invalid Throttle Angle % Column selection", "Invalid selection", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (logMafvColIdx >= 0)
                        table.changeSelection(logMafvColIdx, 0, false, false);
                    else
                    	table.clearSelection();
                    if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null, inputs, "Select Maf Voltage Column", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE))
                        return;
                    logMafvColIdx = table.getSelectedRow();
                    if (logMafvColIdx == -1) {
                        JOptionPane.showMessageDialog(null, "Invalid Maf Voltage Column selection", "Invalid selection", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (logAfrColIdx >= 0)
                        table.changeSelection(logAfrColIdx, 0, false, false);
                    else
                    	table.clearSelection();
                    if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null, inputs, "Select Wideband AFR Column", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE))
                        return;
                    logAfrColIdx = table.getSelectedRow();
                    if (logAfrColIdx == -1) {
                        JOptionPane.showMessageDialog(null, "Invalid AFR Column selection", "Invalid selection", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (logRpmColIdx >= 0)
                        table.changeSelection(logRpmColIdx, 0, false, false);
                    else
                    	table.clearSelection();
                    if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null, inputs, "Select Engine Speed Column", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE))
                        return;
                    logRpmColIdx = table.getSelectedRow();
                    if (logRpmColIdx == -1) {
                        JOptionPane.showMessageDialog(null, "Invalid Engine Speed Column selection", "Invalid selection", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (logLoadColIdx >= 0)
                        table.changeSelection(logLoadColIdx, 0, false, false);
                    else
                    	table.clearSelection();
                    if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null, inputs, "Select Engine Load Column", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE))
                        return;
                    logLoadColIdx = table.getSelectedRow();
                    if (logLoadColIdx == -1) {
                        JOptionPane.showMessageDialog(null, "Invalid Engine Load Column selection", "Invalid selection", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String[] flds;
                    int row = 0;
                    boolean wotFlag = true;
                    line = br.readLine();
                    double throttle;
                    double afr;
                    double rpm;
                    double load;
                    double mafv;
                    double afrErr;
                    int i = 0;
                    for (; i < runTables.length; ++i) {
                        if (runTables[i].getValueAt(0, 0).toString().isEmpty())
                            break;
                    }
                    if (i == runTables.length) {
                        JOptionPane.showMessageDialog(null, "Sorry, currently only " + runTables.length + " runs are supported", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    int j = 0;
                    boolean foundWot = false;
                    while (line != null) {
                        flds = line.split(",", -1);
                        throttle = Double.valueOf(flds[logThtlAngleColIdx]);
                        if (row == 0 && throttle < 99)
                            wotFlag = false;
                        if (throttle < 85) {
                            if (wotFlag == true)
                                wotFlag = false;
                        }
                        else {
                            if (wotFlag == false) {
                                wotFlag = true;
                                if (foundWot) {
                                    i += 1;
                                    if (i == runTables.length)
                                        return;
                                }
                                if (row > 0)
                                    j = 0;
                            }
                            foundWot = true;
                            mafv = Double.valueOf(flds[logMafvColIdx]);
                            afr = Double.valueOf(flds[logAfrColIdx]);
                            rpm = Double.valueOf(flds[logRpmColIdx]);
                            load = Double.valueOf(flds[logLoadColIdx]);
                            afrErr = calculateAfrError(rpm, load, afr);
                            runTables[i].setValueAt(mafv, j, 0);
                            runTables[i].setValueAt(afrErr, j, 1);
                            j += 1;
                        }
                        row += 1;
                        line = br.readLine();
                    }
                    if (!foundWot)
                        JOptionPane.showMessageDialog(null, "Sorry, no WOT pulls were found in the log file", "No WOT data", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error opening file", JOptionPane.ERROR_MESSAGE);
            }
            finally {
                try {
                    br.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Error: file " + file.getAbsoluteFile() + " not found", "File not found", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private double calculateAfrError(double rpm, double load, double afr) {
        double rpmLow = 0;
        double rpmHigh = 0;
        double loadLow = 0;
        double loadHigh = 0;
        double timingLowLow = 0;
        double timingLowHigh = 0;
        double timingHighLow = 0;
        double timingHighHigh = 0;
        int rpmRowLow = 0;
        int rpmRowHigh = 0;
        int loadColLow = 0;
        int loadColHigh = 0;
        int index = 1;
        double value;

        // Get values for RPM
        for (index = 1; index < polfTable.getRowCount(); ++index) {
            value = Double.valueOf(polfTable.getValueAt(index, 0).toString());
            if (rpm < value) {
                rpmHigh = value;
                rpmRowHigh = index;
                break;
            }
            else if (rpm == value) {
                rpmLow = rpmHigh = value;
                rpmRowLow = rpmRowHigh = index;
                break;
            }
            else {
                rpmLow = value;
                rpmRowLow = index;
            }
        }
        if (index == polfTable.getRowCount()) {
            rpmHigh = 10000;
            rpmRowHigh = index - 1;
        }
        // Get values for Load
        for (index = 1; index < polfTable.getColumnCount(); ++index) {
            value = Double.valueOf(polfTable.getValueAt(0, index).toString());
            if (load < value) {
                loadHigh = value;
                loadColHigh = index;
                break;
            }
            else if (rpm == value) {
                loadLow = loadHigh = value;
                loadColLow = loadColHigh = index;
                break;
            }
            else {
                loadLow = value;
                loadColLow = index;
            }
        }
        if (index == polfTable.getColumnCount()) {
            loadHigh = 10000;
            loadColHigh = index - 1;
        }
        timingLowLow = Double.valueOf(polfTable.getValueAt(rpmRowLow, loadColLow).toString());
        timingLowHigh = Double.valueOf(polfTable.getValueAt(rpmRowLow, loadColHigh).toString());
        timingHighLow = Double.valueOf(polfTable.getValueAt(rpmRowHigh, loadColLow).toString());
        timingHighHigh = Double.valueOf(polfTable.getValueAt(rpmRowHigh, loadColHigh).toString());

        double temp1;
        double temp2;
        double interpolatedTiming;
        if (rpmHigh == rpmLow) {
            temp1 = timingLowLow;
            temp2 = timingLowHigh;
        }
        else {
            temp1 = (rpm - rpmLow) * (timingHighLow - timingLowLow) / (rpmHigh - rpmLow) + timingLowLow;
            temp2 = (rpm - rpmLow) * (timingHighHigh - timingLowHigh) / (rpmHigh - rpmLow) + timingLowHigh;
        }
        if (loadHigh == loadLow)
            interpolatedTiming = temp1;
        else
            interpolatedTiming = (load - loadLow) * (temp2 - temp1) / (loadHigh - loadLow) + temp1;
        return ((afr - interpolatedTiming) / interpolatedTiming * 100.0);
    }
    
    private String usage() {
        ResourceBundle bundle;
        bundle = ResourceBundle.getBundle("com.vgi.mafscaling.badnoodle");
        return bundle.getString("usage"); 
    }
}
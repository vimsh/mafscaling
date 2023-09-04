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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

import ij.measure.CurveFitter;

public class MafOLCLMerge extends JTabbedPane implements IMafChartHolder {
    private static final long serialVersionUID = 5259143509677906272L;
    private static final Logger logger = Logger.getLogger(MafOLCLMerge.class);
    private static final int ColumnWidth = 50;
    private static final String OLMafTableName = "Open Loop MAF Scaling";
    private static final String CLMafTableName = "Closed Loop MAF Scaling";
    private static final String MergedMafTableName = "Merged MAF Scaling";
    private static final String XAxisName = "MAF Sensor (Voltage)";
    private static final String YAxisName = "Mass Airflow (g/s)";
    private static final String OLDataName = "Open Loop";
    private static final String CLDataName = "Closed Loop";
    private static final String mergedDataName = "Merged";
    private static final String AutoSelect = "Auto Select";
    private final XYSeries olMafData = new XYSeries(OLDataName);
    private final XYSeries clMafData = new XYSeries(CLDataName);
    private final XYSeries mergedMafData = new XYSeries(mergedDataName);
    private JTable olMafTable = null;
    private JTable clMafTable = null;
    private JTable mergedMafTable = null;
    private JFormattedTextField clMaxVTextBox = null;
    private JFormattedTextField olMinVTextBox = null;
    private JComboBox<String> algoComboBox = null;
    private Insets insets0 = new Insets(0, 0, 0, 0);
    private Insets insets3 = new Insets(3, 3, 3, 3);
    
    private MafChartPanel mafChartPanel = null;
    private ExcelAdapter olExcelAdapter = null;
    private ExcelAdapter clExcelAdapter = null;
    private ExcelAdapter mergedMafExcelAdapter = null;
    private ArrayList<Double> olVoltArray = null;
    private ArrayList<Double> olGsArray = null;
    private ArrayList<Double> clVoltArray = null;
    private ArrayList<Double> clGsArray = null;
    private ArrayList<Double> avgGsArray = null;
    
    public MafOLCLMerge(int tabPlacement) {
        super(tabPlacement);
        olExcelAdapter = new ExcelAdapter() {
            protected void onPaste(JTable table, boolean extendRows, boolean extendCols) {
                super.onPaste(table, extendRows, extendCols);
                loadTableData(table);
                updateMergedMafScale();
            }
            protected void onPasteVertical(JTable table, boolean extendRows, boolean extendCols) {
                super.onPasteVertical(table, extendRows, extendCols);
                loadTableData(table);
                updateMergedMafScale();
            }
            protected void onClearSelection(JTable table) {
                super.onClearSelection(table);
                updateMergedMafScale();
            }
        };
        clExcelAdapter = new ExcelAdapter() {
            protected void onPaste(JTable table, boolean extendRows, boolean extendCols) {
                super.onPaste(table, extendRows, extendCols);
                loadTableData(table);
                updateMergedMafScale();
            }
            protected void onPasteVertical(JTable table, boolean extendRows, boolean extendCols) {
                super.onPasteVertical(table, extendRows, extendCols);
                loadTableData(table);
                updateMergedMafScale();
            }
            protected void onClearSelection(JTable table) {
                super.onClearSelection(table);
                updateMergedMafScale();
            }
        };
        mergedMafExcelAdapter = new ExcelAdapter();
        initialize();
    }

    private void initialize() {
        createDataTab();
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
        gbl_dataPanel.rowHeights = new int[] {0, 0, 0};
        gbl_dataPanel.columnWeights = new double[]{0.0};
        gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 1.0};
        dataPanel.setLayout(gbl_dataPanel);

        createControlPanel(dataPanel);
        createMafScalesScrollPane(dataPanel);
        createGraghPanel(dataPanel);
    }
    
    private void createControlPanel(JPanel dataPanel) {
        JPanel cntlPanel = new JPanel();
        GridBagConstraints gbl_ctrlPanel = new GridBagConstraints();
        gbl_ctrlPanel.insets = insets3;
        gbl_ctrlPanel.anchor = GridBagConstraints.PAGE_START;
        gbl_ctrlPanel.fill = GridBagConstraints.HORIZONTAL;
        gbl_ctrlPanel.weightx = 1.0;
        gbl_ctrlPanel.gridx = 0;
        gbl_ctrlPanel.gridy = 0;
        dataPanel.add(cntlPanel, gbl_ctrlPanel);
        
        GridBagLayout gbl_cntlPanel = new GridBagLayout();
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0, 0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);
        
        NumberFormat doubleFmt = NumberFormat.getNumberInstance();
        doubleFmt.setGroupingUsed(false);
        doubleFmt.setMaximumIntegerDigits(5);
        doubleFmt.setMinimumIntegerDigits(1);
        doubleFmt.setMaximumFractionDigits(3);
        doubleFmt.setMinimumFractionDigits(1);
        doubleFmt.setRoundingMode(RoundingMode.HALF_UP);
        
        NumberFormat scaleDoubleFmt = NumberFormat.getNumberInstance();
        scaleDoubleFmt.setGroupingUsed(false);
        scaleDoubleFmt.setMaximumIntegerDigits(5);
        scaleDoubleFmt.setMinimumIntegerDigits(1);
        scaleDoubleFmt.setMaximumFractionDigits(8);
        scaleDoubleFmt.setMinimumFractionDigits(1);
        scaleDoubleFmt.setRoundingMode(RoundingMode.HALF_UP);

        GridBagConstraints gbc_cntlPanelLabel = new GridBagConstraints();
        gbc_cntlPanelLabel.anchor = GridBagConstraints.EAST;
        gbc_cntlPanelLabel.insets = new Insets(2, 3, 2, 1);
        gbc_cntlPanelLabel.gridx = 0;
        gbc_cntlPanelLabel.gridy = 0;

        GridBagConstraints gbc_cntlPanelInput = new GridBagConstraints();
        gbc_cntlPanelInput.anchor = GridBagConstraints.WEST;
        gbc_cntlPanelInput.insets = new Insets(2, 1, 2, 3);
        gbc_cntlPanelInput.gridx = 1;
        gbc_cntlPanelInput.gridy = 0;
        
        cntlPanel.add(new JLabel("CL Max V"), gbc_cntlPanelLabel);
    
        clMaxVTextBox = new JFormattedTextField(doubleFmt);
        clMaxVTextBox.setColumns(7);
        clMaxVTextBox.setValue(2.1);
        clMaxVTextBox.addPropertyChangeListener("value", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                Object source = e.getSource();
                if (source == clMaxVTextBox)
                    updateMergedMafScale();
            }
        });
        cntlPanel.add(clMaxVTextBox, gbc_cntlPanelInput);

        gbc_cntlPanelLabel.gridx += 2;
        cntlPanel.add(new JLabel("OL Min V"), gbc_cntlPanelLabel);
        
        olMinVTextBox = new JFormattedTextField(doubleFmt);
        olMinVTextBox.setColumns(7);
        olMinVTextBox.setValue(2.9);
        olMinVTextBox.addPropertyChangeListener("value", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                Object source = e.getSource();
                if (source == olMinVTextBox)
                    updateMergedMafScale();
            }
        });
        gbc_cntlPanelInput.gridx += 2;
        cntlPanel.add(olMinVTextBox, gbc_cntlPanelInput);

        gbc_cntlPanelLabel.gridx += 2;
        cntlPanel.add(new JLabel("Fitting Algo"), gbc_cntlPanelLabel);
        
        ArrayList<String> fitList = new ArrayList<String>();
        fitList.addAll(Arrays.asList(CurveFitter.fitList));
        fitList.remove("Log");
        fitList.remove("y = a+b*ln(x-c)");
        fitList.remove("Straight Line");
        fitList.remove("Exponential (linear regression)");
        fitList.remove("Rodbard (NIH Image)");
        Collections.sort(fitList);
        fitList.add(0, AutoSelect);
        String[] algoList = new String[fitList.size()];
        for (int i = 0; i < algoList.length; ++i) {
            algoList[i] = fitList.get(i);
        }
        algoComboBox = new JComboBox<String>(algoList);
        algoComboBox.setSelectedIndex(0);
        algoComboBox.addActionListener(new ActionListener () {
            public void actionPerformed(ActionEvent e) {
                updateMergedMafScale();
            }
        });
        gbc_cntlPanelInput.gridx += 2;
        cntlPanel.add(algoComboBox, gbc_cntlPanelInput);
    }

    private void createMafScalesScrollPane(JPanel dataPanel) {
        JPanel mafPanel = new JPanel();
        GridBagLayout gbl_mafPanelLayout = new GridBagLayout();
        gbl_mafPanelLayout.columnWidths = new int[]{0};
        gbl_mafPanelLayout.rowHeights = new int[]{0, 0, 0};
        gbl_mafPanelLayout.columnWeights = new double[]{1.0};
        gbl_mafPanelLayout.rowWeights = new double[]{0.0, 0.0, 1.0};
        mafPanel.setLayout(gbl_mafPanelLayout);
        
        GridBagConstraints gbc_mafScrollPane = new GridBagConstraints();
        gbc_mafScrollPane.anchor = GridBagConstraints.PAGE_START;
        gbc_mafScrollPane.weightx = 1.0;
        gbc_mafScrollPane.insets = insets0;
        gbc_mafScrollPane.fill = GridBagConstraints.HORIZONTAL;
        gbc_mafScrollPane.gridx = 0;
        gbc_mafScrollPane.gridy = 0;
        
        MafTablePane olMafScrollPane = new MafTablePane(ColumnWidth, OLMafTableName, false, false);
        olMafScrollPane.setMaximumSize(new Dimension(10000, 65));
        olMafScrollPane.setMinimumSize(new Dimension(10000, 65));
        olMafScrollPane.setPreferredSize(new Dimension(10000, 65));
        olMafScrollPane.setBorder(null);
        olMafScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        olMafTable = olMafScrollPane.getJTable();
        olExcelAdapter.addTable(olMafTable, false, false, false, false, false, false, false, false, true);
        mafPanel.add(olMafScrollPane, gbc_mafScrollPane);
        
        MafTablePane clMafScrollPane = new MafTablePane(ColumnWidth, CLMafTableName, false, false);
        clMafScrollPane.setMaximumSize(new Dimension(10000, 65));
        clMafScrollPane.setMinimumSize(new Dimension(10000, 65));
        clMafScrollPane.setPreferredSize(new Dimension(10000, 65));
        clMafScrollPane.setBorder(null);
        clMafScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        clMafTable = clMafScrollPane.getJTable();
        clExcelAdapter.addTable(clMafTable, false, false, false, false, false, false, false, false, true);
        gbc_mafScrollPane.gridy++;
        mafPanel.add(clMafScrollPane, gbc_mafScrollPane);
        
        MafTablePane mergedMafScrollPane = new MafTablePane(ColumnWidth, MergedMafTableName, false, false);
        mergedMafScrollPane.setMaximumSize(new Dimension(10000, 65));
        mergedMafScrollPane.setMinimumSize(new Dimension(10000, 65));
        mergedMafScrollPane.setPreferredSize(new Dimension(10000, 65));
        mergedMafScrollPane.setBorder(null);
        mergedMafScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mergedMafTable = mergedMafScrollPane.getJTable();
        mergedMafExcelAdapter.addTable(mergedMafTable, false, false, true, false, false, true, false, false, true);
        gbc_mafScrollPane.gridy++;
        mafPanel.add(mergedMafScrollPane, gbc_mafScrollPane);
        
        JScrollPane mafScrollPane = new JScrollPane(mafPanel);
        mafScrollPane.setMaximumSize(new Dimension(10000, 215));
        mafScrollPane.setMinimumSize(new Dimension(10000, 215));
        mafScrollPane.setPreferredSize(new Dimension(10000, 215));
        mafScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        mafScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        GridBagConstraints gbl_mafScrollPane = new GridBagConstraints();
        gbl_mafScrollPane.anchor = GridBagConstraints.PAGE_START;
        gbl_mafScrollPane.fill = GridBagConstraints.HORIZONTAL;
        gbl_mafScrollPane.insets = insets0;
        gbl_mafScrollPane.gridx = 0;
        gbl_mafScrollPane.gridy = 1;
        dataPanel.add(mafScrollPane, gbl_mafScrollPane);
    }
    
    private void createGraghPanel(JPanel dataPanel) {
        JFreeChart chart = ChartFactory.createScatterPlot(null, null, null, null, PlotOrientation.VERTICAL, false, true, false);
        chart.setBorderVisible(true);
        mafChartPanel = new MafChartPanel(chart, this);
        
        GridBagConstraints gbl_chartPanel = new GridBagConstraints();
        gbl_chartPanel.anchor = GridBagConstraints.PAGE_START;
        gbl_chartPanel.insets = insets0;
        gbl_chartPanel.fill = GridBagConstraints.BOTH;
        gbl_chartPanel.weightx = 1.0;
        gbl_chartPanel.weighty = 1.0;
        gbl_chartPanel.gridx = 0;
        gbl_chartPanel.gridy = 2;
        dataPanel.add(mafChartPanel.getChartPanel(), gbl_chartPanel);

        XYSplineRenderer lineRenderer = new XYSplineRenderer(3);
        lineRenderer.setUseFillPaint(true);
        lineRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator( 
                StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, 
                new DecimalFormat("0.00"), new DecimalFormat("0.00")));
        
        Stroke stroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, null, 0.0f);
        lineRenderer.setSeriesStroke(0, stroke);
        lineRenderer.setSeriesStroke(1, stroke);
        lineRenderer.setSeriesPaint(0, new Color(201, 0, 0));
        lineRenderer.setSeriesPaint(1, new Color(0, 0, 255));
        lineRenderer.setSeriesShape(0, ShapeUtilities.createDiamond((float) 2.5));
        lineRenderer.setSeriesShape(1, ShapeUtilities.createUpTriangle((float) 2.5));
        
        ValueAxis mafvDomain = new NumberAxis(XAxisName);
        ValueAxis mafgsRange = new NumberAxis(YAxisName);
        
        XYSeriesCollection lineDataset = new XYSeriesCollection();

        lineDataset.addSeries(olMafData);
        lineDataset.addSeries(clMafData);
        lineDataset.addSeries(mergedMafData);
        
        XYPlot plot = chart.getXYPlot();
        plot.setRangePannable(true);
        plot.setDomainPannable(true);
        plot.setDomainGridlinePaint(Color.DARK_GRAY);
        plot.setRangeGridlinePaint(Color.DARK_GRAY);
        plot.setBackgroundPaint(new Color(224, 224, 224));
        plot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);

        plot.setDataset(0, lineDataset);
        plot.setRenderer(0, lineRenderer);
        plot.setDomainAxis(0, mafvDomain);
        plot.setRangeAxis(0, mafgsRange);
        plot.mapDatasetToDomainAxis(0, 0);
        plot.mapDatasetToRangeAxis(0, 0);
        
        LegendTitle legend = new LegendTitle(plot.getRenderer()); 
        legend.setItemFont(new Font("Arial", 0, 10));
        legend.setPosition(RectangleEdge.TOP);
        chart.addLegend(legend);
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
        bundle = ResourceBundle.getBundle("com.vgi.mafscaling.mafolclmerge");
        return bundle.getString("usage"); 
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // WORK FUNCTIONS
    //////////////////////////////////////////////////////////////////////////////////////

    private boolean getMafTableData(JTable mafTable, ArrayList<Double> voltArray, ArrayList<Double> gsArray) {
        String value;
        for (int i = 0; i < mafTable.getColumnCount(); ++i) {
            for (int j = 0; j < mafTable.getRowCount(); ++j) {
                value = mafTable.getValueAt(j, i).toString();
                if (value.isEmpty())
                    return true;
                if (!Utils.validateDouble(value, j, i, mafTable.getName()))
                    return false;
            }
            voltArray.add(Double.parseDouble(mafTable.getValueAt(0, i).toString()));
            gsArray.add(Double.parseDouble(mafTable.getValueAt(1, i).toString()));
        }
        if (voltArray.size() != gsArray.size()) {
            JOptionPane.showMessageDialog(null, "Data sets (volt/gs) in  " + mafTable.getName() + " have different length", "Invalid Data", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    private void loadTableData(JTable table) {
        ArrayList<Double> voltArray = null;
        ArrayList<Double> gsArray = null;
        if (table == olMafTable) {
            voltArray = olVoltArray = new ArrayList<Double>();
            gsArray = olGsArray = new ArrayList<Double>();
        }
        else if (table == clMafTable) {
            voltArray = clVoltArray = new ArrayList<Double>();
            gsArray = clGsArray = new ArrayList<Double>();
        }
        else {
            return;
        }
        
        getMafTableData(table, voltArray, gsArray);
    }
    
    private void updateMergedMafScale() {
        try {
            olMafData.clear();
            clMafData.clear();
            mergedMafData.clear();
            Utils.clearTable(mergedMafTable);
            
            if (clMaxVTextBox.getValue() == null || olMinVTextBox.getValue() == null)
                return;

            if (olVoltArray == null || olGsArray == null || olVoltArray.size() == 0 || olVoltArray.size() != olGsArray.size() ||
                clVoltArray == null || clGsArray == null || clVoltArray.size() == 0 || clVoltArray.size() != clGsArray.size() ||
                olVoltArray.size() != clVoltArray.size()) {
                return;
            }
            
            if (olVoltArray.size() < 10) {
                JOptionPane.showMessageDialog(null, "MAF scale data set should have at least 10 points", "Invalid Data", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            for (int i = 0; i < olVoltArray.size(); ++i) {
                if (olVoltArray.get(i).compareTo(clVoltArray.get(i)) != 0) {
                    JOptionPane.showMessageDialog(null, "MAF voltage values do not match between CL and OL curves at index " + String.valueOf(i), "Invalid Data", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Get average data curve between CL and OL data sets
            double maxCLMafV = (((Number)clMaxVTextBox.getValue()).doubleValue());
            double minOLMafV = (((Number)olMinVTextBox.getValue()).doubleValue());
            
            avgGsArray = new ArrayList<Double>();
            for (int i = 0; i < olVoltArray.size(); ++i) {
                Double v = olVoltArray.get(i);
                if (v.compareTo(maxCLMafV) <= 0) {
                    avgGsArray.add(clGsArray.get(i));
                }
                else if (v.compareTo(minOLMafV) >= 0) {
                    avgGsArray.add(olGsArray.get(i));
                }
                else {
                    avgGsArray.add((olGsArray.get(i) + clGsArray.get(i)) / 2);
                }
            }

            // Run fitting algos
            double[] xarr = new double[olVoltArray.size()];
            for (int i= 0; i < olVoltArray.size(); ++i) {
                xarr[i] = olVoltArray.get(i);
            }
            double[] yarr = new double[avgGsArray.size()];
            for (int i= 0; i < avgGsArray.size(); ++i) {
                yarr[i] = avgGsArray.get(i);
            }
            String algoName = (String) algoComboBox.getSelectedItem();
            if (algoName == AutoSelect) {
                TreeMap<Double, String> goodnessMap = new TreeMap<Double, String>();
                CurveFitter fitter = null;
                for (String name : CurveFitter.fitList) {
                    fitter = new CurveFitter(xarr.clone(), yarr.clone());
                    fitter.doFit(CurveFitter.getFitCode(name));
                    goodnessMap.put(fitter.getFitGoodness(), name);
                }
                algoName = goodnessMap.lastEntry().getValue();
                algoComboBox.setSelectedItem(algoName);
            }

            CurveFitter fitter = new CurveFitter(xarr, yarr);
            fitter.doFit(CurveFitter.getFitCode(algoName));
            ArrayList<Double> mergedGsArray = new ArrayList<Double>();
            for (int i = 0; i < olVoltArray.size(); ++i) {
                mergedGsArray.add(fitter.f(olVoltArray.get(i)));
            }
            
            Utils.ensureColumnCount(olVoltArray.size(), mergedMafTable);
            for (int i = 0; i < olVoltArray.size(); ++i) {
                mergedMafTable.setValueAt(olVoltArray.get(i), 0, i);
                mergedMafTable.setValueAt(mergedGsArray.get(i), 1, i);
            }
            setXYSeries(olMafData, olVoltArray, olGsArray);
            setXYSeries(clMafData, clVoltArray, clGsArray);
            setXYSeries(mergedMafData, olVoltArray, mergedGsArray);
            setRanges(mafChartPanel);
        }
        catch (Exception e) {
            logger.error(e);
        }
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

    private void setRanges(MafChartPanel chartPanel) {
        double maxX = Math.max(olMafData.getMaxX(), clMafData.getMaxX());
        double maxY = Math.max(olMafData.getMaxY(), clMafData.getMaxY());
        double minX = Math.min(olMafData.getMinX(), clMafData.getMinX());
        double minY = Math.min(olMafData.getMinY(), clMafData.getMinY());
        double paddingX = maxX * 0.05;
        double paddingY = maxY * 0.05;
        chartPanel.getChartPanel().getChart().getXYPlot().getDomainAxis(0).setRange(minX - paddingX, maxX + paddingX);
        chartPanel.getChartPanel().getChart().getXYPlot().getRangeAxis(0).setRange(minY - paddingY, maxY + paddingY);
    }

    @Override
    public void onMovePoint(int itemIndex, double valueX, double valueY) {
        mergedMafTable.setValueAt(valueX, 0, itemIndex);
        mergedMafTable.setValueAt(valueY, 1, itemIndex);
    }
}

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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
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
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;

public class MafRescale extends JTabbedPane implements IMafChartHolder {
    private static final long serialVersionUID = -3803091816206090707L;
    private static final Logger logger = Logger.getLogger(MafRescale.class);
    private static final int ColumnWidth = 50;
    private static final int CellsPerSection = 2;
    private static final String OrigMafTableName = "Original MAF Scaling";
    private static final String NewMafTableName = "New MAF Scaling";
    private static final String XAxisName = "MAF Sensor (Voltage)";
    private static final String YAxisName = "Mass Airflow (g/s)";
    private static final String currentDataName = "Original";
    private static final String correctedDataName = "Rescaled";
    private final XYSeries currMafData = new XYSeries(currentDataName);
    private final XYSeries corrMafData = new XYSeries(correctedDataName);
    private JTable origMafTable = null;
    private JTable newMafTable = null;
    private TableCellListener newMafTableCellListener = null;
    private JFormattedTextField newMaxVFmtTextBox = null;
    private JFormattedTextField maxVUnchangedFmtTextBox = null;
    private JFormattedTextField minVFmtTextBox = null;
    private JFormattedTextField modeDeltaVFmtTextBox = null;
    private Insets insets0 = new Insets(0, 0, 0, 0);
    private Insets insets3 = new Insets(3, 3, 3, 3);
    
    private MafChartPanel mafChartPanel = null;
    private ExcelAdapter excelAdapter = null;
    private ExcelAdapter newMafExcelAdapter = null;
    private ArrayList<Double> origVoltArray = null;
    private ArrayList<Double> origGsArray = null;
    private ArrayList<Double> deltaVoltArray = null;
    private double modeDeltaV;

    public MafRescale(int tabPlacement) {
        super(tabPlacement);
        excelAdapter = new ExcelAdapter() {
            protected void onPaste(JTable table, boolean extendRows, boolean extendCols) {
                super.onPaste(table, extendRows, extendCols);
                calculateModeDeltaV();
            }
            protected void onPasteVertical(JTable table, boolean extendRows, boolean extendCols) {
                super.onPasteVertical(table, extendRows, extendCols);
                calculateModeDeltaV();
            }
            protected void onClearSelection(JTable table) {
                super.onClearSelection(table);
                calculateModeDeltaV();
                updateNewMafScale();
            }
        };
        newMafExcelAdapter = new ExcelAdapter() {
            protected void onPaste(JTable table, boolean extendRows, boolean extendCols) {
                super.onPaste(table, extendRows, extendCols);
                recalculateNewGs();
            }
            protected void onPasteVertical(JTable table, boolean extendRows, boolean extendCols) {
                super.onPasteVertical(table, extendRows, extendCols);
                recalculateNewGs();
            }
        };
        initialize();
    }

    private void initialize() {
        createDataTab();
        createUsageTab();
    }

    public TableCellListener getNewMafTableCellListenerListener() {
        return newMafTableCellListener;
    }

    public void setNewMafTableCellListenerListener(TableCellListener listener) {
        newMafTableCellListener = listener;
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
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0, 0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
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
        
        cntlPanel.add(new JLabel("New Max V"), gbc_cntlPanelLabel);
        
        newMaxVFmtTextBox = new JFormattedTextField(doubleFmt);
        newMaxVFmtTextBox.setColumns(7);
        newMaxVFmtTextBox.addPropertyChangeListener("value", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                Object source = e.getSource();
                if (source == newMaxVFmtTextBox)
                    updateNewMafScale();
            }
        });
        cntlPanel.add(newMaxVFmtTextBox, gbc_cntlPanelInput);
        
        gbc_cntlPanelLabel.gridx += 2;
        cntlPanel.add(new JLabel("Min V"), gbc_cntlPanelLabel);
        
        minVFmtTextBox = new JFormattedTextField(doubleFmt);
        minVFmtTextBox.setColumns(7);
        minVFmtTextBox.addPropertyChangeListener("value", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                Object source = e.getSource();
                if (source == minVFmtTextBox)
                    updateNewMafScale();
            }
        });
        gbc_cntlPanelInput.gridx += 2;
        cntlPanel.add(minVFmtTextBox, gbc_cntlPanelInput);

        gbc_cntlPanelLabel.gridx += 2;
        cntlPanel.add(new JLabel("Max Unchanged"), gbc_cntlPanelLabel);
        
        maxVUnchangedFmtTextBox = new JFormattedTextField(doubleFmt);
        maxVUnchangedFmtTextBox.setColumns(7);
        maxVUnchangedFmtTextBox.addPropertyChangeListener("value", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                Object source = e.getSource();
                if (source == maxVUnchangedFmtTextBox)
                    updateNewMafScale();
            }
        });
        gbc_cntlPanelInput.gridx += 2;
        cntlPanel.add(maxVUnchangedFmtTextBox, gbc_cntlPanelInput);
        
        gbc_cntlPanelLabel.gridx += 2;
        cntlPanel.add(new JLabel("Mode deltaV"), gbc_cntlPanelLabel);
        
        modeDeltaVFmtTextBox = new JFormattedTextField(scaleDoubleFmt);
        modeDeltaVFmtTextBox.setColumns(7);
        modeDeltaVFmtTextBox.setEditable(false);
        modeDeltaVFmtTextBox.setBackground(new Color(210,210,210));
        gbc_cntlPanelInput.gridx += 2;
        cntlPanel.add(modeDeltaVFmtTextBox, gbc_cntlPanelInput);
    }

    private void createMafScalesScrollPane(JPanel dataPanel) {
        JPanel mafPanel = new JPanel();
        GridBagLayout gbl_mafPanelLayout = new GridBagLayout();
        gbl_mafPanelLayout.columnWidths = new int[]{0};
        gbl_mafPanelLayout.rowHeights = new int[]{0, 0};
        gbl_mafPanelLayout.columnWeights = new double[]{1.0};
        gbl_mafPanelLayout.rowWeights = new double[]{0.0, 0.0};
        mafPanel.setLayout(gbl_mafPanelLayout);
        
        GridBagConstraints gbc_mafScrollPane = new GridBagConstraints();
        gbc_mafScrollPane.anchor = GridBagConstraints.PAGE_START;
        gbc_mafScrollPane.weightx = 1.0;
        gbc_mafScrollPane.insets = insets0;
        gbc_mafScrollPane.fill = GridBagConstraints.HORIZONTAL;
        gbc_mafScrollPane.gridx = 0;
        gbc_mafScrollPane.gridy = 0;
        
        MafTablePane origMafScrollPane = new MafTablePane(ColumnWidth, OrigMafTableName, false, false);
        origMafScrollPane.setMaximumSize(new Dimension(10000, 65));
        origMafScrollPane.setMinimumSize(new Dimension(10000, 65));
        origMafScrollPane.setPreferredSize(new Dimension(10000, 65));
        origMafScrollPane.setBorder(null);
        origMafScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        origMafTable = origMafScrollPane.getJTable();
        excelAdapter.addTable(origMafTable, false, false, false, false, false, false, false, false, true);
        mafPanel.add(origMafScrollPane, gbc_mafScrollPane);
        
        MafTablePane newMafScrollPane = new MafTablePane(ColumnWidth, NewMafTableName, false, true);
        newMafScrollPane.setMaximumSize(new Dimension(10000, 65));
        newMafScrollPane.setMinimumSize(new Dimension(10000, 65));
        newMafScrollPane.setPreferredSize(new Dimension(10000, 65));
        newMafScrollPane.setBorder(null);
        newMafScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        newMafTable = newMafScrollPane.getJTable();
        newMafExcelAdapter.addTable(newMafTable, false, false, false, false, false, false, false, false, true);
        gbc_mafScrollPane.gridy++;
        mafPanel.add(newMafScrollPane, gbc_mafScrollPane);
        
        JScrollPane mafScrollPane = new JScrollPane(mafPanel);
        mafScrollPane.setMaximumSize(new Dimension(10000, 150));
        mafScrollPane.setMinimumSize(new Dimension(10000, 150));
        mafScrollPane.setPreferredSize(new Dimension(10000, 150));
        mafScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        mafScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        GridBagConstraints gbl_mafScrollPane = new GridBagConstraints();
        gbl_mafScrollPane.anchor = GridBagConstraints.PAGE_START;
        gbl_mafScrollPane.fill = GridBagConstraints.HORIZONTAL;
        gbl_mafScrollPane.insets = insets0;
        gbl_mafScrollPane.weightx = 1.0;
        gbl_mafScrollPane.gridx = 0;
        gbl_mafScrollPane.gridy = 1;
        dataPanel.add(mafScrollPane, gbl_mafScrollPane);
        
        Action action = new AbstractAction() {
            private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
                recalculateNewGs();
            }
        };
        
        setNewMafTableCellListenerListener(new TableCellListener(newMafTable, action));
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
        lineRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator( 
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

        lineDataset.addSeries(currMafData);
        lineDataset.addSeries(corrMafData);
        
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
        bundle = ResourceBundle.getBundle("com.vgi.mafscaling.mafrescale");
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
    
    private void calculateModeDeltaV() {
        origVoltArray = new ArrayList<Double>();
        origGsArray = new ArrayList<Double>();
        if (!getMafTableData(origMafTable, origVoltArray, origGsArray) || origVoltArray.size() == 0)
            return;
        deltaVoltArray = new ArrayList<Double>();
        deltaVoltArray.add(0.0);
        int i;
        for (i = 1; i < origVoltArray.size(); ++i)
            deltaVoltArray.add(origVoltArray.get(i) - origVoltArray.get(i - 1));
        modeDeltaV = Utils.mode(deltaVoltArray);
        modeDeltaVFmtTextBox.setValue(modeDeltaV);
        for (i = deltaVoltArray.size() - 1; i > 0; --i) {
            if (modeDeltaV == deltaVoltArray.get(i)) {
                // hack as if value being set is the same then change even is not sent and updateNewMafScale() is not triggered
                maxVUnchangedFmtTextBox.setValue(null);
                maxVUnchangedFmtTextBox.setValue(origVoltArray.get(i));
                return;
            }
        }
        maxVUnchangedFmtTextBox.setValue(null);
    }
    
    private void calculateNewGs(ArrayList<Double> newVoltArray, ArrayList<Double> newGsArray) {
        TreeMap<Double, Integer> vgsTree = new TreeMap<Double, Integer>();
        for (int i = origVoltArray.size() - 1; i >= 0; --i)
            vgsTree.put(origVoltArray.get(i), i);
        Map.Entry<Double, Integer> kv;
        double x0, y0, x, y, x1, y1;
        for (int i = 1; i < newVoltArray.size(); ++i) {
            x = newVoltArray.get(i);
            kv = vgsTree.floorEntry(x); 
            if (kv == null) {
                newGsArray.add(0.0);
                continue;
            }
            x0 = kv.getKey();
            if (x0 == x) {
                newGsArray.add(origGsArray.get(kv.getValue()));
                continue;
            }
            y0 = origGsArray.get(kv.getValue());
            kv = vgsTree.ceilingEntry(x);
            if (kv == null) {
                newGsArray.add(0.0);
                continue;
            }
            x1 = kv.getKey();
            y1 = origGsArray.get(kv.getValue());
            y = Utils.linearInterpolation(x, x0, x1, y0, y1);
            newGsArray.add(y);
        }
    }
    
    private void recalculateNewGs() {
        try {
            if (origVoltArray == null || origGsArray == null || origVoltArray.size() == 0 || origVoltArray.size() != origGsArray.size())
                return;
            ArrayList<Double> newVoltArray = new ArrayList<Double>();
            ArrayList<Double> newGsArray = new ArrayList<Double>();
            newGsArray.add(origGsArray.get(0));
            for (int i = 0; i < newMafTable.getColumnCount(); ++i) {
                if (Pattern.matches(Utils.fpRegex, newMafTable.getValueAt(0, i).toString()))
                    newVoltArray.add(Double.valueOf(newMafTable.getValueAt(0, i).toString()));
                else
                    break;
            }
            if (newVoltArray.size() != origVoltArray.size())
                return;
            calculateNewGs(newVoltArray, newGsArray);
            for (int i = 0; i < newVoltArray.size(); ++i)
                newMafTable.setValueAt(newGsArray.get(i), 1, i);
            corrMafData.clear();
            setXYSeries(corrMafData, newVoltArray, newGsArray);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
    }
    
    private void updateNewMafScale() {
        try {
            corrMafData.clear();
            currMafData.clear();
            Utils.clearTable(newMafTable);
            
            if (newMaxVFmtTextBox.getValue() == null || 
                maxVUnchangedFmtTextBox.getValue() == null ||
                minVFmtTextBox.getValue() == null ||
                origMafTable.getValueAt(0, 0).toString().isEmpty())
                return;
            
            if (origVoltArray == null || origVoltArray.size() == 0 || origVoltArray.size() != origGsArray.size())
                return;
            
            if (origVoltArray.size() < 10) {
                JOptionPane.showMessageDialog(null, "It looks like you have only partial original MAF scale table", "Invalid Data", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double newMafV = (((Number)newMaxVFmtTextBox.getValue()).doubleValue());
            if (newMafV < origVoltArray.get(0)) {
                JOptionPane.showMessageDialog(null, "New Max V [" + newMafV + "] can't be lower than first MAF table value", "Invalid Data", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (newMafV > origVoltArray.get(origVoltArray.size() - 1)) {
                JOptionPane.showMessageDialog(null, "New Max V [" + newMafV + "] can't be higher than last MAF table value", "Invalid Data", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double minV = (((Number)minVFmtTextBox.getValue()).doubleValue());
            if (minV <= origVoltArray.get(1)) {
                JOptionPane.showMessageDialog(null, "Min V [" + minV + "] must be higher than second MAF table value", "Invalid Data", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (minV > newMafV) {
                JOptionPane.showMessageDialog(null, "Min V [" + minV + "] can't be higher than new MAF V value", "Invalid Data", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double maxVUnch = (((Number)maxVUnchangedFmtTextBox.getValue()).doubleValue());
            if (maxVUnch <= minV) {
                JOptionPane.showMessageDialog(null, "Max Unchanged [" + maxVUnch + "] must be higher than Min V value", "Invalid Data", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (maxVUnch > newMafV) {
                JOptionPane.showMessageDialog(null, "Max Unchanged [" + maxVUnch + "] can't be higher than new MAF V value", "Invalid Data", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int i, j, z;
            ArrayList<Double> newVoltArray = new ArrayList<Double>();
            ArrayList<Double> newGsArray = new ArrayList<Double>();
            newVoltArray.add(origVoltArray.get(0));
            newGsArray.add(origGsArray.get(0));

            // Find first value greater than MinV from original scale, 
            // calculate mid-point and add them as second and third values to the new array.
            // After that simply copy all original values up until Max Unchanged value.
            boolean minFound = false;
            double val;
            for (i = 2; i < origVoltArray.size(); ++i) {
                val = origVoltArray.get(i);
                if (minFound) {
                    if (val <= maxVUnch)
                        newVoltArray.add(val);
                    else
                        break;
                }
                else if (minV <= val) {
                    newVoltArray.add((val - origVoltArray.get(0)) / 2.0 + origVoltArray.get(0));
                    newVoltArray.add(val);
                    minFound = true;
                }
            }
            int newMaxUnchIdx = newVoltArray.size() - 1;
            
            // Find avg % change per section in the original scale but for the same number of points as new scale 
            double pointsCount = origVoltArray.size() - newVoltArray.size();
            int sectionCount = (int)Math.ceil((double)pointsCount / (double)CellsPerSection);
            List<Double> modDeltaList = deltaVoltArray.subList(newMaxUnchIdx, deltaVoltArray.size());
            double avgDelta = Utils.mean(modDeltaList);
            double maxDelta = Collections.max(modDeltaList);
            double minDelta = Collections.min(modDeltaList);
            double avgSectionChange = (maxDelta - minDelta) / sectionCount;
            double changePercent = (maxDelta - minDelta) / avgSectionChange;
            
            // Calculate delta per section
            double delta;
            ArrayList<Double> adj = new ArrayList<Double>();
            for (i = 0; i < sectionCount; ++i)
                adj.add(avgDelta);
            int end = (int) Math.floor(sectionCount / 2.0);
            for (i = 0, j = sectionCount - 1; i < j; ++i, --j) {
                delta = avgDelta / 100.00 * changePercent * (end - i);
                adj.set(i, avgDelta - delta);
                adj.set(j, avgDelta + delta);
            }
            // Apply diff for each cell of each section
            for (i = newMaxUnchIdx + 1, j = 0, z = 0; i < origVoltArray.size(); ++i, ++j) {
                double diff = adj.get(z);
                if (j >= CellsPerSection) {
                    j = 0;
                    ++z;
                    diff = adj.get(z);
                }
                newVoltArray.add(newVoltArray.get(i - 1) + diff);
            }
            // Since the above diffs are based of the original scale change simply adjust the new values to fit the new scale
            double corr = (newMafV - newVoltArray.get(newVoltArray.size() - 1)) / pointsCount;
            for (i = newMaxUnchIdx + 1, j = 1; i < newVoltArray.size(); ++i, ++j)
                newVoltArray.set(i, newVoltArray.get(i) + j * corr);

            calculateNewGs(newVoltArray, newGsArray);

            Utils.ensureColumnCount(newVoltArray.size(), newMafTable);
            for (i = 0; i < newVoltArray.size(); ++i) {
                newMafTable.setValueAt(newVoltArray.get(i), 0, i);
                newMafTable.setValueAt(newGsArray.get(i), 1, i);
            }
            setXYSeries(currMafData, origVoltArray, origGsArray);
            setXYSeries(corrMafData, newVoltArray, newGsArray);
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
        double paddingX = currMafData.getMaxX() * 0.05;
        double paddingY = currMafData.getMaxY() * 0.05;
        chartPanel.getChartPanel().getChart().getXYPlot().getDomainAxis(0).setRange(currMafData.getMinX() - paddingX, currMafData.getMaxX() + paddingX);
        chartPanel.getChartPanel().getChart().getXYPlot().getRangeAxis(0).setRange(currMafData.getMinY() - paddingY, currMafData.getMaxY() + paddingY);
    }

    @Override
    public void onMovePoint(int itemIndex, double valueX, double valueY) {
        newMafTable.setValueAt(valueX, 0, itemIndex);
        newMafTable.setValueAt(valueY, 1, itemIndex);
    }
}

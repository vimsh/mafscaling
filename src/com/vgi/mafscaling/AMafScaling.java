package com.vgi.mafscaling;

import java.awt.BasicStroke;
import java.awt.Color;
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
import java.io.File;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIDefaults;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

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

public abstract class AMafScaling extends FCTabbedPane implements IMafChartHolder, ActionListener {
	private static final long serialVersionUID = -1494227355521802829L;
	protected static final int ColumnWidth = 55;
	protected static final String MafTableName = "Current MAF Scaling";
	protected static final String currentDataName = "Current";
    protected static final String correctedDataName = "Corrected";
    protected static final String smoothedDataName = "Smoothed";
    protected static final String mafCurveDataName = "Smoothed Maf Curve";
    protected static final String currentSlopeDataName = "Current Maf Slope";
    protected static final String smoothedSlopeDataName = "Smoothed Maf Slope";
    protected static final String XAxisName = "MAF Sensor (Voltage)";
    protected static final String Y1AxisName = "Mass Airflow (g/s)";
	protected final ExcelAdapter excelAdapter = new ExcelAdapter();
	protected PrimaryOpenLoopFuelingTable polfTable = null;
	protected MafCompare mafCompare = null;
	protected MafChartPanel mafChartPanel = null;
	protected JTable mafTable = null;
	protected JTable mafSmoothingTable = null;
    protected JCheckBox checkBoxSmoothing = null;
    protected JComboBox<String> smoothComboBox = null;
    protected JCheckBox checkBoxCurrentMaf = null;
    protected JCheckBox checkBoxCorrectedMaf = null;
    protected JCheckBox checkBoxSmoothedMaf = null;
    protected JCheckBox checkBoxRunData = null;
    protected JButton btnCompareButton = null;
    protected JButton btnSmoothButton = null;
    protected JButton btnPlusButton = null;
    protected JButton btnMinusButton = null;
    protected JLabel lblMafIncDec = null;
    protected JFormattedTextField mafIncDecTextField = null;
    protected ArrayList<Double> rpmArray = new ArrayList<Double>();
    protected ArrayList<Double> mafvArray = new ArrayList<Double>();
    protected ArrayList<Double> voltArray = new ArrayList<Double>();
    protected ArrayList<Double> gsArray = new ArrayList<Double>();
    protected ArrayList<Double> gsCorrected = new ArrayList<Double>();
    protected ArrayList<Double> smoothGsArray = new ArrayList<Double>();
    protected XYSeries currMafData = new XYSeries(currentDataName);
    protected XYSeries corrMafData = new XYSeries(correctedDataName);
    protected XYSeries smoothMafData = new XYSeries(smoothedDataName);
    protected XYSeries runData = null;
    protected Insets insets0 = new Insets(0, 0, 0, 0);
    protected Insets insets1 = new Insets(1, 1, 1, 1);
    protected Insets insets2 = new Insets(2, 2, 2, 2);
    protected Insets insets3 = new Insets(3, 3, 3, 3);
    protected String[] optionButtons = { "Yes", "No", "No to all" };

    public AMafScaling(int tabPlacement, PrimaryOpenLoopFuelingTable table, MafCompare comparer) {
        super(tabPlacement);
    	polfTable = table;
    	mafCompare = comparer;
    }
    
    protected void initialize() {
        createDataTab();
        createGraghTab();
        createUsageTab();
    }
    
    public abstract void loadData();
    public abstract void saveData();
    protected abstract void createRunPanel(JPanel dataPanel);
    protected abstract void createGraghTab();
    protected abstract void onEnableSmoothingView(boolean flag);
    protected abstract void onSmoothReset();
    protected abstract void calculateMafScaling();
    protected abstract void clearRunTables();
    protected abstract void loadLogFile();
    protected abstract String usage();
    
    protected void selectLogFile() {
    	fileChooser.setMultiSelectionEnabled(true);
        if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(this))
            return;
        loadLogFile();
    }
	
	protected void onDroppedFiles(List<File> files) {
		if (files.size() > 0 && getSelectedIndex() == 0) {
	    	fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setCurrentDirectory(files.get(0));
			fileChooser.setSelectedFiles((File[])files.toArray());
			fileChooser.approveSelection();
			loadLogFile();
		}
	}

    
    protected void createDataTab() {
        JPanel dataPanel = new JPanel();
        add(dataPanel, "<html><div style='text-align: center;'>D<br>a<br>t<br>a</div></html>");
        GridBagLayout gbl_dataPanel = new GridBagLayout();
        gbl_dataPanel.columnWidths = new int[] {0};
        gbl_dataPanel.rowHeights = new int[] {0, 0, 0, 0};
        gbl_dataPanel.columnWeights = new double[]{1.0};
        gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0};
        dataPanel.setLayout(gbl_dataPanel);
        
        createControlPanel(dataPanel);
        createMafPanel(dataPanel);
        createRunControlPanel(dataPanel);
        createRunPanel(dataPanel);
    }
    
	protected void createControlPanel(JPanel dataPanel) {
	    JPanel cntlPanel = new JPanel();
	    GridBagConstraints gbl_ctrlPanel = new GridBagConstraints();
	    gbl_ctrlPanel.insets = insets3;
	    gbl_ctrlPanel.anchor = GridBagConstraints.NORTH;
	    gbl_ctrlPanel.fill = GridBagConstraints.HORIZONTAL;
	    gbl_ctrlPanel.gridx = 0;
	    gbl_ctrlPanel.gridy = 0;
	    dataPanel.add(cntlPanel, gbl_ctrlPanel);
	    
	    GridBagLayout gbl_cntlPanel = new GridBagLayout();
	    gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
	    gbl_cntlPanel.rowHeights = new int[]{0, 0};
	    gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
	    gbl_cntlPanel.rowWeights = new double[]{0};
	    cntlPanel.setLayout(gbl_cntlPanel);
	    
	    addButton(cntlPanel, 0, "Clear Maf Data", "clearmaf", GridBagConstraints.WEST);
	    addButton(cntlPanel, 1, "Clear Run Data", "clearrun", GridBagConstraints.WEST);
	    addButton(cntlPanel, 2, "Clear All", "clearall", GridBagConstraints.WEST);
	    addButton(cntlPanel, 3, "Load", "load", GridBagConstraints.WEST);
	    addButton(cntlPanel, 4, "Save", "save", GridBagConstraints.WEST);
	    addButton(cntlPanel, 5, "GO", "go", GridBagConstraints.EAST);
	}
    
	protected void createMafPanel(JPanel dataPanel) {
		MafTablePane mafScrollPane = new MafTablePane(ColumnWidth, MafTableName, true, true);
		mafTable = mafScrollPane.getJTable();
	    excelAdapter.addTable(mafTable, false, false, false, false, false, false, false, false, true);
	    GridBagConstraints gbc_mafScrollPane = new GridBagConstraints();
	    gbc_mafScrollPane.ipady = 30;
	    gbc_mafScrollPane.anchor = GridBagConstraints.PAGE_START;
	    gbc_mafScrollPane.insets = insets0;
	    gbc_mafScrollPane.fill = GridBagConstraints.HORIZONTAL;
	    gbc_mafScrollPane.gridx = 0;
	    gbc_mafScrollPane.gridy = 1;
	    dataPanel.add(mafScrollPane, gbc_mafScrollPane);
	}
    
	protected void createRunControlPanel(JPanel dataPanel) {
	    JPanel dataRunButtonPanel = new JPanel();
	    dataRunButtonPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
	    GridBagConstraints gbc_dataRunButtonPanel = new GridBagConstraints();
	    gbc_dataRunButtonPanel.anchor = GridBagConstraints.PAGE_START;
	    gbc_dataRunButtonPanel.weightx = 1.0;
	    gbc_dataRunButtonPanel.insets = insets0;
	    gbc_dataRunButtonPanel.fill = GridBagConstraints.HORIZONTAL;
	    gbc_dataRunButtonPanel.gridx = 0;
	    gbc_dataRunButtonPanel.gridy = 2;
	    dataPanel.add(dataRunButtonPanel, gbc_dataRunButtonPanel);
	
	    GridBagLayout gbl_dataRunButtonPanel = new GridBagLayout();
	    gbl_dataRunButtonPanel.columnWidths = new int[]{0, 0};
	    gbl_dataRunButtonPanel.rowHeights = new int[]{0, 0};
	    gbl_dataRunButtonPanel.columnWeights = new double[]{0.0, 1.0};
	    gbl_dataRunButtonPanel.rowWeights = new double[]{0};
	    dataRunButtonPanel.setLayout(gbl_dataRunButtonPanel);
	
	    addButton(dataRunButtonPanel, 0, "POL Fueling", "fueling", GridBagConstraints.WEST);
	    addButton(dataRunButtonPanel, 1, "Load Log", "loadlog", GridBagConstraints.WEST);
	}
	
	protected JPanel createGraphPlotPanel(JPanel cntlPanel) {
        JPanel plotPanel = new JPanel();
        GridBagLayout gbl_plotPanel = new GridBagLayout();
        gbl_plotPanel.columnWidths = new int[] {0};
        gbl_plotPanel.rowHeights = new int[] {0, 0, 0};
        gbl_plotPanel.columnWeights = new double[]{1.0};
        gbl_plotPanel.rowWeights = new double[]{0.0, 1.0, 0.0};
        plotPanel.setLayout(gbl_plotPanel);
        
        GridBagConstraints gbl_ctrlPanel = new GridBagConstraints();
        gbl_ctrlPanel.insets = insets3;
        gbl_ctrlPanel.anchor = GridBagConstraints.NORTH;
        gbl_ctrlPanel.fill = GridBagConstraints.HORIZONTAL;
        gbl_ctrlPanel.weightx = 1.0;
        gbl_ctrlPanel.gridx = 0;
        gbl_ctrlPanel.gridy = 0;
        plotPanel.add(cntlPanel, gbl_ctrlPanel);
        return plotPanel;
	}
	
	protected void createChart(JPanel plotPanel, String y2AxisName) {
        JFreeChart chart = ChartFactory.createScatterPlot(null, null, null, null, PlotOrientation.VERTICAL, false, true, false);
        chart.setBorderVisible(true);
        mafChartPanel = new MafChartPanel(chart, this);
        
        GridBagConstraints gbl_chartPanel = new GridBagConstraints();
        gbl_chartPanel.anchor = GridBagConstraints.CENTER;
        gbl_chartPanel.fill = GridBagConstraints.BOTH;
        gbl_chartPanel.insets = insets3;
        gbl_chartPanel.weightx = 1.0;
        gbl_chartPanel.weighty = 1.0;
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
        ValueAxis afrerrRange = new NumberAxis(y2AxisName);
        
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
	}
	
    protected void createGraphCommonControls(JPanel cntlPanel, int column) {
        checkBoxCurrentMaf = addCheckBox(cntlPanel, column, "Current", "current");
        checkBoxCorrectedMaf = addCheckBox(cntlPanel, ++column, "Corrected", "corrected");
        checkBoxSmoothedMaf = addCheckBox(cntlPanel, ++column, "Smoothed", "smoothed");
        btnCompareButton = addButton(cntlPanel, ++column, "Compare", "compare", GridBagConstraints.CENTER);
        checkBoxSmoothing = addCheckBox(cntlPanel, ++column, "Smoothing:", "smoothing");
        smoothComboBox = addComboBox(cntlPanel, ++column, new String[]{ " 3 ", " 5 ", " 7 " });
        btnSmoothButton = addButton(cntlPanel, ++column, "Apply", "smooth", GridBagConstraints.WEST);
	    addButton(cntlPanel, ++column, "Reset", "smoothreset", GridBagConstraints.WEST);
    }

    protected void createMafSmoothingPanel(JPanel plotPanel) {
        Dimension minDimension = new Dimension(26, 26);
        NumberFormat doubleFmt = NumberFormat.getNumberInstance();
        doubleFmt.setMaximumFractionDigits(2);
        
        JPanel mafSmoothingPanel = new JPanel();
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
       
        GridBagConstraints gbc_ctrl = new GridBagConstraints();
        gbc_ctrl.anchor = GridBagConstraints.CENTER;
        gbc_ctrl.insets = insets1;
        gbc_ctrl.gridx = 0;
        gbc_ctrl.gridy = 0;
        lblMafIncDec = new JLabel("Inc/Dec");
        mafSmoothingPanel.add(lblMafIncDec, gbc_ctrl);
       
        mafIncDecTextField = new JFormattedTextField(doubleFmt);
        mafIncDecTextField.setValue(new Double(0.1));
        mafIncDecTextField.setColumns(5);
        mafIncDecTextField.setMinimumSize(minDimension);
        gbc_ctrl.fill = GridBagConstraints.HORIZONTAL;
        gbc_ctrl.gridy++;
        mafSmoothingPanel.add(mafIncDecTextField, gbc_ctrl);

        UIDefaults nimbusInsets = new UIDefaults();
        nimbusInsets.put("Button.contentMargins", insets3);
    	
        btnPlusButton = new JButton("<html><div style='text-align: center; font-weight: bold;'>+</div></html>");
        btnPlusButton.setActionCommand("plus");
        btnPlusButton.addActionListener(this);
        btnPlusButton.setMinimumSize(minDimension);
        btnPlusButton.setMargin(insets3);
        btnPlusButton.putClientProperty("Nimbus.Overrides", nimbusInsets);

        gbc_ctrl.gridx++;
        gbc_ctrl.gridy--;
        mafSmoothingPanel.add(btnPlusButton, gbc_ctrl);
 
        btnMinusButton = new JButton("<html><div style='text-align: center; font-weight: bold;'>-</div></html>");
        btnMinusButton.setActionCommand("minus");
        btnMinusButton.addActionListener(this);
        btnMinusButton.setMinimumSize(minDimension);
        btnMinusButton.setMargin(insets3);
        btnMinusButton.putClientProperty("Nimbus.Overrides", nimbusInsets);
        gbc_ctrl.gridy++;
        mafSmoothingPanel.add(btnMinusButton, gbc_ctrl);

		MafTablePane mafSmoothingPane = new MafTablePane(ColumnWidth, null, false, false);
		mafSmoothingPane.hideRowHeaders();
		mafSmoothingTable = mafSmoothingPane.getJTable();
        excelAdapter.addTable(mafSmoothingTable, false, true, true, false, false, true, false, false, true);
        
        GridBagConstraints gbc_mafSmoothingPane = new GridBagConstraints();
        gbc_mafSmoothingPane.anchor = GridBagConstraints.SOUTH;
        gbc_mafSmoothingPane.fill = GridBagConstraints.HORIZONTAL;
        gbc_mafSmoothingPane.insets = insets0;
        gbc_mafSmoothingPane.gridx = 2;
        gbc_mafSmoothingPane.gridy = 0;
        gbc_mafSmoothingPane.gridheight = 2;
        gbc_mafSmoothingPane.weightx = 1.0;
        gbc_mafSmoothingPane.ipady = 30;
        mafSmoothingPanel.add(mafSmoothingPane, gbc_mafSmoothingPane);
        
        lblMafIncDec.setVisible(false);
        mafIncDecTextField.setVisible(false);
        btnPlusButton.setVisible(false);
        btnMinusButton.setVisible(false);
	}
    
	protected JButton addButton(JPanel panel, int column, String name, String action, int align) {
        JButton button = new JButton(name);
        button.setActionCommand(action);
        button.addActionListener(this);
        GridBagConstraints gbc_button = new GridBagConstraints();
        gbc_button.anchor = align;
        gbc_button.insets = insets2;
        gbc_button.gridx = column;
        gbc_button.gridy = 0;
        if (GridBagConstraints.CENTER == align)
            gbc_button.weightx = 1.0;
        panel.add(button, gbc_button);
        return button;
    }
    
	protected JCheckBox addCheckBox(JPanel panel, int column, String name, String action) {
		JCheckBox check = new JCheckBox(name);
        check.setActionCommand(action);
        check.addActionListener(this);
        GridBagConstraints gbc_check = new GridBagConstraints();
        gbc_check.insets = new Insets(3, 3, 3, 0);
        gbc_check.gridx = column;
        gbc_check.gridy = 0;
        panel.add(check, gbc_check);
        return check;
    }
    
	protected JComboBox<String> addComboBox(JPanel panel, int column, String[] values) {
        JComboBox<String> combo = new JComboBox<String>(values);
        combo.setSelectedIndex(0);
        combo.setEnabled(false);
        GridBagConstraints gbc_combo = new GridBagConstraints();
        gbc_combo.anchor = GridBagConstraints.WEST;
        gbc_combo.insets = insets2;
        gbc_combo.gridx = column;
        gbc_combo.gridy = 0;
        panel.add(combo, gbc_combo);
        return combo;
    }

	protected boolean getMafTableData(ArrayList<Double> voltArray, ArrayList<Double> gsArray) {
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
    
	protected void clearMafTable() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
    	try {
	        while (MafTablePane.MafTableColumnCount < mafTable.getColumnCount())
	            Utils.removeColumn(MafTablePane.MafTableColumnCount, mafTable);
	        Utils.clearTable(mafTable);
    	}
    	finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    	}
    }
	

    
	protected void clearData() {
    	mafvArray.clear();
    	rpmArray.clear();
        voltArray.clear();
        gsArray.clear();
        gsCorrected.clear();
        smoothGsArray.clear();
    }
    
	protected void clearNotRunDataCheckboxes() {
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
    
	protected void clearChartCheckBoxes() {
        checkBoxRunData.setSelected(false);
        checkBoxCurrentMaf.setSelected(false);
        checkBoxCorrectedMaf.setSelected(false);
        checkBoxSmoothedMaf.setSelected(false);
    }
	
	protected void enableSmoothingView(boolean flag) {
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
        onEnableSmoothingView(flag);
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
            currMafData.setDescription(currentDataName);
            corrMafData.setDescription(correctedDataName);
            smoothMafData.setDescription(smoothedDataName);
        }
    }
    
	protected void clearChartData() {
        runData.clear();
        currMafData.clear();
        corrMafData.clear();
        smoothMafData.clear();
    }
    
    protected void smoothReset() {
        if (gsCorrected.size() == 0)
            return;
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
	        smoothGsArray.clear();
	        smoothGsArray.addAll(gsCorrected);
	        setXYTable(mafSmoothingTable, voltArray, smoothGsArray);
	        corrMafData.clear();
	        onSmoothReset();
        }
        finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
	protected void changeMafSmoothingCellValue(boolean add) {
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
            mafSmoothingTable.changeSelection(1, itemIndex + 1, false, false);
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
    
    protected void createUsageTab() {
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
    
    protected boolean setCorrectedMafData() {
        return setXYSeries(corrMafData, voltArray, gsCorrected);
    }
    
    protected boolean setSmoothedMafData() {
        return setXYSeries(smoothMafData, voltArray, smoothGsArray);
    }

    protected boolean setXYSeries(XYSeries series, ArrayList<Double> xarr, ArrayList<Double> yarr) {
        if (xarr.size() == 0 || xarr.size() != yarr.size())
            return false;
        series.clear();
        for (int i = 0; i < xarr.size(); ++i)
            series.add(xarr.get(i), yarr.get(i), false);
        series.fireSeriesChanged();
        return true;
    }
    
    protected void smoothCurve() {
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
    
    protected boolean plotCurrentMafData() {
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
    
    protected void plotSmoothingLineSlopes() {
        currMafData.clear();
        smoothMafData.clear();
        plotLineSlope(currMafData, voltArray, gsArray);
        plotLineSlope(smoothMafData, voltArray, smoothGsArray);
        setXYTable(mafSmoothingTable, voltArray, smoothGsArray);
    }
    
    private void plotLineSlope(XYSeries series, ArrayList<Double> xarr, ArrayList<Double> yarr) {
        if (xarr.size() == 0 || yarr.size() == 0 || xarr.size() != yarr.size())
            return;
        for (int j = 0; j < xarr.size() - 1; ++j)
            series.add((double)xarr.get(j + 1), (double)((yarr.get(j + 1) - yarr.get(j)) / (xarr.get(j + 1) - xarr.get(j))));
    }

    protected boolean setXYTable(JTable table, ArrayList<Double> xarr, ArrayList<Double> yarr) {
        if (xarr.size() == 0 || yarr.size() == 0 || xarr.size() != yarr.size())
            return false;
        for (int i = 0; i < xarr.size(); ++i) {
            Utils.ensureColumnCount(i + 1, table);
            table.setValueAt(xarr.get(i), 0, i);
            table.setValueAt(smoothGsArray.get(i), 1, i);
        }
        return true;
    }
    
    protected boolean checkActionPerformed(ActionEvent e) {
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
        	mafCompare.setReferenceMafTables(mafTable, mafSmoothingTable);
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
        else if ("loadlog".equals(e.getActionCommand())) {
            selectLogFile();
        }
        else
        	return false;
        return true;
    }
}

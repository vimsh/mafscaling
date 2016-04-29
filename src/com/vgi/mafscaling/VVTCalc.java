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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYSeriesLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.math.plot.Plot3DPanel;
import org.scijava.swing.checkboxtree.CheckBoxNodeData;
import org.scijava.swing.checkboxtree.CheckBoxNodeEditor;
import org.scijava.swing.checkboxtree.CheckBoxNodePanel;
import org.scijava.swing.checkboxtree.CheckBoxNodeRenderer;


public class VVTCalc extends ACompCalc {
	private static final long serialVersionUID = -5375830681660294773L;
	private static final Logger logger = Logger.getLogger(VVTCalc.class);
    private static final int RunCount = 12;
    private static final int RunRowsCount = 200;
    private static final String xAxisName = "RPM";
    private static final String yVVT1AxisName = "VVT1";
    private static final String yVVT2AxisName = "VVT2";
    private static final String yVEAxisName = "VE";
    private static final String zVEAxisName = "VVT";
    private static final String pullIndexReplaceString = "Pull ";
    private static final String prototypeDisplayValue = "XXXXXXXXXXXXXXXXXXXXXXXXXXX";

    private int wotPoint = Config.getWOTStationaryPointValue();
    private int skipRowsOnTransition = Config.getOLCLTransitionSkipRows();
    private char temperScale = Config.getTemperatureScale();
    private char mapUnit = Config.getMapUnit();

    private int logThtlAngleColIdx = -1;
    private int logRpmColIdx = -1;
    private int logVvt1ColIdx = -1;
    private int logVvt2ColIdx = -1;
    private int logIatColIdx = -1;
    private int logMapColIdx = -1;
    private int logMafColIdx = -1;

	protected ChartPanel[] chartPanels = new ChartPanel[3];
    private JTable[] runTables = new JTable[RunCount];
    private XYSeries[][] runSeries = new XYSeries[3][RunCount];
    private XYSeries vvt1BestSeries = new XYSeries(RunCount + 1);
    private XYSeries vvt2BestSeries = new XYSeries(RunCount + 2);
    private XYSeries ve1BestSeries = new XYSeries(RunCount + 3);
    private XYSeries ve2BestSeries = new XYSeries(RunCount + 4);
    private JComboBox<String> pullsComboBox = null;
    private JPanel dataRunPanel = null;
    private JTree pullTree = null;
    protected ArrayList<Double> xAxisArray = null;

    public VVTCalc(int tabPlacement) {
        super(tabPlacement);
        origTableName = "RPM #1";
        corrTableName = "Best VVT1";
        newTableName = "RPM #2";
        corrCountTableName = "Best VVT2";
        x3dAxisName = "MAP";
        y3dAxisName = "RPM";
        z3dAxisName = "Avg Error %";
		vvt1BestSeries.setDescription(corrTableName);
		vvt2BestSeries.setDescription(corrCountTableName);
		ve1BestSeries.setDescription("Best VE1");
		ve2BestSeries.setDescription("Best VE2");
        initialize(null);
        loadConfig();
    }
	
	public class PullNodeRenderer extends CheckBoxNodeRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			Component renderer = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			renderer.setBackground(null);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
			if (renderer instanceof DefaultTreeCellRenderer && node.getAllowsChildren()) {
				((DefaultTreeCellRenderer)renderer).setIcon(null);
				((DefaultTreeCellRenderer)renderer).setOpaque(false);
				((DefaultTreeCellRenderer)renderer).setBackgroundNonSelectionColor(null);
			}
			else {
				CheckBoxNodePanel panel = (CheckBoxNodePanel)renderer;
				if (!panel.hasFocus() && selected)
					pullTree.clearSelection();
			}
			return renderer;
		}
	}

    //////////////////////////////////////////////////////////////////////////////////////
    // DATA TAB
    //////////////////////////////////////////////////////////////////////////////////////

    protected void createControlPanel(JPanel dataPanel) {
        JPanel cntlPanel = new JPanel();
        GridBagConstraints gbl_ctrlPanel = new GridBagConstraints();
        gbl_ctrlPanel.insets = insets3;
        gbl_ctrlPanel.anchor = GridBagConstraints.NORTH;
        gbl_ctrlPanel.fill = GridBagConstraints.HORIZONTAL;
        gbl_ctrlPanel.gridx = 0;
        gbl_ctrlPanel.gridy = 0;
        gbl_ctrlPanel.weightx = 1.0;
        gbl_ctrlPanel.gridwidth = 2;
        dataPanel.add(cntlPanel, gbl_ctrlPanel);
        
        GridBagLayout gbl_cntlPanel = new GridBagLayout();
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);

	    addButton(cntlPanel, 0, "Load Log", "loadlog", GridBagConstraints.WEST);
	    addButton(cntlPanel, 1, "Clear Run Data", "clearlog", GridBagConstraints.WEST);
	    addButton(cntlPanel, 2, "Clear All", "clearall", GridBagConstraints.WEST);
	    addButton(cntlPanel, 3, "Save RPM Columns", "save", GridBagConstraints.EAST);
	    addButton(cntlPanel, 4, "GO", "go", GridBagConstraints.EAST);
    }

    protected void createDataPanel(JPanel dataPanel, String[] logColumns) {
        JScrollPane dataScrollPane = new JScrollPane();
        GridBagConstraints gbc_dataScrollPane = new GridBagConstraints();
        gbc_dataScrollPane.weightx = 1.0;
        gbc_dataScrollPane.weighty = 1.0;
        gbc_dataScrollPane.fill = GridBagConstraints.BOTH;
        gbc_dataScrollPane.gridx = 0;
        gbc_dataScrollPane.gridy = 1;
        dataPanel.add(dataScrollPane, gbc_dataScrollPane);

        dataRunPanel = new JPanel();
        dataScrollPane.setViewportView(dataRunPanel);
        GridBagLayout gbl_dataRunPanel = new GridBagLayout();
        gbl_dataRunPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_dataRunPanel.rowHeights = new int[] {0};
        gbl_dataRunPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        gbl_dataRunPanel.rowWeights = new double[]{0.0};
        dataRunPanel.setLayout(gbl_dataRunPanel);

        JScrollPane dataTablesScrollPane = new JScrollPane();
        dataTablesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        dataTablesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        GridBagConstraints gbc_dataTablesScrollPanee = new GridBagConstraints();
        gbc_dataTablesScrollPanee.weightx = 0.0;
        gbc_dataTablesScrollPanee.weighty = 1.0;
        gbc_dataTablesScrollPanee.anchor = GridBagConstraints.PAGE_START;
        gbc_dataTablesScrollPanee.fill = GridBagConstraints.BOTH;
        gbc_dataTablesScrollPanee.ipadx = ColumnWidth * 4;
        gbc_dataTablesScrollPanee.gridx = 1;
        gbc_dataTablesScrollPanee.gridy = 1;
        dataPanel.add(dataTablesScrollPane, gbc_dataTablesScrollPanee);
        
        JPanel tablesPanel = new JPanel();
        dataTablesScrollPane.setViewportView(tablesPanel);
        GridBagLayout gbl_tablesPanel = new GridBagLayout();
        gbl_tablesPanel.columnWidths = new int[]{0, 0, 0, 0};
        gbl_tablesPanel.rowHeights = new int[] {0, 0};
        gbl_tablesPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0};
        gbl_tablesPanel.rowWeights = new double[]{0.0, 1.0};
        tablesPanel.setLayout(gbl_tablesPanel);
        
        createLogDataTable(dataRunPanel);
        createDataTables(tablesPanel);
    }

    private void createLogDataTable(JPanel dataRunPanel) {
    	dataRunPanel.removeAll();
        GridBagConstraints gbc_run = new GridBagConstraints();
        gbc_run.anchor = GridBagConstraints.PAGE_START;
        gbc_run.insets = new Insets(0, 2, 0, 2);
        int colCount = 3;
        if (!Config.getVvt2ColumnName().equals(Config.NO_NAME))
        	colCount = 4;
        for (int i = 0; i < RunCount; ++i) {
            runTables[i] = new JTable();
            JTable table = runTables[i];
            table.getTableHeader().setReorderingAllowed(false);
            table.setModel(new DefaultTableModel(RunRowsCount, colCount));
            table.setColumnSelectionAllowed(true);
            table.setCellSelectionEnabled(true);
            table.setBorder(new LineBorder(new Color(0, 0, 0)));
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
            table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            table.getColumnModel().getColumn(0).setHeaderValue("RPM");
            table.getColumnModel().getColumn(1).setHeaderValue("VVT1");
            if (3 == colCount)
                table.getColumnModel().getColumn(2).setHeaderValue("VE");
            else {
	            table.getColumnModel().getColumn(2).setHeaderValue("VVT2");
	            table.getColumnModel().getColumn(3).setHeaderValue("VE");
            }
            Utils.initializeTable(table, ColumnWidth);
            
	        ExcelAdapter excelAdapter = new ExcelAdapter();
            excelAdapter.addTable(table, true, false);
            excelAdapterList.add(excelAdapter);

            gbc_run.gridx = i;
            gbc_run.gridy = 0;
            dataRunPanel.add(table.getTableHeader(), gbc_run);
            gbc_run.gridy = 1;
            dataRunPanel.add(table, gbc_run);
        }
    }
    
    protected void createDataTables(JPanel panel) {
        origTable = createDataTable(panel, origTableName, 1, 0, true);
        corrTable = createDataTable(panel, corrTableName, 1, 1, false);
        newTable = createDataTable(panel, newTableName, 1, 2, true);
        corrCountTable = createDataTable(panel, corrCountTableName, 1, 3, false);
    }
    
    protected void clearRunTables() {
    	clearRunTable(corrTable);
    	clearRunTable(corrCountTable);
        formatTable(origTable);
        formatTable(newTable);
    }
    
    protected void clearRunTable(JTable table) {
        if (table == corrTable)
        	table.setModel(new DefaultTableModel(origTable.getRowCount(), origTable.getColumnCount()));
        else if (table == corrCountTable)
        	table.setModel(new DefaultTableModel(newTable.getRowCount(), newTable.getColumnCount()));
        Utils.initializeTable(table, ColumnWidth);
        formatTable(table);
    }
    
    protected void formatTable(JTable table) {
        if (table == origTable || table == newTable) {
	        Format[][] formatMatrix = { { new DecimalFormat("#"), new DecimalFormat("#") } };
	        NumberFormatRenderer renderer = (NumberFormatRenderer)table.getDefaultRenderer(Object.class);
	        renderer.setFormats(formatMatrix);
        }
        else {
	        Format[][] formatMatrix = { { new DecimalFormat("0.00"), new DecimalFormat("0.00") } };
	        NumberFormatRenderer renderer = (NumberFormatRenderer)table.getDefaultRenderer(Object.class);
	        renderer.setFormats(formatMatrix);
        }
    }
    
    protected void clearTables() {
    	clearRunTable(origTable);
    	clearRunTable(newTable);
    	clearRunTables();
    }
    
    protected void clearLogDataTables() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
	        for (int i = 0; i < runTables.length; ++i) {
	            while (RunRowsCount < runTables[i].getRowCount())
	                Utils.removeRow(RunRowsCount, runTables[i]);
	            Utils.clearTable(runTables[i]);
	        }
	        clearChartData();
        }
        finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE CHART TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    protected void createGraghTab() {
        JPanel plotPanel = new JPanel();
        add(plotPanel, "<html><div style='text-align: center;'>C<br>h<br>a<br>r<br>t</div></html>");

        GridBagLayout gbl_plotPanel = new GridBagLayout();
        gbl_plotPanel.columnWidths = new int[] {0};
        gbl_plotPanel.rowHeights = new int[] {0, 0};
        gbl_plotPanel.columnWeights = new double[]{1.0};
        gbl_plotPanel.rowWeights = new double[]{0.0, 1.0};
        plotPanel.setLayout(gbl_plotPanel);
        
        JPanel chartsPanel = new JPanel();

        GridBagLayout gbl_chartsPanel = new GridBagLayout();
        gbl_chartsPanel.columnWidths = new int[] {0};
        gbl_chartsPanel.rowHeights = new int[] {0, 0, 0};
        gbl_chartsPanel.columnWeights = new double[]{1.0};
        gbl_chartsPanel.rowWeights = new double[]{0.0, 0.0, 0.0};
        chartsPanel.setLayout(gbl_chartsPanel);

        createCtrlPanel(plotPanel);
        createTreePanel();
        createChart(0, chartsPanel, xAxisName, yVVT1AxisName);
        createChart(1, chartsPanel, xAxisName, yVVT2AxisName);
        createChart(2, chartsPanel, xAxisName, yVEAxisName);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(pullTree), chartsPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerLocation(150);
        GridBagConstraints gbc_dataPanel = new GridBagConstraints();
        gbc_dataPanel.insets = insets3;
        gbc_dataPanel.anchor = GridBagConstraints.CENTER;
        gbc_dataPanel.weightx = 1.0;
        gbc_dataPanel.weighty = 1.0;
        gbc_dataPanel.fill = GridBagConstraints.BOTH;
        gbc_dataPanel.gridx = 0;
        gbc_dataPanel.gridy = 1;
        plotPanel.add(splitPane, gbc_dataPanel);
    }
    
	protected void createCtrlPanel(JPanel plotPanel) {
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
        gbl_cntlPanel.columnWidths = new int[]{0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0, 0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 1.0};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);
	}
	
	protected void createTreePanel() {
		DefaultMutableTreeNode wotTreeRoot = new DefaultMutableTreeNode("Root");
		DefaultTreeModel treeModel = new DefaultTreeModel(wotTreeRoot);
		pullTree = new JTree(treeModel);
		pullTree.setCellRenderer(new PullNodeRenderer());
		pullTree.setCellEditor(new CheckBoxNodeEditor(pullTree));
		pullTree.setEditable(true);
		pullTree.setRootVisible(false);
		pullTree.setOpaque(false);
		pullTree.setBackground(null);
		pullTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		pullTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				Object obj = ((DefaultMutableTreeNode)e.getPath().getLastPathComponent()).getUserObject();
				if (obj instanceof CheckBoxNodeData) {
					CheckBoxNodeData checkBoxNode = (CheckBoxNodeData) obj;
			    	XYSeriesCollection dataset;
			    	XYSeries series;
					for (int i = 0; i < 3; ++i) {
					    dataset = (XYSeriesCollection) chartPanels[i].getChart().getXYPlot().getDataset(0);
					    if (checkBoxNode.isChecked()) {
					    	for (int j = 0; j < runSeries[i].length; ++j) {
					    		if (runSeries[i][j] != null) {
					    			series = runSeries[i][j];
					    			if (series.getDescription().equals(checkBoxNode.getText())) {
										dataset.addSeries(series);
										runSeries[i][j] = null;
										break;
					    			}
					    		}
					    	}
					    }
					    else {
						    for (int j = 0; j < dataset.getSeriesCount(); ++j) {
						    	series = dataset.getSeries(j);
						    	int k = Integer.parseInt(series.getDescription().substring(pullIndexReplaceString.length())) - 1;
								if (runSeries[i][k] == null) {
									if (series.getDescription().equals(checkBoxNode.getText())) {
									    runSeries[i][k] = series;
									    dataset.removeSeries(j);
										break;
									}
							    }
							}
					    }
					}
				}
			}
		});
	}

    protected void createChart(int index, JPanel plotPanel, String xAxisName, String yAxisName) {
        JFreeChart chart = ChartFactory.createXYLineChart(null, null, null, null, PlotOrientation.VERTICAL, false, true, false);
        chart.setBorderVisible(true);
        
        ChartPanel chartPanel = new ChartPanel(chart, true, true, true, true, true);
        chartPanels[index] = chartPanel;
        chartPanel.setFocusable(true);
		chartPanel.setAutoscrolls(true);
		if (index == 1 && Config.getVvt2ColumnName().equals(Config.NO_NAME))
			chartPanels[index].setVisible(false);
        
        GridBagConstraints gbl_chartPanel = new GridBagConstraints();
        gbl_chartPanel.anchor = GridBagConstraints.CENTER;
        gbl_chartPanel.insets = new Insets(3, 3, 3, 3);
        gbl_chartPanel.weightx = 1.0;
        gbl_chartPanel.weighty = 1.0;
        gbl_chartPanel.fill = GridBagConstraints.BOTH;
        gbl_chartPanel.gridx = 0;
        gbl_chartPanel.gridy = index;
        plotPanel.add(chartPanel, gbl_chartPanel);

        XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer();
        lineRenderer.setUseFillPaint(true);
        lineRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(
            StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, 
            new DecimalFormat("0.00"), new DecimalFormat("0.00"))
        );

        lineRenderer.setLegendItemLabelGenerator(
    		new StandardXYSeriesLabelGenerator() {
				private static final long serialVersionUID = 7593430826693873496L;
				public String generateLabel(XYDataset dataset, int series) {
					XYSeries xys = ((XYSeriesCollection)dataset).getSeries(series);
					return xys.getDescription();
				}
    		}
        );

        XYLineAndShapeRenderer lineRendererBest = new XYLineAndShapeRenderer();
        lineRendererBest.setUseFillPaint(true);
        lineRendererBest.setBaseToolTipGenerator(new StandardXYToolTipGenerator(
            StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, 
            new DecimalFormat("0.00"), new DecimalFormat("0.00"))
        );

        lineRendererBest.setLegendItemLabelGenerator(
    		new StandardXYSeriesLabelGenerator() {
				private static final long serialVersionUID = -5917538704894141801L;
				public String generateLabel(XYDataset dataset, int series) {
					XYSeries xys = ((XYSeriesCollection)dataset).getSeries(series);
					return xys.getDescription();
				}
    		}
        );
        Stroke stroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, null, 0.0f);
        lineRendererBest.setSeriesPaint(0, Color.DARK_GRAY);
        lineRendererBest.setSeriesStroke(0, stroke);
        lineRendererBest.setSeriesPaint(1, Color.BLACK);
        lineRendererBest.setSeriesStroke(1, stroke);

        NumberAxis xAxis = new NumberAxis(xAxisName);
        xAxis.setAutoRangeIncludesZero(false);
        NumberAxis yAxis = new NumberAxis(yAxisName);
        yAxis.setAutoRangeIncludesZero(false);
        
        XYPlot plot = chart.getXYPlot();
        plot.setRangePannable(true);
        plot.setDomainPannable(true);
        plot.setDomainGridlinePaint(Color.DARK_GRAY);
        plot.setRangeGridlinePaint(Color.DARK_GRAY);
        plot.setBackgroundPaint(new Color(224, 224, 224));

        plot.setDataset(0, new XYSeriesCollection());
        plot.setRenderer(0, lineRenderer);
        plot.setDomainAxis(0, xAxis);
        plot.setRangeAxis(0, yAxis);
        plot.mapDatasetToDomainAxis(0, 0);
        plot.mapDatasetToRangeAxis(0, 0);

        XYSeriesCollection bestVVTDataset = new XYSeriesCollection();
        plot.setDataset(1, bestVVTDataset);
        plot.setRenderer(1, lineRendererBest);
        plot.mapDatasetToDomainAxis(0, 0);
        plot.mapDatasetToRangeAxis(0, 0);
        
        LegendTitle legend = new LegendTitle(plot); 
        legend.setItemFont(new Font("Arial", 0, 10));
        legend.setPosition(RectangleEdge.TOP);
        chart.addLegend(legend);
    }

    private boolean plotVEData() {
    	int i = 0;
    	int j = 0;
    	int k = 0;
    	double rpm;
    	String name;
    	try {
    		clearChartData();
    		DefaultMutableTreeNode treeNode;
    		TreePath path;
	    	XYSeries[] series = new XYSeries[3];
	    	XYSeriesCollection[] datasets = new XYSeriesCollection[3];
	    	XYLineAndShapeRenderer[] lineRenderers = new XYLineAndShapeRenderer[3];
			DefaultMutableTreeNode root = (DefaultMutableTreeNode)pullTree.getModel().getRoot();
			((DefaultTreeModel )pullTree.getModel()).reload(root);
			for (k = 0; k < 3; ++k) {
			    datasets[k] = (XYSeriesCollection) chartPanels[k].getChart().getXYPlot().getDataset(0);
			    lineRenderers[k] = (XYLineAndShapeRenderer) chartPanels[k].getChart().getXYPlot().getRenderer(0);
			}
	        Stroke stroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, null, 0.0f);
			for (i = 0; i < runTables.length; ++i) {
				try {
					JTable table = runTables[i];
					int veIdx = table.getColumnCount() - 1;
		    		series[0] = new XYSeries(i);
		    		series[1] = new XYSeries(i);
		    		series[2] = new XYSeries(i);
		    		for (j = 0; j < table.getRowCount(); ++j) {
		    			if (table.getValueAt(j, 0).equals("") || table.getValueAt(j, veIdx).equals(""))
		    				break;
		    			rpm = Double.valueOf(table.getValueAt(j, 0).toString());
		    			series[0].add(rpm, Double.valueOf(table.getValueAt(j, 1).toString()));
		    			if (3 == veIdx)
		    				series[1].add(rpm, Double.valueOf(table.getValueAt(j, 2).toString()));
		    			series[2].add(rpm, Double.valueOf(table.getValueAt(j, veIdx).toString()));
		    		}
		    		if (series[0].getItemCount() > 0) {
		    			name = pullIndexReplaceString + (i + 1);
		    	        pullsComboBox.addItem(name);
		    			for (k = 0; k < 3; ++k) {
			    			series[k].setDescription(name);
			    	    	datasets[k].addSeries(series[k]);
		                	lineRenderers[k].setSeriesShapesVisible(datasets[k].getSeriesCount() - 1, false);
		                    lineRenderers[k].setSeriesStroke(datasets[k].getSeriesCount() - 1, stroke);
		    			}
		    			treeNode = new DefaultMutableTreeNode(new CheckBoxNodeData(name, true));
		        		root.add(treeNode);
		        		path = new TreePath(root);
		        		pullTree.expandPath(path.pathByAddingChild(treeNode));	
		    		}
		        }
		        catch (NumberFormatException e) {
		            logger.error(e);
		            JOptionPane.showMessageDialog(null, "Error parsing number from Pull " + i + " table, row " + j + ": " + e, "Error", JOptionPane.ERROR_MESSAGE);
		            return false;
		        }
	    	}
			((DefaultTreeModel )pullTree.getModel()).reload(root);
    	}
    	catch (Exception e) {
            logger.error(e);
            JOptionPane.showMessageDialog(null, "Failed to plot VE data: " + e, "Error", JOptionPane.ERROR_MESSAGE);
    	}

    	return true;
    }

    protected void create3dGraghTab() {
        JPanel plotPanel = new JPanel();
        add(plotPanel, "<html><div style='text-align: center;'>3<br>D<br><br>C<br>h<br>a<br>r<br>t</div></html>");
        GridBagLayout gbl_plotPanel = new GridBagLayout();
        gbl_plotPanel.columnWidths = new int[] {0};
        gbl_plotPanel.rowHeights = new int[] {0, 0};
        gbl_plotPanel.columnWeights = new double[]{1.0};
        gbl_plotPanel.rowWeights = new double[]{0.0, 1.0};
        plotPanel.setLayout(gbl_plotPanel);

        JPanel cntlPanel = new JPanel();
        GridBagConstraints gbc_ctrlPanel = new GridBagConstraints();
        gbc_ctrlPanel.insets = insets3;
        gbc_ctrlPanel.anchor = GridBagConstraints.NORTH;
        gbc_ctrlPanel.weightx = 1.0;
        gbc_ctrlPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_ctrlPanel.gridx = 0;
        gbc_ctrlPanel.gridy = 0;
        plotPanel.add(cntlPanel, gbc_ctrlPanel);
        
        GridBagLayout gbl_cntlPanel = new GridBagLayout();
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);

        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.anchor = GridBagConstraints.EAST;
        gbc_label.insets = new Insets(3, 3, 3, 0);
        gbc_label.gridx = 0;
        gbc_label.gridy = 0;
        
        GridBagConstraints gbc_column = new GridBagConstraints();
        gbc_column.anchor = GridBagConstraints.WEST;
        gbc_column.insets = insets3;
        gbc_column.gridx = 1;
        gbc_column.gridy = 0;
        
        cntlPanel.add(new JLabel("Pulls"), gbc_label);
        
        pullsComboBox = new JComboBox<String>();
        pullsComboBox.setPrototypeDisplayValue(prototypeDisplayValue);
        cntlPanel.add(pullsComboBox, gbc_column);
    	
        gbc_label.gridx += 2;
        JButton btnGoButton = new JButton("View");
        btnGoButton.setActionCommand("view");
        btnGoButton.addActionListener(this);
        cntlPanel.add(btnGoButton, gbc_label);
        
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

        plot3d.setAxisLabel(0, xAxisName);
        plot3d.setAxisLabel(1, yVEAxisName);
        plot3d.setAxisLabel(2, zVEAxisName);
        
        GridBagConstraints gbc_chartPanel = new GridBagConstraints();
        gbc_chartPanel.anchor = GridBagConstraints.CENTER;
        gbc_chartPanel.insets = insets3;
        gbc_chartPanel.weightx = 1.0;
        gbc_chartPanel.weighty = 1.0;
        gbc_chartPanel.fill = GridBagConstraints.BOTH;
        gbc_chartPanel.gridx = 0;
        gbc_chartPanel.gridy = 1;
        plotPanel.add(plot3d, gbc_chartPanel);
    }

    private void view3dPlots() {
    	if (pullsComboBox.getSelectedItem() == null || pullsComboBox.getSelectedItem().toString().isEmpty())
    		return;
        plot3d.removeAllPlots();
        String pullName = pullsComboBox.getSelectedItem().toString();
    	int idx = Integer.parseInt(pullName.substring(pullIndexReplaceString.length())) - 1;
		JTable table = runTables[idx];
		int veIdx = table.getColumnCount() - 1;
        String val;
        int xColIdx = 0;
        int yColIdx = veIdx;
        int z1ColIdx = 1;
        int z2ColIdx = (veIdx == 3 ? 2 : -1);
    	double x, y, z1;
    	double z2 = 0;
    	ArrayList<double[]> temp = new ArrayList<double[]>();
        for (int i = 0; i < table.getRowCount(); ++i) {
            val = table.getValueAt(i, xColIdx).toString();
            if (val.equals(""))
            	break;
            x = Double.valueOf(val);
            val = table.getValueAt(i, yColIdx).toString();
            y = Double.valueOf(val);
            val = table.getValueAt(i, z1ColIdx).toString();
            z1 = Double.valueOf(val);
            if (z2ColIdx != -1) {
	            val = (String)table.getValueAt(i, z2ColIdx).toString();
	            z2 = Double.valueOf(val);
            }
            temp.add(new double[]{x, y, z1, z2});
        }
    	double[][] xyzArray1 = new double[temp.size()][3];
    	double[][] xyzArray2 = new double[temp.size()][3];
    	for (int i = 0; i < temp.size(); ++i) {
    		xyzArray1[i][0] = temp.get(i)[0];
    		xyzArray1[i][1] = temp.get(i)[1];
    		xyzArray1[i][2] = temp.get(i)[2];
            if (z2ColIdx != -1) {
	    		xyzArray2[i][0] = temp.get(i)[0];
	    		xyzArray2[i][1] = temp.get(i)[1];
	    		xyzArray2[i][2] = temp.get(i)[3];
            }
    	}
        plot3d.addScatterPlot(pullName + " VVT1", Color.BLUE, xyzArray1);
        plot3d.addScatterPlot(pullName + " VVT2", Color.RED, xyzArray2);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE USAGE TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    protected String usage() {
        ResourceBundle bundle;
        bundle = ResourceBundle.getBundle("com.vgi.mafscaling.vvt_calc");
        return bundle.getString("usage"); 
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // COMMOMN ACTIONS RELATED FUNCTIONS
    //////////////////////////////////////////////////////////////////////////////////////

    private boolean getColumnsFilters(String[] elements) {
    	boolean ret = true;
        ArrayList<String> columns = new ArrayList<String>(Arrays.asList(elements));
        String logThtlAngleColName = Config.getThrottleAngleColumnName();
        String logRpmColName = Config.getRpmColumnName();
        String logVvt1ColName = Config.getVvt1ColumnName();
        String logVvt2ColName = Config.getVvt2ColumnName();
        String logIatColName = Config.getIatColumnName();
        String logMapColName = Config.getMapColumnName();
        String logMafColName = Config.getMassAirflowColumnName();
        logThtlAngleColIdx = columns.indexOf(logThtlAngleColName);
        logRpmColIdx = columns.indexOf(logRpmColName);
        logVvt1ColIdx = columns.indexOf(logVvt1ColName);
        logVvt2ColIdx = columns.indexOf(logVvt2ColName);
        logIatColIdx = columns.indexOf(logIatColName);
        logMapColIdx = columns.indexOf(logMapColName);
        logMafColIdx = columns.indexOf(logMafColName);
        if (logThtlAngleColIdx == -1)    { Config.setThrottleAngleColumnName(Config.NO_NAME); ret = false; }
        if (logRpmColIdx == -1)          { Config.setRpmColumnName(Config.NO_NAME);           ret = false; }
        if (logVvt1ColIdx == -1)         { Config.setVvt1ColumnName(Config.NO_NAME);          ret = false; }
        if (logVvt2ColIdx == -1)         { Config.setVvt2ColumnName(Config.NO_NAME);          }
        if (logIatColIdx == -1)          { Config.setIatColumnName(Config.NO_NAME);           ret = false; }
        if (logMapColIdx == -1)          { Config.setMapColumnName(Config.NO_NAME);           ret = false; }
        if (logMafColIdx == -1)          { Config.setMassAirflowColumnName(Config.NO_NAME);   ret = false; }
        wotPoint = Config.getWOTStationaryPointValue();
        skipRowsOnTransition = Config.getOLCLTransitionSkipRows();
        temperScale = Config.getTemperatureScale();
        mapUnit = Config.getMapUnit();
        if (temperScale != 'F' && temperScale != 'C') {
        	JOptionPane.showMessageDialog(null, "Error, invalid temperature scale, must be 'C' or 'F'", "Error", JOptionPane.ERROR_MESSAGE);
        	ret = false;
        }
        if (mapUnit != 'P' && mapUnit != 'B' && mapUnit != 'K') {
        	JOptionPane.showMessageDialog(null, "Error, invalid MAP unit, must be 'P', 'B', or 'K'", "Error", JOptionPane.ERROR_MESSAGE);
        	ret = false;
        }
        return ret;
    }
    
    protected void loadLogFile() {
    	fileChooser.setMultiSelectionEnabled(true);
        if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(this))
            return;
        File[] files = fileChooser.getSelectedFiles();
        for (File file : files) {
	        BufferedReader br = null;
	        try {
	            br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
	            String line = br.readLine();
	            if (line != null) {
	            	String [] elements = line.split("\\s*,\\s*", -1);
	                getColumnsFilters(elements);
	                
	            	int origLogVvt2ColIdx = logVvt2ColIdx;
	                boolean resetColumns = false;
	                if (logThtlAngleColIdx >= 0 || logRpmColIdx >= 0 || logVvt1ColIdx >= 0 ||
	                	logVvt2ColIdx >= 0 || logIatColIdx >= 0 || logMapColIdx >= 0 || logMafColIdx >=0) {
	                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Would you like to reset column names or filter values?", "Columns/Filters Reset", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE))
	                    	resetColumns = true;
	                }
	
	                if (resetColumns || logThtlAngleColIdx < 0 || logRpmColIdx < 0 ||
	                	logVvt1ColIdx < 0 || logIatColIdx < 0 || logMapColIdx < 0 || logMafColIdx < 0) {
	                	ColumnsFiltersSelection selectionWindow = new VVTColumnsFiltersSelection(false);
	                	if (!selectionWindow.getUserSettings(elements) || !getColumnsFilters(elements))
	                		return;
	                }
	                if (origLogVvt2ColIdx != logVvt2ColIdx) {
	                	createLogDataTable(dataRunPanel);
	                	if (logVvt2ColIdx != -1)
	                		chartPanels[1].setVisible(true);
	                	else
	                		chartPanels[1].setVisible(false);
	                	validate();
	                	repaint();
	                }

	                String[] flds;
	                boolean wotFlag = true;
	                boolean foundWot = false;
	                double prpm = 0;
	                double rpm;
	                double map;
	                double iat;
	                double maf;
	                double ve;
	                double throttle;
	                int skipRowCount = 0;
	                int row = 0;
	                int i = 0;
	                int j = 0;
	                int k = 0;
	                for (; i < runTables.length; ++i) {
	                    if (runTables[i].getValueAt(0, 0).toString().isEmpty())
	                        break;
	                }
	                if (i == runTables.length)
	                    return;
	                setCursor(new Cursor(Cursor.WAIT_CURSOR));
	                while ((line = br.readLine()) != null) {
	                	flds = line.split("\\s*,\\s*", -1);
	                    try {
		                    throttle = Double.valueOf(flds[logThtlAngleColIdx]);
		                    if (row == 0 && throttle < 99)
		                        wotFlag = false;
		                    if (throttle < wotPoint) {
		                        if (wotFlag == true) {
		                            wotFlag = false;
		                            prpm = 0;
		                            skipRowCount = 0;
		                            j -= 1;
		                            while (j > 0 && skipRowCount < skipRowsOnTransition) {
		                            	for (k = 0; k < runTables[i].getColumnCount(); ++k)
		                            		runTables[i].setValueAt("", j, k);
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
		                            if (foundWot &&
		                            	!runTables[i].getValueAt(0, 0).equals("") &&
		                            	!runTables[i].getValueAt(1, 0).equals("") &&
		                            	!runTables[i].getValueAt(2, 0).equals("")) {
		                            	if (j > 1)
		                            		i += 1;
		                                if (i == runTables.length)
		                                    return;
		                            }
		                            if (row > 0)
		                                j = 0;
		                        }
		                        if (skipRowCount >= skipRowsOnTransition) {
		                            foundWot = true;
		                        	rpm = Double.valueOf(flds[logRpmColIdx]);
		                        	if (rpm > prpm) {
		                        		prpm = rpm;
			                        	map = Double.valueOf(flds[logMapColIdx]);
			                        	iat = Double.valueOf(flds[logIatColIdx]);
			                        	maf = Double.valueOf(flds[logMafColIdx]);
			                        	// leaving all the calcs expended for now for easy checkup
			                        	if ('F' == temperScale) {
			                        		if ('P' == mapUnit)
			                        			ve = (maf / ((map * 6894.76) / ((((iat - 32) / 1.8) + 273.15) * 287.05) * 1000)) / (121.9254 * rpm / 3456 * 0.0283 / 60) * 100;
			                        		else if ('B' == mapUnit)
			                        			ve = (maf / ((map * 100000) / ((((iat - 32) / 1.8) + 273.15) * 287.05) * 1000)) / (121.9254 * rpm / 3456 * 0.0283 / 60) * 100;
			                        		else // must be 'K' for kPa
			                        			ve = (maf / ((map * 1000) / ((((iat - 32) / 1.8) + 273.15) * 287.05) * 1000)) / (121.9254 * rpm / 3456 * 0.0283 / 60) * 100;
			                        	}
			                        	else { // must be 'C' for Celcius
			                        		if ('P' == mapUnit)
			                        			ve = (maf / ((map * 6894.76) / ((iat + 273.15) * 287.05) * 1000)) / (121.9254 * rpm / 3456 * 0.0283 / 60) * 100;
			                        		else if ('B' == mapUnit)
			                        			ve = (maf / ((map * 100000) / ((iat + 273.15) * 287.05) * 1000)) / (121.9254 * rpm / 3456 * 0.0283 / 60) * 100;
			                        		else // must be 'K' for kPa
			                        			ve = (maf / ((map * 1000) / ((iat + 273.15) * 287.05) * 1000)) / (121.9254 * rpm / 3456 * 0.0283 / 60 ) * 100;
			                        	}
			                        	k = 0;
		                            	Utils.ensureRowCount(j + 1, runTables[i]);
			                            runTables[i].setValueAt(rpm, j, k++);
			                            runTables[i].setValueAt(Double.valueOf(flds[logVvt1ColIdx]), j, k++);
			                            if (logVvt2ColIdx >= 0)
				                            runTables[i].setValueAt(Double.valueOf(flds[logVvt2ColIdx]), j, k++);
			                            runTables[i].setValueAt(ve, j, k++);
			                            j += 1;
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
	                if (j == 0) {
                    	for (k = 0; k < runTables[i].getColumnCount(); ++k)
                    		runTables[i].setValueAt("", j, k);
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
	            plotVEData();
	        }
        }
    }
    
    protected boolean getAxisData() {
    	return true;
    }

    protected boolean processLog() {
        try {
	    	int i = 0;
	    	int j = 0;
	    	int idx;
	    	double rpm, vvt, ve, x1, x2, y1, y2;
	    	ArrayList<Double> rpmArray;
	    	Double[] ve1Array = new Double[origTable.getRowCount()];
	    	Double[] ve2Array = new Double[newTable.getRowCount()];
			for (i = 0; i < runTables.length; ++i) {
				try {
					JTable table = runTables[i];
					int veIdx = table.getColumnCount() - 1;
					int vvtIdx = 1;
					rpmArray = new ArrayList<Double>();
		    		for (j = 0; j < table.getRowCount(); ++j) {
		    			if (table.getValueAt(j, 0).equals(""))
		    				break;
		    			rpmArray.add(Double.valueOf(table.getValueAt(j, 0).toString()));
		    		}
		    		if (rpmArray.size() > 1) {
		    	    	for (j = 0; j < origTable.getRowCount(); ++j) {
		    	    		if (origTable.getValueAt(j, 0).equals(""))
		    	    			break;	    	    			
		    	    		rpm = Double.valueOf(origTable.getValueAt(j, 0).toString());
		    	            idx = Collections.binarySearch(rpmArray, rpm);
		    	            if (idx == -1)
		    	            	continue;
		    	            if (idx < 0)
		    	                idx = -idx - 2;
		    	            if (rpmArray.size() - 1 == idx)
	    	            		continue;
		    	            x1 = rpmArray.get(idx);
		    	            x2 = rpmArray.get(idx + 1);
		    	            // vvt
		    	            y1 = Double.valueOf(table.getValueAt(idx, vvtIdx).toString());
		    	            y2 = Double.valueOf(table.getValueAt(idx, vvtIdx).toString());
		    	            vvt = Utils.linearInterpolation(rpm, x1, x2, y1, y2);
		    	            // ve
		    	            y1 = Double.valueOf(table.getValueAt(idx, veIdx).toString());
		    	            y2 = Double.valueOf(table.getValueAt(idx, veIdx).toString());
		    	            ve = Utils.linearInterpolation(rpm, x1, x2, y1, y2);

                        	Utils.ensureRowCount(j + 1, corrTable);
		    	            if (corrTable.getValueAt(j, 0).equals("")) {
		    	            	corrTable.setValueAt(vvt, j, 0);
		    	            	ve1Array[j] = ve;
		    	            }
		    	            else if (ve > ve1Array[j]) {
		    	            	corrTable.setValueAt(vvt, j, 0);
		    	            	ve1Array[j] = ve;
		    	            }
		    	    	}
			    		if (veIdx == 3) {
			    			vvtIdx = 2;
			    	    	for (j = 0; j < newTable.getRowCount(); ++j) {
			    	    		if (newTable.getValueAt(j, 0).equals(""))
			    	    			break;
			    	    		rpm = Double.valueOf(newTable.getValueAt(j, 0).toString());
			    	            idx = Collections.binarySearch(rpmArray, rpm);
			    	            if (idx == -1)
			    	            	continue;
			    	            if (idx < 0)
			    	                idx = -idx - 2;
			    	            if (rpmArray.size() - 1 == idx)
			    	            		continue;
			    	            x1 = rpmArray.get(idx);
			    	            x2 = rpmArray.get(idx + 1);
			    	            // vvt
			    	            y1 = Double.valueOf(table.getValueAt(idx, vvtIdx).toString());
			    	            y2 = Double.valueOf(table.getValueAt(idx, vvtIdx).toString());
			    	            vvt = Utils.linearInterpolation(rpm, x1, x2, y1, y2);
			    	            // ve
			    	            y1 = Double.valueOf(table.getValueAt(idx, veIdx).toString());
			    	            y2 = Double.valueOf(table.getValueAt(idx, veIdx).toString());
			    	            ve = Utils.linearInterpolation(rpm, x1, x2, y1, y2);

	                        	Utils.ensureRowCount(j + 1, corrTable);
			    	            if (corrCountTable.getValueAt(j, 0).equals("")) {
			    	            	corrCountTable.setValueAt(vvt, j, 0);
			    	            	ve2Array[j] = ve;
			    	            }
			    	            else if (ve > ve2Array[j]) {
			    	            	corrCountTable.setValueAt(vvt, j, 0);
			    	            	ve2Array[j] = ve;
			    	            }
			    	    	}
			    		}
		    		}
		        }
		        catch (NumberFormatException e) {
		            logger.error(e);
		            JOptionPane.showMessageDialog(null, "Error parsing number from Pull " + (i + 1) + " table, row " + j + ": " + e, "Error", JOptionPane.ERROR_MESSAGE);
		            return false;
		        }
	    	}
    		plotBestVVT(ve1Array, ve2Array);
	        return true;
        }
        catch (Exception e) {
        	e.printStackTrace();
            logger.error(e);
            JOptionPane.showMessageDialog(null, e, "Error processing data", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    
    protected void plotBestVVT(Double[] ve1Array, Double[] ve2Array) {
    	XYSeriesCollection dataset1 = (XYSeriesCollection) chartPanels[0].getChart().getXYPlot().getDataset(1);
    	XYSeriesCollection dataset2 = (XYSeriesCollection) chartPanels[1].getChart().getXYPlot().getDataset(1);
    	XYSeriesCollection dataset3 = (XYSeriesCollection) chartPanels[2].getChart().getXYPlot().getDataset(1);
    	dataset1.removeAllSeries();
    	dataset2.removeAllSeries();
    	dataset3.removeAllSeries();
    	vvt1BestSeries.clear();
    	vvt2BestSeries.clear();
    	ve1BestSeries.clear();
    	ve2BestSeries.clear();
    	String val;
    	double rpm;
    	for (int i = 0; i < corrTable.getRowCount(); ++i) {
    		val = corrTable.getValueAt(i, 0).toString();
    		if (!val.equals("")) {
    			rpm = Double.parseDouble(origTable.getValueAt(i, 0).toString());
    			vvt1BestSeries.add(rpm, Double.parseDouble(val), false);
    			ve1BestSeries.add(rpm, ve1Array[i]);
    		}
    	}
    	for (int i = 0; i < corrCountTable.getRowCount(); ++i) {
    		val = corrCountTable.getValueAt(i, 0).toString();
    		if (!val.equals("")) {
    			rpm = Double.parseDouble(newTable.getValueAt(i, 0).toString());
    			vvt2BestSeries.add(rpm, Double.parseDouble(val), false);
    			ve2BestSeries.add(rpm, ve2Array[i]);
    		}
    	}
    	if (vvt1BestSeries.getItemCount() > 0)
    		dataset1.addSeries(vvt1BestSeries);
    	if (vvt2BestSeries.getItemCount() > 0)
    		dataset2.addSeries(vvt2BestSeries);
    	if (ve1BestSeries.getItemCount() > 0)
    		dataset3.addSeries(ve1BestSeries);
    	if (ve2BestSeries.getItemCount() > 0)
    		dataset3.addSeries(ve2BestSeries);
    }

    protected boolean displayData() {
    	try {
	        return true;
	    }
	    catch (Exception e) {
	        logger.error(e);
	        JOptionPane.showMessageDialog(null, e, "Error processing data", JOptionPane.ERROR_MESSAGE);
	    }
	    return false;    	
	}
    
    private void clearChartData() {
        runData.clear();
        XYSeriesCollection dataset;
        for (ChartPanel chartPanel : chartPanels) {
	        XYPlot plot = chartPanel.getChart().getXYPlot();
	        dataset = (XYSeriesCollection)plot.getDataset(0);
	        dataset.removeAllSeries();
	        dataset = (XYSeriesCollection)plot.getDataset(1);
	        dataset.removeAllSeries();
        }
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)pullTree.getModel().getRoot();
		root.removeAllChildren();
		((DefaultTreeModel )pullTree.getModel()).reload(root);
		runSeries = new XYSeries[3][RunCount];
    	vvt1BestSeries.clear();
    	vvt2BestSeries.clear();
    	ve1BestSeries.clear();
    	ve2BestSeries.clear();
        plot3d.removeAllPlots();
        pullsComboBox.removeAllItems();
        pullsComboBox.addItem("");
    }
    
    private void saveConfig() {
    	StringBuffer sb = new StringBuffer("");
    	for (int i = 0; i < origTable.getRowCount(); ++i) {
    		String val = origTable.getValueAt(i, 0).toString();
    		if (!val.equals(""))
    			sb.append(origTable.getValueAt(i, 0).toString()).append(',');
    	}
    	if (sb.length() > 0)
    		sb.deleteCharAt(sb.length() - 1);
        Config.setVVT1RPMColumn(sb.toString());
        sb = new StringBuffer("");
    	for (int i = 0; i < newTable.getRowCount(); ++i) {
    		String val = newTable.getValueAt(i, 0).toString();
    		if (!val.equals(""))
    			sb.append(newTable.getValueAt(i, 0).toString()).append(',');
    	}
    	if (sb.length() > 0)
    		sb.deleteCharAt(sb.length() - 1);
        Config.setVVT2RPMColumn(sb.toString());
    }
    
    private void loadConfig() {
        String[] vvt1RpmColumn = Config.getVVT1RPMColumn().split("\\s*,\\s*", -1);
        if (vvt1RpmColumn != null) {
	        for (int i = 0; i < vvt1RpmColumn.length; ++i) {
            	Utils.ensureRowCount(i + 1, origTable);
            	Utils.ensureRowCount(i + 1, corrTable);
            	origTable.setValueAt(vvt1RpmColumn[i], i, 0);
	        }
	    	validateTable(origTable);
        }
        String[] vvt2RpmColumn = Config.getVVT1RPMColumn().split("\\s*,\\s*", -1);
        if (vvt2RpmColumn != null) {
	        for (int i = 0; i < vvt2RpmColumn.length; ++i) {
            	Utils.ensureRowCount(i + 1, newTable);
            	Utils.ensureRowCount(i + 1, corrCountTable);
            	newTable.setValueAt(vvt2RpmColumn[i], i, 0);
	        }
	    	validateTable(newTable);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (checkActionPerformed(e))
        	return;
		else if ("view".equals(e.getActionCommand()))
			view3dPlots();
		else if ("save".equals(e.getActionCommand()))
			saveConfig();
    }
}

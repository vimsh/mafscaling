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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
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

public abstract class ACompCalc extends FCTabbedPane implements ActionListener, IMafChartHolder {
    private static final long serialVersionUID = 5944026307547635365L;
    private static final Logger logger = Logger.getLogger(ACompCalc.class);
    protected static final String dvdtAxisName = "dV / dt";
    protected static final String trendDataName = "Trend";
    protected static final int ColumnWidth = 70;
    protected static final int TableWidth = 250;
    protected static final int TableRowCount = 20;
    protected static final int LogDataRowCount = 200;
    protected JScrollPane dataScrollPane = null;
    protected JTable origTable = null;
    protected JTable newTable = null;
    protected JTable corrTable = null;
    protected JTable corrCountTable = null;
    protected JTable logDataTable = null;
    protected JCheckBox compareTableCheckBox = null;
    protected ButtonGroup rbGroup = null;
    protected ChartPanel chartPanel = null;
    protected Plot3DPanel plot3d = null;
    protected ArrayList<ExcelAdapter> excelAdapterList = new ArrayList<ExcelAdapter>();
    protected ArrayList<Double> xAxisArray = null;
    protected ArrayList<Double> yAxisArray = null;
    protected ArrayList<ArrayList<Double>> savedNewTable = new ArrayList<ArrayList<Double>>();
    protected List<XYSeries> corrData = new ArrayList<XYSeries>();
    protected XYSeries runData = new XYSeries(dvdtAxisName);
    protected XYSeries trendData = new XYSeries(trendDataName);
    protected String origTableName;
    protected String newTableName;
    protected String corrTableName;
    protected String corrCountTableName;
    protected String x3dAxisName;
    protected String y3dAxisName;
    protected String z3dAxisName;
    protected Insets insets0 = new Insets(0, 0, 0, 0);
    protected Insets insets1 = new Insets(1, 1, 1, 1);
    protected Insets insets2 = new Insets(2, 2, 2, 2);
    protected Insets insets3 = new Insets(3, 3, 3, 3);
    protected String[] optionButtons = { "Yes", "No", "No to all" };

    public ACompCalc(int tabPlacement) {
        super(tabPlacement);
    }
    
    protected abstract void createControlPanel(JPanel dataPanel);
    protected abstract void createGraphTab();
    protected abstract void loadLogFile();
    protected abstract boolean processLog();
    protected abstract boolean displayData();
    protected abstract void formatTable(JTable table);
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

    protected void initialize(String[] logColumns) {
        createDataTab(logColumns);
        createGraphTab();
        create3dGraphTab();
        createUsageTab();
    }
    
    protected void createDataTab(String[] logColumns) {
        JPanel dataPanel = new JPanel();
        add(dataPanel, "<html><div style='text-align: center;'>D<br>a<br>t<br>a</div></html>");
        GridBagLayout gbl_dataPanel = new GridBagLayout();
        gbl_dataPanel.columnWidths = new int[] {0, 0};
        gbl_dataPanel.rowHeights = new int[] {0, 0};
        gbl_dataPanel.columnWeights = new double[]{0.0, 0.0};
        gbl_dataPanel.rowWeights = new double[]{0.0, 1.0};
        dataPanel.setLayout(gbl_dataPanel);
        
        createControlPanel(dataPanel);
        createDataPanel(dataPanel, logColumns);
    }

    protected void createDataPanel(JPanel dataPanel, String[] logColumns) {
        dataScrollPane = new JScrollPane();
        GridBagConstraints gbc_dataScrollPane = new GridBagConstraints();
        gbc_dataScrollPane.weightx = 0;
        gbc_dataScrollPane.weighty = 1.0;
        gbc_dataScrollPane.fill = GridBagConstraints.BOTH;
        gbc_dataScrollPane.ipadx = ColumnWidth * logColumns.length;
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

        JScrollPane dataTablesScrollPane = new JScrollPane();
        GridBagConstraints gbc_dataTablesScrollPanee = new GridBagConstraints();
        gbc_dataTablesScrollPanee.weightx = 1.0;
        gbc_dataTablesScrollPanee.weighty = 1.0;
        gbc_dataTablesScrollPanee.anchor = GridBagConstraints.PAGE_START;
        gbc_dataTablesScrollPanee.fill = GridBagConstraints.BOTH;
        gbc_dataTablesScrollPanee.gridx = 1;
        gbc_dataTablesScrollPanee.gridy = 1;
        dataPanel.add(dataTablesScrollPane, gbc_dataTablesScrollPanee);
        
        JPanel tablesPanel = new JPanel();
        dataTablesScrollPane.setViewportView(tablesPanel);
        GridBagLayout gbl_tablesPanel = new GridBagLayout();
        gbl_tablesPanel.columnWidths = new int[]{0, 0};
        gbl_tablesPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
        gbl_tablesPanel.columnWeights = new double[]{0.0, 1.0};
        gbl_tablesPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        tablesPanel.setLayout(gbl_tablesPanel);

        createLogDataTable(dataRunPanel, logColumns);
        createDataTables(tablesPanel);
    }
    
    protected void createLogDataTable(JPanel panel, String[] columns) {
            logDataTable = new JTable();
            logDataTable.getTableHeader().setReorderingAllowed(false);
            logDataTable.setModel(new DefaultTableModel(LogDataRowCount, columns.length));
            logDataTable.setColumnSelectionAllowed(true);
            logDataTable.setCellSelectionEnabled(true);
            logDataTable.setBorder(new LineBorder(new Color(0, 0, 0)));
            logDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
            logDataTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            
            for (int i = 0; i < columns.length; ++i)
                logDataTable.getColumnModel().getColumn(i).setHeaderValue(columns[i]);
            Utils.initializeTable(logDataTable, ColumnWidth);

            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = insets0;
            gbc.gridx = 0;
            gbc.gridy = 0;

            JTableHeader header = logDataTable.getTableHeader();
            panel.add(header, gbc);
            
            gbc.gridy = 1;
            panel.add(logDataTable, gbc);

            ExcelAdapter excelAdapter = new ExcelAdapter();
            excelAdapter.addTable(logDataTable, true, false);
            excelAdapterList.add(excelAdapter);
    }
    
    protected void create3dGraphTab() {
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
        gbl_chartPanel.insets = insets3;
        gbl_chartPanel.fill = GridBagConstraints.BOTH;
        gbl_chartPanel.gridx = 0;
        gbl_chartPanel.gridy = 0;
        plotPanel.add(plot3d, gbl_chartPanel);
    }
    
    protected void createDataTables(JPanel panel) {
        origTable = createDataTable(panel, origTableName, 0, true);
        newTable = createDataTable(panel, newTableName, 2, false);
        corrTable = createDataTable(panel, corrTableName, 4, false);
        corrCountTable = createDataTable(panel, corrCountTableName, 6, false);
    }
    
    protected JTable createDataTable(JPanel panel, String tableName, int gridy, boolean isOriginalTable) {
        return createDataTable(panel, tableName, TableRowCount, TableRowCount, 0, gridy, isOriginalTable, true, true);
    }
    
    protected JTable createDataTable(JPanel panel, String tableName, int colCount, int gridx, boolean isOriginalTable) {
        return createDataTable(panel, tableName, colCount, TableRowCount, gridx, 0, isOriginalTable, true, false);
    }
    
    private JTable createDataTable(JPanel panel, String tableName, int colCount, int rowCount, int gridx, int gridy, boolean isOriginalTable, boolean extendRows, boolean extendCols) {
        final JTable table;
        ExcelAdapter excelAdapter;
        if (isOriginalTable) {
            table = new JTable() {
                private static final long serialVersionUID = 3218402382894083287L;
                public boolean isCellEditable(int row, int column) { return false; };
            };
            excelAdapter = new ExcelAdapter() {
                protected void onPaste(JTable table, boolean extendRows, boolean extendCols) {
                    super.onPaste(table, extendRows, extendCols);
                    validateTable(table);
                    clearRunTables();
                }
            };
            excelAdapter.addTable(table, false, true, false, false, true, true, false, extendRows, extendCols);
            excelAdapterList.add(excelAdapter);
        }
        else {
            table = new JTable() {
                private static final long serialVersionUID = -3754572906310312568L;
            };
            excelAdapter = new ExcelAdapter();
            excelAdapter.addTable(table, false, true, false, false, true, true, false, extendRows, extendCols);
            excelAdapterList.add(excelAdapter);
        }
        DefaultTableColumnModel tableModel = new DefaultTableColumnModel();
        final TableColumn tableColumn = new TableColumn(0, (colCount == 1 ? ColumnWidth : TableWidth));
        tableColumn.setHeaderValue(tableName);
        tableModel.addColumn(tableColumn);
        JTableHeader lblTableHeaderName = table.getTableHeader();
        lblTableHeaderName.setColumnModel(tableModel);
        lblTableHeaderName.setReorderingAllowed(false);
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) lblTableHeaderName.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        
        GridBagConstraints gbc_lblTableName = new GridBagConstraints();
        gbc_lblTableName.insets = new Insets((gridy == 0 ? 0 : 5),0,0,0);
        gbc_lblTableName.anchor = GridBagConstraints.NORTHWEST;
        gbc_lblTableName.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblTableName.gridx = gridx;
        gbc_lblTableName.gridy = gridy;
        panel.add(lblTableHeaderName, gbc_lblTableName);
        
        table.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                tableColumn.setWidth(table.getWidth());
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent event) {
                JTable evenTable =(JTable)event.getSource();
                int[] cols = evenTable.getSelectedColumns();
                int[] rows = evenTable.getSelectedRows();
                int lastColIdx = cols.length - 1;
                int lastRowIdx = rows.length - 1;
                JTable[] tables = new JTable[] {origTable, newTable, corrTable, corrCountTable};
                for (JTable t : tables) {
                    if (t == null || t == evenTable)
                        continue;
                    if (t.getColumnCount() - 1 >= cols[lastColIdx] && t.getRowCount() - 1 >= rows[lastRowIdx]) {
                        t.setColumnSelectionInterval(cols[0], cols[lastColIdx]);
                        t.setRowSelectionInterval(rows[0], rows[lastRowIdx]);
                    }
                }
            }
        });
        
        table.setName(tableName);
        table.getTableHeader().setReorderingAllowed(false);
        table.setColumnSelectionAllowed(true);
        table.setCellSelectionEnabled(true);
        table.setBorder(new LineBorder(new Color(0, 0, 0)));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setModel(new DefaultTableModel(rowCount, colCount));
        Utils.initializeTable(table, ColumnWidth);
        
        formatTable(table);
        
        GridBagConstraints gbc_table = new GridBagConstraints();
        gbc_table.insets = insets0;
        gbc_table.anchor = GridBagConstraints.NORTHWEST;
        gbc_table.gridx = gridx;
        gbc_table.gridy = gridy + 1;
        panel.add(table, gbc_table);

        return table;
    }

    protected void createChart(JPanel plotPanel, String xAxisName, String yAxisName) {
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

        NumberAxis xAxis = new NumberAxis(xAxisName);
        xAxis.setAutoRangeIncludesZero(false);
        NumberAxis yAxis = new NumberAxis(yAxisName);
        yAxis.setAutoRangeIncludesZero(false);
        
        XYSeriesCollection scatterDataset = new XYSeriesCollection(runData);
        XYSeriesCollection lineDataset = new XYSeriesCollection();

        trendData.setDescription(trendDataName);
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
    
    protected void createUsageTab() {
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

    protected void addLabel(JPanel panel, int column, String text) {
        JLabel label = new JLabel(text);
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.anchor = GridBagConstraints.EAST;
        gbc_label.insets = new Insets(2, 4, 2, 0);
        gbc_label.gridx = column;
        gbc_label.gridy = 0;
        panel.add(label, gbc_label);
    }
    
    protected JComboBox<String> addComboBox(JPanel panel, int column, String[] values) {
        JComboBox<String> combo = new JComboBox<String>(values);
        combo.setSelectedIndex(0);
        GridBagConstraints gbc_combo = new GridBagConstraints();
        gbc_combo.anchor = GridBagConstraints.WEST;
        gbc_combo.insets = insets2;
        gbc_combo.gridx = column;
        gbc_combo.gridy = 0;
        panel.add(combo, gbc_combo);
        return combo;
    }
    
    protected JCheckBox addCheckBox(JPanel panel, int column, String name, String action) {
        JCheckBox check = new JCheckBox(name);
        check.setActionCommand(action);
        check.addActionListener(this);
        GridBagConstraints gbc_check = new GridBagConstraints();
        gbc_check.insets = new Insets(2, 2, 2, 0);
        gbc_check.gridx = column;
        gbc_check.gridy = 0;
        panel.add(check, gbc_check);
        return check;
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
    
    protected boolean getAxisData() {
        try {
            if (Utils.isTableEmpty(origTable)) {
                JOptionPane.showMessageDialog(null, "PLease paste " + origTable.getName() + " table into top grid", "Error getting Engine Load Compensation table headers", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            xAxisArray = new ArrayList<Double>();
            yAxisArray = new ArrayList<Double>();
            for (int i = 1; i < origTable.getColumnCount(); ++i)
                xAxisArray.add(Double.valueOf(origTable.getValueAt(0, i).toString()));
            for (int i = 1; i < origTable.getRowCount(); ++i)
                yAxisArray.add(Double.valueOf(origTable.getValueAt(i, 0).toString()));
            if (xAxisArray.size() > 0 && yAxisArray.size() > 0)
                return true;
        }
        catch (Exception e) {
            logger.error(e);
            JOptionPane.showMessageDialog(null, "Invalid value in " + origTable.getName() + " table: " + e, "Error getting Engine Load Compensation table headers", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    
    protected void clearTables() {
        clearRunTable(origTable);
        clearRunTables();
    }
    
    protected void clearLogDataTables() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            while (LogDataRowCount < logDataTable.getRowCount())
                Utils.removeRow(LogDataRowCount, logDataTable);
            Utils.clearTable(logDataTable);
            clear2dChartData();
            plot3d.removeAllPlots();
        }
        finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    protected int getLogTableEmptyRow() {
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
    
    protected boolean validateTable(JTable table) {
        if (table == null)
            return false;
        // check if table is empty
        if (Utils.isTableEmpty(table))
            return true;
        // check paste format
        if (!table.getValueAt(0, 0).toString().equalsIgnoreCase("[table3d]") &&
            ((table.getColumnCount() > 1 || table.getRowCount() > 1) && !((table.getValueAt(0, 0).toString().equals(""))) &&
            (table.getColumnCount() > 1 && Pattern.matches(Utils.fpRegex, table.getValueAt(0, 1).toString())) &&
            (table.getRowCount() > 1 && Pattern.matches(Utils.fpRegex, table.getValueAt(1, 0).toString())))) {
            JOptionPane.showMessageDialog(null, "Pasted data doesn't seem to be a valid table with row/column headers.\n\nPlease paste " + table.getName() + " table into first cell", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (table.getValueAt(0, 0).toString().equalsIgnoreCase("[table3d]")) {
            // realign if paste is from RomRaider
            if (table.getColumnCount() > 1 && table.getValueAt(0, 1).toString().equals("")) {
                Utils.removeRow(0, table);
                for (int i = table.getColumnCount() - 2; i >= 0; --i)
                    table.setValueAt(table.getValueAt(0, i), 0, i + 1);
                table.setValueAt("", 0, 0);
            }
            // paste is probably from excel
            else {
                // just blank out the first cell if tabe is 3D
                if (table.getColumnCount() > 1)
                    table.setValueAt("", 0, 0);
                else
                    Utils.removeRow(0, table);
            }
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
    
    protected void calculate() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            clearRunTables();
            if (getAxisData() && processLog())
                displayData();
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

    protected boolean plotTime2dChartData(String xAxisName, ArrayList<Double> xarr, String yAxisName, ArrayList<Double> yarr) {
        clear2dChartData();
        if (setXYSeries(runData, xarr, yarr)) {
            double paddingX = runData.getMaxX() * 0.05;
            double paddingY = runData.getMaxY() * 0.05;
            XYPlot plot = chartPanel.getChart().getXYPlot();
            plot.getDomainAxis(0).setRange(runData.getMinX() - paddingX, runData.getMaxX() + paddingX);
            plot.getRangeAxis(0).setRange(runData.getMinY() - paddingY, runData.getMaxY() + paddingY);
            plot.getDomainAxis(0).setLabel(xAxisName);
            plot.getRangeAxis(0).setLabel(yAxisName);
            plot.getRenderer(0).setSeriesVisible(0, false);
            return true;
        }
        return false;
    }

    protected boolean plotRel2dChartData(String xAxisName, ArrayList<Double> xarr, String yAxisName, ArrayList<Double> yarr) {
        clear2dChartData();
        if (setXYSeries(runData, xarr, yarr)) {
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
            plot.getDomainAxis(0).setLabel(xAxisName);
            plot.getRangeAxis(0).setLabel(yAxisName);
            plot.getRenderer(0).setSeriesVisible(0, true);
            return true;
        }
        return false;
    }

    protected boolean plotCorrectionData(String xAxisName, String yAxisName) {
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
        if (!Utils.isTableEmpty(corrTable)) {
            try {
                for (i = 1; i < corrTable.getColumnCount(); ++i) {
                    val = corrTable.getValueAt(0, i).toString();
                    series = new XYSeries(df.format(Double.valueOf(val)));
                    for (j = 1; j < corrTable.getRowCount(); ++j) { 
                        if (corrTable.getValueAt(j, i) != null) {
                            val = corrTable.getValueAt(j, i).toString();
                            if (!val.isEmpty())
                                series.add(Double.valueOf(corrTable.getValueAt(j, 0).toString()), Double.valueOf(val));
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
                plot.getDomainAxis(0).setLabel(xAxisName);
                plot.getRangeAxis(0).setLabel(yAxisName);
                plot.getRenderer(0).setSeriesVisible(0, false);
            }
            catch (NumberFormatException e) {
                logger.error(e);
                JOptionPane.showMessageDialog(null, "Error parsing number from " + corrTable.getName() + " table, cell(" + i + " : " + j + "): " + e, "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }
    
    protected void plot3dCorrection() {
        plot3d.removeAllPlots();
        if (!Utils.isTableEmpty(corrTable)) {
            try {
                double[] x = new double[xAxisArray.size()];
                int i = 0;
                for (i = 0; i < xAxisArray.size(); ++i)
                    x[i] = xAxisArray.get(i);
                double[] y = new double[yAxisArray.size()];
                for (i = 0; i < yAxisArray.size(); ++i)
                    y[i] = yAxisArray.get(i);
                double[][] z = Utils.doubleZArray(corrTable, x, y);
                Color[][] tableColors = Utils.generateTableColorMatrix(corrTable, 1, 1, y.length + 1, x.length + 1);
                Color[][] colors = new Color[y.length][x.length];
                for (int j = 1; j < tableColors.length; ++j)
                    for (i = 1; i < tableColors[j].length; ++i)
                        colors[j - 1][i - 1] = tableColors[j][i];                
                plot3d.addGridPlot("Average Error % Plot", colors, x, y, z);
            }
            catch (Exception e) {
                logger.error(e);
                JOptionPane.showMessageDialog(null, "Error: " + e, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    protected void clearRunTables() {
        clearRunTable(newTable);
        clearRunTable(corrTable);
        clearRunTable(corrCountTable);
        savedNewTable.clear();
        compareTableCheckBox.setSelected(false);
    }
    
    protected void clearRunTable(JTable table) {
        if (table == origTable)
            table.setModel(new DefaultTableModel(newTable.getRowCount(), newTable.getColumnCount()));
        else
            table.setModel(new DefaultTableModel(origTable.getRowCount(), origTable.getColumnCount()));
        Utils.initializeTable(table, ColumnWidth);
        formatTable(table);
    }

    protected void clear2dChartData() {
        XYPlot plot = chartPanel.getChart().getXYPlot();
        XYSeriesCollection lineDataset = (XYSeriesCollection) plot.getDataset(0);
        for (XYSeries series : corrData)
            lineDataset.removeSeries(series);
        corrData.clear();
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
    
    protected boolean setCompareTables() {
        if (origTable.getColumnCount() != newTable.getColumnCount() || origTable.getRowCount() != newTable.getRowCount())
            return false;
        ArrayList<Double> row;
        double v1, v2;
        Utils.clearTableColors(newTable);
        savedNewTable.clear();
        for (int i = 1; i < origTable.getRowCount(); ++i) {
            row = new ArrayList<Double>();
            savedNewTable.add(row);
            for (int j = 1; j < origTable.getColumnCount(); ++j) {
                try {
                    v1 = Double.valueOf(origTable.getValueAt(i, j).toString());
                    v2 = Double.valueOf(newTable.getValueAt(i, j).toString());
                    row.add(v2);
                    v2 = v2 - v1;
                    if (v2 == 0)
                        newTable.setValueAt("", i, j);
                    else
                        newTable.setValueAt(v2, i, j);
                }
                catch (Exception e) {
                    unsetCompareTables();
                    return false;
                }
            }
        }
        Utils.colorTable(newTable);
        return true;
    }
    
    protected void unsetCompareTables() {
        Utils.clearTableColors(newTable);
        ArrayList<Double> row;
        for (int i = 0; i < savedNewTable.size(); ++i) {
            row = savedNewTable.get(i);
            for (int j = 0; j < row.size(); ++j)
                newTable.setValueAt(row.get(j), i + 1, j + 1);
        }
        Utils.colorTable(newTable);
        savedNewTable.clear();
    }

    protected boolean checkActionPerformed(ActionEvent e) {
        if ("clearorig".equals(e.getActionCommand())) {
            clearTables();
        }
        else if ("clearlog".equals(e.getActionCommand())) {
            clearLogDataTables();
            clearRunTables();
        }
        else if ("clearall".equals(e.getActionCommand())) {
            clearLogDataTables();
            clearTables();
        }
        else if ("go".equals(e.getActionCommand())) {
            calculate();
        }
        else if ("loadlog".equals(e.getActionCommand())) {
            selectLogFile();
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
        else
            return false;
        return true;
    }
    
    public void onMovePoint(int itemIndex, double valueX, double valueY) {
    }
}

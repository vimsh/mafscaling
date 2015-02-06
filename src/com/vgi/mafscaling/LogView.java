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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Stack;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.StandardTickUnitSource;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.math.plot.Plot3DPanel;
import quick.dbtable.Column;
import quick.dbtable.DBTable;
import quick.dbtable.PrintProperties;
import quick.dbtable.Skin;
import quick.dbtable.Filter;

public class LogView extends FCTabbedPane implements ActionListener {
	private static final long serialVersionUID = -3803091816206090707L;
    private static final Logger logger = Logger.getLogger(LogView.class);
    
    public class XYZ {
        @Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			long temp;
			temp = Double.doubleToLongBits(x);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(y);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(z);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			XYZ other = (XYZ) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
				return false;
			if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
				return false;
			if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
				return false;
			return true;
		}
		private double x;
        private double y;
        private double z;
        XYZ(double x, double y, double z) {
        	this.x = x;
        	this.y = y;
        	this.z = z;
        }
		private LogView getOuterType() {
			return LogView.this;
		}
    }
    
    public class TableSkin extends Skin {
		private static final long serialVersionUID = 8263328522848779295L;
		Font headerFont = new Font("Arial", Font.PLAIN, 12);
		Font font = new Font("Arial", Font.PLAIN, 11);
		@SuppressWarnings("unchecked")
		public TableSkin() {
			put(Skin.HEADER_FONT,headerFont);
			put(Skin.TABLE_FONT,font);
			put(Skin.ROW_HEIGTH, new Integer(16));
			put(Skin.FOCUS_CELL_HIGHLIGHT_BORDER,new javax.swing.border.MatteBorder(2, 2, 2, 2, Color.BLACK));
      	}
    }
    
    public class SortingPopUp extends JPopupMenu implements ActionListener {
		private static final long serialVersionUID = -8399244173709551368L;
		JMenuItem sortAscending;
        JMenuItem sortDescending;
        int columnIndex;
        public SortingPopUp(int column) {
        	columnIndex = column;
        	sortAscending = new JMenuItem("Sort Ascending");
        	sortAscending.setActionCommand("sortascending");
        	sortAscending.addActionListener(this);
            add(sortAscending);
            sortDescending = new JMenuItem("Sort Descending");
            sortDescending.setActionCommand("sortdescending");
            sortDescending.addActionListener(this);
            add(sortDescending);
        }
		@Override
		public void actionPerformed(ActionEvent e) {
	        if ("sortascending".equals(e.getActionCommand()))
				sortAscending(columnIndex);
	        else if ("sortdescending".equals(e.getActionCommand()))
				sortDescending(columnIndex);
		}
    }
    
    class DoubleComparator implements quick.dbtable.Comparator {
    	public int compare(int column, Object currentData, Object nextData) {
    		Double v1 = Double.valueOf(currentData.toString());
    		Double v2 = Double.valueOf(nextData.toString());
    		return Double.compare(v1, v2);
    	}
    }
    
    public static class CompareFilter implements Filter {
    	public enum Condition {
    		NONE,
    		GREATER,
    		GREATER_EQUAL,
    		EQUAL,
    		LESS_EQUAL,
    		LESS
    	}
    	private int colId = 0;
    	private Condition condition = Condition.NONE;
    	private String filterString = "0";
    	private double filter = Double.NaN;
    	public void setColumn(int id) {
    		colId = id;
    	}
    	public void setCondition(Condition c) {
    		condition = c;
    	}
    	public void setFilter(String f) {
    		filterString = f;
    		filter = Double.valueOf(filterString);
    	}
    	public int[] filter(TableModel tm) {
    		if (Double.isNaN(filter) || Condition.NONE == condition)
    			return new int[0];
    		ArrayList<Integer> list = new ArrayList<Integer>();
    		double value;
    		int rounding = 0;
    		int i = 0;
			if (filterString.indexOf('.') != -1) {
				filterString = filterString.substring(filterString.indexOf('.'));
				rounding = filterString.length() - 1;
			}
    		for (i = 0; i < tm.getRowCount(); ++i) {
    			try {
	    			value = Double.valueOf((String)tm.getValueAt(i, colId + 1));
    			}
    			catch (Exception e) {
    				continue;
    			}
    	    	switch (condition) {
    	    	case LESS:
    	    		if (value < filter)
    	    			list.add(i);
    	    		break;
    	    	case LESS_EQUAL:
    	    		if (value <= filter)
    	    			list.add(i);
    	    		break;
    	    	case GREATER_EQUAL:
    	    		if (value >= filter)
    	    			list.add(i);
    	    		break;
    	    	case GREATER:
    	    		if (value > filter)
    	    			list.add(i);
    	    		break;
    	    	default:
    	        	double rndVal = value;
    	        	if (rounding > 0) {
    	        		double multiplier = Math.pow(10.0, rounding);
    	        		rndVal = Math.round(value * multiplier) / multiplier;
    	        	}
    	        	else
    	        		rndVal = Math.round(value);
    	    		if (rndVal == filter)
    	    			list.add(i);
    	    		break;
    	    	}
    		}
			int arr[] = new int[list.size()];
			for (i = 0; i < list.size(); ++i)
				arr[i] = list.get(i);
			return arr;
    	}
    }
    
    public class CheckBoxIcon implements Icon {
    	private Color color;
		private boolean checked;
		public CheckBoxIcon() { this.checked = false; this.color = UIManager.getColor("Panel.background"); }
		public CheckBoxIcon(boolean checked, Color color) { this.checked = checked; this.color = color; }
		public Color getColor() { return this.color; }
		public void setColor(Color color) { this.color = color; }
		public boolean isChecked() { return this.checked; }
		public void setChecked(boolean checked) { this.checked = checked; }
		@Override
		public int getIconWidth() { return 20; }
		@Override
		public int getIconHeight() { return 20; }
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
	        g.setColor(Color.BLACK);
	        g.fillRect(x + 4, y + 4, getIconWidth() - 8, getIconHeight() - 8);
	        g.setColor(color);
	        g.fillRect(x + 5, y + 5, getIconWidth() - 10, getIconHeight() - 10);
		}
	}
    
    public class CheckboxHeaderRenderer implements TableCellRenderer {
    	private final CheckBoxIcon checkIcon = new CheckBoxIcon();
    	private int colId;
    	private Color defaultColor = checkIcon.getColor();
    	public CheckboxHeaderRenderer(int col, JTableHeader header) {
    		colId = col;
    		header.addMouseListener(new MouseAdapter() {
    			@Override
    			public void mouseClicked(MouseEvent e) {
    				try {
	    				JTable table = ((JTableHeader) e.getSource()).getTable();
	    				TableColumnModel columnModel = table.getColumnModel();
	    				int viewColumn = columnModel.getColumnIndexAtX(e.getX());
	    				int modelColumn = table.convertColumnIndexToModel(viewColumn);
	    				if (colId != modelColumn)
	    					return;
	    				if (SwingUtilities.isLeftMouseButton(e) && colors.size() > 0) {
	    					checkIcon.setChecked(!checkIcon.isChecked());
		    				if (checkIcon.isChecked()) {
		    					defaultColor = checkIcon.getColor();
		    					checkIcon.setColor(colors.pop());
		    					TableModel model = table.getModel();
		    					addXYSeries(model, colId, columnModel.getColumn(viewColumn).getHeaderValue().toString(), checkIcon.getColor());
		    				}
		    				else {
		    					colors.push(checkIcon.getColor());
		    					checkIcon.setColor(defaultColor);
		    					removeXYSeries(colId);
		    				}
							((JTableHeader) e.getSource()).repaint();
	    				}
	    				else if (SwingUtilities.isRightMouseButton(e)) {
	    					SortingPopUp menu = new SortingPopUp(colId);
	    					menu.show(e.getComponent(), e.getX(), e.getY());
	    				}
    				}
    				catch (Exception ex) {
    		    		ex.printStackTrace();
    				}
    			}
    		});
    	}
    	@Override
    	public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isS, boolean hasF, int row, int col) {
    		TableCellRenderer r = tbl.getTableHeader().getDefaultRenderer();
    		JLabel label = (JLabel) r.getTableCellRendererComponent(tbl, val, isS, hasF, row, col);
    		label.setIcon(checkIcon);
    		return label;
    	}
    	
    	public CheckBoxIcon getCheckIcon() { return checkIcon; }
    	public Color getDefaultColor() { return defaultColor; }
    }
    
    class ImageListCellRenderer implements ListCellRenderer<Object> {
    	public Component getListCellRendererComponent(JList<?> jlist, Object value, int cellIndex, boolean isSelected, boolean cellHasFocus) {
    		if (value instanceof JLabel)
    			return (Component)value;
    		else
    			return new JLabel("???");
    	}
    }
    
    private ChartPanel chartPanel = null;
    private XYPlot plot = null;
    private XYSeriesCollection rpmDataset = null;
    private XYSeriesCollection dataset = null;
    private XYLineAndShapeRenderer rpmPlotRenderer = null;
    private XYLineAndShapeRenderer plotRenderer = null;
    private int rpmCol = -1;
    private int displCount = 0;
    private JPanel logViewPanel = null;
    private JToolBar toolBar = null;
    private DBTable logDataTable = null;
    private DBTFindFrame findWindow = null;
    private JScrollPane headerScrollPane = null;
    private DefaultListModel<JLabel> listModel = null;
    private JButton loadButton = null;
    private JButton printButton = null;
    private JButton previewButton = null;
    private JButton findButton = null;
    private JButton replaceButton = null;
    private JComboBox<String> selectionCombo;
    private JComboBox<String> compareCombo;
    private JTextField  filterText;
    private JButton filterButton;
    private JButton viewButton;
    private Plot3DPanel plot3d = null;
    private JComboBox<String> xAxisColumn = null;
    private JComboBox<String> yAxisColumn = null;
    private JMultiSelectionBox plotsColumn = null;
    private Font curveLabelFont = new Font("Verdana", Font.BOLD, 11);
    private Stack<Color> colors = new Stack<Color>();
    private Insets insets0 = new Insets(0, 0, 0, 0);
    private Insets insets3 = new Insets(3, 3, 3, 3);

	public LogView(int tabPlacement) {
        super(tabPlacement);
        initialize();
    }

    private void initialize() {
        createDataTab();
        createChartTab();
        createUsageTab();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // DATA TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void createDataTab() {
        JPanel dataPanel = new JPanel(new BorderLayout());
        add(dataPanel, "<html><div style='text-align: center;'>D<br>a<br>t<br>a</div></html>");
        
        createToolBar(dataPanel);
        createLogViewPanel();
        createGraghPanel();
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, logViewPanel, chartPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerLocation(150);
        dataPanel.add(splitPane);
    }

    private void createLogViewPanel() {
    	logViewPanel = new JPanel();
	    GridBagLayout gbl_logViewPanel = new GridBagLayout();
	    gbl_logViewPanel.columnWidths = new int[] {0};
	    gbl_logViewPanel.rowHeights = new int[] {0, 0};
	    gbl_logViewPanel.columnWeights = new double[]{1.0};
	    gbl_logViewPanel.rowWeights = new double[]{1.0, 0.0};
	    logViewPanel.setLayout(gbl_logViewPanel);
    	try {
	    	logDataTable = new DBTable();
	    	logDataTable.copyColumnHeaderNames = true;
	    	logDataTable.defaultClickCountToStartEditor = 2;
	    	logDataTable.doNotUseDatabaseSort = true;
	    	logDataTable.listenKeyPressEventsWholeWindow = true;
	    	logDataTable.createControlPanel(DBTable.READ_NAVIGATION);
	    	logDataTable.enableExcelCopyPaste();
	    	logDataTable.setSortEnabled(false); 
	    	logDataTable.setSkin(new TableSkin());
			logDataTable.refresh(new String[1][25]);
			logDataTable.setComparator(new DoubleComparator());
			logDataTable.getTable().setCellSelectionEnabled(true);
			logDataTable.getTable().setColumnSelectionAllowed(true);
			logDataTable.getTable().setRowSelectionAllowed(true);
			logDataTable.getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JTextField rowTextField = ((JTextField)logDataTable.getControlPanel().getComponent(3));
			rowTextField.setPreferredSize(null);
			rowTextField.setColumns(5);

	        GridBagConstraints gbc_logDataTable = new GridBagConstraints();
	        gbc_logDataTable.insets = insets0;
	        gbc_logDataTable.anchor = GridBagConstraints.PAGE_START;
	        gbc_logDataTable.fill = GridBagConstraints.BOTH;
	        gbc_logDataTable.gridx = 0;
	        gbc_logDataTable.gridy = 0;
	        logViewPanel.add(logDataTable, gbc_logDataTable);
	        listModel = new DefaultListModel<JLabel>(); 
	        
	    	selectionCombo.removeAllItems();
	    	String name;
			for (int i = 0; i < logDataTable.getColumnCount(); ++i) {
				Column col = logDataTable.getColumn(i);
				col.setNullable(true);
				col.setHeaderRenderer(new CheckboxHeaderRenderer(i + 1, logDataTable.getTableHeader()));
				name = col.getHeaderValue().toString();
				selectionCombo.addItem(name);
	    		listModel.addElement(new JLabel(name, new CheckBoxIcon(), JLabel.LEFT));
			}
			
			JList<JLabel> menuList = new JList<JLabel>(listModel);
			menuList.setOpaque(false);
			menuList.setCellRenderer(new ImageListCellRenderer());
			menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			menuList.setLayoutOrientation(JList.VERTICAL);
			menuList.setFixedCellHeight(25);
			menuList.addMouseListener(new MouseAdapter() {
    			@Override
    			public void mouseClicked(MouseEvent e) {
    				try {
    					if (e.getClickCount() == 1 && colors.size() > 0) {
        					JList<?> list = (JList<?>)e.getSource();
        					int index = list.locationToIndex(e.getPoint());
        					if (index >= 0) {
        						Column col = logDataTable.getColumn(index);
        						if (col.getHeaderRenderer() instanceof CheckboxHeaderRenderer) {
        							CheckboxHeaderRenderer renderer = (CheckboxHeaderRenderer)col.getHeaderRenderer();
	        			            JLabel label = (JLabel)list.getModel().getElementAt(index);
	        			            CheckBoxIcon checkIcon = (CheckBoxIcon)label.getIcon();
	        			            checkIcon.setChecked(!checkIcon.isChecked());
				    				if (checkIcon.isChecked()) {
				    					checkIcon.setColor(colors.pop());
				    					JTable table = logDataTable.getTable();
				    					TableModel model = table.getModel();
				    					addXYSeries(model, index + 1, col.getHeaderValue().toString(), checkIcon.getColor());
				    				}
				    				else {
				    					colors.push(checkIcon.getColor());
				    					checkIcon.setColor(renderer.getDefaultColor());
				    					removeXYSeries(index + 1);
				    				}
									list.repaint();
        						}
        					}
    					}
    				}
    				catch (Exception ex) {
    		    		ex.printStackTrace();
    				}
    			}
    		});
			
	        headerScrollPane = new JScrollPane(menuList);
	        GridBagConstraints gbc_headersTree = new GridBagConstraints();
	        gbc_headersTree.insets = insets0;
	        gbc_headersTree.anchor = GridBagConstraints.PAGE_START;
	        gbc_headersTree.fill = GridBagConstraints.BOTH;
	        gbc_headersTree.gridx = 0;
	        gbc_headersTree.gridy = 1;
			
	        logViewPanel.add(headerScrollPane, gbc_headersTree);
	        headerScrollPane.setVisible(false);
			
		}
    	catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
    }
    
    private void createToolBar(JPanel panel)
    {
		toolBar = new JToolBar();
		panel.add(toolBar, BorderLayout.NORTH);

		loadButton = addToolbarButton("Load Log File", "/open.png");
		toolBar.addSeparator();
		printButton = addToolbarButton("Print", "/print.png");
		previewButton = addToolbarButton("Print Preview", "/print_preview.png");
		findButton = addToolbarButton("Find", "/find.png");
		replaceButton = addToolbarButton("Replace", "/replace.png");
		toolBar.addSeparator();
		
		viewButton = new JButton("Headers View");
		viewButton.setMargin(new Insets(2, 7, 2, 7));
		viewButton.addActionListener(this);
		toolBar.add(viewButton);
		toolBar.addSeparator();
		
		toolBar.add(new JLabel("Filter "));
		
		JPanel filterPanel = new JPanel();
        GridBagLayout gbl_filterPanel = new GridBagLayout();
        gbl_filterPanel.columnWidths = new int[]{0, 0, 0, 0};
        gbl_filterPanel.rowHeights = new int[]{0};
        gbl_filterPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0};
        gbl_filterPanel.rowWeights = new double[]{0.0};
        filterPanel.setLayout(gbl_filterPanel);

        GridBagConstraints gbc_filter = new GridBagConstraints();
        gbc_filter.insets = insets3;
        gbc_filter.anchor = GridBagConstraints.WEST;
        gbc_filter.gridx = 0;
        gbc_filter.gridy = 0;
        
		selectionCombo = new JComboBox<String>();
		selectionCombo.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXX");
		selectionCombo.addActionListener(this);
        filterPanel.add(selectionCombo, gbc_filter);

        gbc_filter.gridx++;
		compareCombo = new JComboBox<String>(new String[] {"", "<", "<=", "=", ">=", ">"});
		compareCombo.addActionListener(this);
        filterPanel.add(compareCombo, gbc_filter);

        gbc_filter.gridx++;
		filterText = new JTextField();
		filterText.setColumns(5);
        filterPanel.add(filterText, gbc_filter);

        gbc_filter.gridx++;
		filterButton = new JButton("Set");
		filterButton.addActionListener(this);
        filterPanel.add(filterButton, gbc_filter);
		
		toolBar.add(filterPanel);
    }
    
    private void createGraghPanel() {
        JFreeChart chart = ChartFactory.createXYLineChart(null, null, null, null, PlotOrientation.VERTICAL, false, true, false);
        chartPanel = new ChartPanel(chart, true, true, true, true, true);
		chartPanel.setAutoscrolls(true);
		chart.setBackgroundPaint(new Color(60, 60, 65));

		rpmDataset = new XYSeriesCollection();
		rpmPlotRenderer = new XYLineAndShapeRenderer();		
		dataset = new XYSeriesCollection();
		plotRenderer = new XYLineAndShapeRenderer();

		NumberAxis xAxis = new NumberAxis();
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickLabelPaint(Color.WHITE);
        xAxis.setAutoRangeIncludesZero(false);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickLabelPaint(Color.WHITE);
        yAxis.setAutoRangeIncludesZero(false);
        NumberAxis y2Axis = new NumberAxis();
        y2Axis.setTickLabelsVisible(false);
        y2Axis.setTickLabelPaint(Color.WHITE);
        y2Axis.setAutoRangeIncludesZero(false);

        plot = chartPanel.getChart().getXYPlot();
        plot.setRangePannable(true);
        plot.setDomainPannable(true);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setBackgroundPaint(new Color(80, 80, 85));

        plot.setDataset(0, rpmDataset);
        plot.setRenderer(0, rpmPlotRenderer);
        plot.setDomainAxis(0, xAxis);
        plot.setRangeAxis(0, yAxis);
        plot.mapDatasetToDomainAxis(0, 0);
        plot.mapDatasetToRangeAxis(0, 0);

        plot.setDataset(1, dataset);
        plot.setRenderer(1, plotRenderer);
        plot.setRangeAxis(1, y2Axis);
        plot.mapDatasetToDomainAxis(1, 0);
        plot.mapDatasetToRangeAxis(1, 1);
        
        LegendTitle legend = new LegendTitle(plot);
        legend.setItemFont(new Font("Arial", 0, 10));
        legend.setPosition(RectangleEdge.TOP);
        legend.setItemPaint(Color.WHITE);
        chart.addLegend(legend);
        
        chartPanel.addChartMouseListener(
        	new ChartMouseListener() {
    			@Override
        		public void chartMouseMoved(ChartMouseEvent event) {
        			try {
        				plot.clearDomainMarkers();
                        Rectangle2D dataArea = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
        		        Point2D p = chartPanel.translateScreenToJava2D(event.getTrigger().getPoint());
                        double x = plot.getDomainAxis().java2DToValue(p.getX(), dataArea, plot.getDomainAxisEdge());
                        double midX = (dataArea.getMaxX() - dataArea.getMinX()) / 2;
        				int offset = 0;
        				int col = -1;
        				RectangleAnchor rectangleAnchor = null;
        				TextAnchor textAnchor = null;
        		        if (rpmDataset.getSeriesCount() > 0 && rpmPlotRenderer.isSeriesVisible(0) && x >= 0 && rpmDataset.getSeries(0).getItemCount() > (int)x) {
        		        	if (p.getX() < midX) {
	        		        	rectangleAnchor = RectangleAnchor.TOP_RIGHT;
	        		        	textAnchor = TextAnchor.TOP_LEFT;
        		        	}
        		        	else {
            		        	rectangleAnchor = RectangleAnchor.TOP_LEFT;
	        		        	textAnchor = TextAnchor.TOP_RIGHT;
        		        	}
        		        	col = rpmCol;
            		        Marker xMarker = new ValueMarker(x);
            				xMarker.setLabelAnchor(rectangleAnchor);
            				xMarker.setLabelTextAnchor(textAnchor);
            				xMarker.setPaint(Color.WHITE);
            				xMarker.setLabelFont(curveLabelFont);
            				xMarker.setLabelPaint(rpmPlotRenderer.getSeriesPaint(0));
            				xMarker.setLabel(rpmDataset.getSeries(0).getDescription() + ": " + rpmDataset.getSeries(0).getY((int)x));
            				xMarker.setLabelOffset(new RectangleInsets(2, 5, 2, 5));
            				offset += 20;
            				plot.addDomainMarker(xMarker);
        		        }
        		        for (int i = 0; i < dataset.getSeriesCount(); ++i) {
        		        	if (rectangleAnchor == null) {
            		        	if (p.getX() < midX) {
    	        		        	rectangleAnchor = RectangleAnchor.TOP_RIGHT;
    	        		        	textAnchor = TextAnchor.TOP_LEFT;
            		        	}
            		        	else {
                		        	rectangleAnchor = RectangleAnchor.TOP_LEFT;
    	        		        	textAnchor = TextAnchor.TOP_RIGHT;
            		        	}
        		        	}
	        		        if (plotRenderer.isSeriesVisible(i) && x >= 0 && dataset.getSeries(i).getItemCount() > (int)x) {
	        		        	if (col == -1)
	        		        		col = i;
	            		        Marker xMarker = new ValueMarker(x);
	            				xMarker.setLabelAnchor(rectangleAnchor);
	            				xMarker.setLabelTextAnchor(textAnchor);
	            				xMarker.setPaint(Color.WHITE);
	            				xMarker.setLabelFont(curveLabelFont);
		        				xMarker.setLabelPaint(plotRenderer.getSeriesPaint(i));
		        				xMarker.setLabel(dataset.getSeries(i).getDescription() + ": " + dataset.getSeries(i).getY((int)x));
	            				xMarker.setLabelOffset(new RectangleInsets(2 + offset, 5, 2, 5));	            				
	            				offset += 20;
		        				plot.addDomainMarker(xMarker);
	        		        }
        		        }
        		        if (col >= 0) {
	        				chartPanel.repaint();
	        				try {
	        					int slectedCol = logDataTable.getTable().getSelectedColumn();
	        					if (slectedCol < 0)
	        						slectedCol = col;
		        				if (x >= 0 && x < logDataTable.getRowCount()) {
			        				logDataTable.getTable().setRowSelectionInterval((int)x, (int)x);
			        				logDataTable.getTable().changeSelection((int)x, slectedCol, false, false);
		        				}
	        				}
	        				catch (Exception e) { /* ignore */ }
        		        }
        			}
        			catch (Exception e) {
        	    		e.printStackTrace();
        			}
    			}
				@Override
				public void chartMouseClicked(ChartMouseEvent arg0) {
				}
        	}
        );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE 3D CHART TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void createChartTab() {
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
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0};
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
        
        cntlPanel.add(new JLabel("X-Axis"), gbc_label);
        
        xAxisColumn = new JComboBox<String>();
        xAxisColumn.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXX");
        cntlPanel.add(xAxisColumn, gbc_column);
    	
        gbc_label.gridx += 2;
        cntlPanel.add(new JLabel("Y-Axis"), gbc_label);

        gbc_column.gridx += 2;
        yAxisColumn = new JComboBox<String>();
        yAxisColumn.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXX");
        cntlPanel.add(yAxisColumn, gbc_column);

        gbc_label.gridx += 2;
        cntlPanel.add(new JLabel("Plots"), gbc_label);

        gbc_column.gridx += 2;
        plotsColumn = new JMultiSelectionBox();
        plotsColumn.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXX");
        cntlPanel.add(plotsColumn, gbc_column);

        gbc_label.gridx = 7;
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
        bundle = ResourceBundle.getBundle("com.vgi.mafscaling.logview");
        return bundle.getString("usage"); 
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // WORK FUNCTIONS
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void initColors() {
    	colors.clear();
    	colors.push(new Color(255,101,0));
    	colors.push(new Color(255,154,0));
    	colors.push(new Color(255,207,0));
    	colors.push(new Color(156,207,0));
    	colors.push(new Color(49,207,206));
    	colors.push(new Color(49,101,255));
    	colors.push(new Color(255,207,156));
    	colors.push(new Color(206,154,255));
    	colors.push(new Color(255,154,206));
    	colors.push(new Color(156,207,255));
    	colors.push(new Color(255,255,156));
    	colors.push(new Color(206,255,206));
    	colors.push(new Color(206,255,255));
    	colors.push(new Color(0,207,255));
    	colors.push(new Color(0,0,255));
    	colors.push(new Color(0,130,132));
    	colors.push(new Color(132,0,0));
    	colors.push(new Color(132,0,132));
    	colors.push(new Color(0,255,255));
    	colors.push(new Color(255,255,0));
    	colors.push(new Color(255,0,255));
    	colors.push(new Color(0,0,132));
    	colors.push(new Color(206,207,255));
    	colors.push(new Color(0,101,206));
    	colors.push(new Color(255,130,132));
    	colors.push(new Color(99,0,99));
    	colors.push(new Color(206,255,255));
    	colors.push(new Color(255,255,206));
    	colors.push(new Color(156,48,99));
    	colors.push(new Color(156,154,255));
    	colors.push(new Color(221,160,221));
    	colors.push(new Color(176,196,222));
    	colors.push(new Color(32,178,170));
    	colors.push(new Color(250,240,230));
    	colors.push(new Color(210,180,140));
    	colors.push(new Color(143,188,139));
    	colors.push(new Color(219,220,37));
    	colors.push(new Color(42,214,42));
    	colors.push(new Color(241,104,60));
    	colors.push(new Color(29,139,209));
    	/*
        colors.push(Color.decode("#FF6500"));
        colors.push(Color.decode("#FF9A00"));
        colors.push(Color.decode("#FFCF00"));
        colors.push(Color.decode("#9CCF00"));
        colors.push(Color.decode("#31CFCE"));
        colors.push(Color.decode("#3165FF"));
        colors.push(Color.decode("#FFCF9C"));
        colors.push(Color.decode("#CE9AFF"));
        colors.push(Color.decode("#FF9ACE"));
        colors.push(Color.decode("#9CCFFF"));
        colors.push(Color.decode("#FFFF9C"));
        colors.push(Color.decode("#CEFFCE"));
        colors.push(Color.decode("#CEFFFF"));
        colors.push(Color.decode("#00CFFF"));
        colors.push(Color.decode("#0000FF"));
        colors.push(Color.decode("#008284"));
        colors.push(Color.decode("#840000"));
        colors.push(Color.decode("#840084"));
        colors.push(Color.decode("#00FFFF"));
        colors.push(Color.decode("#FFFF00"));
        colors.push(Color.decode("#FF00FF"));
        colors.push(Color.decode("#000084"));
        colors.push(Color.decode("#CECFFF"));
        colors.push(Color.decode("#0065CE"));
        colors.push(Color.decode("#FF8284"));
        colors.push(Color.decode("#630063"));
        colors.push(Color.decode("#CEFFFF"));
        colors.push(Color.decode("#FFFFCE"));
        colors.push(Color.decode("#9C3063"));
        colors.push(Color.decode("#9C9AFF"));
        colors.push(Color.decode("#DDA0DD"));
        colors.push(Color.decode("#B0C4DE"));
        colors.push(Color.decode("#20B2AA"));
        colors.push(Color.decode("#FAF0E6"));
        colors.push(Color.decode("#D2B48C"));
        colors.push(Color.decode("#8FBC8B"));
        colors.push(Color.decode("#DBDC25"));
        colors.push(Color.decode("#2AD62A"));
        colors.push(Color.decode("#F1683C"));
        colors.push(Color.decode("#1D8BD1"));
        */
    }
	
	private JButton addToolbarButton(String tooltip, String image) {
		JButton button = new JButton(new ImageIcon(this.getClass().getResource(image)));
		button.setToolTipText(tooltip);
		button.setMargin(insets0);
		button.setAlignmentY(Component.CENTER_ALIGNMENT);
		button.addActionListener(this);
		toolBar.add(button);
		return button;
	}
    
    private void addXYSeries(TableModel model, int column, String name, Color color) {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
    	try {
			if ((column - 1) == rpmCol) {
		        ((NumberAxis)plot.getRangeAxis(0)).setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		        plot.getRangeAxis(0).setTickLabelsVisible(true);
		        rpmPlotRenderer.setSeriesPaint(0, color);
		        rpmPlotRenderer.setSeriesVisible(0, true);
			}
			else {
		        plot.getRangeAxis(1).setTickLabelsVisible(true);
		        plotRenderer.setSeriesPaint(column - 1, color);
				plotRenderer.setSeriesVisible(column - 1, true);
				displCount += 1;
			}
    	}
    	finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    	}
    	chartPanel.revalidate();
    }
    
    private void removeXYSeries(int column) {
		if ((column - 1) == rpmCol) {
	        ((NumberAxis)plot.getRangeAxis(0)).setStandardTickUnits(new StandardTickUnitSource());
	        plot.getRangeAxis(0).setTickLabelsVisible(false);
	        rpmPlotRenderer.setSeriesVisible(0, false);
		}
		else {
			displCount -= 1;
			if (displCount == 0)
				plot.getRangeAxis(1).setTickLabelsVisible(false);
	        plotRenderer.setSeriesVisible(column - 1, false);
		}
    }
    
    private void sortAscending(int column) {
    	logDataTable.sortByColumn(column, true) ;
    }
    
    private void sortDescending(int column) {
    	logDataTable.sortByColumn(column, false) ;
    }

	private void loadLogFile() {
    	fileChooser.setMultiSelectionEnabled(false);
        if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(this))
            return;
        File file = fileChooser.getSelectedFile();
        Properties prop = new Properties();
        prop.put("delimiter", ",");
        prop.put("firstRowHasColumnNames", "true");
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
		logDataTable.filter(null);
		filterText.setText("");
        try {
        	JTableHeader header = logDataTable.getTableHeader();
        	for (MouseListener listener : header.getMouseListeners())
        		header.removeMouseListener(listener);
        	logDataTable.refresh(file.toURI().toURL(), prop);
        	Column col;
        	String colName;
        	String lcColName;
	        String val;
	        CheckboxHeaderRenderer renderer;
        	Component comp;
        	XYSeries series;
            xAxisColumn.removeAllItems();
            yAxisColumn.removeAllItems();
            plotsColumn.removeAllItems();
            xAxisColumn.addItem("");
            yAxisColumn.addItem("");
            plotsColumn.setText("");
            plot3d.removeAllPlots();
        	rpmDataset.removeAllSeries();
        	dataset.removeAllSeries();
        	rpmCol = -1;
        	displCount = 0;
        	selectionCombo.removeAllItems();
        	listModel.removeAllElements();
			for (int i = 0; i < logDataTable.getColumnCount(); ++i) {
				col = logDataTable.getColumn(i);
				renderer = new CheckboxHeaderRenderer(i + 1, logDataTable.getTableHeader());
				col.setHeaderRenderer(renderer);
				colName = col.getHeaderValue().toString();
                xAxisColumn.addItem(colName);
                yAxisColumn.addItem(colName);
                plotsColumn.addItem(colName);
		    	comp = renderer.getTableCellRendererComponent(logDataTable.getTable(), colName, false, false, 0, 0);
		    	col.setPreferredWidth(comp.getPreferredSize().width + 4);
		    	series = new XYSeries(colName);
		    	series.setDescription(colName);
		    	lcColName = colName.toLowerCase();
				dataset.addSeries(series);
				plotRenderer.setSeriesShapesVisible(i, false);
				plotRenderer.setSeriesVisible(i, false);
				selectionCombo.addItem(colName);
	    		listModel.addElement(new JLabel(colName, renderer.getCheckIcon(), JLabel.LEFT));
				if (rpmDataset.getSeriesCount() == 0 && (lcColName.matches(".*rpm.*") || lcColName.matches(".*eng.*speed.*"))) {
					rpmDataset.addSeries(series);
					rpmPlotRenderer.setSeriesShapesVisible(0, false);
					rpmPlotRenderer.setSeriesVisible(0, false);
					rpmCol = i;
				}
				for (int j = 0; j < logDataTable.getRowCount(); ++j) {
					try {
						val = (String)logDataTable.getValueAt(j, i);
						series.add(j, Double.valueOf(val), false);
					}
					catch (Exception e) {
			            JOptionPane.showMessageDialog(null, "Invalid numeric value in column " + colName + ", row " + (j + 1), "Invalid value", JOptionPane.ERROR_MESSAGE);
			            return;
					}
				}
				series.fireSeriesChanged();
			}
			if (logDataTable.getControlPanel().getComponentCount() > 7)
				logDataTable.getControlPanel().remove(7);
			logDataTable.getControlPanel().add(new JLabel("   [" + file.getName() + "]"));
			initColors();
        }
        catch (Exception ex) {
    		ex.printStackTrace();
    		logger.error(ex);
    	}
    	finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    	}
	}
	
	private void updateChart() {
		if (logDataTable.getColumnCount() != dataset.getSeriesCount())
			return;
    	Column col;
    	String colName;
        String val;
    	XYSeries series;
		int seriesIdx = 0;
		for (int i = 0; i < logDataTable.getColumnCount(); ++i) {
			col = logDataTable.getColumn(i);
			colName = col.getHeaderValue().toString();
	    	series = dataset.getSeries(seriesIdx++);
			if (!series.getDescription().equals(colName)) {
	            JOptionPane.showMessageDialog(null, "Invalid series found for the column index " + i + ": series name " + series.getDescription() + " doesn't match column name " + colName, "Invalid value", JOptionPane.ERROR_MESSAGE);
				return;
			}
			series.clear();
			for (int j = 0; j < logDataTable.getRowCount(); ++j) {
				try {
					val = (String)logDataTable.getValueAt(j, i);
					series.add(j, Double.valueOf(val), false);
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Invalid numeric value in column " + colName + ", row " + (j + 1), "Invalid value", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			series.fireSeriesChanged();
		}
		chartPanel.repaint();
	}

    private void view3dPlots() {
    	if (xAxisColumn.getSelectedItem() == null ||
    		xAxisColumn.getSelectedItem().toString().isEmpty() ||
    		yAxisColumn.getSelectedItem() == null ||
    		yAxisColumn.getSelectedItem().toString().isEmpty() ||
    		plotsColumn.getSelectedItems() == null)
    		return;
        plot3d.removeAllPlots();
        String val;
    	String xAxisColName = (String)xAxisColumn.getSelectedItem();
    	String yAxisColName = (String)yAxisColumn.getSelectedItem();
    	List<String> dataColNames = plotsColumn.getSelectedItems();
    	if (dataColNames.size() > 5) {
            JOptionPane.showMessageDialog(null, "Sorry, only 5 plots are supported. More plots will make the graph too slow.", "Too many parameters", JOptionPane.ERROR_MESSAGE);
            return;
    	}

        int xColIdx = logDataTable.getColumnByHeaderName(xAxisColName).getModelIndex() - 1;
        int yColIdx = logDataTable.getColumnByHeaderName(yAxisColName).getModelIndex() - 1;
    	ArrayList<Color> colorsArray = new ArrayList<Color>();
    	colorsArray.add(Color.BLUE);
    	colorsArray.add(Color.RED);
    	colorsArray.add(Color.GREEN);
    	colorsArray.add(Color.ORANGE);
    	colorsArray.add(Color.GRAY);
    	double x, y, z;
    	XYZ xyz;
    	for (int j = 0; j < dataColNames.size(); ++j) {
    		HashSet<XYZ> uniqueXYZ = new HashSet<XYZ>();
	        int zColIdx = logDataTable.getColumnByHeaderName(dataColNames.get(j)).getModelIndex() - 1;
	        int count = 0;
	        double[][] xyzArrayTemp = new double[logDataTable.getRowCount()][3];
	        for (int i = 0; i < logDataTable.getRowCount(); ++i) {
	            val = (String)logDataTable.getValueAt(i, xColIdx);
	            x = Double.valueOf(val);
	            val = (String)logDataTable.getValueAt(i, yColIdx);
	            y = Double.valueOf(val);
	            val = (String)logDataTable.getValueAt(i, zColIdx);
	            z = Double.valueOf(val);
	            xyz = new XYZ(x, y, z);
	            if (uniqueXYZ.contains(xyz))
	            	continue;
	            uniqueXYZ.add(xyz);
	            xyzArrayTemp[count][0] = x;
	            xyzArrayTemp[count][1] = y;
	            xyzArrayTemp[count][2] = z;
	            count += 1;
	        }
	    	double[][] xyzArray = new double[uniqueXYZ.size()][3];
	    	for (int k = 0; k < xyzArray.length; ++k)
	    		System.arraycopy(xyzArrayTemp[k], 0, xyzArray[k], 0, 3);
	        plot3d.addScatterPlot(dataColNames.get(j), colorsArray.get(j), xyzArray);
    	}
        plot3d.setAxisLabel(0, xAxisColumn.getSelectedItem().toString());
        plot3d.setAxisLabel(1, yAxisColumn.getSelectedItem().toString());
        plot3d.setAxisLabel(2, plotsColumn.getSelectedItemsString());
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == loadButton)
			loadLogFile();
		else if (e.getSource() == printButton)
			logDataTable.print(new PrintProperties());
	    else if (e.getSource() == previewButton)
	    	logDataTable.printPreview(new PrintProperties());
	    else if (e.getSource() == findButton) {
	    	if (findWindow != null)
	    		findWindow.dispose();
	    	findWindow = new DBTFindFrame(SwingUtilities.windowForComponent(this), logDataTable, true);
	    }
	    else if (e.getSource() == replaceButton) {
	    	if (findWindow != null)
	    		findWindow.dispose();
			findWindow = new DBTFindFrame(SwingUtilities.windowForComponent(this), logDataTable, false);
	    }
	    else if (e.getSource() == filterButton) {
	    	String filterString = filterText.getText();
	    	if (filterString != null && !"".equals(filterString)) {
	    		try {
		    		CompareFilter filter = new CompareFilter();
		    		filter.setCondition(CompareFilter.Condition.EQUAL);
		    		if (compareCombo.getSelectedItem().toString().equals(">"))
		    			filter.setCondition(CompareFilter.Condition.GREATER);
		    		if (compareCombo.getSelectedItem().toString().equals(">="))
		    			filter.setCondition(CompareFilter.Condition.GREATER_EQUAL);
		    		else if (compareCombo.getSelectedItem().toString().equals("<"))
		    			filter.setCondition(CompareFilter.Condition.LESS);
		    		else if (compareCombo.getSelectedItem().toString().equals("<="))
		    			filter.setCondition(CompareFilter.Condition.LESS_EQUAL);
		    		filter.setFilter(filterText.getText());
		    		filter.setColumn(selectionCombo.getSelectedIndex());
		    		logDataTable.filter(filter);
			    	updateChart();
	    		}
	    		catch (NumberFormatException ex) {
		            JOptionPane.showMessageDialog(null, "Invalid numeric value: " + filterText.getText(), "Invalid value", JOptionPane.ERROR_MESSAGE);
	    		}
	    	}
	    	else {
	    		logDataTable.filter(null);
		    	updateChart();
	    	}
	    }
	    else if (e.getSource() == viewButton) {
		    GridBagLayout gbl = (GridBagLayout)logViewPanel.getLayout();
	    	if (viewButton.getText().startsWith("Headers")) {
	    		Dimension d = viewButton.getSize();
	    		viewButton.setMinimumSize(d);
	    		viewButton.setPreferredSize(d);
	    		viewButton.setMaximumSize(d);
		        gbl.rowWeights = new double[]{0.0, 1.0};
		        logDataTable.setVisible(false);
		        headerScrollPane.setVisible(true);
	    		viewButton.setText("Grid View");
	    	}
	    	else {
		        gbl.rowWeights = new double[]{1.0, 0.0};
		        logDataTable.setVisible(true);
		        headerScrollPane.setVisible(false);
	    		viewButton.setText("Headers View");
	    	}
	    }
		else if ("view".equals(e.getActionCommand()))
			view3dPlots();
	}

}

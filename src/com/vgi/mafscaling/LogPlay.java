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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.HashSet;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIDefaults;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYSeriesLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

import quick.dbtable.Column;
import quick.dbtable.DBTable;
import quick.dbtable.Skin;

public class LogPlay extends FCTabbedPane implements ActionListener {
	private static final long serialVersionUID = -1614821051358033847L;
	private static final Logger logger = Logger.getLogger(LogPlay.class);

    public class TableHolder {
		public int xColIdx;
		public int yColIdx;
		public int zColIdx;
		public LogPlayTable table;

		TableHolder(int x, int y, int z, LogPlayTable t) {
        	xColIdx = x;
        	yColIdx = y;
        	zColIdx = z;
        	table = t;
        	table.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
        			int size;
	        		synchronized (lock) {
	                	tables.remove(new TableHolder(xColIdx, yColIdx, zColIdx, table));
	                	size = tables.size();
	        		}
                	if (size == 0)
                		stop();
	                	
                }
            });
        }
        @Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + table.hashCode();
			result = prime * result + (int) (xColIdx ^ (xColIdx >>> 32));
			result = prime * result + (int) (yColIdx ^ (yColIdx >>> 32));
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
			TableHolder other = (TableHolder) obj;
			if (xColIdx != other.xColIdx)
				return false;
			if (yColIdx != other.yColIdx)
				return false;
			if (table != other.table)
				return false;
			return true;
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
    
    public class TableHeaderRenderer implements TableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isS, boolean hasF, int row, int col) {
    		TableCellRenderer r = tbl.getTableHeader().getDefaultRenderer();
    		return (JLabel) r.getTableCellRendererComponent(tbl, val, isS, hasF, row, col);
		}

    }
    
    public class PlayAreaSelector extends JDialog {
    	private static final long serialVersionUID = 8151299923726511132L;
    	private JDialog dialog;
    	private LogPlay logPlay;
        private JComboBox<String> columnSelection = new JComboBox<String>();
        private JButton cancelButton = new JButton("Cancel");
        private JButton setButton = new JButton("Set");
        private Window parent = null;
        private ChartPanel chartPanel = null;
        private XYSeries series = new XYSeries("");
        private Stroke stroke = new BasicStroke(1.5f);
        private ValueMarker startMarker = null;
        private ValueMarker endMarker = null;
        private int columnIdx = 0;
    	
    	public PlayAreaSelector(LogPlay sp) {
        	super(SwingUtilities.windowForComponent(sp), "Select play area");
        	logPlay = sp;
        	dialog = this;
        	parent = SwingUtilities.windowForComponent(sp);
        	initialize();
    	}
    	
    	private void initialize() {
            JPanel dataPanel = new JPanel();
            GridBagLayout gbl_dataPanel = new GridBagLayout();
            gbl_dataPanel.columnWidths = new int[] {0, 0, 0, 0};
            gbl_dataPanel.rowHeights = new int[] {0, 0};
            gbl_dataPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0};
            gbl_dataPanel.rowWeights = new double[]{0.0, 1.0};
            dataPanel.setLayout(gbl_dataPanel);
            getContentPane().add(dataPanel);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.EAST;
            gbc.insets = new Insets(3, 10, 3, 0);
            gbc.gridx = 0;
            gbc.gridy = 0;
            
            dataPanel.add(new JLabel("Select Column to View"), gbc);
            
    		columnSelection.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXX");
    		for (int i = 0; i < xAxisColumn.getItemCount(); ++i)
    			columnSelection.addItem(xAxisColumn.getItemAt(i));
    		columnSelection.addItemListener(new ItemListener() {
    		    @Override
    		    public void itemStateChanged(ItemEvent event) {
    		    	if (event.getStateChange() == ItemEvent.SELECTED) {
    		    		columnIdx = columnSelection.getSelectedIndex() - 1;
    		    		if (columnIdx >= 0) {
				    		columnIdx = logDataTable.getCurrentIndexForOriginalColumn(columnIdx);
				    		series.clear();
				    		chartPanel.getChart().getXYPlot().clearDomainMarkers();
				    		startMarker = null;
				    		endMarker = null;
					    	series.setDescription((String)event.getItem());
				    		double val;
				    		for (int i = 0; i < logDataTable.getRowCount(); ++i) {
				    			val = Double.valueOf(logDataTable.getValueAt(i, columnIdx).toString());
								series.add(i, val, false);
				    		}
							series.fireSeriesChanged();
    		    		}
    		       }
    		    }       
    		});
            gbc.insets = insets3;
    		gbc.gridx = 1;
            dataPanel.add(columnSelection, gbc);

            cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					dialog.dispose();
				}
            });
    		gbc.gridx = 2;
            dataPanel.add(cancelButton, gbc);

            setButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					int start = (startMarker == null ? -1 : (int)startMarker.getValue());
					int end = (endMarker == null ? -1 : (int)endMarker.getValue());
					logPlay.setStartEndArea(start, end);
					dialog.dispose();
				}
            });
    		gbc.gridx = 3;
            gbc.insets = new Insets(3, 0, 3, 10);
            dataPanel.add(setButton, gbc);

            JFreeChart chart = ChartFactory.createXYLineChart(null, null, null, null, PlotOrientation.VERTICAL, false, true, false);
            chartPanel = new ChartPanel(chart, false, false, false, true, false);
    		chartPanel.setAutoscrolls(true);
    		chartPanel.setPopupMenu(null);
    		XYLineAndShapeRenderer plotRenderer = new XYLineAndShapeRenderer();		
			plotRenderer.setSeriesShapesVisible(0, false);
			plotRenderer.setSeriesPaint(0, Color.BLUE);
			plotRenderer.setLegendItemLabelGenerator(
        		new StandardXYSeriesLabelGenerator() {
					private static final long serialVersionUID = -2037811005529575231L;
					public String generateLabel(XYDataset dataset, int seriesid) {
						return (series.getItemCount() > 0 ? series.getDescription() : "");
					}
        		}
	        );
	        
			NumberAxis xAxis = new NumberAxis("Time");
	        xAxis.setAutoRangeIncludesZero(false);
	        NumberAxis yAxis = new NumberAxis("Value");
	        yAxis.setAutoRangeIncludesZero(false);

            final XYPlot plot = chart.getXYPlot();
            plot.setRangePannable(true);
            plot.setDomainPannable(true);
            plot.setDomainGridlinePaint(Color.DARK_GRAY);
            plot.setRangeGridlinePaint(Color.DARK_GRAY);
            plot.setBackgroundPaint(new Color(224, 224, 224));
            plot.setDataset(0, new XYSeriesCollection(series));
            plot.setRenderer(0, plotRenderer);
            plot.setDomainAxis(0, xAxis);
            plot.setRangeAxis(0, yAxis);
            plot.mapDatasetToDomainAxis(0, 0);
            plot.mapDatasetToRangeAxis(0, 0);

	        LegendTitle legend = new LegendTitle(plot.getRenderer()); 
	        legend.setItemFont(new Font("Arial", 0, 10));
	        legend.setPosition(RectangleEdge.TOP);
	        chart.addLegend(legend);

	        chartPanel.addChartMouseListener(
	        	new ChartMouseListener() {
	    			@Override
	        		public void chartMouseMoved(ChartMouseEvent event) {
        				if (columnIdx < 0)
        					return;
	        			try {
	        				int x = resetMarker(event);
	        				if (x >= 0 && x < logDataTable.getRowCount()) {
		        				logDataTable.getTable().setRowSelectionInterval(x, x);
		        				logDataTable.getTable().changeSelection(x, columnIdx, false, false);
	        				}
	        			}
	        			catch (Exception e) {
	        	    		e.printStackTrace();
	        			}
	    			}
					@Override
					public void chartMouseClicked(ChartMouseEvent event) {
        				if (columnIdx < 0)
        					return;
						if (SwingUtilities.isLeftMouseButton(event.getTrigger())) {
							if (startMarker == null) {
								startMarker = new ValueMarker(getXFromMousePosition(event));
								startMarker.setPaint(Color.GREEN);
								startMarker.setStroke(stroke);
								resetMarker(event);
							}
							else {
								startMarker = null;
								resetMarker(event);
							}
						}
						else if (SwingUtilities.isRightMouseButton(event.getTrigger())) {
							if (endMarker == null) {
								endMarker = new ValueMarker(getXFromMousePosition(event));
								endMarker.setPaint(Color.GREEN);
								endMarker.setStroke(stroke);
								resetMarker(event);
							}
							else {
								endMarker = null;
								resetMarker(event);
							}
						}
					}
	        	}
	        );

    		gbc.gridx = 0;
    		gbc.gridy = 1;
    		gbc.gridwidth = 4;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            dataPanel.add(chartPanel, gbc);
            
	        pack();
	        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	        setIconImage(null);
	        setLocationRelativeTo(parent);
	        setVisible(true);
    	}

		public int resetMarker(ChartMouseEvent event) {
			XYPlot plot = chartPanel.getChart().getXYPlot();
			plot.clearDomainMarkers();
			double x = getXFromMousePosition(event);
	        if (x >= 0 && series.getItemCount() > (int)x) {
	        	ValueMarker marker = new ValueMarker(x);
		        marker.setPaint(Color.RED);
		        marker.setStroke(stroke);
				plot.addDomainMarker(marker);
	        }
			if (startMarker != null)
				plot.addDomainMarker(startMarker);
			if (endMarker != null)
				plot.addDomainMarker(endMarker);
			chartPanel.repaint();
			return (int)x;
		}
		
		public double getXFromMousePosition(ChartMouseEvent event) {
            Rectangle2D dataArea = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
	        Point2D p = chartPanel.translateScreenToJava2D(event.getTrigger().getPoint());
			XYPlot plot = chartPanel.getChart().getXYPlot();
            return plot.getDomainAxis().java2DToValue(p.getX(), dataArea, plot.getDomainAxisEdge());
		}
    }

    private final int step = 20;
    private Timer timer = null;
    private JPanel logViewPanel = null;
    private JPanel playerPanel = null;
    private JToolBar toolBar = null;
    private DBTFindFrame findWindow = null;
    private DBTable logDataTable = null;
    private JButton loadButton = null;
    private JButton findButton = null;
    private JButton replaceButton = null;
    private JButton playButton = null;
    private JButton stopButton = null;
    private JButton ffwButton = null;
    private JButton rewButton = null;
    private JButton newPlayButton = null;
    private JButton setPlayAreaButton = null;
    private JComboBox<String> xAxisColumn = null;
    private JComboBox<String> yAxisColumn = null;
    private JComboBox<String> zAxisColumn = null;
    private JCheckBox showIntepCells = null;
    private JCheckBox showSignifCells = null;
    private JCheckBox showTraceLine = null;
    private JSlider progressBar = null;
    private HashSet<TableHolder> tables = new HashSet<TableHolder>();
    private PlayAreaSelector playAreaSelector = null;
    private Object lock = new Object();
    private volatile boolean playing = false;
    private int startPlay = -1;
    private int endPlay = -1;
    private int timerDelay = 200;
    private AtomicInteger playNext = null;
    private ImageIcon playIcon = null;
    private ImageIcon pauseIcon = null;
	private Insets insets0 = new Insets(0, 0, 0, 0);
    private Insets insets3 = new Insets(3, 3, 3, 3);
    private UIDefaults zeroInsets = new UIDefaults();

	public LogPlay(int tabPlacement) {
        super(tabPlacement);
        initialize();
    }

    private void initialize() {
    	playNext = new AtomicInteger(1);
        playIcon = new ImageIcon(getClass().getResource("/play.png"));
        pauseIcon = new ImageIcon(getClass().getResource("/pause.png"));
        zeroInsets.put("Button.contentMargins", insets0);
    	timer = new Timer(timerDelay, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	progressBar.setValue(progressBar.getValue() + playNext.get());
            }
        });
        createDataTab();
        createUsageTab();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // DATA TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void createDataTab() {
        JPanel dataPanel = new JPanel(new BorderLayout());
        add(dataPanel, "<html><div style='text-align: center;'>D<br>a<br>t<br>a</div></html>");
        createToolBar(dataPanel);

        JPanel playerLogPanel = new JPanel();
	    GridBagLayout gbl_playerLogPanel = new GridBagLayout();
	    gbl_playerLogPanel.columnWidths = new int[] {0};
	    gbl_playerLogPanel.rowHeights = new int[] {0, 1};
	    gbl_playerLogPanel.columnWeights = new double[]{1.0};
	    gbl_playerLogPanel.rowWeights = new double[]{0.0, 1.0};
	    playerLogPanel.setLayout(gbl_playerLogPanel);
        dataPanel.add(playerLogPanel);
	    
        createPlayer(playerLogPanel);
        createLogDataPanel(playerLogPanel);
    }
    
    private void createToolBar(JPanel dataPanel)
    {
		toolBar = new JToolBar();
		loadButton = addToolbarButton("Load Log File", "/open.png");
		toolBar.addSeparator();
		findButton = addToolbarButton("Find", "/find.png");
		replaceButton = addToolbarButton("Replace", "/replace.png");
		toolBar.addSeparator();
		createSelectionPanel();
		dataPanel.add(toolBar, BorderLayout.NORTH);
    }
		
	private void createSelectionPanel() {
		JPanel selectionPanel = new JPanel();
        GridBagLayout gbl_selectionPanel = new GridBagLayout();
        gbl_selectionPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
        gbl_selectionPanel.rowHeights = new int[]{0};
        gbl_selectionPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        gbl_selectionPanel.rowWeights = new double[]{0.0};
        selectionPanel.setLayout(gbl_selectionPanel);

        GridBagConstraints gbc_selection = new GridBagConstraints();
        gbc_selection.insets = insets3;
        gbc_selection.anchor = GridBagConstraints.WEST;
        gbc_selection.gridx = 0;
        gbc_selection.gridy = 0;

        selectionPanel.add(new JLabel("X-Axis"), gbc_selection);
		
        gbc_selection.gridx++;
        xAxisColumn = new JComboBox<String>();
        xAxisColumn.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXX");
        selectionPanel.add(xAxisColumn, gbc_selection);

        gbc_selection.gridx++;
        selectionPanel.add(new JLabel("Y-Axis"), gbc_selection);

        gbc_selection.gridx++;
        yAxisColumn = new JComboBox<String>();
        yAxisColumn.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXX");
        selectionPanel.add(yAxisColumn, gbc_selection);

        gbc_selection.gridx++;
        selectionPanel.add(new JLabel("Data*"), gbc_selection);

        gbc_selection.gridx++;
        zAxisColumn = new JComboBox<String>();
        zAxisColumn.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXX");
        selectionPanel.add(zAxisColumn, gbc_selection);

        gbc_selection.gridx++;
        newPlayButton = new JButton("New Play Table");
        newPlayButton.addActionListener(this);
        selectionPanel.add(newPlayButton, gbc_selection);
        
        toolBar.add(selectionPanel);
    }
    
    private void createPlayer(JPanel panel) {
    	playerPanel = new JPanel();
	    GridBagLayout gbl_playerPanel = new GridBagLayout();
	    gbl_playerPanel.columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
	    gbl_playerPanel.rowHeights = new int[] {0, 0, 0, 0};
	    gbl_playerPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
	    gbl_playerPanel.rowWeights = new double[]{0.0, 0.0};
	    playerPanel.setLayout(gbl_playerPanel);
	    
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = insets0;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

	    progressBar = new JSlider(0, 0, 0);
        progressBar.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int row = progressBar.getValue();
                if (endPlay >= 0 && endPlay < row) {
                	startPlay = -1;
                	endPlay = -1;
                	stop();
                	return;
                }
                if (logDataTable != null) {
    				logDataTable.getTable().setRowSelectionInterval(row, row);
    				int col = logDataTable.getSelectedColumn();
    				logDataTable.getTable().changeSelection(row, (col > 0 ? col : 0), false, false);
	                double x, y, z;
	                int origXCol, origYCol, origZCol;
	        		synchronized (lock) {
		                for (TableHolder tableHolder : tables) {
							try {
								origXCol = logDataTable.getCurrentIndexForOriginalColumn(tableHolder.xColIdx);
								origYCol = logDataTable.getCurrentIndexForOriginalColumn(tableHolder.yColIdx);
								origZCol = logDataTable.getCurrentIndexForOriginalColumn(tableHolder.zColIdx);
			                	x = Double.valueOf(logDataTable.getValueAt(row, origXCol).toString());
			                	y = Double.valueOf(logDataTable.getValueAt(row, origYCol).toString());
			                	if (origZCol > 0)
			                		z = Double.valueOf(logDataTable.getValueAt(row, origZCol).toString());
			                	else
			                		z = Double.NaN;
			                	tableHolder.table.setCurrentPoint(x, y, z);
							}
							catch (Exception ex) {
					            JOptionPane.showMessageDialog(null, "Invalid numeric value in column " + (tableHolder.xColIdx + 1) + ", row " + (row + 1), "Invalid value", JOptionPane.ERROR_MESSAGE);
					            return;
							}
		                }
	        		}
	                if (row == logDataTable.getRowCount() - 1)
	                	stop();
                }
            }
        });
        gbc.weightx = 1.0;
        gbc.gridwidth = gbl_playerPanel.columnWidths.length;
        playerPanel.add(progressBar, gbc);
	    
        stopButton = addPlayerButton(0, new ImageIcon(getClass().getResource("/stop.png")));
        rewButton = addPlayerButton(1, new ImageIcon(getClass().getResource("/rew.png")));
        playButton = addPlayerButton(2, playIcon);
        ffwButton = addPlayerButton(3, new ImageIcon(getClass().getResource("/ffw.png")));
        showIntepCells = addCheckBox(4, "Show interpolation cells");
        showSignifCells = addCheckBox(5, "Show significant cell");
        showTraceLine = addCheckBox(6, "Show trace line");

		setPlayAreaButton = new JButton("Set Play Area");
		setPlayAreaButton.addActionListener(this);
        GridBagConstraints gbc_setPlayAreaButton = new GridBagConstraints();
        gbc_setPlayAreaButton.insets = insets0;
        gbc_setPlayAreaButton.anchor = GridBagConstraints.WEST;
        gbc_setPlayAreaButton.gridx = 7;
        gbc_setPlayAreaButton.gridy = 1;
        playerPanel.add(setPlayAreaButton, gbc_setPlayAreaButton);

        gbc.weightx = 0.0;
        gbc.gridwidth = 0;
        panel.add(playerPanel, gbc);
    }

    private void createLogDataPanel(JPanel panel) {
    	logViewPanel = new JPanel();
	    GridBagLayout gbl_logViewPanel = new GridBagLayout();
	    gbl_logViewPanel.columnWidths = new int[] {0};
	    gbl_logViewPanel.rowHeights = new int[] {0};
	    gbl_logViewPanel.columnWeights = new double[]{1.0};
	    gbl_logViewPanel.rowWeights = new double[]{1.0};
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
			logDataTable.getTable().setCellSelectionEnabled(true);
			logDataTable.getTable().setColumnSelectionAllowed(true);
			logDataTable.getTable().setRowSelectionAllowed(true);
			logDataTable.getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JTextField rowTextField = ((JTextField)logDataTable.getControlPanel().getComponent(3));
			rowTextField.setPreferredSize(null);
			rowTextField.setColumns(5);

			for (int i = 0; i < logDataTable.getColumnCount(); ++i) {
				Column col = logDataTable.getColumn(i);
				col.setNullable(true);
				col.setHeaderRenderer(new TableHeaderRenderer());
			}
			
	        GridBagConstraints gbc_logDataTable = new GridBagConstraints();
	        gbc_logDataTable.insets = insets0;
	        gbc_logDataTable.anchor = GridBagConstraints.PAGE_START;
	        gbc_logDataTable.fill = GridBagConstraints.BOTH;
	        gbc_logDataTable.gridx = 0;
	        gbc_logDataTable.gridy = 0;
	        logViewPanel.add(logDataTable, gbc_logDataTable);
	        
	        GridBagConstraints gbc_logViewPanel = new GridBagConstraints();
	        gbc_logViewPanel.insets = insets0;
	        gbc_logViewPanel.anchor = GridBagConstraints.PAGE_START;
	        gbc_logViewPanel.fill = GridBagConstraints.BOTH;
	        gbc_logViewPanel.gridx = 0;
	        gbc_logViewPanel.gridy = 1;
	        panel.add(logViewPanel, gbc_logViewPanel);
		}
    	catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
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
        bundle = ResourceBundle.getBundle("com.vgi.mafscaling.logplay");
        return bundle.getString("usage"); 
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // WORK FUNCTIONS
    //////////////////////////////////////////////////////////////////////////////////////

	private void loadLogFile() {
    	fileChooser.setMultiSelectionEnabled(false);
        if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(this))
            return;
        File file = fileChooser.getSelectedFile();
        Properties prop = new Properties();
        prop.put("delimiter", ",");
        prop.put("firstRowHasColumnNames", "true");
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
        	logDataTable.refresh(file.toURI().toURL(), prop);
        	TableCellRenderer renderer;
        	Component comp;
        	Column col;
        	String colName;
            xAxisColumn.removeAllItems();
            yAxisColumn.removeAllItems();
            zAxisColumn.removeAllItems();
            xAxisColumn.addItem("");
            yAxisColumn.addItem("");
            zAxisColumn.addItem("Optional");
			for (int i = 0; i < logDataTable.getColumnCount(); ++i) {
				col = logDataTable.getColumn(i);
				colName = col.getHeaderValue().toString();
				renderer = col.getHeaderRenderer();
			    if (renderer == null)
			        renderer = logDataTable.getTableHeader().getDefaultRenderer();
			    comp = renderer.getTableCellRendererComponent(logDataTable.getTable(), colName, false, false, 0, 0);
		    	col.setPreferredWidth(Math.max(comp.getPreferredSize().width, 60));
                xAxisColumn.addItem(colName);
                yAxisColumn.addItem(colName);
                zAxisColumn.addItem(colName);
			}
			zAxisColumn.setSelectedIndex(0);
			if (logDataTable.getControlPanel().getComponentCount() > 7)
				logDataTable.getControlPanel().remove(7);
			logDataTable.getControlPanel().add(new JLabel("   [" + file.getName() + "]"));
			progressBar.setMinimum(0);
			progressBar.setValue(0);
			progressBar.setMaximum(logDataTable.getRowCount() - 1);
        }
        catch (Exception ex) {
    		ex.printStackTrace();
    		logger.error(ex);
    	}
    	finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    	}
	}
	
	void createPlayTable() {
		int x, y, z;
		x = xAxisColumn.getSelectedIndex() - 1;
		if (x < 0) {
            JOptionPane.showMessageDialog(null, "Please select 'X-Axis column' from drop-down list", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		y = yAxisColumn.getSelectedIndex() - 1;
		if (y < 0) {
            JOptionPane.showMessageDialog(null, "Please select 'X-Axis column' from drop-down list", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		z = zAxisColumn.getSelectedIndex() - 1;
		// no need to lock as og isn't being played yet
		LogPlayTable lpt = new LogPlayTable(SwingUtilities.windowForComponent(this), "X-Axis: " + xAxisColumn.getSelectedItem() + "; Y-Axis: " + yAxisColumn.getSelectedItem());
		lpt.setShowInterpolationCells(showIntepCells.isSelected());
		lpt.setShowSignificantCell(showSignifCells.isSelected());
		lpt.setShowTraceLine(showTraceLine.isSelected());
		TableHolder th = new TableHolder(x, y, z, lpt);
		tables.add(th);
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
	
	private JButton addPlayerButton(int column, ImageIcon icon) {
		JButton button = new JButton(icon);
		button.putClientProperty("Nimbus.Overrides", zeroInsets);
		button.setMargin(insets0);
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.addActionListener(this);
        GridBagConstraints gbc_button = new GridBagConstraints();
        gbc_button.insets = insets0;
        gbc_button.anchor = GridBagConstraints.WEST;
        gbc_button.gridx = column;
        gbc_button.gridy = 1;
        playerPanel.add(button, gbc_button);
        return button;
	}
	
	private JCheckBox addCheckBox(int column, String text) {
		JCheckBox check = new JCheckBox(text);
		check.addActionListener(this);
        GridBagConstraints gbc_check = new GridBagConstraints();
        gbc_check.insets = insets0;
        gbc_check.anchor = GridBagConstraints.WEST;
        gbc_check.gridx = column;
        gbc_check.gridy = 1;
        playerPanel.add(check, gbc_check);
        return check;
	}
	
	private void play() {
		// no need to lock yet
		if (tables.size() == 0) {
            JOptionPane.showMessageDialog(null, "Please create at least one play table", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (playing)
			return;
    	if (startPlay >= 0) {
			logDataTable.getTable().setRowSelectionInterval(startPlay, startPlay);
			int col = logDataTable.getSelectedColumn();
			logDataTable.getTable().changeSelection(startPlay, (col > 0 ? col : 0), false, false);
    	}
        progressBar.setValue(logDataTable.getSelectedRow());
		timer.setDelay(timerDelay);
		playNext.set(1);
        playing = true;
        timer.start();
		playButton.setIcon(pauseIcon);
		setPlayAreaButton.setEnabled(false);
		newPlayButton.setEnabled(false);
		logDataTable.setEditable(false);
		synchronized (lock) {
			for (TableHolder t : tables)
				t.table.setEditable(false);
		}
	}
	
	private void stop() {
		if (!playing)
			return;
		timer.setDelay(timerDelay);
		playNext.set(1);
        playing = false;
        timer.stop();
		playButton.setIcon(playIcon);
		setPlayAreaButton.setEnabled(true);
		newPlayButton.setEnabled(true);
		logDataTable.setEditable(true);
		synchronized (lock) {
			for (TableHolder t : tables)
				t.table.setEditable(true);
		}
	}
	
	private void reset() {
        stop();
        progressBar.setValue(0);
    }
	
	private void rewind() {
		playNext.set(-1);
		if (timer.getDelay() > 0)
			timer.setDelay(timer.getDelay() - step);
	}

	
	private void forward() {
		playNext.set(1);
		if (timer.getDelay() > 0)
			timer.setDelay(timer.getDelay() - step);
	}
	
	private void setShowInterpolationCells() {
		synchronized (lock) {
			for (TableHolder t : tables)
				t.table.setShowInterpolationCells(showIntepCells.isSelected());
		}
	}
	
	private void setShowSignificantCell() {
		synchronized (lock) {
			for (TableHolder t : tables)
				t.table.setShowSignificantCell(showSignifCells.isSelected());
		}
	}
	
	private void setShowTraceLine() {
		synchronized (lock) {
			for (TableHolder t : tables)
				t.table.setShowTraceLine(showTraceLine.isSelected());
		}
	}
	
	public void setStartEndArea(int start, int end) {
    	startPlay = Math.min(start, end);
    	endPlay = Math.max(start, end);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == loadButton)
			loadLogFile();
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
		else if (e.getSource() == newPlayButton)
			createPlayTable();
		else if (e.getSource() == playButton) {
			if (playing)
				stop();
			else
				play();
		}
		else if (e.getSource() == rewButton) {
			rewind();
		}
		else if (e.getSource() == ffwButton) {
			forward();
		}
		else if (e.getSource() == stopButton) {
			reset();
		}
		else if (e.getSource() == showIntepCells) {
			setShowInterpolationCells();
		}
		else if (e.getSource() == showSignifCells) {
			setShowSignificantCell();
		}
		else if (e.getSource() == showTraceLine) {
			setShowTraceLine();
		}
		else if (e.getSource() == setPlayAreaButton) {
	    	if (playAreaSelector != null)
	    		playAreaSelector.dispose();
			playAreaSelector = new PlayAreaSelector(this);
		}
	}

}

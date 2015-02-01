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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
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
    private JComboBox<String> xAxisColumn = null;
    private JComboBox<String> yAxisColumn = null;
    private JComboBox<String> zAxisColumn = null;
    private JCheckBox showIntepCells = null;
    private JCheckBox showSignifCells = null;
    private JCheckBox showTraceLine = null;
    private JSlider progressBar = null;
    private HashSet<TableHolder> tables = new HashSet<TableHolder>();
    private Object lock = new Object();
    private volatile boolean playing = false;
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
	    gbl_playerPanel.columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0};
	    gbl_playerPanel.rowHeights = new int[] {0, 0, 0, 0};
	    gbl_playerPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
	    gbl_playerPanel.rowWeights = new double[]{0.0, 0.0};
	    playerPanel.setLayout(gbl_playerPanel);

	    progressBar = new JSlider(0, 0, 0);
        GridBagConstraints gbl_progressBar = new GridBagConstraints();
        gbl_progressBar.insets = insets0;
        gbl_progressBar.anchor = GridBagConstraints.PAGE_START;
        gbl_progressBar.fill = GridBagConstraints.HORIZONTAL;
        gbl_progressBar.gridx = 0;
        gbl_progressBar.gridy = 0;
        gbl_progressBar.weightx = 1.0;
        gbl_progressBar.gridwidth = gbl_playerPanel.columnWidths.length;
        playerPanel.add(progressBar, gbl_progressBar);
        
        progressBar.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int row = progressBar.getValue();
                if (logDataTable != null) {
    				logDataTable.getTable().setRowSelectionInterval(row, row);
    				logDataTable.getTable().changeSelection(row, 0, false, false);
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
	    
        stopButton = addPlayerButton(0, new ImageIcon(getClass().getResource("/stop.png")));
        rewButton = addPlayerButton(1, new ImageIcon(getClass().getResource("/rew.png")));
        playButton = addPlayerButton(2, playIcon);
        ffwButton = addPlayerButton(3, new ImageIcon(getClass().getResource("/ffw.png")));
        showIntepCells = addCheckBox(4, "Show interpolation cells");
        showSignifCells = addCheckBox(5, "Show significant cell");
        showTraceLine = addCheckBox(6, "Show trace line");
        
        GridBagConstraints gbc_playerPanel = new GridBagConstraints();
        gbc_playerPanel.insets = insets0;
        gbc_playerPanel.anchor = GridBagConstraints.PAGE_START;
        gbc_playerPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_playerPanel.gridx = 0;
        gbc_playerPanel.gridy = 0;
        panel.add(playerPanel, gbc_playerPanel);
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
			
	        GridBagConstraints gbl_logDataTable = new GridBagConstraints();
	        gbl_logDataTable.insets = insets0;
	        gbl_logDataTable.anchor = GridBagConstraints.PAGE_START;
	        gbl_logDataTable.fill = GridBagConstraints.BOTH;
	        gbl_logDataTable.gridx = 0;
	        gbl_logDataTable.gridy = 0;
	        logViewPanel.add(logDataTable, gbl_logDataTable);
	        
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
		TableHolder th = new TableHolder(x, y, z, new LogPlayTable(SwingUtilities.windowForComponent(this), "X-Axis: " + xAxisColumn.getSelectedItem() + "; Y-Axis: " + yAxisColumn.getSelectedItem()));
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
        GridBagConstraints gbl_button = new GridBagConstraints();
        gbl_button.insets = insets0;
        gbl_button.anchor = GridBagConstraints.WEST;
        gbl_button.gridx = column;
        gbl_button.gridy = 1;
        playerPanel.add(button, gbl_button);
        return button;
	}
	
	private JCheckBox addCheckBox(int column, String text) {
		JCheckBox check = new JCheckBox(text);
		check.addActionListener(this);
        GridBagConstraints gbl_check = new GridBagConstraints();
        gbl_check.insets = insets0;
        gbl_check.anchor = GridBagConstraints.WEST;
        gbl_check.gridx = column;
        gbl_check.gridy = 1;
        playerPanel.add(check, gbl_check);
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
        progressBar.setValue(logDataTable.getSelectedRow());
		timer.setDelay(timerDelay);
		playNext.set(1);
        playing = true;
        timer.start();
		playButton.setIcon(pauseIcon);
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
	}

}

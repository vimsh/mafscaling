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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIDefaults;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;

import quick.dbtable.DBTable;

public class LogPlay extends JDialog implements ActionListener {
	private static final long serialVersionUID = -1614821051358033847L;

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

    private final int step = 20;
    private Timer timer = null;
    private LogView logView = null;
    private JPanel playerPanel = null;
    private DBTable logDataTable = null;
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
    private JCheckBox showTraceMarker = null;
    private JSlider progressBar = null;
    private HashSet<TableHolder> tables = new HashSet<TableHolder>();
    private Object lock = new Object();
    private volatile boolean playing = false;
    private volatile boolean showMarker = false;
    private boolean paused = false;
    private int lastRow = 0;
    private int startPlay = -1;
    private int endPlay = -1;
    private int timerDelay = 200;
    private AtomicInteger playNext = null;
    private ImageIcon playIcon = null;
    private ImageIcon pauseIcon = null;
	private Insets insets0 = new Insets(0, 0, 0, 0);
    private Insets insets3 = new Insets(3, 3, 3, 3);
    private UIDefaults zeroInsets = new UIDefaults();

	public LogPlay(LogView logView) {
        super(SwingUtilities.windowForComponent(logView));
        this.logView = logView;
        logDataTable = logView.getLogDataTable();
        initialize();
    }

    private void initialize() {
    	lastRow = logDataTable.getRowCount() - 1;
    	playNext = new AtomicInteger(1);
        playIcon = new ImageIcon(getClass().getResource("/play.png"));
        pauseIcon = new ImageIcon(getClass().getResource("/pause.png"));
        zeroInsets.put("Button.contentMargins", insets0);
    	timer = new Timer(timerDelay, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	progressBar.setValue(progressBar.getValue() + playNext.get());
            }
        });
    	
        JPanel dataPanel = new JPanel();
	    GridBagLayout gbl_dataPanel = new GridBagLayout();
	    gbl_dataPanel.columnWidths = new int[] {0};
	    gbl_dataPanel.rowHeights = new int[] {0, 1};
	    gbl_dataPanel.columnWeights = new double[]{1.0};
	    gbl_dataPanel.rowWeights = new double[]{0.0, 1.0};
	    dataPanel.setLayout(gbl_dataPanel);
        getContentPane().add(dataPanel);

        createSelectionPanel(dataPanel);
        createPlayer(dataPanel);

    	addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                timer.stop();
        		synchronized (lock) {
        			for (TableHolder t : tables)
        				t.table.dispose();
                	tables.clear();
        		}
    			logView.disposeLogView();
            }
    	});
    	
        pack();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(null);
        setResizable(false);
        setLocationRelativeTo(SwingUtilities.windowForComponent(logView));
        setVisible(true);
    }

	private void createSelectionPanel(JPanel panel) {
		JPanel selectionPanel = new JPanel();
        GridBagLayout gbl_selectionPanel = new GridBagLayout();
        gbl_selectionPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
        gbl_selectionPanel.rowHeights = new int[]{0};
        gbl_selectionPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        gbl_selectionPanel.rowWeights = new double[]{0.0};
        selectionPanel.setLayout(gbl_selectionPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = insets3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;

        selectionPanel.add(new JLabel("X-Axis"), gbc);
		
        gbc.gridx++;
        xAxisColumn = new JComboBox<String>();
        selectionPanel.add(xAxisColumn, gbc);

        gbc.gridx++;
        selectionPanel.add(new JLabel("Y-Axis"), gbc);

        gbc.gridx++;
        yAxisColumn = new JComboBox<String>();
        selectionPanel.add(yAxisColumn, gbc);

        gbc.gridx++;
        selectionPanel.add(new JLabel("Data*"), gbc);

        gbc.gridx++;
        zAxisColumn = new JComboBox<String>();
        selectionPanel.add(zAxisColumn, gbc);

        gbc.gridx++;
        gbc.weightx = 1.0;
        newPlayButton = new JButton("New Play Table");
        newPlayButton.addActionListener(this);
        selectionPanel.add(newPlayButton, gbc);

        xAxisColumn.addItem("");
        yAxisColumn.addItem("");
        zAxisColumn.addItem("Optional");
		for (int i = 0; i < logDataTable.getColumnCount(); ++i) {
			String colName = logDataTable.getColumn(i).getHeaderValue().toString();
            xAxisColumn.addItem(colName);
            yAxisColumn.addItem(colName);
            zAxisColumn.addItem(colName);
		}
		zAxisColumn.setSelectedIndex(0);

        gbc.insets = insets0;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(selectionPanel, gbc);
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
		progressBar.setMinimum(0);
		progressBar.setMaximum(lastRow);
		setProgressBar(logDataTable.getSelectedRow());
        progressBar.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int row = progressBar.getValue();
                if (playing) {
                	if (row == lastRow || (endPlay >= 0 && endPlay < row)) {
	                	stop();
	                	return;
                	}
                	if (showMarker) {
	                    Rectangle2D dataArea = logView.getChartPanel().getChartRenderingInfo().getPlotInfo().getDataArea();
	                    XYPlot plot = (XYPlot) logView.getChartPanel().getChart().getPlot();
	                    double x = plot.getDomainAxis().valueToJava2D(row, dataArea, plot.getDomainAxisEdge());
	                    boolean isLeft = (x < (dataArea.getMaxX() - dataArea.getMinX()) / 2) ? true : false;
	                	logView.setMarkers(row, isLeft);
                	}
                }
				logDataTable.getTable().setRowSelectionInterval(row, row);
				logDataTable.getTable().changeSelection(row, logDataTable.getSelectedColumn(), false, false);
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
        showTraceMarker = addCheckBox(7, "Show plot trace marker");

        gbc.weightx = 0.0;
        gbc.gridwidth = 0;
        gbc.gridy = 1;
        panel.add(playerPanel, gbc);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // WORK FUNCTIONS
    //////////////////////////////////////////////////////////////////////////////////////
	
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
		LogPlayTable lpt = new LogPlayTable(this, "X-Axis: " + xAxisColumn.getSelectedItem() + "; Y-Axis: " + yAxisColumn.getSelectedItem());
		lpt.setShowInterpolationCells(showIntepCells.isSelected());
		lpt.setShowSignificantCell(showSignifCells.isSelected());
		lpt.setShowTraceLine(showTraceLine.isSelected());
		TableHolder th = new TableHolder(x, y, z, lpt);
		tables.add(th);
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
    	if (startPlay >= 0 && !paused) {
			logDataTable.getTable().setRowSelectionInterval(startPlay, startPlay);
			int col = logDataTable.getSelectedColumn();
			logDataTable.getTable().changeSelection(startPlay, (col > 0 ? col : 0), false, false);
    	}
    	paused = false;
    	setProgressBar(logDataTable.getSelectedRow());
		timer.setDelay(timerDelay);
		playNext.set(1);
        playing = true;
        timer.start();
		playButton.setIcon(pauseIcon);
		newPlayButton.setEnabled(false);
		logDataTable.setEditable(false);
		logView.disableMouseListener();
		synchronized (lock) {
			for (TableHolder t : tables)
				t.table.setEditable(false);
		}
	}
	
	private void pause() {
		if (!playing)
			return;
        playing = false;
		paused = true;
        timer.stop();
		timer.setDelay(timerDelay);
		playButton.setIcon(playIcon);
		playNext.set(1);
	}
	
	private void stop() {
		if (playing)
			pause();
		if (paused)
			paused = false;
		else
			return;
		setProgressBar(startPlay);
		logDataTable.setEditable(true);
		logView.enableMouseListener();
		newPlayButton.setEnabled(true);
		synchronized (lock) {
			for (TableHolder t : tables)
				t.table.setEditable(true);
		}
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
	
	public void setStartEndArea(ValueMarker startMarker, ValueMarker endMarker) {
		int start = (startMarker == null ? -1 : (int)startMarker.getValue());
		int end = (endMarker == null ? -1 : (int)endMarker.getValue());
		if (start == -1 || end == -1)
			startPlay = Math.max(start, end);
		else {
			startPlay = Math.min(start, end);
			endPlay = Math.max(start, end);
		}
		if (startPlay > lastRow)
			startPlay = lastRow;
		if (endPlay > lastRow)
			endPlay = lastRow;
		setProgressBar(startPlay);
	}
	
	public void setProgressBar(int val) {
		if (val >= 0 && val <= lastRow)
			progressBar.setValue(val);
		else
			progressBar.setValue(0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == newPlayButton)
			createPlayTable();
		else if (e.getSource() == playButton) {
			if (playing)
				pause();
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
			stop();
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
		else if (e.getSource() == showTraceMarker) {
			showMarker = showTraceMarker.isSelected();
		}
	}

}

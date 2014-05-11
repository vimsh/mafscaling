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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;

public class MafCompare extends JFrame {
	private static final long serialVersionUID = 3380903505904186441L;
	private static final Logger logger = Logger.getLogger(MafCompare.class);
    private static final String Title = "MAF Compare / Modify";
    private static final int WindowHeight = 145;
    private static final int RowHeight = 17;
    private static final int ColumnWidth = 50;
    private static final int MafTableColumnCount = 50;
    private ExcelAdapter excelAdapter = new ExcelAdapter();
    private ExcelAdapter compExcelAdapter = null;
    private TableCellListener compMafCellListener = null;
    private JTable origMafTable = null;
    private JTable newMafTable = null;
    private JTable compMafTable = null;
    
    public MafCompare() {
    	compExcelAdapter = new ExcelAdapter() {
    		//@override
    	    protected void onPaste(JTable table, boolean extendRows, boolean extendCols) {
    	    	super.onPaste(table, extendRows, extendCols);
            	int colCount = compMafTable.getColumnCount();
            	Utils.ensureColumnCount(colCount, origMafTable);
            	Utils.ensureColumnCount(colCount, newMafTable);
            	ArrayList<Object> values = new ArrayList<Object>();
            	int i;
            	for (i = (table.getSelectedColumns())[0]; i < colCount; ++i)
            		values.add(compMafTable.getValueAt(0, i));
            	double corr;
            	int j = 0;
            	for (i = (table.getSelectedColumns())[0]; i < colCount; ++i, ++j) {
            		if (Pattern.matches(Utils.fpRegex, values.get(j).toString()) &&
            			Pattern.matches(Utils.fpRegex, origMafTable.getValueAt(1, i).toString())) {
		                corr = Double.valueOf(values.get(j).toString()) / 100.0 + 1.0;
		                newMafTable.setValueAt(Double.valueOf(origMafTable.getValueAt(1, i).toString()) * corr, 1, i);
            		}
            		else
            			break;
            	}
    	    }
    	};
    	initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
    	try {
            ImageIcon tableImage = new ImageIcon(getClass().getResource("/table.jpg"));
	        setTitle(Title);
	        setIconImage(tableImage.getImage());
	        setBounds(100, 100, 621, 372);
	        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	        setSize(Config.getCompWindowSize());
	        setLocation(Config.getCompWindowLocation());
	        setMaximumSize(new Dimension(1600, WindowHeight));
	        setMinimumSize(new Dimension(300, WindowHeight));
	        setLocationRelativeTo(null);
	        setVisible(false);
	        addWindowListener(new WindowAdapter() {
	        	public void windowClosing(WindowEvent e) {
	        		Utils.clearTable(origMafTable);
	        		Utils.clearTable(newMafTable);
	        		Utils.clearTable(compMafTable);
	                Config.setCompWindowSize(getSize());
	                Config.setCompWindowLocation(getLocation());
	        	}
	        });
	
	        JPanel dataPanel = new JPanel();
	        GridBagLayout gbl_dataPanel = new GridBagLayout();
	        gbl_dataPanel.columnWidths = new int[] {0, 0, 0};
	        gbl_dataPanel.rowHeights = new int[] {RowHeight, RowHeight, RowHeight, RowHeight, RowHeight};
	        gbl_dataPanel.columnWeights = new double[]{0.0, 0.0, 0.0};
	        gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
	        dataPanel.setLayout(gbl_dataPanel);
	        getContentPane().add(dataPanel);
	
	        JLabel origLabel = new JLabel("Original");
	        GridBagConstraints gbc_origLabel = new GridBagConstraints();
	        gbc_origLabel.anchor = GridBagConstraints.PAGE_START;
	        gbc_origLabel.insets = new Insets(1, 1, 1, 5);
	        gbc_origLabel.weightx = 0;
	        gbc_origLabel.weighty = 0;
	        gbc_origLabel.gridx = 0;
	        gbc_origLabel.gridy = 0;
	        gbc_origLabel.gridheight = 2;
	        dataPanel.add(origLabel, gbc_origLabel);
	        
	        JLabel newLabel = new JLabel("New");
	        GridBagConstraints gbc_newLabel = new GridBagConstraints();
	        gbc_newLabel.anchor = GridBagConstraints.PAGE_START;
	        gbc_newLabel.insets = new Insets(1, 1, 1, 5);
	        gbc_newLabel.weightx = 0;
	        gbc_newLabel.weighty = 0;
	        gbc_newLabel.gridx = 0;
	        gbc_newLabel.gridy = 2;
	        gbc_newLabel.gridheight = 2;
	        dataPanel.add(newLabel, gbc_newLabel);
	        
	        JLabel compLabel = new JLabel("Change");
	        GridBagConstraints gbc_compLabel = new GridBagConstraints();
	        gbc_compLabel.anchor = GridBagConstraints.PAGE_START;
	        gbc_compLabel.insets = new Insets(1, 1, 1, 5);
	        gbc_compLabel.weightx = 0;
	        gbc_compLabel.weighty = 0;
	        gbc_compLabel.gridx = 0;
	        gbc_compLabel.gridy = 4;
	        dataPanel.add(compLabel, gbc_compLabel);
	        
	        JLabel origVoltLabel = new JLabel("volt");
	        GridBagConstraints gbc_origVoltLabel = new GridBagConstraints();
	        gbc_origVoltLabel.anchor = GridBagConstraints.PAGE_START;
	        gbc_origVoltLabel.insets = new Insets(1, 1, 1, 5);
	        gbc_origVoltLabel.weightx = 0;
	        gbc_origVoltLabel.weighty = 0;
	        gbc_origVoltLabel.gridx = 1;
	        gbc_origVoltLabel.gridy = 0;
	        dataPanel.add(origVoltLabel, gbc_origVoltLabel);
	        
	        JLabel origGsLabel = new JLabel(" g/s");
	        GridBagConstraints gbc_origGsLabel = new GridBagConstraints();
	        gbc_origGsLabel.anchor = GridBagConstraints.PAGE_START;
	        gbc_origGsLabel.insets = new Insets(1, 1, 1, 5);
	        gbc_origGsLabel.weightx = 0;
	        gbc_origGsLabel.weighty = 0;
	        gbc_origGsLabel.gridx = 1;
	        gbc_origGsLabel.gridy = 1;
	        dataPanel.add(origGsLabel, gbc_origGsLabel);
	        
	        JLabel newVoltLabel = new JLabel("volt");
	        GridBagConstraints gbc_newVoltLabel = new GridBagConstraints();
	        gbc_newVoltLabel.anchor = GridBagConstraints.PAGE_START;
	        gbc_newVoltLabel.insets = new Insets(1, 1, 1, 5);
	        gbc_newVoltLabel.weightx = 0;
	        gbc_newVoltLabel.weighty = 0;
	        gbc_newVoltLabel.gridx = 1;
	        gbc_newVoltLabel.gridy = 2;
	        dataPanel.add(newVoltLabel, gbc_newVoltLabel);
	        
	        JLabel newGsLabel = new JLabel(" g/s");
	        GridBagConstraints gbc_newGsLabel = new GridBagConstraints();
	        gbc_newGsLabel.anchor = GridBagConstraints.PAGE_START;
	        gbc_newGsLabel.insets = new Insets(1, 1, 1, 5);
	        gbc_newGsLabel.weightx = 0;
	        gbc_newGsLabel.weighty = 0;
	        gbc_newGsLabel.gridx = 1;
	        gbc_newGsLabel.gridy = 3;
	        dataPanel.add(newGsLabel, gbc_newGsLabel);
	        
	        JLabel compPctLabel = new JLabel(" %  ");
	        GridBagConstraints gbc_compPctLabel = new GridBagConstraints();
	        gbc_compPctLabel.anchor = GridBagConstraints.PAGE_START;
	        gbc_compPctLabel.insets = new Insets(1, 1, 1, 5);
	        gbc_compPctLabel.weightx = 0;
	        gbc_compPctLabel.weighty = 0;
	        gbc_compPctLabel.gridx = 1;
	        gbc_compPctLabel.gridy = 4;
	        dataPanel.add(compPctLabel, gbc_compPctLabel);
	        
	        JPanel tablesPanel = new JPanel();
	        GridBagLayout gbl_tablesPanel = new GridBagLayout();
	        gbl_tablesPanel.columnWidths = new int[]{0};
	        gbl_tablesPanel.rowHeights = new int[] {0, 0, 0};
	        gbl_tablesPanel.columnWeights = new double[]{0.0};
	        gbl_tablesPanel.rowWeights = new double[]{0.0, 0.0, 1.0};
	        tablesPanel.setLayout(gbl_tablesPanel);
	
	        JScrollPane mafScrollPane = new JScrollPane(tablesPanel);
	        mafScrollPane.setMinimumSize(new Dimension(1600, 105));
	        mafScrollPane.getHorizontalScrollBar().setMaximumSize(new Dimension(20, 20));
	        mafScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	        mafScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
	        GridBagConstraints gbc_mafScrollPane = new GridBagConstraints();
	        gbc_mafScrollPane.weightx = 1.0;
	        gbc_mafScrollPane.weighty = 1.0;
	        gbc_mafScrollPane.anchor = GridBagConstraints.PAGE_START;
	        gbc_mafScrollPane.fill = GridBagConstraints.HORIZONTAL;
	        gbc_mafScrollPane.gridx = 2;
	        gbc_mafScrollPane.gridy = 0;
	        gbc_mafScrollPane.gridheight = 5;
	        dataPanel.add(mafScrollPane, gbc_mafScrollPane);

	        origMafTable = new JTable();
	        origMafTable.setColumnSelectionAllowed(true);
	        origMafTable.setCellSelectionEnabled(true);
	        origMafTable.setBorder(new LineBorder(new Color(0, 0, 0)));
	        origMafTable.setRowHeight(RowHeight);
	        origMafTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
	        origMafTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	        origMafTable.setModel(new DefaultTableModel(2, MafTableColumnCount));
	        origMafTable.setTableHeader(null);
	        Utils.initializeTable(origMafTable, ColumnWidth);
	        GridBagConstraints gbc_origMafTable = new GridBagConstraints();
	        gbc_origMafTable.anchor = GridBagConstraints.PAGE_START;
	        gbc_origMafTable.insets = new Insets(0, 0, 0, 0);
	        gbc_origMafTable.fill = GridBagConstraints.BOTH;
	        gbc_origMafTable.weightx = 1.0;
	        gbc_origMafTable.weighty = 0;
	        gbc_origMafTable.gridx = 0;
	        gbc_origMafTable.gridy = 0;
	        tablesPanel.add(origMafTable, gbc_origMafTable);
	        excelAdapter.addTable(origMafTable, false, false, false, false, true, false, true, false, true);
	     
	        newMafTable = new JTable();
	        newMafTable.setColumnSelectionAllowed(true);
	        newMafTable.setCellSelectionEnabled(true);
	        newMafTable.setBorder(new LineBorder(new Color(0, 0, 0)));
	        newMafTable.setRowHeight(RowHeight);
	        newMafTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
	        newMafTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	        newMafTable.setModel(new DefaultTableModel(2, MafTableColumnCount));
	        newMafTable.setTableHeader(null);
	        Utils.initializeTable(newMafTable, ColumnWidth);
	        GridBagConstraints gbc_newMafTable = new GridBagConstraints();
	        gbc_newMafTable.anchor = GridBagConstraints.PAGE_START;
	        gbc_newMafTable.insets = new Insets(0, 0, 0, 0);
	        gbc_newMafTable.fill = GridBagConstraints.BOTH;
	        gbc_newMafTable.weightx = 1.0;
	        gbc_newMafTable.weighty = 0;
	        gbc_newMafTable.gridx = 0;
	        gbc_newMafTable.gridy = 1;
	        tablesPanel.add(newMafTable, gbc_newMafTable);
	        excelAdapter.addTable(newMafTable, false, false, false, false, false, false, false, false, true);
	     
	        compMafTable = new JTable();
	        compMafTable.setColumnSelectionAllowed(true);
	        compMafTable.setCellSelectionEnabled(true);
	        compMafTable.setBorder(new LineBorder(new Color(0, 0, 0)));
	        compMafTable.setRowHeight(RowHeight);
	        compMafTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
	        compMafTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	        compMafTable.setModel(new DefaultTableModel(1, MafTableColumnCount));
	        compMafTable.setTableHeader(null);
	        Utils.initializeTable(compMafTable, ColumnWidth);
	        NumberFormatRenderer numericRenderer = new NumberFormatRenderer();
	        numericRenderer.setFormatter(new DecimalFormat("0.000"));
	        compMafTable.setDefaultRenderer(Object.class, numericRenderer);
	        GridBagConstraints gbc_compMafTable = new GridBagConstraints();
	        gbc_compMafTable.anchor = GridBagConstraints.PAGE_START;
	        gbc_compMafTable.insets = new Insets(0, 0, 0, 0);
	        gbc_compMafTable.fill = GridBagConstraints.BOTH;
	        gbc_compMafTable.weightx = 1.0;
	        gbc_compMafTable.weighty = 0;
	        gbc_compMafTable.gridx = 0;
	        gbc_compMafTable.gridy = 2;
	        tablesPanel.add(compMafTable, gbc_compMafTable);
	        compExcelAdapter.addTable(compMafTable, false, true, false, false, false, true, true, false, true);
	        
	        TableModelListener origTableListener = new TableModelListener() {
	            public void tableChanged(TableModelEvent tme) {
	                if (tme.getType() == TableModelEvent.UPDATE) {
	                	int colCount = origMafTable.getColumnCount();
	                	Utils.ensureColumnCount(colCount, newMafTable);
	                	Utils.ensureColumnCount(colCount, compMafTable);
	                	for (int i = 0; i < colCount; ++i) {
	                		if (Pattern.matches(Utils.fpRegex, origMafTable.getValueAt(1, i).toString()) &&
	                			Pattern.matches(Utils.fpRegex, newMafTable.getValueAt(1, i).toString())) {
	                			compMafTable.setValueAt(((Double.valueOf(newMafTable.getValueAt(1, i).toString()) / Double.valueOf(origMafTable.getValueAt(1, i).toString())) - 1.0) * 100.0, 0, i);
	                		}
	                		else
	                			break;
	                	}
	                }
	            }
	        };
	        
	        TableModelListener newTableListener = new TableModelListener() {
	            public void tableChanged(TableModelEvent tme) {
	                if (tme.getType() == TableModelEvent.UPDATE) {
	                	int colCount = newMafTable.getColumnCount();
	                	Utils.ensureColumnCount(colCount, origMafTable);
	                	Utils.ensureColumnCount(colCount, compMafTable);
	                	for (int i = 0; i < colCount; ++i) {
	                		if (Pattern.matches(Utils.fpRegex, newMafTable.getValueAt(1, i).toString()) &&
	                			Pattern.matches(Utils.fpRegex, origMafTable.getValueAt(1, i).toString())) {
	                			compMafTable.setValueAt(((Double.valueOf(newMafTable.getValueAt(1, i).toString()) / Double.valueOf(origMafTable.getValueAt(1, i).toString())) - 1.0) * 100.0, 0, i);
	                		}
	                		else
	                			break;
	                	}
	                }
	            }
	        };
	        
	        origMafTable.getModel().addTableModelListener(origTableListener);
	        newMafTable.getModel().addTableModelListener(newTableListener);

	        Action action = new AbstractAction() {
				private static final long serialVersionUID = 8148393537657380215L;
				public void actionPerformed(ActionEvent e) {
	                TableCellListener tcl = (TableCellListener)e.getSource();
	                if (Pattern.matches(Utils.fpRegex, compMafTable.getValueAt(0, tcl.getColumn()).toString())) {
		                if (Pattern.matches(Utils.fpRegex, origMafTable.getValueAt(1, tcl.getColumn()).toString())) {
			                double corr = Double.valueOf(compMafTable.getValueAt(0, tcl.getColumn()).toString()) / 100.0 + 1.0;
			                newMafTable.setValueAt(Double.valueOf(origMafTable.getValueAt(1, tcl.getColumn()).toString()) * corr, 1, tcl.getColumn());
		                }
	                }
	                else
	                	compMafTable.setValueAt(tcl.getOldValue(), 0, tcl.getColumn());
	            }
	        };
	        
	        setCompMafCellListener(new TableCellListener(compMafTable, action));
    	}
    	catch (Exception e) {
            logger.error(e);
    	}
    }
    
    @Override
    public void paint(Graphics g) {
    	Dimension d = getSize();
	    Dimension m = getMaximumSize();
	    boolean resize = d.width > m.width || d.height > m.height;
	    d.width = Math.min(m.width, d.width);
	    d.height = Math.min(m.height, d.height);
	    if (resize) {
		    Point p = getLocation();
		    setVisible(false);
		    setSize(d);
		    setLocation(p);
		    setVisible(true);
	    }
	    super.paint(g);
    }

	public TableCellListener getCompMafCellListener() {
		return compMafCellListener;
	}

	public void setCompMafCellListener(TableCellListener compMafCellListener) {
		this.compMafCellListener = compMafCellListener;
	}
}

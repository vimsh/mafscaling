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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIDefaults;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

class WotPullsGroups implements ActionListener {
    private List<String> columns = null;
    private JTable groupTable = null;
    private DefaultTableModel groupModel = null;
    private JTable selectionTable = null;
    private DefaultTableModel selectionModel = null;
    private JTable columnsTable = null;
    private DefaultTableModel columnsModel = null;
    private JPanel columnsPanel = null;
    private Insets insets0 = new Insets(0, 0, 0, 0);
    private Insets insets2 = new Insets(1, 0, 0, 1);
    private TreeMap<String, TreeSet<String>> groups = new TreeMap<String, TreeSet<String>>();
    
    private ImageIcon arrowImageLeft = new ImageIcon(getClass().getResource("/arrow.jpg"));
    private ImageIcon arrowImageRigth = new ImageIcon(getClass().getResource("/arrowr.jpg"));
    private UIDefaults zeroInsets = new UIDefaults();
    
    public WotPullsGroups() {
        zeroInsets.put("Button.contentMargins", insets0);
    }
    
    public void getGroups(JMultiSelectionBox wotPlotsColumn, ArrayList<TreeSet<String>> wotYAxisGroups) {
        columns = wotPlotsColumn.getSelectedItems();
        if (columns == null || columns.size() < 2)
            return;
        Collections.sort(columns);

        columnsPanel = new JPanel();
        GridBagLayout gbl_dataPanel = new GridBagLayout();
        gbl_dataPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
        gbl_dataPanel.rowHeights = new int[] {0, 0, 0};
        gbl_dataPanel.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 1.0, 1.0};
        gbl_dataPanel.rowWeights = new double[]{0.0, 1.0, 1.0};
        columnsPanel.setLayout(gbl_dataPanel);

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        JButton button = new JButton("Add group");
        button.setActionCommand("addgrp");
        button.addActionListener(this);
        buttons.add(button);
        buttons.add(Box.createRigidArea(new Dimension(5,0)));

        button = new JButton("Remove group");
        button.setActionCommand("remgrp");
        button.addActionListener(this);
        buttons.add(button);

        GridBagConstraints gbc_pane = new GridBagConstraints();
        gbc_pane.insets = insets0;
        gbc_pane.anchor = GridBagConstraints.PAGE_START;
        gbc_pane.fill = GridBagConstraints.HORIZONTAL;
        gbc_pane.gridx = 0;
        gbc_pane.gridy = 0;
        gbc_pane.gridwidth = 5;
        columnsPanel.add(buttons, gbc_pane);
        
        gbc_pane = new GridBagConstraints();
        gbc_pane.insets = insets2;
        gbc_pane.anchor = GridBagConstraints.PAGE_START;
        gbc_pane.fill = GridBagConstraints.BOTH;
        gbc_pane.gridx = 0;
        gbc_pane.gridy = 1;
        gbc_pane.gridheight = 2;
        groupTable = createTable(true, "Groups");
        groupTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                int idx = groupTable.getSelectedRow();
                if (idx >= 0) {
                    while (selectionModel.getRowCount() > 0)
                        selectionModel.removeRow(0);
                    TreeSet<String> group = groups.get(groupTable.getValueAt(idx, 0));
                    for (String val : group)
                        selectionModel.addRow(new Object[]{val});
                }
            }
        });
        groupModel = (DefaultTableModel) groupTable.getModel();
        JScrollPane tableScroll = new JScrollPane (groupTable);
        tableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tableScroll.getViewport().setBackground(Color.WHITE);
        tableScroll.setPreferredSize(new Dimension(200, 200));
        columnsPanel.add(tableScroll, gbc_pane);
        
        gbc_pane = new GridBagConstraints();
        gbc_pane.insets = insets2;
        gbc_pane.anchor = GridBagConstraints.PAGE_START;
        gbc_pane.fill = GridBagConstraints.BOTH;
        gbc_pane.gridx = 2;
        gbc_pane.gridy = 1;
        gbc_pane.gridheight = 2;
        selectionTable = createTable(false, "Grouped Columns");
        selectionModel = (DefaultTableModel) selectionTable.getModel();
        tableScroll = new JScrollPane (selectionTable);
        tableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tableScroll.getViewport().setBackground(Color.WHITE);
        tableScroll.setPreferredSize(new Dimension(200, 200));
        columnsPanel.add(tableScroll, gbc_pane);
        
        gbc_pane = new GridBagConstraints();
        gbc_pane.insets = insets2;
        gbc_pane.anchor = GridBagConstraints.PAGE_START;
        gbc_pane.fill = GridBagConstraints.BOTH;
        gbc_pane.gridx = 4;
        gbc_pane.gridy = 1;
        gbc_pane.gridheight = 2;
        columnsTable = createTable(false, "Available Columns");
        columnsModel = (DefaultTableModel) columnsTable.getModel();
        for (String column : columns)
            columnsModel.addRow(new Object[]{column});
        tableScroll = new JScrollPane (columnsTable);
        tableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tableScroll.getViewport().setBackground(Color.WHITE);
        tableScroll.setPreferredSize(new Dimension(200, 200));
        columnsPanel.add(tableScroll, gbc_pane);

        gbc_pane = new GridBagConstraints();
        gbc_pane.anchor = GridBagConstraints.NORTH;
        gbc_pane.insets = insets0;
        gbc_pane.gridx = 1;
        gbc_pane.gridy = 1;
        gbc_pane.gridheight = 2;
        columnsPanel.add(Box.createRigidArea(new Dimension(25, 0)), gbc_pane);

        gbc_pane = new GridBagConstraints();
        gbc_pane.anchor = GridBagConstraints.SOUTH;
        gbc_pane.insets = insets0;
        gbc_pane.gridx = 3;
        gbc_pane.gridy = 1;
        button = new JButton("", arrowImageLeft);
        button.putClientProperty("Nimbus.Overrides", zeroInsets);
        button.setMargin(insets0);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setActionCommand("select");
        button.addActionListener(this);
        columnsPanel.add(button, gbc_pane);

        gbc_pane = new GridBagConstraints();
        gbc_pane.anchor = GridBagConstraints.NORTH;
        gbc_pane.insets = insets0;
        gbc_pane.gridx = 3;
        gbc_pane.gridy = 2;
        button = new JButton("", arrowImageRigth);
        button.putClientProperty("Nimbus.Overrides", zeroInsets);
        button.setMargin(insets0);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setActionCommand("unselect");
        button.addActionListener(this);
        columnsPanel.add(button, gbc_pane);
        columnsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        for (int i = 0; i < wotYAxisGroups.size(); ++i) {
            if (wotYAxisGroups.get(i).size() > 1) {
                String group = "Group " + (i + 1);
                groups.put(group, wotYAxisGroups.get(i));
                groupModel.addRow(new Object[]{group});
                for (String val : wotYAxisGroups.get(i)) {
                    int idx = columns.indexOf(val);
                    columns.remove(idx);
                    columnsModel.removeRow(idx);
                }
            }
        }
        if (groupModel.getRowCount() > 0)
            groupTable.changeSelection(0, 0, false, false);
        
        if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null, columnsPanel, "Columns Y-Axis Grouping", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE))
            return;
        wotYAxisGroups.clear();
        for (String key : groups.keySet()) {
            TreeSet<String> group = groups.get(key);
            if (group.size() > 0)
                wotYAxisGroups.add(group);
        }
    }
    
    protected JTable createTable(boolean isGroup, String name) {
    	JTable table = new JTable() {
            private static final long serialVersionUID = 1L;
            public boolean isCellEditable(int row, int column) { return false; };
            public boolean getScrollableTracksViewportWidth() {
            	return getPreferredSize().width < getParent().getWidth();
            }
        };
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(true);
        table.setSelectionMode(isGroup ? ListSelectionModel.SINGLE_SELECTION : ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setModel(new DefaultTableModel(0, 1));
        table.setBorder(new LineBorder(new Color(0, 0, 0)));
        table.getColumnModel().getColumn(0).setHeaderValue(name);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return table;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("addgrp".equals(e.getActionCommand())) {
            int idx = groupModel.getRowCount();
            String group = "Group " + (idx + 1);
            groups.put(group, new TreeSet<String>());
            groupModel.addRow(new Object[]{group});
            while (selectionModel.getRowCount() > 0)
                selectionModel.removeRow(0);
            groupTable.requestFocus();
            groupTable.changeSelection(idx, 0, false, false);
        }
        else if ("remgrp".equals(e.getActionCommand())) {
            int idx = groupTable.getSelectedRow();
            if (idx >= 0) {
                while (selectionModel.getRowCount() > 0) {
                    String val = (String) selectionModel.getValueAt(0, 0);
                    columns.add(val);
                    columnsModel.addRow(new Object[]{val});
                    selectionModel.removeRow(0);
                }
                groups.remove(groupModel.getValueAt(idx, 0));
                groupModel.removeRow(groupTable.getSelectedRow());
                Collections.sort(columns);
            }
            else
                JOptionPane.showMessageDialog(null, "Please select the group", "Error", JOptionPane.ERROR_MESSAGE);
        }
        else if ("select".equals(e.getActionCommand())) {
            int idx = groupTable.getSelectedRow();
        	if (idx >= 0) {
        	    TreeSet<String> group = groups.get(groupModel.getValueAt(idx, 0));
	        	int[] selected = columnsTable.getSelectedRows();
	            if (selected.length > 0) {
	            	for (int i =  selected.length - 1; i >= 0; --i) {
	            	    idx = selected[i];
		                String val = (String)columnsModel.getValueAt(idx, 0);
		                group.add(val);
		                selectionModel.addRow(new Object[]{val});
		                columns.remove(val);
		                columnsModel.removeRow(idx);
	            	}
	            }
	            selectionTable.clearSelection();
                columnsTable.clearSelection();
        	}
        	else
                JOptionPane.showMessageDialog(null, "Please select the group", "Error", JOptionPane.ERROR_MESSAGE);
        }
        else if ("unselect".equals(e.getActionCommand())) {
            int idx = groupTable.getSelectedRow();
            if (idx >= 0) {
                TreeSet<String> group = groups.get(groupModel.getValueAt(idx, 0));
	        	int[] selected = selectionTable.getSelectedRows();
	            if (selected.length > 0) {
	            	for (int i =  selected.length - 1; i >= 0; --i) {
	            	    idx = selected[i];
		                String val = (String)selectionModel.getValueAt(idx, 0);
		                group.remove(val);
		                columnsModel.addRow(new Object[]{val});
		                columns.add(val);
		                selectionModel.removeRow(idx);
	            	}
	            }
                selectionTable.clearSelection();
                columnsTable.clearSelection();
        	}
        	else
                JOptionPane.showMessageDialog(null, "Please select the group", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

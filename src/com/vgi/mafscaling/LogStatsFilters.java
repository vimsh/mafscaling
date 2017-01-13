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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIDefaults;

public class LogStatsFilters implements ActionListener {
	private String[] colNames = null;
    private ArrayList<JButton> filterButtonList = null;
    private ArrayList<JComboBox<String>> filterComboBoxList = null;
    private ArrayList<JComboBox<String>> filterColumnList = null;
    private ArrayList<JFormattedTextField> filterTextBoxList = null;
    private ArrayList<JButton> tmpFilterButtonList = null;
    private ArrayList<JComboBox<String>> tmpFilterComboBoxList = null;
    private ArrayList<JComboBox<String>> tmpFilterColumnList = null;
    private ArrayList<JFormattedTextField> tmpFilterTextBoxList = null;
    private NumberFormat doubleFmtFilters = null;
    private JPanel dataPanel = null;
    private JPanel filtersPanel = null;
    private Insets insets0 = new Insets(0, 0, 0, 0);
    private Insets insets3 = new Insets(3, 3, 3, 3);
    private UIDefaults andButtonInsets = new UIDefaults();
    
    public LogStatsFilters(String[] names, ArrayList<JButton> buttons, ArrayList<JComboBox<String>> columns, ArrayList<JComboBox<String>> conditions, ArrayList<JFormattedTextField> values) {
    	colNames = names;
        filterButtonList = buttons;
        filterColumnList = columns;
        filterComboBoxList = conditions;
        filterTextBoxList = values;
        initialize();
    }
    
    @SuppressWarnings("unchecked")
    private void initialize() {
        tmpFilterButtonList = (ArrayList<JButton>) filterButtonList.clone();
        tmpFilterColumnList = (ArrayList<JComboBox<String>>) filterColumnList.clone();
        tmpFilterComboBoxList = (ArrayList<JComboBox<String>>) filterComboBoxList.clone();
        tmpFilterTextBoxList = (ArrayList<JFormattedTextField>) filterTextBoxList.clone();
        
        doubleFmtFilters = NumberFormat.getNumberInstance();
        doubleFmtFilters.setGroupingUsed(false);
        doubleFmtFilters.setMaximumFractionDigits(6);
        doubleFmtFilters.setMinimumFractionDigits(0);
        doubleFmtFilters.setRoundingMode(RoundingMode.HALF_UP);

        andButtonInsets.put("Button.contentMargins", new Insets(4, 8, 4, 8));
        
        dataPanel = new JPanel();
        GridBagLayout gbl_dataPanel = new GridBagLayout();
        gbl_dataPanel.columnWidths = new int[]{0};
        gbl_dataPanel.rowHeights = new int[] {0, 0};
        gbl_dataPanel.columnWeights = new double[]{1.0};
        gbl_dataPanel.rowWeights = new double[]{0.0, 1.0};
        dataPanel.setLayout(gbl_dataPanel);
        dataPanel.setPreferredSize(new Dimension(450, 300));

        createControlPanel();
        createFilterPanel();
        addExistingFilters();

        JComponent[] inputs = new JComponent[] { dataPanel };
        do {
            if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null, inputs, "Log Stats Filters", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE))
                return;
        }
        while (!validateFilters());
        
        filterButtonList.clear();
        filterColumnList.clear();
        filterComboBoxList.clear();
        filterTextBoxList.clear();
        filterButtonList.addAll(tmpFilterButtonList);
        filterColumnList.addAll(tmpFilterColumnList);
        filterComboBoxList.addAll(tmpFilterComboBoxList);
        filterTextBoxList.addAll(tmpFilterTextBoxList);
    }

    private void createControlPanel() {
        JPanel panel = new JPanel();
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[]{0, 0};
        gbl_panel.rowHeights = new int[] {0};
        gbl_panel.columnWeights = new double[]{0.0, 1.0};
        gbl_panel.rowWeights = new double[]{0.0};
        panel.setLayout(gbl_panel);

        JButton addButton = new JButton("Add Filter");
        GridBagConstraints gbc_addButton = new GridBagConstraints();
        gbc_addButton.anchor = GridBagConstraints.EAST;
        gbc_addButton.insets = new Insets(3, 0, 3, 3);
        gbc_addButton.gridx = 0;
        gbc_addButton.gridy = 0;
        addButton.setActionCommand("add");
        addButton.addActionListener(this);
        panel.add(addButton, gbc_addButton);

        JButton remButton = new JButton("Remove Selected");
        GridBagConstraints gbc_remButton = new GridBagConstraints();
        gbc_remButton.anchor = GridBagConstraints.EAST;
        gbc_remButton.insets = new Insets(3, 3, 3, 0);
        gbc_remButton.gridx = 1;
        gbc_remButton.gridy = 0;
        remButton.setActionCommand("remove");
        remButton.addActionListener(this);
        panel.add(remButton, gbc_remButton);
        
        GridBagConstraints gbc_fullWidth = new GridBagConstraints();
        gbc_fullWidth.anchor = GridBagConstraints.PAGE_START;
        gbc_fullWidth.fill = GridBagConstraints.HORIZONTAL;
        gbc_fullWidth.insets = insets3;
        gbc_fullWidth.gridx = 0;
        gbc_fullWidth.gridy = 0;
        dataPanel.add(panel, gbc_fullWidth);
    }

    private void createFilterPanel() {
        filtersPanel = new JPanel();
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0};
        gbl_panel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0};
        gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1};
        filtersPanel.setLayout(gbl_panel);
        
        GridBagConstraints gbc_fullWidth = new GridBagConstraints();
        gbc_fullWidth.anchor = GridBagConstraints.PAGE_START;
        gbc_fullWidth.fill = GridBagConstraints.BOTH;
        gbc_fullWidth.insets = insets3;
        gbc_fullWidth.gridx = 0;
        gbc_fullWidth.gridy = 1;

        dataPanel.add(new JScrollPane(filtersPanel), gbc_fullWidth);
    }

    private void addExistingFilters() {
    	for (int row = 0; row < tmpFilterButtonList.size(); ++row) {
    		addAndButton(row, 0, tmpFilterButtonList.get(row));
    		addComboBox(row, 1, tmpFilterColumnList.get(row));
    		addComboBox(row, 2, tmpFilterComboBoxList.get(row));
    		addTextFilter(row, 3, tmpFilterTextBoxList.get(row));
    		addCheckBox(row);
    	}
    }

    private void addFilter() {
        int row = tmpFilterButtonList.size();
        tmpFilterButtonList.add(addAndButton(row, 0, "f" + row + "and"));
        tmpFilterColumnList.add(addComboBox(row, 1, colNames, true));
        tmpFilterComboBoxList.add(addComboBox(row, 2, new String[] {"", "<", "<=", "=", ">=", ">"}, false));
        tmpFilterTextBoxList.add(addTextFilter(row, 3, 5, doubleFmtFilters));
        addCheckBox(row);
        filtersPanel.revalidate();
        filtersPanel.repaint();
    }
    
    private void addComboBox(int row, int column, JComboBox<String> combo) {
        GridBagConstraints gbc_combo = new GridBagConstraints();
        gbc_combo.anchor = GridBagConstraints.WEST;
        gbc_combo.insets = new Insets(3, 0, 3, 3);
        gbc_combo.gridx = column;
        gbc_combo.gridy = row;
        filtersPanel.add(combo, gbc_combo);
    }
    
    private JComboBox<String> addComboBox(int row, int column, String[] values, boolean setProtoVal) {
        JComboBox<String> combo = new JComboBox<String>(values);
        if (setProtoVal)
        	combo.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXX");
        addComboBox(row, column, combo);
        return combo;
    }
    
    private void addTextFilter(int row, int column, JFormattedTextField textField) {
        GridBagConstraints gbc_textField = new GridBagConstraints();
        gbc_textField.anchor = GridBagConstraints.WEST;
        gbc_textField.insets = new Insets(3, 0, 3, 3);
        gbc_textField.gridx = column;
        gbc_textField.gridy = row;
        filtersPanel.add(textField, gbc_textField);
    }
    
    private JFormattedTextField addTextFilter(int row, int column, int numColumns, NumberFormat format) {
        JFormattedTextField textField = new JFormattedTextField(format);
        textField.setColumns(numColumns);
        addTextFilter(row, column, textField);
        return textField;
    }

    private void addAndButton(int row, int column, JButton button) {
        if (button != null) {
            GridBagConstraints gbc_button = new GridBagConstraints();
            gbc_button.anchor = GridBagConstraints.EAST;
            gbc_button.insets = new Insets(0, 0, 0, 3);
            gbc_button.fill = GridBagConstraints.HORIZONTAL;
            gbc_button.gridx = column;
            gbc_button.gridy = row;
            filtersPanel.add(button, gbc_button);
        }
        else {
            JLabel label = new JLabel("          ");
            GridBagConstraints gbc_label = new GridBagConstraints();
            gbc_label.anchor = GridBagConstraints.EAST;
            gbc_label.insets = insets3;
            gbc_label.gridx = 0;
            gbc_label.gridy = 0;
            filtersPanel.add(label, gbc_label);
        }
    }

    private JButton addAndButton(int row, int column, String action) {
        JButton button = null;
        if (row > 0) {
            button = new JButton("and");
            button.setMargin(insets0);
            button.putClientProperty("Nimbus.Overrides", andButtonInsets);
            button.setActionCommand(action);
            button.addActionListener(this);
        }
        addAndButton(row, column, button);
        return button;
    }
    
    private void addCheckBox(int row) {
        GridBagConstraints gbc_checkBox = new GridBagConstraints();
        gbc_checkBox.anchor = GridBagConstraints.WEST;
        gbc_checkBox.insets = new Insets(3, 10, 3, 3);
        gbc_checkBox.gridx = 4;
        gbc_checkBox.gridy = row;
        filtersPanel.add(new JCheckBox(""), gbc_checkBox);
    }
    
    private void add() {
    	addFilter();
    }
    
    
    private void remove() {
    	boolean removed = false;
        for (int k = tmpFilterButtonList.size() - 1; k >= 0; --k) {
        	JCheckBox checkBox = (JCheckBox) filtersPanel.getComponent(k * 5 + 4);
            if (checkBox.isSelected()) {
                tmpFilterButtonList.remove(k);
                tmpFilterColumnList.remove(k);
                tmpFilterComboBoxList.remove(k);
                tmpFilterTextBoxList.remove(k);
                removed = true;
            }
        }
        if (removed) {
        	tmpFilterButtonList.set(0, null);
            filtersPanel.removeAll();
        	addExistingFilters();
	        filtersPanel.revalidate();
	        filtersPanel.repaint();
        }
    }
    
    private boolean validateFilters() {
        for (int k = 0; k < tmpFilterButtonList.size(); ++k) {
            if (tmpFilterComboBoxList.get(k).getSelectedItem() == null ||
            	tmpFilterComboBoxList.get(k).getSelectedItem().equals("") ||
            	tmpFilterColumnList.get(k).getSelectedItem() == null ||
                tmpFilterColumnList.get(k).getSelectedItem().equals("") ||
                tmpFilterTextBoxList.get(k).getValue() == null ||
                tmpFilterTextBoxList.get(k).getValue().equals("")) {
                JOptionPane.showMessageDialog(null, "Please remove any filters that aren't set", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("add".equals(e.getActionCommand()))
            add();
        else if ("remove".equals(e.getActionCommand()))
            remove();
        else {
            boolean handled = false;
            for (int k = 1; !handled && k < tmpFilterButtonList.size(); ++k) {
                String cmd = "f" + k + "and";
                if (cmd.equals(e.getActionCommand())) {
                    handled = true;
                    tmpFilterButtonList.get(k).setText("and".equals(tmpFilterButtonList.get(k).getText()) ? "or" : "and");
                }
            }
        }
    }
}

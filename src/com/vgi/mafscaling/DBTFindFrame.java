package com.vgi.mafscaling;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import quick.dbtable.DBTable;

class DBTFindFrame extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1513208156777653811L;
    private static final Logger logger = Logger.getLogger(DBTFindFrame.class);

    class ChangeListener implements ItemListener{
        public void itemStateChanged(ItemEvent e)
        {
            @SuppressWarnings("unchecked")
            JComboBox<String> cb = (JComboBox<String>) e.getSource();
            String text = (String)cb.getSelectedItem();
            if (text == null || "".equals(text))
                return;
            boolean exists= false;
            for (int i = 0; i < cb.getItemCount(); ++i)
            {
                if (text.equals(cb.getItemAt(i)))
                {
                    exists=true;
                    break;
                }
            }
            if (!exists)
                cb.addItem(text);
        }
    }

    private Window parent = null;
    private JPanel mainPanel = null;
    private JComboBox<String> findCombo = null;
    private JComboBox<String> replaceCombo = null;
    private JCheckBox searchRow = null;
    private JCheckBox searchColumn = null;
    private JButton nextButton = null;
    private JButton previousButton = null;
    private JButton replaceAllButton = null;
    private JButton closeButton = null;
    private DBTable dbTable = null;
    private Vector<Integer> columnVector = null;
    private boolean find = true;
    private Point lastLoc = new Point(0, 0);
    private Insets insetsLabel = new Insets(3, 3, 3, 0);
    private Insets insets3 = new Insets(3, 3, 3, 3);

    DBTFindFrame(Window owner, DBTable table, boolean isFind) {
        super(owner, (isFind ? "Find Dialog" : "Replace Dialog"));
        parent = owner;
        dbTable = table;
        find = isFind;
        initialize();
    }

    private void initialize() {
        try {
            mainPanel = new JPanel();
            GridBagLayout gbl_dataPanel = new GridBagLayout();
            gbl_dataPanel.columnWidths = new int[] {0, 0, 0, 0};
            gbl_dataPanel.rowHeights = new int[] {0, 0, 0, 0, 0};
            gbl_dataPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0};
            gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
            mainPanel.setLayout(gbl_dataPanel);
            getContentPane().add(mainPanel);

            ChangeListener cl = new ChangeListener();
            addLabel(0, "Text to find");
            findCombo = addComboBox(0);
            findCombo.addItemListener(cl);
            if (!find) {
                addLabel(1, "Replace with");
                replaceCombo = addComboBox(1);
                replaceCombo.addItemListener(cl);
            }
            searchRow = addCheckBox(2, "From current row");
            searchColumn = addCheckBox(3, "From current column");
            if (!find) {
                replaceAllButton = addButton(0, "Replace All");
                nextButton = addButton(1, "Replace Next");
                previousButton = addButton(2, "Replace Previous");
            }
            else {
                nextButton = addButton(1, "Find Next");
                previousButton = addButton(2, "Find Previous");
            }
            closeButton = addButton(3, "Close");
            getRootPane().setDefaultButton(nextButton);            
            pack();
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setIconImage(null);
            setResizable(false);
            setLocationRelativeTo(parent);
            setVisible(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateInput() {
        String findText = (String)findCombo.getSelectedItem();
        if (findText == null || "".equals(findText))
            return false;
        if (!find) {
            String replaceText = (String)replaceCombo.getSelectedItem();
            if (replaceText == null || "".equals(replaceText))
                return false;
        }
        return true;
    }
    
    private void addLabel(int row, String text) {
        JLabel label = new JLabel(text);
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.anchor = GridBagConstraints.EAST;
        gbc_label.insets = insetsLabel;
        gbc_label.gridx = 0;
        gbc_label.gridy = row;
        mainPanel.add(label, gbc_label);
    }
    
    private JComboBox<String> addComboBox(int row) {
        JComboBox<String> comboBox = new JComboBox<String>();
        comboBox.setEditable(true);
        GridBagConstraints gbc_comboBox = new GridBagConstraints();
        gbc_comboBox.anchor = GridBagConstraints.WEST;
        gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_comboBox.insets = insets3;
        gbc_comboBox.gridx = 1;
        gbc_comboBox.gridy = row;
        gbc_comboBox.gridwidth = 3;
        mainPanel.add(comboBox, gbc_comboBox);
        return comboBox;
    }
    
    private JCheckBox addCheckBox(int row, String text) {
        JCheckBox flag = new JCheckBox(text);
        GridBagConstraints gbc_check = new GridBagConstraints();
        gbc_check.anchor = GridBagConstraints.WEST;
        gbc_check.insets = insets3;
        gbc_check.gridx = 1;
        gbc_check.gridy = row;
        gbc_check.gridwidth = 3;
        mainPanel.add(flag, gbc_check);
        return flag;
    }
    
    private JButton addButton(int col, String name) {
        JButton button = new JButton(name);
        GridBagConstraints gbc_button = new GridBagConstraints();
        gbc_button.anchor = GridBagConstraints.CENTER;
        gbc_button.insets = insets3;
        gbc_button.gridx = col;
        gbc_button.gridy = 4;
        button.addActionListener(this);
        mainPanel.add(button, gbc_button);
        return button;
    }
    
    private void doWork(boolean direction, boolean replaceAll) {
        if (!validateInput())
            return;
        try {
            int row = -1;
            columnVector = new Vector<Integer>();
            String findText = (String)findCombo.getSelectedItem();
            lastLoc.x = dbTable.getSelectedRow();
            lastLoc.y = dbTable.getSelectedColumn();
            if (searchRow.isSelected())
                row = lastLoc.x;
            if (searchColumn.isSelected())
                columnVector.add(lastLoc.y + 1);
            else {
                for (int i = 1; i <= dbTable.getColumnCount(); ++i)
                    columnVector.addElement(i);
            }
            if (columnVector.size() == 1)
                lastLoc.y = 0;
            if (find) {
                if (!direction) {
                    if (lastLoc.x == 0 && lastLoc.y == 0 && columnVector.size() > 1) {
                        JOptionPane.showMessageDialog(null, "Finished Searching", "Message", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    if (lastLoc.y == 0)
                    {
                        lastLoc.x = lastLoc.x - 1;
                        if (columnVector.size() > 1)
                            lastLoc.y = columnVector.size() - 1;
                    }
                    else
                        lastLoc.y = lastLoc.y - 1;
                }
                if (row == -1)
                    lastLoc = dbTable.find(lastLoc.x, lastLoc.y, findText, columnVector, direction);
                else {
                    int[] rows = { row };
                    if (direction && lastLoc.y < columnVector.size() - 1)
                        lastLoc = dbTable.find(lastLoc.x, lastLoc.y + 1, findText, columnVector, direction, rows);
                    else if (!direction && lastLoc.y > 1)
                        lastLoc = dbTable.find(lastLoc.x, lastLoc.y - 1, findText, columnVector, direction, rows);
                    else {
                        JOptionPane.showMessageDialog(null, "Finished Searching", "Message", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                }
            }
            else {
                String replaceText = (String)replaceCombo.getSelectedItem();
                if (!replaceAll) {
                    if (!direction) {
                        if (lastLoc.x == 0 && lastLoc.y == 0 && columnVector.size() > 1)
                            return;
                        if (lastLoc.y == 0)
                        {
                            lastLoc.x = lastLoc.x - 1;
                            if (columnVector.size() > 1)
                                lastLoc.y = columnVector.size() - 1;
                        }
                        else
                            lastLoc.y = lastLoc.y - 1;
                    }
                    if (row == -1)
                        lastLoc = dbTable.replace(lastLoc.x, lastLoc.y, findText, replaceText, columnVector, direction);
                    else {
                        int[] rows = { row };
                        if (direction && lastLoc.y < columnVector.size() - 1)
                            lastLoc = dbTable.replace(lastLoc.x, lastLoc.y + 1, findText, replaceText, columnVector, direction, rows);
                        else if (!direction && lastLoc.y > 1)
                            lastLoc = dbTable.replace(lastLoc.x, lastLoc.y - 1, findText, replaceText, columnVector, direction, rows);
                        else {
                            JOptionPane.showMessageDialog(null, "Finished Searching", "Message", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }
                    }
                }
                else {
                    if (row == -1)
                        lastLoc = dbTable.replaceAll(0, lastLoc.y, findText, replaceText, columnVector);
                    else {
                        int[] rows = { row };
                        lastLoc = dbTable.replaceAll(lastLoc.x, 1, findText, replaceText, columnVector, rows);
                    }
                }
            }
            if (columnVector.size() == 1)
                lastLoc.y = columnVector.get(0) - 1;
            if (dbTable.getTable().getCellEditor() != null)
                dbTable.getTable().getCellEditor().stopCellEditing();
            
            dbTable.getTable().setRowSelectionInterval(lastLoc.x, lastLoc.x);
            dbTable.getTable().setColumnSelectionInterval(lastLoc.y, lastLoc.y);
            dbTable.getTable().changeSelection(lastLoc.x, lastLoc.y, false, false);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
        dbTable.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == nextButton)
            doWork(true, false);
        else if (e.getSource() == previousButton)
            doWork(false, false);
        else if (e.getSource() == replaceAllButton)
            doWork(false, true);
        else if (e.getSource() == closeButton)
            setVisible(false);
    }
} 
package com.vgi.mafscaling;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class MafTablePane extends JScrollPane {
    private static final long serialVersionUID = 4656913571229048807L;
    public static final int MafTableColumnCount = 35;
    JTable mafTable = null;
    JLabel voltLabel = null;
    JLabel gsLabel = null;
    
    public MafTablePane(int columnWidth, String tableName, boolean editableFirstRow, boolean editableSecondRow) {
        Insets insets0 = new Insets(0, 0, 0, 0);
        JPanel dataMafPanel = new JPanel();
        GridBagLayout gbl_dataMafPanel = new GridBagLayout();
        gbl_dataMafPanel.columnWidths = new int[]{0, 0};
        gbl_dataMafPanel.rowHeights = new int[] {0, 0};
        gbl_dataMafPanel.columnWeights = new double[]{0.0, 1.0};
        gbl_dataMafPanel.rowWeights = new double[]{0.0, 0.0};
        dataMafPanel.setLayout(gbl_dataMafPanel);
        
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.anchor = GridBagConstraints.EAST;
        gbc_label.insets = insets0;
        gbc_label.gridx = 0;
        gbc_label.gridy = 0;
        voltLabel = new JLabel("volt ");
        dataMafPanel.add(voltLabel, gbc_label);
        gbc_label.gridy = 1;
        gsLabel = new JLabel("g/s ");
        dataMafPanel.add(gsLabel, gbc_label);
    
        if (editableFirstRow && editableSecondRow)
            mafTable = new JTable();
        else if (editableFirstRow) {
            mafTable = new JTable() {
                private static final long serialVersionUID = 7749582128758153892L;
                public boolean isCellEditable(int row, int column) { if (row == 1) return true; return false; };
            };
        }
        else if (editableSecondRow) {
            mafTable = new JTable() {
                private static final long serialVersionUID = 7749582128758153892L;
                public boolean isCellEditable(int row, int column) { if (row == 1) return false; return true; };
            };
        }
        else {
            mafTable = new JTable() {
                private static final long serialVersionUID = -7484222189491449568L;
                public boolean isCellEditable(int row, int column) { return false; };
            };
        }
        
        mafTable.setColumnSelectionAllowed(true);
        mafTable.setCellSelectionEnabled(true);
        mafTable.setBorder(new LineBorder(new Color(0, 0, 0)));
        mafTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        mafTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        mafTable.setModel(new DefaultTableModel(2, MafTableColumnCount));
        mafTable.setTableHeader(null);
        mafTable.putClientProperty("terminateEditOnFocusLost", true);
        Utils.initializeTable(mafTable, columnWidth);
        GridBagConstraints gbc_mafTable = new GridBagConstraints();
        gbc_mafTable.insets = insets0;
        gbc_mafTable.fill = GridBagConstraints.HORIZONTAL;
        gbc_mafTable.weightx = 1.0;
        gbc_mafTable.gridx = 1;
        gbc_mafTable.gridy = 0;
        gbc_mafTable.gridheight = 2;
        dataMafPanel.add(mafTable, gbc_mafTable);

        setViewportView(dataMafPanel);
        if (tableName != null)
            setViewportBorder(new TitledBorder(null, tableName, TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    }
    
    public void hideRowHeaders() {
        voltLabel.setVisible(false);
        gsLabel.setVisible(false);
    }
    
    public void showRowHeaders() {
        voltLabel.setVisible(true);
        gsLabel.setVisible(true);
    }
    
    public JTable getJTable() {
        return mafTable;
    }
}

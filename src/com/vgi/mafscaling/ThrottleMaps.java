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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

public class ThrottleMaps extends ACompCalc {
    private static final long serialVersionUID = -1113885906460824445L;
    private static final Logger logger = Logger.getLogger(ThrottleMaps.class);
    
    protected String resultTableName;
    protected int TableRowCount = 10;

    public ThrottleMaps(int tabPlacement) {
        super(tabPlacement);
        origTableName = "Reqd Torque - RPM";
        newTableName = "Requested Torque - Accelerator Pedal";
        corrTableName = "Target Throttle Plate Position";
        resultTableName = "Throttle Plate vs Accelerator Pedal";
        x3dAxisName = "Accelerated Pedal Angle (%)";
        y3dAxisName = "Engine Speed (RPM)";
        z3dAxisName = "Throttle Plate Angle (%)";
        initialize(new String[] {});
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // DATA TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    protected void createDataTab(String[] logColumns) {
        JPanel dataPanel = new JPanel();
        add(dataPanel, "<html><div style='text-align: center;'>D<br>a<br>t<br>a</div></html>");
        GridBagLayout gbl_dataPanel = new GridBagLayout();
        gbl_dataPanel.columnWidths = new int[] {0};
        gbl_dataPanel.rowHeights = new int[] {0, 0, 0};
        gbl_dataPanel.columnWeights = new double[]{0.0};
        gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 1.0};
        dataPanel.setLayout(gbl_dataPanel);
        
        createControlPanel(dataPanel);
        createDataPanel(dataPanel, logColumns);
    }

    protected void createControlPanel(JPanel dataPanel) {
        JPanel cntlPanel = new JPanel();
        GridBagConstraints gbl_ctrlPanel = new GridBagConstraints();
        gbl_ctrlPanel.insets = insets3;
        gbl_ctrlPanel.anchor = GridBagConstraints.NORTHWEST;
        gbl_ctrlPanel.fill = GridBagConstraints.HORIZONTAL;
        gbl_ctrlPanel.gridx = 0;
        gbl_ctrlPanel.gridy = 0;
        gbl_ctrlPanel.weightx = 1.0;
        dataPanel.add(cntlPanel, gbl_ctrlPanel);
        
        GridBagLayout gbl_cntlPanel = new GridBagLayout();
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);

        addButton(cntlPanel, 0, "Clear All", "clearall", GridBagConstraints.WEST);
        addButton(cntlPanel, 1, "Clear Result Data", "clearlog", GridBagConstraints.WEST);
        addCheckBox(cntlPanel, 2, "Hide Input Tables", "hidelogtable");
        addButton(cntlPanel, 3, "GO", "go", GridBagConstraints.EAST);
    }

    protected void createDataPanel(JPanel dataPanel, String[] logColumns) {
        dataScrollPane = new JScrollPane();
        GridBagConstraints gbc_dataScrollPane = new GridBagConstraints();
        gbc_dataScrollPane.insets = insets3;
        gbc_dataScrollPane.anchor = GridBagConstraints.NORTHWEST;
        gbc_dataScrollPane.fill = GridBagConstraints.BOTH;
        gbc_dataScrollPane.gridx = 0;
        gbc_dataScrollPane.gridy = 1;
        gbc_dataScrollPane.weightx = 1.0;
        gbc_dataScrollPane.weighty = 1.0;
        dataPanel.add(dataScrollPane, gbc_dataScrollPane);

        JPanel dataInputPanel = new JPanel();
        dataScrollPane.setViewportView(dataInputPanel);
        GridBagLayout gbl_dataInputPanel = new GridBagLayout();
        gbl_dataInputPanel.columnWidths = new int[]{0, 0, 0, 0};
        gbl_dataInputPanel.rowHeights = new int[] {0, 0};
        gbl_dataInputPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0};
        gbl_dataInputPanel.rowWeights = new double[]{0.0, 1.0};
        dataInputPanel.setLayout(gbl_dataInputPanel);

        JScrollPane resultTableScrollPane = new JScrollPane();
        GridBagConstraints gbc_resultTableScrollPane = new GridBagConstraints();
        gbc_dataScrollPane.insets = insets3;
        gbc_dataScrollPane.anchor = GridBagConstraints.NORTHWEST;
        gbc_resultTableScrollPane.fill = GridBagConstraints.BOTH;
        gbc_resultTableScrollPane.gridx = 0;
        gbc_resultTableScrollPane.gridy = 2;
        gbc_resultTableScrollPane.weightx = 1.0;
        gbc_resultTableScrollPane.weighty = 1.0;
        dataPanel.add(resultTableScrollPane, gbc_resultTableScrollPane);
        
        JPanel resultPanel = new JPanel();
        resultTableScrollPane.setViewportView(resultPanel);
        GridBagLayout gbl_resultPanel = new GridBagLayout();
        gbl_resultPanel.columnWidths = new int[]{0, 0};
        gbl_resultPanel.rowHeights = new int[] {0, 0};
        gbl_resultPanel.columnWeights = new double[]{0.0, 1.0};
        gbl_resultPanel.rowWeights = new double[]{0.0, 1.0};
        resultPanel.setLayout(gbl_resultPanel);

        createDataTables(dataInputPanel);
        createLogDataTable(resultPanel, logColumns);
    }
    
    protected void createLogDataTable(JPanel panel, String[] columns) {
        logDataTable = createDataTable(panel, resultTableName, TableRowCount, TableRowCount, 0, 0, false, true, true);
    }
    
    protected void createDataTables(JPanel panel) {
        origTable = createDataTable(panel, origTableName, 2, TableRowCount, 0, 0, true, true, false);
        newTable = createDataTable(panel, newTableName, TableRowCount, TableRowCount, 1, 0, true, true, true);
        corrTable = createDataTable(panel, corrTableName, TableRowCount, TableRowCount, 2, 0, true, true, true);
    }
    
    protected JTable createDataTable(JPanel panel, String tableName, int colCount, int rowCount, int gridx, int gridy, boolean isOriginalTable, boolean extendRows, boolean extendCols) {
        final JTable table = new JTable() {
            private static final long serialVersionUID = 3218402382894083287L;
        };
        ExcelAdapter excelAdapter = new ExcelAdapter() {
            protected void onPaste(JTable table, boolean extendRows, boolean extendCols) {
                if (table.getSelectedRows() == null || table.getSelectedRows().length == 0 ||
                    table.getSelectedColumns() == null || table.getSelectedColumns().length == 0)
                    return;
                if (table.getSelectedRows()[0] != 0 || table.getSelectedColumns()[0] != 0) {
                    JOptionPane.showMessageDialog(null, "Please paste copied table into the first cell", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                super.onPaste(table, extendRows, extendCols);
                validateTable(table);
                clearRunTables();
            }
            protected void onPasteVertical(JTable table, boolean extendRows, boolean extendCols) {
                if (table.getSelectedRows() == null || table.getSelectedRows().length == 0 ||
                    table.getSelectedColumns() == null || table.getSelectedColumns().length == 0)
                    return;
                if (table.getSelectedRows()[0] != 0 || table.getSelectedColumns()[0] != 0) {
                    JOptionPane.showMessageDialog(null, "Please paste copied table into the first cell", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                super.onPasteVertical(table, extendRows, extendCols);
                validateTable(table);
                clearRunTables();
            }
        };
        if (tableName == origTableName)
            excelAdapter.addTable(table, false, true, false, false, true, false, true, extendRows, extendCols);
        else
            excelAdapter.addTable(table, false, true, false, false, true, true, false, extendRows, extendCols);
        excelAdapterList.add(excelAdapter);

        DefaultTableColumnModel tableModel = new DefaultTableColumnModel();
        final TableColumn tableColumn = new TableColumn(0, colCount * ColumnWidth);
        tableColumn.setHeaderValue(tableName);
        tableModel.addColumn(tableColumn);
        JTableHeader lblTableHeaderName = table.getTableHeader();
        lblTableHeaderName.setColumnModel(tableModel);
        lblTableHeaderName.setReorderingAllowed(false);
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) lblTableHeaderName.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        
        GridBagConstraints gbc_lblTableName = new GridBagConstraints();
        gbc_lblTableName.insets = new Insets(0,0,0,3);
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

        if (tableName == newTableName) {
            table.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent event) {
                    if (Utils.isTableEmpty(origTable) || Utils.isTableEmpty(newTable) || Utils.isTableEmpty(corrTable)) {
                        return;
                    }
                    JTable eventTable =(JTable)event.getSource();
                    int colIdx = eventTable.getSelectedColumn();
                    int rowIdx = eventTable.getSelectedRow();
                    ArrayList<Double> t3xAxisArray = new ArrayList<Double>();
                    for (int i = 1; i < corrTable.getColumnCount(); ++i)
                        t3xAxisArray.add(Double.valueOf(corrTable.getValueAt(0, i).toString()));
    
                    ArrayList<Double> t1yAxisArray = new ArrayList<Double>();
                    for (int i = 0; i < origTable.getRowCount(); ++i)
                        t1yAxisArray.add(Double.valueOf(origTable.getValueAt(i, 0).toString()));
    
                    ArrayList<Double> t3yAxisArray = new ArrayList<Double>();
                    for (int i = 1; i < corrTable.getRowCount(); ++i)
                        t3yAxisArray.add(Double.valueOf(corrTable.getValueAt(i, 0).toString()));
                    if (colIdx > 0 && rowIdx >= 0) {
                        try {
                            calculate(t3xAxisArray, t1yAxisArray, t3yAxisArray, rowIdx, colIdx, true);
                        }
                        catch (Exception e) { }
                    }
                }
            });
        }
        
        Utils.addTableHeaderHighlight(table);
        table.setName(tableName);
        table.getTableHeader().setReorderingAllowed(false);
        table.setColumnSelectionAllowed(true);
        table.setCellSelectionEnabled(true);
        table.setBorder(new LineBorder(new Color(0, 0, 0)));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setModel(new DefaultTableModel(rowCount, colCount));
        table.putClientProperty("terminateEditOnFocusLost", true);
        Utils.initializeTable(table, ColumnWidth);
        
        formatTable(table);
        
        GridBagConstraints gbc_table = new GridBagConstraints();
        gbc_table.insets = new Insets(0,0,0,3);
        gbc_table.anchor = GridBagConstraints.NORTHWEST;
        gbc_table.gridx = gridx;
        gbc_table.gridy = gridy + 1;
        panel.add(table, gbc_table);

        return table;
    }
    
    protected void clearRunTables() {
        logDataTable.setModel(new DefaultTableModel(newTable.getRowCount(), newTable.getColumnCount()));
        Utils.initializeTable(logDataTable, ColumnWidth);
        formatTable(logDataTable);
        plot3d.removeAllPlots();
    }
    
    protected void clearRunTable(JTable table) {
        if (table == origTable)
            table.setModel(new DefaultTableModel(TableRowCount, 2));
        else
            table.setModel(new DefaultTableModel(TableRowCount, TableRowCount));
        Utils.initializeTable(table, ColumnWidth);
        formatTable(table);
    }
    
    protected void clearLogDataTables() {
    }
    
    protected void clearTables() {
        clearRunTable(origTable);
        clearRunTable(newTable);
        clearRunTable(corrTable);
        clearRunTables();
    }
    
    protected void formatTable(JTable table) {
        Format[][] formatMatrix = { { new DecimalFormat("#"), new DecimalFormat("0.0000") } };
        NumberFormatRenderer renderer = (NumberFormatRenderer)table.getDefaultRenderer(Object.class);
        renderer.setFormats(formatMatrix);
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE USAGE TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    protected String usage() {
        ResourceBundle bundle;
        bundle = ResourceBundle.getBundle("com.vgi.mafscaling.thrtlmaps");
        return bundle.getString("usage"); 
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // COMMOMN ACTIONS RELATED FUNCTIONS
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected boolean processLog() {
        if (Utils.isTableEmpty(origTable)) {
            JOptionPane.showMessageDialog(null, "Please paste " + origTable.getName() + " table into first top grid", "Error getting " + origTable.getName() + " table data", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (Utils.isTableEmpty(newTable)) {
            JOptionPane.showMessageDialog(null, "Please paste " + newTable.getName() + " table into second top grid", "Error getting " + newTable.getName() + " table data", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (Utils.isTableEmpty(corrTable)) {
            JOptionPane.showMessageDialog(null, "Please paste " + corrTable.getName() + " table into third top grid", "Error getting " + corrTable.getName() + " table data", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            ArrayList<Double> t3xAxisArray = new ArrayList<Double>();
            for (int i = 1; i < corrTable.getColumnCount(); ++i)
                t3xAxisArray.add(Double.valueOf(corrTable.getValueAt(0, i).toString()));

            ArrayList<Double> t1yAxisArray = new ArrayList<Double>();
            for (int i = 0; i < origTable.getRowCount(); ++i)
                t1yAxisArray.add(Double.valueOf(origTable.getValueAt(i, 0).toString()));

            ArrayList<Double> t3yAxisArray = new ArrayList<Double>();
            for (int i = 1; i < corrTable.getRowCount(); ++i)
                t3yAxisArray.add(Double.valueOf(corrTable.getValueAt(i, 0).toString()));
            
            for (int i = 0; i < newTable.getRowCount(); ++i) {
                for (int j = 0; j < newTable.getColumnCount(); ++j) {
                    if (0 == i || j == 0) {
                        logDataTable.setValueAt(newTable.getValueAt(i, j), i, j);
                    }
                    else {
                        logDataTable.setValueAt(calculate(t3xAxisArray, t1yAxisArray, t3yAxisArray, i, j, false), i, j);
                    }
                }
            }
            Utils.colorTable(logDataTable);
            add3DPlot(logDataTable);
            return true;
        }
        catch (Exception e) {
            logger.error(e);
            JOptionPane.showMessageDialog(null, "Error calculating values: " + e, "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    
    private Double calculate(ArrayList<Double> t3xAxisArray, ArrayList<Double> t1yAxisArray, ArrayList<Double> t3yAxisArray, int row, int column, boolean highlight)
    {
        Double x, x1, x2, y, y1, y2, x1y1, x1y2, x2y1, x2y2, tmp, val1, val2;
        int idx, idxX1, idxX2, idxY1, idxY2;
        
        // get value from table 1
        x = Double.valueOf(newTable.getValueAt(row, 0).toString());
        idx = Utils.closestValueIndex(x, t1yAxisArray);
        tmp = Double.valueOf(origTable.getValueAt(idx, 0).toString());
        if (tmp > x && idx > 0) {
            x1 = Double.valueOf(origTable.getValueAt(idx - 1, 0).toString());
            x2 = tmp;
            y1 = Double.valueOf(origTable.getValueAt(idx - 1, 1).toString());
            y2 = Double.valueOf(origTable.getValueAt(idx, 1).toString());
            val1 = Utils.linearInterpolation(x, x1, x2, y1, y2);
            if (highlight) {
                origTable.setColumnSelectionInterval(0, 1);
                origTable.setRowSelectionInterval(idx - 1, idx);
            }
        }
        else if (tmp < x && idx < t1yAxisArray.size() - 1) {
            x1 = tmp;
            x2 = Double.valueOf(origTable.getValueAt(idx + 1, 0).toString());
            y1 = Double.valueOf(origTable.getValueAt(idx, 1).toString());
            y2 = Double.valueOf(origTable.getValueAt(idx + 1, 1).toString());
            val1 = Utils.linearInterpolation(x, x1, x2, y1, y2);
            if (highlight) {
                origTable.setColumnSelectionInterval(1, 1);
                origTable.setRowSelectionInterval(idx, idx + 1);
            }
        }
        else {
            val1 = Double.valueOf(origTable.getValueAt(idx, 1).toString());
            if (highlight) {
                origTable.setColumnSelectionInterval(1, 1);
                origTable.setRowSelectionInterval(idx, idx);
            }
        }
        
        // get value from table 2
        val2 = Double.valueOf(newTable.getValueAt(row, column).toString());
        
        // get value from table 3
        x = val2 / val1;
        idx = Utils.closestValueIndex(x, t3xAxisArray) + 1;
        tmp = Double.valueOf(corrTable.getValueAt(0, idx).toString());
        if (tmp > x && idx > 1) {
            idxX1 = idx - 1;
            idxX2 = idx;
            x1 = Double.valueOf(corrTable.getValueAt(0, idx - 1).toString());
            x2 = tmp;
        }
        else if (tmp < x && idx < t3xAxisArray.size()) {
            idxX1 = idx;
            idxX2 = idx  + 1;
            x1 = tmp;
            x2 = Double.valueOf(corrTable.getValueAt(0, idx + 1).toString());
        }
        else {
            idxX1 = idxX2 = idx;
            x1 = x2 = tmp;
        }

        y = Double.valueOf(newTable.getValueAt(row, 0).toString());
        idx = Utils.closestValueIndex(y, t3yAxisArray) + 1;
        tmp = Double.valueOf(corrTable.getValueAt(idx, 0).toString());
        if (tmp > y && idx > 1) {
            idxY1 = idx - 1;
            idxY2 = idx;
            y1 = Double.valueOf(corrTable.getValueAt(idx - 1, 0).toString());
            y2 = tmp;
        }
        else if (tmp < y && idx < t3yAxisArray.size()) {
            idxY1 = idx;
            idxY2 = idx  + 1;
            y1 = tmp;
            y2 = Double.valueOf(corrTable.getValueAt(idx + 1, 0).toString());
        }
        else {
            idxY1 = idxY2 = idx;
            y1 = y2 = tmp;
        }
        
        x1y1 = Double.valueOf(corrTable.getValueAt(idxY1, idxX1).toString());
        x1y2 = Double.valueOf(corrTable.getValueAt(idxY2, idxX1).toString());
        x2y1 = Double.valueOf(corrTable.getValueAt(idxY1, idxX2).toString());
        x2y2 = Double.valueOf(corrTable.getValueAt(idxY2, idxX2).toString());
        
        if (highlight) {
            corrTable.setColumnSelectionInterval(idxX1, idxX2);
            corrTable.setRowSelectionInterval(idxY1, idxY2);
            if (!Utils.isTableEmpty(logDataTable)) {
                logDataTable.setColumnSelectionInterval(column, column);
                logDataTable.setRowSelectionInterval(row, row);
            }
        }
        
        return Utils.bilinearInterpolation(x, y, x1, x2, y1, y2, x1y1, x1y2, x2y1, x2y2);
    }

    private void add3DPlot(JTable table) {
        int i;
        double[] xvals = new double[table.getColumnCount() - 1];
        double[] yvals = new double[table.getRowCount() - 1];
        for (i = 1; i < table.getColumnCount(); ++i)
            xvals[i - 1] = Double.valueOf(table.getValueAt(0, i).toString());
        for (i = 1; i < table.getRowCount(); ++i)
            yvals[i - 1] = Double.valueOf(table.getValueAt(i, 0).toString());
        double[][] zvals = Utils.doubleZArray(table, xvals, yvals);
        Color[][] tableColors = Utils.generateTableColorMatrix(table, 1, 1, yvals.length + 1, xvals.length + 1);
        Color[][] colors = new Color[yvals.length][xvals.length];
        for (int j = 1; j < tableColors.length; ++j)
            for (i = 1; i < tableColors[j].length; ++i)
                colors[j - 1][i - 1] = tableColors[j][i];
        plot3d.addGridPlot(resultTableName, colors, xvals, yvals, zvals);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        checkActionPerformed(e);
    }

    @Override
    protected boolean getAxisData() {
        return true;
    }

    @Override
    protected void loadLogFile() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void createGraphTab() {
        // TODO Auto-generated method stub
    }

    @Override
    protected boolean displayData() {
        // TODO Auto-generated method stub
        return false;
    }
}

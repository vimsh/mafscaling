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
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import javax.swing.text.DefaultFormatter;
import org.apache.log4j.Logger;

public class TableRescale extends ACompCalc {
    private static final long serialVersionUID = 7955359935085174590L;
    private static final Logger logger = Logger.getLogger(TableRescale.class);
    
    protected enum TableType {
        Table3D,
        Table2DVertical,
        Table2DHorizontal
    }

    protected TableType tableType = TableType.Table3D;
    protected JCheckBox intXCheckBox = null;
    protected JCheckBox intYCheckBox = null;
    protected JCheckBox intVCheckBox = null;
    protected JComboBox<String> interpTypeCheckBox = null;
    protected String decimalPts = "0.00";
    protected Format[][] formatMatrix = { { new DecimalFormat(decimalPts), new DecimalFormat(decimalPts) }, { new DecimalFormat(decimalPts), new DecimalFormat(decimalPts) } };
    protected double minX = 0;
    protected double maxX = 0;
    protected double minY = 0;
    protected double maxY = 0;

    public TableRescale(int tabPlacement) {
        super(tabPlacement);
        origTableName = "Original Table";
        newTableName = "Rescaled Table";
        x3dAxisName = "X";
        y3dAxisName = "Y";
        z3dAxisName = "Z";
        initialize(new String[] {});
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // DATA TAB
    //////////////////////////////////////////////////////////////////////////////////////

    protected void createControlPanel(JPanel dataPanel) {
        JPanel cntlPanel = new JPanel();
        GridBagConstraints gbl_ctrlPanel = new GridBagConstraints();
        gbl_ctrlPanel.insets = insets3;
        gbl_ctrlPanel.anchor = GridBagConstraints.NORTH;
        gbl_ctrlPanel.fill = GridBagConstraints.HORIZONTAL;
        gbl_ctrlPanel.gridx = 0;
        gbl_ctrlPanel.gridy = 0;
        gbl_ctrlPanel.weightx = 1.0;
        gbl_ctrlPanel.gridwidth = 2;
        dataPanel.add(cntlPanel, gbl_ctrlPanel);
        
        GridBagLayout gbl_cntlPanel = new GridBagLayout();
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);

        addButton(cntlPanel, 0, "Clear", "clearall", GridBagConstraints.WEST);
        addButton(cntlPanel, 1, "Reset Rescaled", "clearlog", GridBagConstraints.WEST);
        addLabel(cntlPanel, 2, "Precision: ");
        addSpinnerFilter(cntlPanel, 3, 2, 1, 4, 1);
        addLabel(cntlPanel, 4, " ");
        intXCheckBox = addCheckBox(cntlPanel, 5, "Integer X-Axis", "chgfmt");
        intYCheckBox = addCheckBox(cntlPanel, 6, "Integer Y-Axis", "chgfmt");
        intVCheckBox = addCheckBox(cntlPanel, 7, "Integer Values", "chgfmt");
        interpTypeCheckBox = addComboBox(cntlPanel, 8, getInterpolationMethods());
        addLabel(cntlPanel, 9, " ");
    }
    
    protected void createDataTables(JPanel panel) {
        dataScrollPane.setVisible(false);
        origTable = createDataTable(panel, origTableName, 0, true);
        excelAdapterList.set(0, new ExcelAdapter() {
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
                if (origTable.getColumnCount() == 2 && origTable.getRowCount() >= 2 && !origTable.getValueAt(0, 0).toString().equals("")) {
                    tableType = TableType.Table2DVertical;
                    intXCheckBox.setEnabled(false);
                    minX = Double.valueOf(origTable.getValueAt(0, 0).toString());
                    maxX = Double.valueOf(origTable.getValueAt(origTable.getRowCount() - 1, 0).toString());
                    minY = 0;
                    maxY = 0;
                }
                else if (origTable.getRowCount() == 2 && origTable.getColumnCount() >= 2 && !origTable.getValueAt(0, 0).toString().equals("")) {
                    tableType = TableType.Table2DHorizontal;
                    intYCheckBox.setEnabled(false);
                    minX = Double.valueOf(origTable.getValueAt(0, 0).toString());
                    maxX = Double.valueOf(origTable.getValueAt(0, origTable.getColumnCount() - 1).toString());
                    minY = 0;
                    maxY = 0;
                }
                else {
                    tableType = TableType.Table3D;
                    minX = Double.valueOf(origTable.getValueAt(0, 1).toString());
                    maxX = Double.valueOf(origTable.getValueAt(0, origTable.getColumnCount() - 1).toString());
                    minY = Double.valueOf(origTable.getValueAt(1, 0).toString());
                    maxY = Double.valueOf(origTable.getValueAt(origTable.getRowCount() - 1, 0).toString());
                }
                interpTypeCheckBox.removeAllItems();
                interpTypeCheckBox.setModel(new DefaultComboBoxModel<String>(getInterpolationMethods()));
            }
        });
        excelAdapterList.get(0).addTable(origTable, false, true, false, false, true, true, false, true, true);
        newTable = createDataTable(panel, newTableName, 2, false);
        newTable.getDefaultEditor(String.class).addCellEditorListener(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent e) {
                TableCellEditor ed = (TableCellEditor)e.getSource();
                String value = ed.getCellEditorValue().toString();
                if (value.isEmpty())
                    return;
                if (!Pattern.matches(Utils.fpRegex, value))
                    JOptionPane.showMessageDialog(null, "Invalid cell value: " + value + ". Value must be numeric", "Error", JOptionPane.ERROR_MESSAGE);
                else {
                    int row = newTable.getSelectedRow();
                    int col = newTable.getSelectedColumn();
                    if (row != -1 && col != -1 && (row == 0 || col == 0))
                        calculate(row, col);
                }
            }
            @Override
            public void editingCanceled(ChangeEvent e) {
            }
        });
    }
    
    protected String[] getInterpolationMethods() {
        if (tableType == TableType.Table3D)
            return new String [] { Utils.InterpolatorType.Bilinear.name(), Utils.InterpolatorType.BicubicSpline.name() };
        return new String [] { Utils.InterpolatorType.Linear.name(), Utils.InterpolatorType.CubicSpline.name() };
    }

    protected boolean isNonOriginalTableCellEditable(int row, int column) {
        if (tableType == TableType.Table3D && ((row > 0 && column > 0) || (row == 0 && column == 0)))
            return false;
        if (tableType == TableType.Table2DHorizontal && row > 0)
            return false;
        if (tableType == TableType.Table2DVertical && column > 0)
            return false;
        return true;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE USAGE TAB
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected String usage() {
        ResourceBundle bundle;
        bundle = ResourceBundle.getBundle("com.vgi.mafscaling.tablerescale");
        return bundle.getString("usage"); 
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // WORK FUNCTIONS
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void calculate(int row, int col) {
        double[] xvals = null;
        double[] yvals = null;
        double[][] zvals = null;
        int i, j;
        try {
            interpTypeCheckBox.setEnabled(false);
            plot3d.removeAllPlots();
            double value = Double.valueOf(newTable.getValueAt(row, col).toString());
            Utils.InterpolatorType type = Utils.InterpolatorType.valueOf((String)interpTypeCheckBox.getSelectedItem());
            if (tableType == TableType.Table3D) {
                if (row == 0 && (value < minX || value > maxX)) {
                    xvals = new double[origTable.getColumnCount() - 1];
                    for (i = 1; i < origTable.getColumnCount(); ++i)
                        xvals[i - 1] = Double.valueOf(origTable.getValueAt(row, i).toString());
                    yvals = new double[origTable.getColumnCount() - 1];
                    type = (type == Utils.InterpolatorType.Bilinear ? Utils.InterpolatorType.Linear : Utils.InterpolatorType.CubicSpline);
                    for (row = 1; row < origTable.getRowCount(); ++row) {
                        for (i = 1; i < origTable.getColumnCount(); ++i)
                            yvals[i - 1] = Double.valueOf(origTable.getValueAt(row, i).toString());
                        double y = Utils.interpolate(xvals, yvals, value, type);
                        newTable.setValueAt(y, row, col);
                    }
                }
                else if (col == 0 && (value < minY || value > maxY)) {
                    xvals = new double[origTable.getRowCount() - 1];
                    for (i = 1; i < origTable.getRowCount(); ++i)
                        xvals[i - 1] = Double.valueOf(origTable.getValueAt(i, col).toString());
                    yvals = new double[origTable.getRowCount() - 1];
                    type = (type == Utils.InterpolatorType.Bilinear ? Utils.InterpolatorType.Linear : Utils.InterpolatorType.CubicSpline);
                    for (col = 1; col < origTable.getColumnCount(); ++col) {
                        for (i = 1; i < origTable.getRowCount(); ++i)
                            yvals[i - 1] = Double.valueOf(origTable.getValueAt(i, col).toString());
                        double x = Utils.interpolate(xvals, yvals, value, type);
                        newTable.setValueAt(x, row, col);
                    }
                }
                else {
                    xvals = new double[origTable.getColumnCount() - 1];
                    yvals = new double[origTable.getRowCount() - 1];
                    for (i = 1; i < origTable.getColumnCount(); ++i)
                        xvals[i - 1] = Double.valueOf(origTable.getValueAt(0, i).toString());
                    for (i = 1; i < origTable.getRowCount(); ++i)
                        yvals[i - 1] = Double.valueOf(origTable.getValueAt(i, 0).toString());
                    zvals = new double[origTable.getColumnCount() - 1][origTable.getRowCount() - 1];
                    for (i = 1; i < origTable.getColumnCount(); ++i) {
                        for (j = 1; j < origTable.getRowCount(); ++j)
                            zvals[i - 1][j - 1] = Double.valueOf(origTable.getValueAt(j, i).toString());
                    }
                    if (row == 0) {
                        double yi;
                        for (i = 1; i < origTable.getRowCount(); ++i) {
                            yi = Double.valueOf(origTable.getValueAt(i, 0).toString());
                            double zi = Utils.interpolate3d(xvals, yvals, zvals, value, yi, type);
                            newTable.setValueAt(zi, i, col);
                        }
                    }
                    else if (col == 0) {
                        double xi;
                        for (i = 1; i < origTable.getColumnCount(); ++i) {
                            xi = Double.valueOf(origTable.getValueAt(0, i).toString());
                            double zi = Utils.interpolate3d(xvals, yvals, zvals, xi, value, type);
                            newTable.setValueAt(zi, row, i);
                        }
                    }
                }
                Utils.colorTable(newTable);
                xvals = new double[newTable.getColumnCount() - 1];
                yvals = new double[newTable.getRowCount() - 1];
                for (i = 1; i < newTable.getColumnCount(); ++i)
                    xvals[i - 1] = Double.valueOf(newTable.getValueAt(0, i).toString());
                for (i = 1; i < newTable.getRowCount(); ++i)
                    yvals[i - 1] = Double.valueOf(newTable.getValueAt(i, 0).toString());
                zvals = Utils.doubleZArray(newTable, xvals, yvals);
                Color[][] tableColors = Utils.generateTableColorMatrix(newTable, 1, 1, yvals.length + 1, xvals.length + 1);
                Color[][] colors = new Color[yvals.length][xvals.length];
                for (j = 1; j < tableColors.length; ++j)
                    for (i = 1; i < tableColors[j].length; ++i)
                        colors[j - 1][i - 1] = tableColors[j][i];
                plot3d.addGridPlot("Rescaled", colors, xvals, yvals, zvals);
            }
            else {
                if (tableType == TableType.Table2DVertical) {
                    col = 1;
                    xvals = new double[origTable.getRowCount()];
                    yvals = new double[origTable.getRowCount()];
                    for (i = 0; i < origTable.getRowCount(); ++i) {
                        xvals[i] = Double.valueOf(origTable.getValueAt(i, 0).toString());
                        yvals[i] = Double.valueOf(origTable.getValueAt(i, 1).toString());
                    }
                }
                else if (tableType == TableType.Table2DHorizontal) {
                    row = 1;
                    xvals = new double[origTable.getColumnCount()];
                    yvals = new double[origTable.getColumnCount()];
                    for (i = 0; i < origTable.getColumnCount(); ++i) {
                        xvals[i] = Double.valueOf(origTable.getValueAt(0, i).toString());
                        yvals[i] = Double.valueOf(origTable.getValueAt(1, i).toString());
                    }
                }
                double y = Utils.interpolate(xvals, yvals, value, type);
                newTable.setValueAt(y, row, col);
                yvals[(tableType == TableType.Table2DVertical ? row : col)] = y;
                Utils.colorTable(newTable);
                double[][] xyzArray = new double[xvals.length][3];
                Color[] colors = new Color[xvals.length];
                BgColorFormatRenderer renderer = (BgColorFormatRenderer)newTable.getDefaultRenderer(Object.class);
                for (j = 0; j < xvals.length; ++j) {
                    for (i = 0; i < xvals.length; ++i) {
                        colors[i] = (tableType == TableType.Table2DVertical ? renderer.getColorAt(i, 1) : renderer.getColorAt(1, i));
                        xyzArray[i][0] = xvals[i];
                        xyzArray[i][1] = xvals[i];
                        xyzArray[i][2] = yvals[i];
                    }
                }
                plot3d.addLinePlot("Rescaled", colors, xyzArray);
            }
        }
        catch (Exception e) {
            logger.error(e);
            JOptionPane.showMessageDialog(null, "Error interpolating new values: " + e, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JSpinner addSpinnerFilter(JPanel panel, int column, int value, int min, int max, int step) {
        final JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        JFormattedTextField fmtText = (JFormattedTextField)spinner.getEditor().getComponent(0);
        DefaultFormatter formatter = (DefaultFormatter) fmtText.getFormatter();
        formatter.setCommitsOnValidEdit(true);
        spinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int value = (Integer)spinner.getValue();
                decimalPts = decimalPts.substring(0, 2);
                for (int i = 0; i < value; ++i)
                    decimalPts += "0";
                setFormatMatrix();
            }
        });
        GridBagConstraints gbc_check = new GridBagConstraints();
        gbc_check.insets = new Insets(2, 2, 2, 0);
        gbc_check.anchor = GridBagConstraints.WEST;
        gbc_check.gridx = column;
        gbc_check.gridy = 0;
        panel.add(spinner, gbc_check);
        return spinner;
    }
    
    private void setFormatMatrix() {
        formatMatrix[0][1] = (intXCheckBox.isSelected() ? new DecimalFormat("#") : new DecimalFormat(decimalPts));
        formatMatrix[1][0] = (intYCheckBox.isSelected() ? new DecimalFormat("#") : new DecimalFormat(decimalPts));
        formatMatrix[1][1] = (intVCheckBox.isSelected() ? new DecimalFormat("#") : new DecimalFormat(decimalPts));
        if (tableType == TableType.Table2DVertical) {
            formatMatrix[0][0] = formatMatrix[1][0];
            formatMatrix[0][1] = formatMatrix[1][1];
        }
        else if (tableType == TableType.Table2DHorizontal) {
            formatMatrix[0][0] = formatMatrix[0][1];
            formatMatrix[1][0] = formatMatrix[1][1];
        }
        formatTable(origTable);
        formatTable(newTable);
        fireStateChanged();
    }
    
    private void copyOriginalTable() {
        if (!Utils.isTableEmpty(origTable)) {
            for (int i = 0; i < origTable.getRowCount(); ++i) {
                for (int j = 0; j < origTable.getColumnCount(); ++j)
                    newTable.setValueAt(origTable.getValueAt(i, j), i, j);
            }
            validateTable(newTable);
        }
    }
    
    protected void clearRunTables() {
        clearRunTable(newTable);
        copyOriginalTable();
        plot3d.removeAllPlots();
        interpTypeCheckBox.setEnabled(true);
    }
    
    protected void clearTables() {
        super.clearTables();
        intXCheckBox.setEnabled(true);
        intYCheckBox.setEnabled(true);
        intXCheckBox.setSelected(false);
        intYCheckBox.setSelected(false);
        intVCheckBox.setSelected(false);
        interpTypeCheckBox.setEnabled(true);
        setFormatMatrix();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (checkActionPerformed(e))
            return;
        if ("chgfmt".equals(e.getActionCommand()))
            setFormatMatrix();
    }

    @Override
    protected void formatTable(JTable table) {
        NumberFormatRenderer renderer = (NumberFormatRenderer)table.getDefaultRenderer(Object.class);
        renderer.setFormats(formatMatrix);
    }

    @Override
    protected void createLogDataTable(JPanel panel, String[] columns) {
    }
    
    @Override
    protected void clearLogDataTables() {
    }

    @Override
    protected void createGraphTab() {
    }

    @Override
    protected void loadLogFile() {
    }

    @Override
    protected boolean processLog() {
        return false;
    }

    @Override
    protected boolean displayData() {
        return false;
    }

}

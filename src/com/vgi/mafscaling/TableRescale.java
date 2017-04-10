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
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.TreeMap;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIDefaults;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import javax.swing.text.DefaultFormatter;
import org.apache.log4j.Logger;
import org.math.plot.Plot3DPanel;

public class TableRescale extends ACompCalc {
    private static final long serialVersionUID = 7955359935085174590L;
    private static final Logger logger = Logger.getLogger(TableRescale.class);
    
    protected enum TableType {
        Table3D,
        Table2DVertical,
        Table2DHorizontal
    }

    private TableType tableType = TableType.Table3D;
    private JCheckBox extrCheckBox = null;
    private JCheckBox intXCheckBox = null;
    private JCheckBox intYCheckBox = null;
    private JCheckBox intVCheckBox = null;
    private JButton undoButton = null;
    private JButton redoButton = null;
    private ImageIcon undoIcon = null;
    private ImageIcon redoIcon = null;
    private Plot3DPanel newPlot3d = null;
    private JComboBox<String> interpTypeCheckBox = null;
    private TreeMap<Long, TreeMap<Long, Double>> workdata = null;
    private Stack<ArrayList<ArrayList<Object>>> undoStack = null;
    private Stack<ArrayList<ArrayList<Object>>> redoStack = null;
    private UIDefaults zeroInsets = new UIDefaults();
    private String decimalPts = "0.00";
    private Format[][] formatMatrix = { { new DecimalFormat(decimalPts), new DecimalFormat(decimalPts) }, { new DecimalFormat(decimalPts), new DecimalFormat(decimalPts) } };
    private double minX = 0;
    private double maxX = 0;
    private int precision = 100000;

    public TableRescale(int tabPlacement) {
        super(tabPlacement);
        origTableName = "Original Table";
        newTableName = "Rescaled Table";
        x3dAxisName = "X";
        y3dAxisName = "Y";
        z3dAxisName = "Z";
        undoIcon = new ImageIcon(getClass().getResource("/undo.png"));
        redoIcon = new ImageIcon(getClass().getResource("/redo.png"));
        zeroInsets.put("Button.contentMargins", insets0);
        initialize(new String[] {});
        addBase3dPanel();
        extrCheckBox.setSelected(true);
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
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0, 0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        gbl_cntlPanel.rowWeights = new double[]{0, 0};
        cntlPanel.setLayout(gbl_cntlPanel);

        addButton(cntlPanel, 0, "Clear", "clearall", GridBagConstraints.WEST);
        addButton(cntlPanel, 1, "Reset Rescaled", "clearlog", GridBagConstraints.WEST);
        interpTypeCheckBox = addComboBox(cntlPanel, 2, getInterpolationMethods());
        extrCheckBox = addCheckBox(cntlPanel, 3, "Extrapolate", null);
        addLabel(cntlPanel, 4, "Precision: ");
        addSpinnerFilter(cntlPanel, 5, 2, 1, 4, 1);
        addLabel(cntlPanel, 6, " ");
        intXCheckBox = addCheckBox(cntlPanel, 7, "Integer X-Axis", "chgfmt");
        intYCheckBox = addCheckBox(cntlPanel, 8, "Integer Y-Axis", "chgfmt");
        intVCheckBox = addCheckBox(cntlPanel, 9, "Integer Values", "chgfmt");
        undoButton = addImageButton(cntlPanel, 10, "undo", undoIcon);
        redoButton = addImageButton(cntlPanel, 11, "redo", redoIcon);
        addLabel(cntlPanel, 12, " ");
    }
    
    protected void createDataTables(JPanel panel) {
        dataScrollPane.setVisible(false);
        origTable = createDataTable(panel, origTableName, 0, true);
        excelAdapterList.set(0, new ExcelAdapter() {
            protected void onPaste(JTable table, boolean extendRows, boolean extendCols) {
                if (!validatePasteData(table))
                    return;
                super.onPaste(table, extendRows, extendCols);
                zeroOutBlankCells();
                validateTable(table);
                clearRunTables();
                initTableType();
                initializeRescale();
                add3DPlot(origTable, "Original");
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
    
    private boolean validatePasteData(JTable table) {
        if (table.getSelectedRows() == null || table.getSelectedRows().length == 0 ||
            table.getSelectedColumns() == null || table.getSelectedColumns().length == 0)
            return false;
        if (table.getSelectedRows()[0] != 0 || table.getSelectedColumns()[0] != 0) {
            JOptionPane.showMessageDialog(null, "Please paste copied table into the first cell", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void initTableType() {
        if (origTable.getColumnCount() == 2 && origTable.getRowCount() >= 2 && !origTable.getValueAt(0, 0).toString().equals("")) {
            tableType = TableType.Table2DVertical;
            intXCheckBox.setEnabled(false);
            minX = Double.valueOf(origTable.getValueAt(0, 0).toString());
            maxX = Double.valueOf(origTable.getValueAt(origTable.getRowCount() - 1, 0).toString());
        }
        else if (origTable.getRowCount() == 2 && origTable.getColumnCount() >= 2 && !origTable.getValueAt(0, 0).toString().equals("")) {
            tableType = TableType.Table2DHorizontal;
            intYCheckBox.setEnabled(false);
            minX = Double.valueOf(origTable.getValueAt(0, 0).toString());
            maxX = Double.valueOf(origTable.getValueAt(0, origTable.getColumnCount() - 1).toString());
        }
        else {
            tableType = TableType.Table3D;
            minX = Double.valueOf(origTable.getValueAt(0, 1).toString());
            maxX = Double.valueOf(origTable.getValueAt(0, origTable.getColumnCount() - 1).toString());
        }
    }
 
    private void initializeRescale() {
        if (Utils.isTableEmpty(origTable))
            return;
        if (tableType == TableType.Table3D) {
            workdata = new TreeMap<Long, TreeMap<Long, Double>>();
            undoStack = new Stack<ArrayList<ArrayList<Object>>>();
            redoStack = new Stack<ArrayList<ArrayList<Object>>>();
            undoButton.setEnabled(false);
            redoButton.setEnabled(false);
            long x, y;
            double z;
            TreeMap<Long, Double> col;
            for (int i = 1; i < origTable.getColumnCount(); ++i) {
                x = Math.round(Double.valueOf(origTable.getValueAt(0, i).toString()) * precision);
                col = new TreeMap<Long, Double>();
                workdata.put(x, col);
                for (int j = 1; j < origTable.getRowCount(); ++j) {
                    y = Math.round(Double.valueOf(origTable.getValueAt(j, 0).toString()) * precision);
                    z = Double.valueOf(origTable.getValueAt(j, i).toString());
                    col.put(y, z);
                }
            }
        }
        else
            workdata = null;
    }
    
    private String[] getInterpolationMethods() {
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
    // CREATE CHART TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    protected void addBase3dPanel() {
        GridBagLayout gbl_plotPanel = (GridBagLayout)plotPanel.getLayout();
        gbl_plotPanel.columnWidths = new int[] {0, 0};
        gbl_plotPanel.columnWeights = new double[]{1.0, 1.0};
                
        newPlot3d = new Plot3DPanel("SOUTH") {
            private static final long serialVersionUID = 7914951068593204419L;
            public void addPlotToolBar(String location) {
                super.addPlotToolBar(location);
                super.plotToolBar.remove(7);
                super.plotToolBar.remove(5);
                super.plotToolBar.remove(4);
            }            
        };
        newPlot3d.setAutoBounds();
        newPlot3d.setAutoscrolls(true);
        newPlot3d.setEditable(false);
        newPlot3d.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        newPlot3d.setForeground(Color.BLACK);
        newPlot3d.getAxis(0).setColor(Color.BLACK);
        newPlot3d.getAxis(1).setColor(Color.BLACK);
        newPlot3d.getAxis(2).setColor(Color.BLACK);
        newPlot3d.setAxisLabel(0, x3dAxisName);
        newPlot3d.setAxisLabel(1, y3dAxisName);
        newPlot3d.setAxisLabel(2, z3dAxisName);
        
        GridBagConstraints gbl_chartPanel = new GridBagConstraints();
        gbl_chartPanel.anchor = GridBagConstraints.CENTER;
        gbl_chartPanel.insets = insets3;
        gbl_chartPanel.fill = GridBagConstraints.BOTH;
        gbl_chartPanel.gridx = 0;
        gbl_chartPanel.gridy = 0;
        plotPanel.add(plot3d, gbl_chartPanel);
        gbl_chartPanel.gridx = 1;
        plotPanel.add(newPlot3d, gbl_chartPanel);
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
    
    @SuppressWarnings("unchecked")
    private void calculate(int row, int col) {
        double[] xvals = null;
        double[] yvals = null;
        double[][] zvals = null;
        int i, j;
        try {
            enableCalculateControls(false);
            pushUndoTable();
            // start processing the change
            Utils.InterpolatorType type = Utils.InterpolatorType.valueOf((String)interpTypeCheckBox.getSelectedItem());
            if (tableType == TableType.Table3D) {
                TreeMap<Long, Double> column = workdata.get(workdata.firstKey());
                long minX = workdata.firstKey();
                long maxX = workdata.lastKey();
                long minY = column.firstKey();
                long maxY = column.lastKey();
                long value = Math.round(Double.valueOf(newTable.getValueAt(row, col).toString()) * precision);
                // new X-Axis value
                if (row == 0 && (value < minX || value > maxX)) {
                    TreeMap<Long, Double> newColumn;
                    if (extrCheckBox.isSelected()) {
                        newColumn = (TreeMap<Long, Double>)column.clone();
                        Utils.InterpolatorType t = (type == Utils.InterpolatorType.Bilinear ? Utils.InterpolatorType.Linear : Utils.InterpolatorType.CubicSpline);
                        xvals = new double[workdata.size()];
                        yvals = new double[workdata.size()];
                        i = 0;
                        for (long k : workdata.keySet())
                            xvals[i++] = k;
                        for (long k : column.keySet()) {
                            i = 0;
                            for (TreeMap<Long, Double> c : workdata.values())
                                yvals[i++] = c.get(k);
                            double y = Utils.interpolate(xvals, yvals, value, t);
                            newColumn.put(k, y);
                        }
                    }
                    else {
                        long x = (value < minX ? minX : maxX);
                        newColumn = (TreeMap<Long, Double>) workdata.get(x).clone();
                    }
                    workdata.put(value, newColumn);
                }
                // new Y-Axis value
                else if (col == 0 && (value < minY || value > maxY)) {
                    if (extrCheckBox.isSelected()) {
                        Utils.InterpolatorType t = (type == Utils.InterpolatorType.Bilinear ? Utils.InterpolatorType.Linear : Utils.InterpolatorType.CubicSpline);
                        xvals = new double[column.size()];
                        yvals = new double[column.size()];
                        Long[] oxvals = column.keySet().toArray(new Long[column.size()]);
                        for (TreeMap<Long, Double> c : workdata.values()) {
                            i = 0;
                            for (long k : oxvals) {
                                xvals[i] = k;
                                yvals[i++] = c.get(k);
                            }
                            double y = Utils.interpolate(xvals, yvals, value, t);
                            c.put(value, y);
                        }
                    }
                    else {
                        long y = (value < minY ? minY : maxY);
                        for (TreeMap<Long, Double> c : workdata.values())
                            c.put(value, c.get(y));
                    }
                }

                xvals = new double[workdata.size()];
                yvals = new double[column.size()];
                zvals = new double[workdata.size()][column.size()];
                i = 0;
                for (long k : workdata.keySet())
                    xvals[i++] = k;
                i = 0;
                for (long k : column.keySet())
                    yvals[i++] = k;
                for (i = 0; i < xvals.length; ++i) {
                    for (j = 0; j < yvals.length; ++j)
                        zvals[i][j] = workdata.get((long)xvals[i]).get((long)yvals[j]);
                }
                if (row == 0) {
                    double yi;
                    for (i = 1; i < newTable.getRowCount(); ++i) {
                        yi = Math.round(Double.valueOf(newTable.getValueAt(i, 0).toString()) * precision);
                        double zi = Utils.interpolate3d(xvals, yvals, zvals, value, yi, type);
                        newTable.setValueAt(zi, i, col);
                    }
                }
                else if (col == 0) {
                    double xi;
                    for (i = 1; i < newTable.getColumnCount(); ++i) {
                        xi = Math.round(Double.valueOf(newTable.getValueAt(0, i).toString()) * precision);
                        double zi = Utils.interpolate3d(xvals, yvals, zvals, xi, value, type);
                        newTable.setValueAt(zi, row, i);
                    }
                }
            }
            else {
                double value = Double.valueOf(newTable.getValueAt(row, col).toString());
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
                double y;
                if (!extrCheckBox.isSelected() && (value < minX || value > maxX))
                    y = (value < minX ? yvals[0] : yvals[yvals.length - 1]);
                else
                    y = Utils.interpolate(xvals, yvals, value, type);
                newTable.setValueAt(y, row, col);
            }
            Utils.colorTable(newTable);
            add3DPlot(newTable, "Rescaled");
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
    
    private JButton addImageButton(JPanel panel, int column, String action, ImageIcon icon) {
        JButton button = new JButton(icon);
        button.putClientProperty("Nimbus.Overrides", zeroInsets);
        button.setMargin(insets0);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setActionCommand(action);
        button.addActionListener(this);
        button.setEnabled(false);
        GridBagConstraints gbc_button = new GridBagConstraints();
        gbc_button.insets = insets0;
        gbc_button.anchor = GridBagConstraints.WEST;
        gbc_button.gridx = column;
        gbc_button.gridy = 0;
        panel.add(button, gbc_button);
        return button;
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
    
    private void zeroOutBlankCells() {
        if (!Utils.isTableEmpty(origTable)) {
            // Check last column with value
            int col = 0;
            for (int i = 0; i < origTable.getColumnCount(); ++i) {
                if (!origTable.getValueAt(0, i).toString().isEmpty())
                    col = i;
            }
            int row = 0;
            for (int i = 0; i < origTable.getRowCount(); ++i) {
                if (!origTable.getValueAt(i, 0).toString().isEmpty())
                    row = i;
            }
            // 3D table
            if (col > 1 && row > 1 && origTable.getValueAt(0, 0).toString().isEmpty()) {
                for (int i = 0; i <= col; ++i) {
                    for (int j = 0; j <= row; ++j) {
                        if (origTable.getValueAt(j, i).toString().isEmpty() && (i != 0 || j != 0))
                            origTable.setValueAt("0", j, i);
                    }
                }
            }
            // 2D horizontal
            else if (col > 1) {
                for (int i = 0; i <= col; ++i) {
                    if (origTable.getValueAt(0, i).toString().isEmpty())
                        origTable.setValueAt("0", 0, i);
                    if (origTable.getValueAt(1, i).toString().isEmpty())
                        origTable.setValueAt("0", 1, i);
                }
            }
            // 2D horizontal
            else if (row > 1) {
                for (int i = 0; i <= row; ++i) {
                    if (origTable.getValueAt(i, 0).toString().isEmpty())
                        origTable.setValueAt("0", i, 0);
                    if (origTable.getValueAt(i, 1).toString().isEmpty())
                        origTable.setValueAt("0", i, 1);
                }
            }
        }
    }

    private void add3DPlot(JTable table, String name) {
        int i;
        double[] xvals = new double[table.getColumnCount() - 1];
        double[] yvals = new double[table.getRowCount() - 1];
        if (tableType == TableType.Table3D) {
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
            if (table == newTable)
                newPlot3d.addGridPlot(name, colors, xvals, yvals, zvals);
            else
                plot3d.addGridPlot(name, colors, xvals, yvals, zvals);
        }
        else {
            if (tableType == TableType.Table2DVertical) {
                xvals = new double[table.getRowCount()];
                yvals = new double[table.getRowCount()];
                for (i = 0; i < table.getRowCount(); ++i) {
                    xvals[i] = Double.valueOf(table.getValueAt(i, 0).toString());
                    yvals[i] = Double.valueOf(table.getValueAt(i, 1).toString());
                }
            }
            else if (tableType == TableType.Table2DHorizontal) {
                xvals = new double[table.getColumnCount()];
                yvals = new double[table.getColumnCount()];
                for (i = 0; i < table.getColumnCount(); ++i) {
                    xvals[i] = Double.valueOf(table.getValueAt(0, i).toString());
                    yvals[i] = Double.valueOf(table.getValueAt(1, i).toString());
                }
            }
            double[][] xyzArray = new double[xvals.length][3];
            Color[] colors = new Color[xvals.length];
            BgColorFormatRenderer renderer = (BgColorFormatRenderer)table.getDefaultRenderer(Object.class);
            for (int j = 0; j < xvals.length; ++j) {
                for (i = 0; i < xvals.length; ++i) {
                    colors[i] = (tableType == TableType.Table2DVertical ? renderer.getColorAt(i, 1) : renderer.getColorAt(1, i));
                    xyzArray[i][0] = xvals[i];
                    xyzArray[i][1] = xvals[i];
                    xyzArray[i][2] = yvals[i];
                }
            }
            if (table == newTable)
                newPlot3d.addLinePlot(name, colors, xyzArray);
            else
                plot3d.addLinePlot(name, colors, xyzArray);
        }
    }
    
    protected void pushUndoTable() {
        ArrayList<ArrayList<Object>> table = saveUndoRedoTable(undoStack, undoButton);
        int row = newTable.getSelectedRow();
        int col = newTable.getSelectedColumn();
        if (undoStack.size() > 0) {
            ArrayList<ArrayList<Object>> prevTable = undoStack.peek();
            table.get(col).set(row, prevTable.get(col).get(row));
        }
        else
            table.get(col).set(row, origTable.getValueAt(row, col));
    }
    
    protected ArrayList<ArrayList<Object>> saveUndoRedoTable(Stack<ArrayList<ArrayList<Object>>> stack, JButton button) {
        ArrayList<ArrayList<Object>> table = new ArrayList<ArrayList<Object>>();
        for (int i = 0; i < newTable.getColumnCount(); ++i) {
            ArrayList<Object> column = new ArrayList<Object>();
            for (int j = 0; j < newTable.getRowCount(); ++j)
                column.add(newTable.getValueAt(j, i));
            table.add(column);
        }
        stack.push(table);
        button.setEnabled(true);
        return table;
    }
    
    private void onUndoRedo(char type) {
        Stack<ArrayList<ArrayList<Object>>> fromStack = redoStack;
        Stack<ArrayList<ArrayList<Object>>> toStack = undoStack;
        JButton fromButton = redoButton;
        JButton toButton = undoButton;
        if (type == 'U') {
            fromStack = undoStack;
            toStack = redoStack;
            fromButton = undoButton;
            toButton = redoButton;
        }
        if (fromStack.size() > 0) {
            ArrayList<ArrayList<Object>> table = fromStack.pop();
            if (fromStack.size() == 0)
                fromButton.setEnabled(false);
            saveUndoRedoTable(toStack, toButton);
            for (int i = 0; i < newTable.getColumnCount(); ++i) {
                ArrayList<Object> column = table.get(i);
                for (int j = 0; j < newTable.getRowCount(); ++j)
                    newTable.setValueAt(column.get(j), j, i);
            }
            Utils.colorTable(newTable);
        }
    }
    
    protected void enableCalculateControls(boolean flag) {
        newPlot3d.removeAllPlots();
        interpTypeCheckBox.setEnabled(flag);
        extrCheckBox.setEnabled(flag);
    }
    
    protected void clearRunTables() {
        clearRunTable(newTable);
        copyOriginalTable();
        enableCalculateControls(true);
        initializeRescale();
    }
    
    protected void clearTables() {
        super.clearTables();
        plot3d.removeAllPlots();
        enableCalculateControls(true);
        intXCheckBox.setEnabled(true);
        intYCheckBox.setEnabled(true);
        intXCheckBox.setSelected(false);
        intYCheckBox.setSelected(false);
        intVCheckBox.setSelected(false);
        setFormatMatrix();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (checkActionPerformed(e))
            return;
        if ("chgfmt".equals(e.getActionCommand()))
            setFormatMatrix();
        else if ("undo".equals(e.getActionCommand()))
            onUndoRedo('U');
        else if ("redo".equals(e.getActionCommand()))
            onUndoRedo('R');
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

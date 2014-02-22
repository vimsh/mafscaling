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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public final class Utils {
    /**
     * Shortened numeric validation regex
     */
    public final static String fpRegex = ("[\\x00-\\x20]*[+-]?(((\\p{Digit}+)(\\.)?((\\p{Digit}+)?))|(\\.((\\p{Digit}+))))[\\x00-\\x20]*");

    //////////////////////////////////////////////////////////////////////////////
    // COLORING METHODS
    //////////////////////////////////////////////////////////////////////////////
    
    /**
     * Method returns unique gradient colors between RED(0xFF0000) and BLUE(0x0000FF)
     * @param numColors, number of unique gradient colors between RED and BLUE
     * @return array of unique gradient colors
     */
    public static Color[] getColorArray(int numColors) {
        return getColorArray(new Color(0xFF0000), new Color(0x0000FF), numColors);
    }
    
    /**
     * Method returns unique gradient colors between specified colors
     * @param begin, start color
     * @param end, end color
     * @param numColors, number of gradient unique colors between start and end colors
     * @return array of unique gradient colors
     */
    public static Color[] getColorArray(Color begin, Color end, int numColors) {
        Color[] gradient = new Color[numColors];
        float[] hsv1 = Color.RGBtoHSB(begin.getRed(), begin.getGreen(), begin.getBlue(), null);  
        float[] hsv2 = Color.RGBtoHSB(end.getRed(), end.getGreen(), end.getBlue(), null);  
        int a1 = begin.getAlpha() ;  
        float h1 = hsv1[0] ;  
        float s1 = hsv1[1];  
        float v1 = hsv1[2];  
        float da = end.getAlpha()- a1;  
        float dh = hsv2[0] - h1;  
        float ds = hsv2[1]- s1;  
        float dv = hsv2[2] - v1;
        for (int i = 0; i < gradient.length; ++i) {  
            float rel = i / (float)(gradient.length - 1);
            int rgb = Color.HSBtoRGB(h1 + dh * rel, s1 + ds * rel, v1 + dv * rel) ;  
            rgb +=(((int)(a1 + da * rel)) << 24) ;   
            gradient[i] = new Color(rgb);
        }
        return gradient;
    }

    //////////////////////////////////////////////////////////////////////////////
    // TABLE METHODS
    //////////////////////////////////////////////////////////////////////////////
    
    /**
     * Methods return table gradient colors matrix
     * @param table, table to get matrix for
     * @param startRow, row to begin coloring from
     * @param startColumn, column to begin coloring from
     */
    public static Color[][] generateTableColorMatrix(JTable table, int startRow, int startColumn) {
        Color[][] colorMatrix = null;
        TreeSet<Double> uniqueValues = new TreeSet<Double>();
        int i, j;
        Object cellValue;
        String value;
        for (i = startRow; i < table.getRowCount(); ++i) {
            for (j = startColumn; j < table.getColumnCount(); ++j) {
                cellValue = table.getValueAt(i, j);
                if (cellValue != null) {
                    value = cellValue.toString();
                    if (!value.isEmpty() && Pattern.matches(Utils.fpRegex, value))
                        uniqueValues.add(Double.valueOf(value));
                }
            }
        }
        if (uniqueValues.size() > 0) {
            List<Color> colors = Arrays.asList(Utils.getColorArray(uniqueValues.size()));
            Collections.reverse(colors);
            colorMatrix = new Color[table.getRowCount()][table.getColumnCount()];
            for (i = startRow; i < table.getRowCount(); ++i) {
                for (j = startColumn; j < table.getColumnCount(); ++j) {
                    cellValue = table.getValueAt(i, j);
                    if (cellValue != null) {
                        value = cellValue.toString();
                        if (!value.isEmpty() && Pattern.matches(Utils.fpRegex, value))
                            colorMatrix[i][j] = colors.get(uniqueValues.headSet(Double.valueOf(value)).size());
                    }
                }
            }
        }
        return colorMatrix;
    }
    
    /**
     * Method sets gradient background color for unique values assuming the first row and first column are headers.
     * The table must have default renderer set as BgColorFormatRenderer
     * @param table
     */
    public static void colorTable(JTable table) {
        Color[][] colorMatrix = Utils.generateTableColorMatrix(table, 1, 1);
        if (colorMatrix != null) {
            for (int i = 0; i < colorMatrix.length; ++i)
                colorMatrix[i][0] = Color.LIGHT_GRAY;
            for (int i = 0; i < colorMatrix[0].length; ++i)
                colorMatrix[0][i] = Color.LIGHT_GRAY;
            BgColorFormatRenderer renderer = (BgColorFormatRenderer)table.getDefaultRenderer(Object.class);
            if (renderer != null)
                renderer.setColors(colorMatrix);
        }
        ((DefaultTableModel)table.getModel()).fireTableDataChanged();
    }
    
    /**
     * Method clears the table cells and sets default value - an empty string
     * @param table
     */
    public static void clearTable(JTable table) {
        if (table.getCellEditor() != null)
            table.getCellEditor().stopCellEditing();
        for (int i = 0; i < table.getColumnCount(); ++i) {
            for (int j = 0; j < table.getRowCount(); ++j)
                table.setValueAt("", j, i);
        }
        TableCellRenderer renderer = table.getDefaultRenderer(Object.class);
        if (renderer != null && renderer instanceof BgColorFormatRenderer)
        	((BgColorFormatRenderer)renderer).setColors(null);
        ((DefaultTableModel)table.getModel()).fireTableDataChanged();
    }

    /**
     * Method initializes the table cells with default value - an empty string, and sets cell width to specified width
     * @param table
     */
    public static void initializeTable(JTable table, int columnWidth) {
        table.setDefaultRenderer(Object.class, new NumberFormatRenderer());
        for (int i = 0; i < table.getColumnCount(); ++i) {
            table.getColumnModel().getColumn(i).setMinWidth(columnWidth);
            table.getColumnModel().getColumn(i).setMaxWidth(columnWidth);
            for (int j = 0; j < table.getRowCount(); ++j)
                table.setValueAt("", j, i);
        }
    }
    
    /**
     * Method checks if the column count is greater or equal to specified count, if not - it add a column
     * @param count, number of columns to check for
     * @param table
     */
    public static void ensureColumnCount(int count, JTable table) {
        DefaultTableModel model = (DefaultTableModel)table.getModel();
        while (count > table.getColumnCount())
            model.addColumn("");
    }
    
    /**
     * Method checks if the row count is greater or equal to specified count, if not - it add a row
     * @param count, number of rows to check for
     * @param table
     */
    public static void ensureRowCount(int count, JTable table) {
        DefaultTableModel model = (DefaultTableModel)table.getModel();
        while (count > table.getRowCount())
            model.addRow(new Object[table.getColumnCount()]);
    }
    
    /**
     * Method removes row from table
     * @param index of the row to be removed
     * @param table, table from which to remove the row
     */
    public static void removeRow(int index, JTable table) {
        if (index < table.getRowCount())
            ((DefaultTableModel)table.getModel()).removeRow(index);
    }
    
    /**
     * Method removes column from table
     * @param index of the column to be removed
     * @param table, table from which to remove the column
     */
    public static void removeColumn(int index, JTable table) {
        if (index < table.getColumnCount()) {
            int j;
            Object[][] data = new String[table.getRowCount()][table.getColumnCount() - 1];
            int[] minwidth = new int[table.getColumnCount() - 1];
            int[] maxwidth = new int[table.getColumnCount() - 1];
            int[] prefwidth = new int[table.getColumnCount() - 1];
            for (int i = 0; i < table.getRowCount(); ++i) {
                for (j = 0; j < table.getColumnCount(); ++j) {
                    if (j == index)
                        continue;
                    if (j < index) {
                        data[i][j] = table.getValueAt(i, j);
                        if (i == 0) {
                            minwidth[j] = table.getColumnModel().getColumn(j).getMinWidth();
                            maxwidth[j] = table.getColumnModel().getColumn(j).getMaxWidth();
                            prefwidth[j] = table.getColumnModel().getColumn(j).getPreferredWidth();
                        }
                    }
                    else {
                        data[i][j - 1] = table.getValueAt(i, j);
                        if (i == 0) {
                            minwidth[j - 1] = table.getColumnModel().getColumn(j).getMinWidth();
                            maxwidth[j - 1] = table.getColumnModel().getColumn(j).getMaxWidth();
                            prefwidth[j - 1] = table.getColumnModel().getColumn(j).getPreferredWidth();
                        }
                    }
                    
                }
            }
            ((DefaultTableModel)table.getModel()).setDataVector(data, new String[table.getColumnCount() - 1]);
            for (j = 0; j < table.getColumnCount(); ++j) {
                table.getColumnModel().getColumn(j).setMinWidth(minwidth[j]);
                table.getColumnModel().getColumn(j).setMaxWidth(maxwidth[j]);
                table.getColumnModel().getColumn(j).setPreferredWidth(prefwidth[j]);
            }
        }
    }
    
    /**
     * Method checks that value in a table cell is numeric
     * @param value to check 
     * @param row index
     * @param col index
     * @param tableName
     * @return
     */
    public static boolean validateDouble(String value, int row, int col, String tableName) {
        if (!Pattern.matches(Utils.fpRegex, value)) {
            JOptionPane.showMessageDialog(null, "Invalid data in table " + tableName + " - value is not a number in cell (" + row + ", " + col + "): " + value, "Invalid Data", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    //////////////////////////////////////////////////////////////////////////////
    // MATH METHODS
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Method returns index of value closest to the specified in the given array of values
     * @param val, target value
     * @param list, array of values
     * @return closet value to the target value.
     * Returns lower index if distance is the same between lower and higher indexed values.
     */
    public static int closestValueIndex(double val, ArrayList<Double> list) {
        int index = Collections.binarySearch(list, val);
        if (index < 0) {
            int idxPrev = Math.max(0, -index - 2);
            int idxNext = Math.min(list.size() - 1, -index - 1);
            return val - list.get(idxPrev) <= list.get(idxNext) - val ? idxPrev : idxNext;
        }
        return index;
    }

}

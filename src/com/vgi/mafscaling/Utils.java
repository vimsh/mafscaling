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
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public final class Utils {
    /**
     * Shortened numeric validation regex
     */
    public final static String fpRegex = "[\\x00-\\x20]*[+-]?(NaN|Infinity|((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*";
    //		("[\\x00-\\x20]*[+-]?(((\\p{Digit}+)(\\.)?((\\p{Digit}+)?))|(\\.((\\p{Digit}+))))[\\x00-\\x20]*");
    public final static String tmRegex = ".*\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}.*";
    public final static String onOffRegex = "(?i)^\\s*(ON|OFF|OPENED|OPEN|CLOSED|CLOSE)\\s*$";
    public final static Pattern offPattern = Pattern.compile("^\\s*(OFF|CLOSED|CLOSE)\\s*$", Pattern.CASE_INSENSITIVE);
    public final static Pattern onPattern = Pattern.compile("^\\s*(ON|OPENED|OPEN)\\s*$", Pattern.CASE_INSENSITIVE);
    public final static Color ZeroColor = new Color(255, 255, 255, 0);
    
    private static long baseTime = 0;

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
    	Color[] gradient;
    	if (numColors == 1)
    		gradient = new Color[3];
    	else
    		gradient = new Color[numColors];
        float[] hsv1 = Color.RGBtoHSB(begin.getRed(), begin.getGreen(), begin.getBlue(), null);  
        float[] hsv2 = Color.RGBtoHSB(end.getRed(), end.getGreen(), end.getBlue(), null);  
        int a1 = begin.getAlpha();
        float h1 = hsv1[0] ;  
        float s1 = hsv1[1];  
        float v1 = hsv1[2];  
        float da = end.getAlpha()- a1;  
        float dh = hsv2[0] - h1;  
        float ds = hsv2[1]- s1;  
        float dv = hsv2[2] - v1;
        for (int i = 0; i < gradient.length; ++i) {
            float rel = i / (float)(gradient.length - 1);
            int rgb = Color.HSBtoRGB(h1 + dh * rel, s1 + ds * rel, v1 + dv * rel);  
            rgb +=(((int)(a1 + da * rel)) << 24);
            gradient[i] = new Color(rgb);
        }
    	if (numColors == 1)
    		gradient = new Color[] { gradient[1] };
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
    	return generateTableColorMatrix(table, startRow, startColumn, table.getRowCount(), table.getColumnCount());
    }
    
    /**
     * Methods return table gradient colors matrix
     * @param table, table to get matrix for
     * @param startRow, row to begin coloring from
     * @param startColumn, column to begin coloring from
     * @param endRow, row to finish coloring on
     * @param endColumn, column to finish coloring on
     * @return
     */
    public static Color[][] generateTableColorMatrix(JTable table, int startRow, int startColumn, int endRow, int endColumn) {
        Color[][] colorMatrix = null;
        TreeSet<Double> uniqueValues = new TreeSet<Double>();
        int i, j;
        String value;
        for (i = startRow; i < endRow; ++i) {
            for (j = startColumn; j < endColumn; ++j) {
            	value = table.getValueAt(i, j).toString();
                if (!value.isEmpty() && Pattern.matches(Utils.fpRegex, value))
                    uniqueValues.add(Double.valueOf(value));
            }
        }
        if (uniqueValues.size() > 0) {
            List<Color> colors = Arrays.asList(Utils.getColorArray(uniqueValues.size()));
            Collections.reverse(colors);
            colorMatrix = new Color[endRow][endColumn];
            for (i = startRow; i < endRow; ++i) {
                for (j = startColumn; j < endColumn; ++j) {
                	value = table.getValueAt(i, j).toString();
                    if (!value.isEmpty() && Pattern.matches(Utils.fpRegex, value))
                        colorMatrix[i][j] = colors.get(uniqueValues.headSet(Double.valueOf(value)).size());
        			else
        				colorMatrix[i][j] = ZeroColor;
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
        if (colorMatrix == null)
        	colorMatrix = new Color[table.getRowCount()][table.getColumnCount()];
        for (int i = 0; i < colorMatrix.length; ++i)
            colorMatrix[i][0] = Color.LIGHT_GRAY;
        for (int i = 0; i < colorMatrix[0].length; ++i)
            colorMatrix[0][i] = Color.LIGHT_GRAY;
        BgColorFormatRenderer renderer = (BgColorFormatRenderer)table.getDefaultRenderer(Object.class);
        if (renderer != null)
            renderer.setColors(colorMatrix);
        ((DefaultTableModel)table.getModel()).fireTableDataChanged();
    }
    
    /**
     * Method clears the table cells color
     * @param table
     */
    public static void clearTableColors(JTable table) {
        TableCellRenderer renderer = table.getDefaultRenderer(Object.class);
        if (renderer != null && renderer instanceof BgColorFormatRenderer)
        	((BgColorFormatRenderer)renderer).setColors(null);
        ((DefaultTableModel)table.getModel()).fireTableDataChanged();
    }
    
    /**
     * Method sets gradient background color for first row and first column assuming those are headers.
     * The table must have default renderer set as BgColorFormatRenderer
     * @param table
     */
    public static void colorTableHeaders(JTable table) {
        Color[][] colorMatrix = new Color[table.getRowCount()][table.getColumnCount()];
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
     * Method sets columns width to the widest value
     * @param table
     * @param column
     * @param margin
     */
    public static void adjustColumnSizes(JTable table, int column, int margin) {
    	DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
    	TableColumn col = colModel.getColumn(column);
    	int width;
    	TableCellRenderer renderer = col.getHeaderRenderer();
    	if (renderer == null)
    		renderer = table.getTableHeader().getDefaultRenderer();
    	Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);
    	width = comp.getPreferredSize().width;
    	for (int r = 0; r < table.getRowCount(); ++r) {
    		renderer = table.getCellRenderer(r, column);
    		comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, column), false, false, r, column);
    		int currentWidth = comp.getPreferredSize().width;
    		width = Math.max(width, currentWidth);
    	}
    	width += 2 * margin;
    	col.setPreferredWidth(width);
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
        clearTableColors(table);
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
    	if (count <= table.getColumnCount())
    		return;
        int[] minwidth = new int[count];
        int[] maxwidth = new int[count];
        int[] prefwidth = new int[count];
        int i, j;
        for (i = 0; i < table.getColumnCount(); ++i) {
	        minwidth[i] = table.getColumnModel().getColumn(i).getMinWidth();
	        maxwidth[i] = table.getColumnModel().getColumn(i).getMaxWidth();
	        prefwidth[i] = table.getColumnModel().getColumn(i).getPreferredWidth();
        }
        DefaultTableModel model = (DefaultTableModel)table.getModel();
        for (i = table.getColumnCount(); i < count; ++i) {
            model.addColumn("");
            minwidth[i] = minwidth[i - 1];
            maxwidth[i] = maxwidth[i - 1];
            prefwidth[i] = prefwidth[i - 1];
            for (j = 0; j < table.getRowCount(); ++j)
            	table.setValueAt("", j, i);
        }
        for (i = 0; i < count; ++i) {
            table.getColumnModel().getColumn(i).setMinWidth(minwidth[i]);
            table.getColumnModel().getColumn(i).setMaxWidth(maxwidth[i]);
            table.getColumnModel().getColumn(i).setPreferredWidth(prefwidth[i]);
        }
    }
    
    /**
     * Method checks if the row count is greater or equal to specified count, if not - it add a row
     * @param count, number of rows to check for
     * @param table
     */
    public static void ensureRowCount(int count, JTable table) {
        DefaultTableModel model = (DefaultTableModel)table.getModel();
        int i, j;
        for (i = table.getRowCount(); i < count; ++i) {
            model.addRow(new Object[table.getColumnCount()]);
            for (j = 0; j < table.getColumnCount(); ++j)
            	table.setValueAt("", i, j);
        }
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
            Object[][] data = new Object[table.getRowCount()][table.getColumnCount() - 1];
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
    
    /**
     * Method check is the table is empty
     * @param table
     * @return
     */
    public static boolean isTableEmpty(JTable table) {
        boolean isEmpty = true;
        for (int i = 0; i < table.getRowCount() && isEmpty; ++i) {
            for (int j = 0; j < table.getColumnCount() && isEmpty; ++j) {
                if (!table.getValueAt(i, j).toString().isEmpty())
                	isEmpty = false;
            }
        }
        return isEmpty;
    }
    
    /**
     * Methods copies table to another table
     * @param fromTable
     * @param toTable
     */
    public static void copyTable(JTable fromTable, JTable toTable) {
    	ensureColumnCount(fromTable.getColumnCount(), toTable);
    	ensureRowCount(fromTable.getRowCount(), toTable);
        for (int i = 0; i < fromTable.getRowCount(); ++i) {
            for (int j = 0; j < fromTable.getColumnCount(); ++j)
            	toTable.setValueAt(fromTable.getValueAt(i, j), i, j);
        }
    }

    /**
     * Method calculates plotting z[][] based on provided x and y arrays and related table
     * @param x is a column table header
     * @param y is a row table header
     * @return double array
     */
    public static double[][] doubleZArray(JTable dataTable, double[] x, double[] y) {
    	double[][] z = new double[y.length][x.length];
    	for (int i = 0; i < x.length; ++i) {
    		for (int j = 0; j < y.length; ++j) {
    			if (!dataTable.getValueAt(j + 1, i + 1).toString().isEmpty())
    				z[j][i] = Double.valueOf(dataTable.getValueAt(j + 1, i + 1).toString());
    		}
    	}
    	return z;
    }

    /**
     * Method copies Color[][] array from input data table for z-axis based on provided x and y arrays
     * @param x
     * @param y
     * @return
     */
    public static Color[][] doubleColorArray(JTable dataTable, double[] x, double[] y) {
        BgColorFormatRenderer renderer = (BgColorFormatRenderer)dataTable.getDefaultRenderer(Object.class);
    	Color[][] z = new Color[y.length][x.length];
    	for (int i = 0; i < x.length; ++i) {
    		for (int j = 0; j < y.length; ++j) {
    			if (!dataTable.getValueAt(j + 1, i + 1).toString().isEmpty())
    				z[j][i] = renderer.getColorAt(j + 1, i + 1);
    			else
    				z[j][i] = ZeroColor;
    		}
    	}
    	return z;
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

    /**
     * Method returns a linearly interpolated value
     * @param x is X value you want to interpolate at
     * @param x1 is X value for previous point
     * @param x2 is X value for following point
     * @param y1 is Y value for previous point
     * @param y2 is Y value for following point
     * @return Y interpolated value
     */
    public static double linearInterpolation(double x, double x1, double x2, double y1, double y2) {
        return (x1 == x2) ? 0.0 : (y1 + (x - x1) * (y2 - y1) / (x2 - x1));
    }

    /**
     * Method returns a value interpolated from 3D table
     * @param x is X value you want to interpolate at
     * @param y is Y value you want to interpolate at
     * @param x0 is nearest x-axis value less than X
     * @param x1 is nearest x-axis value greater than X
     * @param y0 is nearest y-axis value less than Y
     * @param y1 is nearest y-axis value greater than Y
     * @param x0y0 is the table value at x0 and y0
     * @param x0y1 is the table value at x0 and y1
     * @param x1y0 is the table value at x1 and y0
     * @param x1y1 is the table value at x1 and y1
     * @return
     */
    public static double table3DInterpolation(double x, double y, double x0, double x1, double y0, double y1, double x0y0, double x0y1, double x1y0, double x1y1) {
    	double t1, t2;
        if (y1 == y0) {
            t1 = x0y0;
            t2 = x1y0;
        }
        else {
            t1 = (y - y0) * (x0y1 - x0y0) / (y1 - y0) + x0y0;
            t2 = (y - y0) * (x1y1 - x1y0) / (y1 - y0) + x1y0;
        }
        if (x1 == x0)
        	return t1;
        else
        	return (x - x0) * (t2 - t1) / (x1 - x0) + t1;
    }
    
    /**
     * Method rounds input number by a specific step
     * @param input
     * @param step
     * @return
     */
    public static double round(double input, double step) {
    	return ((Math.round(input / step)) * step);
    }

    /**
     * Calculate mean of the array
     * @param data
     * @return
     */
    public static double mean(List<Double> data) {
    	double val = 0;
    	for (int i = 0; i < data.size(); ++i)
			val += data.get(i);
		return val / data.size();
    }

    /**
     * Calculate median of the array
     * @param data
     * @return
     */
    public static double median(List<Double> data) {
    	double val = 0;
		Collections.sort(data);
		int mid = data.size() / 2;
		if (data.size() % 2 == 1)
			val = data.get(mid);
		else
			val = (data.get(mid - 1) + data.get(mid)) / 2;
		return val;
    }

    /**
     * Calculate mode of the array
     * @param data
     * @return
     */
    public static double mode(List<Double> data) {
    	ArrayList<Double> modes = new ArrayList<Double>();
    	HashMap<Double, Integer> countMap = new HashMap<Double, Integer>();
	    int max = -1;
	    Integer count;
	    for (Double n : data) {
	    	count = countMap.get(n);
	    	if (count == null)
	    		count = 0;
	    	count += 1;
	    	countMap.put(n, count);
	        if (count > max)
	            max = count;
	    }
	    for (Map.Entry<Double, Integer> entry : countMap.entrySet()) {
	        if (entry.getValue() == max)
	            modes.add(entry.getKey());
	    }
	    Collections.sort(modes);
	    return modes.get(modes.size() / 2);
    }

    /**
     * Calculate range of the array
     * @param data
     * @return
     */
    public static double range(List<Double> data) {
		return Collections.max(data) - Collections.min(data);
    }

    /**
     * Calculate variance of the array
     * @param data
     * @return
     */
    public static double variance(List<Double> data) {
    	double mean = mean(data);
    	double sum = 0;
    	double diff = 0;
    	for (Double d : data) {
    		diff = d - mean;
    		sum += (diff * diff);
    	}
		return sum / data.size();
    }

    /**
     * Calculate standard deviation of the array
     * @param data
     * @return
     */
    public static double standardDeviation(List<Double> data) {
		return Math.sqrt(variance(data));
    }

    /**
     * Method returns random number within min/max range, inclusive
     * @param min
     * @param max
     * @return
     */
    public static int getRandomInRange(int min, int max) {
    	return (min + (int)(Math.random() * ((max - min) + 1)));
    }
    
    /**
     * Methods compare two double values with 5 decimal points precision
     * @param x
     * @param y
     * @return true if x is equal to y with up to 5 decimal points, false otherwise
     */
    public static boolean equals(double x, double y) {
        return (Math.abs(x - y) < 0.00001);
    }

    /**
     * Method calculates commanded afr from POL table
     * @param rpm
     * @param col
     * @param minWotEnrichment
     * @param polfTable
     * @return commanded afr
     */
    public static double calculateCommandedAfr(double rpm, double col, double minWotEnrichment, PrimaryOpenLoopFuelingTable polfTable) {
        double rpmLow = 0;
        double rpmHigh = 0;
        double colLow = 0;
        double colHigh = 0;
        double timingLowLow = 0;
        double timingLowHigh = 0;
        double timingHighLow = 0;
        double timingHighHigh = 0;
        int rpmRowLow = 1;
        int rpmRowHigh = 1;
        int colLowIdx = 1;
        int colHighIdx = 1;
        int index = 1;
        double value;

        // Get values for RPM
        for (index = 1; index < polfTable.getRowCount(); ++index) {
            value = Double.valueOf(polfTable.getValueAt(index, 0).toString());
            if (rpm < value) {
                rpmHigh = value;
                rpmRowHigh = index;
                break;
            }
            else if (rpm == value) {
                rpmLow = rpmHigh = value;
                rpmRowLow = rpmRowHigh = index;
                break;
            }
            else {
                rpmLow = value;
                rpmRowLow = index;
            }
        }
        if (index == polfTable.getRowCount()) {
            rpmHigh = 10000;
            rpmRowHigh = index - 1;
        }
        // Get values for y
        for (index = 1; index < polfTable.getColumnCount(); ++index) {
            value = Double.valueOf(polfTable.getValueAt(0, index).toString());
            if (col < value) {
                colHigh = value;
                colHighIdx = index;
                break;
            }
            else if (rpm == value) {
                colLow = colHigh = value;
                colLowIdx = colHighIdx = index;
                break;
            }
            else {
                colLow = value;
                colLowIdx = index;
            }
        }
        if (index == polfTable.getColumnCount()) {
            colHigh = 10000;
            colHighIdx = index - 1;
        }
        timingLowLow = Double.valueOf(polfTable.getValueAt(rpmRowLow, colLowIdx).toString());
        if (timingLowLow > minWotEnrichment)
        	timingLowLow = minWotEnrichment;
        timingLowHigh = Double.valueOf(polfTable.getValueAt(rpmRowLow, colHighIdx).toString());
        if (timingLowHigh > minWotEnrichment)
        	timingLowHigh = minWotEnrichment;
        timingHighLow = Double.valueOf(polfTable.getValueAt(rpmRowHigh, colLowIdx).toString());
        if (timingHighLow > minWotEnrichment)
        	timingHighLow = minWotEnrichment;
        timingHighHigh = Double.valueOf(polfTable.getValueAt(rpmRowHigh, colHighIdx).toString());
        if (timingHighHigh > minWotEnrichment)
        	timingHighHigh = minWotEnrichment;

        return Utils.table3DInterpolation(col, rpm, colLow, colHigh, rpmLow, rpmHigh, timingLowLow, timingHighLow, timingLowHigh, timingHighHigh);
    }
    
    /**
     * Method resets baseTime variable used for conversion from absolute time in hh:mm:ss.sss string format to relative time in msec
     * @param s
     */
    public static void resetBaseTime(String s) {
    	if (s.indexOf(':') > 0 && s.indexOf('.') > 0) {
    		int tmZero = '0' * 11;
    		baseTime = ((s.charAt(0) * 10 + s.charAt(1) - tmZero) * 3600 + (s.charAt(3) * 10 + s.charAt(4) - tmZero) * 60 + s.charAt(6) * 10 + s.charAt(7) - tmZero) * 1000;
        }
    	else
    		baseTime = 0;
    }

    /**
     * Method used for parsing various time column formats and returns time in msec as long
     * @param s time as string from log file
     * @return time in msec as long
     */
    public static long parseTime(String s) {
    	if (s.indexOf(':') > 0 && s.indexOf('.') > 0) {
    		int tmZero = '0' * 11;
    		int msZero = '0' * 111;
    		return ((s.charAt(0) * 10 + s.charAt(1) - tmZero) * 3600 +
    				(s.charAt(3) * 10 + s.charAt(4) - tmZero) * 60 +
    				 s.charAt(6) * 10 + s.charAt(7) - tmZero) * 1000 +
    				 (s.charAt(9) * 100 + s.charAt(10) * 10 + s.charAt(11) - msZero) - baseTime;
        }
    	if (s.indexOf('.') > 0)
    		return (long)(Double.valueOf(s) * 1000);
    	return Long.valueOf(s);
    }

    /**
     * Method used for parsing log values besides time
     * This method should only be used where all columns of the log file(s) being loaded, eg LogStats, LogView 
     * @param s value as string from log file
     * @return value as double
     */
    public static double parseValue(String s) {
    	if (Utils.onPattern.matcher(s).find())
			return 1.0;
    	if (Utils.offPattern.matcher(s).find())
			return 0.0;
    	return Double.valueOf(s);
    }
}

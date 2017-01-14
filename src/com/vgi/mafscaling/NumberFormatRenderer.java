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

import java.awt.Component;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.regex.Pattern;
import javax.swing.JTable;
import javax.swing.SwingConstants;


class NumberFormatRenderer extends BgColorFormatRenderer {
    private static final long serialVersionUID = 4722830336189723801L;
    private Format[][] formats = null;
    private DecimalFormat formatter = new DecimalFormat("0.00");
    
    /**
     * Default constructor, sets cells alignement to right
     */
    public NumberFormatRenderer() {
        setHorizontalAlignment(SwingConstants.RIGHT);
    }

    /**
     * Method sets cells format matrix
     * @param formatMatrix
     */
    public void setFormats(Format[][] formatMatrix) {
        formats = formatMatrix;
    }

    /**
     * Method returns cells format matrix
     * @return format matrix
     */
    public Format[][] getFormats() {
        return formats;
    }
    
    /**
     * Method sets background color for a specific cell
     * @param color, background color
     * @param row, cell row index
     * @param column, cell column index
     */
    public void setFormatAt(Format format, int row, int column) {
        if (formats != null && row < formats.length && column < formats[0].length)
            formats[row][column] = format;
    }
    
    /**
     * Method sets numeric formatter
     * @param format
     */
    public void setFormatter(DecimalFormat format) {
        formatter = format;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null)
            value = "";
        else if (formats != null) {
            if (Pattern.matches(Utils.fpRegex, value.toString())) {
                int frow = row;
                if (frow >= formats.length)
                    frow = formats.length - 1;
                int fcol = column;
                if (fcol >= formats[frow].length)
                    fcol = formats[frow].length - 1;
                value = formats[frow][fcol].format(Double.valueOf(value.toString()));
            }
        }
        else if (Pattern.matches(Utils.fpRegex, value.toString()))
            value = formatter.format(Double.valueOf(value.toString()));
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column );
    } 
}
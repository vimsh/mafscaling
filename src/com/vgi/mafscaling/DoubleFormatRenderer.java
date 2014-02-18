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
import java.util.regex.Pattern;
import javax.swing.JTable;
import javax.swing.SwingConstants;


class DoubleFormatRenderer extends BgColorFormatRenderer {
    private static final long serialVersionUID = 4722830336189723801L;
    private DecimalFormat formatter = new DecimalFormat("0.00");

    /**
     * Default constructor, sets cells alignement to right
     */
    public DoubleFormatRenderer() {
        setHorizontalAlignment(SwingConstants.RIGHT);
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
        else if (Pattern.matches(Utils.fpRegex, value.toString()))
            value = formatter.format(Double.valueOf(value.toString()));
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column );
    } 
}
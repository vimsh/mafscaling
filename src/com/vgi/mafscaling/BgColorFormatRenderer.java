package com.vgi.mafscaling;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class BgColorFormatRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = -2283383526525346419L;
    Color bgColor = getBackground();
    private Color[][] colors = null;

    /**
     * Method sets background color matrix for cells
     * @param colorMatrix
     */
    public void setColors(Color[][] colorMatrix) {
        colors = colorMatrix;
    }

    /**
     * Method returns background color matrix for cells
     * @return colorMatrix
     */
    public Color[][] getColors() {
        return colors;
    }
    
    /**
     * Method sets background color for a specific cell
     * @param color, background color
     * @param row, cell row index
     * @param column, cell column index
     */
    public void setColorAt(Color color, int row, int column) {
        if (colors != null && row < colors.length && column < colors[0].length)
            colors[row][column] = color;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (colors != null) {
            if (row < colors.length && column < colors[0].length)
                setBackground(colors[row][column]);
            else
                setBackground(bgColor);
        }
        else
            setBackground(bgColor);
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column );
    }

}

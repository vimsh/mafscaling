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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;

public class ExcelAdapter implements ActionListener {
    private static final Logger logger = Logger.getLogger(ExcelAdapter.class);
    
    class JTableHolder {
        private JTable table;
        private boolean extendRows = false;
        private boolean extendCols = false;
        JTableHolder(JTable table, boolean extendRows, boolean extendCols) {
            this.table = table;
            this.extendRows = extendRows;
            this.extendCols = extendCols;
        }
        JTable getTable() {
            return this.table;
        }
        boolean getExtendRows() {
            return this.extendRows;
        }
        boolean getExtendCols() {
            return this.extendCols;
        }
    }
    class ExcelAdapterMenuItem extends JMenuItem {
        private static final long serialVersionUID = -1678268797976684465L;
        private JTableHolder tableHolder;
        ExcelAdapterMenuItem(JTableHolder tableHolder, String name, ActionListener listener) {
            super(name);
            this.tableHolder = tableHolder;
            addActionListener(listener);
        }
        public JTableHolder getTableHolder() {
            return this.tableHolder;
        }
    }
    private static final String eol = System.getProperty("line.separator");
    private String rowstring;
    private String value;
    private StringSelection stsel;
    private ArrayList<JTableHolder> tableHolders = new ArrayList<JTableHolder>();
    private ArrayList<ExcelAdapterMenuItem> menuItems = new ArrayList<ExcelAdapterMenuItem>();
    private Clipboard system = Toolkit.getDefaultToolkit().getSystemClipboard();

    /**
     * @param myJTable is table on which it enables Copy-Paste and acts as a Clipboard listener.
     * @param disableCopy, if true no copying will be avalable
     * @param disablePaste, if true no pasting will be avalable
     * @param disableClear, if true no clearing will be avalable
     */
    public void addTable(JTable table, boolean disableCopy, boolean disableCut, boolean disablePaste, boolean disableClear) {
        addTable(table, disableCopy, disableCut, disablePaste, disableClear, false, false);
    }

    /**
     * @param myJTable is table on which it enables Copy-Paste and acts as a Clipboard listener.
     * @param extendRows, if true will automatically add rows to the table to be able to paste all data
     * @param extendCols, if true will automatically add columns to the table to be able to paste all data
     */
    public void addTable(JTable table, boolean extendRows, boolean extendCols) {
        addTable(table, false, false, false, false, extendRows, extendCols);
    }

    /**
     * @param myJTable is table on which it enables Copy-Paste and acts as a Clipboard listener.
     * @param disableCopy, if true no copying will be avalable
     * @param disableCut, if true no cutting will be avalable
     * @param disablePaste, if true no pasting will be avalable
     * @param disableClear, if true no clearing will be avalable
     * @param extendRows, if true will automatically add rows to the table to be able to paste all data
     * @param extendCols, if true will automatically add columns to the table to be able to paste all data
     */
    private void addTable(JTable table, boolean disableCopy, boolean disableCut, boolean disablePaste, boolean disableClear, boolean extendRows, boolean extendCols) {
    	addTable(table, disableCopy, disableCut, disablePaste, disableClear, true, true, true, extendRows, extendCols);
    }

    /**
     * @param myJTable is table on which it enables Copy-Paste and acts as a Clipboard listener.
     * @param disableCopy, if true no copying will be avalable
     * @param disableCut, if true no cutting will be avalable
     * @param disablePaste, if true no pasting will be avalable
     * @param disableClear, if true no clearing will be avalable
     * @param disableVertCopy, if true no vertical copy will be avalable
     * @param disableVertPaste, if true no vertical pasting will be avalable
     * @param extendRows, if true will automatically add rows to the table to be able to paste all data
     * @param extendCols, if true will automatically add columns to the table to be able to paste all data
     */
    public void addTable(JTable table, boolean disableCopy, boolean disableCut, boolean disablePaste, boolean disableClear, boolean disableVertCopy, boolean disableVertPaste, boolean disableRRCopy, boolean extendRows, boolean extendCols) {
        JTableHolder tableHolder = new JTableHolder(table, extendRows, extendCols);
        tableHolders.add(tableHolder);
        JPopupMenu popup = new JPopupMenu();
        if (!disableCopy) {
            ExcelAdapterMenuItem copyMenuItem = new ExcelAdapterMenuItem(tableHolder, "Copy", this);
            copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false));
            menuItems.add(copyMenuItem);
            popup.add(copyMenuItem);
            table.registerKeyboardAction(this, "Copy", KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false), JComponent.WHEN_FOCUSED);
        }
        if (!disableVertCopy) {
            ExcelAdapterMenuItem copyMenuItem = new ExcelAdapterMenuItem(tableHolder, "Copy Vertical", this);
            copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK, false));
            menuItems.add(copyMenuItem);
            popup.add(copyMenuItem);
            table.registerKeyboardAction(this, "Copy Vertical", KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK, false), JComponent.WHEN_FOCUSED);
        }
        if (!disableRRCopy) {
            ExcelAdapterMenuItem copyMenuItem = new ExcelAdapterMenuItem(tableHolder, "Copy RomRaider", this);
            copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK, false));
            menuItems.add(copyMenuItem);
            popup.add(copyMenuItem);
            table.registerKeyboardAction(this, "Copy RomRaider", KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK, false), JComponent.WHEN_FOCUSED);
        }
        if (!disableCut) {
            ExcelAdapterMenuItem cutMenuItem = new ExcelAdapterMenuItem(tableHolder, "Cut", this);
            cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK, false));
            menuItems.add(cutMenuItem);
            popup.add(cutMenuItem);
            table.registerKeyboardAction(this, "Cut", KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK, false), JComponent.WHEN_FOCUSED);
        }
        if (!disablePaste) {
            ExcelAdapterMenuItem pasteMenuItem = new ExcelAdapterMenuItem(tableHolder, "Paste", this);
            pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false));
            menuItems.add(pasteMenuItem);
            popup.add(pasteMenuItem);
            table.registerKeyboardAction(this, "Paste", KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false), JComponent.WHEN_FOCUSED);
        }
        if (!disableVertPaste) {
            ExcelAdapterMenuItem pasteMenuItem = new ExcelAdapterMenuItem(tableHolder, "Paste Vertical", this);
            pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK, false));
            menuItems.add(pasteMenuItem);
            popup.add(pasteMenuItem);
            table.registerKeyboardAction(this, "Paste Vertical", KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK, false), JComponent.WHEN_FOCUSED);
        }
        if (!disableClear) {
            ExcelAdapterMenuItem clearMenuItem = new ExcelAdapterMenuItem(tableHolder, "Clear Selection", this);
            clearMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false));
            menuItems.add(clearMenuItem);
            popup.add(clearMenuItem);
            table.registerKeyboardAction(this, "Clear Selection", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false), JComponent.WHEN_FOCUSED);
        }
        ExcelAdapterMenuItem selectAllMenuItem = new ExcelAdapterMenuItem(tableHolder, "Select All", this);
        selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK, false));
        menuItems.add(selectAllMenuItem);
        popup.add(selectAllMenuItem);
        table.registerKeyboardAction(this, "Select All", KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK, false), JComponent.WHEN_FOCUSED);
        
        table.setComponentPopupMenu(popup);
    }
    
    public void removeTable(JTable table) {
        ArrayList<JTableHolder> removeTable = new ArrayList<JTableHolder>();
        ArrayList<ExcelAdapterMenuItem> removeMenu = new ArrayList<ExcelAdapterMenuItem>();
        for (int i = 0; i < tableHolders.size(); ++i) {
            if (tableHolders.get(i).getTable() == table) {
                removeTable.add(tableHolders.get(i));
                for (int j = 0; j < menuItems.size(); ++j) {
                    if (menuItems.get(j).getTableHolder() == tableHolders.get(i))
                        removeMenu.add(menuItems.get(j));
                }
            }    
        }
        for (JTableHolder th : removeTable)
            tableHolders.remove(th);
        for (ExcelAdapterMenuItem mi : removeMenu)
            menuItems.remove(mi);
    }
    
    /**
     * This method is activated on the Keystrokes we are listening to
     * in this implementation. Here it listens for Copy and Paste ActionCommands.
     * Selections comprising non-adjacent cells result in invalid selection and
     * then copy action cannot be performed.
     * Paste is done by aligning the upper left corner of the selection with the
     * 1st element in the current selection of the JTable.
     */
    public void actionPerformed(ActionEvent e) {
        JTableHolder tableHolder = null;
        if (e.getSource().getClass().getName().contains("JTable")) {
            for (int i = 0; i < tableHolders.size(); ++i) {
                tableHolder = tableHolders.get(i);
                if (((JTable)e.getSource()) == tableHolder.getTable())
                    break;
            }
            if (tableHolder == null)
                return;
        }
        else {
            ExcelAdapterMenuItem menuItem = null;
            for (int i = 0; i < menuItems.size(); ++i) {
                menuItem = menuItems.get(i);
                if (e.getSource() instanceof ExcelAdapterMenuItem && ((ExcelAdapterMenuItem)e.getSource()) == menuItem)
                    break;
            }
            if (menuItem == null)
                return;
            tableHolder = menuItem.getTableHolder();
        }

        if (e.getActionCommand().equals("Clear Selection"))
            onClearSelection(tableHolder.getTable());
        if (e.getActionCommand().equals("Select All"))
            onSelectAll(tableHolder.getTable());
        else if (e.getActionCommand().equals("Copy"))
            onCopy(tableHolder.getTable());
        else if (e.getActionCommand().equals("Copy Vertical"))
            onCopyVertical(tableHolder.getTable());
        else if (e.getActionCommand().equals("Copy RomRaider"))
            onCopyRR(tableHolder.getTable());
        else if (e.getActionCommand().equals("Cut")) {
            onCopy(tableHolder.getTable());
            onClearSelection(tableHolder.getTable());
        }
        else if (e.getActionCommand().equals("Paste"))
            onPaste(tableHolder.getTable(), tableHolder.getExtendRows(), tableHolder.getExtendCols());
        else if (e.getActionCommand().equals("Paste Vertical"))
            onPasteVertical(tableHolder.getTable(), tableHolder.getExtendRows(), tableHolder.getExtendCols());
    }
    
    private void onClearSelection(JTable table) {
        // Check to ensure we have selected only a contiguous block of cells
        int numcols = table.getSelectedColumnCount();
        int numrows = table.getSelectedRowCount();
        if (numcols == 0 || numrows == 0)
            return;
        int[] rowsselected = table.getSelectedRows();
        int[] colsselected = table.getSelectedColumns();
        if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0] &&
               numrows == rowsselected.length) &&
              (numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0] &&
               numcols == colsselected.length))) {
            JOptionPane.showMessageDialog(null, "Invalid Delete Selection", "Invalid Delete Selection", JOptionPane.ERROR_MESSAGE);
            return;
        }
        TableCellRenderer renderer = table.getDefaultRenderer(Object.class);
        BgColorFormatRenderer bgRenderer = null;
        if (renderer != null && renderer instanceof BgColorFormatRenderer)
        	bgRenderer = (BgColorFormatRenderer)renderer;
        for (int i = 0; i < numrows; ++i) {
            for (int j = 0; j < numcols; ++j) {
                table.setValueAt("", rowsselected[i], colsselected[j]);
                if (bgRenderer != null)
                	bgRenderer.setColorAt(Color.WHITE, rowsselected[i], colsselected[j]);
            }
        }
    }
    
    private void onSelectAll(JTable table) {
        table.selectAll();
    }
    
    private void onCopy(JTable table) {
        StringBuffer sbf = new StringBuffer();
        // Check to ensure we have selected only a contiguous block of cells
        int numcols = table.getSelectedColumnCount();
        int numrows = table.getSelectedRowCount();
        if (numcols == 0 || numrows == 0)
            return;
        int[] rowsselected = table.getSelectedRows();
        int[] colsselected = table.getSelectedColumns();
        if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0] &&
               numrows == rowsselected.length) &&
              (numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0] &&
               numcols == colsselected.length))) {
            JOptionPane.showMessageDialog(null, "Invalid Copy Selection", "Invalid Copy Selection", JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (int i = 0; i < numrows; ++i) {
            for (int j = 0; j < numcols; ++j) {
                sbf.append(table.getValueAt(rowsselected[i], colsselected[j]));
                if (j < numcols - 1)
                    sbf.append("\t");
            }
            sbf.append(eol);
        }
        stsel  = new StringSelection(sbf.toString());
        system = Toolkit.getDefaultToolkit().getSystemClipboard();
        system.setContents(stsel, stsel);
    }
    
    private void onCopyVertical(JTable table) {
        StringBuffer sbf = new StringBuffer();
        // Check to ensure we have selected only a contiguous block of cells
        int numcols = table.getSelectedColumnCount();
        int numrows = table.getSelectedRowCount();
        if (numcols == 0 || numrows == 0)
            return;
        int[] rowsselected = table.getSelectedRows();
        int[] colsselected = table.getSelectedColumns();
        if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0] &&
               numrows == rowsselected.length) &&
              (numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0] &&
               numcols == colsselected.length))) {
            JOptionPane.showMessageDialog(null, "Invalid Copy Selection", "Invalid Copy Selection", JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (int i = 0; i < numcols; ++i) {
        	for (int j = 0; j < numrows; ++j) {
                sbf.append(table.getValueAt(rowsselected[j], colsselected[i]));
                if (j < numrows - 1)
                	sbf.append("\t");
            }
            sbf.append(eol);
        }
        stsel  = new StringSelection(sbf.toString());
        system = Toolkit.getDefaultToolkit().getSystemClipboard();
        system.setContents(stsel, stsel);
    }
    
    private void onCopyRR(JTable table) {
        StringBuffer sbf = new StringBuffer("[Table2D]" + eol);
        // Check to ensure we have selected only a contiguous block of cells
        int numcols = table.getSelectedColumnCount();
        int numrows = table.getSelectedRowCount();
        if (numcols == 0 || numrows == 0)
            return;
        int[] rowsselected = table.getSelectedRows();
        int[] colsselected = table.getSelectedColumns();
        if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0] &&
               numrows == rowsselected.length) &&
              (numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0] &&
               numcols == colsselected.length))) {
            JOptionPane.showMessageDialog(null, "Invalid Copy Selection", "Invalid Copy Selection", JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (int i = 0; i < numrows; ++i) {
            for (int j = 0; j < numcols; ++j) {
                sbf.append(table.getValueAt(rowsselected[i], colsselected[j]));
                if (j < numcols - 1)
                    sbf.append("\t");
            }
            sbf.append(eol);
        }
        stsel  = new StringSelection(sbf.toString());
        system = Toolkit.getDefaultToolkit().getSystemClipboard();
        system.setContents(stsel, stsel);
    }
    
    private void onPaste(JTable table, boolean extendRows, boolean extendCols) {
        if (table.getSelectedRows() == null || table.getSelectedRows().length == 0 ||
            table.getSelectedColumns() == null || table.getSelectedColumns().length == 0)
            return;
        int startRow = (table.getSelectedRows())[0];
        int startCol = (table.getSelectedColumns())[0];
        try {
            String trstring = (String)(system.getContents(this).getTransferData(DataFlavor.stringFlavor));
            String[] lines = trstring.replaceFirst("\\[Table2D\\]\\r?\\n", "").split("\\r?\\n");
            int rowCount = lines.length;
            if (rowCount > 0) {
                // add extra rows to the table to accommodate for paste data
                if (extendRows && startRow + rowCount > table.getRowCount())
                	Utils.ensureRowCount(startRow + rowCount, table);
                // add extra columns to the table to accommodate for paste data
                rowstring = lines[0];
                String[] entries = rowstring.split("\t", -1);
                int colCount = entries.length;
                if (extendCols && startCol + colCount > table.getColumnCount())
                	Utils.ensureColumnCount(startCol + colCount, table);
                // populate cells with the data
                for (int i = 0; i < rowCount; ++i) {
                    if (i > 0)
                        rowstring = lines[i];
                    entries = rowstring.split("\t", -1);
                    for (int j = 0; j < entries.length; ++j) {
                        value = entries[j];
                        if (startRow + i < table.getRowCount() && startCol + j< table.getColumnCount())
                            table.setValueAt(value, startRow + i, startCol + j);
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
    }
    
    private void onPasteVertical(JTable table, boolean extendRows, boolean extendCols) {
        if (table.getSelectedRows() == null || table.getSelectedRows().length == 0 ||
            table.getSelectedColumns() == null || table.getSelectedColumns().length == 0)
            return;
        int startRow = (table.getSelectedRows())[0];
        int startCol = (table.getSelectedColumns())[0];
        try {
            String trstring = (String)(system.getContents(this).getTransferData(DataFlavor.stringFlavor));
            String[] lines = trstring.split("\\r?\\n");
            ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
            for (String colsArr : lines) {
            	String[] rowsArr = colsArr.split("\t", -1);
            	ArrayList<String> rows = new ArrayList<String>();
            	for (String row : rowsArr)
            		rows.add(row);
            	data.add(rows);
            }
            int rowCount = (data.size() > 0 ? data.get(0).size() : 0);
            if (rowCount > 0) {
                // add extra rows to the table to accommodate for paste data
                if (extendRows && startRow + rowCount > table.getRowCount())
                	Utils.ensureRowCount(startRow + rowCount, table);
                // add extra columns to the table to accommodate for paste data
                int colCount = data.size();
                if (extendCols && startCol + colCount > table.getColumnCount())
                	Utils.ensureColumnCount(startCol + colCount, table);
                // populate cells with the data
                for (int i = 0; i < rowCount; ++i) {
                    for (int j = 0; j < colCount; ++j) {
                        value = data.get(j).get(i);
                        if (startRow + i < table.getRowCount() && startCol + j< table.getColumnCount())
                            table.setValueAt(value, startRow + i, startCol + j);
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
    }
}

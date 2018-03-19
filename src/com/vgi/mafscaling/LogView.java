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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.StandardTickUnitSource;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.math.plot.Plot3DPanel;
import org.scijava.swing.checkboxtree.CheckBoxNodeData;
import org.scijava.swing.checkboxtree.CheckBoxNodeEditor;
import org.scijava.swing.checkboxtree.CheckBoxNodePanel;
import org.scijava.swing.checkboxtree.CheckBoxNodeRenderer;
import quick.dbtable.Column;
import quick.dbtable.DBTable;
import quick.dbtable.PrintProperties;
import quick.dbtable.Skin;
import quick.dbtable.Filter;

public class LogView extends FCTabbedPane implements ActionListener {
    private static final long serialVersionUID = -3803091816206090707L;
    private static final Logger logger = Logger.getLogger(LogView.class);
    private static final int tableControlPanelNumComponents = 7;
    private static final int tableRowTextFieldComponentIdx = 3;
    private static final int tableRowTextFieldSize = 5;
    private static final int iconWidthHeight = 20;
    private static final int iconFillPadding1 = 4;
    private static final int iconFillPadding2 = 5;
    private static final int maxNumPlots = 5;
    private static final Color chartColor = new Color(60, 60, 65);
    private static final Color chartBgColor = new Color(80, 80, 85);
    private static final String prototypeDisplayValue = "XXXXXXXXXXXXXXXXXXXXXXXXXXX";
    private static final String greater = ">";
    private static final String greaterEqual = ">=";
    private static final String less = "<";
    private static final String lessEqual = "<=";
    private static final String equal = "=";
    private static final String rpmAxisName = "Engine Speed (RPM)";
    private static final String timeAxisName = "Time (RPM aligned)";
    private static final String fileNameReplaceString = "\\<.*?\\>";
    private static final String pullIndexReplaceString = "Pull ";
    private static final String thrtlMatchString = ".*throttle.*";
    private static final String rpmMatchString = ".*rpm.*";
    private static final String engineSpeedMatchString = ".*eng.*speed.*";
    private static final String timeMatchString = "^.*time\\s*(\\(.*\\))?$";
    
    public class XYZ {
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            long temp;
            temp = Double.doubleToLongBits(x);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(y);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(z);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            XYZ other = (XYZ) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
                return false;
            if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
                return false;
            if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
                return false;
            return true;
        }
        private double x;
        private double y;
        private double z;
        XYZ(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        private LogView getOuterType() {
            return LogView.this;
        }
    }
    
    public class TableSkin extends Skin {
        private static final long serialVersionUID = 8263328522848779295L;
        Font headerFont = new Font("Arial", Font.PLAIN, 12);
        Font font = new Font("Arial", Font.PLAIN, 11);
        @SuppressWarnings("unchecked")
        public TableSkin() {
            put(Skin.HEADER_FONT,headerFont);
            put(Skin.TABLE_FONT,font);
            put(Skin.ROW_HEIGTH, new Integer(16));
            put(Skin.FOCUS_CELL_HIGHLIGHT_BORDER,new javax.swing.border.MatteBorder(2, 2, 2, 2, Color.BLACK));
          }
    }
    
    public class SortingPopUp extends JPopupMenu implements ActionListener {
        private static final long serialVersionUID = -8399244173709551368L;
        JMenuItem sortAscending;
        JMenuItem sortDescending;
        int columnIndex;
        public SortingPopUp(int column) {
            columnIndex = column;
            sortAscending = new JMenuItem("Sort Ascending");
            sortAscending.setActionCommand("sortascending");
            sortAscending.addActionListener(this);
            add(sortAscending);
            sortDescending = new JMenuItem("Sort Descending");
            sortDescending.setActionCommand("sortdescending");
            sortDescending.addActionListener(this);
            add(sortDescending);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if ("sortascending".equals(e.getActionCommand()))
                sortAscending(columnIndex);
            else if ("sortdescending".equals(e.getActionCommand()))
                sortDescending(columnIndex);
        }
    }
    
    public class DoubleComparator implements quick.dbtable.Comparator {
        public int compare(int column, Object currentData, Object nextData) {
            Double v1 = Double.valueOf(currentData.toString());
            Double v2 = Double.valueOf(nextData.toString());
            return Double.compare(v1, v2);
        }
    }
    
    public static class CompareFilter implements Filter {
        public enum Condition {
            NONE,
            GREATER,
            GREATER_EQUAL,
            EQUAL,
            LESS_EQUAL,
            LESS
        }
        private int colId = 0;
        private Condition condition = Condition.NONE;
        private String filterString = "0";
        private double filter = Double.NaN;
        public void setColumn(int id) {
            colId = id;
        }
        public void setCondition(Condition c) {
            condition = c;
        }
        public void setFilter(String f) {
            filterString = f;
            filter = Double.valueOf(filterString);
        }
        public int[] filter(TableModel tm) {
            if (Double.isNaN(filter) || Condition.NONE == condition)
                return new int[0];
            ArrayList<Integer> list = new ArrayList<Integer>();
            double value;
            int rounding = 0;
            int i = 0;
            if (filterString.indexOf('.') != -1) {
                filterString = filterString.substring(filterString.indexOf('.'));
                rounding = filterString.length() - 1;
            }
            for (i = 0; i < tm.getRowCount(); ++i) {
                try {
                    value = Double.valueOf((String)tm.getValueAt(i, colId));
                }
                catch (Exception e) {
                    continue;
                }
                switch (condition) {
                case LESS:
                    if (value < filter)
                        list.add(i);
                    break;
                case LESS_EQUAL:
                    if (value <= filter)
                        list.add(i);
                    break;
                case GREATER_EQUAL:
                    if (value >= filter)
                        list.add(i);
                    break;
                case GREATER:
                    if (value > filter)
                        list.add(i);
                    break;
                default:
                    double rndVal = value;
                    if (rounding > 0) {
                        double multiplier = Math.pow(10.0, rounding);
                        rndVal = Math.round(value * multiplier) / multiplier;
                    }
                    else
                        rndVal = Math.round(value);
                    if (Utils.equals(rndVal, filter))
                        list.add(i);
                    break;
                }
            }
            int arr[] = new int[list.size()];
            for (i = 0; i < list.size(); ++i)
                arr[i] = list.get(i);
            return arr;
        }
    }
    
    public class FileNodeRenderer extends CheckBoxNodeRenderer {
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component renderer = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            renderer.setBackground(null);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            if (renderer instanceof DefaultTreeCellRenderer && node.getAllowsChildren()) {
                ((DefaultTreeCellRenderer)renderer).setIcon(null);
                ((DefaultTreeCellRenderer)renderer).setOpaque(false);
                ((DefaultTreeCellRenderer)renderer).setBackgroundNonSelectionColor(null);
            }
            else {
                CheckBoxNodePanel panel = (CheckBoxNodePanel)renderer;
                if (!panel.hasFocus() && selected)
                    wotTree.clearSelection();
            }
            return renderer;
        }
    }
    
    public class CheckBoxIcon implements Icon {
        private Color color;
        private boolean checked;
        public CheckBoxIcon() { this.checked = false; this.color = UIManager.getColor("Panel.background"); }
        public CheckBoxIcon(boolean checked, Color color) { this.checked = checked; this.color = color; }
        public Color getColor() { return this.color; }
        public void setColor(Color color) { this.color = color; }
        public boolean isChecked() { return this.checked; }
        public void setChecked(boolean checked) { this.checked = checked; }
        @Override
        public int getIconWidth() { return iconWidthHeight; }
        @Override
        public int getIconHeight() { return iconWidthHeight; }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.BLACK);
            g.fillRect(x + iconFillPadding1, y + iconFillPadding1, getIconWidth() - iconFillPadding1 * 2, getIconHeight() - iconFillPadding1 * 2);
            g.setColor(color);
            g.fillRect(x + iconFillPadding2, y + iconFillPadding2, getIconWidth() - iconFillPadding2 * 2, getIconHeight() - iconFillPadding2 * 2);
        }
    }
    
    public class CheckboxHeaderRenderer implements TableCellRenderer {
        private final CheckBoxIcon checkIcon = new CheckBoxIcon();
        private int colId = -1;
        MouseListener mouseListener = null;
        private Color defaultColor = checkIcon.getColor();
        public CheckboxHeaderRenderer(int col, JTableHeader header) {
            colId = col;
            mouseListener = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        JTable table = ((JTableHeader) e.getSource()).getTable();
                        TableColumnModel columnModel = table.getColumnModel();
                        int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                        int modelColumn = table.convertColumnIndexToModel(viewColumn);
                        if (colId != modelColumn)
                            return;
                        if (SwingUtilities.isLeftMouseButton(e) && colors.size() > 0) {
                            checkIcon.setChecked(!checkIcon.isChecked());
                            if (checkIcon.isChecked()) {
                                defaultColor = checkIcon.getColor();
                                checkIcon.setColor(colors.pop());
                                TableModel model = table.getModel();
                                addXYSeries(model, colId - 1, columnModel.getColumn(viewColumn).getHeaderValue().toString(), checkIcon.getColor());
                            }
                            else {
                                colors.push(checkIcon.getColor());
                                checkIcon.setColor(defaultColor);
                                removeXYSeries(colId - 1);
                            }
                            ((JTableHeader) e.getSource()).repaint();
                        }
                        else if (SwingUtilities.isRightMouseButton(e)) {
                            SortingPopUp menu = new SortingPopUp(colId);
                            menu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };
            header.addMouseListener(mouseListener);
        }
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isS, boolean hasF, int row, int col) {
            TableCellRenderer r = tbl.getTableHeader().getDefaultRenderer();
            JLabel label = (JLabel) r.getTableCellRendererComponent(tbl, val, isS, hasF, row, col);
            label.setIcon(checkIcon);
            return label;
        }
        
        public CheckBoxIcon getCheckIcon() { return checkIcon; }
        public Color getDefaultColor() { return defaultColor; }
        public MouseListener getMouseListener() { return mouseListener; }
    }
    
    class ImageListCellRenderer implements ListCellRenderer<Object> {
        public Component getListCellRendererComponent(JList<?> jlist, Object value, int cellIndex, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof JLabel)
                return (Component)value;
            else
                return new JLabel("???");
        }
    }

    private class ColorPreviewPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private Color color;
        private int width = 400;
        private int height = 60;
        private int margin = 40;
        public ColorPreviewPanel(JColorChooser chooser) {
            color = chooser.getColor();
            setPreferredSize(new Dimension(width, height));
        }
        public void paint(Graphics g) {
            g.setColor(chartBgColor);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(color);
            g.fillRect(margin, getHeight() / 2 - 1, getWidth() - margin * 2, 1);
        }
        public void setColor(Color c) {
            color = c;
        }
    }
    
    private static JColorChooser colorChooser = new JColorChooser();
    private ChartPanel chartPanel = null;
    private ChartPanel wotChartPanel = null;
    private JTree wotTree = null;
    private JPopupMenu wotTreeMenu = null;
    private DefaultMutableTreeNode wotSelectedNode = null;
    private XYPlot plot = null;
    private XYPlot wotPlot = null;
    private XYSeriesCollection rpmDataset = null;
    private XYSeriesCollection dataset = null;
    private XYLineAndShapeRenderer rpmPlotRenderer = null;
    private XYLineAndShapeRenderer plotRenderer = null;
    private int rpmCol = -1;
    private int displCount = 0;
    private JPanel logViewPanel = null;
    private JToolBar toolBar = null;
    private DBTable logDataTable = null;
    private DBTFindFrame findWindow = null;
    private LogPlay logPlayWindow = null;
    private JScrollPane headerScrollPane = null;
    private DefaultListModel<JLabel> listModel = null;
    private JButton loadButton = null;
    private JButton printButton = null;
    private JButton previewButton = null;
    private JButton findButton = null;
    private JButton replaceButton = null;
    private JComboBox<String> selectionCombo;
    private JComboBox<String> compareCombo;
    private JTextField  filterText;
    private JButton filterButton;
    private JButton viewButton;
    private JButton logPlayButton;
    private Plot3DPanel plot3d = null;
    private ChartMouseListener chartMouseListener = null;
    private JComboBox<String> xAxisColumn = null;
    private JComboBox<String> yAxisColumn = null;
    private JMultiSelectionBox plotsColumn = null;
    private JMultiSelectionBox wotPlotsColumn = null;
    private ButtonGroup wotRbGroup = null;
    private JButton linkYAxis = null;
    private HashMap<String, ArrayList<HashMap<String, ArrayList<Double>>>> filesData = null;
    private ArrayList<TreeSet<String>> wotYAxisGroups = null;
    private int wotPoint = Config.getWOTStationaryPointValue();
    private int logThtlAngleColIdx = -1;
    private String logRpmColName = null;
    private String logTimeColName = null;
    private File lastPullExportDir = null;
    private ValueMarker startMarker = null;
    private ValueMarker endMarker = null;
    private XYDomainMutilineAnnotation xMarker = null;
    private XYDomainMutilineAnnotation wotMarker = null;
    private Stack<Color> colors = new Stack<Color>();
    private Insets insets0 = new Insets(0, 0, 0, 0);
    private Insets insets3 = new Insets(3, 3, 3, 3);
    private Insets insets10 = new Insets(10, 10, 10, 10);
    private boolean showWotCurvePoints = false;

    public LogView(int tabPlacement) {
        super(tabPlacement);
        initialize();
    }

    private void initialize() {
        createDataTab();
        createChartTab();
        createWotChartTab();
        createUsageTab();
    }
    
    public DBTable getLogDataTable() {
        return logDataTable;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // DATA TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void createDataTab() {
        JPanel dataPanel = new JPanel(new BorderLayout());
        add(dataPanel, "<html><div style='text-align: center;'>D<br>a<br>t<br>a</div></html>");
        
        createToolBar(dataPanel);
        createLogViewPanel();
        createGraghPanel();
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, logViewPanel, chartPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerLocation(150);
        dataPanel.add(splitPane);
    }

    private void createLogViewPanel() {
        logViewPanel = new JPanel();
        GridBagLayout gbl_logViewPanel = new GridBagLayout();
        gbl_logViewPanel.columnWidths = new int[] {0};
        gbl_logViewPanel.rowHeights = new int[] {0, 0};
        gbl_logViewPanel.columnWeights = new double[]{1.0};
        gbl_logViewPanel.rowWeights = new double[]{1.0, 0.0};
        logViewPanel.setLayout(gbl_logViewPanel);
        try {
            logDataTable = new DBTable();
            logDataTable.copyColumnHeaderNames = true;
            logDataTable.defaultClickCountToStartEditor = 2;
            logDataTable.doNotUseDatabaseSort = true;
            logDataTable.listenKeyPressEventsWholeWindow = true;
            logDataTable.createControlPanel(DBTable.READ_NAVIGATION);
            logDataTable.enableExcelCopyPaste();
            logDataTable.setSortEnabled(false); 
            logDataTable.setSkin(new TableSkin());
            logDataTable.refresh(new String[1][25]);
            logDataTable.setComparator(new DoubleComparator());
            logDataTable.getTable().setCellSelectionEnabled(true);
            logDataTable.getTable().setColumnSelectionAllowed(true);
            logDataTable.getTable().setRowSelectionAllowed(true);
            logDataTable.getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JTextField rowTextField = ((JTextField)logDataTable.getControlPanel().getComponent(tableRowTextFieldComponentIdx));
            rowTextField.setPreferredSize(null);
            rowTextField.setColumns(tableRowTextFieldSize);

            GridBagConstraints gbc_logDataTable = new GridBagConstraints();
            gbc_logDataTable.insets = insets0;
            gbc_logDataTable.anchor = GridBagConstraints.PAGE_START;
            gbc_logDataTable.fill = GridBagConstraints.BOTH;
            gbc_logDataTable.gridx = 0;
            gbc_logDataTable.gridy = 0;
            logViewPanel.add(logDataTable, gbc_logDataTable);
            listModel = new DefaultListModel<JLabel>(); 
            
            selectionCombo.removeAllItems();
            TreeSet<String> sortedCols = new TreeSet<String>();
            JTableHeader tableHeader = logDataTable.getTableHeader();
            for (int i = 0; i < logDataTable.getColumnCount(); ++i) {
                Column col = logDataTable.getColumn(i);
                col.setNullable(true);
                col.setHeaderRenderer(new CheckboxHeaderRenderer(i + 1, tableHeader));
                sortedCols.add(col.getHeaderValue().toString());
            }
            for (String s : sortedCols) {
                selectionCombo.addItem(s);
                listModel.addElement(new JLabel(s, new CheckBoxIcon(), JLabel.LEFT));
            }
            
            JList<JLabel> menuList = new JList<JLabel>(listModel);
            menuList.setOpaque(false);
            menuList.setCellRenderer(new ImageListCellRenderer());
            menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            menuList.setLayoutOrientation(JList.VERTICAL_WRAP);
            menuList.setFixedCellHeight(25);
            menuList.setVisibleRowCount(-1);
            menuList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        if (e.getClickCount() == 1 && colors.size() > 0) {
                            JList<?> list = (JList<?>)e.getSource();
                            int index = list.locationToIndex(e.getPoint());
                            if (index >= 0) {
                                JLabel label = (JLabel)list.getModel().getElementAt(index);
                                Column col = logDataTable.getColumnByHeaderName(label.getText());
                                if (col.getHeaderRenderer() instanceof CheckboxHeaderRenderer) {
                                    CheckboxHeaderRenderer renderer = (CheckboxHeaderRenderer)col.getHeaderRenderer();
                                    CheckBoxIcon checkIcon = (CheckBoxIcon)label.getIcon();
                                    checkIcon.setChecked(!checkIcon.isChecked());
                                    if (checkIcon.isChecked()) {
                                        checkIcon.setColor(colors.pop());
                                        JTable table = logDataTable.getTable();
                                        TableModel model = table.getModel();
                                        addXYSeries(model, col.getModelIndex() - 1, col.getHeaderValue().toString(), checkIcon.getColor());
                                    }
                                    else {
                                        colors.push(checkIcon.getColor());
                                        checkIcon.setColor(renderer.getDefaultColor());
                                        removeXYSeries(col.getModelIndex() - 1);
                                    }
                                    list.repaint();
                                }
                            }
                        }
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            
            headerScrollPane = new JScrollPane(menuList);
            GridBagConstraints gbc_headersTree = new GridBagConstraints();
            gbc_headersTree.insets = insets0;
            gbc_headersTree.anchor = GridBagConstraints.PAGE_START;
            gbc_headersTree.fill = GridBagConstraints.BOTH;
            gbc_headersTree.gridx = 0;
            gbc_headersTree.gridy = 1;
            
            logViewPanel.add(headerScrollPane, gbc_headersTree);
            headerScrollPane.setVisible(false);
            
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
    }
    
    private void createToolBar(JPanel panel)
    {
        toolBar = new JToolBar();
        panel.add(toolBar, BorderLayout.NORTH);

        loadButton = addToolbarButton("Load Log File", "/open.png");
        toolBar.addSeparator();
        printButton = addToolbarButton("Print", "/print.png");
        previewButton = addToolbarButton("Print Preview", "/print_preview.png");
        findButton = addToolbarButton("Find", "/find.png");
        replaceButton = addToolbarButton("Replace", "/replace.png");
        toolBar.addSeparator();
        
        viewButton = new JButton("Headers View");
        viewButton.setMargin(new Insets(2, 7, 2, 7));
        viewButton.addActionListener(this);
        toolBar.add(viewButton);
        toolBar.addSeparator();
        
        logPlayButton = new JButton("Log Play");
        logPlayButton.setMargin(new Insets(2, 7, 2, 7));
        logPlayButton.addActionListener(this);
        toolBar.add(logPlayButton);
        toolBar.addSeparator();
        
        toolBar.add(new JLabel("Filter "));
        
        JPanel filterPanel = new JPanel();
        GridBagLayout gbl_filterPanel = new GridBagLayout();
        gbl_filterPanel.columnWidths = new int[]{0, 0, 0, 0};
        gbl_filterPanel.rowHeights = new int[]{0};
        gbl_filterPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0};
        gbl_filterPanel.rowWeights = new double[]{0.0};
        filterPanel.setLayout(gbl_filterPanel);

        GridBagConstraints gbc_filter = new GridBagConstraints();
        gbc_filter.insets = insets3;
        gbc_filter.anchor = GridBagConstraints.WEST;
        gbc_filter.gridx = 0;
        gbc_filter.gridy = 0;
        
        selectionCombo = new JComboBox<String>();
        selectionCombo.setPrototypeDisplayValue(prototypeDisplayValue);
        selectionCombo.addActionListener(this);
        filterPanel.add(selectionCombo, gbc_filter);

        gbc_filter.gridx++;
        compareCombo = new JComboBox<String>(new String[] {"", less, lessEqual, equal, greaterEqual, greater});
        compareCombo.addActionListener(this);
        filterPanel.add(compareCombo, gbc_filter);

        gbc_filter.gridx++;
        filterText = new JTextField();
        filterText.setColumns(5);
        filterPanel.add(filterText, gbc_filter);

        gbc_filter.gridx++;
        filterButton = new JButton("Set");
        filterButton.addActionListener(this);
        filterPanel.add(filterButton, gbc_filter);
        
        toolBar.add(filterPanel);
    }
    
    private void createGraghPanel() {
        JFreeChart chart = ChartFactory.createXYLineChart(null, null, null, null, PlotOrientation.VERTICAL, false, true, false);
        chartPanel = new ChartPanel(chart, true, true, true, true, true);
        chartPanel.setFocusable(true);
        chartPanel.setAutoscrolls(true);
        chartPanel.setPopupMenu(null);
        chart.setBackgroundPaint(chartColor);

        rpmDataset = new XYSeriesCollection();
        rpmPlotRenderer = new XYLineAndShapeRenderer();        
        dataset = new XYSeriesCollection();
        plotRenderer = new XYLineAndShapeRenderer();

        NumberAxis xAxis = new NumberAxis();
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickLabelPaint(Color.WHITE);
        xAxis.setAutoRangeIncludesZero(false);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickLabelPaint(Color.WHITE);
        yAxis.setAutoRangeIncludesZero(false);
        NumberAxis y2Axis = new NumberAxis();
        y2Axis.setTickLabelsVisible(false);
        y2Axis.setTickLabelPaint(Color.WHITE);
        y2Axis.setAutoRangeIncludesZero(false);

        plot = chartPanel.getChart().getXYPlot();
        plot.setRangePannable(true);
        plot.setDomainPannable(true);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setBackgroundPaint(chartBgColor);

        plot.setDataset(0, rpmDataset);
        plot.setRenderer(0, rpmPlotRenderer);
        plot.setDomainAxis(0, xAxis);
        plot.setRangeAxis(0, yAxis);
        plot.mapDatasetToDomainAxis(0, 0);
        plot.mapDatasetToRangeAxis(0, 0);

        plot.setDataset(1, dataset);
        plot.setRenderer(1, plotRenderer);
        plot.setRangeAxis(1, y2Axis);
        plot.mapDatasetToDomainAxis(1, 0);
        plot.mapDatasetToRangeAxis(1, 1);
        
        LegendTitle legend = new LegendTitle(plot);
        legend.setItemFont(new Font("Arial", 0, 10));
        legend.setPosition(RectangleEdge.TOP);
        legend.setItemPaint(Color.WHITE);
        chart.addLegend(legend);
        
        xMarker = new XYDomainMutilineAnnotation();
        xMarker.setDefaultPaint(Color.WHITE);
        plot.addAnnotation(xMarker);
        
        chartMouseListener = new ChartMouseListener() {
            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
                try {
                    Rectangle2D dataArea = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
                    Point2D p = chartPanel.translateScreenToJava2D(event.getTrigger().getPoint());
                    double x = plot.getDomainAxis().java2DToValue(p.getX(), dataArea, plot.getDomainAxisEdge());
                    boolean isLeft = (p.getX() < (dataArea.getMaxX() - dataArea.getMinX()) / 2) ? true : false;
                    if (setMarkers(x, isLeft)) {
                        try {
                            int selectedCol = logDataTable.getTable().getSelectedColumn();
                            if (selectedCol < 0)
                                selectedCol = 0;
                            if (logPlayWindow == null || startMarker != null || endMarker != null) {
                                logDataTable.getTable().setRowSelectionInterval((int)x, (int)x);
                                logDataTable.getTable().changeSelection((int)x, selectedCol, false, false);
                            }
                            else {
                                logPlayWindow.setProgressBar((int)x);
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                chartPanel.requestFocusInWindow();
                if (logPlayWindow == null)
                    return;
                if (xMarker.count() == 0)
                    return;
                Rectangle2D dataArea = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
                Point2D p = chartPanel.translateScreenToJava2D(event.getTrigger().getPoint());
                double x = plot.getDomainAxis().java2DToValue(p.getX(), dataArea, plot.getDomainAxisEdge());
                if (x < 0 || (int)x >= logDataTable.getRowCount())
                    return;
                if (SwingUtilities.isLeftMouseButton(event.getTrigger())) {
                    if (startMarker == null) {
                        startMarker = new ValueMarker(x);
                        startMarker.setPaint(Color.GREEN);
                        startMarker.setStroke(new BasicStroke(1.5f));
                        plot.addDomainMarker(startMarker);
                    }
                    else {
                        plot.removeDomainMarker(startMarker);
                        startMarker = null;
                    }
                }
                else if (SwingUtilities.isRightMouseButton(event.getTrigger())) {
                    if (endMarker == null) {
                        endMarker = new ValueMarker(x);
                        endMarker.setPaint(Color.GREEN);
                        endMarker.setStroke(new BasicStroke(1.5f));
                        plot.addDomainMarker(endMarker);
                    }
                    else {
                        plot.removeDomainMarker(endMarker);
                        endMarker = null;
                    }
                }
                chartPanel.repaint();
                logPlayWindow.setStartEndArea(startMarker, endMarker);
            }
        };
        chartPanel.addChartMouseListener(chartMouseListener);
        chartPanel.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (!chartPanel.hasFocus())
                    return;
                int keyCode = e.getKeyCode();
                if (keyCode < KeyEvent.VK_LEFT || keyCode > KeyEvent.VK_DOWN)
                    return;
                ValueAxis axis = null;
                if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT)
                    axis = ((XYPlot)chartPanel.getChart().getXYPlot()).getDomainAxis();
                else
                    axis = ((XYPlot)chartPanel.getChart().getXYPlot()).getRangeAxis();
                if (axis != null) {
                    double delta = (axis.getUpperBound()- axis.getLowerBound()) / 100.0;
                    if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_DOWN)
                        axis.setRange(axis.getLowerBound()- delta, axis.getUpperBound() - delta);
                    else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_RIGHT)
                        axis.setRange(axis.getLowerBound() + delta, axis.getUpperBound() + delta);
                }
            }
            public void keyReleased(KeyEvent arg0) { }
            public void keyTyped(KeyEvent arg0) { }
        });
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE 3D CHART TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void createChartTab() {
        JPanel plotPanel = new JPanel();
        add(plotPanel, "<html><div style='text-align: center;'>3<br>D<br><br>C<br>h<br>a<br>r<br>t</div></html>");
        GridBagLayout gbl_plotPanel = new GridBagLayout();
        gbl_plotPanel.columnWidths = new int[] {0};
        gbl_plotPanel.rowHeights = new int[] {0, 0};
        gbl_plotPanel.columnWeights = new double[]{1.0};
        gbl_plotPanel.rowWeights = new double[]{0.0, 1.0};
        plotPanel.setLayout(gbl_plotPanel);

        JPanel cntlPanel = new JPanel();
        GridBagConstraints gbc_ctrlPanel = new GridBagConstraints();
        gbc_ctrlPanel.insets = insets3;
        gbc_ctrlPanel.anchor = GridBagConstraints.NORTH;
        gbc_ctrlPanel.weightx = 1.0;
        gbc_ctrlPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_ctrlPanel.gridx = 0;
        gbc_ctrlPanel.gridy = 0;
        plotPanel.add(cntlPanel, gbc_ctrlPanel);
        
        GridBagLayout gbl_cntlPanel = new GridBagLayout();
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);

        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.anchor = GridBagConstraints.EAST;
        gbc_label.insets = new Insets(3, 3, 3, 0);
        gbc_label.gridx = 0;
        gbc_label.gridy = 0;
        
        GridBagConstraints gbc_column = new GridBagConstraints();
        gbc_column.anchor = GridBagConstraints.WEST;
        gbc_column.insets = insets3;
        gbc_column.gridx = 1;
        gbc_column.gridy = 0;
        
        cntlPanel.add(new JLabel("X-Axis"), gbc_label);
        
        xAxisColumn = new JComboBox<String>();
        xAxisColumn.setPrototypeDisplayValue(prototypeDisplayValue);
        cntlPanel.add(xAxisColumn, gbc_column);
        
        gbc_label.gridx += 2;
        cntlPanel.add(new JLabel("Y-Axis"), gbc_label);

        gbc_column.gridx += 2;
        yAxisColumn = new JComboBox<String>();
        yAxisColumn.setPrototypeDisplayValue(prototypeDisplayValue);
        cntlPanel.add(yAxisColumn, gbc_column);

        gbc_label.gridx += 2;
        cntlPanel.add(new JLabel("Plots"), gbc_label);

        gbc_column.gridx += 2;
        plotsColumn = new JMultiSelectionBox();
        plotsColumn.setPrototypeDisplayValue(prototypeDisplayValue);
        cntlPanel.add(plotsColumn, gbc_column);

        gbc_label.gridx += 2;
        JButton btnGoButton = new JButton("View");
        btnGoButton.setActionCommand("view");
        btnGoButton.addActionListener(this);
        cntlPanel.add(btnGoButton, gbc_label);
        
        plot3d = new Plot3DPanel("SOUTH") {
            private static final long serialVersionUID = 7914951068593204419L;
            public void addPlotToolBar(String location) {
                super.addPlotToolBar(location);
                super.plotToolBar.remove(7);
                super.plotToolBar.remove(5);
                super.plotToolBar.remove(4);
            }            
        };
        plot3d.setAutoBounds();
        plot3d.setAutoscrolls(true);
        plot3d.setEditable(false);
        plot3d.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        plot3d.setForeground(Color.BLACK);
        plot3d.getAxis(0).setColor(Color.BLACK);
        plot3d.getAxis(1).setColor(Color.BLACK);
        plot3d.getAxis(2).setColor(Color.BLACK);
        
        GridBagConstraints gbc_chartPanel = new GridBagConstraints();
        gbc_chartPanel.anchor = GridBagConstraints.CENTER;
        gbc_chartPanel.insets = insets3;
        gbc_chartPanel.weightx = 1.0;
        gbc_chartPanel.weighty = 1.0;
        gbc_chartPanel.fill = GridBagConstraints.BOTH;
        gbc_chartPanel.gridx = 0;
        gbc_chartPanel.gridy = 1;
        plotPanel.add(plot3d, gbc_chartPanel);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE WOT TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void createWotChartTab() {
        JPanel plotPanel = new JPanel();
        add(plotPanel, "<html><div style='text-align: center;'>W<br>O<br>T<br><br>P<br>u<br>l<br>l<br>s</div></html>");
        GridBagLayout gbl_plotPanel = new GridBagLayout();
        gbl_plotPanel.columnWidths = new int[] {0};
        gbl_plotPanel.rowHeights = new int[] {0, 0};
        gbl_plotPanel.columnWeights = new double[]{1.0};
        gbl_plotPanel.rowWeights = new double[]{0.0, 1.0};
        plotPanel.setLayout(gbl_plotPanel);
        
        createWotCtrlPanel(plotPanel);
        createWotTreePanel();
        createWotChart();
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(wotTree), wotChartPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerLocation(150);
        GridBagConstraints gbc_dataPanel = new GridBagConstraints();
        gbc_dataPanel.insets = insets3;
        gbc_dataPanel.anchor = GridBagConstraints.CENTER;
        gbc_dataPanel.weightx = 1.0;
        gbc_dataPanel.weighty = 1.0;
        gbc_dataPanel.fill = GridBagConstraints.BOTH;
        gbc_dataPanel.gridx = 0;
        gbc_dataPanel.gridy = 1;
        plotPanel.add(splitPane, gbc_dataPanel);
    }
    
    protected void createWotCtrlPanel(JPanel plotPanel) {
        wotRbGroup = new ButtonGroup();
        JPanel cntlPanel = new JPanel();
        GridBagConstraints gbc_ctrlPanel = new GridBagConstraints();
        gbc_ctrlPanel.insets = insets3;
        gbc_ctrlPanel.anchor = GridBagConstraints.NORTH;
        gbc_ctrlPanel.weightx = 1.0;
        gbc_ctrlPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_ctrlPanel.gridx = 0;
        gbc_ctrlPanel.gridy = 0;
        plotPanel.add(cntlPanel, gbc_ctrlPanel);
        
        GridBagLayout gbl_cntlPanel = new GridBagLayout();
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets3;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JButton btnAddButton = new JButton("Load Log");
        btnAddButton.setActionCommand("loadWot");
        btnAddButton.addActionListener(this);
        cntlPanel.add(btnAddButton, gbc);

        gbc.gridx += 1;
        gbc.anchor = GridBagConstraints.EAST;
        cntlPanel.add(new JLabel("Plots"), gbc);

        gbc.gridx += 1;
        gbc.anchor = GridBagConstraints.WEST;
        wotPlotsColumn = new JMultiSelectionBox();
        wotPlotsColumn.setPrototypeDisplayValue(prototypeDisplayValue);
        cntlPanel.add(wotPlotsColumn, gbc);

        gbc.gridx += 1;
        linkYAxis = new JButton("Link Y-Axis");
        linkYAxis.setActionCommand("linkyaxis");
        linkYAxis.addActionListener(this);
        linkYAxis.setEnabled(false);
        cntlPanel.add(linkYAxis, gbc);

        gbc.gridx += 1;
        JRadioButton button = new JRadioButton("RPM");
        wotRbGroup.add(button);
        cntlPanel.add(button, gbc);

        gbc.gridx += 1;
        button = new JRadioButton("RPM (skip down spikes)");
        wotRbGroup.add(button);
        cntlPanel.add(button, gbc);

        gbc.gridx += 1;
        button = new JRadioButton(timeAxisName);
        wotRbGroup.add(button);
        cntlPanel.add(button, gbc);

        gbc.gridx += 1;
        JCheckBox checkBox = new JCheckBox("Show points");
        checkBox.setActionCommand("showpts");
        checkBox.addActionListener(this);
        cntlPanel.add(checkBox, gbc);

        gbc.gridx += 1;
        JButton exportButton = new JButton("Export Selected Pulls");
        exportButton.setActionCommand("export");
        exportButton.addActionListener(this);
        cntlPanel.add(exportButton, gbc);

        gbc.gridx += 1;
        gbc.anchor = GridBagConstraints.EAST;
        JButton btnGoButton = new JButton("View");
        btnGoButton.setActionCommand("viewWot");
        btnGoButton.addActionListener(this);
        cntlPanel.add(btnGoButton, gbc);
        ((JRadioButton)wotRbGroup.getElements().nextElement()).setSelected(true);
    }
    
    protected void createWotTreePanel() {
        wotTreeMenu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Select All");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (wotSelectedNode != null)
                    setWotNodesChecked(true);
            }
        });
        wotTreeMenu.add(item);
        item = new JMenuItem("Clear All");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (wotSelectedNode != null)
                    setWotNodesChecked(false);
            }
        });
        wotTreeMenu.add(item);
        DefaultMutableTreeNode wotTreeRoot = new DefaultMutableTreeNode("Root");
        DefaultTreeModel treeModel = new DefaultTreeModel(wotTreeRoot);
        wotTree = new JTree(treeModel);
        wotTree.setCellRenderer(new FileNodeRenderer());
        wotTree.setCellEditor(new CheckBoxNodeEditor(wotTree));
        wotTree.setEditable(true);
        wotTree.setRootVisible(false);
        wotTree.setOpaque(false);
        wotTree.setBackground(null);
        wotTree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON3) {
                    TreePath pathForLocation = wotTree.getPathForLocation(event.getPoint().x, event.getPoint().y);
                    if (pathForLocation != null) {
                        wotTree.setComponentPopupMenu(wotTreeMenu);
                        wotSelectedNode = (DefaultMutableTreeNode) pathForLocation.getLastPathComponent();
                    }
                    else {
                        wotTree.setComponentPopupMenu(null);
                        wotSelectedNode = null;
                    }
                }
                super.mousePressed(event);
            }
        });
        wotTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    }
    
    protected void createWotChart() {
        wotMarker = new XYDomainMutilineAnnotation();
        wotMarker.setDefaultPaint(Color.WHITE);
        JFreeChart chart = ChartFactory.createXYLineChart(null, null, null, null, PlotOrientation.VERTICAL, false, true, false);
        wotChartPanel = new ChartPanel(chart, true, true, true, true, true);
        wotChartPanel.setFocusable(true);
        wotChartPanel.setAutoscrolls(true);
        chart.setBackgroundPaint(chartColor);
        
        wotPlot = chart.getXYPlot();
        wotPlot.setRangePannable(true);
        wotPlot.setDomainPannable(true);
        wotPlot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        wotPlot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        wotPlot.setBackgroundPaint(chartBgColor);
        wotPlot.addAnnotation(wotMarker);

        wotPlot.setRangeAxis(null);

        NumberAxis xAxis = new NumberAxis();
        xAxis.setAutoRangeIncludesZero(false);
        xAxis.setTickLabelPaint(Color.WHITE);
        xAxis.setLabelPaint(Color.LIGHT_GRAY);
        wotPlot.setDomainAxis(0, xAxis);
        
        wotPlot.setDataset(0, new XYSeriesCollection());
        wotPlot.mapDatasetToDomainAxis(0, 0);
        wotPlot.mapDatasetToRangeAxis(0, 0);
        
        LegendTitle legend = new LegendTitle(wotPlot); 
        legend.setItemFont(new Font("Arial", 0, 10));
        legend.setPosition(RectangleEdge.TOP);
        legend.setItemPaint(Color.WHITE);
        chart.addLegend(legend);
        
        wotChartPanel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
                try {
                    wotPlot.clearRangeMarkers();
                    wotMarker.clearLabels(false);
                    Rectangle2D dataArea = wotChartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
                    Point2D p = wotChartPanel.translateScreenToJava2D(event.getTrigger().getPoint());
                    double x = wotPlot.getDomainAxis().java2DToValue(p.getX(), dataArea, wotPlot.getDomainAxisEdge());
                    XYSeriesCollection dataset;
                    XYSeries series;
                    for (int i = 0; i < wotPlot.getDatasetCount(); ++i) {
                        dataset = (XYSeriesCollection)wotPlot.getDataset(i);
                        if (dataset != null && dataset.getSeriesCount() > 0) {
                            for (int j = 0; j < dataset.getSeriesCount(); ++j) {
                                series = dataset.getSeries(j);
                                int idx = 0;
                                double closest;
                                double pclosest = Math.abs(series.getX(0).doubleValue() - x);
                                for (int k = 1; k < series.getItemCount(); ++k) {
                                    closest = Math.abs(series.getX(k).doubleValue() - x);
                                    if (closest < pclosest){
                                        idx = k;
                                        pclosest = closest;
                                    }
                                }
                                double x0 = series.getX(0).doubleValue();
                                double x1 = series.getX(series.getItemCount() - 1).doubleValue();
                                double xerr = x0 * 0.01;
                                if (x >= (x0 - xerr) && x <= (x1 + xerr))
                                	wotMarker.addLabel(series.getDescription() + ": ", wotPlot.getRenderer(i).getSeriesPaint(j), series.getY(idx).toString(), false);
                            }
                        }
                    }
                    if (wotMarker.count() > 0) {
                        boolean isLeft = (p.getX() < (dataArea.getMaxX() - dataArea.getMinX()) / 2) ? true : false;
                        if (isLeft) {
                            wotMarker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
                            wotMarker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
                        }
                        else {
                            wotMarker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
                            wotMarker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
                        }
                        wotMarker.setValue(x);
                    }
                    wotChartPanel.repaint();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    logger.error(ex);
                }
            }

            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                wotChartPanel.requestFocusInWindow();
                ChartEntity entity = event.getEntity();                
                if (entity == null || !(entity instanceof LegendItemEntity))
                    return;
                LegendItemEntity itemEntity = (LegendItemEntity)entity;
                int seriesIndex = ((XYDataset)itemEntity.getDataset()).indexOf(itemEntity.getSeriesKey());
                XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)wotChartPanel.getChart().getXYPlot().getRenderer();
                Paint p = renderer.getSeriesPaint(seriesIndex);
                colorChooser.setColor((Color)p);
                final ColorPreviewPanel preview = new ColorPreviewPanel(colorChooser);
                colorChooser.setPreviewPanel(preview);
                AbstractColorChooserPanel[] panels = colorChooser.getChooserPanels();
                for (AbstractColorChooserPanel accp : panels) {
                    if(!accp.getDisplayName().equals("Swatches")) {
                        colorChooser.removeChooserPanel(accp);
                    } 
                }
                ColorSelectionModel model = colorChooser.getSelectionModel();                
                model.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent event) {
                        ColorSelectionModel model = (ColorSelectionModel)event.getSource();
                        preview.setColor(model.getSelectedColor());
                    }
                });
                if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, colorChooser, "Select new color for " + itemEntity.getSeriesKey(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE))
                    renderer.setSeriesPaint(seriesIndex, colorChooser.getColor());
            }
        });
        wotChartPanel.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (!wotChartPanel.hasFocus())
                    return;
                int keyCode = e.getKeyCode();
                if (keyCode < KeyEvent.VK_LEFT || keyCode > KeyEvent.VK_DOWN)
                    return;
                ValueAxis axis = null;
                if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT)
                    axis = ((XYPlot)wotChartPanel.getChart().getXYPlot()).getDomainAxis();
                else
                    axis = ((XYPlot)wotChartPanel.getChart().getXYPlot()).getRangeAxis();
                if (axis != null) {
                    double delta = (axis.getUpperBound()- axis.getLowerBound()) / 100.0;
                    if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_DOWN)
                        axis.setRange(axis.getLowerBound()- delta, axis.getUpperBound() - delta);
                    else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_RIGHT)
                        axis.setRange(axis.getLowerBound() + delta, axis.getUpperBound() + delta);
                }
            }
            public void keyReleased(KeyEvent arg0) { }
            public void keyTyped(KeyEvent arg0) { }
        });
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE USAGE TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void createUsageTab() {
        JTextPane  usageTextArea = new JTextPane();
        usageTextArea.setMargin(insets10);
        usageTextArea.setContentType("text/html");
        usageTextArea.setText(usage());
        usageTextArea.setEditable(false);
        usageTextArea.setCaretPosition(0);

        JScrollPane textScrollPane = new JScrollPane(usageTextArea);
        textScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        textScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(textScrollPane, "<html><div style='text-align: center;'>U<br>s<br>a<br>g<br>e</div></html>");
    }
    
    private String usage() {
        ResourceBundle bundle;
        bundle = ResourceBundle.getBundle("com.vgi.mafscaling.logview");
        return bundle.getString("usage"); 
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // WORK FUNCTIONS
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void initColors() {
        colors.clear();
        colors.push(new Color(255,101,0));
        colors.push(new Color(255,154,0));
        colors.push(new Color(255,207,0));
        colors.push(new Color(156,207,0));
        colors.push(new Color(49,207,206));
        colors.push(new Color(49,101,255));
        colors.push(new Color(255,207,156));
        colors.push(new Color(206,154,255));
        colors.push(new Color(255,154,206));
        colors.push(new Color(156,207,255));
        colors.push(new Color(255,255,156));
        colors.push(new Color(206,255,206));
        colors.push(new Color(206,255,255));
        colors.push(new Color(0,207,255));
        colors.push(new Color(0,0,255));
        colors.push(new Color(0,130,132));
        colors.push(new Color(132,0,0));
        colors.push(new Color(132,0,132));
        colors.push(new Color(0,255,255));
        colors.push(new Color(255,255,0));
        colors.push(new Color(255,0,255));
        colors.push(new Color(0,0,132));
        colors.push(new Color(206,207,255));
        colors.push(new Color(0,101,206));
        colors.push(new Color(255,130,132));
        colors.push(new Color(99,0,99));
        colors.push(new Color(206,255,255));
        colors.push(new Color(255,255,206));
        colors.push(new Color(156,48,99));
        colors.push(new Color(156,154,255));
        colors.push(new Color(221,160,221));
        colors.push(new Color(176,196,222));
        colors.push(new Color(32,178,170));
        colors.push(new Color(250,240,230));
        colors.push(new Color(210,180,140));
        colors.push(new Color(143,188,139));
        colors.push(new Color(219,220,37));
        colors.push(new Color(42,214,42));
        colors.push(new Color(241,104,60));
        colors.push(new Color(29,139,209));
        /*
        colors.push(Color.decode("#FF6500"));
        colors.push(Color.decode("#FF9A00"));
        colors.push(Color.decode("#FFCF00"));
        colors.push(Color.decode("#9CCF00"));
        colors.push(Color.decode("#31CFCE"));
        colors.push(Color.decode("#3165FF"));
        colors.push(Color.decode("#FFCF9C"));
        colors.push(Color.decode("#CE9AFF"));
        colors.push(Color.decode("#FF9ACE"));
        colors.push(Color.decode("#9CCFFF"));
        colors.push(Color.decode("#FFFF9C"));
        colors.push(Color.decode("#CEFFCE"));
        colors.push(Color.decode("#CEFFFF"));
        colors.push(Color.decode("#00CFFF"));
        colors.push(Color.decode("#0000FF"));
        colors.push(Color.decode("#008284"));
        colors.push(Color.decode("#840000"));
        colors.push(Color.decode("#840084"));
        colors.push(Color.decode("#00FFFF"));
        colors.push(Color.decode("#FFFF00"));
        colors.push(Color.decode("#FF00FF"));
        colors.push(Color.decode("#000084"));
        colors.push(Color.decode("#CECFFF"));
        colors.push(Color.decode("#0065CE"));
        colors.push(Color.decode("#FF8284"));
        colors.push(Color.decode("#630063"));
        colors.push(Color.decode("#CEFFFF"));
        colors.push(Color.decode("#FFFFCE"));
        colors.push(Color.decode("#9C3063"));
        colors.push(Color.decode("#9C9AFF"));
        colors.push(Color.decode("#DDA0DD"));
        colors.push(Color.decode("#B0C4DE"));
        colors.push(Color.decode("#20B2AA"));
        colors.push(Color.decode("#FAF0E6"));
        colors.push(Color.decode("#D2B48C"));
        colors.push(Color.decode("#8FBC8B"));
        colors.push(Color.decode("#DBDC25"));
        colors.push(Color.decode("#2AD62A"));
        colors.push(Color.decode("#F1683C"));
        colors.push(Color.decode("#1D8BD1"));
        */
    }
    
    private JButton addToolbarButton(String tooltip, String image) {
        JButton button = new JButton(new ImageIcon(this.getClass().getResource(image)));
        button.setToolTipText(tooltip);
        button.setMargin(insets0);
        button.setAlignmentY(Component.CENTER_ALIGNMENT);
        button.addActionListener(this);
        toolBar.add(button);
        return button;
    }
    
    private void addXYSeries(TableModel model, int column, String name, Color color) {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            XYSeries series;
            if (column == rpmCol) {
                series = rpmDataset.getSeries(0);
                ((NumberAxis)plot.getRangeAxis(0)).setStandardTickUnits(NumberAxis.createIntegerTickUnits());
                plot.getRangeAxis(0).setTickLabelsVisible(true);
                rpmPlotRenderer.setSeriesPaint(0, color);
                rpmPlotRenderer.setSeriesVisible(0, true);
            }
            else {
                series = dataset.getSeries(column);
                plot.getRangeAxis(1).setTickLabelsVisible(true);
                plotRenderer.setSeriesPaint(column, color);
                plotRenderer.setSeriesVisible(column, true);
                displCount += 1;
            }
            if (xMarker.count() > 0)
                xMarker.addLabel(series.getDescription() + ": ", color, series.getY((int) xMarker.getValue()).toString(), true);
        }
        finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
        chartPanel.revalidate();
    }
    
    private void removeXYSeries(int column) {
        if (column == rpmCol) {
            ((NumberAxis)plot.getRangeAxis(0)).setStandardTickUnits(new StandardTickUnitSource());
            plot.getRangeAxis(0).setTickLabelsVisible(false);
            rpmPlotRenderer.setSeriesVisible(0, false);
            xMarker.removeLabel(rpmPlotRenderer.getSeriesPaint(0));
        }
        else {
            displCount -= 1;
            if (displCount == 0)
                plot.getRangeAxis(1).setTickLabelsVisible(false);
            plotRenderer.setSeriesVisible(column, false);
            xMarker.removeLabel(plotRenderer.getSeriesPaint(column));
        }
    }
    
    private void sortAscending(int column) {
        logDataTable.sortByColumn(column, true) ;
    }
    
    private void sortDescending(int column) {
        logDataTable.sortByColumn(column, false) ;
    }

    private void convertTimeToMsec() {
        String val;
        boolean timeColFound = false;
        for (int i = 0; i < logDataTable.getColumnCount() && !timeColFound; ++i) {
            String colName = logDataTable.getColumn(i).getHeaderValue().toString();
            if (colName.toLowerCase().matches(timeMatchString)) {
                if (logDataTable.getRowCount() > 0) {
                    val = (String)logDataTable.getValueAt(0, i);
                    if (val.matches(Utils.tmRegex)) {
                        long time = 0;
                        long tmbase = 0;
                        for (int j = 0; j < logDataTable.getRowCount(); ++j) {
                            try {
                                if (j == 0)
                                    Utils.resetBaseTime((String)logDataTable.getValueAt(j, i));
                                time = Utils.parseTime((String)logDataTable.getValueAt(j, i));
                                if (tmbase == 0) {
                                    tmbase = time;
                                    if (time > 1000)
                                        time = 0;
                                }
                                logDataTable.setValueAt(String.valueOf(time), j, i);
                            }
                            catch (Exception e) {
                                JOptionPane.showMessageDialog(null, "Invalid numeric value in column " + colName + ", row " + (j + 1), "Invalid value", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void convertOnOffToNumMsec() {
        String val;
        for (int i = logDataTable.getColumnCount() - 1; i >= 0 ; --i) {
            if (logDataTable.getRowCount() > 0) {
                val = (String)logDataTable.getValueAt(0, i);
                if (val.matches(Utils.onOffRegex)) {
                    for (int j = 0; j < logDataTable.getRowCount(); ++j) {
                        val = (String)logDataTable.getValueAt(j, i);
                        logDataTable.setValueAt(String.valueOf(Utils.parseValue(val)), j, i);
                    }
                }
                else if (!Pattern.matches(Utils.fpRegex, val))
                    logDataTable.removeColumn(logDataTable.getColumn(i));
            }
        }
    }
    
    private void selectLogFile() {
        fileChooser.setMultiSelectionEnabled(false);
        if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(this))
            return;
        loadLogFile();
    }
    
    private void loadLogFile() {
        // close log player
        if (logPlayWindow != null)
            disposeLogView();
        // process log file
        File file = fileChooser.getSelectedFile();
        Properties prop = new Properties();
        prop.put("delimiter", ",");
        prop.put("firstRowHasColumnNames", "true");
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        logDataTable.filter(null);
        filterText.setText("");
        Column col;
        String colName;
        String lcColName;
        String val;
        try {
            for (int i = 0; i < logDataTable.getColumnCount(); ++i) {
                try {
                    CheckboxHeaderRenderer cbr = (CheckboxHeaderRenderer)logDataTable.getColumn(i).getHeaderRenderer();
                    logDataTable.getTableHeader().removeMouseListener(cbr.getMouseListener());
                }
                catch (Exception e) {
                }
            }
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsoluteFile()), Config.getEncoding()));
                String line = null;
                br.mark(1024);
                while ((line = br.readLine()) != null && !line.contains(",")) {
                    br.mark(1024);
                    continue;
                }
                br.reset();
                logDataTable.refresh(br, prop);
                // Below is a hack code to check and convert time column hh:mm:ss.sss to msec number
                convertTimeToMsec();
                // Below is a hack code to check and convert column(s) with on/off values to number
                convertOnOffToNumMsec();
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            finally {
                if (br != null) {
                    try {
                        br.close();
                    }
                    catch (IOException e) {
                        logger.error(e);
                    }
                }
            }
            CheckboxHeaderRenderer renderer;
            Component comp;
            XYSeries series;
            selectionCombo.removeAllItems();
            listModel.removeAllElements();
            xAxisColumn.removeAllItems();
            yAxisColumn.removeAllItems();
            plotsColumn.removeAllItems();
            xAxisColumn.addItem("");
            yAxisColumn.addItem("");
            plotsColumn.setText("");
            plot3d.removeAllPlots();
            rpmDataset.removeAllSeries();
            dataset.removeAllSeries();
            xMarker.clearLabels(true);
            rpmCol = -1;
            displCount = 0;
            JTableHeader tableHeader = logDataTable.getTableHeader();
            TreeSet<String> sortedColumns = new TreeSet<String>();
            for (int i = 0; i < logDataTable.getColumnCount(); ++i) {
                col = logDataTable.getColumn(i);
                renderer = new CheckboxHeaderRenderer(i + 1, tableHeader);
                col.setHeaderRenderer(renderer);
                colName = col.getHeaderValue().toString();
                sortedColumns.add(colName);
                comp = renderer.getTableCellRendererComponent(logDataTable.getTable(), colName, false, false, 0, 0);
                col.setPreferredWidth(comp.getPreferredSize().width + 4);
                series = new XYSeries(colName);
                series.setDescription(colName);
                lcColName = colName.toLowerCase();
                dataset.addSeries(series);
                plotRenderer.setSeriesShapesVisible(i, false);
                plotRenderer.setSeriesVisible(i, false);
                if (rpmDataset.getSeriesCount() == 0 && (lcColName.matches(rpmMatchString) || lcColName.matches(engineSpeedMatchString))) {
                    rpmDataset.addSeries(series);
                    rpmPlotRenderer.setSeriesShapesVisible(0, false);
                    rpmPlotRenderer.setSeriesVisible(0, false);
                    rpmCol = i;
                }
                for (int j = 0; j < logDataTable.getRowCount(); ++j) {
                    try {
                        val = (String)logDataTable.getValueAt(j, i);
                        series.add(j, Double.valueOf(val), false);
                    }
                    catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "Invalid numeric value in column " + colName + ", row " + (j + 1), "Invalid value", JOptionPane.ERROR_MESSAGE);
                        if (br != null) br.close();
                        return;
                    }
                }
                series.fireSeriesChanged();
            }
            for (String s : sortedColumns) {
                xAxisColumn.addItem(s);
                yAxisColumn.addItem(s);
                plotsColumn.addItem(s);
                selectionCombo.addItem(s);
                renderer = (CheckboxHeaderRenderer)logDataTable.getColumnByHeaderName(s).getHeaderRenderer();
                listModel.addElement(new JLabel(s, renderer.getCheckIcon(), JLabel.LEFT));
            }
            if (logDataTable.getControlPanel().getComponentCount() > tableControlPanelNumComponents)
                logDataTable.getControlPanel().remove(tableControlPanelNumComponents);
            logDataTable.getControlPanel().add(new JLabel("     [" + file.getName() + "]"));
            initColors();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex);
        }
        finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public ChartPanel getChartPanel() {
        return chartPanel;
    }
    
    private void updateChart() {
        if (logDataTable.getColumnCount() != dataset.getSeriesCount())
            return;
        Column col;
        String colName;
        String val;
        XYSeries series;
        int seriesIdx = 0;
        for (int i = 0; i < logDataTable.getColumnCount(); ++i) {
            int colIdx = logDataTable.getCurrentIndexForOriginalColumn(i);
            col = logDataTable.getColumn(colIdx);
            colName = col.getHeaderValue().toString();
            series = dataset.getSeries(seriesIdx++);
            if (!series.getDescription().equals(colName)) {
                JOptionPane.showMessageDialog(null, "Invalid series found for the column index " + colIdx + ": series name " + series.getDescription() + " doesn't match column name " + colName, "Invalid value", JOptionPane.ERROR_MESSAGE);
                return;
            }
            series.clear();
            for (int j = 0; j < logDataTable.getRowCount(); ++j) {
                try {
                    val = (String)logDataTable.getValueAt(j, colIdx);
                    series.add(j, Double.valueOf(val), false);
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Invalid numeric value in column " + colName + ", row " + (j + 1), "Invalid value", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            series.fireSeriesChanged();
        }
        chartPanel.repaint();
    }

    private void view3dPlots() {
        if (xAxisColumn.getSelectedItem() == null ||
            xAxisColumn.getSelectedItem().toString().isEmpty() ||
            yAxisColumn.getSelectedItem() == null ||
            yAxisColumn.getSelectedItem().toString().isEmpty() ||
            plotsColumn.getSelectedItems() == null)
            return;
        plot3d.removeAllPlots();
        String val;
        String xAxisColName = (String)xAxisColumn.getSelectedItem();
        String yAxisColName = (String)yAxisColumn.getSelectedItem();
        List<String> dataColNames = plotsColumn.getSelectedItems();
        if (dataColNames.size() > maxNumPlots) {
            JOptionPane.showMessageDialog(null, "Sorry, only " + maxNumPlots + " plots are supported. More plots will make the graph too slow.", "Too many parameters", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int xColIdx = logDataTable.getColumnByHeaderName(xAxisColName).getModelIndex() - 1;
        xColIdx = logDataTable.getCurrentIndexForOriginalColumn(xColIdx);
        int yColIdx = logDataTable.getColumnByHeaderName(yAxisColName).getModelIndex() - 1;
        yColIdx = logDataTable.getCurrentIndexForOriginalColumn(yColIdx);
        ArrayList<Color> colorsArray = new ArrayList<Color>();
        colorsArray.add(Color.BLUE);
        colorsArray.add(Color.RED);
        colorsArray.add(Color.GREEN);
        colorsArray.add(Color.ORANGE);
        colorsArray.add(Color.GRAY);
        double x, y, z;
        XYZ xyz;
        for (int j = 0; j < dataColNames.size(); ++j) {
            HashSet<XYZ> uniqueXYZ = new HashSet<XYZ>();
            int zColIdx = logDataTable.getColumnByHeaderName(dataColNames.get(j)).getModelIndex() - 1;
            zColIdx = logDataTable.getCurrentIndexForOriginalColumn(zColIdx);
            int count = 0;
            double[][] xyzArrayTemp = new double[logDataTable.getRowCount()][3];
            for (int i = 0; i < logDataTable.getRowCount(); ++i) {
                val = (String)logDataTable.getValueAt(i, xColIdx);
                x = Double.valueOf(val);
                val = (String)logDataTable.getValueAt(i, yColIdx);
                y = Double.valueOf(val);
                val = (String)logDataTable.getValueAt(i, zColIdx);
                z = Double.valueOf(val);
                xyz = new XYZ(x, y, z);
                if (uniqueXYZ.contains(xyz))
                    continue;
                uniqueXYZ.add(xyz);
                xyzArrayTemp[count][0] = x;
                xyzArrayTemp[count][1] = y;
                xyzArrayTemp[count][2] = z;
                count += 1;
            }
            double[][] xyzArray = new double[uniqueXYZ.size()][3];
            for (int k = 0; k < xyzArray.length; ++k)
                System.arraycopy(xyzArrayTemp[k], 0, xyzArray[k], 0, 3);
            plot3d.addScatterPlot(dataColNames.get(j), colorsArray.get(j), xyzArray);
        }
        plot3d.setAxisLabel(0, xAxisColumn.getSelectedItem().toString());
        plot3d.setAxisLabel(1, yAxisColumn.getSelectedItem().toString());
        plot3d.setAxisLabel(2, plotsColumn.getSelectedItemsString());
    }
    
    private boolean getColumnsFilters(ArrayList<String> columns) {
        boolean ret = true;
        String logThtlAngleColName = Config.getLogThrottleAngleColumnName();
        logRpmColName = Config.getLogRpmColumnName();
        logTimeColName = Config.getLogTimeColumnName();
        logThtlAngleColIdx = columns.indexOf(logThtlAngleColName);
        int logRpmColIdx = columns.indexOf(logRpmColName);
        int logTimeColIdx = columns.indexOf(logTimeColName);
        if (logThtlAngleColIdx == -1) {
            String lcColName;
            for (int i = 0; i < columns.size(); ++i) {
                lcColName = columns.get(i).toLowerCase();
                if (lcColName.matches(thrtlMatchString)) {
                    logThtlAngleColIdx = i;
                    logThtlAngleColName = columns.get(i);
                    break;
                }
            }
        }
        if (logRpmColIdx == -1) {
            String lcColName;
            for (int i = 0; i < columns.size(); ++i) {
                lcColName = columns.get(i).toLowerCase();
                if (lcColName.matches(rpmMatchString) || lcColName.matches(engineSpeedMatchString)) {
                    logRpmColIdx = i;
                    logRpmColName = columns.get(i);
                    break;
                }
            }
        }
        if (logTimeColIdx == -1) {
            String lcColName;
            for (int i = 0; i < columns.size(); ++i) {
                lcColName = columns.get(i).toLowerCase();
                if (lcColName.matches(timeMatchString)) {
                    logTimeColIdx = i;
                    logTimeColName = columns.get(i);
                    break;
                }
            }
        }
        wotPoint = Config.getLogWOTStationaryPointValue();

        JComboBox<String> thrlColumn = new JComboBox<String>();
        JComboBox<String> rpmColumn = new JComboBox<String>();
        JComboBox<String> timeColumn = new JComboBox<String>();
        JSpinner wotPointSpinner = new JSpinner(new SpinnerNumberModel(wotPoint, 50, 100, 5));
        TreeSet<String> sortedColumns = new TreeSet<String>(columns);
        for (String col : sortedColumns) {
            thrlColumn.addItem(col);
            rpmColumn.addItem(col);
            timeColumn.addItem(col);
        }
        thrlColumn.setSelectedItem(logThtlAngleColName);
        rpmColumn.setSelectedItem(logRpmColName);
        timeColumn.setSelectedItem(logTimeColName);
       
        JPanel selectionPanel = new JPanel();
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        selectionPanel.setLayout(new GridLayout(0, 2, 5, 7));
        selectionPanel.add(new JLabel(ColumnsFiltersSelection.timeLabelText, JLabel.RIGHT));
        selectionPanel.add(timeColumn);
        selectionPanel.add(new JLabel(ColumnsFiltersSelection.rpmLabelText, JLabel.RIGHT));
        selectionPanel.add(rpmColumn);
        selectionPanel.add(new JLabel(ColumnsFiltersSelection.thrtlAngleLabelText, JLabel.RIGHT));
        selectionPanel.add(thrlColumn);
        selectionPanel.add(new JLabel(ColumnsFiltersSelection.wotStationaryLabelText, JLabel.RIGHT));
        selectionPanel.add(wotPointSpinner);
        
        if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null, selectionPanel, "Settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE))
            ret = false;
        else {
            logThtlAngleColName = thrlColumn.getSelectedItem().toString();
            logRpmColName = rpmColumn.getSelectedItem().toString();
            logTimeColName = timeColumn.getSelectedItem().toString();
            wotPoint = Integer.valueOf(wotPointSpinner.getValue().toString());
            logThtlAngleColIdx = columns.indexOf(logThtlAngleColName);
            logRpmColIdx = columns.indexOf(logRpmColName);
            logTimeColIdx = columns.indexOf(logTimeColName);
            Config.setLogWOTStationaryPointValue(wotPoint);
            if (logThtlAngleColIdx == -1) {
                Config.setLogThrottleAngleColumnName(Config.NO_NAME);
                ret = false;
            }
            else
                Config.setLogThrottleAngleColumnName(logThtlAngleColName);
            if (logRpmColIdx == -1) {
                Config.setLogRpmColumnName(Config.NO_NAME);
                ret = false;
            }
            else
                Config.setLogRpmColumnName(logRpmColName);
            if (logTimeColIdx == -1) {
                Config.setLogTimeColumnName(Config.NO_NAME);
                ret = false;
            }
            else
                Config.setLogTimeColumnName(logTimeColName);
        }
        return ret;
    }

    private void selectWotLogFiles() {
        fileChooser.setMultiSelectionEnabled(true);
        if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(this))
            return;
        loadWotLogFiles();
    }

    private void setWotNodesChecked(boolean checked) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)wotTree.getModel().getRoot();
        if (root.getChildCount() > 0) {
            DefaultMutableTreeNode fileNode = (DefaultMutableTreeNode) root.getChildAt(0);
            for (int i = 0; i < fileNode.getChildCount(); ++i) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileNode.getChildAt(i);
                CheckBoxNodeData checkbox = (CheckBoxNodeData) node.getUserObject();
                checkbox.setChecked(checked);
            }
        }
        wotTree.repaint();
    }

    private void getWotYAxisGroups() {
        // check if there are any selected columns
        List<String> dataColNames = wotPlotsColumn.getSelectedItems();
        if (dataColNames == null || dataColNames.size() == 0)
            return;
        // remove columns (and empty) groups for any unselected columns
        TreeSet<String> colNames = new TreeSet<String>();
        for (int i = 0; i < dataColNames.size(); ++i)
            colNames.add(dataColNames.get(i));
        for (Iterator<TreeSet<String>> it = wotYAxisGroups.iterator(); it.hasNext();) {
            TreeSet<String> group = it.next();
            for (Iterator<String> iter = group.iterator(); iter.hasNext();) {
                String colName = iter.next();
                if (!colNames.contains(colName))
                    iter.remove();
            }
            if (group.size() == 0)
                it.remove();
        }
        // add newly selected columns (and groups)
        for (String colName : colNames) {
            boolean grouped = false;
            for (int i = 0; !grouped && i < wotYAxisGroups.size(); ++i) {
                TreeSet<String> group = wotYAxisGroups.get(i);
                if (group.contains(colName))
                    grouped = true;
            }
            if (!grouped) {
                TreeSet<String> group = new TreeSet<String>();
                group.add(colName);
                wotYAxisGroups.add(group);
            }
        }
    }

    private void loadWotLogFiles() {
        File[] files = fileChooser.getSelectedFiles();
        if (files.length < 1)
            return;
        filesData = new HashMap<String, ArrayList<HashMap<String, ArrayList<Double>>>>();
        wotYAxisGroups = new ArrayList<TreeSet<String>>();
        TreeSet<String> columns = new TreeSet<String>();
        ArrayList<String> colNames = null;
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)wotTree.getModel().getRoot();
        root.removeAllChildren();
        ((DefaultTreeModel )wotTree.getModel()).reload(root);
        wotPlotsColumn.removeAllItems();
        wotPlotsColumn.setText("");
        clearWotPlots();
        double val;
        int i = 0;
        int row = 0;
        for (File file : files) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsoluteFile()), Config.getEncoding()));
                String line = null;
                String [] elements = null;
                while ((line = br.readLine()) != null && (elements = line.split(Utils.fileFieldSplitter, -1)) != null && elements.length < 2)
                    continue;
                if (line.charAt(line.length() - 1) == ',')
                    Arrays.copyOf(elements, elements.length - 1);
                colNames = new ArrayList<String>(Arrays.asList(elements));                    
                if (false == getColumnsFilters(colNames))
                    continue;
                DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode("<html><u>" + file.getName() + "</u></html>");
                ArrayList<HashMap<String, ArrayList<Double>>> pulls = new ArrayList<HashMap<String, ArrayList<Double>>>();
                HashMap<String, ArrayList<Double>> pullData = new HashMap<String, ArrayList<Double>>();
                ArrayList<Double> columnData;
                String[] flds;
                row = 0;
                int pullRows = 0;
                boolean wotFlag = true;
                while ((line = br.readLine()) != null) {
                    if (line.length() > 0 && line.charAt(line.length() - 1) == ',')
                        line = line.substring(0, line.length() - 1);
                    flds = line.split(Utils.fileFieldSplitter, -1);
                    val = Double.valueOf(flds[logThtlAngleColIdx]);
                    if (row == 0 && val < 99)
                        wotFlag = false;
                    if (val < wotPoint) {
                        if (wotFlag == true) {
                            wotFlag = false;
                            if (pullRows >= 10) {
                                pulls.add(pullData);
                                pullData = new HashMap<String, ArrayList<Double>>();
                                fileNode.add(new DefaultMutableTreeNode(new CheckBoxNodeData(pullIndexReplaceString + pulls.size(), true)));
                            }
                        }
                    }
                    else {
                        boolean newPullData = false;
                        if (wotFlag == false || row == 0) {
                            wotFlag = true;
                            newPullData = true;
                            pullRows = 0;
                        }
                        pullRows += 1;
                        for (i = 0; i < colNames.size(); ++i) {
                            if (newPullData) {
                                columnData = new ArrayList<Double>();
                                pullData.put(colNames.get(i), columnData);
                            }
                            else
                                columnData = pullData.get(colNames.get(i));
                            if (flds[i].matches(Utils.tmRegex)) {
                                if (row == 0)
                                    Utils.resetBaseTime(flds[i]);
                                columnData.add((double)Utils.parseTime(flds[i]));
                            }
                            else
                                columnData.add(Utils.parseValue(flds[i]));
                        }
                    }
                    row += 1;
                }
                if (wotFlag == true) {
                    if (pullRows >= 10) {
                        pulls.add(pullData);
                        pullData = new HashMap<String, ArrayList<Double>>();
                        fileNode.add(new DefaultMutableTreeNode(new CheckBoxNodeData(pullIndexReplaceString + pulls.size(), true)));
                    }
                }
                if (pulls.size() > 0) {
                    root.add(fileNode);
                    TreePath path = new TreePath(root);
                    wotTree.expandPath(path.pathByAddingChild(fileNode));                        
                    filesData.put(file.getName(), pulls);
                    columns.addAll(colNames);
                }
            }
            catch (NumberFormatException ne) {
                logger.error(ne);
                JOptionPane.showMessageDialog(null, "Error parsing number at " + file.getName() + ", column " + colNames.get(i) + " line " + row + ": " + ne, "Error processing file", JOptionPane.ERROR_MESSAGE);
                return;
            }
            catch (Exception e) {
                logger.error(e);
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error processing file " + file.getName(), JOptionPane.ERROR_MESSAGE);
                return;
            }
            finally {
                if (br != null) {
                    try {
                        br.close();
                    }
                    catch (IOException e) {
                        logger.error(e);
                    }
                }
            }
        }
        if (columns.size() > 0) {
            for (String col : columns)
                wotPlotsColumn.addItem(col);
            ((DefaultTreeModel )wotTree.getModel()).reload(root);
            for (row = 0; row < wotTree.getRowCount(); ++row)
                wotTree.expandRow(row);
            linkYAxis.setEnabled(true);
        }
        else {
            JOptionPane.showMessageDialog(null, "No WOT pulls were found in the log file(s)", "Oops", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    protected void clearWotPlots() {
        for (int i = 0; i < wotPlot.getDatasetCount(); ++i) {
            XYSeriesCollection dataset = (XYSeriesCollection)wotPlot.getDataset(i);
            if (dataset != null) {
                dataset.removeAllSeries();
                wotPlot.setDataset(i, null);
                wotPlot.setRangeAxis(i, null);
            }
        }
        wotPlot.clearRangeMarkers();
        wotMarker.clearLabels(true);
    }
    
    private void viewWotPlotsByTime() {
        getWotYAxisGroups();
        if (wotYAxisGroups.size() == 0) {
            JOptionPane.showMessageDialog(null, "Please select columns to plot", "Invalid parameters", JOptionPane.ERROR_MESSAGE);
            return;
        }
        clearWotPlots();
        
        ArrayList<HashMap<String, ArrayList<Double>>> filePulls;
        HashMap<String, ArrayList<Double>> pullData;
        ArrayList<Double> rpmData;
        ArrayList<Double> timeData;
        ArrayList<Double> colData = null;
        DefaultMutableTreeNode fileNode;
        CheckBoxNodeData pullNode;
        String fileName;
        String pullName;
        int pullIdx;
        int idx;
        int rpm;
        int maxrpm = 0;
        double time = 0;
        double newtime = 0;
        double maxtime = 0;
        double timeOffset = 0;
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            // sort all pulls by RPM
            TreeMap<Integer, ArrayList<HashMap<String, HashMap<String, ArrayList<Double>>>>> pullsByRpm = new TreeMap<Integer, ArrayList<HashMap<String, HashMap<String, ArrayList<Double>>>>>();
            ArrayList<HashMap<String, HashMap<String, ArrayList<Double>>>> pulls;
            DefaultMutableTreeNode root = (DefaultMutableTreeNode)wotTree.getModel().getRoot();
            for (idx = 0; idx < root.getChildCount(); ++idx) {
                fileNode = (DefaultMutableTreeNode)root.getChildAt(idx);
                fileName = fileNode.getUserObject().toString().replaceAll(fileNameReplaceString, "");
                for (int j = 0; j < fileNode.getChildCount(); ++j) {
                    pullNode = (CheckBoxNodeData)((DefaultMutableTreeNode)fileNode.getChildAt(j)).getUserObject();
                    if (!pullNode.isChecked())
                        continue;
                    pullName = pullNode.getText();
                    filePulls = filesData.get(fileName);
                    pullIdx = Integer.parseInt(pullName.replaceAll(pullIndexReplaceString, "")) - 1;
                    pullData = filePulls.get(pullIdx);
                    pullName = " [" + pullName + ": " + fileName + "]";
                    HashMap<String, HashMap<String, ArrayList<Double>>> pullDataByName = new HashMap<String, HashMap<String, ArrayList<Double>>>();
                    pullDataByName.put(pullName, pullData);
                    rpmData = pullData.get(logRpmColName);
                    rpm = rpmData.get(0).intValue();
                    pulls = pullsByRpm.get(rpm);
                    if (pulls == null) {
                        pulls = new ArrayList<HashMap<String, HashMap<String, ArrayList<Double>>>>();
                        pullsByRpm.put(rpm, pulls);
                    }
                    pulls.add(pullDataByName);
                }
            }
            // Reset pulls time by aligning RPM
            ArrayList<HashMap<String, HashMap<String, ArrayList<Double>>>> allpulls = new ArrayList<HashMap<String, HashMap<String, ArrayList<Double>>>>();
            while (pullsByRpm.size() > 0) {
                time = newtime;
                newtime = 0;
                rpm = pullsByRpm.firstKey();
                maxrpm = rpm;
                pulls = pullsByRpm.remove(rpm); 
                for (idx = 0; idx < pulls.size(); ++idx) {
                    pullData = (pulls.get(idx)).entrySet().iterator().next().getValue();
                    rpmData = pullData.get(logRpmColName);
                    timeData = pullData.get(logTimeColName);
                    colData = new ArrayList<Double>();
                    for (int j = 0; j < rpmData.size(); ++j) {
                        double tm = timeData.get(j);
                        if (j == 0)
                            timeOffset = tm - time;
                        tm = tm - timeOffset;
                        colData.add(tm);
                        rpm = rpmData.get(j).intValue();
                        if (pullsByRpm.size() > 0 && newtime == 0 && rpm >= pullsByRpm.firstKey())
                            newtime = tm;
                    }
                    maxrpm = Math.max(maxrpm, rpmData.get(rpmData.size() - 1).intValue());
                    maxtime = Math.max(maxtime, colData.get(colData.size() - 1));
                    pullData.put("xaxis", colData);
                }
                if (pullsByRpm.size() > 0 && newtime == 0)
                    newtime = (maxtime * pullsByRpm.firstKey()) / maxrpm;
                allpulls.addAll(pulls);
            }
            // Plot data
            for (int i = 0; i < wotYAxisGroups.size(); ++i) {
                TreeSet<String> group = wotYAxisGroups.get(i);
                XYSeriesCollection dataset = new XYSeriesCollection();
                String yAxisName = "";
                for (String yAxisColName : group) {
                    yAxisName += (yAxisName.isEmpty() ? yAxisColName : ", " + yAxisColName);
                    for (idx = 0; idx < allpulls.size(); ++idx) {
                        Map.Entry<String, HashMap<String, ArrayList<Double>>> keyval = allpulls.get(idx).entrySet().iterator().next();
                        pullName = yAxisColName + keyval.getKey();
                        pullData = keyval.getValue();
                        timeData = pullData.get("xaxis");
                        colData = pullData.get(yAxisColName);
                        if (colData != null) {
                            XYSeries series = new XYSeries(pullName);
                            series.setDescription(pullName);
                            for (int k = 0; k < timeData.size(); ++k)
                                series.add(Double.valueOf(timeData.get(k)), Double.valueOf(colData.get(k)));
                            dataset.addSeries(series);
                        }
                    }
                }
                NumberAxis yAxis = new NumberAxis(yAxisName);
                yAxis.setAutoRangeIncludesZero(false);
                yAxis.setTickLabelPaint(Color.WHITE);
                yAxis.setLabelPaint(Color.LIGHT_GRAY);
                XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer();
                lineRenderer.setBaseShapesVisible(showWotCurvePoints);
                wotPlot.setRenderer(i, lineRenderer);
                wotPlot.setRangeAxis(i, yAxis, false);
                wotPlot.setDataset(i, dataset);
                wotPlot.mapDatasetToRangeAxis(i, i);
                wotPlot.mapDatasetToDomainAxis(i, 0);
                wotPlot.setRangeAxisLocation(i, (i % 2 == 0 ? AxisLocation.BOTTOM_OR_LEFT : AxisLocation.BOTTOM_OR_RIGHT));
            }
        }
        finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    private void viewWotPlotsByRpm(boolean skipDrops) {
        getWotYAxisGroups();
        if (wotYAxisGroups.size() == 0) {
            JOptionPane.showMessageDialog(null, "Please select columns to plot", "Invalid parameters", JOptionPane.ERROR_MESSAGE);
            return;
        }
        clearWotPlots();
        
        ArrayList<HashMap<String, ArrayList<Double>>> filePulls;
        HashMap<String, ArrayList<Double>> pullData;
        ArrayList<Double> rpmData;
        ArrayList<Double> colData;
        DefaultMutableTreeNode fileNode;
        CheckBoxNodeData pullNode;
        String fileName;
        String pullName;
        int pullIdx;
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            for (int i = 0; i < wotYAxisGroups.size(); ++i) {
                TreeSet<String> group = wotYAxisGroups.get(i);
                XYSeriesCollection dataset = new XYSeriesCollection();
                String yAxisName = "";
                for (String yAxisColName : group) {
                    yAxisName += (yAxisName.isEmpty() ? yAxisColName : ", " + yAxisColName);
                    DefaultMutableTreeNode root = (DefaultMutableTreeNode)wotTree.getModel().getRoot();
                    for (int idx = 0; idx < root.getChildCount(); ++idx) {
                        fileNode = (DefaultMutableTreeNode)root.getChildAt(idx);
                        fileName = fileNode.getUserObject().toString().replaceAll(fileNameReplaceString, "");
                        for (int j = 0; j < fileNode.getChildCount(); ++j) {
                            pullNode = (CheckBoxNodeData)((DefaultMutableTreeNode)fileNode.getChildAt(j)).getUserObject();
                            if (!pullNode.isChecked())
                                continue;
                            pullName = pullNode.getText();
                            filePulls = filesData.get(fileName);
                            pullIdx = Integer.parseInt(pullName.replaceAll(pullIndexReplaceString, "")) - 1;
                            pullData = filePulls.get(pullIdx);
                            rpmData = pullData.get(logRpmColName);
                            colData = pullData.get(yAxisColName);
                            if (colData != null) {
                                pullName = yAxisColName + " [" + pullName + ": " + fileName + "]";
                                XYSeries series = new XYSeries(pullName);
                                series.setDescription(pullName);
                                for (int k = 0; k < rpmData.size(); ++k) {
                                    if (skipDrops) {
                                        if (k > 0 && rpmData.get(k) > rpmData.get(k - 1))
                                            series.add(Double.valueOf(rpmData.get(k)), Double.valueOf(colData.get(k)));
                                    }
                                    else
                                        series.add(Double.valueOf(rpmData.get(k)), Double.valueOf(colData.get(k)));
                                }
                                dataset.addSeries(series);
                            }
                        }
                    }
                }
                NumberAxis yAxis = new NumberAxis(yAxisName);
                yAxis.setAutoRangeIncludesZero(false);
                yAxis.setTickLabelPaint(Color.WHITE);
                yAxis.setLabelPaint(Color.LIGHT_GRAY);
                XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer();
                lineRenderer.setBaseShapesVisible(showWotCurvePoints);
                wotPlot.setRenderer(i, lineRenderer);
                wotPlot.setRangeAxis(i, yAxis, false);
                wotPlot.setDataset(i, dataset);
                wotPlot.mapDatasetToRangeAxis(i, i);
                wotPlot.mapDatasetToDomainAxis(i, 0);
                wotPlot.setRangeAxisLocation(i, (i % 2 == 0 ? AxisLocation.BOTTOM_OR_LEFT : AxisLocation.BOTTOM_OR_RIGHT));
            }
        }
        finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    public boolean setMarkers(double x, boolean isLeft) {
        xMarker.clearLabels(false);
        if (x < 0 || x >= logDataTable.getRowCount())
            return false;
        XYSeries series = null;
        if (isLeft) {
            xMarker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
            xMarker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        }
        else {
            xMarker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
            xMarker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
        }
        if (rpmDataset.getSeriesCount() > 0 && rpmPlotRenderer.isSeriesVisible(0)) {
            series = rpmDataset.getSeries(0);
            xMarker.addLabel(series.getDescription() + ": ", rpmPlotRenderer.getSeriesPaint(0), series.getY((int)x).toString(), false);
        }
        for (int i = 0; i < dataset.getSeriesCount(); ++i) {
            if (plotRenderer.isSeriesVisible(i)) {
                series = dataset.getSeries(i);
                xMarker.addLabel(series.getDescription() + ": ", plotRenderer.getSeriesPaint(i), series.getY((int)x).toString(), false);
            }
        }
        xMarker.setValue(x);
        return (series != null);
    }
    
    private void exportSelectedWotPulls() {
        ArrayList<HashMap<String, ArrayList<Double>>> filePulls;
        HashMap<String, ArrayList<Double>> pullData;
        DefaultMutableTreeNode fileNode;
        CheckBoxNodeData pullNode;
        String fileName = null;
        String nodeName = null;
        String dirName = null;
        int pullIdx;
        int cnt;
        int i, j;

        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            DefaultMutableTreeNode root = (DefaultMutableTreeNode)wotTree.getModel().getRoot();
            for (int idx = 0; idx < root.getChildCount(); ++idx) {
                fileNode = (DefaultMutableTreeNode)root.getChildAt(idx);
                fileName = fileNode.getUserObject().toString().replaceAll(fileNameReplaceString, "");
                for (cnt = 0; cnt < fileNode.getChildCount(); ++cnt) {
                    pullNode = (CheckBoxNodeData)((DefaultMutableTreeNode)fileNode.getChildAt(cnt)).getUserObject();
                    if (!pullNode.isChecked())
                        continue;
                    filePulls = filesData.get(fileName);
                    nodeName = pullNode.getText();
                    pullIdx = Integer.parseInt(nodeName.replaceAll(pullIndexReplaceString, "")) - 1;
                    pullData = filePulls.get(pullIdx);
                    if (pullData.size() == 0)
                        continue;
                    if (dirName == null) {
                        JFileChooser fc = new JFileChooser();
                        fc.setCurrentDirectory((lastPullExportDir == null ? fileChooser.getCurrentDirectory() : lastPullExportDir));
                        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        fc.setAcceptAllFileFilterUsed(false);
                        if (JFileChooser.APPROVE_OPTION != fc.showSaveDialog(null))
                            return;
                        File dir = fc.getSelectedFile();
                        if (!dir.exists() || !dir.isDirectory()) {
                            JOptionPane.showMessageDialog(null, "Directory doesn't exist: " + dir.getAbsolutePath(), "Invalid directory", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        lastPullExportDir = dir;
                        dirName = dir.getAbsolutePath();
                    }
                    ArrayList<String> columns = new ArrayList<String>();
                    ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
                    for (Map.Entry<String, ArrayList<Double>> entry : pullData.entrySet()) {
                        columns.add(entry.getKey());
                        data.add(entry.getValue());
                    }
                    Writer out = null;
                    try {
                        File file = new File(dirName + File.separator + fileName + "_" + nodeName + ".csv");
                        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), Config.getEncoding()));
                        int lidx = columns.size() - 1;
                        int rows = data.get(0).size();
                        for (i = 0; i <= lidx; ++i)
                            out.write(columns.get(i) + (i < lidx ? "," : ""));
                        out.write("\n");
                        for (j = 0; j < rows; ++j) {
                            for (i = 0; i <= lidx; ++i)
                                out.write(data.get(i).get(j) + (i < lidx ? "," : ""));
                            out.write("\n");
                        }
                    }
                    catch (Exception e) {
                        logger.error(e);
                    }
                    finally {
                        if (out != null) {
                            try {
                                out.close();
                            }
                            catch (IOException e) {
                                logger.error(e);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            logger.error(e);
        }
        finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    public void disposeLogView() {
        logPlayButton.setEnabled(true);
        logPlayWindow.dispose();
        logPlayWindow = null;
        startMarker = null;
        endMarker = null;
        disableMouseListener();
        enableMouseListener();
        chartPanel.repaint();
    }
    
    public void enableMouseListener() {
        chartPanel.addChartMouseListener(chartMouseListener);
    }
    
    public void disableMouseListener() {
        chartPanel.removeChartMouseListener(chartMouseListener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loadButton)
            selectLogFile();
        else if (e.getSource() == printButton)
            logDataTable.print(new PrintProperties());
        else if (e.getSource() == previewButton)
            logDataTable.printPreview(new PrintProperties());
        else if (e.getSource() == findButton) {
            if (findWindow != null)
                findWindow.dispose();
            findWindow = new DBTFindFrame(SwingUtilities.windowForComponent(this), logDataTable, true);
        }
        else if (e.getSource() == replaceButton) {
            if (findWindow != null)
                findWindow.dispose();
            findWindow = new DBTFindFrame(SwingUtilities.windowForComponent(this), logDataTable, false);
        }
        else if (e.getSource() == filterButton) {
            String filterString = filterText.getText();
            if (filterString != null && !"".equals(filterString) && !compareCombo.getSelectedItem().toString().isEmpty()) {
                try {
                    CompareFilter filter = new CompareFilter();
                    filter.setCondition(CompareFilter.Condition.EQUAL);
                    if (compareCombo.getSelectedItem().toString().equals(greater))
                        filter.setCondition(CompareFilter.Condition.GREATER);
                    if (compareCombo.getSelectedItem().toString().equals(greaterEqual))
                        filter.setCondition(CompareFilter.Condition.GREATER_EQUAL);
                    else if (compareCombo.getSelectedItem().toString().equals(less))
                        filter.setCondition(CompareFilter.Condition.LESS);
                    else if (compareCombo.getSelectedItem().toString().equals(lessEqual))
                        filter.setCondition(CompareFilter.Condition.LESS_EQUAL);
                    filter.setFilter(filterText.getText());
                    filter.setColumn(logDataTable.getColumnByHeaderName((String)selectionCombo.getSelectedItem()).getModelIndex());
                    logDataTable.filter(filter);
                    updateChart();
                }
                catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid numeric value: " + filterText.getText(), "Invalid value", JOptionPane.ERROR_MESSAGE);
                }
            }
            else {
                filterText.setText("");
                compareCombo.setSelectedItem("");
                logDataTable.filter(null);
                updateChart();
            }
        }
        else if (e.getSource() == viewButton) {
            GridBagLayout gbl = (GridBagLayout)logViewPanel.getLayout();
            if (viewButton.getText().startsWith("Headers")) {
                Dimension d = viewButton.getSize();
                viewButton.setMinimumSize(d);
                viewButton.setPreferredSize(d);
                viewButton.setMaximumSize(d);
                gbl.rowWeights = new double[]{0.0, 1.0};
                logDataTable.setVisible(false);
                headerScrollPane.setVisible(true);
                viewButton.setText("Grid View");
            }
            else {
                gbl.rowWeights = new double[]{1.0, 0.0};
                logDataTable.setVisible(true);
                headerScrollPane.setVisible(false);
                viewButton.setText("Headers View");
            }
        }
        else if (e.getSource() == logPlayButton) {
            if (logDataTable.getRowCount() > 1) {
                if (logPlayWindow != null)
                    disposeLogView();
                logPlayWindow = new LogPlay(this);
                logPlayButton.setEnabled(false);
            }
        }
        else if ("showpts".equals(e.getActionCommand())) {
            showWotCurvePoints = ((JCheckBox)e.getSource()).isSelected();
            for (int i = 0; i < wotPlot.getRendererCount(); ++i) {
                XYLineAndShapeRenderer lineRenderer = (XYLineAndShapeRenderer)wotPlot.getRenderer(i);
                XYSeriesCollection dataset = (XYSeriesCollection)wotPlot.getDataset(i);
                if (lineRenderer == null || dataset == null)
                    continue;
                lineRenderer.setBaseShapesVisible(showWotCurvePoints);
            }
        }
        else if ("linkyaxis".equals(e.getActionCommand())) {
        	WotPullsGroups grpSel = new WotPullsGroups();
        	grpSel.getGroups(wotPlotsColumn, wotYAxisGroups);
        }
        else if ("export".equals(e.getActionCommand()))
            exportSelectedWotPulls();
        else if ("view".equals(e.getActionCommand()))
            view3dPlots();
        else if ("viewWot".equals(e.getActionCommand())) {
            Enumeration<AbstractButton> buttons = wotRbGroup.getElements();
            if (((JRadioButton)buttons.nextElement()).isSelected()) {
                viewWotPlotsByRpm(false);
                wotPlot.getDomainAxis(0).setLabel(rpmAxisName);
            }
            else if (((JRadioButton)buttons.nextElement()).isSelected()) {
                viewWotPlotsByRpm(true);
                wotPlot.getDomainAxis(0).setLabel(rpmAxisName);
            }
            else {
                viewWotPlotsByTime();
                wotPlot.getDomainAxis(0).setLabel(timeAxisName);
            }
        }
        else if ("loadWot".equals(e.getActionCommand()))
            selectWotLogFiles();
    }
    
    protected void onDroppedFiles(List<File> files) {
        if (files.size() > 0) {
            int idx = getSelectedIndex();
            if (idx == 0) {
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.setCurrentDirectory(files.get(0));
                fileChooser.setSelectedFile(files.get(0));
                fileChooser.approveSelection();
                loadLogFile();
            }
            else if (idx == 2) {
                fileChooser.setMultiSelectionEnabled(true);
                fileChooser.setCurrentDirectory(files.get(0));
                fileChooser.setSelectedFiles((File[])files.toArray());
                fileChooser.approveSelection();
                loadWotLogFiles();
            }
        }
    }

}

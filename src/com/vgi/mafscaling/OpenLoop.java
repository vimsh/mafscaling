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
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;

public class OpenLoop extends AMafScaling {
    private static final long serialVersionUID = 2988105467764335997L;
    private static final Logger logger = Logger.getLogger(OpenLoop.class);
    private static final String SaveDataFileHeader = "[open_loop run data]";
    private static final String RunTableName = "Run ";
    private static final String Y2AxisName = "AFR Error (%)";
    private static final String rpmAxisName = "RPM";
    private static final String runDataName = "AFR Error";
    private static final int RunCount = 12;
    private static final int RunRowsCount = 200;
    
    private double minMafV = Config.getMafVMinimumValue();
    private double afrErrPrct = Config.getWidebandAfrErrorPercentValue();
    private double minWotEnrichment = Config.getWOTEnrichmentValue();
    private int wotPoint = Config.getWOTStationaryPointValue();
    private int afrRowOffset = Config.getWBO2RowOffset();
    private int skipRowsOnTransition = Config.getOLCLTransitionSkipRows();
    
    private int logThtlAngleColIdx = -1;
    private int logAfLearningColIdx = -1;
    private int logAfCorrectionColIdx = -1;
    private int logMafvColIdx = -1;
    private int logAfrColIdx = -1;
    private int logRpmColIdx = -1;
    private int logLoadColIdx = -1;
    private int logCommandedAfrCol = -1;
    
    private JCheckBox checkBoxMafRpmData = null;
    private ArrayList<Double> afrArray = new ArrayList<Double>();
    private final JTable[] runTables = new JTable[RunCount];

    public OpenLoop(int tabPlacement, PrimaryOpenLoopFuelingTable table, MafCompare comparer) {
        super(tabPlacement, table, comparer);
        runData = new XYSeries(runDataName);
        initialize();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // DATA TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    protected void createRunPanel(JPanel dataPanel) {
        JScrollPane dataScrollPane = new JScrollPane();
        GridBagConstraints gbc_dataScrollPane = new GridBagConstraints();
        gbc_dataScrollPane.weightx = 1.0;
        gbc_dataScrollPane.weighty = 1.0;
        gbc_dataScrollPane.fill = GridBagConstraints.BOTH;
        gbc_dataScrollPane.gridx = 0;
        gbc_dataScrollPane.gridy = 3;
        dataPanel.add(dataScrollPane, gbc_dataScrollPane);
        
        JPanel dataRunPanel = new JPanel();
        dataScrollPane.setViewportView(dataRunPanel);
        GridBagLayout gbl_dataRunPanel = new GridBagLayout();
        gbl_dataRunPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_dataRunPanel.rowHeights = new int[] {0};
        gbl_dataRunPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        gbl_dataRunPanel.rowWeights = new double[]{0.0};
        dataRunPanel.setLayout(gbl_dataRunPanel);

        createRunTables(dataRunPanel);
    }
    
    private void createRunTables(JPanel dataRunPanel) {
        GridBagConstraints gbc_run = new GridBagConstraints();
        gbc_run.anchor = GridBagConstraints.PAGE_START;
        gbc_run.insets = new Insets(0, 2, 0, 2);
        for (int i = 0; i < RunCount; ++i) {
            runTables[i] = new JTable();
            JTable table = runTables[i];
            table.getTableHeader().setReorderingAllowed(false);
            table.setModel(new DefaultTableModel(RunRowsCount, 3));
            table.setColumnSelectionAllowed(true);
            table.setCellSelectionEnabled(true);
            table.setBorder(new LineBorder(new Color(0, 0, 0)));
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
            table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            table.getColumnModel().getColumn(0).setHeaderValue("<html><center>Engine<br>Speed<br>(RPM)<br></center></html>");
            table.getColumnModel().getColumn(1).setHeaderValue("<html><center>MAF<br>Sensor<br>Voltage<br></center></html>");
            table.getColumnModel().getColumn(2).setHeaderValue("<html><center>AFR<br>Error<br>%<br></center></html>");
            Utils.initializeTable(table, ColumnWidth);
            excelAdapter.addTable(table, true, false);

            gbc_run.gridx = i;
            gbc_run.gridy = 0;
            dataRunPanel.add(table.getTableHeader(), gbc_run);
            gbc_run.gridy = 1;
            dataRunPanel.add(table, gbc_run);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE CHART TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    protected void createGraghTab() {
        JPanel cntlPanel = new JPanel();
        JPanel plotPanel = createGraphPlotPanel(cntlPanel);
        add(plotPanel, "<html><div style='text-align: center;'>C<br>h<br>a<br>r<br>t</div></html>");
        
        GridBagLayout gbl_cntlPanel = new GridBagLayout();
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0, 0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);
        
        GridBagConstraints gbc_check = new GridBagConstraints();
        gbc_check.insets = insets2;
        gbc_check.gridx = 0;
        gbc_check.gridy = 0;
        
        checkBoxMafRpmData = new JCheckBox("MafV/RPM");
        checkBoxMafRpmData.setActionCommand("mafrpm");
        checkBoxMafRpmData.addActionListener(this);
        cntlPanel.add(checkBoxMafRpmData, gbc_check);

        gbc_check.gridx++;
        checkBoxRunData = new JCheckBox("AFR Error");
        checkBoxRunData.setActionCommand("rdata");
        checkBoxRunData.addActionListener(this);
        cntlPanel.add(checkBoxRunData, gbc_check);

        gbc_check.gridx++;
        createGraphCommonControls(cntlPanel, gbc_check.gridx);

        createChart(plotPanel, Y2AxisName);        
        createMafSmoothingPanel(plotPanel);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // COMMON ACTIONS RELATED FUNCTIONS
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void actionPerformed(ActionEvent e) {
        if (checkActionPerformed(e))
            return;
        if ("mafrpm".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected()) {
                clearNotRunDataCheckboxes();
                if (!plotMafVRpmData())
                    checkBox.setSelected(false);
            }
            else
                runData.clear();
            setRanges();
        }
        else if ("rdata".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected()) {
                if (checkBoxMafRpmData.isSelected()) {
                    checkBoxMafRpmData.setSelected(false);
                    runData.clear();
                }
                if (!plotRunData())
                    checkBox.setSelected(false);
            }
            else
                runData.clear();
            setRanges();
        }
        else if ("current".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected()) {
                if (checkBoxMafRpmData.isSelected()) {
                    checkBoxMafRpmData.setSelected(false);
                    runData.clear();
                }
                if (!plotCurrentMafData())
                    checkBox.setSelected(false);
            }
            else
                currMafData.clear();
            setRanges();
        }
        else if ("corrected".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected()) {
                if (checkBoxMafRpmData.isSelected()) {
                    checkBoxMafRpmData.setSelected(false);
                    runData.clear();
                }
                if (!setCorrectedMafData())
                    checkBox.setSelected(false);
            }
            else
                corrMafData.clear();
            setRanges();
        }
        else if ("smoothed".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected()) {
                if (checkBoxMafRpmData.isSelected()) {
                    checkBoxMafRpmData.setSelected(false);
                    runData.clear();
                }
                if (!setSmoothedMafData())
                    checkBox.setSelected(false);
            }
            else
                smoothMafData.clear();
            setRanges();
        }
        else if ("smoothing".equals(e.getActionCommand())) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            if (checkBox.isSelected())
                enableSmoothingView(true);
            else
                enableSmoothingView(false);
            setRanges();
        }
    }
    
    protected void clearRunTables() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            for (int i = 0; i < runTables.length; ++i) {
                while (RunRowsCount < runTables[i].getRowCount())
                    Utils.removeRow(RunRowsCount, runTables[i]);
                Utils.clearTable(runTables[i]);
            }
        }
        finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    protected void clearData() {
        super.clearData();
        afrArray.clear();
    }
    
    protected void clearChartCheckBoxes() {
        super.clearChartCheckBoxes();
        checkBoxMafRpmData.setSelected(false);
    }
    
    protected void calculateMafScaling() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            clearData();
            clearChartData();
            clearChartCheckBoxes();

            TreeMap<Integer, ArrayList<Double>> result = new TreeMap<Integer, ArrayList<Double>>();
            if (!getMafTableData(voltArray, gsArray))
                return;
            if (!sortRunData(result) || result.isEmpty())
                return;
            calculateCorrectedGS(result);
            setCorrectedMafData();
            
            smoothGsArray.addAll(gsCorrected);
            checkBoxCorrectedMaf.setSelected(true);
            
            setXYTable(mafSmoothingTable, voltArray, smoothGsArray);

            setRanges();
            setSelectedIndex(1);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            JOptionPane.showMessageDialog(null, "Error: " + e, "Error", JOptionPane.ERROR_MESSAGE);    
        }
        finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    private boolean sortRunData(TreeMap<Integer, ArrayList<Double>> result) {
        int closestVoltIdx;
        double rpm;
        double voltage;
        double error;
        ArrayList<Double> closestVolatageArray;
        for (int i = 0; i < runTables.length; ++i) {
            JTable table = runTables[i];
            String tableName = RunTableName + (i + 1);
            String rpmValue;
            String mafvValue;
            String afrValue;
            for (int j = 0; j < table.getRowCount(); ++j) {
                rpmValue = table.getValueAt(j, 0).toString();
                mafvValue = table.getValueAt(j, 1).toString();
                afrValue = table.getValueAt(j, 2).toString();
                if (rpmValue.isEmpty() || mafvValue.isEmpty() || afrValue.isEmpty())
                    continue;
                if (!Utils.validateDouble(rpmValue, j, 0, tableName) ||
                    !Utils.validateDouble(mafvValue, j, 1, tableName) ||
                    !Utils.validateDouble(afrValue, j, 2, tableName))
                    return false;
                rpm = Double.parseDouble(rpmValue);
                voltage = Double.parseDouble(mafvValue);
                error = Double.parseDouble(afrValue);
                rpmArray.add(rpm);
                mafvArray.add(voltage);
                afrArray.add(error);
                closestVoltIdx = Utils.closestValueIndex(voltage, voltArray);
                closestVolatageArray = result.get(closestVoltIdx);
                if (closestVolatageArray == null) {
                    closestVolatageArray = new ArrayList<Double>();
                    result.put(closestVoltIdx, closestVolatageArray);
                }
                closestVolatageArray.add(error);
            }
        }
        return true;
    }
    
    private void calculateCorrectedGS(TreeMap<Integer, ArrayList<Double>> result) {
        ArrayList<Double> closestVolatageArray;
        double gs = 0;
        double avgError = 0;
        int lastErrIndex = 0;
        int i;
        gsCorrected.addAll(gsArray);
        for (i = 0; i < gsCorrected.size(); ++i) {
            gs = gsCorrected.get(i);
            avgError = 0;
            closestVolatageArray = result.get(i);
            if (closestVolatageArray != null) {
                for (int j = 0; j < closestVolatageArray.size(); ++j)
                    avgError += closestVolatageArray.get(j);
                avgError /= closestVolatageArray.size();
                lastErrIndex = i;
            }
            gsCorrected.set(i, gs * (1 + avgError/100.0));
        }
        avgError = 0;
        ArrayList<Double> sortedAfrArray = result.get(lastErrIndex);
        Collections.sort(sortedAfrArray, Collections.reverseOrder());
        for (i = 0; i < 10 && i < sortedAfrArray.size(); ++i)
            avgError += sortedAfrArray.get(i);
        if (i > 0)
            avgError /= i;
        for (i = lastErrIndex + 1; i < gsCorrected.size(); ++i) {
            gs = gsCorrected.get(i);
            gsCorrected.set(i, gs +(gs * 0.01 * avgError));
        }
    }

    private boolean plotMafVRpmData() {
        return setXYSeries(runData, rpmArray, mafvArray);
    }

    private boolean plotRunData() {
        return setXYSeries(runData, mafvArray, afrArray);
    }
    
    private void setRanges() {
        double paddingX;
        double paddingY;
        XYPlot plot = mafChartPanel.getChartPanel().getChart().getXYPlot();
        plot.getDomainAxis(0).setLabel(XAxisName);
        plot.getRangeAxis(1).setLabel(Y2AxisName);
        plot.getRangeAxis(0).setVisible(true);
        if (checkBoxMafRpmData.isSelected() && checkBoxMafRpmData.isEnabled()) {
            paddingX = runData.getMaxX() * 0.05;
            paddingY = runData.getMaxY() * 0.05;
            plot.getDomainAxis(0).setRange(runData.getMinX() - paddingX, runData.getMaxX() + paddingX);
            plot.getRangeAxis(1).setRange(runData.getMinY() - paddingY, runData.getMaxY() + paddingY);
            plot.getRangeAxis(0).setVisible(false);
            plot.getRangeAxis(1).setLabel(XAxisName);
            plot.getDomainAxis(0).setLabel(rpmAxisName);
        }
        else if (checkBoxRunData.isSelected() && checkBoxRunData.isEnabled() &&
                !checkBoxCurrentMaf.isSelected() && !checkBoxCorrectedMaf.isSelected() && !checkBoxSmoothedMaf.isSelected()) {
            paddingX = runData.getMaxX() * 0.05;
            paddingY = runData.getMaxY() * 0.05;
            plot.getDomainAxis(0).setRange(runData.getMinX() - paddingX, runData.getMaxX() + paddingX);
            plot.getRangeAxis(1).setRange(runData.getMinY() - paddingY, runData.getMaxY() + paddingY);
        }
        else if (checkBoxSmoothing.isSelected()) {
            double maxY = Collections.max(Arrays.asList(new Double[] { currMafData.getMaxY(), smoothMafData.getMaxY(), corrMafData.getMaxY() }));
            double minY = Collections.min(Arrays.asList(new Double[] { currMafData.getMinY(), smoothMafData.getMinY(), corrMafData.getMinY() }));
            paddingX = smoothMafData.getMaxX() * 0.05;
            paddingY = maxY * 0.05;
            plot.getDomainAxis(0).setRange(smoothMafData.getMinX() - paddingX, smoothMafData.getMaxX() + paddingX);
            plot.getRangeAxis(0).setRange(minY - paddingY, maxY + paddingY);
        }
        else if ((checkBoxCurrentMaf.isSelected() && checkBoxCurrentMaf.isEnabled()) ||
                 (checkBoxCorrectedMaf.isSelected() && checkBoxCorrectedMaf.isEnabled()) ||
                 (checkBoxSmoothedMaf.isSelected() && checkBoxSmoothedMaf.isEnabled())) {
            paddingX = voltArray.get(voltArray.size() - 1) * 0.05;
            paddingY = gsCorrected.get(gsCorrected.size() - 1) * 0.05;
            plot.getDomainAxis(0).setRange(voltArray.get(0) - paddingX, voltArray.get(voltArray.size() - 1) + paddingX);
            plot.getRangeAxis(0).setRange(gsCorrected.get(0) - paddingY, gsCorrected.get(gsCorrected.size() - 1) + paddingY);
            if (checkBoxRunData.isSelected()) {
                paddingX = runData.getMaxX() * 0.05;
                paddingY = runData.getMaxY() * 0.05;
                plot.getRangeAxis(1).setRange(runData.getMinY() - paddingY, runData.getMaxY() + paddingY);
            }
        }
        else {
            plot.getRangeAxis(0).setAutoRange(true);
            plot.getDomainAxis(0).setAutoRange(true);
        }
    }
    
    protected void onEnableSmoothingView(boolean flag) {
        checkBoxMafRpmData.setEnabled(!flag);
        if (flag == false) {
            if (checkBoxMafRpmData.isSelected())
                plotMafVRpmData();
            if (checkBoxRunData.isSelected())
                plotRunData();
            if (checkBoxCurrentMaf.isSelected())
                plotCurrentMafData();
            if (checkBoxCorrectedMaf.isSelected())
                setCorrectedMafData();
            if (checkBoxSmoothedMaf.isSelected())
                setSmoothedMafData();
        }
    }
    
    protected void onSmoothReset() {
        if (!checkBoxMafRpmData.isEnabled() || !checkBoxMafRpmData.isSelected())
            setCorrectedMafData();
        if (checkBoxSmoothing.isSelected())
            plotSmoothingLineSlopes();
        else if (checkBoxSmoothedMaf.isSelected())
            setSmoothedMafData();
    }
   
    public void saveData() {
        if (JFileChooser.APPROVE_OPTION != fileChooser.showSaveDialog(this))
            return;
        File file = fileChooser.getSelectedFile();
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        int i, j;
        Writer out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Config.getEncoding()));
            // write string identifier
            out.write(SaveDataFileHeader + "\n");
            // write maf data
            for (i = 0; i < mafTable.getRowCount(); ++i) {
                for (j = 0; j < mafTable.getColumnCount(); ++j)
                    out.write(mafTable.getValueAt(i, j).toString() + ",");
                out.write("\n");
            }
            // write run data
            for (int t = 0; t < runTables.length; ++t) {
                for (i = 0; i < runTables[t].getColumnCount(); ++i) {
                    for (j = 0; j < runTables[t].getRowCount(); ++j)
                        out.write(runTables[t].getValueAt(j, i).toString() + ",");
                    out.write("\n");
                }
            }
        }
        catch (Exception e) {
            logger.error(e);
        }
        finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
   
    public void loadData() {
        fileChooser.setMultiSelectionEnabled(false);
        if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(this))
            return;
        File file = fileChooser.getSelectedFile();
        int i, j, k, l;
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsoluteFile()), Config.getEncoding()));
            String line = br.readLine();
            if (line == null || !line.equals(SaveDataFileHeader)) {
                JOptionPane.showMessageDialog(null, "Invalid saved data file!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            line = br.readLine();
            String[] elements;
            JTable table = null;
            i = k = l = 0;
            while (line != null) {
                elements = line.split(Utils.fileFieldSplitter, -1);
                switch (i) {
                case 0:
                    Utils.ensureColumnCount(elements.length - 1, mafTable);
                    for (j = 0; j < elements.length - 1; ++j)
                        mafTable.setValueAt(elements[j], i, j);
                    break;
                case 1:
                    Utils.ensureColumnCount(elements.length - 1, mafTable);
                    for (j = 0; j < elements.length - 1; ++j)
                        mafTable.setValueAt(elements[j], i, j);
                    break;
                default:
                    int offset = runTables.length * 3 + mafTable.getRowCount();
                    if (i > 1 && i < offset) {
                        if (l == 0 )
                            table = runTables[k++];
                        Utils.ensureRowCount(elements.length - 1, table);
                        for (j = 0; j < elements.length - 1; ++j)
                            table.setValueAt(elements[j], j, l);
                        l += 1;
                        if (l == 3)
                            l = 0;
                    }
                }
                i += 1;
                line = br.readLine();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
        finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
    
    private boolean getColumnsFilters(String[] elements, boolean isPolSet) {
        boolean ret = true;
        ArrayList<String> columns = new ArrayList<String>(Arrays.asList(elements));
        String logThtlAngleColName = Config.getThrottleAngleColumnName();
        String logAfLearningColName = Config.getAfLearningColumnName();
        String logAfCorrectionColName = Config.getAfCorrectionColumnName();
        String logMafvColName = Config.getMafVoltageColumnName();
        String logAfrColName = Config.getWidebandAfrColumnName();
        String logRpmColName = Config.getRpmColumnName();
        String logLoadColName = Config.getLoadColumnName();
        String logCommandedAfrColName = Config.getCommandedAfrColumnName();
        logThtlAngleColIdx = columns.indexOf(logThtlAngleColName);
        logAfLearningColIdx = columns.indexOf(logAfLearningColName);
        logAfCorrectionColIdx = columns.indexOf(logAfCorrectionColName);
        logMafvColIdx = columns.indexOf(logMafvColName);
        logAfrColIdx = columns.indexOf(logAfrColName);
        logRpmColIdx = columns.indexOf(logRpmColName);
        logLoadColIdx = columns.indexOf(logLoadColName);
        logCommandedAfrCol = columns.indexOf(logCommandedAfrColName);
        if (logThtlAngleColIdx == -1)             { Config.setThrottleAngleColumnName(Config.NO_NAME); ret = false; }
        if (logAfLearningColIdx == -1)            { Config.setAfLearningColumnName(Config.NO_NAME);    ret = false; }
        if (logAfCorrectionColIdx == -1)          { Config.setAfCorrectionColumnName(Config.NO_NAME);  ret = false; }
        if (logMafvColIdx == -1)                  { Config.setMafVoltageColumnName(Config.NO_NAME);    ret = false; }
        if (logAfrColIdx == -1)                   { Config.setWidebandAfrColumnName(Config.NO_NAME);   ret = false; }
        if (logRpmColIdx == -1)                   { Config.setRpmColumnName(Config.NO_NAME);           ret = false; }
        if (logLoadColIdx == -1)                  { Config.setLoadColumnName(Config.NO_NAME);          ret = false; }
        if (logCommandedAfrCol == -1 && !isPolSet){ Config.setCommandedAfrColumnName(Config.NO_NAME);  ret = false; }
        wotPoint = Config.getWOTStationaryPointValue();
        minMafV = Config.getMafVMinimumValue();
        afrErrPrct = Config.getWidebandAfrErrorPercentValue();
        minWotEnrichment = Config.getWOTEnrichmentValue();
        afrRowOffset = Config.getWBO2RowOffset();
        skipRowsOnTransition = Config.getOLCLTransitionSkipRows();
        return ret;
    }
    
    protected void loadLogFile() {
        boolean displayDialog = true;
        boolean isPolSet = polfTable.isSet();
        File[] files = fileChooser.getSelectedFiles();
        for (File file : files) {
            BufferedReader br = null;
            ArrayDeque<String[]> buffer = new ArrayDeque<String[]>();
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsoluteFile()), Config.getEncoding()));
                String line = null;
                String [] elements = null;
                while ((line = br.readLine()) != null && (elements = line.split(Utils.fileFieldSplitter, -1)) != null && elements.length < 2)
                    continue;
                getColumnsFilters(elements, false);
                boolean resetColumns = false;
                if (logThtlAngleColIdx >= 0 || logAfLearningColIdx >= 0 || logAfCorrectionColIdx >= 0 || logMafvColIdx >= 0 ||
                    logAfrColIdx >= 0 || logRpmColIdx >= 0 || logLoadColIdx >= 0 || logCommandedAfrCol >= 0) {
                    if (displayDialog) {
                        int rc = JOptionPane.showOptionDialog(null, "Would you like to reset column names or filter values?", "Columns/Filters Reset", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, optionButtons, optionButtons[0]);
                        if (rc == 0)
                            resetColumns = true;
                        else if (rc == 2)
                            displayDialog = false;
                    }
                }
                
                if (resetColumns || logThtlAngleColIdx < 0 || logAfLearningColIdx < 0 || logAfCorrectionColIdx < 0 || logMafvColIdx < 0 ||
                        logAfrColIdx < 0 || logRpmColIdx < 0 || logLoadColIdx < 0 || (logCommandedAfrCol < 0 && !isPolSet)) {
                    ColumnsFiltersSelection selectionWindow = new OLColumnsFiltersSelection(isPolSet);
                    if (!selectionWindow.getUserSettings(elements) || !getColumnsFilters(elements, isPolSet))
                        return;
                }
                
                String[] flds;
                String[] afrflds;
                boolean wotFlag = true;
                boolean foundWot = false;
                double throttle;
                double stft;
                double ltft;
                double afr;
                double rpm;
                double load;
                double mafv;
                double cmdafr = 0;
                double afrErr = 0;
                int skipRowCount = 0;
                int row = 0;
                int i = 0;
                int j = 0;
                for (; i < runTables.length; ++i) {
                    if (runTables[i].getValueAt(0, 0).toString().isEmpty())
                        break;
                }
                if (i == runTables.length)
                    return;
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                for (int k = 0; k <= afrRowOffset && line != null; ++k) {
                    line = br.readLine();
                    if (line != null)
                        buffer.addFirst(line.split(Utils.fileFieldSplitter, -1));
                }
                while (line != null && buffer.size() > afrRowOffset) {
                    afrflds = buffer.getFirst();
                    flds = buffer.removeLast();
                    line = br.readLine();
                    if (line != null)
                        buffer.addFirst(line.split(Utils.fileFieldSplitter, -1));

                    try {
                        throttle = Double.valueOf(flds[logThtlAngleColIdx]);
                        if (row == 0 && throttle < 99)
                            wotFlag = false;
                        if (throttle < wotPoint) {
                            if (wotFlag == true) {
                                wotFlag = false;
                                skipRowCount = 0;
                                j -= 1;
                                while (j > 0 && skipRowCount < skipRowsOnTransition) {
                                    runTables[i].setValueAt("", j, 0);
                                    runTables[i].setValueAt("", j, 1);
                                    runTables[i].setValueAt("", j, 2);
                                    skipRowCount += 1;
                                    j -= 1;
                                }
                                skipRowCount = 0;
                            }
                        }
                        else {
                            if (wotFlag == false) {
                                wotFlag = true;
                                skipRowCount = 0;
                                if (foundWot &&
                                    !runTables[i].getValueAt(0, 0).equals("") &&
                                    !runTables[i].getValueAt(1, 0).equals("") &&
                                    !runTables[i].getValueAt(2, 0).equals("")) {
                                    i += 1;
                                    if (i == runTables.length)
                                        return;
                                }
                                if (row > 0)
                                    j = 0;
                            }
                            if (skipRowCount >= skipRowsOnTransition) {
                                mafv = Double.valueOf(flds[logMafvColIdx]);
                                if (minMafV <= mafv) {
                                    foundWot = true;
                                    stft = Double.valueOf(flds[logAfCorrectionColIdx]);
                                    ltft = Double.valueOf(flds[logAfLearningColIdx]);
                                    afr = Double.valueOf(afrflds[logAfrColIdx]);
                                    rpm = Double.valueOf(flds[logRpmColIdx]);
                                    load = Double.valueOf(flds[logLoadColIdx]);
        
                                    afr = afr / ((100.0 - (ltft + stft)) / 100.0);
                                    
                                    if (logCommandedAfrCol >= 0)
                                        cmdafr = Double.valueOf(flds[logCommandedAfrCol]);
                                    else if (isPolSet)
                                        cmdafr = Utils.calculateCommandedAfr(rpm, load, minWotEnrichment, polfTable);
                                    else {
                                        JOptionPane.showMessageDialog(null, "Please set either \"Commanded AFR\" column or \"Primary Open Loop Fueling\" table", "Error", JOptionPane.ERROR_MESSAGE);
                                        return;
                                    }

                                    afrErr = (afr - cmdafr) / cmdafr * 100.0;
                                    if (Math.abs(afrErr) <= afrErrPrct) {
                                        Utils.ensureRowCount(j + 1, runTables[i]);
                                        runTables[i].setValueAt(rpm, j, 0);
                                        runTables[i].setValueAt(mafv, j, 1);
                                        runTables[i].setValueAt(afrErr, j, 2);
                                        j += 1;
                                    }
                                }
                            }
                            skipRowCount += 1;
                        }
                    }
                    catch (NumberFormatException e) {
                        logger.error(e);
                        JOptionPane.showMessageDialog(null, "Error parsing number at " + file.getName() + " line " + (row + 1) + ": " + e, "Error processing file", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    row += 1;
                }

                if (!foundWot) {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    JOptionPane.showMessageDialog(null, "Sorry, no WOT pulls were found in the log file", "No WOT data", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            catch (Exception e) {
                logger.error(e);
                JOptionPane.showMessageDialog(null, e, "Error opening file", JOptionPane.ERROR_MESSAGE);
            }
            finally {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
    }
    
    protected String usage() {
        ResourceBundle bundle;
        bundle = ResourceBundle.getBundle("com.vgi.mafscaling.open_loop");
        return bundle.getString("usage"); 
    }
}
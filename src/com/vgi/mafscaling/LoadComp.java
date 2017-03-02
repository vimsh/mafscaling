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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.ResourceBundle;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import org.apache.log4j.Logger;

public class LoadComp extends ACompCalc {
    private static final long serialVersionUID = 2988105467764335997L;
    private static final Logger logger = Logger.getLogger(LoadComp.class);

    private static final String iatAxisName = "IAT";
    private static final String trpmAxisName = "Trims";
    private static final String timeAxisName = "Time";
    private static final String rpmAxisName = "RPM";
    private static final String mafvAxisName = "MAF Voltage";
    private boolean isLoadCompInRatio = Config.getIsLoadCompInRatio();
    private int thrtlMaxChange = Config.getThrottleChangeMaxValue();
    private int minCellHitCount = Config.getLCMinCellHitCount();
    private int corrApplied = Config.getLCCorrectionAppliedValue();
    private double afrMin = Config.getLCAfrMinimumValue();
    private double afrMax = Config.getLCAfrMaximumValue();
    private double mpMin = Config.getLCMPMinimumValue();
    private double mpMax = Config.getLCMPMaximumValue();
    private double rpmMax = Config.getRPMMaximumValue();
    private double rpmMin = Config.getRPMMinimumValue();
    private double dvDtMax = Config.getLCDvDtMaximumValue();
    private double iatMax = Config.getLCIatMaximumValue();
    private double cruiseValue = Config.getCruiseStatusValue();
    private double atmPressure = Config.getAtmPressureValue();
    
    private int logThrottleAngleColIdx = -1;
    private int logAfLearningColIdx = -1;
    private int logAfCorrectionColIdx = -1;
    private int logAfrColIdx = -1;
    private int logRpmColIdx = -1;
    private int logTimeColIdx = -1;
    private int logMafvColIdx = -1;
    private int logIatColIdx = -1;
    private int logMpColIdx = -1;
    private int logCruiseStatusColIdx = -1;

    private String[] logColumns = new String[] { "Time", "RPM", "IAT", "STFT", "LTFT", "MP", "MafV", "Trims", "dV/dt" };
    private ArrayList<Double> trimArray = new ArrayList<Double>();
    private ArrayList<Double> rpmArray = new ArrayList<Double>();
    private ArrayList<Double> mafvArray = new ArrayList<Double>();
    private ArrayList<Double> timeArray = new ArrayList<Double>();
    private ArrayList<Double> iatArray = new ArrayList<Double>();
    private ArrayList<Double> dvdtArray = new ArrayList<Double>();
    private HashMap<Double, HashMap<Double, ArrayList<Double>>> xData = null;

    public LoadComp(int tabPlacement) {
        super(tabPlacement);
        origTableName = "Current MP table";
        newTableName = "New MP table";
        corrTableName = "MP Correction (%) table";
        corrCountTableName = "MP Correction Count table";
        x3dAxisName = "MP";
        y3dAxisName = "RPM";
        z3dAxisName = "Avg Error %";
        initialize(logColumns);
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
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);

        addButton(cntlPanel, 0, "Load Log", "loadlog", GridBagConstraints.WEST);
        addButton(cntlPanel, 1, "Clear MP Data", "clearorig", GridBagConstraints.WEST);
        addButton(cntlPanel, 2, "Clear Run Data", "clearlog", GridBagConstraints.WEST);
        addButton(cntlPanel, 3, "Clear All", "clearall", GridBagConstraints.WEST);
        addCheckBox(cntlPanel, 4, "Hide Log Table", "hidelogtable");
        compareTableCheckBox = addCheckBox(cntlPanel, 5, "Compare Tables", "comparetables");
        addButton(cntlPanel, 6, "GO", "go", GridBagConstraints.EAST);
    }
    
    protected void formatTable(JTable table) {
        if (table == corrCountTable) {
            Format[][] formatMatrix = { { new DecimalFormat("#"), new DecimalFormat("0.00") }, { new DecimalFormat("#"), new DecimalFormat("#") } };
            NumberFormatRenderer renderer = (NumberFormatRenderer)table.getDefaultRenderer(Object.class);
            renderer.setFormats(formatMatrix);
        }
        else {
            Format[][] formatMatrix = { { new DecimalFormat("#"), new DecimalFormat("0.00") } };
            NumberFormatRenderer renderer = (NumberFormatRenderer)table.getDefaultRenderer(Object.class);
            renderer.setFormats(formatMatrix);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE CHART TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    protected void createGraphTab() {
        rbGroup = new ButtonGroup();
        JPanel plotPanel = new JPanel();
        add(plotPanel, "<html><div style='text-align: center;'>C<br>h<br>a<br>r<br>t</div></html>");

        GridBagLayout gbl_plotPanel = new GridBagLayout();
        gbl_plotPanel.columnWidths = new int[] {0};
        gbl_plotPanel.rowHeights = new int[] {0, 0};
        gbl_plotPanel.columnWeights = new double[]{1.0};
        gbl_plotPanel.rowWeights = new double[]{0.0, 1.0};
        plotPanel.setLayout(gbl_plotPanel);

        JPanel cntlPanel = new JPanel();
        GridBagConstraints gbl_ctrlPanel = new GridBagConstraints();
        gbl_ctrlPanel.insets = new Insets(3, 3, 3, 3);
        gbl_ctrlPanel.anchor = GridBagConstraints.NORTH;
        gbl_ctrlPanel.weightx = 1.0;
        gbl_ctrlPanel.fill = GridBagConstraints.HORIZONTAL;
        gbl_ctrlPanel.gridx = 0;
        gbl_ctrlPanel.gridy = 0;
        plotPanel.add(cntlPanel, gbl_ctrlPanel);
        
        GridBagLayout gbl_cntlPanel = new GridBagLayout();
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0, 0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);
        
        addRadioButton(cntlPanel, 0, "dV/dt", "dvdt");
        addRadioButton(cntlPanel, 1, "IAT", "iat");
        addRadioButton(cntlPanel, 2, "Trims/RPM", "trpm");
        addRadioButton(cntlPanel, 3, "Trims/MAFv", "trmafv");
        addRadioButton(cntlPanel, 4, "Corrections", "corr");
        
        createChart(plotPanel, timeAxisName, dvdtAxisName);
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE USAGE TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    protected String usage() {
        ResourceBundle bundle;
        bundle = ResourceBundle.getBundle("com.vgi.mafscaling.load_comp");
        return bundle.getString("usage"); 
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // COMMOMN ACTIONS RELATED FUNCTIONS
    //////////////////////////////////////////////////////////////////////////////////////
    
    private boolean getColumnsFilters(String[] elements) {
        boolean ret = true;
        ArrayList<String> columns = new ArrayList<String>(Arrays.asList(elements));
        String logThrottleAngleColName = Config.getThrottleAngleColumnName();
        String logAfLearningColName = Config.getAfLearningColumnName();
        String logAfCorrectionColName = Config.getAfCorrectionColumnName();
        String logAfrColName = Config.getAfrColumnName();
        String logRpmColName = Config.getRpmColumnName();
        String logTimeColName = Config.getTimeColumnName();
        String logMafvColName = Config.getMafVoltageColumnName();
        String logIatColName = Config.getIatColumnName();
        String logMpColName = Config.getMpColumnName();
        String logCruiseStatusColName = Config.getCruiseStatusColumnName();
        logThrottleAngleColIdx = columns.indexOf(logThrottleAngleColName);
        logAfLearningColIdx = columns.indexOf(logAfLearningColName);
        logAfCorrectionColIdx = columns.indexOf(logAfCorrectionColName);
        logAfrColIdx = columns.indexOf(logAfrColName);
        logRpmColIdx = columns.indexOf(logRpmColName);
        logTimeColIdx = columns.indexOf(logTimeColName);
        logMafvColIdx = columns.indexOf(logMafvColName);
        logIatColIdx = columns.indexOf(logIatColName);
        logMpColIdx = columns.indexOf(logMpColName);
        logCruiseStatusColIdx = columns.indexOf(logCruiseStatusColName);
        if (logThrottleAngleColIdx == -1){ Config.setThrottleAngleColumnName(Config.NO_NAME);ret = false; }
        if (logAfLearningColIdx == -1)   { Config.setAfLearningColumnName(Config.NO_NAME);   ret = false; }
        if (logAfCorrectionColIdx == -1) { Config.setAfCorrectionColumnName(Config.NO_NAME); ret = false; }
        if (logAfrColIdx == -1)          { Config.setAfrColumnName(Config.NO_NAME);          ret = false; }
        if (logRpmColIdx == -1)          { Config.setRpmColumnName(Config.NO_NAME);          ret = false; }
        if (logTimeColIdx == -1)         { Config.setTimeColumnName(Config.NO_NAME);         ret = false; }
        if (logMafvColIdx == -1)         { Config.setMafVoltageColumnName(Config.NO_NAME);   ret = false; }
        if (logIatColIdx == -1)          { Config.setIatColumnName(Config.NO_NAME);          ret = false; }
        if (logMpColIdx == -1)           { Config.setMpColumnName(Config.NO_NAME);           ret = false; }
        if (logCruiseStatusColIdx == -1) { Config.setCommandedAfrColumnName(Config.NO_NAME); }
        isLoadCompInRatio = Config.getIsLoadCompInRatio();
        thrtlMaxChange = Config.getThrottleChangeMaxValue();
        minCellHitCount = Config.getLCMinCellHitCount();
        afrMin = Config.getLCAfrMinimumValue();
        afrMax = Config.getLCAfrMaximumValue();
        mpMin = Config.getLCMPMinimumValue();
        mpMax = Config.getLCMPMaximumValue();
        rpmMax = Config.getRPMMaximumValue();
        rpmMin = Config.getRPMMinimumValue();
        dvDtMax = Config.getLCDvDtMaximumValue();
        iatMax = Config.getLCIatMaximumValue();
        cruiseValue = Config.getCruiseStatusValue();
        corrApplied = Config.getLCCorrectionAppliedValue();
        atmPressure = Config.getAtmPressureValue();
        return ret;
    }
    
    protected void loadLogFile() {
        boolean displayDialog = true;
        File[] files = fileChooser.getSelectedFiles();
        for (File file : files) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsoluteFile()), Config.getEncoding()));
                String line = null;
                String [] elements = null;
                while ((line = br.readLine()) != null && (elements = line.split(Utils.fileFieldSplitter, -1)) != null && elements.length < 2)
                    continue;
                getColumnsFilters(elements);
                boolean resetColumns = false;
                if (logThrottleAngleColIdx >= 0 || logAfLearningColIdx >= 0 || logAfCorrectionColIdx >= 0 || logAfrColIdx >= 0 ||
                    logRpmColIdx >= 0 || logTimeColIdx >=0 || logMafvColIdx >= 0 || logIatColIdx >= 0 || logMpColIdx >= 0) {
                    if (displayDialog) {
                        int rc = JOptionPane.showOptionDialog(null, "Would you like to reset column names or filter values?", "Columns/Filters Reset", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, optionButtons, optionButtons[0]);
                        if (rc == 0)
                            resetColumns = true;
                        else if (rc == 2)
                            displayDialog = false;
                    }
                }

                if (resetColumns || logThrottleAngleColIdx < 0 || logAfLearningColIdx < 0 || logAfCorrectionColIdx < 0 || logAfrColIdx < 0 ||
                    logRpmColIdx < 0 || logTimeColIdx < 0 || logMafvColIdx < 0 || logIatColIdx < 0 || logMpColIdx < 0) {
                    ColumnsFiltersSelection selectionWindow = new LCColumnsFiltersSelection();
                    if (!selectionWindow.getUserSettings(elements) || !getColumnsFilters(elements))
                        return;
                }
                
                String[] flds;
                line = br.readLine();
                boolean removed = false;
                int i = 2;
                int row = getLogTableEmptyRow();
                long time = 0;
                long prevTime = 0;
                double cruise = -1;
                double thrtlMaxChange2 = thrtlMaxChange * 2.0;
                double throttle = 0;
                double pThrottle = 0;
                double ppThrottle = 0;
                double trims = 0;
                double stft = 0;
                double ltft = 0;
                double afr = 0;
                double mp = 0;
                double dVdt = 0;
                double pmafv = 0;
                double mafv = 0;
                double iat;
                double rpm;
                clearRunTables();
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                try {
                    if (-1 == logCruiseStatusColIdx)
                        cruiseValue = -1;
                    while (line != null) {
                        flds = line.split(Utils.fileFieldSplitter, -1);
                        ppThrottle = pThrottle;
                        pThrottle = throttle;
                        try {
                            throttle = Double.valueOf(flds[logThrottleAngleColIdx]);
                            // Calculate dV/dt
                            prevTime = time;
                            if (prevTime == 0)
                                Utils.resetBaseTime(flds[logTimeColIdx]);
                            time = Utils.parseTime(flds[logTimeColIdx]);
                            pmafv = mafv;
                            mafv = Double.valueOf(flds[logMafvColIdx]);
                            if ((time - prevTime) == 0.0)
                                dVdt = 100.0;
                            else
                                dVdt = Math.abs(((mafv - pmafv) / (time - prevTime)) * 1000.0);
                            if (row > 1 && Math.abs(pThrottle - throttle) > thrtlMaxChange) {
                                if (!removed)
                                    Utils.removeRow(row--, logDataTable);
                                removed = true;
                            }
                            else if (row <= 2 || Math.abs(ppThrottle - throttle) <= thrtlMaxChange2) {
                                // Filters
                                afr = Double.valueOf(flds[logAfrColIdx]);
                                rpm = Double.valueOf(flds[logRpmColIdx]);
                                mp = Double.valueOf(flds[logMpColIdx]);
                                iat = Double.valueOf(flds[logIatColIdx]);
                                stft = Double.valueOf(flds[logAfCorrectionColIdx]);
                                ltft = Double.valueOf(flds[logAfLearningColIdx]);
                                trims = stft + ltft;
                                if (cruiseValue != -1)
                                    cruise = Double.valueOf(flds[logCruiseStatusColIdx]);
                                if (afrMin <= afr && afr <= afrMax && rpmMin <= rpm && rpm <= rpmMax && mpMin <= mp && mp <= mpMax && dVdt <= dvDtMax && iat <= iatMax && cruise == cruiseValue) {
                                    removed = false;
                                    Utils.ensureRowCount(row + 1, logDataTable);
                                    logDataTable.setValueAt(time, row, 0);
                                    logDataTable.setValueAt(rpm, row, 1);
                                    logDataTable.setValueAt(iat, row, 2);
                                    logDataTable.setValueAt(stft, row, 3);
                                    logDataTable.setValueAt(ltft, row, 4);
                                    logDataTable.setValueAt(mp, row, 5);
                                    logDataTable.setValueAt(mafv, row, 6);
                                    logDataTable.setValueAt(trims, row, 7);
                                    logDataTable.setValueAt(dVdt, row, 8);
                                    trimArray.add(trims);
                                    rpmArray.add(rpm);
                                    mafvArray.add(mafv);
                                    timeArray.add((double)time);
                                    iatArray.add(iat);
                                    dvdtArray.add(dVdt);
                                    row += 1;
                                }
                                else
                                    removed = true;
                            }
                            else
                                removed = true;
                        }
                        catch (NumberFormatException e) {
                            logger.error(e);
                            JOptionPane.showMessageDialog(null, "Error parsing number at " + file.getName() + " line " + i + ": " + e, "Error processing file", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        line = br.readLine();
                        i += 1;
                    }
                    JRadioButton button = (JRadioButton) rbGroup.getElements().nextElement();
                    button.setSelected(true);
                    plotDvdtData();
                }
                finally {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
            catch (Exception e) {
                logger.error(e);
                JOptionPane.showMessageDialog(null, e, "Error opening file", JOptionPane.ERROR_MESSAGE);
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
    }

    protected boolean processLog() {
        try {
            double x, y, val;
            String xStr, yStr, valStr;
            xData = new HashMap<Double, HashMap<Double, ArrayList<Double>>>();
            HashMap<Double, ArrayList<Double>> yData;
            ArrayList<Double> data;
            for (int i = 0; i < logDataTable.getRowCount(); ++i) {
                xStr = logDataTable.getValueAt(i, 5).toString();
                yStr = logDataTable.getValueAt(i, 1).toString();
                valStr = logDataTable.getValueAt(i, 7).toString();
                if (!Pattern.matches(Utils.fpRegex, xStr)) {
                    JOptionPane.showMessageDialog(null, "Invalid value for MP, row " + i + 1, "Invalid value", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (!Pattern.matches(Utils.fpRegex, yStr)) {
                    JOptionPane.showMessageDialog(null, "Invalid value for RPM, row " + i, "Invalid value", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (!Pattern.matches(Utils.fpRegex, valStr)) {
                    JOptionPane.showMessageDialog(null, "Invalid value for Trims, row " + i, "Invalid value", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                x = xAxisArray.get(Utils.closestValueIndex(Double.valueOf(xStr), xAxisArray));
                y = yAxisArray.get(Utils.closestValueIndex(Double.valueOf(yStr), yAxisArray));
                val = Double.valueOf(valStr);

                yData = xData.get(x);
                if (yData == null) {
                    yData = new HashMap<Double, ArrayList<Double>>();
                    xData.put(x, yData);
                }
                data = yData.get(y);
                if (data == null) {
                    data = new ArrayList<Double>();
                    yData.put(y, data);
                }
                data.add(val);
            }
            return true;
        }
        catch (Exception e) {
            logger.error(e);
            JOptionPane.showMessageDialog(null, e, "Error processing data", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    protected boolean displayData() {
        try {
            int cnt;
            double x, y, val;
            HashMap<Double, ArrayList<Double>> yData;
            ArrayList<Double> data;
            Color[][] colorMatrix = new Color[corrTable.getRowCount()][corrTable.getColumnCount()];
            for (int i = 1; i < xAxisArray.size() + 1; ++i) {
                newTable.setValueAt(origTable.getValueAt(0, i), 0, i);
                corrTable.setValueAt(origTable.getValueAt(0, i), 0, i);
                corrCountTable.setValueAt(origTable.getValueAt(0, i), 0, i);
                for (int j = 1; j < yAxisArray.size() + 1; ++j) {
                    if (i == 1) {
                        newTable.setValueAt(origTable.getValueAt(j, 0), j, 0);
                        corrTable.setValueAt(origTable.getValueAt(j, 0), j, 0);
                        corrCountTable.setValueAt(origTable.getValueAt(j, 0), j, 0);
                    }
                    x = xAxisArray.get(i - 1);
                    y = yAxisArray.get(j - 1);
                    yData = xData.get(x);
                    if (yData == null)
                        newTable.setValueAt(origTable.getValueAt(j, i), j, i);
                    else {
                        data = yData.get(y);
                        if (data == null)
                            newTable.setValueAt(origTable.getValueAt(j, i), j, i);
                        else {
                            cnt = data.size();
                            val = (Utils.mean(data) + Utils.mode(data)) / 2.0 + atmPressure - 14.7;
                            corrTable.setValueAt(val, j, i);
                            corrCountTable.setValueAt(cnt, j, i);
                            if (cnt > minCellHitCount) {
                                val = val / 100.0 * corrApplied;
                                val = 1 + val / 100.0;
                                if (isLoadCompInRatio)
                                    newTable.setValueAt(val * Double.valueOf(origTable.getValueAt(j, i).toString()), j, i);
                                else
                                    newTable.setValueAt(val * (100.0 + Double.valueOf(origTable.getValueAt(j, i).toString())) - 100.0, j, i);
                                colorMatrix[j][i] = Color.PINK;
                            }
                            else
                                newTable.setValueAt(origTable.getValueAt(j, i), j, i);
                        }
                    }
                }
            }
            Utils.colorTable(newTable);
            
            for (int i = 0; i < colorMatrix.length; ++i)
                colorMatrix[i][0] = Color.LIGHT_GRAY;
            for (int i = 0; i < colorMatrix[0].length; ++i)
                colorMatrix[0][i] = Color.LIGHT_GRAY;
            ((BgColorFormatRenderer)corrTable.getDefaultRenderer(Object.class)).setColors(colorMatrix);
            ((BgColorFormatRenderer)corrCountTable.getDefaultRenderer(Object.class)).setColors(colorMatrix);

            plot3dCorrection();
            return true;
        }
        catch (Exception e) {
            logger.error(e);
            JOptionPane.showMessageDialog(null, e, "Error processing data", JOptionPane.ERROR_MESSAGE);
        }
        return false;        
    }
    
    private void clearChartData() {
        trimArray.clear();
        rpmArray.clear();
        mafvArray.clear();
        timeArray.clear();
        iatArray.clear();
        dvdtArray.clear();
        runData.clear();
        trendData.clear();
    }
    
    protected void clearLogDataTables() {
        super.clearLogDataTables();
        clearChartData();
    }
    
    private boolean plotDvdtData() {
        return plotTime2dChartData(timeAxisName, timeArray, dvdtAxisName, dvdtArray);
    }

    private boolean plotIatData() {
        return plotTime2dChartData(timeAxisName, timeArray, iatAxisName, iatArray);
    }

    private boolean plotTrimRpmData() {
        return plotRel2dChartData(rpmAxisName, rpmArray, trpmAxisName, trimArray);
    }

    private boolean plotTrimMafvData() {
        return plotRel2dChartData(mafvAxisName, mafvArray, trpmAxisName, trimArray);
    }

    private boolean plotCorrectionData() {
        return plotCorrectionData(rpmAxisName, trpmAxisName);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (checkActionPerformed(e))
            return;
        if ("dvdt".equals(e.getActionCommand())) {
            JRadioButton radioButton = (JRadioButton)e.getSource();
            if (radioButton.isSelected()) {
                if (!plotDvdtData())
                    radioButton.setSelected(false);
            }
            else
                runData.clear();
        }
        else if ("iat".equals(e.getActionCommand())) {
            JRadioButton radioButton = (JRadioButton)e.getSource();
            if (radioButton.isSelected()) {
                if (!plotIatData())
                    radioButton.setSelected(false);
            }
            else
                runData.clear();
        }
        else if ("trpm".equals(e.getActionCommand())) {
            JRadioButton radioButton = (JRadioButton)e.getSource();
            if (radioButton.isSelected()) {
                if (!plotTrimRpmData())
                    radioButton.setSelected(false);
            }
            else {
                runData.clear();
                trendData.clear();
            }
        }
        else if ("trmafv".equals(e.getActionCommand())) {
            JRadioButton radioButton = (JRadioButton)e.getSource();
            if (radioButton.isSelected()) {
                if (!plotTrimMafvData())
                    radioButton.setSelected(false);
            }
            else {
                runData.clear();
                trendData.clear();
            }
        }
        else if ("corr".equals(e.getActionCommand())) {
            JRadioButton radioButton = (JRadioButton)e.getSource();
            if (radioButton.isSelected()) {
                if (!plotCorrectionData())
                    radioButton.setSelected(false);
            }
            else
                clear2dChartData();
        }
    }
}

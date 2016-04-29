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
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.ResourceBundle;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import org.apache.log4j.Logger;

public class MafIatComp extends ACompCalc {
	private static final long serialVersionUID = 42535949864314233L;
	private static final Logger logger = Logger.getLogger(MafIatComp.class);

    private static final String iatAxisName = "IAT";
    private static final String timeAxisName = "Time";
    private static final String mafAxisName = "MAF";
    private static final String corrAxisName = "AFR Correction";
    private boolean isMafIatInRatio = Config.getIsMafIatInRatio();
    private int afrRowOffset = Config.getWBO2RowOffset();
    private int corrApplied = Config.getMICorrectionAppliedValue();
    private int clValue = Config.getClOlStatusValue();
    private int thrtlMaxChange = Config.getMIThrottleChangeMaxValue();
    private int minCellHitCount = Config.getMIMinCellHitCount();
    private double afrMin = Config.getMIAfrMinimumValue();
    private double afrMax = Config.getMIAfrMaximumValue();
    private double dvDtMax = Config.getMIDvDtMaximumValue();
    private double minWotEnrichment = Config.getWOTEnrichmentValue();

    private int logClOlStatusColIdx = -1;
    private int logThrottleAngleColIdx = -1;
    private int logAfLearningColIdx = -1;
    private int logAfCorrectionColIdx = -1;
    private int logAfrColIdx = -1;
    private int logWBAfrColIdx = -1;
    private int logRpmColIdx = -1;
    private int logLoadColIdx = -1;
    private int logCommandedAfrCol = -1;
    private int logTimeColIdx = -1;
    private int logMafvColIdx = -1;
    private int logIatColIdx = -1;
    private int logMafColIdx = -1;

    private String[] logColumns = new String[] { "Time", "MAF", "IAT", "AFR Corr", "dV/dt" };
    private PrimaryOpenLoopFuelingTable polfTable = null;
    private ArrayList<Double> errCorrArray = new ArrayList<Double>();
    private ArrayList<Double> mafArray = new ArrayList<Double>();
    private ArrayList<Double> mafvArray = new ArrayList<Double>();
    private ArrayList<Double> timeArray = new ArrayList<Double>();
    private ArrayList<Double> iatArray = new ArrayList<Double>();
    private ArrayList<Double> dvdtArray = new ArrayList<Double>();
    private HashMap<Double, HashMap<Double, ArrayList<Double>>> xData = null;

    public MafIatComp(int tabPlacement, PrimaryOpenLoopFuelingTable table) {
        super(tabPlacement);
    	polfTable = table;
        origTableName = "Current MAF IAT compensation table";
        newTableName = "New MAF IAT compensation table";
        corrTableName = "MAF IAT Correction (ratio) table";
        corrCountTableName = "MAF IAT Correction Count table";
        x3dAxisName = "IAT";
        y3dAxisName = "MAF";
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
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);

	    addButton(cntlPanel, 0, "POL Fueling", "fueling", GridBagConstraints.WEST);
	    addButton(cntlPanel, 1, "Load Log", "loadlog", GridBagConstraints.WEST);
	    addButton(cntlPanel, 2, "Clear IAT Data", "clearorig", GridBagConstraints.WEST);
	    addButton(cntlPanel, 3, "Clear Run Data", "clearlog", GridBagConstraints.WEST);
	    addButton(cntlPanel, 4, "Clear All", "clearall", GridBagConstraints.WEST);
	    addCheckBox(cntlPanel, 5, "Hide Log Table", "hidelogtable");
	    compareTableCheckBox = addCheckBox(cntlPanel, 6, "Compare Tables", "comparetables");
	    addButton(cntlPanel, 7, "GO", "go", GridBagConstraints.EAST);
    }
    
    protected void formatTable(JTable table) {
        if (table == corrCountTable) {
	        Format[][] formatMatrix = { { new DecimalFormat("0.0"), new DecimalFormat("0.00") }, { new DecimalFormat("0.0"), new DecimalFormat("#") } };
	        NumberFormatRenderer renderer = (NumberFormatRenderer)table.getDefaultRenderer(Object.class);
	        renderer.setFormats(formatMatrix);
        }
        else {
	        Format[][] formatMatrix = { { new DecimalFormat("0.0"), new DecimalFormat("0.00") } };
	        NumberFormatRenderer renderer = (NumberFormatRenderer)table.getDefaultRenderer(Object.class);
	        renderer.setFormats(formatMatrix);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE CHART TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    protected void createGraghTab() {
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
        addRadioButton(cntlPanel, 2, "AFR Corr/MAF", "mafcorr");
        addRadioButton(cntlPanel, 3, "AFR Corr/IAT", "iatcorr");
        addRadioButton(cntlPanel, 4, "Corrections", "corr");
        
        createChart(plotPanel, timeAxisName, dvdtAxisName);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE USAGE TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    protected String usage() {
        ResourceBundle bundle;
        bundle = ResourceBundle.getBundle("com.vgi.mafscaling.mafiat_comp");
        return bundle.getString("usage"); 
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // COMMOMN ACTIONS RELATED FUNCTIONS
    //////////////////////////////////////////////////////////////////////////////////////
    
    private boolean getColumnsFilters(String[] elements, boolean isPolSet) {
    	boolean ret = true;
        ArrayList<String> columns = new ArrayList<String>(Arrays.asList(elements));
        String logClOlStatusColName = Config.getClOlStatusColumnName();
        String logThrottleAngleColName = Config.getThrottleAngleColumnName();
        String logAfLearningColName = Config.getAfLearningColumnName();
        String logAfCorrectionColName = Config.getAfCorrectionColumnName();
        String logWBAfrColName = Config.getWidebandAfrColumnName();
        String logAfrColName = Config.getAfrColumnName();
        String logTimeColName = Config.getTimeColumnName();
        String logMafvColName = Config.getMafVoltageColumnName();
        String logIatColName = Config.getIatColumnName();
        String logMafColName = Config.getMassAirflowColumnName();
        String logCommandedAfrColName = Config.getCommandedAfrColumnName();
        String logRpmColName = Config.getRpmColumnName();
        String logLoadColName = Config.getLoadColumnName();
        logClOlStatusColIdx = columns.indexOf(logClOlStatusColName);
        logThrottleAngleColIdx = columns.indexOf(logThrottleAngleColName);
        logAfLearningColIdx = columns.indexOf(logAfLearningColName);
        logAfCorrectionColIdx = columns.indexOf(logAfCorrectionColName);
        logWBAfrColIdx = columns.indexOf(logWBAfrColName);
        logAfrColIdx = columns.indexOf(logAfrColName);
        logCommandedAfrCol = columns.indexOf(logCommandedAfrColName);
        logTimeColIdx = columns.indexOf(logTimeColName);
        logMafvColIdx = columns.indexOf(logMafvColName);
        logIatColIdx = columns.indexOf(logIatColName);
        logMafColIdx = columns.indexOf(logMafColName);
        logRpmColIdx = columns.indexOf(logRpmColName);
        logLoadColIdx = columns.indexOf(logLoadColName);
        if (logClOlStatusColIdx == -1)   { Config.setClOlStatusColumnName(Config.NO_NAME);   ret = false; }
        if (logThrottleAngleColIdx == -1){ Config.setThrottleAngleColumnName(Config.NO_NAME);ret = false; }
        if (logAfLearningColIdx == -1)   { Config.setAfLearningColumnName(Config.NO_NAME);   ret = false; }
        if (logAfCorrectionColIdx == -1) { Config.setAfCorrectionColumnName(Config.NO_NAME); ret = false; }
        if (logWBAfrColIdx == -1)        { Config.setWidebandAfrColumnName(Config.NO_NAME);  ret = false; }
        if (logAfrColIdx == -1)          { Config.setAfrColumnName(Config.NO_NAME);          ret = false; }
        if (logTimeColIdx == -1)         { Config.setTimeColumnName(Config.NO_NAME);         ret = false; }
        if (logMafvColIdx == -1)         { Config.setMafVoltageColumnName(Config.NO_NAME);   ret = false; }
        if (logIatColIdx == -1)          { Config.setIatColumnName(Config.NO_NAME);          ret = false; }
        if (logMafColIdx == -1)          { Config.setMassAirflowColumnName(Config.NO_NAME);  ret = false; }
        if (logRpmColIdx == -1)          { Config.setRpmColumnName(Config.NO_NAME);          ret = false; }
        if (logLoadColIdx == -1)         { Config.setLoadColumnName(Config.NO_NAME);         ret = false; }
        if (logCommandedAfrCol == -1)    { Config.setCommandedAfrColumnName(Config.NO_NAME); if (!isPolSet) ret = false; }
        isMafIatInRatio = Config.getIsMafIatInRatio();
        afrRowOffset = Config.getWBO2RowOffset();
        corrApplied = Config.getMICorrectionAppliedValue();
        clValue = Config.getMIClOlStatusValue();
        thrtlMaxChange = Config.getMIThrottleChangeMaxValue();
        minCellHitCount = Config.getMIMinCellHitCount();
        afrMin = Config.getMIAfrMinimumValue();
        afrMax = Config.getMIAfrMaximumValue();
        dvDtMax = Config.getMIDvDtMaximumValue();
        minWotEnrichment = Config.getWOTEnrichmentValue();
        return ret;
    }
    
    protected void loadLogFile() {
    	fileChooser.setMultiSelectionEnabled(true);
        if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(this))
            return;
        boolean isPolSet = polfTable.isSet();
        File[] files = fileChooser.getSelectedFiles();
        for (File file : files) {
	        BufferedReader br = null;
	        ArrayDeque<String[]> buffer = new ArrayDeque<String[]>();
	        try {
	            br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
	            String line = br.readLine();
	            if (line != null) {
	            	String [] elements = line.split("\\s*,\\s*", -1);
	                getColumnsFilters(elements, false);
	
	                boolean resetColumns = false;
	                if (logThrottleAngleColIdx >= 0 || logAfLearningColIdx >= 0 || logAfCorrectionColIdx >= 0 ||
	                	logWBAfrColIdx >= 0 || logAfrColIdx >= 0 || logCommandedAfrCol >= 0 || logTimeColIdx >=0 ||
	                	logMafvColIdx >= 0 || logIatColIdx >= 0 || logMafColIdx >= 0 || logClOlStatusColIdx >= 0 ||
	                	logRpmColIdx >= 0 || logLoadColIdx >= 0) {
	                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Would you like to reset column names or filter values?", "Columns/Filters Reset", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE))
	                    	resetColumns = true;
	                }
	
	                if (resetColumns || logThrottleAngleColIdx < 0 || logAfLearningColIdx < 0 ||
	                	logAfCorrectionColIdx < 0 || logWBAfrColIdx < 0 || logAfrColIdx < 0 || logTimeColIdx < 0 || 
	                	logMafvColIdx < 0 || logIatColIdx < 0 || logMafColIdx < 0 || logClOlStatusColIdx < 0 ||
	                	logRpmColIdx < 0 || logLoadColIdx < 0 || (logCommandedAfrCol < 0 && !isPolSet)) {
	                	ColumnsFiltersSelection selectionWindow = new MafIatColumnsFiltersSelection(isPolSet);
	                	if (!selectionWindow.getUserSettings(elements) || !getColumnsFilters(elements, isPolSet))
	                		return;
	                }
	                
	                String[] flds;
	                String[] afrflds;
	                boolean removed = false;
	                int i = 2;
	                int row = getLogTableEmptyRow();
	                long time = 0;
	                long prevTime = 0;
	                double thrtlMaxChange2 = thrtlMaxChange * 2.0;
	                double throttle = 0;
	                double pThrottle = 0;
	                double ppThrottle = 0;
	                double afr = 0;
	                double rpm = 0;
	                double load = 0;
	                double corr = 0;
	                double cmdafr = 0;
	                double trims = 0;
	                double dVdt = 0;
	                double pmafv = 0;
	                double maf = 0;
	                double mafv = 0;
	                double iat;
	                clearRunTables();
	                setCursor(new Cursor(Cursor.WAIT_CURSOR));
	                for (int k = 0; k <= afrRowOffset && line != null; ++k) {
	                	line = br.readLine();
	                	if (line != null)
	                		buffer.addFirst(line.split("\\s*,\\s*", -1));
	                }
	                try {
		                while (line != null && buffer.size() > afrRowOffset) {
		                    afrflds = buffer.getFirst();
		                    flds = buffer.removeLast();
		                    line = br.readLine();
		                	if (line != null)
		                		buffer.addFirst(line.split("\\s*,\\s*", -1));
		                	
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
		                    		trims = Double.valueOf(flds[logAfLearningColIdx]) + Double.valueOf(flds[logAfCorrectionColIdx]);
	                            	if (clValue == Double.valueOf(flds[logClOlStatusColIdx])) {
	                            		afr = Double.valueOf(flds[logAfrColIdx]);
	                            		corr = (100.0 + trims) / 100.0;
	                            	}
	                            	else {
	                            		afr = Double.valueOf(afrflds[logWBAfrColIdx]);
			                            rpm = Double.valueOf(flds[logRpmColIdx]);
			                            load = Double.valueOf(flds[logLoadColIdx]);
			                            if (logCommandedAfrCol >= 0)
			                            	cmdafr = Double.valueOf(flds[logCommandedAfrCol]);
			                            else if (isPolSet)
			                            	cmdafr = Utils.calculateCommandedAfr(rpm, load, minWotEnrichment, polfTable);
			                            else {
			                            	JOptionPane.showMessageDialog(null, "Please set either \"Commanded AFR\" column or \"Primary Open Loop Fueling\" table", "Error", JOptionPane.ERROR_MESSAGE);
			                            	return;
			                            }
	                            		corr = (afr / ((100.0 - trims) / 100.0)) / cmdafr;
	                            	}
		                        	iat = Double.valueOf(flds[logIatColIdx]);
		                        	maf = Double.valueOf(flds[logMafColIdx]);
		                        	if (afrMin <= afr && afr <= afrMax && dVdt <= dvDtMax) {
			                    		removed = false;
		                                Utils.ensureRowCount(row + 1, logDataTable);
		                                logDataTable.setValueAt(time, row, 0);
		                                logDataTable.setValueAt(maf, row, 1);
		                                logDataTable.setValueAt(iat, row, 2);
		                                logDataTable.setValueAt(corr, row, 3);
		                                logDataTable.setValueAt(dVdt, row, 4);
		                                mafArray.add(maf);
		                                errCorrArray.add(corr);
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
            	xStr = logDataTable.getValueAt(i, 2).toString();
            	yStr = logDataTable.getValueAt(i, 1).toString();
            	valStr = logDataTable.getValueAt(i, 3).toString();
                if (!Pattern.matches(Utils.fpRegex, xStr)) {
                    JOptionPane.showMessageDialog(null, "Invalid value for IAT, row " + i + 1, "Invalid value", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (!Pattern.matches(Utils.fpRegex, yStr)) {
                    JOptionPane.showMessageDialog(null, "Invalid value for MAF, row " + i, "Invalid value", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (!Pattern.matches(Utils.fpRegex, valStr)) {
                    JOptionPane.showMessageDialog(null, "Invalid value for AFR Corr, row " + i, "Invalid value", JOptionPane.ERROR_MESSAGE);
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
    		        		val = (Utils.mean(data) + Utils.mode(data)) / 2.0;
	    	        		corrTable.setValueAt(val, j, i);
	    	        		corrCountTable.setValueAt(cnt, j, i);
	    	        		if (cnt > minCellHitCount) {
	    		        		val = val / 100.0 * corrApplied;
	    		        		if (isMafIatInRatio)
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
        mafArray.clear();
        errCorrArray.clear();
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

    private boolean plotMafErrCorrData() {
        return plotRel2dChartData(mafAxisName, mafArray, corrAxisName, errCorrArray);
    }
    
    private boolean plotIatErrCorrData() {
        return plotRel2dChartData(iatAxisName, iatArray, corrAxisName, errCorrArray);
    }

    private boolean plotCorrectionData() {
        return plotCorrectionData(mafAxisName, corrAxisName);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (checkActionPerformed(e))
        	return;
        if ("fueling".equals(e.getActionCommand())) {
            polfTable.getSetUserFueling();
        }
        else if ("dvdt".equals(e.getActionCommand())) {
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
        else if ("mafcorr".equals(e.getActionCommand())) {
        	JRadioButton radioButton = (JRadioButton)e.getSource();
            if (radioButton.isSelected()) {
                if (!plotMafErrCorrData())
                	radioButton.setSelected(false);
            }
            else {
                runData.clear();
                trendData.clear();
            }
        }
        else if ("iatcorr".equals(e.getActionCommand())) {
        	JRadioButton radioButton = (JRadioButton)e.getSource();
            if (radioButton.isSelected()) {
                if (!plotIatErrCorrData())
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

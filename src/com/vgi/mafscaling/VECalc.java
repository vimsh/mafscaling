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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Stroke;
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
import java.util.ResourceBundle;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYSeriesLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;

public class VECalc extends ACompCalc {
	private static final long serialVersionUID = -6288885401403089256L;
	private static final Logger logger = Logger.getLogger(VECalc.class);
    
    class LogData {
    	public double rpm = 0;
    	public double mp = 0;
    	public double iat = 0;
    	public double afr = 0;
    	public double maf = 0;
    	public double ffb = 0;
    	public double sd = 0;
    	public double sderr = 0;
    	public double afrerr = 0;
    }

    private static final String xAxisName = "RPM";
    private static final String yAxisName = "Estimated VE";
    private int clValue = Config.getVEClOlStatusValue();
    private int afrRowOffset = Config.getWBO2RowOffset();
    private int thrtlMaxChange = Config.getVEThrottleChangeMaxValue();
    private int minCellHitCount = Config.getVEMinCellHitCount();
    private double thrtlMin = Config.getVEThrottleMinimumValue();
    private double afrMax = Config.getVEOlAfrMaximumValue();
    private double afrMin = Config.getVEAfrMinimumValue();
    private double rpmMin = Config.getVERPMMinimumValue();
    private double ffbMax = Config.getFFBMaximumValue();
    private double ffbMin = Config.getFFBMinimumValue();
    private double mpMin = Config.getVEMPMinimumValue();
    private double iatMax = Config.getVEIatMaximumValue();
    private boolean isOl = Config.veOpenLoop();
    private int corrApplied = Config.getVECorrectionAppliedValue();
    private int logClOlStatusColIdx = -1;
    private int logThrottleAngleColIdx = -1;
    private int logRpmColIdx = -1;
    private int logMpColIdx = -1;
    private int logIatColIdx = -1;
    private int logWbAfrColIdx = -1;
    private int logStockAfrColIdx = -1;
    private int logAfLearningColIdx = -1;
    private int logAfCorrectionColIdx = -1;
    private int logMafColIdx = -1;    
    private int logFfbColIdx = -1;    
    private int logSdColIdx = -1;
    
    private String[] logColumns = new String[] { "RPM", "IAT", "MP", "FFB", "AFR", "MAF", "VE" };
    private JComboBox<String> sdType = null;
    private JComboBox<String> mpType = null;
    private JComboBox<String> dataType = null;
    private ArrayList<Double> trims = new ArrayList<Double>();
    private HashMap<Double, HashMap<Double, ArrayList<LogData>>> xData = null;

    public VECalc(int tabPlacement) {
        super(tabPlacement);
        origTableName = "Current VE table";
        newTableName = "New VE table";
        corrTableName = "Error % table";
        corrCountTableName = "Error % Count table";
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
        gbl_cntlPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);

	    addButton(cntlPanel, 0, "Load Log", "loadlog", GridBagConstraints.WEST);
	    addButton(cntlPanel, 1, "Clear SD Data", "clearorig", GridBagConstraints.WEST);
	    addButton(cntlPanel, 2, "Clear Run Data", "clearlog", GridBagConstraints.WEST);
	    addButton(cntlPanel, 3, "Clear All", "clearall", GridBagConstraints.WEST);
	    addLabel(cntlPanel, 4, "SD");
	    sdType = addComboBox(cntlPanel, 5, new String [] { "RR SD", "Cobb SD" });
	    addLabel(cntlPanel, 6, "MP");
	    mpType = addComboBox(cntlPanel, 7, new String [] { "Torr/mmHG Abs", "Torr/mmHG Rel Sea Lvl", "Psi Abs", "Psi Rel Sea Lvl" });
	    addLabel(cntlPanel, 8, "Run");
	    dataType = addComboBox(cntlPanel, 9, new String [] { "MAF Builder", "AFR Tuner" });
	    addCheckBox(cntlPanel, 10, "Hide Log Table", "hidelogtable");
	    compareTableCheckBox = addCheckBox(cntlPanel, 11, "Compare Tables", "comparetables");
	    addButton(cntlPanel, 12, "GO", "go", GridBagConstraints.EAST);
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
    
    protected void createGraghTab() {
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
        gbl_cntlPanel.columnWidths = new int[]{0, 0};
        gbl_cntlPanel.rowHeights = new int[]{0, 0};
        gbl_cntlPanel.columnWeights = new double[]{0.0, 1.0};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);
        
        createChart(plotPanel, xAxisName, yAxisName);
    }
        

    protected void createChart(JPanel plotPanel, String xAxisName, String yAxisName) {
        JFreeChart chart = ChartFactory.createScatterPlot(null, null, null, null, PlotOrientation.VERTICAL, false, true, false);
        chart.setBorderVisible(true);

        chartPanel = new ChartPanel(chart, true, true, true, true, true);
		chartPanel.setAutoscrolls(true);
		chartPanel.setMouseZoomable(false);
        
        GridBagConstraints gbl_chartPanel = new GridBagConstraints();
        gbl_chartPanel.anchor = GridBagConstraints.CENTER;
        gbl_chartPanel.insets = new Insets(3, 3, 3, 3);
        gbl_chartPanel.weightx = 1.0;
        gbl_chartPanel.weighty = 1.0;
        gbl_chartPanel.fill = GridBagConstraints.BOTH;
        gbl_chartPanel.gridx = 0;
        gbl_chartPanel.gridy = 1;
        plotPanel.add(chartPanel, gbl_chartPanel);

        XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer();
        lineRenderer.setUseFillPaint(true);
        lineRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator( 
                StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, 
                new DecimalFormat("0.00"), new DecimalFormat("0.00")));
        
        Stroke stroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, null, 0.0f);
        lineRenderer.setSeriesStroke(0, stroke);
        lineRenderer.setSeriesPaint(0, Color.RED);
        lineRenderer.setSeriesShape(0, ShapeUtilities.createDiamond((float) 2.5));

        lineRenderer.setLegendItemLabelGenerator(
        		new StandardXYSeriesLabelGenerator() {
					private static final long serialVersionUID = 7593430826693873496L;
					public String generateLabel(XYDataset dataset, int series) {
						XYSeries xys = ((XYSeriesCollection)dataset).getSeries(series);
						return xys.getDescription();
					}
        		}
        );

        NumberAxis xAxis = new NumberAxis(xAxisName);
        xAxis.setAutoRangeIncludesZero(false);
        NumberAxis yAxis = new NumberAxis(yAxisName);
        yAxis.setAutoRangeIncludesZero(false);
        
        XYSeriesCollection lineDataset = new XYSeriesCollection();
        
        XYPlot plot = chart.getXYPlot();
        plot.setRangePannable(true);
        plot.setDomainPannable(true);
        plot.setDomainGridlinePaint(Color.DARK_GRAY);
        plot.setRangeGridlinePaint(Color.DARK_GRAY);
        plot.setBackgroundPaint(new Color(224, 224, 224));

        plot.setDataset(0, lineDataset);
        plot.setRenderer(0, lineRenderer);
        plot.setDomainAxis(0, xAxis);
        plot.setRangeAxis(0, yAxis);
        plot.mapDatasetToDomainAxis(0, 0);
        plot.mapDatasetToRangeAxis(0, 0);
        
        LegendTitle legend = new LegendTitle(plot.getRenderer()); 
        legend.setItemFont(new Font("Arial", 0, 10));
        legend.setPosition(RectangleEdge.TOP);
        chart.addLegend(legend);
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // CREATE USAGE TAB
    //////////////////////////////////////////////////////////////////////////////////////
    
    protected String usage() {
        ResourceBundle bundle;
        bundle = ResourceBundle.getBundle("com.vgi.mafscaling.ve_calc");
        return bundle.getString("usage"); 
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // COMMOMN ACTIONS RELATED FUNCTIONS
    //////////////////////////////////////////////////////////////////////////////////////
    
    private boolean getColumnsFilters(String[] elements) {
    	boolean ret = true;
        ArrayList<String> columns = new ArrayList<String>(Arrays.asList(elements));
        String logClOlStatusColName = Config.getClOlStatusColumnName();
        String logThrottleAngleColName = Config.getThrottleAngleColumnName();
        String logFfbColName = Config.getFinalFuelingBaseColumnName();
        String logSdColName = Config.getVEFlowColumnName();
        String logWbAfrColName = Config.getWidebandAfrColumnName();
        String logStockAfrColName = Config.getAfrColumnName();
        String logAfLearningColName = Config.getAfLearningColumnName();
        String logAfCorrectionColName = Config.getAfCorrectionColumnName();
        String logRpmColName = Config.getRpmColumnName();
        String logMafColName = Config.getMassAirflowColumnName();
        String logIatColName = Config.getIatColumnName();
        String logMpColName = Config.getMpColumnName();
        isOl = Config.veOpenLoop();
        logClOlStatusColIdx = columns.indexOf(logClOlStatusColName);
        logThrottleAngleColIdx = columns.indexOf(logThrottleAngleColName);
        logFfbColIdx = columns.indexOf(logFfbColName);
        logSdColIdx = columns.indexOf(logSdColName);
        logWbAfrColIdx = columns.indexOf(logWbAfrColName);
        logStockAfrColIdx = columns.indexOf(logStockAfrColName);
        logAfLearningColIdx = columns.indexOf(logAfLearningColName);
        logAfCorrectionColIdx = columns.indexOf(logAfCorrectionColName);
        logRpmColIdx = columns.indexOf(logRpmColName);
        logMafColIdx = columns.indexOf(logMafColName);
        logIatColIdx = columns.indexOf(logIatColName);
        logMpColIdx = columns.indexOf(logMpColName);
        if (logThrottleAngleColIdx == -1)        { Config.setThrottleAngleColumnName(Config.NO_NAME);    ret = false; }
        if (logFfbColIdx == -1)                  { Config.setFinalFuelingBaseColumnName(Config.NO_NAME); ret = false; }
        if (logSdColIdx == -1)                   { Config.setVEFlowColumnName(Config.NO_NAME);           ret = false; }
        if (logWbAfrColIdx == -1 && isOl)        { Config.setWidebandAfrColumnName(Config.NO_NAME);      ret = false; }
        if (logStockAfrColIdx == -1 && !isOl)    { Config.setAfrColumnName(Config.NO_NAME);              ret = false; }
        if (logAfLearningColIdx == -1 && !isOl)  { Config.setAfLearningColumnName(Config.NO_NAME);       ret = false; }
        if (logAfCorrectionColIdx == -1 && !isOl){ Config.setAfCorrectionColumnName(Config.NO_NAME);     ret = false; }
        if (logRpmColIdx == -1)                  { Config.setRpmColumnName(Config.NO_NAME);              ret = false; }
        if (logMafColIdx == -1)                  { Config.setMassAirflowColumnName(Config.NO_NAME);      ret = false; }
        if (logMpColIdx == -1)                   { Config.setMpColumnName(Config.NO_NAME);               ret = false; }
        if (logIatColIdx == -1)                  { Config.setIatColumnName(Config.NO_NAME);              ret = false; }
        clValue = Config.getVEClOlStatusValue();
        rpmMin = Config.getVERPMMinimumValue();
        mpMin = Config.getVEMPMinimumValue();
        iatMax = Config.getVEIatMaximumValue();
        ffbMax = Config.getFFBMaximumValue();
        ffbMin = Config.getFFBMinimumValue();
        thrtlMaxChange = Config.getVEThrottleChangeMaxValue();
        minCellHitCount = Config.getVEMinCellHitCount();
        thrtlMin = Config.getVEThrottleMinimumValue();
        afrMax = (isOl ? Config.getVEOlAfrMaximumValue() : Config.getVEClAfrMaximumValue());
        afrMin = Config.getVEAfrMinimumValue();
        afrRowOffset = Config.getWBO2RowOffset();
        corrApplied = Config.getVECorrectionAppliedValue();
        return ret;
    }
    
    protected void loadLogFile() {
        boolean displayDialog = true;
    	fileChooser.setMultiSelectionEnabled(true);
        if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(this))
            return;
        File[] files = fileChooser.getSelectedFiles();
        for (File file : files) {
	        BufferedReader br = null;
	        ArrayDeque<String[]> buffer = new ArrayDeque<String[]>();
	        try {
	            br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
	            String line = null;
	            String [] elements = null;
	            while ((line = br.readLine()) != null && (elements = line.split("\\s*,\\s*", -1)) != null && elements.length < 2)
	            	continue;
                getColumnsFilters(elements);
                boolean resetColumns = false;
                if (logThrottleAngleColIdx >= 0 || logFfbColIdx >= 0 || logSdColIdx >= 0 || (logWbAfrColIdx >= 0 && isOl) ||
                	(logStockAfrColIdx >= 0 && !isOl) || (logAfLearningColIdx >= 0 && !isOl) || (logAfCorrectionColIdx >= 0 && !isOl) ||
                	logRpmColIdx >= 0 || logMafColIdx >= 0 || logIatColIdx >= 0 || logMpColIdx >= 0) {
                	if (displayDialog) {
	                    int rc = JOptionPane.showOptionDialog(null, "Would you like to reset column names or filter values?", "Columns/Filters Reset", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, optionButtons, optionButtons[0]);
	                    if (rc == 0)
	                    	resetColumns = true;
	                    else if (rc == 2)
	                    	displayDialog = false;
                	}
                }

                if (resetColumns || logThrottleAngleColIdx < 0 || logFfbColIdx < 0 || logSdColIdx < 0 || (logWbAfrColIdx < 0 && isOl) ||
                	(logStockAfrColIdx < 0 && !isOl) || (logAfLearningColIdx < 0 && !isOl) || (logAfCorrectionColIdx < 0 && !isOl) ||
                	logRpmColIdx < 0 || logMafColIdx < 0 || logIatColIdx < 0 || logMpColIdx < 0) {
                	ColumnsFiltersSelection selectionWindow = new VEColumnsFiltersSelection();
                	if (!selectionWindow.getUserSettings(elements) || !getColumnsFilters(elements))
                		return;
                }
                
                if (logClOlStatusColIdx == -1)
                	clValue = -1;
                
                String[] flds;
                String[] afrflds;
                boolean removed = false;
                int i = 2;
                int clol = -1;
                int row = getLogTableEmptyRow();
                double thrtlMaxChange2 = thrtlMaxChange + thrtlMaxChange / 2.0;
                double throttle = 0;
                double pThrottle = 0;
                double ppThrottle = 0;
                double afr = 0;
                double rpm;
                double ffb;
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
                    	throttle = Double.valueOf(flds[logThrottleAngleColIdx]);
	                    try {
	                    	if (row > 0 && Math.abs(pThrottle - throttle) > thrtlMaxChange) {
	                    		if (!removed)
	                    			Utils.removeRow(row--, logDataTable);
	                    		removed = true;
	                    	}
	                    	else if (row <= 0 || Math.abs(ppThrottle - throttle) <= thrtlMaxChange2) {
	                            // Filters
	                        	afr = (isOl ? Double.valueOf(afrflds[logWbAfrColIdx]) : Double.valueOf(afrflds[logStockAfrColIdx]));
                                rpm = Double.valueOf(flds[logRpmColIdx]);
                                ffb = Double.valueOf(flds[logFfbColIdx]);
                                iat = Double.valueOf(flds[logIatColIdx]);
    	                    	if (clValue != -1)
    	                    		clol = (int)Utils.parseValue(flds[logClOlStatusColIdx]);
    	                    	boolean flag = isOl ? ((afr <= afrMax || throttle >= thrtlMin) && afr <= afrMax) : (afrMin <= afr);
	                        	if (flag && clol == clValue && rpmMin <= rpm && ffbMin <= ffb && ffb <= ffbMax && iat <= iatMax) {
		                    		removed = false;
		                    		if (!isOl)
		                    			trims.add(Double.valueOf(flds[logAfLearningColIdx]) + Double.valueOf(flds[logAfCorrectionColIdx]));
	                                Utils.ensureRowCount(row + 1, logDataTable);
	                                logDataTable.setValueAt(rpm, row, 0);
	                                logDataTable.setValueAt(iat, row, 1);
	                                logDataTable.setValueAt(Double.valueOf(flds[logMpColIdx]), row, 2);
	                                logDataTable.setValueAt(ffb, row, 3);
	                                logDataTable.setValueAt(afr, row, 4);
	                                logDataTable.setValueAt(Double.valueOf(flds[logMafColIdx]), row, 5);
	                                logDataTable.setValueAt(Double.valueOf(flds[logSdColIdx]), row, 6);
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
        	double mapG = 1;
        	double mapO = 0;
        	String mpStr = mpType.getSelectedItem().toString();
        	String sdStr = sdType.getSelectedItem().toString();
        	if (mpStr.contains("Torr"))
        		mapG = 1.0 / 51.715;
        	if (mpStr.contains("Abs") && sdStr.contains("RR"))
        		mapO = -14.7;
        	else if (mpStr.contains("Rel") && sdStr.contains("Cobb"))
        		mapO = 14.7;

        	String rpmStr, iatStr, afrStr, mafStr, ffbStr;
        	LogData logData;
        	xData = new HashMap<Double, HashMap<Double, ArrayList<LogData>>>();
            HashMap<Double, ArrayList<LogData>> yData;
            ArrayList<LogData> data;
            for (int i = 0; i < logDataTable.getRowCount(); ++i) {
            	rpmStr = logDataTable.getValueAt(i, 0).toString();
            	iatStr = logDataTable.getValueAt(i, 1).toString();
            	mpStr  = logDataTable.getValueAt(i, 2).toString();
            	ffbStr = logDataTable.getValueAt(i, 3).toString();
            	afrStr = logDataTable.getValueAt(i, 4).toString();
            	mafStr = logDataTable.getValueAt(i, 5).toString();
            	sdStr  = logDataTable.getValueAt(i, 6).toString();
                if (rpmStr.isEmpty() || mpStr.isEmpty() || iatStr.isEmpty() || afrStr.isEmpty() || mafStr.isEmpty() || ffbStr.isEmpty() || sdStr.isEmpty())
                	continue;
                logData = new LogData();
                logData.mp = (Double.valueOf(mpStr) * mapG) + mapO;
                
                if (logData.mp < mpMin)
                	continue;
                
                logData.rpm = Double.valueOf(rpmStr);
                logData.mp = xAxisArray.get(Utils.closestValueIndex(logData.mp, xAxisArray));
                logData.rpm = yAxisArray.get(Utils.closestValueIndex(logData.rpm, yAxisArray));
                logData.iat = Double.valueOf(iatStr);
                logData.maf = Double.valueOf(mafStr);
                logData.sd = Double.valueOf(sdStr);
                logData.afr = Double.valueOf(afrStr);
                logData.ffb = Double.valueOf(ffbStr);
                logData.sderr = ((logData.sd - logData.maf) / logData.maf) * 100.0;
                if (isOl)
                	logData.afrerr = ((logData.afr - logData.ffb) / logData.ffb) * 100.0;
                else
                	logData.afrerr = trims.get(i);// + ((14.7 - logData.ffb) / 14.7 * 100);
                
                yData = xData.get(logData.mp);
                if (yData == null) {
                	yData = new HashMap<Double, ArrayList<LogData>>();
                	xData.put(logData.mp, yData);
                }
                data = yData.get(logData.rpm);
                if (data == null) {
                	data = new ArrayList<LogData>();
                	yData.put(logData.rpm, data);
                }
                data.add(logData);
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
        	boolean isMaf = (dataType.getSelectedIndex() == 0? true : false);
            ArrayList<LogData> data;
            HashMap<Double, ArrayList<LogData>> yData;
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
	    	        		val = 0;
	    	        		for (LogData d : data)
	    	        			val += (isMaf ? d.sderr : d.afrerr);
	    	        		val /= cnt;
	    	        		corrTable.setValueAt(val, j, i);
	    	        		corrCountTable.setValueAt(cnt, j, i);
	    	        		if (cnt > minCellHitCount) {
	    		        		val = 1 + (val / 100.0 * corrApplied) / 100.0;
	    		        		if (isMaf)
	    		        			newTable.setValueAt(Double.valueOf(origTable.getValueAt(j, i).toString()) / val, j, i);
	    		        		else
	    		        			newTable.setValueAt(Double.valueOf(origTable.getValueAt(j, i).toString()) * val, j, i);
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
            
            plotCorrectionData();
            
	        return true;
        }
        catch (Exception e) {
            logger.error(e);
            JOptionPane.showMessageDialog(null, e, "Error processing data", JOptionPane.ERROR_MESSAGE);
        }
        return false;    	
    }
    
    private void clearChartData() {
        trims.clear();
        runData.clear();
        trendData.clear();
    }
    
    protected void clearLogDataTables() {
    	super.clearLogDataTables();
    	clearChartData();
    }

    private boolean plotCorrectionData() {
        plot3d.removeAllPlots();
    	XYSeries series;
        XYPlot plot = chartPanel.getChart().getXYPlot();
    	XYSeriesCollection lineDataset = (XYSeriesCollection) plot.getDataset(0);
    	DecimalFormat df = new DecimalFormat(".00");
    	String val;
    	int i = 0;
    	int j = 0;
    	if (!Utils.isTableEmpty(corrTable)) {
    		try {
		    	for (i = 1; i < corrTable.getColumnCount(); ++i) {
		    		val = corrTable.getValueAt(0, i).toString();
		    		series = new XYSeries(df.format(Double.valueOf(val)));
		    		for (j = 1; j < corrTable.getRowCount(); ++j) { 
		    			if (corrTable.getValueAt(j, i) != null) {
		    				val = corrTable.getValueAt(j, i).toString();
		    				if (!val.isEmpty())
		    					series.add(Double.valueOf(corrTable.getValueAt(j, 0).toString()), Double.valueOf(val));
		    			}
		    		}
		    		if (series.getItemCount() > 0) {
		    			corrData.add(series);
		    			series.setDescription(series.getKey().toString());
		    	        lineDataset.addSeries(series);
		    		}
		    	}
		    	plot.getDomainAxis(0).setAutoRange(true);
		    	plot.getRangeAxis(0).setAutoRange(true);
		    	plot.getDomainAxis(0).setLabel(xAxisName);
		    	plot.getRangeAxis(0).setLabel(yAxisName);
		        plot.getRenderer(0).setSeriesVisible(0, false);

		        double[] x = new double[xAxisArray.size()];
		        for (i = 0; i < xAxisArray.size(); ++i)
		        	x[i] = xAxisArray.get(i);
		        double[] y = new double[yAxisArray.size()];
		        for (i = 0; i < yAxisArray.size(); ++i)
		        	y[i] = yAxisArray.get(i);
		        double[][] z = Utils.doubleZArray(corrTable, x, y);
		        Color[][] tableColors = Utils.generateTableColorMatrix(corrTable, 1, 1, y.length + 1, x.length + 1);
		        Color[][] colors = new Color[y.length][x.length];
		        for (j = 1; j < tableColors.length; ++j)
			        for (i = 1; i < tableColors[j].length; ++i)
			        	colors[j - 1][i - 1] = tableColors[j][i];
		        plot3d.addGridPlot("Average Error % Plot", colors, x, y, z);
            }
            catch (NumberFormatException e) {
                logger.error(e);
                JOptionPane.showMessageDialog(null, "Error parsing number from " + corrTableName + " table, cell(" + i + " : " + j + "): " + e, "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
    	}
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	checkActionPerformed(e);
    }
}
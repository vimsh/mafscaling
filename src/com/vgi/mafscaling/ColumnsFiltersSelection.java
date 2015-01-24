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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;

abstract class ColumnsFiltersSelection implements ActionListener {
    protected static final String rpmLabelText = "Engine Speed";
    protected static final String loadLabelText = "Engine Load";
    protected static final String mpLabelText = "Manifold Pressure";
    protected static final String afLearningLabelText = "AFR Learning (LTFT)";
    protected static final String afCorrectionLabelText = "AFR Correction (STFT)";
    protected static final String mafVLabelText = "MAF Voltage";
    protected static final String mafLabelText = "Mass Airflow";
    protected static final String wbAfrLabelText = "Wideband AFR";
    protected static final String thrtlAngleLabelText = "Throttle Angle %";
    protected static final String commAfrLabelText = "Commanded AFR";
    protected static final String stockAfrLabelText = "Stock AFR";
    protected static final String clolStatusLabelText = "CL/OL Status";
    protected static final String cruiseStatusLabelText = "Cruise/Non-cruise Status *";
    protected static final String timeLabelText = "Time";
    protected static final String iatLabelText = "Intake Air Temperature";
    protected static final String ffbLabelText = "Final Fueling Base";
    protected static final String veFlowLabelText = "VE Flow/VE Commanded";
    protected static final String thrtlChangeMaxLabelText = "Throttle Change % Maximum";
    protected static final String minThrottleLabelText = "Throttle Input Minimum";
    protected static final String isLoadCompInRatioLabelText = "Load Comp values are in ratio";
    protected static final String isMafIatInRatioLabelText = "MAF IAT Comp values are in ratio";
    protected static final String clolStatusValLabelText = "CL/OL Status Value";
    protected static final String cruiseStatusValLabelText = "Cruise/Non-cruise Status Value";
    protected static final String correctionAppliedValLabelText = "Correction % to apply";
    protected static final String maxRPMLabelText = "RPM Maximum";
    protected static final String minRPMLabelText = "RPM Minimum";
    protected static final String maxFFBLabelText = "FFB Maximum";
    protected static final String minFFBLabelText = "FFB Minimum";
    protected static final String minMafVLabelText = "MAF Voltage Minimum";
    protected static final String maxMafVLabelText = "MAF Voltage Maximum";
    protected static final String maxIatLabelText = "IAT Maximum";
    protected static final String maxAfrLabelText = "AFR Maximum";
    protected static final String minAfrLabelText = "AFR Minimum";
    protected static final String maxCorrLabelText = "Error Correction Maximum";
    protected static final String minCorrLabelText = "Error Correction Minimum";
    protected static final String maxDvdtLabelText = "dV/dt Maximum";
    protected static final String minCellHitCountLabelText = "Cell Hit Minimum Count";
    protected static final String minEngineLoadLabelText = "Engine Load Minimum";
    protected static final String minManifoldPressureLabelText = "Manifold Pressure Minimum";
    protected static final String wotStationaryLabelText = "WOT Stationary Point (Angle %)";
    protected static final String afrErrorLabelText = "AFR Error +/- % Value";
    protected static final String minWOTEnrichmentLabelText = "Min WOT Enrichment";
    protected static final String wbo2RowOffsetLabelText = "Wideband AFR Row Offset";
    protected static final String olClTransitionSkipRowsLabelText = "OL/CL Transition - #Rows to Skip";
	protected boolean isPolfTableSet;
	protected JTable columnsTable = null;
	protected JTextField thrtlAngleName = null;
	protected JTextField afLearningName = null;
	protected JTextField afCorrectionName = null;
	protected JTextField mafVName = null;
	protected JTextField mafName = null;
	protected JTextField wbAfrName = null;
	protected JTextField stockAfrName = null;
	protected JTextField rpmName = new JTextField();
	protected JTextField loadName = null;
	protected JTextField mpName = null;
	protected JTextField commAfrName = null;
	protected JTextField clolStatusName = null;
	protected JTextField cruiseStatusName = null;
	protected JTextField timeName = null;
	protected JTextField iatName = null;
	protected JTextField ffbName = null;
	protected JTextField veFlowName = null;
	protected JFormattedTextField minMafVFilter = null;
	protected JFormattedTextField maxMafVFilter = null;
	protected JFormattedTextField maxRPMFilter = null;
	protected JFormattedTextField minRPMFilter = null;
	protected JFormattedTextField maxFFBFilter = null;
	protected JFormattedTextField minFFBFilter = null;
	protected JFormattedTextField minEngineLoadFilter = null;
	protected JFormattedTextField minMPFilter = null;
	protected JFormattedTextField afrErrorFilter = null;
	protected JFormattedTextField wotEnrichmentField = null;
	protected JFormattedTextField wbo2RowOffsetField = null;
	protected JFormattedTextField olClTransitionSkipRowsField = null;
	protected JFormattedTextField maxAfrFilter = null;
	protected JFormattedTextField minAfrFilter = null;
	protected JFormattedTextField maxIatFilter = null;
	protected JFormattedTextField maxDvdtFilter = null;
	protected JFormattedTextField thrtlMinimumFilter = null;
	protected JFormattedTextField minCellHitCountFilter = null;
	protected JSpinner wotStationaryPointFilter = null;
	protected JSpinner clolStatusFilter = null;
	protected JSpinner cruiseStatusFilter = null;
	protected JSpinner correctionAppliedValue = null;
	protected JSpinner thrtlChangeMaxFilter = null;
	protected JCheckBox isLoadCompInRatioBool = null;
	protected JCheckBox isMafIatInRatioBool = null;
	protected JPanel columnsPanel = null;
	protected JPanel filtersPanel = null;
	protected Border border = BorderFactory.createEtchedBorder();
	protected Insets insets0 = new Insets(0, 0, 0, 0);
	protected Insets insets2 = new Insets(2, 20, 2, 5);
	protected Insets insets3 = new Insets(1, 0, 0, 1);
	protected Insets insets4 = new Insets(0, 3, 0, 3);
	protected Insets insets5 = new Insets(5, 5, 5, 5);
	
	protected NumberFormat doubleFmt = NumberFormat.getNumberInstance();
	protected NumberFormat intFmt = NumberFormat.getNumberInstance();
	protected ImageIcon arrowImage = new ImageIcon(getClass().getResource("/arrow.jpg"));
	protected UIDefaults zeroInsets = new UIDefaults();
    protected final int windowHeight = 540;
    protected final int windowWidth = 670;
	protected int colrow = 0;
	protected int filtrow = 0;
	
	public ColumnsFiltersSelection(boolean isPolfTableSet) {
		this.isPolfTableSet = isPolfTableSet;
        doubleFmt.setMaximumFractionDigits(2);
        intFmt.setMaximumFractionDigits(0);
        intFmt.setGroupingUsed(false);
        zeroInsets.put("Button.contentMargins", insets0);
	}
    
    public boolean getUserSettings(String[] columns) {
    	createColumnsPanel(columns);
    	createFiltersPanel();
    	
        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));
        selectionPanel.add(columnsPanel);
        selectionPanel.add(filtersPanel);
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        final JScrollPane pane = new JScrollPane(selectionPanel);
        pane.setPreferredSize(new Dimension(windowWidth, windowHeight));
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // bring scroll pane to the start
        SwingUtilities.invokeLater(new Runnable() { public void run() { pane.getVerticalScrollBar().setValue(0); } });
        
        do {
            if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null, pane, "Columns / Filters Settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE))
                return false;
        }
        while (!validate());
        
        return true;
    }
    
    abstract void addColSelection();
    abstract void addFilterSelection();
    abstract boolean validate(StringBuffer error);
    abstract boolean processDefaultButton(ActionEvent e);
    
    protected void createColumnsPanel(String[] columns) {
    	columnsPanel = new JPanel();
        GridBagLayout gbl_dataPanel = new GridBagLayout();
        gbl_dataPanel.columnWidths = new int[]{0, 0, 0, 0};
        gbl_dataPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0};
        gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        columnsPanel.setLayout(gbl_dataPanel);
        
        // Optional Note
        addNote(columnsPanel, colrow, 4, "NOTE: Fields marked with asterisk (*) are optional");
        // columns note
        addCommentLabel(columnsPanel, ++colrow, 4, "<html><b>Columns Selection - use blank row to clear optional columns.</b></html>");
        // Add columns for specific implementation
        addColSelection();
        // save dimension of all add column component to set preferred size fo rthe panel so that croll pane doesn't extends
        Dimension d = columnsPanel.getPreferredSize();
        // Columns selection table
        columnsTable = new JTable() {
            private static final long serialVersionUID = 1L;
            public boolean isCellEditable(int row, int column) { return false; };
        };
        columnsTable.setColumnSelectionAllowed(false);
        columnsTable.setCellSelectionEnabled(true);
        columnsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        columnsTable.setTableHeader(null);
        columnsTable.setModel(new DefaultTableModel(columns.length + 1, 1));
        columnsTable.setValueAt("", 0, 0);
        for (int i = 0; i < columns.length; ++i)
        	columnsTable.setValueAt(columns[i], i + 1, 0);        
        JScrollPane scrollPane = new JScrollPane(columnsTable);
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.insets = insets3;
        gbc_scrollPane.anchor = GridBagConstraints.PAGE_START;
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 3;
        gbc_scrollPane.gridy = 2;
        gbc_scrollPane.gridheight = colrow + 1;
        columnsPanel.add(scrollPane, gbc_scrollPane);
        // set size for panel to prevent scroll pane extension 
        columnsPanel.setPreferredSize(d);
    }
    
    protected void addRPMColSelection() {
        // RPM
        addLabel(columnsPanel, ++colrow, rpmLabelText);
        rpmName = addColumn(colrow, Config.getRpmColumnName());
        addCopyButton(colrow, "rpm");
    }
    
    protected void addThrottleAngleColSelection() {
        // Throttle Angle
        addLabel(columnsPanel, ++colrow, thrtlAngleLabelText);
        thrtlAngleName = addColumn(colrow, Config.getThrottleAngleColumnName());
        addCopyButton(colrow, "thrtlAngle");
    }
    
    protected void addLoadColSelection() {
        // Load
        addLabel(columnsPanel, ++colrow, loadLabelText);
        loadName = addColumn(colrow, Config.getLoadColumnName());
        addCopyButton(colrow, "load");
    }
    
    protected void addAFLearningColSelection() {
        // LTFT
        addLabel(columnsPanel, ++colrow, afLearningLabelText);
        afLearningName = addColumn(colrow, Config.getAfLearningColumnName());
        addCopyButton(colrow, "afrlearn");
    }
    
    protected void addAFCorrectionColSelection() {
        // STFT
        addLabel(columnsPanel, ++colrow, afCorrectionLabelText);
        afCorrectionName = addColumn(colrow, Config.getAfCorrectionColumnName());
        addCopyButton(colrow, "afrcorr");
    }
    
    protected void addMAFVoltageColSelection() {
        // MAF Voltage
        addLabel(columnsPanel, ++colrow, mafVLabelText);
        mafVName = addColumn(colrow, Config.getMafVoltageColumnName());
        addCopyButton(colrow, "mafv");
    }
    
    protected void addMAFColSelection() {
        // MAF
        addLabel(columnsPanel, ++colrow, mafLabelText);
        mafName = addColumn(colrow, Config.getMassAirflowColumnName());
        addCopyButton(colrow, "maf");
    }
    
    protected void addWidebandAFRColSelection() {
        // Wideband AFR
        addLabel(columnsPanel, ++colrow, wbAfrLabelText);
        wbAfrName = addColumn(colrow, Config.getWidebandAfrColumnName());
        addCopyButton(colrow, "wbafr");
    }
    
    protected void addCommandedAFRColSelection() {
        // Commanded AFR
        addLabel(columnsPanel, ++colrow, commAfrLabelText + (isPolfTableSet ? " *" : ""));
        commAfrName = addColumn(colrow, Config.getCommandedAfrColumnName());
        addCopyButton(colrow, "cmdafr");
    }
    
    protected void addStockAFRColSelection() {
        // Stock AFR
        addLabel(columnsPanel, ++colrow, stockAfrLabelText);
        stockAfrName = addColumn(colrow, Config.getAfrColumnName());
        addCopyButton(colrow, "afr");
    }
    
    protected void addClOlStatusColSelection() {
        // Closed/Open Loop Status
        addLabel(columnsPanel, ++colrow, clolStatusLabelText);
        clolStatusName = addColumn(colrow, Config.getClOlStatusColumnName());
        addCopyButton(colrow, "clolstat");
    }
    
    protected void addCruiseStatusColSelection() {
        // Cruise/Non-cruise Status
        addLabel(columnsPanel, ++colrow, cruiseStatusLabelText);
        cruiseStatusName = addColumn(colrow, Config.getCruiseStatusColumnName());
        addCopyButton(colrow, "cruisestat");
    }
    
    protected void addManifoldPressureColSelection() {
        // Manifold Pressure
        addLabel(columnsPanel, ++colrow, mpLabelText);
        mpName = addColumn(colrow, Config.getMpColumnName());
        addCopyButton(colrow, "mp");
    }
    
    protected void addTimeColSelection() {
        // Time
        addLabel(columnsPanel, ++colrow, timeLabelText);
        timeName = addColumn(colrow, Config.getTimeColumnName());
        addCopyButton(colrow, "time");
    }
    
    protected void addIATColSelection() {
        // IAT
        addLabel(columnsPanel, ++colrow, iatLabelText);
        iatName = addColumn(colrow, Config.getIatColumnName());
        addCopyButton(colrow, "iat");
    }
    
    protected void addFFBColSelection() {
        // FFB
        addLabel(columnsPanel, ++colrow, ffbLabelText);
        ffbName = addColumn(colrow, Config.getFinalFuelingBaseColumnName());
        addCopyButton(colrow, "ffb");
    }
    
    protected void addVEFlowColSelection() {
        // VE Flow
        addLabel(columnsPanel, ++colrow, veFlowLabelText);
        veFlowName = addColumn(colrow, Config.getVEFlowColumnName());
        addCopyButton(colrow, "veflow");
    }

	protected void createFiltersPanel() {
	    filtersPanel = new JPanel();
	    GridBagLayout gbl_dataPanel = new GridBagLayout();
	    gbl_dataPanel.columnWidths = new int[]{0, 0, 0};
        gbl_dataPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	    gbl_dataPanel.columnWeights = new double[]{0.0, 0.0, 1.0};
        gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        filtersPanel.setLayout(gbl_dataPanel);
	    
	    // Filters note
        addCommentLabel(filtersPanel, filtrow, 3, "<html><b>Filters Selection - use large/small values out of range to disable a filter</b></html>");
        // Add filters for specific implementation 
	    addFilterSelection();
	}
    
    
    protected void addMAFVoltageMaximumFilter() {
        // MAF Voltage Maximum Note
        addNote(filtersPanel, ++filtrow, 3, "Set this filter to process just Closed Loop part of MAF curve data");        
        addLabel(filtersPanel, ++filtrow, maxMafVLabelText);
        maxMafVFilter = addTextFilter(filtrow, doubleFmt);
        addDefaultButton(filtrow, "maxmafv");
    }
    
    protected void addMAFVoltageMinimumFilter() {
        // MAF Voltage Minimum Note
        addNote(filtersPanel, ++filtrow, 3, "Set this filter to process just Open Loop part of MAF curve data");
        addLabel(filtersPanel, ++filtrow, minMafVLabelText);
        minMafVFilter = addTextFilter(filtrow, doubleFmt);
        addDefaultButton(filtrow, "minmafv");
    }
    
    protected void addRPMMaximumFilter() {
        // RPM Maximum Note
        addNote(filtersPanel, ++filtrow, 3, "Remove data where RPM is above the filter value");
        addLabel(filtersPanel, ++filtrow, maxRPMLabelText);
        maxRPMFilter = addTextFilter(filtrow, intFmt);
        addDefaultButton(filtrow, "maxrpm");
    }
    
    protected void addRPMMinimumFilter() {
        // RPM Minimum Note
        addNote(filtersPanel, ++filtrow, 3, "Remove data where RPM is below the filter value");
        addLabel(filtersPanel, ++filtrow, minRPMLabelText);
        minRPMFilter = addTextFilter(filtrow, intFmt);
        addDefaultButton(filtrow, "minrpm");
    }

    protected void addFFBMaximumFilter() {
        // FFB Maximum Note
        addNote(filtersPanel, ++filtrow, 3, "Remove data where FFB is above the filter value");
        addLabel(filtersPanel, ++filtrow, maxFFBLabelText);
        maxFFBFilter = addTextFilter(filtrow, intFmt);
        addDefaultButton(filtrow, "maxffb");
    }
    
    protected void addFFBMinimumFilter() {
        // FFB Minimum Note
        addNote(filtersPanel, ++filtrow, 3, "Remove data where FFB is below the filter value");
        addLabel(filtersPanel, ++filtrow, minFFBLabelText);
        minFFBFilter = addTextFilter(filtrow, intFmt);
        addDefaultButton(filtrow, "minffb");
    }

    protected void addAFRMaximumFilter() {
        // AFR Maximum Note
        addNote(filtersPanel, ++filtrow, 3, "Remove data where AFR is above the specified maximum");
        addLabel(filtersPanel, ++filtrow, maxAfrLabelText);
        maxAfrFilter = addTextFilter(filtrow, doubleFmt);
        addDefaultButton(filtrow, "maxafr");
    }
    
    protected void addAFRMinimumFilter() {
        // AFR Minimum Note
        addNote(filtersPanel, ++filtrow, 3, "Remove data where AFR is below the specified minimum");
        addLabel(filtersPanel, ++filtrow, minAfrLabelText);
        minAfrFilter = addTextFilter(filtrow, doubleFmt);
        addDefaultButton(filtrow, "minafr");
    }
    
    protected void addIATMaximumFilter() {
        // Intake Air Temperature Maximum Note
        addNote(filtersPanel, ++filtrow, 3, "Set this filter to filter out data with high Intake Air Temperature");
        addLabel(filtersPanel, ++filtrow, maxIatLabelText);
        maxIatFilter = addTextFilter(filtrow, doubleFmt);
        addDefaultButton(filtrow, "maxiat");
    }
    
    protected void addDvDtMaximumFilter() {
        // dVdt Maximum Note
        addNote(filtersPanel, ++filtrow, 3, "Remove data where dV/dt is above the specified maximum");
        addLabel(filtersPanel, ++filtrow, maxDvdtLabelText);
        maxDvdtFilter = addTextFilter(filtrow, doubleFmt);
        addDefaultButton(filtrow, "maxdvdt");
    }
    
    protected void addThrottleChangeMaximumFilter() {
        // Throttle Change % Maximum Note
        addNote(filtersPanel, ++filtrow, 3, "Set this filter to filter out throttle tip-in errors");
        addLabel(filtersPanel, ++filtrow, thrtlChangeMaxLabelText);
        thrtlChangeMaxFilter = addSpinnerFilter(filtrow, Config.getThrottleChangeMaxValue(), -1, 30, 1);
        addDefaultButton(filtrow, "thrtlchange");
    }
    
    protected void addThrottleMinimumFilter() {
        // Throttle Minimum Note
        addNote(filtersPanel, ++filtrow, 3, "Remove data where Throttle Input is below the specified minimum");
        addLabel(filtersPanel, ++filtrow, minThrottleLabelText);
        thrtlMinimumFilter = addTextFilter(filtrow, doubleFmt);
        addDefaultButton(filtrow, "minthrtl");
    }
    
    protected void addEngineLoadMinimumFilter() {
        // Engine Load Minimum Note
        addNote(filtersPanel, ++filtrow, 3, "Remove data where Engine Load is below the filter value");
        addLabel(filtersPanel, ++filtrow, minEngineLoadLabelText);
        minEngineLoadFilter = addTextFilter(filtrow, doubleFmt);
        addDefaultButton(filtrow, "minengload");
    }
    
    protected void addManifoldPressureMinimumFilter() {
        // Manifold Pressure Minimum Note
        addNote(filtersPanel, ++filtrow, 3, "Remove data where Manifold Pressure is below the filter value");
        addLabel(filtersPanel, ++filtrow, minManifoldPressureLabelText);
        minMPFilter = addTextFilter(filtrow, doubleFmt);
        addDefaultButton(filtrow, "minmp");
    }
    
    protected void addCellHitCountMinimumFilter() {
        // Cell Hit Count Minimum Note
        addNote(filtersPanel, ++filtrow, 3, "Minimum cell hit count required to take data set for that cell into consideration");
        addLabel(filtersPanel, ++filtrow, minCellHitCountLabelText);
        minCellHitCountFilter = addTextFilter(filtrow, intFmt);
        addDefaultButton(filtrow, "minhitcnt");
    }
    
    protected void addWOTStationaryPointFilter() {
        // WOT Stationary Point Note
        addNote(filtersPanel, ++filtrow, 3, "CL/OL transition. Use Throttle Angle % but could use Accel Pedal Angle % instead");
        addLabel(filtersPanel, ++filtrow, wotStationaryLabelText);
        wotStationaryPointFilter = addSpinnerFilter(filtrow, Config.getWOTStationaryPointValue(), 50, 100, 5);
        addDefaultButton(filtrow, "wotpoint");
    }
    
    protected void addAFRErrorPctFilter() {
        // AFR Error Percent Note
        addNote(filtersPanel, ++filtrow, 3, "Remove data where AFR Error % exceeds desired change %");
        addLabel(filtersPanel, ++filtrow, afrErrorLabelText);
        afrErrorFilter = addTextFilter(filtrow, doubleFmt);
        addDefaultButton(filtrow, "afrerr");
    }
    
    protected void addWOTEnrichmentMinimumFilter() {
        // Minimum WOT Enrichment Note
        addNote(filtersPanel, ++filtrow, 3, "Minimum Primary Open Loop Enrichment (Throttle) - WOT override for POL Fueling table. Set above 16 if you don't have this table");
        addLabel(filtersPanel, ++filtrow, minWOTEnrichmentLabelText);
        wotEnrichmentField = addTextFilter(filtrow, doubleFmt);
        addDefaultButton(filtrow, "wotenrich");
    }
    
    protected void addWideBandAFRRowOffsetFilter() {
        // Wide Band AFR filtrow offset
        addNote(filtersPanel, ++filtrow, 3, "Delay between the ECU readings and the wideband O2 reading. Eg if correct WBO2 for row 1 is on row 2 then offset is 1");
        addLabel(filtersPanel, ++filtrow, wbo2RowOffsetLabelText);
        wbo2RowOffsetField = addTextFilter(filtrow, intFmt);
        addDefaultButton(filtrow, "wbo2offset");
    }
    
    protected void addOLCLTransitionSkipRowsFilter() {
        // OL/CL Transition skip rows
        addNote(filtersPanel, ++filtrow, 3, "Skip the first and last N rows on Open Loop / Closed Loop transition");
        addLabel(filtersPanel, ++filtrow, olClTransitionSkipRowsLabelText);
        olClTransitionSkipRowsField = addTextFilter(filtrow, intFmt);
        addDefaultButton(filtrow, "olcltransit");
    }
    
    protected void addCLOLStatusFilter() {
        // CL/OL Status value for CL Note
        addNote(filtersPanel, ++filtrow, 3, "Filter out data using logged OL/CL status (EcuTek CL: 2, OL: 4, OP2/RR CL: 8, OL: 7)");
        addLabel(filtersPanel, ++filtrow, clolStatusValLabelText);
        clolStatusFilter = addSpinnerFilter(filtrow, Config.getClOlStatusValue(), -1, 10, 1);
        addDefaultButton(filtrow, "clolstatus");
    }
    
    protected void addCruiseStatusFilter() {
        // Cruise Status value for LC Note
        addNote(filtersPanel, ++filtrow, 3, "Filter out data using logged Cruise/Non-cruise status (leave at -1 if you ROM has only 1 Load Comp table)");
        addLabel(filtersPanel, ++filtrow, cruiseStatusValLabelText);
        cruiseStatusFilter = addSpinnerFilter(filtrow, Config.getCruiseStatusValue(), -1, 10, 1);
        addDefaultButton(filtrow, "cruisestatus");
    }
    
    protected void addCorrectionAppliedValue() {
        // Correction Applied Status value for LC Note
        addNote(filtersPanel, ++filtrow, 3, "Percent of correction to apply");
        addLabel(filtersPanel, ++filtrow, correctionAppliedValLabelText);
        correctionAppliedValue = addSpinnerFilter(filtrow, Config.getLCCorrectionAppliedValue(), 5, 100, 5);
        addDefaultButton(filtrow, "corrapply");
    }
    
    protected void addLoadCompInRatioFlag() {
        // Load Compensation values in ratio
        addNote(filtersPanel, ++filtrow, 3, "Set this if your Load Compensation table values specified as ratio (eg EcuTek)");
        addLabel(filtersPanel, ++filtrow, isLoadCompInRatioLabelText);
        isLoadCompInRatioBool = addFlag(filtrow);
    }
    
    protected void addMafIatInRatioFlag() {
        // MAF IAT Compensation values in ratio
        addNote(filtersPanel, ++filtrow, 3, "Set this if your MAF IAT Compensation table values specified as ratio (eg EcuTek)");
        addLabel(filtersPanel, ++filtrow, isMafIatInRatioLabelText);
        isMafIatInRatioBool = addFlag(filtrow);        
    }
    
    private boolean validate() {
    	boolean ret = true;
    	StringBuffer error = new StringBuffer("");
    	ret = validate(error);    	
    	if (!ret)
    		JOptionPane.showMessageDialog(null, error, "Error", JOptionPane.ERROR_MESSAGE);
    	return ret;
    }
    
    private String isEmpty(String current) {
    	if (current.equals(Config.NO_NAME))
    		return "";
    	return current;
    }

    private void addCommentLabel(JPanel panel, int row, int colspan, String text) {
        JLabel label = new JLabel(text);
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.anchor = GridBagConstraints.WEST;
        gbc_label.fill = GridBagConstraints.HORIZONTAL;
        gbc_label.insets = insets5;
        gbc_label.gridx = 0;
        gbc_label.gridy = row;
        gbc_label.gridwidth = colspan;
        panel.add(label, gbc_label);
    }
    
    private void addLabel(JPanel panel, int row, String text) {
        JLabel label = new JLabel(text);
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.anchor = GridBagConstraints.EAST;
        gbc_label.insets = insets3;
        gbc_label.gridx = 0;
        gbc_label.gridy = row;
        panel.add(label, gbc_label);
    }
    
    private void addNote(JPanel panel, int row, int colspan, String note) {
        JEditorPane label = createWrapLabel(note);
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.anchor = GridBagConstraints.WEST;
        gbc_label.fill = GridBagConstraints.HORIZONTAL;
        gbc_label.insets = insets5;
        gbc_label.gridx = 0;
        gbc_label.gridy = row;
        gbc_label.gridwidth = colspan;
        panel.add(label, gbc_label);
    }
    
    private JEditorPane createWrapLabel(String text) {
        JEditorPane label = new JEditorPane();
        label.setEditable(false);
        label.setForeground(Color.BLUE);
        label.setBackground(new Color(0,0,0,0));
        label.setText(text);
        return label;
    }
    
    private JTextField addColumn(int row, String value) {
    	JTextField textField = new JTextField(isEmpty(value));
    	textField.setColumns(17);
    	textField.setEditable(false);
    	textField.setBackground(Color.WHITE);
    	textField.setMargin(insets4);
        GridBagConstraints gbc_textField = new GridBagConstraints();
        gbc_textField.anchor = GridBagConstraints.WEST;
        gbc_textField.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField.insets = insets3;
        gbc_textField.gridx = 1;
        gbc_textField.gridy = row;
        columnsPanel.add(textField, gbc_textField);
        return textField;
    }
    
    private void addCopyButton(int row, String action) {
	    JButton button = new JButton("", arrowImage);
	    button.putClientProperty("Nimbus.Overrides", zeroInsets);
	    button.setMargin(insets0);
	    button.setBorderPainted(false);
	    button.setContentAreaFilled(false);
	    GridBagConstraints gbc_button = new GridBagConstraints();
	    gbc_button.anchor = GridBagConstraints.CENTER;
	    gbc_button.insets = insets0;
	    gbc_button.gridx = 2;
	    gbc_button.gridy = row;
	    button.setActionCommand(action);
	    button.addActionListener(this);
	    columnsPanel.add(button, gbc_button);
    }

    private JFormattedTextField addTextFilter(int row, NumberFormat format) {
    	JFormattedTextField textField = new JFormattedTextField(format);
    	textField.setColumns(6);
    	textField.setBackground(Color.WHITE);
    	textField.setMargin(insets4);
        GridBagConstraints gbc_textField = new GridBagConstraints();
        gbc_textField.anchor = GridBagConstraints.WEST;
        gbc_textField.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField.insets = insets3;
        gbc_textField.gridx = 1;
        gbc_textField.gridy = row;
        filtersPanel.add(textField, gbc_textField);
        return textField;
    }
    
    private JSpinner addSpinnerFilter(int row, int value, int min, int max, int step) {
    	JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        GridBagConstraints gbc_spinner = new GridBagConstraints();
        gbc_spinner.anchor = GridBagConstraints.WEST;
        gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
        gbc_spinner.insets = insets3;
        gbc_spinner.gridx = 1;
        gbc_spinner.gridy = row;
        filtersPanel.add(spinner, gbc_spinner);
        return spinner;
    }
    
    private JCheckBox addFlag(int row) {
        JCheckBox flag = new JCheckBox();
        GridBagConstraints gbc_flag = new GridBagConstraints();
        gbc_flag.anchor = GridBagConstraints.WEST;
        gbc_flag.insets = insets3;
        gbc_flag.gridx = 1;
        gbc_flag.gridy = row;
        filtersPanel.add(flag, gbc_flag);
        return flag;
    }
    
    private void addDefaultButton(int row, String action) {
	    JButton button = new JButton("default");
	    GridBagConstraints gbc_button = new GridBagConstraints();
	    gbc_button.anchor = GridBagConstraints.WEST;
	    gbc_button.insets = insets2;
	    gbc_button.gridx = 2;
	    gbc_button.gridy = filtrow;
	    button.setActionCommand(action);
	    button.addActionListener(this);
	    filtersPanel.add(button, gbc_button);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	if (processDefaultButton(e))
    		return;
    	int row = columnsTable.getSelectedRow();
    	if (row < 0) {
    		JOptionPane.showMessageDialog(null, "Please select column name from the list", "Error", JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    	String value = columnsTable.getValueAt(row, 0).toString();
    	JTextField textField = null;
        if ("thrtlAngle".equals(e.getActionCommand()))
        	textField = thrtlAngleName;
        else if ("afrlearn".equals(e.getActionCommand()))
        	textField = afLearningName;
        else if ("afrcorr".equals(e.getActionCommand()))
        	textField = afCorrectionName;
        else if ("mafv".equals(e.getActionCommand()))
        	textField = mafVName;
        else if ("maf".equals(e.getActionCommand()))
        	textField = mafName;
        else if ("wbafr".equals(e.getActionCommand()))
        	textField = wbAfrName;
        else if ("rpm".equals(e.getActionCommand()))
        	textField = rpmName;
        else if ("load".equals(e.getActionCommand()))
        	textField = loadName;
        else if ("mp".equals(e.getActionCommand()))
        	textField = mpName;
        else if ("cmdafr".equals(e.getActionCommand()))
        	textField = commAfrName;
        else if ("afr".equals(e.getActionCommand()))
        	textField = stockAfrName;
        else if ("clolstat".equals(e.getActionCommand()))
        	textField = clolStatusName;
        else if ("cruisestat".equals(e.getActionCommand()))
        	textField = cruiseStatusName;
        else if ("time".equals(e.getActionCommand()))
        	textField = timeName;
        else if ("iat".equals(e.getActionCommand()))
        	textField = iatName;
        else if ("ffb".equals(e.getActionCommand()))
        	textField = ffbName;
        else if ("veflow".equals(e.getActionCommand()))
        	textField = veFlowName;
        else
        	return;
        textField.setText(value);
        textField.setCaretPosition(0);
    }
}

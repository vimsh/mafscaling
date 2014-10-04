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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
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
	protected JTextField rpmName = null;
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
	protected JPanel selectionPanel = null;
	protected Border border = null;
	protected Dimension minTextDimension = null;
	protected Dimension minFilterDimension = null;
	protected Dimension minDefaultButtonDimension = null;
	protected Insets insets0 = null;
	protected Insets insets1 = null;
	protected Insets insets2 = null;
	protected Insets insets3 = null;
	protected NumberFormat doubleFmt = null;
	protected NumberFormat intFmt = null;
	protected ImageIcon arrowImage = null;
	protected int row = 0;
	
	public ColumnsFiltersSelection(boolean isPolfTableSet) {
		this.isPolfTableSet = isPolfTableSet;
		border = BorderFactory.createEtchedBorder();
        minTextDimension = new Dimension(200, 16);
        minFilterDimension = new Dimension(80, 16);
        minDefaultButtonDimension = new Dimension(60, 18);
        insets0 = new Insets(0, 0, 0, 0);
        insets1 = new Insets(1, 1, 1, 1);
        insets2 = new Insets(2, 2, 2, 2);
        insets3 = new Insets(3, 3, 3, 3);
        doubleFmt = NumberFormat.getNumberInstance();
        doubleFmt.setMaximumFractionDigits(2);
        intFmt = NumberFormat.getNumberInstance();
        intFmt.setMaximumFractionDigits(0);
        intFmt.setGroupingUsed(false);
        arrowImage = new ImageIcon(getClass().getResource("/arrow.jpg"));
	}
    
    public boolean getUserSettings(String[] columns) {
        selectionPanel = new JPanel();
        GridBagLayout gbl_dataPanel = new GridBagLayout();
        gbl_dataPanel.columnWidths = new int[]{0, 0, 0, 0};
        gbl_dataPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_dataPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0};
        gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        selectionPanel.setLayout(gbl_dataPanel);
        
        // Optional Note
        JLabel optNoteLabel = new JLabel("NOTE: Fields marked with asterisk (*) are optional");
        optNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_optNoteLabel = new GridBagConstraints();
        gbc_optNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_optNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_optNoteLabel.insets = new Insets(5, 50, 5, 5);
        gbc_optNoteLabel.gridx = 0;
        gbc_optNoteLabel.gridy = row;
        gbc_optNoteLabel.gridwidth = 4;
        selectionPanel.add(optNoteLabel, gbc_optNoteLabel);

        row += 1;
        // columns note
        JLabel colNoteLabel = new JLabel("<html><b>Columns Selection - use blank row to clear optional columns</b></html>");
        GridBagConstraints gbc_colNoteLabel = new GridBagConstraints();
        gbc_colNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_colNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_colNoteLabel.insets = new Insets(5, 5, 5, 5);
        gbc_colNoteLabel.gridx = 0;
        gbc_colNoteLabel.gridy = row;
        gbc_colNoteLabel.gridwidth = 4;
        selectionPanel.add(colNoteLabel, gbc_colNoteLabel);

        addColSelection();

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

        GridBagConstraints gbc_selectionPanel = new GridBagConstraints();
        gbc_selectionPanel.insets = insets3;
        gbc_selectionPanel.anchor = GridBagConstraints.SOUTH;
        gbc_selectionPanel.fill = GridBagConstraints.BOTH;
        gbc_selectionPanel.gridx = 3;
        gbc_selectionPanel.gridy = 2;
        gbc_selectionPanel.gridheight = getColSelectionGridHeight();
        JScrollPane scrollPane = new JScrollPane(columnsTable);
        selectionPanel.add(scrollPane, gbc_selectionPanel);

        row += 1;
        // filters note
        JLabel filtNoteLabel = new JLabel("<html><b>Filters Selection - use large/small values out of range to disable a filter</b></html>");
        GridBagConstraints gbc_filtNoteLabel = new GridBagConstraints();
        gbc_filtNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_filtNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_filtNoteLabel.insets = new Insets(5, 5, 5, 5);
        gbc_filtNoteLabel.gridx = 0;
        gbc_filtNoteLabel.gridy = row;
        gbc_filtNoteLabel.gridwidth = 4;
        selectionPanel.add(filtNoteLabel, gbc_filtNoteLabel);

        addFilterSelection();
        
        // Set window params
        selectionPanel.setPreferredSize(new Dimension(650, getWindowHeight()));
        JScrollPane pane = new JScrollPane(selectionPanel);
        pane.setPreferredSize(new Dimension(670, 540));
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JComponent[] inputs = new JComponent[] { pane };
        do {
            if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null, inputs, "Columns / Filters Settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE))
                return false;
        }
        while (!validate());
        
        return true;
    }
    
    abstract int getWindowHeight();
    abstract int getColSelectionGridHeight();
    abstract void addColSelection();
    abstract void addFilterSelection();
    abstract boolean validate(StringBuffer error);
    abstract boolean processDefaultButton(ActionEvent e);
    
    protected void addRPMColSelection() {
        row += 1;
        // RPM
        JLabel rpmLabel = new JLabel(rpmLabelText);
        GridBagConstraints gbc_rpmLabel = new GridBagConstraints();
        gbc_rpmLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_rpmLabel.insets = insets3;
        gbc_rpmLabel.gridx = 0;
        gbc_rpmLabel.gridy = row;
        selectionPanel.add(rpmLabel, gbc_rpmLabel);
        
        rpmName = new JTextField(isEmpty(Config.getRpmColumnName()));
        rpmName.setMinimumSize(minTextDimension);
        rpmName.setEditable(false);
        rpmName.setBackground(Color.WHITE);
        GridBagConstraints gbc_rpmName = new GridBagConstraints();
        gbc_rpmName.anchor = GridBagConstraints.NORTHWEST;
        gbc_rpmName.insets = insets3;
        gbc_rpmName.gridx = 1;
        gbc_rpmName.gridy = row;
        selectionPanel.add(rpmName, gbc_rpmName);

        JButton rpmButton = new JButton("", arrowImage);
        rpmButton.setMargin(insets0);
        rpmButton.setBorderPainted(false);
        rpmButton.setContentAreaFilled(false);
        GridBagConstraints gbc_rpmButton = new GridBagConstraints();
        gbc_rpmButton.anchor = GridBagConstraints.CENTER;
        gbc_rpmButton.insets = insets1;
        gbc_rpmButton.gridx = 2;
        gbc_rpmButton.gridy = row;
        rpmButton.setActionCommand("rpm");
        rpmButton.addActionListener(this);
        selectionPanel.add(rpmButton, gbc_rpmButton);
    }
    
    protected void addThrottleAngleColSelection() {
        row += 1;
        // Throttle Angle
        JLabel thrtlAngleLabel = new JLabel(thrtlAngleLabelText);
        GridBagConstraints gbc_thrtlAngleLabel = new GridBagConstraints();
        gbc_thrtlAngleLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_thrtlAngleLabel.insets = insets3;
        gbc_thrtlAngleLabel.gridx = 0;
        gbc_thrtlAngleLabel.gridy = row;
        selectionPanel.add(thrtlAngleLabel, gbc_thrtlAngleLabel);
        
        thrtlAngleName = new JTextField(isEmpty(Config.getThrottleAngleColumnName()));
        thrtlAngleName.setMinimumSize(minTextDimension);
        thrtlAngleName.setEditable(false);
        thrtlAngleName.setBackground(Color.WHITE);
        GridBagConstraints gbc_thrtlAngleName= new GridBagConstraints();
        gbc_thrtlAngleName.anchor = GridBagConstraints.NORTHWEST;
        gbc_thrtlAngleName.insets = insets3;
        gbc_thrtlAngleName.gridx = 1;
        gbc_thrtlAngleName.gridy = row;
        selectionPanel.add(thrtlAngleName, gbc_thrtlAngleName);

        JButton thrtlAngleButton = new JButton("", arrowImage);
        thrtlAngleButton.setMargin(insets0);
        thrtlAngleButton.setBorderPainted(false);
        thrtlAngleButton.setContentAreaFilled(false);
        GridBagConstraints gbc_thrtlAngleButton = new GridBagConstraints();
        gbc_thrtlAngleButton.anchor = GridBagConstraints.CENTER;
        gbc_thrtlAngleButton.insets = insets1;
        gbc_thrtlAngleButton.gridx = 2;
        gbc_thrtlAngleButton.gridy = row;
        thrtlAngleButton.setActionCommand("thrtlAngle");
        thrtlAngleButton.addActionListener(this);
        selectionPanel.add(thrtlAngleButton, gbc_thrtlAngleButton);
    }
    
    protected void addLoadColSelection() {
        row += 1;
        // Load
        JLabel loadLabel = new JLabel(loadLabelText);
        GridBagConstraints gbc_loadLabel = new GridBagConstraints();
        gbc_loadLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_loadLabel.insets = insets3;
        gbc_loadLabel.gridx = 0;
        gbc_loadLabel.gridy = row;
        selectionPanel.add(loadLabel, gbc_loadLabel);
        
        loadName = new JTextField(isEmpty(Config.getLoadColumnName()));
        loadName.setMinimumSize(minTextDimension);
        loadName.setEditable(false);
        loadName.setBackground(Color.WHITE);
        GridBagConstraints gbc_loadName = new GridBagConstraints();
        gbc_loadName.anchor = GridBagConstraints.NORTHWEST;
        gbc_loadName.insets = insets3;
        gbc_loadName.gridx = 1;
        gbc_loadName.gridy = row;
        selectionPanel.add(loadName, gbc_loadName);

        JButton loadButton = new JButton("", arrowImage);
        loadButton.setMargin(insets0);
        loadButton.setBorderPainted(false);
        loadButton.setContentAreaFilled(false);
        GridBagConstraints gbc_loadButton = new GridBagConstraints();
        gbc_loadButton.anchor = GridBagConstraints.CENTER;
        gbc_loadButton.insets = insets1;
        gbc_loadButton.gridx = 2;
        gbc_loadButton.gridy = row;
        loadButton.setActionCommand("load");
        loadButton.addActionListener(this);
        selectionPanel.add(loadButton, gbc_loadButton);
    }
    
    protected void addAFLearningColSelection() {
        row += 1;
        // LTFT
        JLabel afLearningLabel = new JLabel(afLearningLabelText);
        GridBagConstraints gbc_afLearningLabel = new GridBagConstraints();
        gbc_afLearningLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_afLearningLabel.insets = insets3;
        gbc_afLearningLabel.gridx = 0;
        gbc_afLearningLabel.gridy = row;
        selectionPanel.add(afLearningLabel, gbc_afLearningLabel);
        
        afLearningName = new JTextField(isEmpty(Config.getAfLearningColumnName()));
        afLearningName.setMinimumSize(minTextDimension);
        afLearningName.setEditable(false);
        afLearningName.setBackground(Color.WHITE);
        GridBagConstraints gbc_afLearningName = new GridBagConstraints();
        gbc_afLearningName.anchor = GridBagConstraints.NORTHWEST;
        gbc_afLearningName.insets = insets3;
        gbc_afLearningName.gridx = 1;
        gbc_afLearningName.gridy = row;
        selectionPanel.add(afLearningName, gbc_afLearningName);

        JButton afLearningButton = new JButton("", arrowImage);
        afLearningButton.setMargin(insets0);
        afLearningButton.setBorderPainted(false);
        afLearningButton.setContentAreaFilled(false);
        GridBagConstraints gbc_afLearningButton = new GridBagConstraints();
        gbc_afLearningButton.anchor = GridBagConstraints.CENTER;
        gbc_afLearningButton.insets = insets1;
        gbc_afLearningButton.gridx = 2;
        gbc_afLearningButton.gridy = row;
        afLearningButton.setActionCommand("afrlearn");
        afLearningButton.addActionListener(this);
        selectionPanel.add(afLearningButton, gbc_afLearningButton);
    }
    
    protected void addAFCorrectionColSelection() {
        row += 1;
        // STFT
        JLabel afCorrectionLabel = new JLabel(afCorrectionLabelText);
        GridBagConstraints gbc_afCorrectionLabel = new GridBagConstraints();
        gbc_afCorrectionLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_afCorrectionLabel.insets = insets3;
        gbc_afCorrectionLabel.gridx = 0;
        gbc_afCorrectionLabel.gridy = row;
        selectionPanel.add(afCorrectionLabel, gbc_afCorrectionLabel);
        
        afCorrectionName = new JTextField(isEmpty(Config.getAfCorrectionColumnName()));
        afCorrectionName.setMinimumSize(minTextDimension);
        afCorrectionName.setEditable(false);
        afCorrectionName.setBackground(Color.WHITE);
        GridBagConstraints gbc_afCorrectionName = new GridBagConstraints();
        gbc_afCorrectionName.anchor = GridBagConstraints.NORTHWEST;
        gbc_afCorrectionName.insets = insets3;
        gbc_afCorrectionName.gridx = 1;
        gbc_afCorrectionName.gridy = row;
        selectionPanel.add(afCorrectionName, gbc_afCorrectionName);

        JButton afCorrectionButton = new JButton("", arrowImage);
        afCorrectionButton.setMargin(insets0);
        afCorrectionButton.setBorderPainted(false);
        afCorrectionButton.setContentAreaFilled(false);
        GridBagConstraints gbc_afCorrectionButton = new GridBagConstraints();
        gbc_afCorrectionButton.anchor = GridBagConstraints.CENTER;
        gbc_afCorrectionButton.insets = insets1;
        gbc_afCorrectionButton.gridx = 2;
        gbc_afCorrectionButton.gridy = row;
        afCorrectionButton.setActionCommand("afrcorr");
        afCorrectionButton.addActionListener(this);
        selectionPanel.add(afCorrectionButton, gbc_afCorrectionButton);
    }
    
    protected void addMAFVoltageColSelection() {
        row += 1;
        // MAF Voltage
        JLabel mafVLabel = new JLabel(mafVLabelText);
        GridBagConstraints gbc_mafVLabel = new GridBagConstraints();
        gbc_mafVLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_mafVLabel.insets = insets3;
        gbc_mafVLabel.gridx = 0;
        gbc_mafVLabel.gridy = row;
        selectionPanel.add(mafVLabel, gbc_mafVLabel);
        
        mafVName = new JTextField(isEmpty(Config.getMafVoltageColumnName()));
        mafVName.setMinimumSize(minTextDimension);
        mafVName.setEditable(false);
        mafVName.setBackground(Color.WHITE);
        GridBagConstraints gbc_mafVName = new GridBagConstraints();
        gbc_mafVName.anchor = GridBagConstraints.NORTHWEST;
        gbc_mafVName.insets = insets3;
        gbc_mafVName.gridx = 1;
        gbc_mafVName.gridy = row;
        selectionPanel.add(mafVName, gbc_mafVName);

        JButton mafVButton = new JButton("", arrowImage);
        mafVButton.setMargin(insets0);
        mafVButton.setBorderPainted(false);
        mafVButton.setContentAreaFilled(false);
        GridBagConstraints gbc_mafVButton = new GridBagConstraints();
        gbc_mafVButton.anchor = GridBagConstraints.CENTER;
        gbc_mafVButton.insets = insets1;
        gbc_mafVButton.gridx = 2;
        gbc_mafVButton.gridy = row;
        mafVButton.setActionCommand("mafv");
        mafVButton.addActionListener(this);
        selectionPanel.add(mafVButton, gbc_mafVButton);
    }
    
    protected void addMAFColSelection() {
        row += 1;
        // MAF
        JLabel mafLabel = new JLabel(mafLabelText);
        GridBagConstraints gbc_mafLabel = new GridBagConstraints();
        gbc_mafLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_mafLabel.insets = insets3;
        gbc_mafLabel.gridx = 0;
        gbc_mafLabel.gridy = row;
        selectionPanel.add(mafLabel, gbc_mafLabel);
        
        mafName = new JTextField(isEmpty(Config.getMassAirflowColumnName()));
        mafName.setMinimumSize(minTextDimension);
        mafName.setEditable(false);
        mafName.setBackground(Color.WHITE);
        GridBagConstraints gbc_mafName = new GridBagConstraints();
        gbc_mafName.anchor = GridBagConstraints.NORTHWEST;
        gbc_mafName.insets = insets3;
        gbc_mafName.gridx = 1;
        gbc_mafName.gridy = row;
        selectionPanel.add(mafName, gbc_mafName);

        JButton mafButton = new JButton("", arrowImage);
        mafButton.setMargin(insets0);
        mafButton.setBorderPainted(false);
        mafButton.setContentAreaFilled(false);
        GridBagConstraints gbc_mafButton = new GridBagConstraints();
        gbc_mafButton.anchor = GridBagConstraints.CENTER;
        gbc_mafButton.insets = insets1;
        gbc_mafButton.gridx = 2;
        gbc_mafButton.gridy = row;
        mafButton.setActionCommand("maf");
        mafButton.addActionListener(this);
        selectionPanel.add(mafButton, gbc_mafButton);
    }
    
    protected void addWidebandAFRColSelection() {
        row += 1;
        // Wideband AFR
        JLabel wbAfrLabel = new JLabel(wbAfrLabelText);
        GridBagConstraints gbc_wbAfrLabel = new GridBagConstraints();
        gbc_wbAfrLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_wbAfrLabel.insets = insets3;
        gbc_wbAfrLabel.gridx = 0;
        gbc_wbAfrLabel.gridy = row;
        selectionPanel.add(wbAfrLabel, gbc_wbAfrLabel);
        
        wbAfrName = new JTextField(isEmpty(Config.getWidebandAfrColumnName()));
        wbAfrName.setMinimumSize(minTextDimension);
        wbAfrName.setEditable(false);
        wbAfrName.setBackground(Color.WHITE);
        GridBagConstraints gbc_wbAfrName = new GridBagConstraints();
        gbc_wbAfrName.anchor = GridBagConstraints.NORTHWEST;
        gbc_wbAfrName.insets = insets3;
        gbc_wbAfrName.gridx = 1;
        gbc_wbAfrName.gridy = row;
        selectionPanel.add(wbAfrName, gbc_wbAfrName);

        JButton wbAfrButton = new JButton("", arrowImage);
        wbAfrButton.setMargin(insets0);
        wbAfrButton.setBorderPainted(false);
        wbAfrButton.setContentAreaFilled(false);
        GridBagConstraints gbc_wbAfrButton = new GridBagConstraints();
        gbc_wbAfrButton.anchor = GridBagConstraints.CENTER;
        gbc_wbAfrButton.insets = insets1;
        gbc_wbAfrButton.gridx = 2;
        gbc_wbAfrButton.gridy = row;
        wbAfrButton.setActionCommand("wbafr");
        wbAfrButton.addActionListener(this);
        selectionPanel.add(wbAfrButton, gbc_wbAfrButton);
    }
    
    protected void addCommandedAFRColSelection() {
        row += 1;
        // Commanded AFR
        JLabel commAfrLabel = new JLabel(commAfrLabelText + (isPolfTableSet ? " *" : ""));
        GridBagConstraints gbc_commAfrLabel = new GridBagConstraints();
        gbc_commAfrLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_commAfrLabel.insets = insets3;
        gbc_commAfrLabel.gridx = 0;
        gbc_commAfrLabel.gridy = row;
        selectionPanel.add(commAfrLabel, gbc_commAfrLabel);
        
        commAfrName = new JTextField(isEmpty(Config.getCommandedAfrColumnName()));
        commAfrName.setMinimumSize(minTextDimension);
        commAfrName.setEditable(false);
        commAfrName.setBackground(Color.WHITE);
        GridBagConstraints gbc_commAfrName = new GridBagConstraints();
        gbc_commAfrName.anchor = GridBagConstraints.NORTHWEST;
        gbc_commAfrName.insets = insets3;
        gbc_commAfrName.gridx = 1;
        gbc_commAfrName.gridy = row;
        selectionPanel.add(commAfrName, gbc_commAfrName);

        JButton commAfrButton = new JButton("", arrowImage);
        commAfrButton.setMargin(insets0);
        commAfrButton.setBorderPainted(false);
        commAfrButton.setContentAreaFilled(false);
        GridBagConstraints gbc_commAfrButton = new GridBagConstraints();
        gbc_commAfrButton.anchor = GridBagConstraints.CENTER;
        gbc_commAfrButton.insets = insets1;
        gbc_commAfrButton.gridx = 2;
        gbc_commAfrButton.gridy = row;
        commAfrButton.setActionCommand("cmdafr");
        commAfrButton.addActionListener(this);
        selectionPanel.add(commAfrButton, gbc_commAfrButton);
    }
    
    protected void addStockAFRColSelection() {
        row += 1;
        // Stock AFR
        JLabel stockAfrLabel = new JLabel(stockAfrLabelText);
        GridBagConstraints gbc_stockAfrLabel = new GridBagConstraints();
        gbc_stockAfrLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_stockAfrLabel.insets = insets3;
        gbc_stockAfrLabel.gridx = 0;
        gbc_stockAfrLabel.gridy = row;
        selectionPanel.add(stockAfrLabel, gbc_stockAfrLabel);
        
        stockAfrName = new JTextField(isEmpty(Config.getAfrColumnName()));
        stockAfrName.setMinimumSize(minTextDimension);
        stockAfrName.setEditable(false);
        stockAfrName.setBackground(Color.WHITE);
        GridBagConstraints gbc_stockAfrName = new GridBagConstraints();
        gbc_stockAfrName.anchor = GridBagConstraints.NORTHWEST;
        gbc_stockAfrName.insets = insets3;
        gbc_stockAfrName.gridx = 1;
        gbc_stockAfrName.gridy = row;
        selectionPanel.add(stockAfrName, gbc_stockAfrName);

        JButton stockAfrButton = new JButton("", arrowImage);
        stockAfrButton.setMargin(insets0);
        stockAfrButton.setBorderPainted(false);
        stockAfrButton.setContentAreaFilled(false);
        GridBagConstraints gbc_stockAfrButton = new GridBagConstraints();
        gbc_stockAfrButton.anchor = GridBagConstraints.CENTER;
        gbc_stockAfrButton.insets = insets1;
        gbc_stockAfrButton.gridx = 2;
        gbc_stockAfrButton.gridy = row;
        stockAfrButton.setActionCommand("afr");
        stockAfrButton.addActionListener(this);
        selectionPanel.add(stockAfrButton, gbc_stockAfrButton);
    }
    
    protected void addClOlStatusColSelection() {
        row += 1;
        // Closed/Open Loop Status
        JLabel clolStatusLabel = new JLabel(clolStatusLabelText);
        GridBagConstraints gbc_clolStatusLabel = new GridBagConstraints();
        gbc_clolStatusLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_clolStatusLabel.insets = insets3;
        gbc_clolStatusLabel.gridx = 0;
        gbc_clolStatusLabel.gridy = row;
        selectionPanel.add(clolStatusLabel, gbc_clolStatusLabel);
        
        clolStatusName = new JTextField(isEmpty(Config.getClOlStatusColumnName()));
        clolStatusName.setMinimumSize(minTextDimension);
        clolStatusName.setEditable(false);
        clolStatusName.setBackground(Color.WHITE);
        GridBagConstraints gbc_clolStatusName = new GridBagConstraints();
        gbc_clolStatusName.anchor = GridBagConstraints.NORTHWEST;
        gbc_clolStatusName.insets = insets3;
        gbc_clolStatusName.gridx = 1;
        gbc_clolStatusName.gridy = row;
        selectionPanel.add(clolStatusName, gbc_clolStatusName);

        JButton clolStatusButton = new JButton("", arrowImage);
        clolStatusButton.setMargin(insets0);
        clolStatusButton.setBorderPainted(false);
        clolStatusButton.setContentAreaFilled(false);
        GridBagConstraints gbc_clolStatusButton = new GridBagConstraints();
        gbc_clolStatusButton.anchor = GridBagConstraints.CENTER;
        gbc_clolStatusButton.insets = insets1;
        gbc_clolStatusButton.gridx = 2;
        gbc_clolStatusButton.gridy = row;
        clolStatusButton.setActionCommand("clolstat");
        clolStatusButton.addActionListener(this);
        selectionPanel.add(clolStatusButton, gbc_clolStatusButton);
    }
    
    protected void addCruiseStatusColSelection() {
        row += 1;
        // Cruise/Non-cruise Status
        JLabel cruiseStatusLabel = new JLabel(cruiseStatusLabelText);
        GridBagConstraints gbc_cruiseStatusLabel = new GridBagConstraints();
        gbc_cruiseStatusLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_cruiseStatusLabel.insets = insets3;
        gbc_cruiseStatusLabel.gridx = 0;
        gbc_cruiseStatusLabel.gridy = row;
        selectionPanel.add(cruiseStatusLabel, gbc_cruiseStatusLabel);
        
        cruiseStatusName = new JTextField(isEmpty(Config.getCruiseStatusColumnName()));
        cruiseStatusName.setMinimumSize(minTextDimension);
        cruiseStatusName.setEditable(false);
        cruiseStatusName.setBackground(Color.WHITE);
        GridBagConstraints gbc_cruiseStatusName = new GridBagConstraints();
        gbc_cruiseStatusName.anchor = GridBagConstraints.NORTHWEST;
        gbc_cruiseStatusName.insets = insets3;
        gbc_cruiseStatusName.gridx = 1;
        gbc_cruiseStatusName.gridy = row;
        selectionPanel.add(cruiseStatusName, gbc_cruiseStatusName);

        JButton cruiseStatusButton = new JButton("", arrowImage);
        cruiseStatusButton.setMargin(insets0);
        cruiseStatusButton.setBorderPainted(false);
        cruiseStatusButton.setContentAreaFilled(false);
        GridBagConstraints gbc_cruiseStatusButton = new GridBagConstraints();
        gbc_cruiseStatusButton.anchor = GridBagConstraints.CENTER;
        gbc_cruiseStatusButton.insets = insets1;
        gbc_cruiseStatusButton.gridx = 2;
        gbc_cruiseStatusButton.gridy = row;
        cruiseStatusButton.setActionCommand("cruisestat");
        cruiseStatusButton.addActionListener(this);
        selectionPanel.add(cruiseStatusButton, gbc_cruiseStatusButton);
    }
    
    protected void addManifoldPressureColSelection() {
        row += 1;
        // Manifold Pressure
        JLabel mpLabel = new JLabel(mpLabelText);
        GridBagConstraints gbc_mpLabel = new GridBagConstraints();
        gbc_mpLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_mpLabel.insets = insets3;
        gbc_mpLabel.gridx = 0;
        gbc_mpLabel.gridy = row;
        selectionPanel.add(mpLabel, gbc_mpLabel);
        
        mpName = new JTextField(isEmpty(Config.getMpColumnName()));
        mpName.setMinimumSize(minTextDimension);
        mpName.setEditable(false);
        mpName.setBackground(Color.WHITE);
        GridBagConstraints gbc_mpName = new GridBagConstraints();
        gbc_mpName.anchor = GridBagConstraints.NORTHWEST;
        gbc_mpName.insets = insets3;
        gbc_mpName.gridx = 1;
        gbc_mpName.gridy = row;
        selectionPanel.add(mpName, gbc_mpName);

        JButton mpButton = new JButton("", arrowImage);
        mpButton.setMargin(insets0);
        mpButton.setBorderPainted(false);
        mpButton.setContentAreaFilled(false);
        GridBagConstraints gbc_mpButton = new GridBagConstraints();
        gbc_mpButton.anchor = GridBagConstraints.CENTER;
        gbc_mpButton.insets = insets1;
        gbc_mpButton.gridx = 2;
        gbc_mpButton.gridy = row;
        mpButton.setActionCommand("mp");
        mpButton.addActionListener(this);
        selectionPanel.add(mpButton, gbc_mpButton);
    }
    
    protected void addTimeColSelection() {
        row += 1;
        // Time
        JLabel timeLabel = new JLabel(timeLabelText);
        GridBagConstraints gbc_timeLabel = new GridBagConstraints();
        gbc_timeLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_timeLabel.insets = insets3;
        gbc_timeLabel.gridx = 0;
        gbc_timeLabel.gridy = row;
        selectionPanel.add(timeLabel, gbc_timeLabel);
        
        timeName = new JTextField(isEmpty(Config.getTimeColumnName()));
        timeName.setMinimumSize(minTextDimension);
        timeName.setEditable(false);
        timeName.setBackground(Color.WHITE);
        GridBagConstraints gbc_timeName = new GridBagConstraints();
        gbc_timeName.anchor = GridBagConstraints.NORTHWEST;
        gbc_timeName.insets = insets3;
        gbc_timeName.gridx = 1;
        gbc_timeName.gridy = row;
        selectionPanel.add(timeName, gbc_timeName);

        JButton timeButton = new JButton("", arrowImage);
        timeButton.setMargin(insets0);
        timeButton.setBorderPainted(false);
        timeButton.setContentAreaFilled(false);
        GridBagConstraints gbc_timeButton = new GridBagConstraints();
        gbc_timeButton.anchor = GridBagConstraints.CENTER;
        gbc_timeButton.insets = insets1;
        gbc_timeButton.gridx = 2;
        gbc_timeButton.gridy = row;
        timeButton.setActionCommand("time");
        timeButton.addActionListener(this);
        selectionPanel.add(timeButton, gbc_timeButton);
    }
    
    protected void addIATColSelection() {
        row += 1;
        // IAT
        JLabel iatLabel = new JLabel(iatLabelText);
        GridBagConstraints gbc_iatLabel = new GridBagConstraints();
        gbc_iatLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_iatLabel.insets = insets3;
        gbc_iatLabel.gridx = 0;
        gbc_iatLabel.gridy = row;
        selectionPanel.add(iatLabel, gbc_iatLabel);
        
        iatName = new JTextField(isEmpty(Config.getIatColumnName()));
        iatName.setMinimumSize(minTextDimension);
        iatName.setEditable(false);
        iatName.setBackground(Color.WHITE);
        GridBagConstraints gbc_iatName = new GridBagConstraints();
        gbc_iatName.anchor = GridBagConstraints.NORTHWEST;
        gbc_iatName.insets = insets3;
        gbc_iatName.gridx = 1;
        gbc_iatName.gridy = row;
        selectionPanel.add(iatName, gbc_iatName);

        JButton iatButton = new JButton("", arrowImage);
        iatButton.setMargin(insets0);
        iatButton.setBorderPainted(false);
        iatButton.setContentAreaFilled(false);
        GridBagConstraints gbc_iatButton = new GridBagConstraints();
        gbc_iatButton.anchor = GridBagConstraints.CENTER;
        gbc_iatButton.insets = insets1;
        gbc_iatButton.gridx = 2;
        gbc_iatButton.gridy = row;
        iatButton.setActionCommand("iat");
        iatButton.addActionListener(this);
        selectionPanel.add(iatButton, gbc_iatButton);
    }
    
    protected void addFFBColSelection() {
        row += 1;
        // FFB
        JLabel ffbLabel = new JLabel(ffbLabelText);
        GridBagConstraints gbc_ffbLabel = new GridBagConstraints();
        gbc_ffbLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_ffbLabel.insets = insets3;
        gbc_ffbLabel.gridx = 0;
        gbc_ffbLabel.gridy = row;
        selectionPanel.add(ffbLabel, gbc_ffbLabel);
        
        ffbName = new JTextField(isEmpty(Config.getFinalFuelingBaseColumnName()));
        ffbName.setMinimumSize(minTextDimension);
        ffbName.setEditable(false);
        ffbName.setBackground(Color.WHITE);
        GridBagConstraints gbc_ffbName = new GridBagConstraints();
        gbc_ffbName.anchor = GridBagConstraints.NORTHWEST;
        gbc_ffbName.insets = insets3;
        gbc_ffbName.gridx = 1;
        gbc_ffbName.gridy = row;
        selectionPanel.add(ffbName, gbc_ffbName);

        JButton ffbButton = new JButton("", arrowImage);
        ffbButton.setMargin(insets0);
        ffbButton.setBorderPainted(false);
        ffbButton.setContentAreaFilled(false);
        GridBagConstraints gbc_ffbButton = new GridBagConstraints();
        gbc_ffbButton.anchor = GridBagConstraints.CENTER;
        gbc_ffbButton.insets = insets1;
        gbc_ffbButton.gridx = 2;
        gbc_ffbButton.gridy = row;
        ffbButton.setActionCommand("ffb");
        ffbButton.addActionListener(this);
        selectionPanel.add(ffbButton, gbc_ffbButton);
    }
    
    protected void addVEFlowColSelection() {
        row += 1;
        // VE Flow
        JLabel veFlowLabel = new JLabel(veFlowLabelText);
        GridBagConstraints gbc_veFlowLabel = new GridBagConstraints();
        gbc_veFlowLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_veFlowLabel.insets = insets3;
        gbc_veFlowLabel.gridx = 0;
        gbc_veFlowLabel.gridy = row;
        selectionPanel.add(veFlowLabel, gbc_veFlowLabel);
        
        veFlowName = new JTextField(isEmpty(Config.getVEFlowColumnName()));
        veFlowName.setMinimumSize(minTextDimension);
        veFlowName.setEditable(false);
        veFlowName.setBackground(Color.WHITE);
        GridBagConstraints gbc_veFlowName = new GridBagConstraints();
        gbc_veFlowName.anchor = GridBagConstraints.NORTHWEST;
        gbc_veFlowName.insets = insets3;
        gbc_veFlowName.gridx = 1;
        gbc_veFlowName.gridy = row;
        selectionPanel.add(veFlowName, gbc_veFlowName);

        JButton veFlowButton = new JButton("", arrowImage);
        veFlowButton.setMargin(insets0);
        veFlowButton.setBorderPainted(false);
        veFlowButton.setContentAreaFilled(false);
        GridBagConstraints gbc_veFlowButton = new GridBagConstraints();
        gbc_veFlowButton.anchor = GridBagConstraints.CENTER;
        gbc_veFlowButton.insets = insets1;
        gbc_veFlowButton.gridx = 2;
        gbc_veFlowButton.gridy = row;
        veFlowButton.setActionCommand("veflow");
        veFlowButton.addActionListener(this);
        selectionPanel.add(veFlowButton, gbc_veFlowButton);
    }
    
    protected void addMAFVoltageMaximumFilter() {
        row += 1;
        // MAF Voltage Maximum Note
        JLabel maxMafVNoteLabel = new JLabel("Set this filter to process just Closed Loop part of MAF curve data");
        maxMafVNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_maxMafVNoteLabel = new GridBagConstraints();
        gbc_maxMafVNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_maxMafVNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_maxMafVNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_maxMafVNoteLabel.gridx = 0;
        gbc_maxMafVNoteLabel.gridy = row;
        gbc_maxMafVNoteLabel.gridwidth = 4;
        selectionPanel.add(maxMafVNoteLabel, gbc_maxMafVNoteLabel);
        
        row += 1;
        JLabel maxMafVLabel = new JLabel(maxMafVLabelText);
        GridBagConstraints gbc_maxMafVLabel = new GridBagConstraints();
        gbc_maxMafVLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_maxMafVLabel.insets = insets3;
        gbc_maxMafVLabel.gridx = 0;
        gbc_maxMafVLabel.gridy = row;
        selectionPanel.add(maxMafVLabel, gbc_maxMafVLabel);
        
        maxMafVFilter = new JFormattedTextField(doubleFmt);
        maxMafVFilter.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_maxMafVFilter = new GridBagConstraints();
        gbc_maxMafVFilter.anchor = GridBagConstraints.NORTHWEST;
        gbc_maxMafVFilter.insets = insets3;
        gbc_maxMafVFilter.gridx = 1;
        gbc_maxMafVFilter.gridy = row;
        selectionPanel.add(maxMafVFilter, gbc_maxMafVFilter);
        
        JButton maxMafVDefaultButton = new JButton("default");
        GridBagConstraints gbc_maxMafVDefaultButton = new GridBagConstraints();
        gbc_maxMafVDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_maxMafVDefaultButton.insets = insets0;
        gbc_maxMafVDefaultButton.gridx = 2;
        gbc_maxMafVDefaultButton.gridy = row;
        gbc_maxMafVDefaultButton.gridwidth = 2;
        maxMafVDefaultButton.setActionCommand("maxmafv");
        maxMafVDefaultButton.setMinimumSize(minDefaultButtonDimension);
        maxMafVDefaultButton.setMargin(insets2);
        maxMafVDefaultButton.setBorder(border);
        maxMafVDefaultButton.addActionListener(this);
        selectionPanel.add(maxMafVDefaultButton, gbc_maxMafVDefaultButton);
    }
    
    protected void addMAFVoltageMinimumFilter() {
        row += 1;
        // MAF Voltage Minimum Note
        JLabel minMafVNoteLabel = new JLabel("Set this filter to process just Open Loop part of MAF curve data");
        minMafVNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_minMafVNoteLabel = new GridBagConstraints();
        gbc_minMafVNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_minMafVNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_minMafVNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_minMafVNoteLabel.gridx = 0;
        gbc_minMafVNoteLabel.gridy = row;
        gbc_minMafVNoteLabel.gridwidth = 4;
        selectionPanel.add(minMafVNoteLabel, gbc_minMafVNoteLabel);
        
        row += 1;
        JLabel minMafVLabel = new JLabel(minMafVLabelText);
        GridBagConstraints gbc_minMafVLabel = new GridBagConstraints();
        gbc_minMafVLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_minMafVLabel.insets = insets3;
        gbc_minMafVLabel.gridx = 0;
        gbc_minMafVLabel.gridy = row;
        selectionPanel.add(minMafVLabel, gbc_minMafVLabel);
        
        minMafVFilter = new JFormattedTextField(doubleFmt);
        minMafVFilter.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_minMafVFilter = new GridBagConstraints();
        gbc_minMafVFilter.anchor = GridBagConstraints.NORTHWEST;
        gbc_minMafVFilter.insets = insets3;
        gbc_minMafVFilter.gridx = 1;
        gbc_minMafVFilter.gridy = row;
        selectionPanel.add(minMafVFilter, gbc_minMafVFilter);
        
        JButton minMafVDefaultButton = new JButton("default");
        GridBagConstraints gbc_minMafVDefaultButton = new GridBagConstraints();
        gbc_minMafVDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_minMafVDefaultButton.insets = insets0;
        gbc_minMafVDefaultButton.gridx = 2;
        gbc_minMafVDefaultButton.gridy = row;
        gbc_minMafVDefaultButton.gridwidth = 2;
        minMafVDefaultButton.setActionCommand("minmafv");
        minMafVDefaultButton.setMinimumSize(minDefaultButtonDimension);
        minMafVDefaultButton.setMargin(insets2);
        minMafVDefaultButton.setBorder(border);
        minMafVDefaultButton.addActionListener(this);
        selectionPanel.add(minMafVDefaultButton, gbc_minMafVDefaultButton);
    }
    
    protected void addRPMMaximumFilter() {        
        row += 1;
        // RPM Maximum Note
        JLabel maxRPMNoteLabel = new JLabel("Remove data where RPM is above the filter value.");
        maxRPMNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_maxRPMNoteLabel = new GridBagConstraints();
        gbc_maxRPMNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_maxRPMNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_maxRPMNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_maxRPMNoteLabel.gridx = 0;
        gbc_maxRPMNoteLabel.gridy = row;
        gbc_maxRPMNoteLabel.gridwidth = 4;
        selectionPanel.add(maxRPMNoteLabel, gbc_maxRPMNoteLabel);
        
        row += 1;
        JLabel maxRPMLabel = new JLabel(maxRPMLabelText);
        GridBagConstraints gbc_maxRPMLabel = new GridBagConstraints();
        gbc_maxRPMLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_maxRPMLabel.insets = insets3;
        gbc_maxRPMLabel.gridx = 0;
        gbc_maxRPMLabel.gridy = row;
        selectionPanel.add(maxRPMLabel, gbc_maxRPMLabel);
        
        maxRPMFilter = new JFormattedTextField(intFmt);
        maxRPMFilter.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_maxRPMFilter = new GridBagConstraints();
        gbc_maxRPMFilter.anchor = GridBagConstraints.NORTHWEST;
        gbc_maxRPMFilter.insets = insets3;
        gbc_maxRPMFilter.gridx = 1;
        gbc_maxRPMFilter.gridy = row;
        selectionPanel.add(maxRPMFilter, gbc_maxRPMFilter);
        
        JButton maxRPMDefaultButton = new JButton("default");
        GridBagConstraints gbc_maxRPMDefaultButton = new GridBagConstraints();
        gbc_maxRPMDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_maxRPMDefaultButton.insets = insets0;
        gbc_maxRPMDefaultButton.gridx = 2;
        gbc_maxRPMDefaultButton.gridy = row;
        gbc_maxRPMDefaultButton.gridwidth = 2;
        maxRPMDefaultButton.setActionCommand("maxrpm");
        maxRPMDefaultButton.setMinimumSize(minDefaultButtonDimension);
        maxRPMDefaultButton.setMargin(insets2);
        maxRPMDefaultButton.setBorder(border);
        maxRPMDefaultButton.addActionListener(this);
        selectionPanel.add(maxRPMDefaultButton, gbc_maxRPMDefaultButton);
    }
    
    protected void addRPMMinimumFilter() {        
        row += 1;
        // RPM Minimum Note
        JLabel minRPMNoteLabel = new JLabel("Remove data where RPM is below the filter value");
        minRPMNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_minRPMNoteLabel = new GridBagConstraints();
        gbc_minRPMNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_minRPMNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_minRPMNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_minRPMNoteLabel.gridx = 0;
        gbc_minRPMNoteLabel.gridy = row;
        gbc_minRPMNoteLabel.gridwidth = 4;
        selectionPanel.add(minRPMNoteLabel, gbc_minRPMNoteLabel);
        
        row += 1;
        JLabel minRPMLabel = new JLabel(minRPMLabelText);
        GridBagConstraints gbc_minRPMLabel = new GridBagConstraints();
        gbc_minRPMLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_minRPMLabel.insets = insets3;
        gbc_minRPMLabel.gridx = 0;
        gbc_minRPMLabel.gridy = row;
        selectionPanel.add(minRPMLabel, gbc_minRPMLabel);
        
        minRPMFilter = new JFormattedTextField(intFmt);
        minRPMFilter.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_minRPMFilter = new GridBagConstraints();
        gbc_minRPMFilter.anchor = GridBagConstraints.NORTHWEST;
        gbc_minRPMFilter.insets = insets3;
        gbc_minRPMFilter.gridx = 1;
        gbc_minRPMFilter.gridy = row;
        selectionPanel.add(minRPMFilter, gbc_minRPMFilter);
        
        JButton minRPMDefaultButton = new JButton("default");
        GridBagConstraints gbc_minRPMDefaultButton = new GridBagConstraints();
        gbc_minRPMDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_minRPMDefaultButton.insets = insets0;
        gbc_minRPMDefaultButton.gridx = 2;
        gbc_minRPMDefaultButton.gridy = row;
        gbc_minRPMDefaultButton.gridwidth = 2;
        minRPMDefaultButton.setActionCommand("minrpm");
        minRPMDefaultButton.setMinimumSize(minDefaultButtonDimension);
        minRPMDefaultButton.setMargin(insets2);
        minRPMDefaultButton.setBorder(border);
        minRPMDefaultButton.addActionListener(this);
        selectionPanel.add(minRPMDefaultButton, gbc_minRPMDefaultButton);
    }

    protected void addFFBMaximumFilter() {        
        row += 1;
        // FFB Maximum Note
        JLabel maxFFBNoteLabel = new JLabel("Remove data where FFB is above the filter value.");
        maxFFBNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_maxFFBNoteLabel = new GridBagConstraints();
        gbc_maxFFBNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_maxFFBNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_maxFFBNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_maxFFBNoteLabel.gridx = 0;
        gbc_maxFFBNoteLabel.gridy = row;
        gbc_maxFFBNoteLabel.gridwidth = 4;
        selectionPanel.add(maxFFBNoteLabel, gbc_maxFFBNoteLabel);
        
        row += 1;
        JLabel maxFFBLabel = new JLabel(maxFFBLabelText);
        GridBagConstraints gbc_maxFFBLabel = new GridBagConstraints();
        gbc_maxFFBLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_maxFFBLabel.insets = insets3;
        gbc_maxFFBLabel.gridx = 0;
        gbc_maxFFBLabel.gridy = row;
        selectionPanel.add(maxFFBLabel, gbc_maxFFBLabel);
        
        maxFFBFilter = new JFormattedTextField(intFmt);
        maxFFBFilter.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_maxFFBFilter = new GridBagConstraints();
        gbc_maxFFBFilter.anchor = GridBagConstraints.NORTHWEST;
        gbc_maxFFBFilter.insets = insets3;
        gbc_maxFFBFilter.gridx = 1;
        gbc_maxFFBFilter.gridy = row;
        selectionPanel.add(maxFFBFilter, gbc_maxFFBFilter);
        
        JButton maxFFBDefaultButton = new JButton("default");
        GridBagConstraints gbc_maxFFBDefaultButton = new GridBagConstraints();
        gbc_maxFFBDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_maxFFBDefaultButton.insets = insets0;
        gbc_maxFFBDefaultButton.gridx = 2;
        gbc_maxFFBDefaultButton.gridy = row;
        gbc_maxFFBDefaultButton.gridwidth = 2;
        maxFFBDefaultButton.setActionCommand("maxffb");
        maxFFBDefaultButton.setMinimumSize(minDefaultButtonDimension);
        maxFFBDefaultButton.setMargin(insets2);
        maxFFBDefaultButton.setBorder(border);
        maxFFBDefaultButton.addActionListener(this);
        selectionPanel.add(maxFFBDefaultButton, gbc_maxFFBDefaultButton);
    }
    
    protected void addFFBMinimumFilter() {        
        row += 1;
        // FFB Minimum Note
        JLabel minFFBNoteLabel = new JLabel("Remove data where FFB is below the filter value");
        minFFBNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_minFFBNoteLabel = new GridBagConstraints();
        gbc_minFFBNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_minFFBNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_minFFBNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_minFFBNoteLabel.gridx = 0;
        gbc_minFFBNoteLabel.gridy = row;
        gbc_minFFBNoteLabel.gridwidth = 4;
        selectionPanel.add(minFFBNoteLabel, gbc_minFFBNoteLabel);
        
        row += 1;
        JLabel minFFBLabel = new JLabel(minFFBLabelText);
        GridBagConstraints gbc_minFFBLabel = new GridBagConstraints();
        gbc_minFFBLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_minFFBLabel.insets = insets3;
        gbc_minFFBLabel.gridx = 0;
        gbc_minFFBLabel.gridy = row;
        selectionPanel.add(minFFBLabel, gbc_minFFBLabel);
        
        minFFBFilter = new JFormattedTextField(intFmt);
        minFFBFilter.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_minFFBFilter = new GridBagConstraints();
        gbc_minFFBFilter.anchor = GridBagConstraints.NORTHWEST;
        gbc_minFFBFilter.insets = insets3;
        gbc_minFFBFilter.gridx = 1;
        gbc_minFFBFilter.gridy = row;
        selectionPanel.add(minFFBFilter, gbc_minFFBFilter);
        
        JButton minFFBDefaultButton = new JButton("default");
        GridBagConstraints gbc_minFFBDefaultButton = new GridBagConstraints();
        gbc_minFFBDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_minFFBDefaultButton.insets = insets0;
        gbc_minFFBDefaultButton.gridx = 2;
        gbc_minFFBDefaultButton.gridy = row;
        gbc_minFFBDefaultButton.gridwidth = 2;
        minFFBDefaultButton.setActionCommand("minffb");
        minFFBDefaultButton.setMinimumSize(minDefaultButtonDimension);
        minFFBDefaultButton.setMargin(insets2);
        minFFBDefaultButton.setBorder(border);
        minFFBDefaultButton.addActionListener(this);
        selectionPanel.add(minFFBDefaultButton, gbc_minFFBDefaultButton);
    }

    protected void addAFRMaximumFilter() {        
        row += 1;
        // AFR Maximum Note
        JLabel maxAfrNoteLabel = new JLabel("Remove data where AFR is above the specified maximum");
        maxAfrNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_maxAfrNoteLabel = new GridBagConstraints();
        gbc_maxAfrNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_maxAfrNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_maxAfrNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_maxAfrNoteLabel.gridx = 0;
        gbc_maxAfrNoteLabel.gridy = row;
        gbc_maxAfrNoteLabel.gridwidth = 4;
        selectionPanel.add(maxAfrNoteLabel, gbc_maxAfrNoteLabel);
        
        row += 1;
        JLabel maxAfrLabel = new JLabel(maxAfrLabelText);
        GridBagConstraints gbc_maxAfrLabel = new GridBagConstraints();
        gbc_maxAfrLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_maxAfrLabel.insets = insets3;
        gbc_maxAfrLabel.gridx = 0;
        gbc_maxAfrLabel.gridy = row;
        selectionPanel.add(maxAfrLabel, gbc_maxAfrLabel);
        
        maxAfrFilter = new JFormattedTextField(doubleFmt);
        maxAfrFilter.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_maxAfrFilter = new GridBagConstraints();
        gbc_maxAfrFilter.anchor = GridBagConstraints.NORTHWEST;
        gbc_maxAfrFilter.insets = insets3;
        gbc_maxAfrFilter.gridx = 1;
        gbc_maxAfrFilter.gridy = row;
        selectionPanel.add(maxAfrFilter, gbc_maxAfrFilter);
        
        JButton maxAfrDefaultButton = new JButton("default");
        GridBagConstraints gbc_maxAfrDefaultButton = new GridBagConstraints();
        gbc_maxAfrDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_maxAfrDefaultButton.insets = insets0;
        gbc_maxAfrDefaultButton.gridx = 2;
        gbc_maxAfrDefaultButton.gridy = row;
        gbc_maxAfrDefaultButton.gridwidth = 2;
        maxAfrDefaultButton.setActionCommand("maxafr");
        maxAfrDefaultButton.setMinimumSize(minDefaultButtonDimension);
        maxAfrDefaultButton.setMargin(insets2);
        maxAfrDefaultButton.setBorder(border);
        maxAfrDefaultButton.addActionListener(this);
        selectionPanel.add(maxAfrDefaultButton, gbc_maxAfrDefaultButton);
    }
    
    protected void addAFRMinimumFilter() {        
        row += 1;
        // AFR Minimum Note
        JLabel minAfrNoteLabel = new JLabel("Remove data where AFR is below the specified minimum");
        minAfrNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_minAfrNoteLabel = new GridBagConstraints();
        gbc_minAfrNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_minAfrNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_minAfrNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_minAfrNoteLabel.gridx = 0;
        gbc_minAfrNoteLabel.gridy = row;
        gbc_minAfrNoteLabel.gridwidth = 4;
        selectionPanel.add(minAfrNoteLabel, gbc_minAfrNoteLabel);
        
        row += 1;
        JLabel minAfrLabel = new JLabel(minAfrLabelText);
        GridBagConstraints gbc_minAfrLabel = new GridBagConstraints();
        gbc_minAfrLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_minAfrLabel.insets = insets3;
        gbc_minAfrLabel.gridx = 0;
        gbc_minAfrLabel.gridy = row;
        selectionPanel.add(minAfrLabel, gbc_minAfrLabel);
        
        minAfrFilter = new JFormattedTextField(doubleFmt);
        minAfrFilter.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_minAfrFilter = new GridBagConstraints();
        gbc_minAfrFilter.anchor = GridBagConstraints.NORTHWEST;
        gbc_minAfrFilter.insets = insets3;
        gbc_minAfrFilter.gridx = 1;
        gbc_minAfrFilter.gridy = row;
        selectionPanel.add(minAfrFilter, gbc_minAfrFilter);
        
        JButton minAfrDefaultButton = new JButton("default");
        GridBagConstraints gbc_minAfrDefaultButton = new GridBagConstraints();
        gbc_minAfrDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_minAfrDefaultButton.insets = insets0;
        gbc_minAfrDefaultButton.gridx = 2;
        gbc_minAfrDefaultButton.gridy = row;
        gbc_minAfrDefaultButton.gridwidth = 2;
        minAfrDefaultButton.setActionCommand("minafr");
        minAfrDefaultButton.setMinimumSize(minDefaultButtonDimension);
        minAfrDefaultButton.setMargin(insets2);
        minAfrDefaultButton.setBorder(border);
        minAfrDefaultButton.addActionListener(this);
        selectionPanel.add(minAfrDefaultButton, gbc_minAfrDefaultButton);
    }
    
    protected void addIATMaximumFilter() {
        row += 1;
        // Intake Air Temperature Maximum Note
        JLabel maxIatNoteLabel = new JLabel("Set this filter to filter out data with high Intake Air Temperature");
        maxIatNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_maxIatNoteLabel = new GridBagConstraints();
        gbc_maxIatNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_maxIatNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_maxIatNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_maxIatNoteLabel.gridx = 0;
        gbc_maxIatNoteLabel.gridy = row;
        gbc_maxIatNoteLabel.gridwidth = 4;
        selectionPanel.add(maxIatNoteLabel, gbc_maxIatNoteLabel);
        
        row += 1;
        JLabel maxIatLabel = new JLabel(maxIatLabelText);
        GridBagConstraints gbc_maxIatLabel = new GridBagConstraints();
        gbc_maxIatLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_maxIatLabel.insets = insets3;
        gbc_maxIatLabel.gridx = 0;
        gbc_maxIatLabel.gridy = row;
        selectionPanel.add(maxIatLabel, gbc_maxIatLabel);
        
        maxIatFilter = new JFormattedTextField(doubleFmt);
        maxIatFilter.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_maxIatFilter = new GridBagConstraints();
        gbc_maxIatFilter.anchor = GridBagConstraints.NORTHWEST;
        gbc_maxIatFilter.insets = insets3;
        gbc_maxIatFilter.gridx = 1;
        gbc_maxIatFilter.gridy = row;
        selectionPanel.add(maxIatFilter, gbc_maxIatFilter);
        
        JButton maxIatDefaultButton = new JButton("default");
        GridBagConstraints gbc_maxIatDefaultButton = new GridBagConstraints();
        gbc_maxIatDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_maxIatDefaultButton.insets = insets0;
        gbc_maxIatDefaultButton.gridx = 2;
        gbc_maxIatDefaultButton.gridy = row;
        gbc_maxIatDefaultButton.gridwidth = 2;
        maxIatDefaultButton.setActionCommand("maxiat");
        maxIatDefaultButton.setMinimumSize(minDefaultButtonDimension);
        maxIatDefaultButton.setMargin(insets2);
        maxIatDefaultButton.setBorder(border);
        maxIatDefaultButton.addActionListener(this);
        selectionPanel.add(maxIatDefaultButton, gbc_maxIatDefaultButton);
    }
    
    protected void addDvDtMaximumFilter() {        
        row += 1;
        // dVdt Maximum Note
        JLabel maxDvdtNoteLabel = new JLabel("Remove data where dV/dt is above the specified maximum");
        maxDvdtNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_maxDvdtNoteLabel = new GridBagConstraints();
        gbc_maxDvdtNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_maxDvdtNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_maxDvdtNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_maxDvdtNoteLabel.gridx = 0;
        gbc_maxDvdtNoteLabel.gridy = row;
        gbc_maxDvdtNoteLabel.gridwidth = 4;
        selectionPanel.add(maxDvdtNoteLabel, gbc_maxDvdtNoteLabel);
        
        row += 1;
        JLabel maxDvdtLabel = new JLabel(maxDvdtLabelText);
        GridBagConstraints gbc_maxDvdtLabel = new GridBagConstraints();
        gbc_maxDvdtLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_maxDvdtLabel.insets = insets3;
        gbc_maxDvdtLabel.gridx = 0;
        gbc_maxDvdtLabel.gridy = row;
        selectionPanel.add(maxDvdtLabel, gbc_maxDvdtLabel);
        
        maxDvdtFilter = new JFormattedTextField(doubleFmt);
        maxDvdtFilter.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_maxDvdtFilter = new GridBagConstraints();
        gbc_maxDvdtFilter.anchor = GridBagConstraints.NORTHWEST;
        gbc_maxDvdtFilter.insets = insets3;
        gbc_maxDvdtFilter.gridx = 1;
        gbc_maxDvdtFilter.gridy = row;
        selectionPanel.add(maxDvdtFilter, gbc_maxDvdtFilter);
        
        JButton maxDvdtDefaultButton = new JButton("default");
        GridBagConstraints gbc_maxDvdtDefaultButton = new GridBagConstraints();
        gbc_maxDvdtDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_maxDvdtDefaultButton.insets = insets0;
        gbc_maxDvdtDefaultButton.gridx = 2;
        gbc_maxDvdtDefaultButton.gridy = row;
        gbc_maxDvdtDefaultButton.gridwidth = 2;
        maxDvdtDefaultButton.setActionCommand("maxdvdt");
        maxDvdtDefaultButton.setMinimumSize(minDefaultButtonDimension);
        maxDvdtDefaultButton.setMargin(insets2);
        maxDvdtDefaultButton.setBorder(border);
        maxDvdtDefaultButton.addActionListener(this);
        selectionPanel.add(maxDvdtDefaultButton, gbc_maxDvdtDefaultButton);
    }
    
    protected void addThrottleChangeMaximumFilter() {
        row += 1;
        // Throttle Change % Maximum Note
        JLabel thrtlChangeMaxNoteLabel = new JLabel("Set this filter to filter out throttle tip-in errors");
        thrtlChangeMaxNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_thrtlChangeMaxNoteLabel = new GridBagConstraints();
        gbc_thrtlChangeMaxNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_thrtlChangeMaxNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_thrtlChangeMaxNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_thrtlChangeMaxNoteLabel.gridx = 0;
        gbc_thrtlChangeMaxNoteLabel.gridy = row;
        gbc_thrtlChangeMaxNoteLabel.gridwidth = 4;
        selectionPanel.add(thrtlChangeMaxNoteLabel, gbc_thrtlChangeMaxNoteLabel);
        
        row += 1;
        JLabel thrtlChangeMaxLabel = new JLabel(thrtlChangeMaxLabelText);
        GridBagConstraints gbc_thrtlChangeMaxLabel = new GridBagConstraints();
        gbc_thrtlChangeMaxLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_thrtlChangeMaxLabel.insets = insets3;
        gbc_thrtlChangeMaxLabel.gridx = 0;
        gbc_thrtlChangeMaxLabel.gridy = row;
        selectionPanel.add(thrtlChangeMaxLabel, gbc_thrtlChangeMaxLabel);
        
        thrtlChangeMaxFilter = new JSpinner(new SpinnerNumberModel(Config.getThrottleChangeMaxValue(), -1, 30, 1));
        thrtlChangeMaxFilter.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_thrtlChangeMaxFilter = new GridBagConstraints();
        gbc_thrtlChangeMaxFilter.anchor = GridBagConstraints.NORTHWEST;
        gbc_thrtlChangeMaxFilter.insets = insets3;
        gbc_thrtlChangeMaxFilter.gridx = 1;
        gbc_thrtlChangeMaxFilter.gridy = row;
        selectionPanel.add(thrtlChangeMaxFilter, gbc_thrtlChangeMaxFilter);
        
        JButton thrtlChangeMaxDefaultButton = new JButton("default");
        GridBagConstraints gbc_thrtlChangeMaxDefaultButton = new GridBagConstraints();
        gbc_thrtlChangeMaxDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_thrtlChangeMaxDefaultButton.insets = insets0;
        gbc_thrtlChangeMaxDefaultButton.gridx = 2;
        gbc_thrtlChangeMaxDefaultButton.gridy = row;
        gbc_thrtlChangeMaxDefaultButton.gridwidth = 2;
        thrtlChangeMaxDefaultButton.setActionCommand("thrtlchange");
        thrtlChangeMaxDefaultButton.setMinimumSize(minDefaultButtonDimension);
        thrtlChangeMaxDefaultButton.setMargin(insets2);
        thrtlChangeMaxDefaultButton.setBorder(border);
        thrtlChangeMaxDefaultButton.addActionListener(this);
        selectionPanel.add(thrtlChangeMaxDefaultButton, gbc_thrtlChangeMaxDefaultButton);
    }
    
    protected void addThrottleMinimumFilter() {        
        row += 1;
        // Throttle Minimum Note
        JLabel minThrtlNoteLabel = new JLabel("Remove data where Throttle Input is below the specified minimum");
        minThrtlNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_minThrtlNoteLabel = new GridBagConstraints();
        gbc_minThrtlNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_minThrtlNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_minThrtlNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_minThrtlNoteLabel.gridx = 0;
        gbc_minThrtlNoteLabel.gridy = row;
        gbc_minThrtlNoteLabel.gridwidth = 4;
        selectionPanel.add(minThrtlNoteLabel, gbc_minThrtlNoteLabel);
        
        row += 1;
        JLabel minThrtlLabel = new JLabel(minThrottleLabelText);
        GridBagConstraints gbc_minThrtlLabel = new GridBagConstraints();
        gbc_minThrtlLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_minThrtlLabel.insets = insets3;
        gbc_minThrtlLabel.gridx = 0;
        gbc_minThrtlLabel.gridy = row;
        selectionPanel.add(minThrtlLabel, gbc_minThrtlLabel);
        
        thrtlMinimumFilter = new JFormattedTextField(doubleFmt);
        thrtlMinimumFilter.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_thrtlMinimumFilter = new GridBagConstraints();
        gbc_thrtlMinimumFilter.anchor = GridBagConstraints.NORTHWEST;
        gbc_thrtlMinimumFilter.insets = insets3;
        gbc_thrtlMinimumFilter.gridx = 1;
        gbc_thrtlMinimumFilter.gridy = row;
        selectionPanel.add(thrtlMinimumFilter, gbc_thrtlMinimumFilter);
        
        JButton minThrtlDefaultButton = new JButton("default");
        GridBagConstraints gbc_minThrtlDefaultButton = new GridBagConstraints();
        gbc_minThrtlDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_minThrtlDefaultButton.insets = insets0;
        gbc_minThrtlDefaultButton.gridx = 2;
        gbc_minThrtlDefaultButton.gridy = row;
        gbc_minThrtlDefaultButton.gridwidth = 2;
        minThrtlDefaultButton.setActionCommand("minthrtl");
        minThrtlDefaultButton.setMinimumSize(minDefaultButtonDimension);
        minThrtlDefaultButton.setMargin(insets2);
        minThrtlDefaultButton.setBorder(border);
        minThrtlDefaultButton.addActionListener(this);
        selectionPanel.add(minThrtlDefaultButton, gbc_minThrtlDefaultButton);
    }
    
    protected void addEngineLoadMinimumFilter() {
        row += 1;
        // Engine Load Minimum Note
        JLabel minEngineLoadNoteLabel = new JLabel("Remove data where Engine Load is below the filter value");
        minEngineLoadNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_minEngineLoadNoteLabel = new GridBagConstraints();
        gbc_minEngineLoadNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_minEngineLoadNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_minEngineLoadNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_minEngineLoadNoteLabel.gridx = 0;
        gbc_minEngineLoadNoteLabel.gridy = row;
        gbc_minEngineLoadNoteLabel.gridwidth = 4;
        selectionPanel.add(minEngineLoadNoteLabel, gbc_minEngineLoadNoteLabel);
        
        row += 1;
        JLabel minEngineLoadLabel = new JLabel(minEngineLoadLabelText);
        GridBagConstraints gbc_minEngineLoadLabel = new GridBagConstraints();
        gbc_minEngineLoadLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_minEngineLoadLabel.insets = insets3;
        gbc_minEngineLoadLabel.gridx = 0;
        gbc_minEngineLoadLabel.gridy = row;
        selectionPanel.add(minEngineLoadLabel, gbc_minEngineLoadLabel);
        
        minEngineLoadFilter = new JFormattedTextField(doubleFmt);
        minEngineLoadFilter.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_minEngineLoadFilter = new GridBagConstraints();
        gbc_minEngineLoadFilter.anchor = GridBagConstraints.NORTHWEST;
        gbc_minEngineLoadFilter.insets = insets3;
        gbc_minEngineLoadFilter.gridx = 1;
        gbc_minEngineLoadFilter.gridy = row;
        selectionPanel.add(minEngineLoadFilter, gbc_minEngineLoadFilter);
        
        JButton minEngineLoadDefaultButton = new JButton("default");
        GridBagConstraints gbc_minEngineLoadDefaultButton = new GridBagConstraints();
        gbc_minEngineLoadDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_minEngineLoadDefaultButton.insets = insets0;
        gbc_minEngineLoadDefaultButton.gridx = 2;
        gbc_minEngineLoadDefaultButton.gridy = row;
        gbc_minEngineLoadDefaultButton.gridwidth = 2;
        minEngineLoadDefaultButton.setActionCommand("minengload");
        minEngineLoadDefaultButton.setMinimumSize(minDefaultButtonDimension);
        minEngineLoadDefaultButton.setMargin(insets2);
        minEngineLoadDefaultButton.setBorder(border);
        minEngineLoadDefaultButton.addActionListener(this);
        selectionPanel.add(minEngineLoadDefaultButton, gbc_minEngineLoadDefaultButton);
    }
    
    protected void addManifoldPressureMinimumFilter() {
        row += 1;
        // Manifold Pressure Minimum Note
        JLabel minMPNoteLabel = new JLabel("Remove data where Manifold Pressure is below the filter value");
        minMPNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_minMPNoteLabel = new GridBagConstraints();
        gbc_minMPNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_minMPNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_minMPNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_minMPNoteLabel.gridx = 0;
        gbc_minMPNoteLabel.gridy = row;
        gbc_minMPNoteLabel.gridwidth = 4;
        selectionPanel.add(minMPNoteLabel, gbc_minMPNoteLabel);
        
        row += 1;
        JLabel minMPLabel = new JLabel(minManifoldPressureLabelText);
        GridBagConstraints gbc_minMPLabel = new GridBagConstraints();
        gbc_minMPLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_minMPLabel.insets = insets3;
        gbc_minMPLabel.gridx = 0;
        gbc_minMPLabel.gridy = row;
        selectionPanel.add(minMPLabel, gbc_minMPLabel);
        
        minMPFilter = new JFormattedTextField(doubleFmt);
        minMPFilter.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_minMPFilter = new GridBagConstraints();
        gbc_minMPFilter.anchor = GridBagConstraints.NORTHWEST;
        gbc_minMPFilter.insets = insets3;
        gbc_minMPFilter.gridx = 1;
        gbc_minMPFilter.gridy = row;
        selectionPanel.add(minMPFilter, gbc_minMPFilter);
        
        JButton minMPDefaultButton = new JButton("default");
        GridBagConstraints gbc_minMPDefaultButton = new GridBagConstraints();
        gbc_minMPDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_minMPDefaultButton.insets = insets0;
        gbc_minMPDefaultButton.gridx = 2;
        gbc_minMPDefaultButton.gridy = row;
        gbc_minMPDefaultButton.gridwidth = 2;
        minMPDefaultButton.setActionCommand("minmp");
        minMPDefaultButton.setMinimumSize(minDefaultButtonDimension);
        minMPDefaultButton.setMargin(insets2);
        minMPDefaultButton.setBorder(border);
        minMPDefaultButton.addActionListener(this);
        selectionPanel.add(minMPDefaultButton, gbc_minMPDefaultButton);
    }
    
    protected void addCellHitCountMinimumFilter() {        
        row += 1;
        // Cell Hit Count Minimum Note
        JLabel minCellHitCountNoteLabel = new JLabel("Minimum cell hit count required to take data set for that cell into consideration");
        minCellHitCountNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_minCellHitCountNoteLabel = new GridBagConstraints();
        gbc_minCellHitCountNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_minCellHitCountNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_minCellHitCountNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_minCellHitCountNoteLabel.gridx = 0;
        gbc_minCellHitCountNoteLabel.gridy = row;
        gbc_minCellHitCountNoteLabel.gridwidth = 4;
        selectionPanel.add(minCellHitCountNoteLabel, gbc_minCellHitCountNoteLabel);
        
        row += 1;
        JLabel minCellHitCountLabel = new JLabel(minCellHitCountLabelText);
        GridBagConstraints gbc_minCellHitCountLabel = new GridBagConstraints();
        gbc_minCellHitCountLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_minCellHitCountLabel.insets = insets3;
        gbc_minCellHitCountLabel.gridx = 0;
        gbc_minCellHitCountLabel.gridy = row;
        selectionPanel.add(minCellHitCountLabel, gbc_minCellHitCountLabel);
        
        minCellHitCountFilter = new JFormattedTextField(intFmt);
        minCellHitCountFilter.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_minCellHitCountFilter = new GridBagConstraints();
        gbc_minCellHitCountFilter.anchor = GridBagConstraints.NORTHWEST;
        gbc_minCellHitCountFilter.insets = insets3;
        gbc_minCellHitCountFilter.gridx = 1;
        gbc_minCellHitCountFilter.gridy = row;
        selectionPanel.add(minCellHitCountFilter, gbc_minCellHitCountFilter);
        
        JButton minCellHitCountDefaultButton = new JButton("default");
        GridBagConstraints gbc_minCellHitCountDefaultButton = new GridBagConstraints();
        gbc_minCellHitCountDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_minCellHitCountDefaultButton.insets = insets0;
        gbc_minCellHitCountDefaultButton.gridx = 2;
        gbc_minCellHitCountDefaultButton.gridy = row;
        gbc_minCellHitCountDefaultButton.gridwidth = 2;
        minCellHitCountDefaultButton.setActionCommand("minhitcnt");
        minCellHitCountDefaultButton.setMinimumSize(minDefaultButtonDimension);
        minCellHitCountDefaultButton.setMargin(insets2);
        minCellHitCountDefaultButton.setBorder(border);
        minCellHitCountDefaultButton.addActionListener(this);
        selectionPanel.add(minCellHitCountDefaultButton, gbc_minCellHitCountDefaultButton);
    }
    
    protected void addWOTStationaryPointFilter() {        
        row += 1;
        // WOT Stationary Point Note
        JLabel wotPointNoteLabel = new JLabel("CL/OL transition. Use Throttle Angle % but could use Accel Pedal Angle % instead");
        wotPointNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_wotPointNoteLabel = new GridBagConstraints();
        gbc_wotPointNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_wotPointNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_wotPointNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_wotPointNoteLabel.gridx = 0;
        gbc_wotPointNoteLabel.gridy = row;
        gbc_wotPointNoteLabel.gridwidth = 4;
        selectionPanel.add(wotPointNoteLabel, gbc_wotPointNoteLabel);
        
        row += 1;
        JLabel wotStationaryLabel = new JLabel(wotStationaryLabelText);
        GridBagConstraints gbc_wotStationaryLabel = new GridBagConstraints();
        gbc_wotStationaryLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_wotStationaryLabel.insets = insets3;
        gbc_wotStationaryLabel.gridx = 0;
        gbc_wotStationaryLabel.gridy = row;
        selectionPanel.add(wotStationaryLabel, gbc_wotStationaryLabel);
        
        wotStationaryPointFilter = new JSpinner(new SpinnerNumberModel(Config.getWOTStationaryPointValue(), 50, 100, 5));
        wotStationaryPointFilter.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_wotStationaryPointFilter = new GridBagConstraints();
        gbc_wotStationaryPointFilter.anchor = GridBagConstraints.NORTHWEST;
        gbc_wotStationaryPointFilter.insets = insets3;
        gbc_wotStationaryPointFilter.gridx = 1;
        gbc_wotStationaryPointFilter.gridy = row;
        selectionPanel.add(wotStationaryPointFilter, gbc_wotStationaryPointFilter);
        
        JButton wotStationaryPointDefaultButton = new JButton("default");
        GridBagConstraints gbc_wotStationaryPointDefaultButton = new GridBagConstraints();
        gbc_wotStationaryPointDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_wotStationaryPointDefaultButton.insets = insets0;
        gbc_wotStationaryPointDefaultButton.gridx = 2;
        gbc_wotStationaryPointDefaultButton.gridy = row;
        gbc_wotStationaryPointDefaultButton.gridwidth = 2;
        wotStationaryPointDefaultButton.setActionCommand("wotpoint");
        wotStationaryPointDefaultButton.setMinimumSize(minDefaultButtonDimension);
        wotStationaryPointDefaultButton.setMargin(insets2);
        wotStationaryPointDefaultButton.setBorder(border);
        wotStationaryPointDefaultButton.addActionListener(this);
        selectionPanel.add(wotStationaryPointDefaultButton, gbc_wotStationaryPointDefaultButton);
    }
    
    protected void addAFRErrorPctFilter() {
        row += 1;
        // AFR Error Percent Note
        JLabel afrErrorNoteLabel = new JLabel("Remove data where AFR Error % exceeds desired change %");
        afrErrorNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_afrErrorNoteLabel = new GridBagConstraints();
        gbc_afrErrorNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_afrErrorNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_afrErrorNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_afrErrorNoteLabel.gridx = 0;
        gbc_afrErrorNoteLabel.gridy = row;
        gbc_afrErrorNoteLabel.gridwidth = 4;
        selectionPanel.add(afrErrorNoteLabel, gbc_afrErrorNoteLabel);
        
        row += 1;
        JLabel afrErrorLabel = new JLabel(afrErrorLabelText);
        GridBagConstraints gbc_afrErrorLabel = new GridBagConstraints();
        gbc_afrErrorLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_afrErrorLabel.insets = insets3;
        gbc_afrErrorLabel.gridx = 0;
        gbc_afrErrorLabel.gridy = row;
        selectionPanel.add(afrErrorLabel, gbc_afrErrorLabel);
        
        afrErrorFilter = new JFormattedTextField(doubleFmt);
        afrErrorFilter.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_afrErrorFilter = new GridBagConstraints();
        gbc_afrErrorFilter.anchor = GridBagConstraints.NORTHWEST;
        gbc_afrErrorFilter.insets = insets3;
        gbc_afrErrorFilter.gridx = 1;
        gbc_afrErrorFilter.gridy = row;
        selectionPanel.add(afrErrorFilter, gbc_afrErrorFilter);
        
        JButton afrErrorDefaultButton = new JButton("default");
        GridBagConstraints gbc_afrErrorDefaultButton = new GridBagConstraints();
        gbc_afrErrorDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_afrErrorDefaultButton.insets = insets0;
        gbc_afrErrorDefaultButton.gridx = 2;
        gbc_afrErrorDefaultButton.gridy = row;
        gbc_afrErrorDefaultButton.gridwidth = 2;
        afrErrorDefaultButton.setActionCommand("afrerr");
        afrErrorDefaultButton.setMinimumSize(minDefaultButtonDimension);
        afrErrorDefaultButton.setMargin(insets2);
        afrErrorDefaultButton.setBorder(border);
        afrErrorDefaultButton.addActionListener(this);
        selectionPanel.add(afrErrorDefaultButton, gbc_afrErrorDefaultButton);
    }
    
    protected void addWOTEnrichmentMinimumFilter() {
    	row += 1;
        // Minimum WOT Enrichment Note
        JLabel wotEnrichmentNoteLabel = new JLabel("Minimum Primary Open Loop Enrichment (Throttle) - WOT override for POL Fueling table. Set above 16 if you don't have this table.");
        wotEnrichmentNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_wotEnrichmentNoteLabel = new GridBagConstraints();
        gbc_wotEnrichmentNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_wotEnrichmentNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_wotEnrichmentNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_wotEnrichmentNoteLabel.gridx = 0;
        gbc_wotEnrichmentNoteLabel.gridy = row;
        gbc_wotEnrichmentNoteLabel.gridwidth = 4;
        selectionPanel.add(wotEnrichmentNoteLabel, gbc_wotEnrichmentNoteLabel);
        
        row += 1;
        JLabel wotEnrichmentLabel = new JLabel(minWOTEnrichmentLabelText);
        GridBagConstraints gbc_wotEnrichmentLabel = new GridBagConstraints();
        gbc_wotEnrichmentLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_wotEnrichmentLabel.insets = insets3;
        gbc_wotEnrichmentLabel.gridx = 0;
        gbc_wotEnrichmentLabel.gridy = row;
        selectionPanel.add(wotEnrichmentLabel, gbc_wotEnrichmentLabel);
        
        wotEnrichmentField = new JFormattedTextField(doubleFmt);
        wotEnrichmentField.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_wotEnrichmentField = new GridBagConstraints();
        gbc_wotEnrichmentField.anchor = GridBagConstraints.NORTHWEST;
        gbc_wotEnrichmentField.insets = insets3;
        gbc_wotEnrichmentField.gridx = 1;
        gbc_wotEnrichmentField.gridy = row;
        selectionPanel.add(wotEnrichmentField, gbc_wotEnrichmentField);
        
        JButton wotEnrichmentDefaultButton = new JButton("default");
        GridBagConstraints gbc_wotEnrichmentDefaultButton = new GridBagConstraints();
        gbc_wotEnrichmentDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_wotEnrichmentDefaultButton.insets = insets0;
        gbc_wotEnrichmentDefaultButton.gridx = 2;
        gbc_wotEnrichmentDefaultButton.gridy = row;
        gbc_wotEnrichmentDefaultButton.gridwidth = 2;
        wotEnrichmentDefaultButton.setActionCommand("wotenrich");
        wotEnrichmentDefaultButton.setMinimumSize(minDefaultButtonDimension);
        wotEnrichmentDefaultButton.setMargin(insets2);
        wotEnrichmentDefaultButton.setBorder(border);
        wotEnrichmentDefaultButton.addActionListener(this);
        selectionPanel.add(wotEnrichmentDefaultButton, gbc_wotEnrichmentDefaultButton);
    }
    
    protected void addWideBandAFRRowOffsetFilter() {
    	row += 1;
        // Wide Band AFR row offset
        JLabel wbo2RowOffsetNoteLabel = new JLabel("Delay between the ECU readings and the wideband O2 reading. Eg if correct WBO2 for row 1 is on row 2 then offset is 1.");
        wbo2RowOffsetNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_wbo2RowOffsetNoteLabel = new GridBagConstraints();
        gbc_wbo2RowOffsetNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_wbo2RowOffsetNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_wbo2RowOffsetNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_wbo2RowOffsetNoteLabel.gridx = 0;
        gbc_wbo2RowOffsetNoteLabel.gridy = row;
        gbc_wbo2RowOffsetNoteLabel.gridwidth = 4;
        selectionPanel.add(wbo2RowOffsetNoteLabel, gbc_wbo2RowOffsetNoteLabel);
        
        row += 1;
        JLabel wbo2RowOffsetLabel = new JLabel(wbo2RowOffsetLabelText);
        GridBagConstraints gbc_wbo2RowOffsetLabel = new GridBagConstraints();
        gbc_wbo2RowOffsetLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_wbo2RowOffsetLabel.insets = insets3;
        gbc_wbo2RowOffsetLabel.gridx = 0;
        gbc_wbo2RowOffsetLabel.gridy = row;
        selectionPanel.add(wbo2RowOffsetLabel, gbc_wbo2RowOffsetLabel);
        
        wbo2RowOffsetField = new JFormattedTextField(intFmt);
        wbo2RowOffsetField.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_wbo2RowOffsetField = new GridBagConstraints();
        gbc_wbo2RowOffsetField.anchor = GridBagConstraints.NORTHWEST;
        gbc_wbo2RowOffsetField.insets = insets3;
        gbc_wbo2RowOffsetField.gridx = 1;
        gbc_wbo2RowOffsetField.gridy = row;
        selectionPanel.add(wbo2RowOffsetField, gbc_wbo2RowOffsetField);
        
        JButton wbo2RowOffsetDefaultButton = new JButton("default");
        GridBagConstraints gbc_wbo2RowOffsetDefaultButton = new GridBagConstraints();
        gbc_wbo2RowOffsetDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_wbo2RowOffsetDefaultButton.insets = insets0;
        gbc_wbo2RowOffsetDefaultButton.gridx = 2;
        gbc_wbo2RowOffsetDefaultButton.gridy = row;
        gbc_wbo2RowOffsetDefaultButton.gridwidth = 2;
        wbo2RowOffsetDefaultButton.setActionCommand("wbo2offset");
        wbo2RowOffsetDefaultButton.setMinimumSize(minDefaultButtonDimension);
        wbo2RowOffsetDefaultButton.setMargin(insets2);
        wbo2RowOffsetDefaultButton.setBorder(border);
        wbo2RowOffsetDefaultButton.addActionListener(this);
        selectionPanel.add(wbo2RowOffsetDefaultButton, gbc_wbo2RowOffsetDefaultButton);
    }
    
    protected void addOLCLTransitionSkipRowsFilter() {        
        row += 1;
        // OL/CL Transition skip rows
        JLabel olClTransitionSkipRowsNoteLabel = new JLabel("Skip the first and last N rows on Open Loop / Closed Loop transition.");
        olClTransitionSkipRowsNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_olClTransitionSkipRowsNoteLabel = new GridBagConstraints();
        gbc_olClTransitionSkipRowsNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_olClTransitionSkipRowsNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_olClTransitionSkipRowsNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_olClTransitionSkipRowsNoteLabel.gridx = 0;
        gbc_olClTransitionSkipRowsNoteLabel.gridy = row;
        gbc_olClTransitionSkipRowsNoteLabel.gridwidth = 4;
        selectionPanel.add(olClTransitionSkipRowsNoteLabel, gbc_olClTransitionSkipRowsNoteLabel);
        
        row += 1;
        JLabel olClTransitionSkipRowsLabel = new JLabel(olClTransitionSkipRowsLabelText);
        GridBagConstraints gbc_olClTransitionSkipRowsLabel = new GridBagConstraints();
        gbc_olClTransitionSkipRowsLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_olClTransitionSkipRowsLabel.insets = insets3;
        gbc_olClTransitionSkipRowsLabel.gridx = 0;
        gbc_olClTransitionSkipRowsLabel.gridy = row;
        selectionPanel.add(olClTransitionSkipRowsLabel, gbc_olClTransitionSkipRowsLabel);
        
        olClTransitionSkipRowsField = new JFormattedTextField(intFmt);
        olClTransitionSkipRowsField.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_olClTransitionSkipRowsField = new GridBagConstraints();
        gbc_olClTransitionSkipRowsField.anchor = GridBagConstraints.NORTHWEST;
        gbc_olClTransitionSkipRowsField.insets = insets3;
        gbc_olClTransitionSkipRowsField.gridx = 1;
        gbc_olClTransitionSkipRowsField.gridy = row;
        selectionPanel.add(olClTransitionSkipRowsField, gbc_olClTransitionSkipRowsField);
        
        JButton olClTransitionSkipRowsDefaultButton = new JButton("default");
        GridBagConstraints gbc_olClTransitionSkipRowsDefaultButton = new GridBagConstraints();
        gbc_olClTransitionSkipRowsDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_olClTransitionSkipRowsDefaultButton.insets = insets0;
        gbc_olClTransitionSkipRowsDefaultButton.gridx = 2;
        gbc_olClTransitionSkipRowsDefaultButton.gridy = row;
        gbc_olClTransitionSkipRowsDefaultButton.gridwidth = 2;
        olClTransitionSkipRowsDefaultButton.setActionCommand("olcltransit");
        olClTransitionSkipRowsDefaultButton.setMinimumSize(minDefaultButtonDimension);
        olClTransitionSkipRowsDefaultButton.setMargin(insets2);
        olClTransitionSkipRowsDefaultButton.setBorder(border);
        olClTransitionSkipRowsDefaultButton.addActionListener(this);
        selectionPanel.add(olClTransitionSkipRowsDefaultButton, gbc_olClTransitionSkipRowsDefaultButton);
    }
    
    protected void addCLOLStatusFilter() {
        row += 1;
        // CL/OL Status value for CL Note
        JLabel clolStatusNoteLabel = new JLabel("Filter out data using logged OL/CL status (EcuTek CL: 2, OL: 4, OP2/RR CL: 8, OL: 7)");
        clolStatusNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_clolStatusNoteLabel = new GridBagConstraints();
        gbc_clolStatusNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_clolStatusNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_clolStatusNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_clolStatusNoteLabel.gridx = 0;
        gbc_clolStatusNoteLabel.gridy = row;
        gbc_clolStatusNoteLabel.gridwidth = 4;
        selectionPanel.add(clolStatusNoteLabel, gbc_clolStatusNoteLabel);
        
        row += 1;
        JLabel clolStatusLabel = new JLabel(clolStatusValLabelText);
        GridBagConstraints gbc_clolStatusLabel = new GridBagConstraints();
        gbc_clolStatusLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_clolStatusLabel.insets = insets3;
        gbc_clolStatusLabel.gridx = 0;
        gbc_clolStatusLabel.gridy = row;
        selectionPanel.add(clolStatusLabel, gbc_clolStatusLabel);
        
        clolStatusFilter = new JSpinner(new SpinnerNumberModel(Config.getClOlStatusValue(), -1, 10, 1));
        clolStatusFilter.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_clolStatusFilter = new GridBagConstraints();
        gbc_clolStatusFilter.anchor = GridBagConstraints.NORTHWEST;
        gbc_clolStatusFilter.insets = insets3;
        gbc_clolStatusFilter.gridx = 1;
        gbc_clolStatusFilter.gridy = row;
        selectionPanel.add(clolStatusFilter, gbc_clolStatusFilter);
        
        JButton clolStatusDefaultButton = new JButton("default");
        GridBagConstraints gbc_clolStatusDefaultButton = new GridBagConstraints();
        gbc_clolStatusDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_clolStatusDefaultButton.insets = insets0;
        gbc_clolStatusDefaultButton.gridx = 2;
        gbc_clolStatusDefaultButton.gridy = row;
        gbc_clolStatusDefaultButton.gridwidth = 2;
        clolStatusDefaultButton.setActionCommand("clolstatus");
        clolStatusDefaultButton.setMinimumSize(minDefaultButtonDimension);
        clolStatusDefaultButton.setMargin(insets2);
        clolStatusDefaultButton.setBorder(border);
        clolStatusDefaultButton.addActionListener(this);
        selectionPanel.add(clolStatusDefaultButton, gbc_clolStatusDefaultButton);
    }
    
    protected void addCruiseStatusFilter() {
        row += 1;
        // Cruise Status value for LC Note
        JLabel cruiseStatusNoteLabel = new JLabel("Filter out data using logged Cruise/Non-cruise status (leave at -1 if you ROM has only 1 Load Comp table)");
        cruiseStatusNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_cruiseStatusNoteLabel = new GridBagConstraints();
        gbc_cruiseStatusNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_cruiseStatusNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_cruiseStatusNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_cruiseStatusNoteLabel.gridx = 0;
        gbc_cruiseStatusNoteLabel.gridy = row;
        gbc_cruiseStatusNoteLabel.gridwidth = 4;
        selectionPanel.add(cruiseStatusNoteLabel, gbc_cruiseStatusNoteLabel);
        
        row += 1;
        JLabel cruiseStatusLabel = new JLabel(cruiseStatusValLabelText);
        GridBagConstraints gbc_cruiseStatusLabel = new GridBagConstraints();
        gbc_cruiseStatusLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_cruiseStatusLabel.insets = insets3;
        gbc_cruiseStatusLabel.gridx = 0;
        gbc_cruiseStatusLabel.gridy = row;
        selectionPanel.add(cruiseStatusLabel, gbc_cruiseStatusLabel);
        
        cruiseStatusFilter = new JSpinner(new SpinnerNumberModel(Config.getCruiseStatusValue(), -1, 10, 1));
        cruiseStatusFilter.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_cruiseStatusFilter = new GridBagConstraints();
        gbc_cruiseStatusFilter.anchor = GridBagConstraints.NORTHWEST;
        gbc_cruiseStatusFilter.insets = insets3;
        gbc_cruiseStatusFilter.gridx = 1;
        gbc_cruiseStatusFilter.gridy = row;
        selectionPanel.add(cruiseStatusFilter, gbc_cruiseStatusFilter);
        
        JButton cruiseStatusDefaultButton = new JButton("default");
        GridBagConstraints gbc_cruiseStatusDefaultButton = new GridBagConstraints();
        gbc_cruiseStatusDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_cruiseStatusDefaultButton.insets = insets0;
        gbc_cruiseStatusDefaultButton.gridx = 2;
        gbc_cruiseStatusDefaultButton.gridy = row;
        gbc_cruiseStatusDefaultButton.gridwidth = 2;
        cruiseStatusDefaultButton.setActionCommand("cruisestatus");
        cruiseStatusDefaultButton.setMinimumSize(minDefaultButtonDimension);
        cruiseStatusDefaultButton.setMargin(insets2);
        cruiseStatusDefaultButton.setBorder(border);
        cruiseStatusDefaultButton.addActionListener(this);
        selectionPanel.add(cruiseStatusDefaultButton, gbc_cruiseStatusDefaultButton);
    }
    
    protected void addCorrectionAppliedValue() {
        row += 1;
        // Correction Applied Status value for LC Note
        JLabel correctionAppliedNoteLabel = new JLabel("Percent of correction to apply");
        correctionAppliedNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_correctionAppliedNoteLabel = new GridBagConstraints();
        gbc_correctionAppliedNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_correctionAppliedNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_correctionAppliedNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_correctionAppliedNoteLabel.gridx = 0;
        gbc_correctionAppliedNoteLabel.gridy = row;
        gbc_correctionAppliedNoteLabel.gridwidth = 4;
        selectionPanel.add(correctionAppliedNoteLabel, gbc_correctionAppliedNoteLabel);
        
        row += 1;
        JLabel correctionAppliedLabel = new JLabel(correctionAppliedValLabelText);
        GridBagConstraints gbc_correctionAppliedLabel = new GridBagConstraints();
        gbc_correctionAppliedLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_correctionAppliedLabel.insets = insets3;
        gbc_correctionAppliedLabel.gridx = 0;
        gbc_correctionAppliedLabel.gridy = row;
        selectionPanel.add(correctionAppliedLabel, gbc_correctionAppliedLabel);
        
        correctionAppliedValue = new JSpinner(new SpinnerNumberModel(Config.getLCCorrectionAppliedValue(), 5, 100, 5));
        correctionAppliedValue.setMinimumSize(minFilterDimension);
        GridBagConstraints gbc_correctionAppliedValue = new GridBagConstraints();
        gbc_correctionAppliedValue.anchor = GridBagConstraints.NORTHWEST;
        gbc_correctionAppliedValue.insets = insets3;
        gbc_correctionAppliedValue.gridx = 1;
        gbc_correctionAppliedValue.gridy = row;
        selectionPanel.add(correctionAppliedValue, gbc_correctionAppliedValue);
        
        JButton correctionAppliedDefaultButton = new JButton("default");
        GridBagConstraints gbc_correctionAppliedDefaultButton = new GridBagConstraints();
        gbc_correctionAppliedDefaultButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_correctionAppliedDefaultButton.insets = insets0;
        gbc_correctionAppliedDefaultButton.gridx = 2;
        gbc_correctionAppliedDefaultButton.gridy = row;
        gbc_correctionAppliedDefaultButton.gridwidth = 2;
        correctionAppliedDefaultButton.setActionCommand("corrapply");
        correctionAppliedDefaultButton.setMinimumSize(minDefaultButtonDimension);
        correctionAppliedDefaultButton.setMargin(insets2);
        correctionAppliedDefaultButton.setBorder(border);
        correctionAppliedDefaultButton.addActionListener(this);
        selectionPanel.add(correctionAppliedDefaultButton, gbc_correctionAppliedDefaultButton);
    }
    
    protected void addLoadCompInRatioFlag() {
        row += 1;
        // Load Compensation values in ratio
        JLabel isLoadCompInRatioNoteLabel = new JLabel("Set this if your Load Compensation table values specified as ratio (eg EcuTek)");
        isLoadCompInRatioNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_isLoadCompInRatioNoteLabel = new GridBagConstraints();
        gbc_isLoadCompInRatioNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_isLoadCompInRatioNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_isLoadCompInRatioNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_isLoadCompInRatioNoteLabel.gridx = 0;
        gbc_isLoadCompInRatioNoteLabel.gridy = row;
        gbc_isLoadCompInRatioNoteLabel.gridwidth = 4;
        selectionPanel.add(isLoadCompInRatioNoteLabel, gbc_isLoadCompInRatioNoteLabel);
        
        row += 1;
        JLabel isLoadCompInRatioLabel = new JLabel(isLoadCompInRatioLabelText);
        GridBagConstraints gbc_isLoadCompInRatio = new GridBagConstraints();
        gbc_isLoadCompInRatio.anchor = GridBagConstraints.NORTHEAST;
        gbc_isLoadCompInRatio.insets = insets3;
        gbc_isLoadCompInRatio.gridx = 0;
        gbc_isLoadCompInRatio.gridy = row;
        selectionPanel.add(isLoadCompInRatioLabel, gbc_isLoadCompInRatio);
        
        isLoadCompInRatioBool = new JCheckBox();
        GridBagConstraints gbc_isLoadCompInRatioBool = new GridBagConstraints();
        gbc_isLoadCompInRatioBool.anchor = GridBagConstraints.NORTHWEST;
        gbc_isLoadCompInRatioBool.insets = insets3;
        gbc_isLoadCompInRatioBool.gridx = 1;
        gbc_isLoadCompInRatioBool.gridy = row;
        selectionPanel.add(isLoadCompInRatioBool, gbc_isLoadCompInRatioBool);
    }
    
    protected void addMafIatInRatioFlag() {
        row += 1;
        // MAF IAT Compensation values in ratio
        JLabel isMafIatInRatioNoteLabel = new JLabel("Set this if your MAF IAT Compensation table values specified as ratio (eg EcuTek)");
        isMafIatInRatioNoteLabel.setForeground(Color.BLUE);
        GridBagConstraints gbc_isMafIatInRatioNoteLabel = new GridBagConstraints();
        gbc_isMafIatInRatioNoteLabel.anchor = GridBagConstraints.WEST;
        gbc_isMafIatInRatioNoteLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_isMafIatInRatioNoteLabel.insets = new Insets(5, 10, 5, 5);
        gbc_isMafIatInRatioNoteLabel.gridx = 0;
        gbc_isMafIatInRatioNoteLabel.gridy = row;
        gbc_isMafIatInRatioNoteLabel.gridwidth = 4;
        selectionPanel.add(isMafIatInRatioNoteLabel, gbc_isMafIatInRatioNoteLabel);
        
        row += 1;
        JLabel isMafIatInRatioLabel = new JLabel(isMafIatInRatioLabelText);
        GridBagConstraints gbc_isMafIatInRatio = new GridBagConstraints();
        gbc_isMafIatInRatio.anchor = GridBagConstraints.NORTHEAST;
        gbc_isMafIatInRatio.insets = insets3;
        gbc_isMafIatInRatio.gridx = 0;
        gbc_isMafIatInRatio.gridy = row;
        selectionPanel.add(isMafIatInRatioLabel, gbc_isMafIatInRatio);
        
        isMafIatInRatioBool = new JCheckBox();
        GridBagConstraints gbc_isMafIatInRatioBool = new GridBagConstraints();
        gbc_isMafIatInRatioBool.anchor = GridBagConstraints.NORTHWEST;
        gbc_isMafIatInRatioBool.insets = insets3;
        gbc_isMafIatInRatioBool.gridx = 1;
        gbc_isMafIatInRatioBool.gridy = row;
        selectionPanel.add(isMafIatInRatioBool, gbc_isMafIatInRatioBool);
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
        if ("thrtlAngle".equals(e.getActionCommand()))
        	thrtlAngleName.setText(value);
        else if ("afrlearn".equals(e.getActionCommand()))
        	afLearningName.setText(value);
        else if ("afrcorr".equals(e.getActionCommand()))
        	afCorrectionName.setText(value);
        else if ("mafv".equals(e.getActionCommand()))
        	mafVName.setText(value);
        else if ("maf".equals(e.getActionCommand()))
        	mafName.setText(value);
        else if ("wbafr".equals(e.getActionCommand()))
        	wbAfrName.setText(value);
        else if ("rpm".equals(e.getActionCommand()))
        	rpmName.setText(value);
        else if ("load".equals(e.getActionCommand()))
        	loadName.setText(value);
        else if ("mp".equals(e.getActionCommand()))
        	mpName.setText(value);
        else if ("cmdafr".equals(e.getActionCommand()))
        	commAfrName.setText(value);
        else if ("afr".equals(e.getActionCommand()))
        	stockAfrName.setText(value);
        else if ("clolstat".equals(e.getActionCommand()))
        	clolStatusName.setText(value);
        else if ("cruisestat".equals(e.getActionCommand()))
        	cruiseStatusName.setText(value);
        else if ("time".equals(e.getActionCommand()))
        	timeName.setText(value);
        else if ("iat".equals(e.getActionCommand()))
        	iatName.setText(value);
        else if ("ffb".equals(e.getActionCommand()))
        	ffbName.setText(value);
        else if ("veflow".equals(e.getActionCommand()))
        	veFlowName.setText(value);
    }
}

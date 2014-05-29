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

import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import javax.swing.table.DefaultTableModel;

public class ColumnsFiltersSelection implements ActionListener {
	public enum  TaskTab {
		OPEN_LOOP,
		CLOSED_LOOP,
		LOAD_COMP
	}
    private static final String rpmLabelText = "Engine Speed";
    private static final String loadLabelText = "Engine Load";
    private static final String mpLabelText = "Manifold Pressure";
    private static final String afLearningLabelText = "AFR Learning (LTFT)";
    private static final String afCorrectionLabelText = "AFR Correction (STFT)";
    private static final String mafVLabelText = "MAF Voltage";
    private static final String wbAfrLabelText = "Wideband AFR";
    private static final String thtlAngleLabelText = "Throttle Angle %";
    private static final String commAfrLabelText = "Commanded AFR";
    private static final String stockAfrLabelText = "Stock AFR";
    private static final String clolStatusLabelText = "CL/OL Status";
    private static final String timeLabelText = "Time";
    private static final String iatLabelText = "Intake Air Temperature";
    private static final String minMafVLabelText = "MAF Voltage Minimum";
    private static final String maxMafVLabelText = "MAF Voltage Maximum";
    private static final String maxIatLabelText = "IAT Maximum";
    private static final String maxAfrLabelText = "AFR Maximum";
    private static final String minAfrLabelText = "AFR Minimum";
    private static final String maxDvdtLabelText = "dV/dt Maximum";
    private static final String minEngineLoadLabelText = "Engine Load Minimum";
    private static final String wotStationaryLabelText = "WOT stationary point (Angle %)";
    private static final String afrErrorLabelText = "AFR Error +/- % value";
    private static final String iatOffsetLabelText = "IAT variance from lowest";
    private static final String trimsVarianceLabelText = "Fuel Trims +/- variance";
	private TaskTab taskTab;
	private boolean isPolfTableSet;
	private JTable columnsTable = null;
	private JTextField thtlAngleName = null;
	private JTextField afLearningName = null;
	private JTextField afCorrectionName = null;
	private JTextField mafVName = null;
	private JTextField wbAfrName = null;
	private JTextField stockAfrName = null;
	private JTextField rpmName = null;
	private JTextField loadName = null;
	private JTextField mpName = null;
	private JTextField commAfrName = null;
	private JTextField clolStatusName = null;
	private JTextField timeName = null;
	private JTextField iatName = null;
	private JFormattedTextField minMafVFilter = null;
	private JFormattedTextField maxMafVFilter = null;
	private JFormattedTextField minEngineLoadFilter = null;
	private JFormattedTextField afrErrorFilter = null;
	private JFormattedTextField maxAfrFilter = null;
	private JFormattedTextField minAfrFilter = null;
	private JFormattedTextField trimsVarianceFilter = null;
	private JFormattedTextField maxIatFilter = null;
	private JFormattedTextField maxDvdtFilter = null;
	private JSpinner wotStationaryPointFilter = null;
	private JSpinner clolStatusFilter = null;
	
	public ColumnsFiltersSelection(TaskTab taskTab, boolean isPolfTableSet) {
		this.taskTab = taskTab;
		this.isPolfTableSet = isPolfTableSet;
	}
    
    public boolean getUserSettings(String[] columns) {
        JPanel selectionPanel = new JPanel();
        GridBagLayout gbl_dataPanel = new GridBagLayout();
        gbl_dataPanel.columnWidths = new int[]{0, 0, 0, 0};
        gbl_dataPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_dataPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0};
        gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        selectionPanel.setLayout(gbl_dataPanel);
        
        Dimension minTextDimension = new Dimension(200, 16);
        Dimension minFilterDimension = new Dimension(80, 16);
        Insets insets0 = new Insets(0, 0, 0, 0);
        Insets insets1 = new Insets(1, 1, 1, 1);
        Insets insets3 = new Insets(3, 3, 3, 3);
        NumberFormat doubleFmt = NumberFormat.getNumberInstance();
        doubleFmt.setMaximumFractionDigits(2);
        ImageIcon arrowImage = new ImageIcon(getClass().getResource("/arrow.jpg"));
        
        int row = 0;
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
        
        if (taskTab == TaskTab.LOAD_COMP) {
	        row += 1;
	        // MP
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
        else {
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
        
        if (taskTab == TaskTab.OPEN_LOOP) {
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

	        row += 1;
	        // Throttle Angle
	        JLabel thtlAngleLabel = new JLabel(thtlAngleLabelText);
	        GridBagConstraints gbc_thtlAngleLabel = new GridBagConstraints();
	        gbc_thtlAngleLabel.anchor = GridBagConstraints.NORTHEAST;
	        gbc_thtlAngleLabel.insets = insets3;
	        gbc_thtlAngleLabel.gridx = 0;
	        gbc_thtlAngleLabel.gridy = row;
	        selectionPanel.add(thtlAngleLabel, gbc_thtlAngleLabel);
	        
	        thtlAngleName = new JTextField(isEmpty(Config.getThrottleAngleColumnName()));
	        thtlAngleName.setMinimumSize(minTextDimension);
	        thtlAngleName.setEditable(false);
	        thtlAngleName.setBackground(Color.WHITE);
	        GridBagConstraints gbc_thtlAngleName= new GridBagConstraints();
	        gbc_thtlAngleName.anchor = GridBagConstraints.NORTHWEST;
	        gbc_thtlAngleName.insets = insets3;
	        gbc_thtlAngleName.gridx = 1;
	        gbc_thtlAngleName.gridy = row;
	        selectionPanel.add(thtlAngleName, gbc_thtlAngleName);
	
	        JButton thtlAngleButton = new JButton("", arrowImage);
	        thtlAngleButton.setMargin(insets0);
	        thtlAngleButton.setBorderPainted(false);
	        thtlAngleButton.setContentAreaFilled(false);
	        GridBagConstraints gbc_thtlAngleButton = new GridBagConstraints();
	        gbc_thtlAngleButton.anchor = GridBagConstraints.CENTER;
	        gbc_thtlAngleButton.insets = insets1;
	        gbc_thtlAngleButton.gridx = 2;
	        gbc_thtlAngleButton.gridy = row;
	        thtlAngleButton.setActionCommand("thtlAngle");
	        thtlAngleButton.addActionListener(this);
	        selectionPanel.add(thtlAngleButton, gbc_thtlAngleButton);

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
        else if (taskTab == TaskTab.CLOSED_LOOP) {
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
        else if (taskTab == TaskTab.LOAD_COMP) {
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
        gbc_selectionPanel.gridheight = (taskTab == TaskTab.OPEN_LOOP ? 8 : (taskTab == TaskTab.CLOSED_LOOP ? 9 : 7));
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
        
        if (taskTab == TaskTab.OPEN_LOOP) {
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
	        minMafVFilter.setText(String.valueOf(Config.getMafVMinimumValue()));
	        minMafVFilter.setMinimumSize(minFilterDimension);
	        GridBagConstraints gbc_minMafVFilter = new GridBagConstraints();
	        gbc_minMafVFilter.anchor = GridBagConstraints.NORTHWEST;
	        gbc_minMafVFilter.insets = insets3;
	        gbc_minMafVFilter.gridx = 1;
	        gbc_minMafVFilter.gridy = row;
	        selectionPanel.add(minMafVFilter, gbc_minMafVFilter);
	        
            row += 1;
            // WOT Stationary Point Note
            JLabel wotPointNoteLabel = new JLabel("CL/OL transition. Use \"Throttle Angle %\" but could use \"Accel Pedal Angle %\" instead");
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
	        
	        wotStationaryPointFilter = new JSpinner(new SpinnerNumberModel(Config.getWotStationaryPointValue(), 50, 100, 5));
	        wotStationaryPointFilter.setMinimumSize(minFilterDimension);
	        GridBagConstraints gbc_wotStationaryPointFilter = new GridBagConstraints();
	        gbc_wotStationaryPointFilter.anchor = GridBagConstraints.NORTHWEST;
	        gbc_wotStationaryPointFilter.insets = insets3;
	        gbc_wotStationaryPointFilter.gridx = 1;
	        gbc_wotStationaryPointFilter.gridy = row;
	        selectionPanel.add(wotStationaryPointFilter, gbc_wotStationaryPointFilter);
	        
            row += 1;
            // AFR Error Percent Note
            JLabel afrErrorNoteLabel = new JLabel("Remove data where \"AFR Error %\" exceeds desired change %");
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
	        afrErrorFilter.setText(String.valueOf(Config.getWidebandAfrErrorPercentValue()));
	        afrErrorFilter.setMinimumSize(minFilterDimension);
	        GridBagConstraints gbc_afrErrorFilter = new GridBagConstraints();
	        gbc_afrErrorFilter.anchor = GridBagConstraints.NORTHWEST;
	        gbc_afrErrorFilter.insets = insets3;
	        gbc_afrErrorFilter.gridx = 1;
	        gbc_afrErrorFilter.gridy = row;
	        selectionPanel.add(afrErrorFilter, gbc_afrErrorFilter);
        }
        else if (taskTab == TaskTab.CLOSED_LOOP) {
            row += 1;
            // CL/OL Status value for CL Note
            JLabel clolStatusNoteLabel = new JLabel("Set this filter to filter out Open Loop data using logged OL/CL status");
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
	        JLabel clolStatusLabel = new JLabel(clolStatusLabelText);
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
	        maxMafVFilter.setText(String.valueOf(Config.getMafVMaximumValue()));
	        maxMafVFilter.setMinimumSize(minFilterDimension);
	        GridBagConstraints gbc_maxMafVFilter = new GridBagConstraints();
	        gbc_maxMafVFilter.anchor = GridBagConstraints.NORTHWEST;
	        gbc_maxMafVFilter.insets = insets3;
	        gbc_maxMafVFilter.gridx = 1;
	        gbc_maxMafVFilter.gridy = row;
	        selectionPanel.add(maxMafVFilter, gbc_maxMafVFilter);

            row += 1;
            // Engine Load Minimum Note
            JLabel minEngineLoadNoteLabel = new JLabel("Set this filter to process data above specific Engine Load (eg filter out idle)");
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
	        minEngineLoadFilter.setText(String.valueOf(Config.getLoadMinimumValue()));
	        minEngineLoadFilter.setMinimumSize(minFilterDimension);
	        GridBagConstraints gbc_minEngineLoadFilter = new GridBagConstraints();
	        gbc_minEngineLoadFilter.anchor = GridBagConstraints.NORTHWEST;
	        gbc_minEngineLoadFilter.insets = insets3;
	        gbc_minEngineLoadFilter.gridx = 1;
	        gbc_minEngineLoadFilter.gridy = row;
	        selectionPanel.add(minEngineLoadFilter, gbc_minEngineLoadFilter);

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
	        maxIatFilter.setText(String.valueOf(Config.getIatMaximumValue()));
	        maxIatFilter.setMinimumSize(minFilterDimension);
	        GridBagConstraints gbc_maxIatFilter = new GridBagConstraints();
	        gbc_maxIatFilter.anchor = GridBagConstraints.NORTHWEST;
	        gbc_maxIatFilter.insets = insets3;
	        gbc_maxIatFilter.gridx = 1;
	        gbc_maxIatFilter.gridy = row;
	        selectionPanel.add(maxIatFilter, gbc_maxIatFilter);
	        
            row += 1;
            // AFR Maximum Note
            JLabel maxAfrNoteLabel = new JLabel("Remove data where \"AFR\" is above the specified maximum");
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
	        maxAfrFilter.setText(String.valueOf(Config.getAfrMaximumValue()));
	        maxAfrFilter.setMinimumSize(minFilterDimension);
	        GridBagConstraints gbc_maxAfrFilter = new GridBagConstraints();
	        gbc_maxAfrFilter.anchor = GridBagConstraints.NORTHWEST;
	        gbc_maxAfrFilter.insets = insets3;
	        gbc_maxAfrFilter.gridx = 1;
	        gbc_maxAfrFilter.gridy = row;
	        selectionPanel.add(maxAfrFilter, gbc_maxAfrFilter);
	        
            row += 1;
            // AFR Minimum Note
            JLabel minAfrNoteLabel = new JLabel("Remove data where \"AFR\" is below the specified minimum");
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
	        minAfrFilter.setText(String.valueOf(Config.getAfrMinimumValue()));
	        minAfrFilter.setMinimumSize(minFilterDimension);
	        GridBagConstraints gbc_minAfrFilter = new GridBagConstraints();
	        gbc_minAfrFilter.anchor = GridBagConstraints.NORTHWEST;
	        gbc_minAfrFilter.insets = insets3;
	        gbc_minAfrFilter.gridx = 1;
	        gbc_minAfrFilter.gridy = row;
	        selectionPanel.add(minAfrFilter, gbc_minAfrFilter);
	        
            row += 1;
            // dVdt Maximum Note
            JLabel maxDvdtNoteLabel = new JLabel("Remove data where \"dV/dt\" is above the specified maximum");
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
	        maxDvdtFilter.setText(String.valueOf(Config.getDvDtMaximumValue()));
	        maxDvdtFilter.setMinimumSize(minFilterDimension);
	        GridBagConstraints gbc_maxDvdtFilter = new GridBagConstraints();
	        gbc_maxDvdtFilter.anchor = GridBagConstraints.NORTHWEST;
	        gbc_maxDvdtFilter.insets = insets3;
	        gbc_maxDvdtFilter.gridx = 1;
	        gbc_maxDvdtFilter.gridy = row;
	        selectionPanel.add(maxDvdtFilter, gbc_maxDvdtFilter);
        }
        else if (taskTab == TaskTab.LOAD_COMP) {
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
	        JLabel maxIatLabel = new JLabel(iatOffsetLabelText);
	        GridBagConstraints gbc_maxIatLabel = new GridBagConstraints();
	        gbc_maxIatLabel.anchor = GridBagConstraints.NORTHEAST;
	        gbc_maxIatLabel.insets = insets3;
	        gbc_maxIatLabel.gridx = 0;
	        gbc_maxIatLabel.gridy = row;
	        selectionPanel.add(maxIatLabel, gbc_maxIatLabel);
	        
	        maxIatFilter = new JFormattedTextField(doubleFmt);
	        maxIatFilter.setText(String.valueOf(Config.getIatLCMinimumOffset()));
	        maxIatFilter.setMinimumSize(minFilterDimension);
	        GridBagConstraints gbc_maxIatFilter = new GridBagConstraints();
	        gbc_maxIatFilter.anchor = GridBagConstraints.NORTHWEST;
	        gbc_maxIatFilter.insets = insets3;
	        gbc_maxIatFilter.gridx = 1;
	        gbc_maxIatFilter.gridy = row;
	        selectionPanel.add(maxIatFilter, gbc_maxIatFilter);
	        
            row += 1;
            // Trims Maximum Note
            JLabel maxTrimsNoteLabel = new JLabel("Remove data where combined \"Fuel Trims\" are above/below the specified variance");
            maxTrimsNoteLabel.setForeground(Color.BLUE);
            GridBagConstraints gbc_maxTrimsNoteLabel = new GridBagConstraints();
            gbc_maxTrimsNoteLabel.anchor = GridBagConstraints.WEST;
            gbc_maxTrimsNoteLabel.fill = GridBagConstraints.HORIZONTAL;
            gbc_maxTrimsNoteLabel.insets = new Insets(5, 10, 5, 5);
            gbc_maxTrimsNoteLabel.gridx = 0;
            gbc_maxTrimsNoteLabel.gridy = row;
            gbc_maxTrimsNoteLabel.gridwidth = 4;
            selectionPanel.add(maxTrimsNoteLabel, gbc_maxTrimsNoteLabel);
	        
	        row += 1;
	        JLabel trimsVarianceLabel = new JLabel(trimsVarianceLabelText);
	        GridBagConstraints gbc_trimsVarianceLabel = new GridBagConstraints();
	        gbc_trimsVarianceLabel.anchor = GridBagConstraints.NORTHEAST;
	        gbc_trimsVarianceLabel.insets = insets3;
	        gbc_trimsVarianceLabel.gridx = 0;
	        gbc_trimsVarianceLabel.gridy = row;
	        selectionPanel.add(trimsVarianceLabel, gbc_trimsVarianceLabel);
	        
	        trimsVarianceFilter = new JFormattedTextField(doubleFmt);
	        trimsVarianceFilter.setText(String.valueOf(Config.getTrimsLCVarianceValue()));
	        trimsVarianceFilter.setMinimumSize(minFilterDimension);
	        GridBagConstraints gbc_trimsVarianceFilter = new GridBagConstraints();
	        gbc_trimsVarianceFilter.anchor = GridBagConstraints.NORTHWEST;
	        gbc_trimsVarianceFilter.insets = insets3;
	        gbc_trimsVarianceFilter.gridx = 1;
	        gbc_trimsVarianceFilter.gridy = row;
	        selectionPanel.add(trimsVarianceFilter, gbc_trimsVarianceFilter);
	        
            row += 1;
            // dVdt Maximum Note
            JLabel maxDvdtNoteLabel = new JLabel("Remove data where \"dV/dt\" is above the specified maximum");
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
	        maxDvdtFilter.setText(String.valueOf(Config.getDvDtLCMaximumValue()));
	        maxDvdtFilter.setMinimumSize(minFilterDimension);
	        GridBagConstraints gbc_maxDvdtFilter = new GridBagConstraints();
	        gbc_maxDvdtFilter.anchor = GridBagConstraints.NORTHWEST;
	        gbc_maxDvdtFilter.insets = insets3;
	        gbc_maxDvdtFilter.gridx = 1;
	        gbc_maxDvdtFilter.gridy = row;
	        selectionPanel.add(maxDvdtFilter, gbc_maxDvdtFilter);
        }
        
        // Set window params
        selectionPanel.setPreferredSize(new Dimension(650, (taskTab == TaskTab.CLOSED_LOOP ? 600 : 400)));
        JScrollPane pane = new JScrollPane(selectionPanel);
        pane.setPreferredSize(new Dimension(670, 420));
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
    
    public String isEmpty(String current) {
    	if (current.equals(Config.NO_NAME))
    		return "";
    	return current;
    }
    
    private boolean validate() {
    	boolean ret = true;
    	String error = "";
    	String value;
    	String colName;

    	// Engine Speed
    	value = rpmName.getText().trim();
    	colName = rpmLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error += "\"" + colName + "\" column must be specified\n";
    	}
    	else
    		Config.setRpmColumnName(value);
    	if (taskTab == TaskTab.LOAD_COMP) {
	    	// MP
	    	value = mpName.getText().trim();
	    	colName = mpLabelText;
	    	if (value.isEmpty()) {
	    		ret = false;
	    		error += "\"" + colName + "\" column must be specified\n";
	    	}
	    	else
	    		Config.setMpColumnName(value);
    	}
    	else {
	    	// Engine Load
	    	value = loadName.getText().trim();
	    	colName = loadLabelText;
	    	if (value.isEmpty()) {
	    		ret = false;
	    		error += "\"" + colName + "\" column must be specified\n";
	    	}
	    	else
	    		Config.setLoadColumnName(value);
    	}
    	// AFR Learning
    	value = afLearningName.getText().trim();
    	colName = afLearningLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error += "\"" + colName + "\" column must be specified\n";
    	}
    	else
    		Config.setAfLearningColumnName(value);
    	// AFR Correction
    	value = afCorrectionName.getText().trim();
    	colName = afCorrectionLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error += "\"" + colName + "\" column must be specified\n";
    	}
    	else
    		Config.setAfCorrectionColumnName(value);
    	// Maf Voltage
    	value = mafVName.getText().trim();
    	colName = mafVLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error += "\"" + colName + "\" column must be specified\n";
    	}
    	else
    		Config.setMafVoltageColumnName(value);
    	if (taskTab == TaskTab.OPEN_LOOP) {
	    	// Wideband AFR
	    	value = wbAfrName.getText().trim();
	    	colName = wbAfrLabelText;
	    	if (value.isEmpty()) {
	    		ret = false;
	    		error += "\"" + colName + "\" column must be specified\n";
	    	}
	    	else
	    		Config.setWidebandAfrColumnName(value);
	    	// Throttle Angle
	    	value = thtlAngleName.getText().trim();
	    	colName = thtlAngleLabelText;
	    	if (value.isEmpty()) {
	    		ret = false;
	    		error += "\"" + colName + "\" column must be specified\n";
	    	}
	    	else
	    		Config.setThrottleAngleColumnName(value);
	    	// Commanded AFR
	    	value = commAfrName.getText().trim();
	    	colName = commAfrLabelText;
	    	if (isPolfTableSet) {
		    	if (value.isEmpty())
		    		value = Config.NO_NAME;
		    	Config.setCommandedAfrColumnName(value);
	    	}
	    	else {
		    	if (value.isEmpty()) {
		    		ret = false;
		    		error += "\"" + colName + "\" column must be specified if \"Primary Open Loop Fueling\" table is not set.\n";
		    	}
		    	else
		    		Config.setCommandedAfrColumnName(value);
	    	}
	    	// Min MAF Voltage filter
	    	Config.setMafVMinimumValue(Double.valueOf(minMafVFilter.getText()));
	    	// WOT Stationary point
	    	Config.setWotStationaryPointValue(Integer.valueOf(wotStationaryPointFilter.getValue().toString()));
	    	// Afr Error filter
	    	Config.setWidebandAfrErrorPercentValue(Double.valueOf(afrErrorFilter.getText()));
    	}
    	else if (taskTab == TaskTab.CLOSED_LOOP) {
	    	// Stock AFR
	    	value = stockAfrName.getText().trim();
	    	colName = stockAfrLabelText;
	    	if (value.isEmpty()) {
	    		ret = false;
	    		error += "\"" + colName + "\" column must be specified\n";
	    	}
	    	else
	    		Config.setAfrColumnName(value);
	    	// CL/OL Status
	    	value = clolStatusName.getText().trim();
	    	colName = clolStatusLabelText;
	    	if (value.isEmpty()) {
	    		ret = false;
	    		error += "\"" + colName + "\" column must be specified\n";
	    	}
	    	else
	    		Config.setClOlStatusColumnName(value);
	    	// Time
	    	value = timeName.getText().trim();
	    	colName = timeLabelText;
	    	if (value.isEmpty()) {
	    		ret = false;
	    		error += "\"" + colName + "\" column must be specified\n";
	    	}
	    	else
	    		Config.setTimeColumnName(value);
	    	// Intake Air Temperature
	    	value = iatName.getText().trim();
	    	colName = iatLabelText;
	    	if (value.isEmpty()) {
	    		ret = false;
	    		error += "\"" + colName + "\" column must be specified\n";
	    	}
	    	else
	    		Config.setIatColumnName(value);
	    	// CL/OL Status
	    	Config.setClOlStatusValue(Integer.valueOf(clolStatusFilter.getValue().toString()));
	    	// Max MAF Voltage filter
	    	Config.setMafVMaximumValue(Double.valueOf(maxMafVFilter.getText()));
	    	// Engine Load filter
	    	Config.setLoadMinimumValue(Double.valueOf(minEngineLoadFilter.getText()));
	    	// IAT filter
	    	Config.setIatMaximumValue(Double.valueOf(maxIatFilter.getText()));
	    	// AFR filters
	    	Config.setAfrMaximumValue(Double.valueOf(maxAfrFilter.getText()));
	    	Config.setAfrMinimumValue(Double.valueOf(minAfrFilter.getText()));
	    	// dV/dt filter
	    	Config.setDvDtMaximumValue(Double.valueOf(maxDvdtFilter.getText()));
    	}
    	else if (taskTab == TaskTab.LOAD_COMP) {
	    	// Time
	    	value = timeName.getText().trim();
	    	colName = timeLabelText;
	    	if (value.isEmpty()) {
	    		ret = false;
	    		error += "\"" + colName + "\" column must be specified\n";
	    	}
	    	else
	    		Config.setTimeColumnName(value);
	    	// Intake Air Temperature
	    	value = iatName.getText().trim();
	    	colName = iatLabelText;
	    	if (value.isEmpty()) {
	    		ret = false;
	    		error += "\"" + colName + "\" column must be specified\n";
	    	}
	    	else
	    		Config.setIatColumnName(value);
	    	// IAT filter
	    	Config.setIatLCMinimumOffset(Double.valueOf(maxIatFilter.getText()));
	    	// Trims filters
	    	Config.setTrimsLCVarianceValue(Double.valueOf(trimsVarianceFilter.getText()));
	    	// dV/dt filter
	    	Config.setDvDtLCMaximumValue(Double.valueOf(maxDvdtFilter.getText()));
    	}
    	if (!ret)
    		JOptionPane.showMessageDialog(null, error, "Error", JOptionPane.ERROR_MESSAGE);
    	return ret;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	int row = columnsTable.getSelectedRow();
    	if (row < 0) {
    		JOptionPane.showMessageDialog(null, "Please select column name from the list", "Error", JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    	String value = columnsTable.getValueAt(row, 0).toString();
        if ("thtlAngle".equals(e.getActionCommand()))
        	thtlAngleName.setText(value);
        else if ("afrlearn".equals(e.getActionCommand()))
        	afLearningName.setText(value);
        else if ("afrcorr".equals(e.getActionCommand()))
        	afCorrectionName.setText(value);
        else if ("mafv".equals(e.getActionCommand()))
        	mafVName.setText(value);
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
        else if ("time".equals(e.getActionCommand()))
        	timeName.setText(value);
        else if ("iat".equals(e.getActionCommand()))
        	iatName.setText(value);
    }
}

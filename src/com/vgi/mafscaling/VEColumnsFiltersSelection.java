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

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;

public class VEColumnsFiltersSelection extends ColumnsFiltersSelection {
	
	public VEColumnsFiltersSelection(boolean isPolfTableSet) {
		super(isPolfTableSet);
	}
    
    protected int getWindowHeight() {
    	return 850;
    }
    
    protected int getColSelectionGridHeight() {
    	return 9;
    }
    
    protected void addColSelection() {
        addRPMColSelection();
        addIATColSelection();
        addThrottleAngleColSelection();
    	addManifoldPressureColSelection();
        addWidebandAFRColSelection();
        addFFBColSelection();
        addMAFColSelection();
        addVEFlowColSelection();
        addClOlStatusColSelection();
    }
    
    protected void addFilterSelection() {
        addThrottleChangeMaximumFilter();
        thrtlChangeMaxFilter.setValue(Config.getVEThrottleChangeMaxValue());
        addThrottleMinimumFilter();
        thrtlMinimumFilter.setValue(Config.getVEThrottleMinimumValue());
        addAFRMaximumFilter();
        maxAfrFilter.setText(String.valueOf(Config.getVEAfrMaximumValue()));
    	addCLOLStatusFilter();
    	clolStatusFilter.setValue(Config.getClOlStatusValue());
    	addIATMaximumFilter();
        maxIatFilter.setText(String.valueOf(Config.getVEIatMaximumValue()));
        addRPMMinimumFilter();
        minRPMFilter.setText(String.valueOf(Config.getVERPMMinimumValue()));
        addManifoldPressureMinimumFilter();
        minMPFilter.setText(String.valueOf(Config.getVEMPMinimumValue()));
        addFFBMaximumFilter();
        maxFFBFilter.setText(String.valueOf(Config.getFFBMaximumValue()));
        addFFBMinimumFilter();
        minFFBFilter.setText(String.valueOf(Config.getFFBMinimumValue()));
        addWideBandAFRRowOffsetFilter();
        wbo2RowOffsetField.setText(String.valueOf(Config.getWBO2RowOffset()));
        addCellHitCountMinimumFilter();
    	minCellHitCountFilter.setText(String.valueOf(Config.getVEMinCellHitCount()));
    	addCorrectionAppliedValue();
    	correctionAppliedValue.setValue(Config.getVECorrectionAppliedValue());
    	
    	for (Component c : selectionPanel.getComponents()) {
    		if (c instanceof JLabel) {
    			JLabel label = (JLabel)c;
    			if (label.getText().startsWith("Remove data where Throttle Input is below"))
    				label.setText(label.getText() + " (*** works with AFR Maximum filter)");
    			else if (label.getText().startsWith("Remove data where AFR is above"))
    				label.setText(label.getText() + "(*** works with Throttle Input Minimum filter)");
    			else if (label.getText().startsWith("Remove data where RPM is below"))
    				label.setText(label.getText() + " (hint: check min RPM in SD table (y-axis)");
    			else if (label.getText().startsWith("Remove data where Manifold Pressure is below"))
    				label.setText(label.getText() + " (hint: check min MP in SD table (x-axis)");
    			else if (label.getText().equals(clolStatusLabelText))
    				label.setText(clolStatusLabelText + " *");
    		}
    	}
    }
    
    protected boolean validate(StringBuffer error) {
    	boolean ret = true;
    	String value;
    	String colName;
    	
    	// Engine Speed
    	value = rpmName.getText().trim();
    	colName = rpmLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setRpmColumnName(value);
    	
    	// Throttle Angle
    	value = thrtlAngleName.getText().trim();
    	colName = thrtlAngleLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setThrottleAngleColumnName(value);

    	// MP
    	value = mpName.getText().trim();
    	colName = mpLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setMpColumnName(value);

    	// Wideband AFR
    	value = wbAfrName.getText().trim();
    	colName = wbAfrLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setWidebandAfrColumnName(value);

    	// MAF
    	value = mafName.getText().trim();
    	colName = mafLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setMassAirflowColumnName(value);

    	// FFB
    	value = ffbName.getText().trim();
    	colName = ffbLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setFinalFuelingBaseColumnName(value);

    	// FFB
    	value = veFlowName.getText().trim();
    	colName = veFlowLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setVEFlowColumnName(value);

    	
    	// RPM Minimum
    	Config.setVERPMMinimumValue(Integer.valueOf(minRPMFilter.getText()));
    	
    	// MP Minimum
    	Config.setVEMPMinimumValue(Double.valueOf(minMPFilter.getText()));
		
    	// IAT filter
    	Config.setVEIatMaximumValue(Double.valueOf(maxIatFilter.getText()));
    	
    	// Throttle Change % Maximum
    	Config.setVEThrottleChangeMaxValue(Integer.valueOf(thrtlChangeMaxFilter.getValue().toString()));

    	// Throttle Minimum Input
    	Config.setVEThrottleMinimumValue(Integer.valueOf(thrtlMinimumFilter.getText()));
    	
    	// FFB filters
    	Config.setFFBMaximumValue(Double.valueOf(maxFFBFilter.getText()));
    	Config.setFFBMinimumValue(Double.valueOf(minFFBFilter.getText()));
        
        // AFR Maximum
    	Config.setVEAfrMaximumValue(Double.valueOf(maxAfrFilter.getText()));
    	
    	// WBO2 Row Offset
    	Config.setWBO2RowOffset(Integer.valueOf(wbo2RowOffsetField.getText()));
    	
    	// Minimum Cell Hit Count Filter
		Config.setVEMinCellHitCount(Integer.valueOf(minCellHitCountFilter.getText()));
    	
    	// Correction applied
    	Config.setVECorrectionAppliedValue(Integer.valueOf(correctionAppliedValue.getValue().toString()));
    	
    	return ret;
    }
    
    protected boolean processDefaultButton(ActionEvent e) {
    	if ("thrtlchange".equals(e.getActionCommand()))
        	thrtlChangeMaxFilter.setValue(Integer.valueOf(Config.DefaultVEThrottleChangeMax));
        else if ("minrpm".equals(e.getActionCommand()))
        	minRPMFilter.setText(Config.DefaultRPMMinimum);
        else if ("minmp".equals(e.getActionCommand()))
        	minMPFilter.setText(Config.DefaultMPMinimum);
        else if ("maxiat".equals(e.getActionCommand()))
        	maxIatFilter.setText(Config.DefaultVEIATMaximum);
        else if ("maxffb".equals(e.getActionCommand()))
        	maxFFBFilter.setText(Config.DefaultFFBMaximum);
        else if ("minffb".equals(e.getActionCommand()))
        	minFFBFilter.setText(Config.DefaultFFBMinimum);
        else if ("clolstatus".equals(e.getActionCommand()))
        	clolStatusFilter.setValue(Integer.valueOf(Config.DefaultClOlStatusValue));
    	else if ("minthrtl".equals(e.getActionCommand()))
    		thrtlMinimumFilter.setValue(Integer.valueOf(Config.DefaultVEThrottleMinimum));
        else if ("maxafr".equals(e.getActionCommand()))
        	maxAfrFilter.setText(Config.DefaultVEAfrMaximum);
        else if ("wbo2offset".equals(e.getActionCommand()))
        	wbo2RowOffsetField.setText(Config.DefaultWBO2RowOffset);
        else if ("minhitcnt".equals(e.getActionCommand()))
        	minCellHitCountFilter.setText(Config.DefaultVEMinCellHitCount);
        else if ("corrapply".equals(e.getActionCommand()))
        	correctionAppliedValue.setValue(Integer.valueOf(Config.DefaultCorrectionAppliedValue));
        else
        	return false;
        return true;
    }
}

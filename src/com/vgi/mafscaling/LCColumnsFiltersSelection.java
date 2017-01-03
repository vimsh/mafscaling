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
import javax.swing.JEditorPane;

public class LCColumnsFiltersSelection extends ColumnsFiltersSelection {
    
    protected void addColSelection() {
        addRPMColSelection();
        addThrottleAngleColSelection();
        addAFLearningColSelection();
        addAFCorrectionColSelection();
        addMAFVoltageColSelection();
    	addStockAFRColSelection();
    	addManifoldPressureColSelection();
    	addTimeColSelection();
    	addIATColSelection();
        addCruiseStatusColSelection();
    }
    
    protected void addFilterSelection() {
    	addLoadCompInRatioFlag();
        isLoadCompInRatioBool.setSelected(Config.getIsLoadCompInRatio());
        addAtmPressureFilter();
        atmPressureFilter.setText(String.valueOf(Config.getAtmPressureValue()));
        addThrottleChangeMaximumFilter();
        thrtlChangeMaxFilter.setValue(Config.getThrottleChangeMaxValue());
        addRPMMaximumFilter();
        maxRPMFilter.setText(String.valueOf(Config.getRPMMaximumValue()));
        addRPMMinimumFilter();
        minRPMFilter.setText(String.valueOf(Config.getRPMMinimumValue()));
        addIATMaximumFilter();
        maxIatFilter.setText(String.valueOf(Config.getLCIatMaximumValue()));
        addAFRMaximumFilter();
        maxAfrFilter.setText(String.valueOf(Config.getLCAfrMaximumValue()));
        addAFRMinimumFilter();
        minAfrFilter.setText(String.valueOf(Config.getLCAfrMinimumValue()));
        

        addManifoldPressureMaximumFilter();
        maxMPFilter.setText(String.valueOf(Config.getLCMPMaximumValue()));
        addManifoldPressureMinimumFilter();
        minMPFilter.setText(String.valueOf(Config.getLCMPMinimumValue()));
        
        addDvDtMaximumFilter();
        maxDvdtFilter.setText(String.valueOf(Config.getLCDvDtMaximumValue()));
        addCellHitCountMinimumFilter();
    	minCellHitCountFilter.setText(String.valueOf(Config.getLCMinCellHitCount()));
    	addCruiseStatusFilter();
    	cruiseStatusFilter.setValue(Config.getCruiseStatusValue());
    	addCorrectionAppliedValue();
    	correctionAppliedValue.setValue(Config.getLCCorrectionAppliedValue());
    	
    	for (Component c : filtersPanel.getComponents()) {
    		if (c instanceof JEditorPane) {
    			JEditorPane label = (JEditorPane)c;
    			if (label.getText().startsWith("Remove data where RPM is above"))
    				label.setText(label.getText() + " (hint: check max RPM in Load Comp table)");
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

    	// AFR Learning
    	value = afLearningName.getText().trim();
    	colName = afLearningLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setAfLearningColumnName(value);
    	
    	// AFR Correction
    	value = afCorrectionName.getText().trim();
    	colName = afCorrectionLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setAfCorrectionColumnName(value);
    	
    	// Maf Voltage
    	value = mafVName.getText().trim();
    	colName = mafVLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setMafVoltageColumnName(value);

    	// MP
    	value = mpName.getText().trim();
    	colName = mpLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setMpColumnName(value);
    	
    	// Throttle Angle
    	value = thrtlAngleName.getText().trim();
    	colName = thrtlAngleLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setThrottleAngleColumnName(value);

    	// Cruise/Non-cruise Status
    	Config.setCruiseStatusColumnName(cruiseStatusName.getText().trim());

    	// Stock AFR
    	value = stockAfrName.getText().trim();
    	colName = stockAfrLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setAfrColumnName(value);
    	
    	// Time
    	value = timeName.getText().trim();
    	colName = timeLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setTimeColumnName(value);
    	
    	// Intake Air Temperature
    	value = iatName.getText().trim();
    	colName = iatLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setIatColumnName(value);
    	
        // Load Compensation values in ratio
    	Config.setIsLoadCompInRatio(isLoadCompInRatioBool.isSelected());
    	
    	// Atm Pressure filters
    	Config.setAtmPressureValue(Double.valueOf(atmPressureFilter.getText()));
    	
    	// Throttle Change % Maximum
    	Config.setThrottleChangeMaxValue(Integer.valueOf(thrtlChangeMaxFilter.getValue().toString()));
    	
    	// RPM filters
    	Config.setRPMMaximumValue(Integer.valueOf(maxRPMFilter.getText()));
    	Config.setRPMMinimumValue(Integer.valueOf(minRPMFilter.getText()));
    	
    	// Minimum Cell Hit Count Filter
		Config.setLCMinCellHitCount(Integer.valueOf(minCellHitCountFilter.getText()));
		
    	// IAT filter
    	Config.setLCIatMaximumValue(Double.valueOf(maxIatFilter.getText()));
    	
    	// AFR filters
    	Config.setLCAfrMaximumValue(Double.valueOf(maxAfrFilter.getText()));
    	Config.setLCAfrMinimumValue(Double.valueOf(minAfrFilter.getText()));
    	
    	// MP filters
    	Config.setLCMPMaximumValue(Double.valueOf(maxMPFilter.getText()));
    	Config.setLCMPMinimumValue(Double.valueOf(minMPFilter.getText()));
    	
    	// dV/dt filter
    	Config.setLCDvDtMaximumValue(Double.valueOf(maxDvdtFilter.getText()));
    	
    	// Cruise/Non-cruise Status
    	value = cruiseStatusFilter.getValue().toString();
    	if (!value.isEmpty())
    		Config.setCruiseStatusValue(Integer.valueOf(value));
    	
    	// Correction applied
    	Config.setLCCorrectionAppliedValue(Integer.valueOf(correctionAppliedValue.getValue().toString()));
    	
    	return ret;
    }
    
    protected boolean processDefaultButton(ActionEvent e) {
    	if ("thrtlchange".equals(e.getActionCommand()))
        	thrtlChangeMaxFilter.setValue(Integer.valueOf(Config.DefaultThrottleChangeMax));
        else if ("maxrpm".equals(e.getActionCommand()))
        	maxRPMFilter.setText(Config.DefaultRPMMaximum);
        else if ("minrpm".equals(e.getActionCommand()))
        	minRPMFilter.setText(Config.DefaultRPMMinimum);
        else if ("maxiat".equals(e.getActionCommand()))
        	maxIatFilter.setText(Config.DefaultLCIATMaximum);
        else if ("maxafr".equals(e.getActionCommand()))
        	maxAfrFilter.setText(Config.DefaultLCAfrMaximum);
        else if ("minafr".equals(e.getActionCommand()))
        	minAfrFilter.setText(Config.DefaultLCAfrMinimum);
        else if ("maxmp".equals(e.getActionCommand()))
        	maxMPFilter.setText(Config.DefaultMPMaximum);
        else if ("minmp".equals(e.getActionCommand()))
        	minMPFilter.setText(Config.DefaultMPMinimum);
        else if ("maxdvdt".equals(e.getActionCommand()))
        	maxDvdtFilter.setText(Config.DefaultDvDtMaximum);
        else if ("minhitcnt".equals(e.getActionCommand()))
        	minCellHitCountFilter.setText(Config.DefaultLCMinCellHitCount);
        else if ("cruisestatus".equals(e.getActionCommand()))
        	cruiseStatusFilter.setValue(Integer.valueOf(Config.DefaultCruiseStatusValue));
        else if ("corrapply".equals(e.getActionCommand()))
        	correctionAppliedValue.setValue(Integer.valueOf(Config.DefaultCorrectionAppliedValue));
        else if ("atmpress".equals(e.getActionCommand()))
        	atmPressureFilter.setText(Config.DefaultAtmPressure);
        else
        	return false;
        return true;
    }
}

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

import java.awt.event.ActionEvent;

public class CLColumnsFiltersSelection extends ColumnsFiltersSelection {
    
    protected void addColSelection() {
        addRPMColSelection();
        addLoadColSelection();
        addAFLearningColSelection();
        addAFCorrectionColSelection();
        addMAFVoltageColSelection();
        addStockAFRColSelection();
        addClOlStatusColSelection();
    	addTimeColSelection();
    	addIATColSelection();
    }
    
    protected void addFilterSelection() {
    	addCLOLStatusFilter();
    	clolStatusFilter.setValue(Config.getClOlStatusValue());
    	addMAFVoltageMaximumFilter();
        maxMafVFilter.setText(String.valueOf(Config.getMafVMaximumValue()));
    	addEngineLoadMinimumFilter();
        minEngineLoadFilter.setText(String.valueOf(Config.getLoadMinimumValue()));
        addIATMaximumFilter();
        maxIatFilter.setText(String.valueOf(Config.getIatMaximumValue()));
        addAFRMaximumFilter();
        maxAfrFilter.setText(String.valueOf(Config.getAfrMaximumValue()));
        addAFRMinimumFilter();
        minAfrFilter.setText(String.valueOf(Config.getAfrMinimumValue()));
        addDvDtMaximumFilter();
        maxDvdtFilter.setText(String.valueOf(Config.getDvDtMaximumValue()));
        addCellHitCountMinimumFilter();
    	minCellHitCountFilter.setText(String.valueOf(Config.getCLMinCellHitCount()));
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
    	
    	// Engine Load
    	value = loadName.getText().trim();
    	colName = loadLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setLoadColumnName(value);

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

    	// CL/OL Status
    	value = clolStatusName.getText().trim();
    	colName = clolStatusLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setClOlStatusColumnName(value);

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
    	    	
        // CL/OL Status
        value = clolStatusFilter.getValue().toString();
        colName = clolStatusLabelText;
        if (value.isEmpty() || value.equals("-1")) {
        	ret = false;
        	error.append("\"").append(colName).append("\" value must be specified\n");
        }
        else
        	Config.setClOlStatusValue(Integer.valueOf(value));
    	
    	// Max MAF Voltage filter
    	Config.setMafVMaximumValue(Double.valueOf(maxMafVFilter.getText()));
    	
    	// Engine Load filter
    	Config.setLoadMinimumValue(Double.valueOf(minEngineLoadFilter.getText()));
    	
    	// Minimum Cell Hit Count Filter
		Config.setCLMinCellHitCount(Integer.valueOf(minCellHitCountFilter.getText()));
		
    	// IAT filter
    	Config.setIatMaximumValue(Double.valueOf(maxIatFilter.getText()));
    	
    	// AFR filters
    	Config.setAfrMaximumValue(Double.valueOf(maxAfrFilter.getText()));
    	Config.setAfrMinimumValue(Double.valueOf(minAfrFilter.getText()));
    	
    	// dV/dt filter
    	Config.setDvDtMaximumValue(Double.valueOf(maxDvdtFilter.getText()));
    	
    	return ret;
    }
    
    protected boolean processDefaultButton(ActionEvent e) {
    	if ("maxmafv".equals(e.getActionCommand()))
        	maxMafVFilter.setText(Config.DefaultMafVMaximum);
        else if ("clolstatus".equals(e.getActionCommand()))
        	clolStatusFilter.setValue(Integer.valueOf(Config.DefaultClOlStatusValue));
        else if ("minengload".equals(e.getActionCommand()))
        	minEngineLoadFilter.setText(Config.DefaultLoadMinimum);
        else if ("maxiat".equals(e.getActionCommand()))
        	maxIatFilter.setText(Config.DefaultIATMaximum);
        else if ("maxafr".equals(e.getActionCommand()))
        	maxAfrFilter.setText(Config.DefaultAfrMaximum);
        else if ("minafr".equals(e.getActionCommand()))
        	minAfrFilter.setText(Config.DefaultAfrMinimum);
        else if ("maxdvdt".equals(e.getActionCommand()))
        	maxDvdtFilter.setText(Config.DefaultDvDtMaximum);
        else if ("minhitcnt".equals(e.getActionCommand()))
        	minCellHitCountFilter.setText(Config.DefaultCLMinCellHitCount);
        else
        	return false;
        return true;
    }
}

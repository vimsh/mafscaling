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

public class VVTColumnsFiltersSelection extends ColumnsFiltersSelection {
	
	public VVTColumnsFiltersSelection(boolean isPolfTableSet) {
		super(isPolfTableSet, false);
	}
    
    protected void addColSelection() {
        addThrottleAngleColSelection();
        addRPMColSelection();
        addVVTSystem1ColSelection();
        addVVTSystem2ColSelection();
    	addIATColSelection();
        addManifoldAbsolutePressureColSelection();
    	addMAFColSelection();
    }
    
    protected void addFilterSelection() {
        addWOTStationaryPointFilter();
        wotStationaryPointFilter.setValue(Config.getWOTStationaryPointValue());
        addOLCLTransitionSkipRowsFilter();
        olClTransitionSkipRowsField.setText(String.valueOf(Config.getOLCLTransitionSkipRows()));
        addTemperatureScaleFilter();
        temperatureScaleField.setSelectedItem(Character.toString(Config.getTemperatureScale()));
        addManifoldAbsolutePressureUnitFilter();
        mapUnitField.setSelectedItem(('P' == Config.getMapUnit() ? "Psi" : ('B' == Config.getMapUnit() ? "Bar" : "kPa")));
    }
    
    protected boolean validate(StringBuffer error) {
    	boolean ret = true;
    	String value;
    	String colName;
    	
    	// Throttle Angle
    	value = thrtlAngleName.getText().trim();
    	colName = thrtlAngleLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setThrottleAngleColumnName(value);
    	
    	// Engine Speed
    	value = rpmName.getText().trim();
    	colName = rpmLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setRpmColumnName(value);
    	
    	// VVT System 1
    	value = vvt1Name.getText().trim();
    	colName = vvt1LabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setVvt1ColumnName(value);
    	
    	// VVT System 2
    	value = vvt2Name.getText().trim();
    	colName = vvt2LabelText;
    	if (value.isEmpty())
    		Config.setVvt2ColumnName(Config.NO_NAME);
    	else
    		Config.setVvt2ColumnName(value);
    	
    	// Intake Air Temperature
    	value = iatName.getText().trim();
    	colName = iatLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setIatColumnName(value);

    	// MAP
    	value = mapName.getText().trim();
    	colName = mapLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setMapColumnName(value);

    	// MAF
    	value = mafName.getText().trim();
    	colName = mafLabelText;
    	if (value.isEmpty()) {
    		ret = false;
    		error.append("\"").append(colName).append("\" column must be specified\n");
    	}
    	else
    		Config.setMassAirflowColumnName(value);
    	
    	// WOT Stationary point
    	Config.setWOTStationaryPointValue(Integer.valueOf(wotStationaryPointFilter.getValue().toString()));
    	
    	// OL/CL Transition Skip Rows
    	Config.setOLCLTransitionSkipRows(Integer.valueOf(olClTransitionSkipRowsField.getText()));
    	
    	// Temperature Scale
    	Config.setTemperatureScale(temperatureScaleField.getSelectedItem().toString().charAt(0));
    	
    	// Manifold Absolute Pressure Unit
    	Config.setMapUnit(Character.toUpperCase(mapUnitField.getSelectedItem().toString().charAt(0)));
    	
    	return ret;
    }
    
    protected boolean processDefaultButton(ActionEvent e) {
    	if ("wotpoint".equals(e.getActionCommand()))
        	wotStationaryPointFilter.setValue(Integer.valueOf(Config.DefaultWOTStationaryPoint));
        else if ("olcltransit".equals(e.getActionCommand()))
        	olClTransitionSkipRowsField.setText(Config.DefaultOLCLTransitionSkipRows);
        else if ("tempscale".equals(e.getActionCommand()))
        	temperatureScaleField.setSelectedItem(Config.DefaultTempScale);
        else if ("mapunit".equals(e.getActionCommand()))
        	mapUnitField.setSelectedItem(Config.DefaultMapUnit);
        else
        	return false;
        return true;
    }
}

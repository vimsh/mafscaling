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

public class OLColumnsFiltersSelection extends ColumnsFiltersSelection {
    private boolean isPolfTableSet = false;
    private boolean isPolfTableMap = false;
    
    public OLColumnsFiltersSelection(boolean isPolfTableSet, boolean isPolfTableMap) {
        this.isPolfTableSet = isPolfTableSet;
        this.isPolfTableMap = isPolfTableMap;
    }
    
    protected void addColumnsNote() {
        if (!isPolfTableSet)
            addColumnsNote("If you don't have 'Commanded AFR' / 'Fueling Final Base' please set 'POL Fueling' table first");
    }
    
    protected void addColSelection() {
        addRPMColSelection();
        if (isPolfTableMap)
            addManifoldAbsolutePressureColSelection();
        else if (isPolfTableSet)
            addLoadColSelection();
        addAFLearningColSelection();
        addAFCorrectionColSelection();
        addMAFVoltageColSelection();
        addWidebandAFRColSelection();
        addThrottleAngleColSelection();
        addCommandedAFRColSelection(isPolfTableSet);
    }
    
    protected void addFilterSelection() {
        addMAFVoltageMinimumFilter();
        minMafVFilter.setText(String.valueOf(Config.getMafVMinimumValue()));
        addWOTStationaryPointFilter();
        wotStationaryPointFilter.setValue(Config.getWOTStationaryPointValue());
        addAFRErrorPctFilter();
        afrErrorFilter.setText(String.valueOf(Config.getWidebandAfrErrorPercentValue()));
        addWOTEnrichmentMinimumFilter();
        wotEnrichmentField.setText(String.valueOf(Config.getWOTEnrichmentValue()));
        addWideBandAFRRowOffsetFilter();
        wbo2RowOffsetField.setText(String.valueOf(Config.getWBO2RowOffset()));
        addOLCLTransitionSkipRowsFilter();
        olClTransitionSkipRowsField.setText(String.valueOf(Config.getOLCLTransitionSkipRows()));
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
        
        if (isPolfTableMap) {
            // Manifold Absolute Pressure
            value = mapName.getText().trim();
            colName = mapLabelText;
            if (value.isEmpty()) {
                ret = false;
                error.append("\"").append(colName).append("\" column must be specified\n");
            }
            else
                Config.setMapColumnName(value);
        }
        else if (isPolfTableSet) {
            // Engine Load
            value = loadName.getText().trim();
            colName = loadLabelText;
            if (value.isEmpty()) {
                ret = false;
                error.append("\"").append(colName).append("\" column must be specified\n");
            }
            else
                Config.setLoadColumnName(value);
        }
        
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

        // Wideband AFR
        value = wbAfrName.getText().trim();
        colName = wbAfrLabelText;
        if (value.isEmpty()) {
            ret = false;
            error.append("\"").append(colName).append("\" column must be specified\n");
        }
        else
            Config.setWidebandAfrColumnName(value);
        
        // Throttle Angle
        value = thrtlAngleName.getText().trim();
        colName = thrtlAngleLabelText;
        if (value.isEmpty()) {
            ret = false;
            error.append("\"").append(colName).append("\" column must be specified\n");
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
                error.append("\"").append(colName).append("\" column must be specified if \"Primary Open Loop Fueling\" table is not set.\n");
            }
            else
                Config.setCommandedAfrColumnName(value);
        }
        
        // Min MAF Voltage filter
        Config.setMafVMinimumValue(Double.valueOf(minMafVFilter.getText()));
        
        // WOT Stationary point
        Config.setWOTStationaryPointValue(Integer.valueOf(wotStationaryPointFilter.getValue().toString()));
        
        // Afr Error filter
        Config.setWidebandAfrErrorPercentValue(Double.valueOf(afrErrorFilter.getText()));
        
        // WOT Enrichment
        Config.setWOTEnrichmentValue(Double.valueOf(wotEnrichmentField.getText()));
        
        // WBO2 Row Offset
        Config.setWBO2RowOffset(Integer.valueOf(wbo2RowOffsetField.getText()));
        
        // OL/CL Transition Skip Rows
        Config.setOLCLTransitionSkipRows(Integer.valueOf(olClTransitionSkipRowsField.getText()));
        
        return ret;
    }
    
    protected boolean processDefaultButton(ActionEvent e) {
        if ("minmafv".equals(e.getActionCommand()))
            minMafVFilter.setText(Config.DefaultMafVMinimum);
        else if ("wotpoint".equals(e.getActionCommand()))
            wotStationaryPointFilter.setValue(Integer.valueOf(Config.DefaultWOTStationaryPoint));
        else if ("afrerr".equals(e.getActionCommand()))
            afrErrorFilter.setText(Config.DefaultWidebandAfrErrorPercent);
        else if ("wotenrich".equals(e.getActionCommand()))
            wotEnrichmentField.setText(Config.DefaultWOTEnrichment);
        else if ("wbo2offset".equals(e.getActionCommand()))
            wbo2RowOffsetField.setText(Config.DefaultWBO2RowOffset);
        else if ("olcltransit".equals(e.getActionCommand()))
            olClTransitionSkipRowsField.setText(Config.DefaultOLCLTransitionSkipRows);
        else
            return false;
        return true;
    }
}

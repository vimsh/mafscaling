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

public class MafIatColumnsFiltersSelection extends ColumnsFiltersSelection {
    private boolean isPolfTableSet = false;
    
    public MafIatColumnsFiltersSelection(boolean isPolfTableSet) {
        this.isPolfTableSet = isPolfTableSet;
    }
    
    protected void addColumnsNote() {
        if (!isPolfTableSet)
            addColumnsNote("If you don't have 'Commanded AFR' please set 'POL Fueling' table first to use 'Load'");
    }
    
    protected void addColSelection() {
        addTimeColSelection();
        addRPMColSelection();
        if (isPolfTableSet)
            addLoadColSelection();
        addThrottleAngleColSelection();
        addAFLearningColSelection();
        addAFCorrectionColSelection();
        addMAFVoltageColSelection();
        addStockAFRColSelection();
        addWidebandAFRColSelection();
        addIATColSelection();
        addMAFColSelection();
        addClOlStatusColSelection();
        addCommandedAFRColSelection(isPolfTableSet);
    }
    
    protected void addFilterSelection() {
        addMafIatInRatioFlag();
        isMafIatInRatioBool.setSelected(Config.getIsMafIatInRatio());
        addCLOLStatusFilter();
        clolStatusFilter.setValue(Config.getMIClOlStatusValue());
        addThrottleChangeMaximumFilter();
        thrtlChangeMaxFilter.setValue(Config.getMIThrottleChangeMaxValue());
        addAFRMaximumFilter();
        maxAfrFilter.setText(String.valueOf(Config.getMIAfrMaximumValue()));
        addAFRMinimumFilter();
        minAfrFilter.setText(String.valueOf(Config.getMIAfrMinimumValue()));
        addDvDtMaximumFilter();
        maxDvdtFilter.setText(String.valueOf(Config.getMIDvDtMaximumValue()));
        addWOTEnrichmentMinimumFilter();
        wotEnrichmentField.setText(String.valueOf(Config.getWOTEnrichmentValue()));
        addWideBandAFRRowOffsetFilter();
        wbo2RowOffsetField.setText(String.valueOf(Config.getWBO2RowOffset()));
        addCellHitCountMinimumFilter();
        minCellHitCountFilter.setText(String.valueOf(Config.getMIMinCellHitCount()));
        addCorrectionAppliedValue();
        correctionAppliedValue.setValue(Config.getMICorrectionAppliedValue());
        
        for (Component c : filtersPanel.getComponents()) {
            if (c instanceof JEditorPane) {
                JEditorPane label = (JEditorPane)c;
                if (label.getText().startsWith("Filter out data using logged OL/CL status"))
                    label.setText("Set Closed Loop status (EcuTek CL: 2, RR CL: 8)");
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
        
        if (isPolfTableSet) {
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
        
        // Throttle Angle
        value = thrtlAngleName.getText().trim();
        colName = thrtlAngleLabelText;
        if (value.isEmpty()) {
            ret = false;
            error.append("\"").append(colName).append("\" column must be specified\n");
        }
        else
            Config.setThrottleAngleColumnName(value);
        
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

        // Wideband AFR
        value = wbAfrName.getText().trim();
        colName = wbAfrLabelText;
        if (value.isEmpty()) {
            ret = false;
            error.append("\"").append(colName).append("\" column must be specified\n");
        }
        else
            Config.setWidebandAfrColumnName(value);

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

        // MAF
        value = mafName.getText().trim();
        colName = mafLabelText;
        if (value.isEmpty()) {
            ret = false;
            error.append("\"").append(colName).append("\" column must be specified\n");
        }
        else
            Config.setMassAirflowColumnName(value);
        
        // CL/OL Status
        value = clolStatusFilter.getValue().toString();
        colName = clolStatusLabelText;
        if (value.isEmpty() || value.equals("-1")) {
            ret = false;
            error.append("\"").append(colName).append("\" value must be specified\n");
        }
        else
            Config.setMIClOlStatusValue(Integer.valueOf(value));
        
        // MAF IAT Compensation values in ratio
        Config.setIsMafIatInRatio(isMafIatInRatioBool.isSelected());
        
        // Throttle Change % Maximum
        Config.setMIThrottleChangeMaxValue(Integer.valueOf(thrtlChangeMaxFilter.getValue().toString()));
        
        // Minimum Cell Hit Count Filter
        Config.setMIMinCellHitCount(Integer.valueOf(minCellHitCountFilter.getText()));
        
        // AFR filters
        Config.setMIAfrMaximumValue(Double.valueOf(maxAfrFilter.getText()));
        Config.setMIAfrMinimumValue(Double.valueOf(minAfrFilter.getText()));
        
        // dV/dt filter
        Config.setMIDvDtMaximumValue(Double.valueOf(maxDvdtFilter.getText()));
        
        // WOT Enrichment
        Config.setWOTEnrichmentValue(Double.valueOf(wotEnrichmentField.getText()));
        
        // WBO2 Row Offset
        Config.setWBO2RowOffset(Integer.valueOf(wbo2RowOffsetField.getText()));
        
        // Correction applied
        Config.setMICorrectionAppliedValue(Integer.valueOf(correctionAppliedValue.getValue().toString()));
        
        return ret;
    }
    
    protected boolean processDefaultButton(ActionEvent e) {
        if ("thrtlchange".equals(e.getActionCommand()))
            thrtlChangeMaxFilter.setValue(Integer.valueOf(Config.DefaultThrottleChangeMax));
        else if ("maxafr".equals(e.getActionCommand()))
            maxAfrFilter.setText(Config.DefaultMIAfrMaximum);
        else if ("minafr".equals(e.getActionCommand()))
            minAfrFilter.setText(Config.DefaultMIAfrMinimum);
        else if ("maxdvdt".equals(e.getActionCommand()))
            maxDvdtFilter.setText(Config.DefaultDvDtMaximum);
        else if ("minhitcnt".equals(e.getActionCommand()))
            minCellHitCountFilter.setText(Config.DefaultMIMinCellHitCount);
        else if ("wotenrich".equals(e.getActionCommand()))
            wotEnrichmentField.setText(Config.DefaultWOTEnrichment);
        else if ("wbo2offset".equals(e.getActionCommand()))
            wbo2RowOffsetField.setText(Config.DefaultWBO2RowOffset);
        else if ("corrapply".equals(e.getActionCommand()))
            correctionAppliedValue.setValue(Integer.valueOf(Config.DefaultCorrectionAppliedValue));
        else
            return false;
        return true;
    }
}

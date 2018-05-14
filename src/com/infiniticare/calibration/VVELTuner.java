package com.infiniticare.calibration;

import java.util.ArrayList;
import java.util.Map;

import com.infiniticare.model.Log;

public final class VVELTuner extends AirflowTuner {

	@Override
	public void initializeAirflowMaps() {
		ecutekMapCold = tuningInput.getVVELMapCold();
		ecutekMapHot = tuningInput.getVVELMapHot();
		ecutekMapColdCorrectionFactor = tuningInput.getVvelCompensationCorrectionFactorCold();
		ecutekMapHotCorrectionFactor = tuningInput.getVvelCompensationCorrectionFactorHot();
	}
		

	@Override
	protected ArrayList<Double> getAirflowIndexYs(Log log) throws IllegalArgumentException, IllegalAccessException {
		Map<String, String> attributeMap = tuningInput.getAttributeMap();
		String attributeLabel = attributeMap.get(TuningInput.VVEL_VALVE_DURATION_CAM_ANGLE);
		return log.getLoggedAttribute(attributeLabel);
	}
}

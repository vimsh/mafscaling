package com.infiniticare.calibration;

import java.util.ArrayList;
import java.util.Map;

import com.infiniticare.model.Log;

/**
 * Represents a relationship between Manifold Pressure and VVEL.
 * @author Eugene Turkov
 *
 */
public class MAPVVELTuner extends AirflowTuner {

	@Override
	public void initializeAirflowMaps() throws NumberFormatException, IllegalArgumentException, IllegalAccessException {
		ecutekMapCold = tuningInput.getMAPVVELMapCold();
		ecutekMapHot = tuningInput.getMAPVVELMapHot();
		ecutekMapColdCorrectionFactor = tuningInput.getMAPVVELMapColdCorrectionFactor();
		ecutekMapHotCorrectionFactor = tuningInput.getMAPVVELMapHotCorrectionFactor();
		
	}

	@Override
	protected ArrayList<Double> getAirflowIndexYs(Log log) throws IllegalArgumentException, IllegalAccessException {
		Map<String, String> attributeMap = tuningInput.getAttributeMap();
		String attributeLabel = attributeMap.get(TuningInput.VVEL_VALVE_DURATION_CAM_ANGLE);
		return log.getLoggedAttribute(attributeLabel);
	}

	@Override
	protected ArrayList<Double> getAirflowIndexXs(Log log) throws IllegalArgumentException, IllegalAccessException {
		Map<String, String> attributeMap = tuningInput.getAttributeMap();
		String attributeLabel = attributeMap.get(TuningInput.MANIFOLD_PRESSURE);
		return log.getLoggedAttribute(attributeLabel);
	}
}

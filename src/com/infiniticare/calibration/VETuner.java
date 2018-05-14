package com.infiniticare.calibration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.infiniticare.model.Log;

public class VETuner extends AirflowTuner {

	@Override
	public void initializeAirflowMaps() {
		ecutekMapCold = tuningInput.getVEMapCold();
		ecutekMapHot = tuningInput.getVEMapHot();
		ecutekMapColdCorrectionFactor = tuningInput.getVeCompensationCorrectionFactorCold();
		ecutekMapHotCorrectionFactor = tuningInput.getVeCompensationCorrectionFactorHot();
	}

	@Override
	protected ArrayList<Double> getAirflowIndexYs(Log log)
			throws IllegalArgumentException, IllegalAccessException {
		Map<String, String> attributeMap = tuningInput.getAttributeMap();
		String attributeLabel = attributeMap.get(TuningInput.MANIFOLD_PRESSURE);
		return log.getLoggedAttribute(attributeLabel);
	}
	
	@Override
	/**
	 * Write tune data to files as necessary.
	 */
	public void saveTuningResults() throws IOException {
		ecutekMapCold.save(ecutekMapCold.getFile().getAbsolutePath() + ".new"
				, tuningInput.getMinimumHitCountRequiredToSuggestChanges(), false, ecutekMapColdCorrectionFactor, false);
		ecutekMapHot.save(ecutekMapHot.getFile().getAbsolutePath() + ".new"
				, tuningInput.getMinimumHitCountRequiredToSuggestChanges(), false, ecutekMapHotCorrectionFactor, false);
	}	
}

package com.infiniticare.calibration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.infiniticare.model.KnockMember;
import com.infiniticare.model.Log;
import com.infiniticare.model.ecutek.TimingMap;

/**
 * Perform tuning of ignition timing maps.
 * @author Eugene Turkov
 *
 */
public class TimingTuner extends Tuner {
	private ArrayList<TimingMap> timingMaps;
	private ArrayList<TimingMap> timingMasks;
	Map<String, String> attributeMap;
	
	@Override
	public void tune(TuningInput tuningInput) throws IOException, IllegalArgumentException, IllegalAccessException {
		super.tune(tuningInput);
		initializeTimingMaps();
		
		double hotColdTransitionTemp = tuningInput.getHotColdTransitionTemp();
		ArrayList<Log> logs = tuningInput.getLogs();
		if(logs == null || logs.isEmpty()) {
			throw new UnsupportedOperationException("No logs found, unable to perform tuning operations.");
		}
		if(attributeMap == null) {
			attributeMap = tuningInput.getAttributeMap();
		}
		for (Log log : logs) {
			ArrayList<Double> times = log.getLoggedAttribute(attributeMap.get(TuningInput.TIME));
			ArrayList<Double> coolantTemps = log.getLoggedAttribute(attributeMap.get(TuningInput.ECT));
			ArrayList<Double> afrB1s = log.getLoggedAttribute(attributeMap.get(TuningInput.AFR_BANK1));
			ArrayList<Double> afrB2s = log.getLoggedAttribute(attributeMap.get(TuningInput.AFR_BANK2));
			ArrayList<Double> afrTargets = log.getLoggedAttribute(attributeMap.get(TuningInput.AFR_AVG));
			ArrayList<Double> olfts = log.getLoggedAttribute(attributeMap.get(TuningInput.OLFT));
			ArrayList<Double> engineLoads = log.getLoggedAttribute(attributeMap.get(TuningInput.ENGINE_LOAD_PERCENT));
			ArrayList<Double> rpms = log.getLoggedAttribute(attributeMap.get(TuningInput.RPM));

			for (int i = 0; i < times.size(); i++) {
				double coolantTemp = coolantTemps.get(i);
				if (coolantTemp > hotColdTransitionTemp
						&& bothBanksAFRsWithinRange(afrB1s.get(i), afrB2s.get(i), afrTargets.get(i))
						&& olfts.get(i) > 0.96 && olfts.get(i) < 1.04) {
					double engineLoad = engineLoads.get(i);
					double rpm = rpms.get(i);
					KnockMember knockMember = getKnockMember(log, i);
					double averageKnockDistance = knockMember.getAverageDistanceFromThreshold();
					// adjust timing based on engine noise. Set a threshold where timing should be pulled.
					/*
					if(averageKnockDistance > tuningInput.getMaxAllowedKnockThresholdDistance()) {
						removeTiming(Log log);
					} else if(averageKnockDistance < tuningInput.getMaxAllowedKnockThresholdDistance()) {
						addTiming(Log log); // do not add more than the calculated ignition timing by the ECU.
					} else {
						// do nothing
					}*/
					
				}
			}
		}
		
		// step 1, correct any locations where the maximum of each cyl knock sensor/threshold is 0.2 or higher, 
		// subtract 1 degree. Subtract more if knock correction was present
		// if signal strength is 0.05 or less, add one degree but do not exceed the calculated timing. Also, if
		// timing has been pulled here before, do not add any, use the timing mask for this. Compute the timing mask as a combination 
		
	}

	private void addTiming(Log log) {
		//ArrayList<Double> ignitionTimingsCalculated = log.getLoggedAttribute(attributeMap.get(TuningInput.IGNITION_TIMING_CALCULATED));
		//ArrayList<Double> ignitionTimings = log.getLoggedAttribute(attributeMap.get(TuningInput.IGNITION_TIMING));	
	}

	private void removeTiming(Log log) {
		// TODO Auto-generated method stub
		
	}

	private KnockMember getKnockMember(Log log, int i) {
		String knockSensorName = TuningInput.KNOCK_SENSOR_CYLINDER_1.substring(0, TuningInput.KNOCK_SENSOR_CYLINDER_1.length() - 1);
		String knockThresholdName = TuningInput.KNOCK_THRESHOLD_CYLINDER_1.substring(0, TuningInput.KNOCK_THRESHOLD_CYLINDER_1.length() - 1);

		double loudestKnockValueSoFar = -1;
		double thresholdForLoudestCylinderSoFar = -1;
		double knockSumSoFar = 0;
		double knockThresholdSoFar = 0;
		
		for(int j = 1; j < 7; j++) {
			double knockValue = log.getLoggedAttribute(attributeMap.get(knockSensorName + j)).get(i);
			double knockThresholdValue = log.getLoggedAttribute(attributeMap.get(knockThresholdName + j)).get(i);
			knockSumSoFar += knockValue;
			knockThresholdSoFar += knockThresholdValue;
			
			if(knockValue > loudestKnockValueSoFar) {
				loudestKnockValueSoFar = knockValue;
				thresholdForLoudestCylinderSoFar = knockThresholdValue;
			}
		}
		
		KnockMember knockMember = new KnockMember();
		knockMember.setLoudestKnockValue(loudestKnockValueSoFar);
		knockMember.setThresholdForLoudestCylinder(thresholdForLoudestCylinderSoFar);
		knockMember.setAverageKnockValue(knockSumSoFar/tuningInput.getEngineCylinderCount());
		knockMember.setAverageKnockThresholdValue(knockThresholdSoFar/tuningInput.getEngineCylinderCount());
		return new KnockMember();
	}

	private boolean bothBanksAFRsWithinRange(Double afrB1, Double afrB2, Double afrTarget) {
		if(afrB1/afrTarget > 0.96 && afrB1/afrTarget < 1.04 && afrB2/afrTarget > 0.96 && afrB2/afrTarget < 1.04) {
			return true;
		}
		return false;
	}

	private void initializeTimingMaps() {
		timingMaps = tuningInput.getTimingMaps();
		timingMasks = tuningInput.getTimingMasks();
	}

	@Override
	public void saveTuningResults() {
		// TODO Auto-generated method stub
		
	}
}

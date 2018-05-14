package com.infiniticare.calibration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.infiniticare.model.Cell;
import com.infiniticare.model.Log;
import com.infiniticare.model.ecutek.EcutekMap;


/**
 * @author Eugene Turkov
 * Airflow tuning is related to calibration of volumetric efficiency map(s) and related compensations.
 */
public abstract class AirflowTuner extends Tuner {
	protected EcutekMap ecutekMapCold;
	protected EcutekMap ecutekMapHot;
	protected double ecutekMapColdCorrectionFactor;
	protected double ecutekMapHotCorrectionFactor;

	@Override
	/**
	 * Perform tuning based on a given tuning input.
	 */
	public void tune(TuningInput tuningInput) throws IOException, IllegalArgumentException, IllegalAccessException {
		super.tune(tuningInput);
		initializeAirflowMaps();
		this.tune(true);
		this.tune(false);
	}

	public abstract void initializeAirflowMaps() throws NumberFormatException, IllegalArgumentException, IllegalAccessException;

	/**
	 * Perform tuning.
	 * @param tuneCold If true, perform tuning cold side of temperature threshold, otherwise perform tuning for
	 * hot side.
	 */
	private void tune(boolean tuneCold) throws IllegalArgumentException, IllegalAccessException {
		EcutekMap ecutekMap = null;
		if(tuneCold) {
			ecutekMap = ecutekMapCold;
		} else {
			ecutekMap = ecutekMapHot;
		}
		double hotColdTransitionTemp = tuningInput.getHotColdTransitionTemp();
		ArrayList<Log> logs = tuningInput.getLogs();
		if(logs == null || logs.isEmpty()) {
			throw new UnsupportedOperationException("No logs found, unable to perform tuning operations.");
		}
		Map<String, String> attributeMap = tuningInput.getAttributeMap();
		for (Log log : logs) {
			ArrayList<Double> times = log.getLoggedAttribute(attributeMap.get(TuningInput.TIME));
			ArrayList<Double> coolantTemps = log.getLoggedAttribute(attributeMap.get(TuningInput.ECT));
			ArrayList<Double> speedDensities = log
					.getLoggedAttribute(attributeMap.get(TuningInput.SPEED_DENSITY_STATE));
			ArrayList<Double> ltftB1s = log.getLoggedAttribute(attributeMap.get(TuningInput.LTFT_B1));
			ArrayList<Double> ltftB2s = log.getLoggedAttribute(attributeMap.get(TuningInput.LTFT_B2));
			ArrayList<Double> stftB1s = log.getLoggedAttribute(attributeMap.get(TuningInput.STFT_B1));
			ArrayList<Double> stftB2s = log.getLoggedAttribute(attributeMap.get(TuningInput.STFT_B2));
			ArrayList<Double> olfts = log.getLoggedAttribute(attributeMap.get(TuningInput.OLFT));
			ArrayList<Double> airflowIndexYs = getAirflowIndexYs(log);
			ArrayList<Double> airflowIndexXs = getAirflowIndexXs(log);

			for (int i = 0; i < times.size(); i++) {
				double coolantTemp = coolantTemps.get(i);
				boolean speedDensityActive = speedDensities.get(i) == 1 ? true : false;
				if (validCoolantTemp(hotColdTransitionTemp, coolantTemp,tuneCold) && speedDensityActive) {
					double ltftAvg = ((ltftB1s.get(i) + ltftB2s.get(i)) / 2.0) / 100.0;
					double airflowIndexColumn = airflowIndexYs.get(i);
					double stftAvg = ((stftB1s.get(i) + stftB2s.get(i)) / 2.0) / 100.0;
					double olft = olfts.get(i);
					double correctionNeeded = (ltftAvg + stftAvg + olft) - 2;
					double airflowIndexRow = airflowIndexXs.get(i);

					if (olft != 1.0 || !anyChangesInTheLastSeconds(0.01, i)) {
						Cell cell = ecutekMap.getCell(airflowIndexRow, airflowIndexColumn);
						if (cell != null && correctionNeeded != 1.0) {
							double originalValue = cell.getPopulation().get(0);
							double newVeValue = originalValue + ((correctionNeeded * 100.0) - 100.0);
							cell.getPopulation().add(newVeValue);
						}
					}
				}
			}
		}
	}

	/**
	 * Get the log attribute which represents the X/Column input into the table as an index. Used to located the
	 * necessary table cell.
	 * @param log the log containing the data.
	 * @return a list of elements from the log that represent indexing information for the table.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	protected ArrayList<Double> getAirflowIndexXs(Log log) throws IllegalArgumentException, IllegalAccessException {
		Map<String, String> attributeMap = tuningInput.getAttributeMap();
		String attributeLabel = attributeMap.get(TuningInput.RPM);
		return log.getLoggedAttribute(attributeLabel);
	}
	
	/**
	 * Get the log attribute which represents the Y/Row input into the table as an index. Used to located the
	 * necessary table cell.
	 * @param log the log containing the data.
	 * @return a list of elements from the log that represent indexing information for the table.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	protected abstract ArrayList<Double> getAirflowIndexYs(Log log) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * Determine if provided coolant temp falls within the desired range.
	 * @param hotColdTransitionTemp the temperature up to which we consider cold operation and after which we consider hot operation.
	 * @param coolantTemp actual coolant temp for evaluation
	 * @param tuneCold indicates if we are doing cold or hot tuning.
	 * @return true if the temperature falls within the specified range.
	 */
	private boolean validCoolantTemp(double hotColdTransitionTemp, double coolantTemp, boolean tuneCold) {
		if(tuneCold) {
			return coolantTemp < hotColdTransitionTemp;
		} else {
			return coolantTemp > hotColdTransitionTemp;
		}
	}

	/**
	 * Handle transient conditions here.
	 * @param secondsToLookBack
	 * @param i the current data log index.
	 * @return true if any changes occurred, false otherwise.
	 */
	private boolean anyChangesInTheLastSeconds(double secondsToLookBack, int i) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	/**
	 * Write tune data to files as necessary.
	 */
	public void saveTuningResults() throws IOException {
		ecutekMapCold.save(ecutekMapCold.getFile().getAbsolutePath() + ".new"
				, tuningInput.getMinimumHitCountRequiredToSuggestChanges(), false, ecutekMapColdCorrectionFactor, true);
		ecutekMapHot.save(ecutekMapHot.getFile().getAbsolutePath() + ".new"
				, tuningInput.getMinimumHitCountRequiredToSuggestChanges(), false, ecutekMapHotCorrectionFactor, false);
	}
}

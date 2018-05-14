package com.infiniticare.calibration;

import java.io.IOException;
import java.util.ArrayList;

/**
 * TunerManager is a class used to organize all tuners and relay commands to/from them in an organized fashion.
 * @author Eugene Turkov
 *
 */
public class TunerManager {
	private ArrayList<Tuner> tuners = new ArrayList<Tuner>();
	private TuningInput tuningInput;
	
	public TunerManager(TuningInput tuningInput) {
		this.tuningInput = tuningInput;
		initializeTuners();
	}

	private void initializeTuners() {
		//tuners.add(new TimingTuner());
		tuners.add(new VVELTuner());
		tuners.add(new VETuner());
		//tuners.add(new MAPVVELTuner());
	}

	public void saveTuningResults() throws IOException {
		for (Tuner tuner : tuners) {
			tuner.saveTuningResults();
		}
	}

	public void tune() throws IOException, IllegalArgumentException, IllegalAccessException {
		for (Tuner tuner : tuners) {
			tuner.tune(tuningInput);
		}
	}
}

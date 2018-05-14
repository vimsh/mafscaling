package com.infiniticare.calibration;

import java.io.IOException;

/**
 * Generic tuner to represent all tuner classes. Supports tune function and saving.
 * @author Eugene
 *
 */
public abstract class Tuner {
	
	protected TuningInput tuningInput;
	
	public void tune(TuningInput tuningInput) throws IOException, IllegalArgumentException, IllegalAccessException {
		this.tuningInput = tuningInput;
	}
	public abstract void saveTuningResults() throws IOException;
}

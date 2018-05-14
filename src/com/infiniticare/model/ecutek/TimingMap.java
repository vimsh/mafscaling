package com.infiniticare.model.ecutek;

import java.io.File;

import com.infiniticare.model.Cell;

/**
 * This class represents a timing map as visualized by Ecutek Racerom.
 * @author Eugene Turkov
 *
 */
public class TimingMap extends EcutekMap {
	public TimingMap(File file) {
		super(file);
	}
	
	public Cell getTiming(double engineSpeed, double engineLoad) {
		return super.getCell(engineSpeed, engineLoad);
	}
	
}

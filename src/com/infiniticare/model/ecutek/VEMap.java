package com.infiniticare.model.ecutek;

import java.io.File;

import com.infiniticare.model.Cell;

/**
 * This class represents a Volumetric Efficiency map as represented by Ecutek racerom.
 * @author Eugene Turkov
 *
 */
public class VEMap extends EcutekMap {
	public VEMap(File file) {
		super(file);
	}
	
	public VEMap(File file, boolean b) {
		super(file, b);
	}

	public Cell getCell(double engineSpeed, double manifoldPressure) {
		return super.getCell(engineSpeed, manifoldPressure);
	}
	
}

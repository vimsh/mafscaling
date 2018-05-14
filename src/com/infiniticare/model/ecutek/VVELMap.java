package com.infiniticare.model.ecutek;

import java.io.File;

import com.infiniticare.model.Cell;

/**
 * This class represents the Ecutek Racerom construct of the VVEL Compensation Map
 * @author Eugene Turkov
 *
 */
public class VVELMap extends EcutekMap {
	public VVELMap(File file) {
		super(file);
	}
	
	public VVELMap(File file, boolean b) {
		super(file, b);
	}

	public Cell getCell(double engineSpeed, double vvelCamAngle) {
		return super.getCell(engineSpeed, vvelCamAngle);
	}
}

package com.infiniticare.model.ecutek;

import java.io.File;

import com.infiniticare.model.Cell;

/**
 * Represents a relationship between Manifold Pressure and VVEL. Custom map.
 * @author Eugene Turkov
 *
 */
public class MAPVVELMap extends EcutekMap {
	public MAPVVELMap(File file) {
		super(file);
	}
	
	public MAPVVELMap(File file, boolean b) {
		super(file, b);
	}

	public Cell getCell(double manifoldPressure, double vvelCamAngle) {
		return super.getCell(manifoldPressure, vvelCamAngle);
	}
}

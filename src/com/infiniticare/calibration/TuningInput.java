package com.infiniticare.calibration;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.infiniticare.model.Log;
import com.infiniticare.model.ecutek.MAPVVELMap;
import com.infiniticare.model.ecutek.TimingMap;
import com.infiniticare.model.ecutek.VEMap;
import com.infiniticare.model.ecutek.VVELMap;

public class TuningInput {
	public final static String LOG_FILES_LOCATION = "LOG_FILES_LOCATION";
	public final static String VE_MAP_HOT = "VE_MAP_HOT";
	public final static String VE_MAP_COLD = "VE_MAP_COLD";
	public final static String VVEL_MAP_HOT = "VVEL_MAP_HOT";
	public final static String VVEL_MAP_COLD = "VVEL_MAP_COLD";
	public final static String MAP_VVEL_COMPENSATION_COLD = "MAP_VVEL_COMPENSATION_COLD";
	public final static String MAP_VVEL_COMPENSATION_HOT = "MAP_VVEL_COMPENSATION_HOT";
	public final static String VVEL_COMPENSATION_CORRECTION_FACTOR_COLD = "VVEL_COMPENSATION_CORRECTION_FACTOR_COLD";
	public final static String VVEL_COMPENSATION_CORRECTION_FACTOR_HOT = "VVEL_COMPENSATION_CORRECTION_FACTOR_HOT";
	public final static String VE_COMPENSATION_CORRECTION_FACTOR_COLD = "VE_COMPENSATION_CORRECTION_FACTOR_COLD";
	public final static String VE_COMPENSATION_CORRECTION_FACTOR_HOT = "VE_COMPENSATION_CORRECTION_FACTOR_HOT";
	public final static String MAP_VVEL_CORRECTION_FACTOR_COLD = "MAP_VVEL_CORRECTION_FACTOR_COLD";
	public final static String MAP_VVEL_CORRECTION_FACTOR_HOT = "MAP_VVEL_CORRECTION_FACTOR_HOT";
	public final static String TIMING_MAP = "TIMING_MAP";
	public final static String TIMING_CORRECTION_THRESHOLD = "TIMING_CORRECTION_THRESHOLD";
	public final static String TRANSIET_LOOKBACK_TIME = "TRANSIENT_LOOKBACK_TIME";
	public final static String OLFT = "OLFT";
	public final static String RPM = "RPM";
	public final static String ECT = "ECT";
	public final static String STFT_B1 = "STFT_B1";
	public final static String STFT_B2 = "STFT_B2";
	public final static String LTFT_B1 = "LTFT_B1";
	public final static String LTFT_B2 = "LTFT_B2";
	public final static String IAT = "IAT";
	public final static String VVEL_VALVE_DURATION_CAM_ANGLE = "VVEL_VALVE_DURATION_CAM_ANGLE";
	public final static String TIME = "TIME";
	public final static String THROTTLE_POSITION = "THROTTLE_POSITION";
	public final static String ACCEL_PEDAL_POSITION = "ACCEL_PEDAL_POSITION";
	public final static String SPEED_DENSITY_STATE = "SPEED_DENSITY_STATE";
	public final static String ENGINE_LOAD_PERCENT = "ENGINE_LOAD_PERCENT";
	public final static String AFR_AVG = "AFR_AVG";
	public final static String AFR_TARGET = "AFR_TARGET";
	public final static String IGNITION_CORRECTION = "IGNITION_CORRECTION";	
	public final static String HOT_COLD_TRANSITION_TEMP = "HOT_COLD_TRANSITION_TEMP";
	public final static String MANIFOLD_PRESSURE = "MANIFOLD_PRESSURE";
	public final static String MINIMUM_HIT_COUNT_REQUIRED_TO_SUGGEST_CHANGES = "MINIMUM_HIT_COUNT_REQUIRED_TO_SUGGEST_CHANGES";
	
	private File logFilesLocation;
	private Properties properties;
	private ArrayList<Log> logs;
	private VEMap veMapHot;
	private VEMap veMapCold;
	private VVELMap vvelMapHot;
	private VVELMap vvelMapCold;
	private MAPVVELMap mapVVELMapHot;
	private MAPVVELMap mapVVELMapCold;
	private TimingMap timingMap;
	
	private Double vvelCompensationCorrectionFactorCold;
	private Double vvelCompensationCorrectionFactorHot;
	private Double veCompensationCorrectionFactorCold;
	private Double veCompensationCorrectionFactorHot;
	private Double mapVVELMapColdCompensationCorrectionFactor;
	private Double mapVVELMapHotCompensationCorrectionFactor;
	private Double timingCorrectionThreshold;
	private Double transientLookbackTime;
	private Double hotColdTransitionTemp;
	private HashMap<String,String> attributeMap;
	private Integer minimumHitCountRequiredToSuggestChanges;
	
	public TuningInput() {
	}	
	
	public TuningInput(File propertiesFile) throws IOException, IllegalArgumentException, IllegalAccessException {
		properties = new Properties();
		FileInputStream fileInputStream = new FileInputStream(propertiesFile);
		properties.load(fileInputStream);
		fileInputStream.close();
		getAttributeMap();
		loadLogs();
	}
	
	private void loadLogs() throws IOException {
		logFilesLocation = new File(properties.getProperty(LOG_FILES_LOCATION));
		logs = new ArrayList<Log>();
		File[] logFiles = logFilesLocation.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if(file.isFile() && file.getName().toLowerCase().endsWith("csv")) {
					return true;
				}
				return false;
			}
		});
		
		for (File file : logFiles) {
			Log log = new Log(file);
			logs.add(log);
		}
	}
	
	public Map<String, String> getAttributeMap() throws IllegalArgumentException, IllegalAccessException {
		if(attributeMap == null) {
			 attributeMap = new HashMap<String,String>();
			 Field[] declaredFields = getClass().getDeclaredFields();
			 for (Field field : declaredFields) {
				field.setAccessible(true);
				if(Modifier.isStatic(field.getModifiers())) {
					String fieldValue = field.get(this).toString();
					attributeMap.put(fieldValue, properties.getProperty(fieldValue));
				}
			}
		}
		return attributeMap;
	}
	
	public Double getVvelCompensationCorrectionFactorCold() {
		if(vvelCompensationCorrectionFactorCold == null) {
			String string = attributeMap.get(VVEL_COMPENSATION_CORRECTION_FACTOR_COLD);
			vvelCompensationCorrectionFactorCold = Double.valueOf(string);
		}
		return vvelCompensationCorrectionFactorCold;
	}

	public void setVvelCompensationCorrectionFactorCold(Double vvelCompensationCorrectionFactorCold) {
		this.vvelCompensationCorrectionFactorCold = vvelCompensationCorrectionFactorCold;
	}

	public Double getVvelCompensationCorrectionFactorHot() {
		if(vvelCompensationCorrectionFactorHot == null) {
			vvelCompensationCorrectionFactorHot = Double.valueOf(attributeMap.get(VVEL_COMPENSATION_CORRECTION_FACTOR_HOT));
		}
		return vvelCompensationCorrectionFactorHot;
	}

	public void setVvelCompensationCorrectionFactorHot(Double vvelCompensationCorrectionFactorHot) {
		this.vvelCompensationCorrectionFactorHot = vvelCompensationCorrectionFactorHot;
	}

	public Double getVeCompensationCorrectionFactorCold() {
		if(veCompensationCorrectionFactorCold == null) {
			veCompensationCorrectionFactorCold = Double.valueOf(attributeMap.get(VE_COMPENSATION_CORRECTION_FACTOR_COLD));
		}
		return vvelCompensationCorrectionFactorCold;
	}

	public void setVeCompensationCorrectionFactorCold(Double veCompensationCorrectionFactorCold) {
		this.veCompensationCorrectionFactorCold = veCompensationCorrectionFactorCold;
	}

	public Double getVeCompensationCorrectionFactorHot() {
		if(veCompensationCorrectionFactorHot == null) {
			veCompensationCorrectionFactorHot = Double.valueOf(attributeMap.get(VE_COMPENSATION_CORRECTION_FACTOR_HOT));
		}
		return vvelCompensationCorrectionFactorHot;
	}

	public void setVeCompensationCorrectionFactorHot(Double veCompensationCorrectionFactorHot) {
		this.veCompensationCorrectionFactorHot = veCompensationCorrectionFactorHot;
	}	
	
	public Double getHotColdTransitionTemp() {
		if(hotColdTransitionTemp == null) {
			hotColdTransitionTemp = Double.valueOf(properties.getProperty(HOT_COLD_TRANSITION_TEMP));
		}
		return hotColdTransitionTemp;
	}

	public VVELMap getVVELMapCold() {
		if(vvelMapCold == null) {
			String vvelMapColdFileName = properties.getProperty(VVEL_MAP_COLD);
			File file = new File(logFilesLocation + File.separator + vvelMapColdFileName);
			vvelMapCold = new VVELMap(file, true);
		}
		return vvelMapCold;
	}

	public VVELMap getVVELMapHot() {
		if(vvelMapHot == null) {
			String vvelMapHotFileName = properties.getProperty(VVEL_MAP_HOT);
			File file = new File(logFilesLocation + File.separator + vvelMapHotFileName);
			vvelMapHot = new VVELMap(file);
		}
		return vvelMapHot;
	}

	public File getLogFilesLocation() {
		return logFilesLocation;
	}

	public void setLogFilesLocation(File logFilesLocation) {
		this.logFilesLocation = logFilesLocation;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public ArrayList<Log> getLogs() {
		return logs;
	}

	public void setLogs(ArrayList<Log> logs) {
		this.logs = logs;
	}

	public VEMap getVEMapHot() {
		if(veMapHot == null) {
			String veMapHotFileName = properties.getProperty(VE_MAP_HOT);
			File file = new File(logFilesLocation + File.separator + veMapHotFileName);
			veMapHot = new VEMap(file);
		}
		return veMapHot;
	}

	public void setVeMapHot(VEMap veMapHot) {
		this.veMapHot = veMapHot;
	}

	public VEMap getVEMapCold() {
		if(veMapCold == null) {
			String veMapColdFileName = properties.getProperty(VE_MAP_COLD);
			File file = new File(logFilesLocation + File.separator + veMapColdFileName);
			veMapCold = new VEMap(file);
		}
		return veMapCold;
	}

	public void setVeMapCold(VEMap veMapCold) {
		this.veMapCold = veMapCold;
	}

	public void setVVELMapHot(VVELMap vvelMapHot) {
		this.vvelMapHot = vvelMapHot;
	}

	public void setVvelMapCold(VVELMap vvelMapCold) {
		this.vvelMapCold = vvelMapCold;
	}

	public MAPVVELMap getMAPVVELMapHot() {
		if(mapVVELMapHot == null) {
			String mapVVELMapHotFileName = properties.getProperty(MAP_VVEL_COMPENSATION_HOT);
			File file = new File(logFilesLocation + File.separator + mapVVELMapHotFileName);
			mapVVELMapHot = new MAPVVELMap(file, true);
		}
		return mapVVELMapHot;
	}

	public void setMapVVELMapHot(MAPVVELMap mapVVELMapHot) {
		this.mapVVELMapHot = mapVVELMapHot;
	}

	public MAPVVELMap getMAPVVELMapCold() {
		if(mapVVELMapCold == null) {
			String mapVVELMapColdFileName = properties.getProperty(MAP_VVEL_COMPENSATION_COLD);
			File file = new File(logFilesLocation + File.separator + mapVVELMapColdFileName);
			mapVVELMapCold = new MAPVVELMap(file, true);
		}
		return mapVVELMapCold;
	}

	public void setMapVVELMapCold(MAPVVELMap mapVVELMapCold) {
		this.mapVVELMapCold = mapVVELMapCold;
	}

	public TimingMap getTimingMap() {
		return timingMap;
	}

	public void setTimingMap(TimingMap timingMap) {
		this.timingMap = timingMap;
	}

	public Double getTimingCorrectionThreshold() {
		return timingCorrectionThreshold;
	}

	public void setTimingCorrectionThreshold(Double timingCorrectionThreshold) {
		this.timingCorrectionThreshold = timingCorrectionThreshold;
	}

	public Double getTransientLookbackTime() {
		if(transientLookbackTime == null) {
			transientLookbackTime = Double.valueOf(attributeMap.get(TRANSIET_LOOKBACK_TIME));
		}
		return transientLookbackTime;
	}

	public void setTransientLookbackTime(Double transientLookbackTime) {
		this.transientLookbackTime = transientLookbackTime;
	}

	public Integer getMinimumHitCountRequiredToSuggestChanges() {
		if(minimumHitCountRequiredToSuggestChanges == null) {
			minimumHitCountRequiredToSuggestChanges = Integer.valueOf(properties.getProperty(MINIMUM_HIT_COUNT_REQUIRED_TO_SUGGEST_CHANGES));
		}
		return minimumHitCountRequiredToSuggestChanges;
	}

	public void setMinimumHitCountRequiredToSuggestChanges(Integer minimumHitCountRequiredToSuggestChanges) {
		this.minimumHitCountRequiredToSuggestChanges = minimumHitCountRequiredToSuggestChanges;
	}

	public double getMAPVVELMapColdCorrectionFactor() throws NumberFormatException, IllegalArgumentException, IllegalAccessException {
		if(mapVVELMapColdCompensationCorrectionFactor == null) {
			mapVVELMapColdCompensationCorrectionFactor = Double.valueOf(getAttributeMap().get(MAP_VVEL_CORRECTION_FACTOR_COLD));
		}
		
		return mapVVELMapColdCompensationCorrectionFactor;
	}
	
	public double getMAPVVELMapHotCorrectionFactor() throws NumberFormatException, IllegalArgumentException, IllegalAccessException {
		if(mapVVELMapHotCompensationCorrectionFactor == null) {
			mapVVELMapHotCompensationCorrectionFactor = Double.valueOf(getAttributeMap().get(MAP_VVEL_CORRECTION_FACTOR_HOT));
		}
		
		return mapVVELMapHotCompensationCorrectionFactor;
	}
}

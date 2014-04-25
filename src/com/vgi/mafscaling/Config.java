package com.vgi.mafscaling;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Config {
    private static final Logger logger = Logger.getLogger(Config.class);
	private static final String CFG_FILE = "config.xml";
	private static Properties props = new Properties();
	
	public static Dimension getWindowSize() {
		return new Dimension(Integer.parseInt(props.getProperty("WindowWidth", "300")), Integer.parseInt(props.getProperty("WindowHeight", "200")));
	}
	
	public static void setWindowSize(Dimension d) {
		props.setProperty("WindowWidth", Integer.toString(d.width));
		props.setProperty("WindowHeight", Integer.toString(d.height));
	}

	public static Point getWindowLocation() {
		return new Point(Integer.parseInt(props.getProperty("WindowPositionX", "50")), Integer.parseInt(props.getProperty("WindowPositionY", "50")));
	}

	public static void setWindowLocation(Point p) {
		props.setProperty("WindowPositionX", Integer.toString(p.x));
		props.setProperty("WindowPositionY", Integer.toString(p.y));
	}
	
	public static String getThrottleAngleColumnName() {
		return props.getProperty("ThrottleAngleColumnName", "#$#");
	}
	
	public static void setThrottleAngleColumnName(String name) {
		props.setProperty("ThrottleAngleColumnName", name);
	}

	public static String getMafVoltageColumnName() {
		return props.getProperty("MafVoltageColumnName", "#$#");
	}
	
	public static void setMafVoltageColumnName(String name) {
		props.setProperty("MafVoltageColumnName", name);
	}

	public static String getIatColumnName() {
		return props.getProperty("IATColumnName", "#$#");
	}
	
	public static void setIatColumnName(String name) {
		props.setProperty("IATColumnName", name);
	}

	public static String getWidebandAfrColumnName() {
		return props.getProperty("WidebandAfrColumnName", "#$#");
	}
	
	public static void setWidebandAfrColumnName(String name) {
		props.setProperty("WidebandAfrColumnName", name);
	}

	public static String getAfrColumnName() {
		return props.getProperty("AfrColumnName", "#$#");
	}
	
	public static void setAfrColumnName(String name) {
		props.setProperty("AfrColumnName", name);
	}

	public static String getRpmColumnName() {
		return props.getProperty("RpmColumnName", "#$#");
	}
	
	public static void setRpmColumnName(String name) {
		props.setProperty("RpmColumnName", name);
	}

	public static String getLoadColumnName() {
		return props.getProperty("LoadColumnName", "#$#");
	}
	
	public static void setLoadColumnName(String name) {
		props.setProperty("LoadColumnName", name);
	}
	
	public static String getCommandedAfrColumnName() {
		return props.getProperty("CommandedAfrColumnName", "#$#");
	}
	
	public static void setCommandedAfrColumnName(String name) {
		props.setProperty("CommandedAfrColumnName", name);
	}

	public static String getTimeColumnName() {
		return props.getProperty("TimeColumnName", "#$#");
	}
	
	public static void setTimeColumnName(String name) {
		props.setProperty("TimeColumnName", name);
	}

	public static String getClOlStatusColumnName() {
		return props.getProperty("ClOlStatusColumnName", "#$#");
	}
	
	public static void setClOlStatusColumnName(String name) {
		props.setProperty("ClOlStatusColumnName", name);
	}

	public static String getAfLearningColumnName() {
		return props.getProperty("AfLearningColumnName", "#$#");
	}
	
	public static void setAfLearningColumnName(String name) {
		props.setProperty("AfLearningColumnName", name);
	}

	public static String getAfCorrectionColumnName() {
		return props.getProperty("AfCorrectionColumnName", "#$#");
	}
	
	public static void setAfCorrectionColumnName(String name) {
		props.setProperty("AfCorrectionColumnName", name);
	}

	public static int getClOlStatusValue() {
		return Integer.parseInt(props.getProperty("ClOlStatusValue", "-1"));
	}

	public static void setClOlStatusValue(int v) {
		props.setProperty("ClOlStatusValue", Integer.toString(v));
	}

	public static int getThrottleChangeValue() {
		return Integer.parseInt(props.getProperty("ThrottleChangeValue", "2"));
	}

	public static void setThrottleChangeValue(int v) {
		props.setProperty("ThrottleChangeValue", Integer.toString(v));
	}

	public static int getWotStationaryPointValue() {
		return Integer.parseInt(props.getProperty("WotStationaryPoint", "85"));
	}

	public static void setWotStationaryPointValue(int v) {
		props.setProperty("WotStationaryPoint", Integer.toString(v));
	}

	public static double getMafVMaximumValue() {
		return Double.parseDouble(props.getProperty("MafVMaximum", "5"));
	}

	public static void setMafVMaximumValue(double v) {
		props.setProperty("MafVMaximum", Double.toString(v));
	}

	public static double getIatMaximumValue() {
		return Double.parseDouble(props.getProperty("IATMaximum", "300"));
	}

	public static void setIatMaximumValue(double v) {
		props.setProperty("IATMaximum", Double.toString(v));
	}

	public static double getMafVMinimumValue() {
		return Double.parseDouble(props.getProperty("MafVMinimum", "0"));
	}
	
	public static void setWidebandAfrErrorPercentValue(double v) {
		props.setProperty("WidebandAfrErrorPercent", Double.toString(v));
	}

	public static double getWidebandAfrErrorPercentValue() {
		return Double.parseDouble(props.getProperty("WidebandAfrErrorPercent", "200"));
	}

	public static void setMafVMinimumValue(double v) {
		props.setProperty("MafVMinimum", Double.toString(v));
	}

	public static double getAfrMinimumValue() {
		return Double.parseDouble(props.getProperty("AfrMinimum", "13.7"));
	}

	public static void setAfrMinimumValue(double v) {
		props.setProperty("AfrMinimum", Double.toString(v));
	}

	public static double getAfrMaximumValue() {
		return Double.parseDouble(props.getProperty("AfrMaximum", "15"));
	}

	public static void setAfrMaximumValue(double v) {
		props.setProperty("AfrMaximum", Double.toString(v));
	}

	public static double getLoadMinimumValue() {
		return Double.parseDouble(props.getProperty("LoadMinimum", "0.05"));
	}

	public static void setLoadMinimumValue(double v) {
		props.setProperty("LoadMinimum", Double.toString(v));
	}

	public static double getDvDtMaximumValue() {
		return Double.parseDouble(props.getProperty("DvDtMaximum", "0.5"));
	}

	public static void setDvDtMaximumValue(double v) {
		props.setProperty("DvDtMaximum", Double.toString(v));
	}

	public static String getDefaultPOLFueling() {
		return props.getProperty("DefaultPOLFueling", "");
	}

	public static void setDefaultPOLFueling(String s) {
		props.setProperty("DefaultPOLFueling", s);
	}

	public static String getPOLFuelingFiles() {
		return props.getProperty("POLFuelingFiles", "");
	}

	public static void setPOLFuelingFiles(String s) {
		props.setProperty("POLFuelingFiles", s);
	}
	
	public static void load() {
		InputStream is = null;
		try {
			is = new FileInputStream(new File(CFG_FILE));
			props.loadFromXML(is);
		}
		catch (Exception e) {
            logger.error(e);
		}
		finally {
			if (is != null) {
				try {
					is.close();
				}
				catch (IOException e) {
		            logger.error(e);
				}
			}
		}
	}
	
	public static void save() {
		OutputStream os = null;
		try {
			os = new FileOutputStream(new File(CFG_FILE));
			props.storeToXML(os, "settings");
		}
		catch (Exception e) {
            logger.error(e);
		}
		finally {
			if (os != null) {
				try {
					os.close();
				}
				catch (IOException e) {
		            logger.error(e);
				}
			}
		}
	}

}

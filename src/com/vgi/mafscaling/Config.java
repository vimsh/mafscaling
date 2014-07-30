/*
* Open-Source tuning tools
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License along
* with this program; if not, write to the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

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
	public static final String NO_NAME = "#$#";
	private static Properties props = new Properties();

	public static String getProperty(String name) {
		return props.getProperty(name, "");
	}

	public static void setProperty(String name, String prop) {
		props.setProperty(name, prop);
	}

	public static void removeProperty(String name) {
		props.remove(name);
	}
	
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
	
	public static Dimension getCompWindowSize() {
		return new Dimension(Integer.parseInt(props.getProperty("CompareWindowWidth", "300")), Integer.parseInt(props.getProperty("CompareWindowHeight", "300")));
	}
	
	public static void setCompWindowSize(Dimension d) {
		props.setProperty("CompareWindowWidth", Integer.toString(d.width));
		props.setProperty("CompareWindowHeight", Integer.toString(d.height));
	}

	public static Point getCompWindowLocation() {
		return new Point(Integer.parseInt(props.getProperty("CompareWindowPositionX", "50")), Integer.parseInt(props.getProperty("WindowPositionY", "50")));
	}

	public static void setCompWindowLocation(Point p) {
		props.setProperty("CompareWindowPositionX", Integer.toString(p.x));
		props.setProperty("CompareWindowPositionY", Integer.toString(p.y));
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

	public static String getMpColumnName() {
		return props.getProperty("MPColumnName", "#$#");
	}
	
	public static void setMpColumnName(String name) {
		props.setProperty("MPColumnName", name);
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

	public static double getDvDtLCMaximumValue() {
		return Double.parseDouble(props.getProperty("DvDtLoadCompMaximum", "0.7"));
	}

	public static void setDvDtLCMaximumValue(double v) {
		props.setProperty("DvDtLoadCompMaximum", Double.toString(v));
	}

	public static double getIatLCMinimumOffset() {
		return Double.parseDouble(props.getProperty("IATLoadCompMinimumOffset", "10"));
	}

	public static void setIatLCMinimumOffset(double v) {
		props.setProperty("IATLoadCompMinimumOffset", Double.toString(v));
	}

	public static double getTrimsLCVarianceValue() {
		return Double.parseDouble(props.getProperty("TrimsLoadCompVariance", "10"));
	}

	public static void setTrimsLCVarianceValue(double v) {
		props.setProperty("TrimsLoadCompVariance", Double.toString(v));
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

	public static String getXAxisTemplates() {
		return props.getProperty("XAxisTemplates", ",");
	}

	public static void setXAxisTemplates(String s) {
		props.setProperty("XAxisTemplates", s);
	}

	public static String getYAxisTemplates() {
		return props.getProperty("YAxisTemplates", ",");
	}

	public static void setYAxisTemplates(String s) {
		props.setProperty("YAxisTemplates", s);
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

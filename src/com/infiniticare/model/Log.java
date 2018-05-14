package com.infiniticare.model;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Represents and Ecutek Log file. May work for other log file types as well.
 * @author Eugene Turkov
 *
 */
public class Log {
	private File logFile;
	private HashMap<String,Integer> columnNameToIndexMap = new HashMap<String,Integer>();
	private HashMap<Integer,ArrayList<Double>> columnIdToValuesMap = new HashMap<Integer,ArrayList<Double>>();
	
	/**
	 * Load a log file given a string representing a file location.
	 * @param string
	 * @throws IOException
	 */
	public Log(String string) throws IOException {
		logFile = new File(string);
		loadFile(logFile);
	}
	
	public HashMap<String, Integer> getColumnNameToIndexMap() {
		return columnNameToIndexMap;
	}

	public void setColumnNameToIndexMap(HashMap<String, Integer> columnNameToIndexMap) {
		this.columnNameToIndexMap = columnNameToIndexMap;
	}

	public HashMap<Integer, ArrayList<Double>> getColumnIdToValuesMap() {
		return columnIdToValuesMap;
	}

	public void setColumnIdToValuesMap(HashMap<Integer, ArrayList<Double>> columnIdToValuesMap) {
		this.columnIdToValuesMap = columnIdToValuesMap;
	}

	/**
	 * Load a log file given a file object.
	 * @param file
	 * @throws IOException
	 */
	public Log(File file) throws IOException {
		logFile = file;
		loadFile(logFile);
	}
	
	/**
	 * Returns the current log file object instance.
	 * @return the log file.
	 */
	public File getLogFile() {
		return logFile;
	}
	
	/**
	 * Internal log file loading method for Ecutek log files. Works for both hard cable and bluetooth logs.
	 * @param file
	 * @throws IOException
	 */
	private void loadFile(File file) throws IOException {
		logFile = file;
		FileReader fileReader = new FileReader(logFile);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = bufferedReader.readLine();
		while(line != null) {
			if(!line.startsWith("#")) {
				updateMap(line);
			}
			line = bufferedReader.readLine();
		}
		bufferedReader.close();
		fileReader.close();
	}
	
	/**
	 * Returns a list of ordered elements from the log for the specified logging attribute.
	 * @param attributeName
	 * @return a list of values for this logging attribute.
	 */
	public ArrayList<Double> getLoggedAttribute(String attributeName) {
		Integer index = columnNameToIndexMap.get(attributeName);
		if(index == null) {
			Set<String> keySet = columnNameToIndexMap.keySet();
			for (String key : keySet) {
				if(key.toLowerCase().indexOf(attributeName.toLowerCase()) > -1) {
					index = columnNameToIndexMap.get(key);
					break;
				}
			}
		}
		return columnIdToValuesMap.get(index);
	}

	/**
	 * Keeps track of various mappings (this is where the "magic" happens).
	 * @param line
	 */
	private void updateMap(String line) {
		String[] split = line.split(",");
		String[] elements = split;
		if(line.matches("^[A-Za-z]{1,}.*")) {
			// this is the line containing the titles
			for(int i = 0; i < elements.length; i++) {
				columnNameToIndexMap.put(elements[i], i);
			}
		} else {
			// this is a line containing data;
			String[] data = line.split(",");
			for(int i = 0; i < data.length; i++) {
				ArrayList<Double> dataArray = columnIdToValuesMap.get(i);
				if(dataArray == null) {
					dataArray = new ArrayList<Double>();		
					columnIdToValuesMap.put(i, dataArray);
				}
				try {
				dataArray.add(Double.parseDouble(data[i]));
				} catch(NumberFormatException e) {
					// assume it's speed density
					if(data[i].equals("OFF") || data[i].equals("???")) {
						dataArray.add(0.0);
					} else if(data[i].equals("ACTIVE")) {
						dataArray.add(1.0);
					} else {
						throw new UnsupportedOperationException("No strings allowed!");
					}
				}
			}
		}
	}

	/**
	 * Determine if the two logs have the same attributes.
	 * @param foreignLog
	 * @return true if attributes match, false otherwise.
	 */
	public boolean attributesMatch(Log foreignLog) {
		if(columnNameToIndexMap.equals(foreignLog.getColumnNameToIndexMap())
				&& columnIdToValuesMap.equals(foreignLog.getColumnIdToValuesMap())) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return logFile.getName();
	}
	
}

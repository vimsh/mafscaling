package com.infiniticare.model.ecutek;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.infiniticare.model.Cell;

/**
 * A generic Ecutek map implementation. Can be a custom map.
 * @author Eugene Turkov
 *
 */
public class EcutekMap {
	Logger logger = Logger.getLogger(getClass());
	private HashMap<Double, HashMap<Double, Cell>> table = new HashMap<Double, HashMap<Double, Cell>>();
	private ArrayList<Double> rowNames = new ArrayList<Double>();
	private double[] columnNames;
	private File file;
	
	public void clearTableMember() {
		table = new HashMap<Double, HashMap<Double, Cell>>();
	}
	
	public EcutekMap(File file) {
		this(file, false);
	}
	
	public EcutekMap(File file, boolean isFromCustomMap) {
		if(file == null) {
			return;
		}
		this.file = file;
		FileReader fileReader;
		try {
			fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = bufferedReader.readLine();
			int rowIndex = -1;
			
			while(line != null) {
				String[] columnValues = line.split("\t");
				if(rowIndex == -1) {
					columnNames = new double[columnValues.length - 1];
					for(int i = 1; i < columnValues.length; i++) {
						String string = columnValues[i];
						if(string.trim().equals("")) {
							string = "0.0";
							columnValues[i] = "0.0";
						}
						double columnName = Double.valueOf(string);
						columnNames[i - 1] = columnName;
					}
				} else {
					// the first element is the row name
					String string = columnValues[0];
					if(string.trim().equals("")) {
						string = "0.0";
						columnValues[0] = "0.0";
					}
					rowNames.add(rowIndex, Double.valueOf(string));
					
					for (int i = 1; i < columnValues.length; i++) {
						int columnId = i - 1;
						double rowName = rowNames.get(rowIndex);
						double columnName = columnNames[columnId];
						String perspectiveColumnValue = columnValues[i];
						if(perspectiveColumnValue.equals("")) {
							perspectiveColumnValue = "0.0";
						}
						double columnValue = Double.parseDouble(perspectiveColumnValue);
						if(isFromCustomMap) {
							columnValue = columnValue * 100.0;
						}
						add(rowName, columnName, columnValue);
					}
				}
				line = bufferedReader.readLine();
				rowIndex++;
			}		
			
			bufferedReader.close();
			fileReader.close();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(),e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
		
	}
	
	public EcutekMap() {
		
	}
	
	public File getFile() {
		return file;
	}

	@Override
	public String toString() {
		return this.getFile().getAbsolutePath();
	}

	private void add(Double rowIndex, Double columnIndex, double value) {
		Cell tableCell = getCell(rowIndex, columnIndex);
		tableCell.getPopulation().add(value);
	}

	public Cell getCell(Double rowIndex, Double columnIndex) {
		double rowName = getClosestRow(rowIndex);
		double columnName = getClosestColumn(columnIndex);
		
		if(rowName == -1 || columnName == -1) {
			return null;
		}
		
		HashMap<Double, Cell> row = table.get(rowName);
		if(row == null) {
			row = new HashMap<Double, Cell>();
			table.put(rowName, row);
		}
		
		Cell tableCellData = row.get(columnName);
		if(tableCellData == null) {
			tableCellData = new Cell();
			row.put(columnName, tableCellData);
		}
		
		return tableCellData;
	}

	/*
	 *  maybe the difference should be very small in order to return something useful. Not useful if it's right on the edge.
	 */
	private double getClosestColumn(Double manifoldAbsolutePressure) {
		double shortestDistanceSoFar = Double.MAX_VALUE;
		double bestChoiceSoFar = -1;
		for(int i = 0; i < columnNames.length; i++) {
			double columnName = columnNames[i];
			double difference = Math.abs(columnName - manifoldAbsolutePressure);
			if(difference < shortestDistanceSoFar) {
				if(i == 0) {
					if(manifoldAbsolutePressure >= columnName) {
						shortestDistanceSoFar = difference;
						bestChoiceSoFar = columnNames[i];
					} else {
						return -1;
					}
				} else if(i + 1 == columnNames.length) {
					if(manifoldAbsolutePressure <= columnName) {
						shortestDistanceSoFar = difference;
						bestChoiceSoFar = columnNames[i];
					} else {
						return -1;
					}
				} else {
					shortestDistanceSoFar = difference;
					bestChoiceSoFar = columnNames[i];
				}
			}
		}
		return bestChoiceSoFar;		
	}
	
	public void setRowNames(ArrayList<Double> rowNames) {
		this.rowNames = rowNames;
	}

	public void setColumnNames(double[] columnNames) {
		this.columnNames = columnNames;
	}

	/*
	 *  maybe the difference should be very small in order to return something useful. Not useful if it's right on the edge.
	 */
	private double getClosestRow(Double engineSpeed) {
		double shortestDistanceSoFar = Double.MAX_VALUE;
		double bestChoiceSoFar = -1;
		for(int i = 0; i < rowNames.size(); i++) {
			double rowName = rowNames.get(i);
			double difference = Math.abs(rowName - engineSpeed);
			if(difference < shortestDistanceSoFar) {
				if(i == 0) {
					if(engineSpeed >= rowName) {
						shortestDistanceSoFar = difference;
						bestChoiceSoFar = rowNames.get(i);
					} else {
						return -1;
					}
				} else if(i + 1 == rowNames.size()) {
					if(engineSpeed <= rowName) {
						shortestDistanceSoFar = difference;
						bestChoiceSoFar = rowNames.get(i);
					} else {
						return -1;
					}
				} else {
					shortestDistanceSoFar = difference;
					bestChoiceSoFar = rowNames.get(i);
				}
				
			}
		}
		return bestChoiceSoFar;		
	}

	public void save(String revisedVEMapFileLocationString, int minimumCount, boolean forTiming, double correctionFactor, boolean fromCustomMap) throws IOException {
		logger.info("Saving...");
		File file = new File(revisedVEMapFileLocationString);
		if(file.exists()) {
			file = new File(revisedVEMapFileLocationString.substring(0,revisedVEMapFileLocationString.lastIndexOf('.')) + "." + System.currentTimeMillis() + ".map");
		}
		
		// add columnNames
		StringBuilder stringBuffer = new StringBuilder();
		for(int i = 0; i < columnNames.length; i++) {
			stringBuffer.append("\t");
			stringBuffer.append(columnNames[i]);
		}
		
		// add one row at a time
		for(int i = 0; i < rowNames.size(); i++) {
			double rowName = rowNames.get(i);
			stringBuffer.append("\n");
			stringBuffer.append(rowName);
			for(int j = 0; j < columnNames.length; j++) {
				double columnName = columnNames[j];
				stringBuffer.append("\t");
				HashMap<Double, Cell> columns = table.get(rowName);
				if(columns != null) {
					Cell tableCellData = columns.get(columnName);
					if(tableCellData != null) {
						double representativeValue = tableCellData.getRepresentativeValue(minimumCount); // used to be get representative value
						//Double average = tableCellData.getMean();
						logger.info("Processing row: " + i + " column:" + j);

						Double originalValue = tableCellData.getPopulation().get(0);

						if(forTiming) {
							if(tableCellData.getPopulation().size() == 1) {
								stringBuffer.append(originalValue);
							} else {
								representativeValue = tableCellData.getSmallestElement() + 1;
								
								if(representativeValue > originalValue) {
									representativeValue = originalValue;
								}
								stringBuffer.append(Math.floor(representativeValue));
							}
						} else {	
							double d = representativeValue / originalValue;
							// d is the multiplier for the original value.
							// now we want to scale it by the correction factor
							// if d is > 1, then d - 1 and divide by 2. Then add 1 back.
							if(d > 1) {
								d = d - 1; // gives us something like 0.2
								d = d * correctionFactor; // now we have something like 0.1 if the correction factor was 0.5
								d = d + 1; // now we have our final correction of 1.1 rather than the original 1.2
							} else if (d < 1) {
								d = 1 - d; // gives us something like 0.15 when d is 0.85
								d = d * correctionFactor; // now we have something ike 0.075 if correction factor is 0.5
								d = 1 - d; // now we have 0.925
							}
							
							representativeValue = originalValue * d;
							
							if(representativeValue != originalValue) {
								if(fromCustomMap) {
									representativeValue = representativeValue / 100.0;
								}
								stringBuffer.append(representativeValue);
							} else {
								if(fromCustomMap) {
									originalValue = originalValue / 100.0;
								}
								stringBuffer.append(originalValue);
							}
						}
					}
				} else {
					stringBuffer.append("0.0");
				}
			}
		}
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(stringBuffer.toString());
		fileWriter.flush();
		fileWriter.close();
		logger.info("Done writing to file: " + file.getAbsolutePath());
	}

	public Double getPopularElement(ArrayList<Double> a)
	{
	  int count = 1, tempCount;
	  double popular = a.get(0);
	  double temp = 0;
	  for (int i = 0; i < (a.size() - 1); i++)
	  {
	    temp = a.get(i);
	    tempCount = 0;
	    for (int j = 1; j < a.size(); j++)
	    {
	      if (temp == a.get(j))
	        tempCount++;
	    }
	    if (tempCount > count)
	    {
	      popular = temp;
	      count = tempCount;
	    }
	  }
	  return popular;
	}
}

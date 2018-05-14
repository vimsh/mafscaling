package com.infiniticare.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.infiniticare.model.Cell;
public class Map {
	private HashMap<Double, HashMap<Double, Cell>> table = new HashMap<Double, HashMap<Double, Cell>>();
	private ArrayList<Double> rowNames = new ArrayList<Double>();
	private double[] columnNames;
	private File file;
	
	public void clearTableMember() {
		table = new HashMap<Double, HashMap<Double, Cell>>();
	}
	
	public Map(File file) {
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
						double columnName = Double.valueOf(columnValues[i]);
						columnNames[i - 1] = columnName;
					}
				} else {
					// the first element is the row name
					rowNames.add(rowIndex, Double.valueOf(columnValues[0]));
					
					for (int i = 1; i < columnValues.length; i++) {
						int columnId = i - 1;
						double rowName = rowNames.get(rowIndex);
						double columnName = columnNames[columnId];
						String perspectiveColumnValue = columnValues[i];
						if(perspectiveColumnValue.equals("")) {
							perspectiveColumnValue = "0";
						}
						double columnValue = Double.parseDouble(perspectiveColumnValue);
						add(rowName, columnName, columnValue);
					}
				}
				line = bufferedReader.readLine();
				rowIndex++;
			}		
			
			bufferedReader.close();
			fileReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public Map() {
		
	}
	
	public File getFile() {
		return file;
	}

	@Override
	public String toString() {
		return super.toString();
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

	public void save(String revisedVEMapFileLocationString, int minimumCount, boolean forTiming) throws IOException {
		System.out.println("saving...");
		File file = new File(revisedVEMapFileLocationString);
		if(file.exists()) {
			file = new File(revisedVEMapFileLocationString.substring(0,revisedVEMapFileLocationString.lastIndexOf('.')) + "." + System.currentTimeMillis() + ".map");
		}
		
		// load the pre-existing file
		//SDVEMap oldMap = new SDVEMap(originalVEMapFileLocationString);
		
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
						System.out.println("processing " + i + "," + j);

						Double originalValue = tableCellData.getPopulation().get(0);
						if(originalValue != representativeValue) {
							System.out.println("here");
						}
						if(forTiming) {
							if(tableCellData.getPopulation().size() == 1) {
								stringBuffer.append(originalValue);
							} else {
								//representativeValue = getPopularElement(tableCellData.getPopulation());
								representativeValue = tableCellData.getSmallestElement() + 1;
								/*
								double mean = tableCellData.getMean();
								double roundedMean = Math.round(mean);
								if(roundedMean < representativeValue) {
									representativeValue = roundedMean;
								}*/

								if(representativeValue > originalValue) {
									representativeValue = originalValue;
								}
								stringBuffer.append(Math.floor(representativeValue));
							}
						} else {			
							
							if(representativeValue > originalValue) {
								double d = representativeValue / originalValue;
								double differecence = d - 1.0;
								double halfDifference = differecence / 2;
								double halfDifferenceWithOne = halfDifference + 1;
								double newAverage = originalValue * halfDifferenceWithOne; 
								stringBuffer.append(newAverage);
							} else if(representativeValue < originalValue) { 
								double d = representativeValue / originalValue;
								double differecence = 1.0 - d;
								double halfDifference = differecence / 2;
								double halfDifferenceWithOne = halfDifference + 1;
								double newAverage = representativeValue * halfDifferenceWithOne;
								stringBuffer.append(newAverage);
							} else {
								stringBuffer.append(originalValue);
							}
							//stringBuffer.append(representativeValue);

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
		System.out.println("done writing to file: " + file.getAbsolutePath());
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

package com.infiniticare.model.ecutek;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.infiniticare.model.Cell;

/**
 * An instance of this class represents an EcutekTable as known in the Ecutek ProECU software. Choosing the
 * option "copy entire map into clipboard" and saving the data to a file on the file system, this class is
 * able to instantly load all data into a useable format.
 * @author Eugene Turkov
 *
 */
public class EcutekTable {
	private HashMap<Double, HashMap<Double, Cell>> table;
	private ArrayList<Double> rowNames = new ArrayList<Double>();
	private double[] columnNames;
	private File file;
	
	public void clearTableMember() {
		table = new HashMap<Double, HashMap<Double, Cell>>();
	}
	
	/**
	 * Instantiates this table from file. The file must be clipboard data saved from Ecutek.
	 * @param file
	 * @throws IOException
	 */
	public EcutekTable(File file) throws IOException {	
		table = new HashMap<Double, HashMap<Double, Cell>>();
	
		// if no file is specified, the table will be empty.
		if(file != null) {
			this.file = file;
			FileReader fileReader;
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
		}		
	}
	
	/**
	 * Default constructor creates an empty table.
	 * @throws IOException
	 */
	public EcutekTable() throws IOException {
		this(null);
	}
	
	public File getFile() {
		return file;
	}

	/**
	 * Adds a value to the population of this cell.
	 * @param rowIndex
	 * @param columnIndex
	 * @param value
	 */
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

	/**
	 * Given the supplied column name (column index), return closest matching column name.
	 * 
	 *  For example if the columns are named as 0 0.5 1 1.5 2 2.5 and the columnIndex of 1.1 would return the value 1.
	 * @param columnIndex
	 * @return the closest match.
	 */
	private double getClosestColumn(Double columnIndex) {
		double shortestDistanceSoFar = Double.MAX_VALUE;
		double bestChoiceSoFar = -1;
		for(int i = 0; i < columnNames.length; i++) {
			double columnName = columnNames[i];
			double difference = Math.abs(columnName - columnIndex);
			if(difference < shortestDistanceSoFar) {
				if(i == 0) {
					if(columnIndex >= columnName) {
						shortestDistanceSoFar = difference;
						bestChoiceSoFar = columnNames[i];
					} else {
						return -1;
					}
				} else if(i + 1 == columnNames.length) {
					if(columnIndex <= columnName) {
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

	/**
	 * Given the supplied row name (row index), return closest matching row name.
	 * 
	 *  For example if the rows are named as 0 0.5 1 1.5 2 2.5 and the rowIndex of 1.1 would return the value 1.
	 * @param rowIndex
	 * @return the closest match.
	 */
	private double getClosestRow(Double rowIndex) {
		double shortestDistanceSoFar = Double.MAX_VALUE;
		double bestChoiceSoFar = -1;
		for(int i = 0; i < rowNames.size(); i++) {
			double rowName = rowNames.get(i);
			double difference = Math.abs(rowName - rowIndex);
			if(difference < shortestDistanceSoFar) {
				if(i == 0) {
					if(rowIndex >= rowName) {
						shortestDistanceSoFar = difference;
						bestChoiceSoFar = rowNames.get(i);
					} else {
						return -1;
					}
				} else if(i + 1 == rowNames.size()) {
					if(rowIndex <= rowName) {
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

	/**
	 * Saves this table to a file on the disk.
	 * @param destinationFileName
	 * @throws IOException
	 */
	public void save(File file) throws IOException {
		System.out.println("saving...");
		if(file.exists()) {
			String destinationFileName = file.getName();
			file = new File(destinationFileName.substring(0,destinationFileName.lastIndexOf('.')) + "." + System.currentTimeMillis() + ".map");
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
						double representativeValue = tableCellData.getRepresentativeValue();
						System.out.println("processing " + i + "," + j);
						stringBuffer.append(representativeValue);
					} 
				} else {
					stringBuffer.append("100.0");
				}
			}
		}
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(stringBuffer.toString());
		fileWriter.flush();
		fileWriter.close();
		System.out.println("done writing to file: " + file.getAbsolutePath());
	}
}

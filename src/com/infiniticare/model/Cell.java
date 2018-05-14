package com.infiniticare.model;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A cell may contain one or more numbers inside of it.
 * @author Eugene Turkov
 *
 */
public class Cell {
	private static final int MIN_POPULATION_SIZE_TO_CALCULATE_REPRESENTATIVE_VALUE = 2;
	
	/**
	 * These are the values to be stored in the Cell.
	 */
	private ArrayList<Double> population = new ArrayList<Double>();
	
	public Cell() {
	}
	
	/**
	 * The the average of a subset of values in this cell.
	 * @param i the first element
	 * @param n the last element
	 * @return
	 */
	public double getSampleMean(int i, int n) {
		double sumSoFar = 0;
		while(i < n) {
			Double value = population.get(i);
			sumSoFar += value;
			i++;
		}
		return sumSoFar / (double)(i);
	}
	
	/**
	 * The average of the values in this cell.
	 * 
	 */
	public double getMean() {
		return getSampleMean(0,population.size());
	}
	
	/**
	 * On average how far is a data point from the mean?
	 * @return
	 */
	public double getVariance() {
		double squaredDeltaSum = 0;
		int size = population.size();
		int lastPercentComplete = 0;
		for (int i = 0; i < size; i++) {

			int percentComplete = (int)(((double)i/(double)size) * 100);
			if(lastPercentComplete < percentComplete && percentComplete % 5 == 0) {
				lastPercentComplete = percentComplete;
				System.out.println(percentComplete + " % complete.");
			}
			
			double value = population.get(i);
			double currentMean = getMean();
			double delta = Math.abs(value - currentMean);
			double squaredDelta = delta * delta;
			squaredDeltaSum += squaredDelta;
		}
		
		double averageOfSquaredDeltas = squaredDeltaSum / size;
		return averageOfSquaredDeltas;
	}
	
	/**
	 * 
	 */
	public double getStandardDeviation() {
		double variance = getVariance();
		if(variance == 0) {
			return 0;
		}
		double standardDeviation = Math.sqrt(variance);
		return standardDeviation;
	}	
	
	/**
	 * For easy testing purposes only.
	 * @param args
	 */
	public static void main(String[] args) {
		Cell cell = new Cell();
		ArrayList<Double> population = new ArrayList<Double>();
		cell.setPopulation(population);

		for(int i = 0; i < 20; i++) {
			population.add(81.5);
		}
		
		for(int i = 0 ; i < 30; i++) {
			population.add(85.2);
		}
		
		for(int i = 0; i < 15; i++) {
			population.add(89.1);
		}
		
		population.add(51.0);
		population.add(68.9);
		population.add(97.9);

		System.out.println(cell.getRepresentativeValue(1));
	}
	
	/**
	 * Returns a value representing this cell. All values are considered.
	 * @return
	 */
	public double getRepresentativeValue() {
		return this.getRepresentativeValue(1);
	}
	/**
	 * 
	 * @return the value representative of all the REASONABLE values.
	 */
	private double getRepresentativeValue(double maxAllowedDeviations) {
		if(population.size() < MIN_POPULATION_SIZE_TO_CALCULATE_REPRESENTATIVE_VALUE) {
			return population.get(0);
		}
		double standardDeviation = getStandardDeviation();
		if(standardDeviation == 0) {
			return population.get(0);
		}
		double mean = getMean();
		double sumSoFar = 0;
		int numbersIncluded = 0;
		for(int i = 0; i < population.size(); i++) {
			Double value = population.get(i);
			double delta = Math.abs(mean - value);
			double nDeviations = delta / standardDeviation;
			if(nDeviations < maxAllowedDeviations) {
				sumSoFar += value;
				numbersIncluded++;
			}
		}		
		
		double average = sumSoFar / (double)numbersIncluded;
		//boolean enteredForLoop = false;
		while(Double.valueOf(average).equals(Double.NaN)) {
//			/enteredForLoop = true;
			maxAllowedDeviations += 0.1;
			//System.out.println("incremented max allowed deviations to: " + maxAllowedDeviations);
			average = getRepresentativeValue(maxAllowedDeviations);
		}
		
		/*
		if(!enteredForLoop) {
			
			System.out.println("deviation threshold: " + maxAllowedDeviations);
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("these are the values in this cell: ");
			for(int i = 0; i < population.size(); i++) {
				Double value = population.get(i);
				stringBuffer.append(value.toString());
				if(i + 1 < population.size()) {
					stringBuffer.append(",");
				}
			}
			stringBuffer.append(" resulting value is: " + average);
			System.out.println(stringBuffer.toString());
			
		}*/
		
		return average;
	}

	/**
	 * Returns the representative value of this cell. If the minimum number of elements is not
	 * satisfied, then the original first element that was used when the table was loaded is returned.
	 * In effect, the result of this method would return the original default value with no change if the
	 * minimum population size is not satisfied.
	 * @param minimumNumber
	 * @return representative population, or default value if population does not meet the minimum number.
	 */
	public double getRepresentativeValue(int minimumNumber) {
		if(this.getPopulation().size() > minimumNumber) {
			double representativeValue = getRepresentativeValue(0.1);
			return representativeValue;
		} else {
			return population.get(0);
		}
	 }
	
	public ArrayList<Double> getPopulation() {
		return population;
	}

	public void setPopulation(ArrayList<Double> population) {
		this.population = population;
	}

	public Double getSmallestElement() {
		Collections.sort(getPopulation());
		return population.get(0);
	}
}
package com.infiniticare.model;

public class KnockMember {
	private double loudestKnockValue;
	private double thresholdForLoudestCylinder;
	private double averageKnockValue;
	private double averageKnockThresholdValue;
	
	public double getLoudestKnockValue() {
		return loudestKnockValue;
	}

	public void setLoudestKnockValue(double loudestKnockValue) {
		this.loudestKnockValue = loudestKnockValue;
	}

	public double getThresholdForLoudestCylinder() {
		return thresholdForLoudestCylinder;
	}

	public void setThresholdForLoudestCylinder(double thresholdForLoudestCylinder) {
		this.thresholdForLoudestCylinder = thresholdForLoudestCylinder;
	}

	public KnockMember() {
	}

	public void setAverageKnockValue(double averageKnockValue) {
		this.averageKnockValue = averageKnockValue;
	}

	public void setAverageKnockThresholdValue(double averageKnockThresholdValue) {
		this.averageKnockThresholdValue = averageKnockThresholdValue;
	}
	
	public double getAverageDistanceFromThreshold() {
		return averageKnockValue/averageKnockThresholdValue;
	}
	
	public double getDistanceFromThresholdForLoudestKnockValue;

}

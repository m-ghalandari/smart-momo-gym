package de.momogym.dto;

import java.io.Serializable;

public class WorkoutSetDTO implements Serializable {
	private int setNumber;
	private Double weight;
	private String reps;
	private boolean completed;

	public WorkoutSetDTO(int setNumber, Double targetWeight, String targetReps) {
		this.setNumber = setNumber;
		this.weight = targetWeight != null ? targetWeight : 0.0;
		this.reps = targetReps;
		this.completed = false;
	}

	public int getSetNumber() { return setNumber; }
	public Double getWeight() { return weight; }
	public void setWeight(Double weight) { this.weight = weight; }
	public String getReps() { return reps; }
	public void setReps(String reps) { this.reps = reps; }
	public boolean isCompleted() { return completed; }
	public void setCompleted(boolean completed) { this.completed = completed; }
}
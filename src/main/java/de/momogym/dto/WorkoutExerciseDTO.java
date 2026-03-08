package de.momogym.dto;

import de.momogym.persistence.PlannedExercise;

import java.io.Serializable;

public class WorkoutExerciseDTO implements Serializable {
	private PlannedExercise plannedExercise;
	private int actualSets;
	private String actualReps;
	private Double actualWeight;

	public WorkoutExerciseDTO(PlannedExercise pe) {
		this.plannedExercise = pe;

		this.actualSets = pe.getSets();
		this.actualReps = pe.getReps();
		this.actualWeight = pe.getWeight() != null ? pe.getWeight() : 0.0;
	}

	public PlannedExercise getPlannedExercise() { return plannedExercise; }
	public int getActualSets() { return actualSets; }
	public void setActualSets(int actualSets) { this.actualSets = actualSets; }
	public String getActualReps() { return actualReps; }
	public void setActualReps(String actualReps) { this.actualReps = actualReps; }
	public Double getActualWeight() { return actualWeight; }
	public void setActualWeight(Double actualWeight) { this.actualWeight = actualWeight; }
}

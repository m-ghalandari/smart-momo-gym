package de.momogym.dto;

import de.momogym.persistence.PlannedExercise;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WorkoutExerciseDTO implements Serializable {
	private PlannedExercise plannedExercise;
	private List<WorkoutSetDTO> sets;

	public WorkoutExerciseDTO(PlannedExercise pe) {
		this.plannedExercise = pe;
		this.sets = new ArrayList<>();

		// Generiert automatisch die Reihen für die UI basierend auf den Ziel-Sätzen
		for (int i = 1; i <= pe.getSets(); i++) {
			this.sets.add(new WorkoutSetDTO(i, pe.getWeight(), pe.getReps()));
		}
	}

	public PlannedExercise getPlannedExercise() { return plannedExercise; }
	public List<WorkoutSetDTO> getSets() { return sets; }

	// Prüft, ob alle Sätze dieser Übung abgehakt wurden
	public boolean isAllSetsCompleted() {
		return sets.stream().allMatch(WorkoutSetDTO::isCompleted);
	}
}
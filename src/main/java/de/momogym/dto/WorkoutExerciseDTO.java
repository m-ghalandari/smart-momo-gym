package de.momogym.dto;

import de.momogym.persistence.PlannedExercise;
import de.momogym.persistence.PlannedSet;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WorkoutExerciseDTO implements Serializable {
	private PlannedExercise plannedExercise;
	private List<WorkoutSetDTO> sets;

	public WorkoutExerciseDTO(PlannedExercise pe) {
		this.plannedExercise = pe;
		this.sets = new ArrayList<>();

		if (pe.getPlannedSets() != null && !pe.getPlannedSets().isEmpty()) {
			for (PlannedSet ps : pe.getPlannedSets()) {
				this.sets.add(new WorkoutSetDTO(ps.getSetNumber(), ps.getWeight(), ps.getReps()));
			}
		} else {
			for (int i = 1; i <= pe.getSets(); i++) {
				String weightStr = pe.getWeight() != null ? String.valueOf(pe.getWeight()) : "0";
				if (weightStr.endsWith(".0")) weightStr = weightStr.substring(0, weightStr.length() - 2);
				this.sets.add(new WorkoutSetDTO(i, weightStr, pe.getReps()));
			}
		}
	}

	public PlannedExercise getPlannedExercise() { return plannedExercise; }
	public List<WorkoutSetDTO> getSets() { return sets; }

	public boolean isAllSetsCompleted() {
		return sets.stream().allMatch(WorkoutSetDTO::isCompleted);
	}
}
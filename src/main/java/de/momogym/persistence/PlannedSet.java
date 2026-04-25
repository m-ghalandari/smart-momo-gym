package de.momogym.persistence;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "PLANNED_SET")
public class PlannedSet implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "planned_set_seq")
	@SequenceGenerator(name = "planned_set_seq", sequenceName = "PLANNED_SET_SEQ", allocationSize = 1)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PLANNED_EXERCISE_ID", nullable = false)
	private PlannedExercise plannedExercise;

	@Column(name = "SET_NUMBER")
	private int setNumber; // z.B. 1, 2, 3

	@Column(name = "REPS")
	private String reps; // String, um "12" oder auch EGYM-Werte zu erlauben

	@Column(name = "WEIGHT_INFO")
	private String weight; // String, für "20", "20/25" oder "p.S 10"

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PlannedExercise getPlannedExercise() {
		return plannedExercise;
	}

	public void setPlannedExercise(PlannedExercise plannedExercise) {
		this.plannedExercise = plannedExercise;
	}

	public int getSetNumber() {
		return setNumber;
	}

	public void setSetNumber(int setNumber) {
		this.setNumber = setNumber;
	}

	public String getReps() {
		return reps;
	}

	public void setReps(String reps) {
		this.reps = reps;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}
}
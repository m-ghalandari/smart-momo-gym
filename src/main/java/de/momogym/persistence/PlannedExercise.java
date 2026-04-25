package de.momogym.persistence;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PLANNED_EXERCISE")
public class PlannedExercise {

    @Id
    @SequenceGenerator(name = "planned_exercise_seq", sequenceName = "PLANNED_EXERCISE_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "planned_exercise_seq")
    private Long id;

    // VIELE geplante Übungen gehören zu EINEM Tag
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DAY_ID", nullable = false)
    private TrainingDay trainingDay;

    // VIELE geplante Übungen (z.B. 3x10 Bankdrücken)
    // zeigen auf EINE Übung im Katalog (Bankdrücken)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "EXERCISE_ID", nullable = false)
    private Exercise exercise;

	@OneToMany(mappedBy = "plannedExercise", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@OrderBy("setNumber ASC")
	private List<PlannedSet> plannedSets = new ArrayList<>();

    @Column(name = "SETS")
    private int sets; // Sätze (z.B. 3)

    @Column(name = "REPS")
    private String reps; // Wiederholungen (z.B. "8-12" oder "10")

    @Column(name = "SORT_ORDER")
    private int sortOrder; // Reihenfolge der Übung am Tag

	@Column(name = "WEIGHT_KG")
	private Double weight;

    public PlannedExercise() {
    }

    public PlannedExercise(TrainingDay trainingDay, Exercise exercise, int sets, String reps, int sortOrder) {
        this.trainingDay = trainingDay;
        this.exercise = exercise;
        this.sets = sets;
        this.reps = reps;
        this.sortOrder = sortOrder;
    }

	// Prüft, ob alle generierten Sätze exakt gleich sind (für die UI-Anzeige)
	@Transient
	public boolean isUniform() {
		if (plannedSets == null || plannedSets.isEmpty()) {
			return true;
		}

		String firstReps = plannedSets.get(0).getReps();
		String firstWeight = plannedSets.get(0).getWeight();

		for (PlannedSet ps : plannedSets) {
			if (!java.util.Objects.equals(firstReps, ps.getReps()) ||
				!java.util.Objects.equals(firstWeight, ps.getWeight())) {
				return false;
			}
		}
		return true;
	}

    public Long getId() {
        return id;
    }

    public TrainingDay getTrainingDay() {
        return trainingDay;
    }

    public void setTrainingDay(TrainingDay trainingDay) {
        this.trainingDay = trainingDay;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public String getReps() {
        return reps;
    }

    public void setReps(String reps) {
        this.reps = reps;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

	public Double getWeight() {
		return weight;
	}
	public void setWeight(Double weight) {
		this.weight = weight;
	}

	public List<PlannedSet> getPlannedSets() {
		return plannedSets;
	}
	public void setPlannedSets(List<PlannedSet> plannedSets) {
		this.plannedSets = plannedSets;
	}
}

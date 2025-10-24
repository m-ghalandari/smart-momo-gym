package de.momogym.persistence;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TRAINING_DAY")
public class TrainingDay {

    @Id
    @SequenceGenerator(name = "training_day_seq", sequenceName = "TRAINING_DAY_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "training_day_seq")
    private Long id;

    @Column(name = "DAY_NAME")
    private String name; // z.B. "Tag 1: Push"

    // Die Reihenfolge im Plan (z.B. 1, 2, 3, 4)
    @Column(name = "DAY_SEQUENCE", nullable = false)
    private int daySequence;

    // VIELE Tage gehören zu EINEM Trainingsplan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PLAN_ID", nullable = false)
    private TrainingPlan trainingPlan;

    @OneToMany(
            mappedBy = "trainingDay",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("sortOrder ASC") // 'sortOrder' (aus V1) ist für Übungen *innerhalb* des Tages
    private List<PlannedExercise> plannedExercises = new ArrayList<>();

    public TrainingDay() {
    }

    public TrainingDay(String name, int daySequence, TrainingPlan trainingPlan, List<PlannedExercise> plannedExercises) {
        this.name = name;
        this.daySequence = daySequence;
        this.trainingPlan = trainingPlan;
        this.plannedExercises = plannedExercises;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDaySequence() {
        return daySequence;
    }

    public void setDaySequence(int daySequence) {
        this.daySequence = daySequence;
    }

    public TrainingPlan getTrainingPlan() {
        return trainingPlan;
    }

    public void setTrainingPlan(TrainingPlan trainingPlan) {
        this.trainingPlan = trainingPlan;
    }

    public List<PlannedExercise> getPlannedExercises() {
        return plannedExercises;
    }

    public void setPlannedExercises(List<PlannedExercise> plannedExercises) {
        this.plannedExercises = plannedExercises;
    }
}

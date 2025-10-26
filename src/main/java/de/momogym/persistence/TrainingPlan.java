package de.momogym.persistence;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TRAINING_PLAN",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_PLAN_NAME_PER_ATHLETE",
                columnNames = {"PLAN_NAME", "ATHLETE_ID"}
        )
)
public class TrainingPlan {

    @Id
    @SequenceGenerator(name = "training_plan_seq", sequenceName = "TRAINING_PLAN_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "training_plan_seq")
    private Long id;

    @Column(name = "PLAN_NAME", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ATHLETE_ID", nullable = false)
    private Athlete athlete;

    @Column(name = "IS_ACTIVE_PLAN")
    private boolean isActive;

    @Column(name = "CURRENT_DAY_SEQUENCE")
    private int currentDaySequence;

    // Wir sorgen dafür, dass die Tage immer nach der Sequenz sortiert geladen werden.
    @OneToMany(
            mappedBy = "trainingPlan",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("daySequence ASC") // WICHTIG: Sortiert nach 1, 2, 3...
    private List<TrainingDay> trainingDays = new ArrayList<>();

    public TrainingPlan() {
    }

    public TrainingPlan(String name, Athlete athlete, boolean isActive, int currentDaySequence, List<TrainingDay> trainingDays) {
        this.name = name;
        this.athlete = athlete;
        this.isActive = isActive;
        this.currentDaySequence = currentDaySequence;
        this.trainingDays = trainingDays;
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

    public Athlete getAthlete() {
        return athlete;
    }

    public void setAthlete(Athlete athlete) {
        this.athlete = athlete;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getCurrentDaySequence() {
        return currentDaySequence;
    }

    public void setCurrentDaySequence(int currentDaySequence) {
        this.currentDaySequence = currentDaySequence;
    }

    public List<TrainingDay> getTrainingDays() {
        return trainingDays;
    }

    public void setTrainingDays(List<TrainingDay> trainingDays) {
        this.trainingDays = trainingDays;
    }
}

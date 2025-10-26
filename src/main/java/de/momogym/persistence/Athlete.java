package de.momogym.persistence;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ATHLETE")
public class Athlete {

    @Id
    @SequenceGenerator(
            name = "athlete_seq_gen",
            sequenceName = "ATHLETE_SEQ",
            initialValue = 1,
            allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "athlete_seq_gen")
    private Long id;

    @Column(name = "USERNAME", nullable = false, unique = true)
    private String username;

    // Ein Athlet hat VIELE Pläne
    @OneToMany(
            mappedBy = "athlete",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<TrainingPlan> trainingPlans = new ArrayList<>();

    // Ein Athlet hat VIELE Log-Einträge
    @OneToMany(
            mappedBy = "athlete",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<ExerciseLog> logs = new ArrayList<>();

    public Athlete() {
    }
    public Athlete(String username) {
        this.username = username;
    }


    public Athlete(String username, List<TrainingPlan> trainingPlans, List<ExerciseLog> logs) {
        this.username = username;
        this.trainingPlans = trainingPlans;
        this.logs = logs;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<TrainingPlan> getTrainingPlans() {
        return trainingPlans;
    }

    public void setTrainingPlans(List<TrainingPlan> trainingPlans) {
        this.trainingPlans = trainingPlans;
    }

    public List<ExerciseLog> getLogs() {
        return logs;
    }

    public void setLogs(List<ExerciseLog> logs) {
        this.logs = logs;
    }
}

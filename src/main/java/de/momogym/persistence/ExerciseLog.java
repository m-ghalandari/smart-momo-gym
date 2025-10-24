package de.momogym.persistence;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "EXERCISE_LOG")
public class ExerciseLog {

    @Id
    @SequenceGenerator(name = "exercise_log_seq", sequenceName = "EXERCISE_LOG_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exercise_log_seq")
    private Long id;

    // WELCHER Athlet hat trainiert?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ATHLETE_ID", nullable = false)
    private Athlete athlete;

    // WELCHE Übung wurde gemacht?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXERCISE_ID", nullable = false)
    private Exercise exercise; // Verweis auf den Katalog

    // WANN wurde trainiert?
    @Column(name = "LOG_DATE", nullable = false)
    private LocalDate logDate;

    // WIE VIEL Gewicht? (Req 2 & 3)
    @Column(name = "WEIGHT_KG")
    private double weight; // z.B. 80.5

    @Column(name = "SETS")
    private int sets; // z.B. 3

    @Column(name = "REPS")
    private int reps; // z.B. 8 (ggf. als String "8, 8, 7" speichern, je nach Bedarf)

    public ExerciseLog() {
    }

    public ExerciseLog(Athlete athlete, Exercise exercise, LocalDate logDate, double weight, int sets, int reps) {
        this.athlete = athlete;
        this.exercise = exercise;
        this.logDate = logDate;
        this.weight = weight;
        this.sets = sets;
        this.reps = reps;
    }

    public Long getId() {
        return id;
    }

    public Athlete getAthlete() {
        return athlete;
    }

    public void setAthlete(Athlete athlete) {
        this.athlete = athlete;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDate logDate) {
        this.logDate = logDate;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }
}

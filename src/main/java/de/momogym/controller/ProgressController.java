package de.momogym.controller;

import de.momogym.persistence.Exercise;
import de.momogym.persistence.ExerciseLog;
import de.momogym.persistence.PlannedExercise;
import de.momogym.persistence.TrainingDay;
import de.momogym.persistence.TrainingPlan;
import de.momogym.services.TrainingPlanService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Named("progressController")
@ViewScoped
public class ProgressController implements Serializable {

	@Inject
	private TrainingPlanService trainingPlanService;

	private Long planId;
	private TrainingPlan trainingPlan;
	private List<Exercise> planExercises; // Für das Dropdown-Menü
	private Long selectedExerciseId;      // Die ausgewählte Übung

	// Strings für das Chart.js JavaScript
	private String chartLabels = "[]";
	private String chartData = "[]";
	private boolean hasData = false;

	@PostConstruct
	public void init() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String idParam = params.get("planId");

		if (idParam != null) {
			this.planId = Long.valueOf(idParam);
			this.trainingPlan = trainingPlanService.findTrainingPlanByIdWithDaysAndExercises(planId);

			// Alle einzigartigen Übungen dieses Plans für das Dropdown filtern
			Set<Exercise> uniqueExercises = new HashSet<>();
			for (TrainingDay day : trainingPlan.getTrainingDays()) {
				for (PlannedExercise pe : day.getPlannedExercises()) {
					uniqueExercises.add(pe.getExercise());
				}
			}
			this.planExercises = new ArrayList<>(uniqueExercises);
		}
	}

	// Wird aufgerufen, wenn man im Dropdown eine Übung auswählt
	public void loadChartData() {
		if (selectedExerciseId == null) {
			hasData = false;
			return;
		}

		List<ExerciseLog> logs = trainingPlanService.getLogsForExerciseInPlan(planId, selectedExerciseId);

		if (logs.isEmpty()) {
			hasData = false;
			chartLabels = "[]";
			chartData = "[]";
			return;
		}

		hasData = true;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.");

		// Formatiert die Daten für JavaScript: ['01.10.', '03.10.', '05.10.']
		chartLabels = "[" + logs.stream()
			.map(l -> "'" + l.getLogDate().format(formatter) + "'")
			.collect(Collectors.joining(",")) + "]";

		// Formatiert die Gewichte für JavaScript: [80.0, 82.5, 85.0]
		chartData = "[" + logs.stream()
			.map(l -> String.valueOf(l.getWeight()))
			.collect(Collectors.joining(",")) + "]";
	}

	// --- GETTER & SETTER ---
	public Long getPlanId() { return planId; }
	public TrainingPlan getTrainingPlan() { return trainingPlan; }
	public List<Exercise> getPlanExercises() { return planExercises; }
	public Long getSelectedExerciseId() { return selectedExerciseId; }
	public void setSelectedExerciseId(Long selectedExerciseId) { this.selectedExerciseId = selectedExerciseId; }
	public String getChartLabels() { return chartLabels; }
	public String getChartData() { return chartData; }
	public boolean isHasData() { return hasData; }
}

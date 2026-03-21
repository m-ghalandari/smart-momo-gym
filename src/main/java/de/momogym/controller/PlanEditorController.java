package de.momogym.controller;

import de.momogym.persistence.Exercise;
import de.momogym.persistence.PlannedExercise;
import de.momogym.persistence.TrainingPlan;
import de.momogym.services.ExerciseService;
import de.momogym.services.TrainingPlanService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named("planEditorController")
@ViewScoped
public class PlanEditorController implements Serializable {

	@Inject
	private TrainingPlanService trainingPlanService;

	@Inject
	private ExerciseService exerciseService;

	private Long planId;
	private TrainingPlan trainingPlan;
	private List<Exercise> availableExercises;

	private Map<Long, ExerciseInput> inputsPerDay = new HashMap<>();
	private Long activeDayId;

	private PlannedExercise exerciseToEdit;

	@PostConstruct
	public void init() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		// 1. Plan ID laden
		String planIdParam = params.get("planId");
		String dayIdParam = params.get("dayId");

		if (planIdParam != null) {
			this.planId = Long.valueOf(planIdParam);
			loadTrainingPlan();
		}

		// 2. Alle Übungen laden (für Dropdown)
		this.availableExercises = exerciseService.findAllExercises();
		if (this.trainingPlan != null && !this.trainingPlan.getTrainingDays().isEmpty()) {
			if (dayIdParam != null) {
				this.activeDayId = Long.valueOf(dayIdParam);
			} else {
				this.activeDayId = this.trainingPlan.getTrainingDays().get(0).getId();
			}
		}
	}

	private void loadTrainingPlan() {
		this.trainingPlan = trainingPlanService.findTrainingPlanByIdWithDaysAndExercises(planId);
	}

	public void prepareEdit(PlannedExercise exerciseToEdit) {
		this.exerciseToEdit = exerciseToEdit;
	}

	public void saveEdit() {
		if (exerciseToEdit != null) {
			trainingPlanService.updatePlannedExercise(
				exerciseToEdit.getId(),
				exerciseToEdit.getSets(),
				exerciseToEdit.getReps(),
				exerciseToEdit.getWeight()
			);

			this.exerciseToEdit = null;
			loadTrainingPlan();
			addMessage(FacesMessage.SEVERITY_INFO, "Gespeichert", "Änderungen übernommen.");
		}
	}

	public void cancelEdit() {
		this.exerciseToEdit = null;
		loadTrainingPlan();
	}

	public PlannedExercise getExerciseToEdit() {
		return exerciseToEdit;
	}
	/**
	 * Diese Methode wird von der View aufgerufen, um das Eingabe-Objekt für einen bestimmten Tag zu bekommen.
	 * Wenn es noch keins gibt, erstellen wir eins.
	 */
	public ExerciseInput getInputForDay(Long dayId) {
		inputsPerDay.putIfAbsent(dayId, new ExerciseInput());
		return inputsPerDay.get(dayId);
	}

	public void addExercise(Long dayId) {
		try {
			// Daten aus dem spezifischen Input-Objekt holen
			ExerciseInput input = inputsPerDay.get(dayId);

			if (input == null || input.getExerciseId() == null) {
				addMessage(FacesMessage.SEVERITY_ERROR, "Bitte eine Übung wählen.", "Übung auswählen!");
				return;
			}

			trainingPlanService.addExerciseToTrainingDay(dayId, input.getExerciseId(), input.getSets(), input.getReps(), input.getWeight());

			// Ansicht aktualisieren
			loadTrainingPlan();

			// Eingabefelder für diesen Tag zurücksetzen
			inputsPerDay.put(dayId, new ExerciseInput());

			addMessage(FacesMessage.SEVERITY_INFO, "Erfolg", "Übung hinzugefügt.");

		} catch (Exception e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Fehler", e.getMessage());
		}
	}

	public void removeExercise(Long plannedExerciseId) {
		try {
			System.out.println( "Removing exercise " + plannedExerciseId);
			trainingPlanService.removePlannedExercise(plannedExerciseId);
			loadTrainingPlan();
			addMessage(FacesMessage.SEVERITY_INFO, "Gelöscht", "Übung wurde entfernt.");
		} catch (Exception e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Fehler", e.getMessage());
		}
	}

	public void toggleActiveStatus(){
		boolean currentStatus = trainingPlan.isActive();

		trainingPlanService.updatePlanStatus(planId, currentStatus);

		String statusText = currentStatus ? "aktiviert" : "deaktiviert";
		addMessage(FacesMessage.SEVERITY_INFO, "Status geändert", "Plan wurde " + statusText);
	}

	public void deleteTrainingDay(Long dayId){
		trainingPlanService.deleteTrainingDay(dayId);

		loadTrainingPlan();

		if (dayId.equals(this.activeDayId)) {
			if (!this.trainingPlan.getTrainingDays().isEmpty()) {
				this.activeDayId = this.trainingPlan.getTrainingDays().get(0).getId();
			} else {
				this.activeDayId = null;
			}
		}

		addMessage(FacesMessage.SEVERITY_INFO, "Gelöscht", "Tag wurde entfernt.");
	}

	private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
	}
	public Long getActiveDayId() {
		return activeDayId;
	}

	public void setActiveDayId(Long activeDayId) {
		this.activeDayId = activeDayId;
	}

	// Methode zum Umschalten per Klick (AJAX)
	public void switchDay(Long dayId) {
		this.activeDayId = dayId;
	}

	// --- Getter ---
	public TrainingPlan getTrainingPlan() { return trainingPlan; }
	public List<Exercise> getAvailableExercises() { return availableExercises; }

	public Long getPlanId() { return planId; }

	// --- Innere Klasse für die Formulardaten (DTO) ---
	public static class ExerciseInput implements Serializable {
		private Long exerciseId;
		private int sets = 3;    // Standardwert
		private String reps = "8-12"; // Standardwert
		private Double weight = 0.0;

		// Getter und Setter
		public Long getExerciseId() { return exerciseId; }
		public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }
		public int getSets() { return sets; }
		public void setSets(int sets) { this.sets = sets; }
		public String getReps() { return reps; }
		public void setReps(String reps) { this.reps = reps; }
		public Double getWeight() { return weight; }
		public void setWeight(Double weight) { this.weight = weight; }
	}
}
package de.momogym.controller;

import de.momogym.dto.WorkoutExerciseDTO;
import de.momogym.persistence.ExerciseLog;
import de.momogym.persistence.PlannedExercise;
import de.momogym.persistence.TrainingDay;
import de.momogym.services.TrainingPlanService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named("workoutSessionController")
@ViewScoped
public class WorkoutSessionController implements Serializable {

	@Inject
	private TrainingPlanService trainingPlanService;

	private TrainingDay trainingDay;
	private Long planId; // Um beim "Abbrechen" zurück navigieren zu können

	private List<WorkoutExerciseDTO> pendingExercises;
	private List<WorkoutExerciseDTO> completedExercises;

	@PostConstruct
	public void init(){
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String planIdParam = params.get("planId");
		String dayIdParam = params.get("dayId");

		if(planIdParam != null) {
			this.planId = Long.valueOf(planIdParam);
		}

		if(dayIdParam != null && planIdParam != null) {
			this.trainingDay = trainingPlanService.findTrainingDayWithExercises(Long.valueOf(dayIdParam));
			this.pendingExercises = new ArrayList<>();
			this.completedExercises = new ArrayList<>();

			List<ExerciseLog> todaysLogs = trainingPlanService.getLogsForTodayAndPlan(this.planId);

			Map<Long, ExerciseLog> logMap = todaysLogs.stream()
				.collect(Collectors.toMap(log -> log.getExercise().getId(), log -> log));

			for (PlannedExercise pe : trainingDay.getPlannedExercises()) {
				WorkoutExerciseDTO dto = new WorkoutExerciseDTO(pe);

				if (logMap.containsKey(pe.getExercise().getId())) {
					ExerciseLog log = logMap.get(pe.getExercise().getId());
					dto.setActualSets(log.getSets());
					dto.setActualReps(log.getReps());
					dto.setActualWeight(log.getWeight());
					completedExercises.add(dto);
				} else {
					pendingExercises.add(dto);
				}
			}
		}
	}

	public void markAsDone(WorkoutExerciseDTO exercise) {
		pendingExercises.remove(exercise);
		trainingPlanService.logWorkoutExercise(
			this.planId,
			exercise.getPlannedExercise().getExercise().getId(),
			exercise.getActualSets(),
			exercise.getActualReps(),
			exercise.getActualWeight()
		);

		pendingExercises.remove(exercise);
		completedExercises.add(exercise);
	}

	public String finishWorkout(){
		return "planEditor?faces-redirect=true&planId=" + this.planId + "&dayId=" + this.trainingDay.getId();
	}

	public TrainingDay getTrainingDay() {
		return trainingDay;
	}

	public Long getPlanId() {
		return planId;
	}

	public List<WorkoutExerciseDTO> getPendingExercises() {
		return pendingExercises;
	}
	public List<WorkoutExerciseDTO> getCompletedExercises() {
		return completedExercises;
	}
}

package de.momogym.controller;

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

@Named("workoutSessionController")
@ViewScoped
public class WorkoutSessionController implements Serializable {

	@Inject
	private TrainingPlanService trainingPlanService;

	private TrainingDay trainingDay;
	private Long planId; // Um beim "Abbrechen" zurück navigieren zu können

	private List<PlannedExercise> pendingExercises;

	@PostConstruct
	public void init(){
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String planIdParam = params.get("planId");
		String dayIdParam = params.get("dayId");

		if(dayIdParam != null) {
			this.trainingDay = trainingPlanService.findTrainingDayWithExercises(Long.valueOf(dayIdParam));
			this.pendingExercises = new ArrayList<>(this.trainingDay.getPlannedExercises());
		}
		if(planIdParam != null) {
			this.planId = Long.valueOf(planIdParam);
		}
	}

	public void markAsDone(PlannedExercise exercise) {
		pendingExercises.remove(exercise);
		// Später: Hier rufen wir den Service auf, um das Log in der DB zu speichern!
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

	public List<PlannedExercise> getPendingExercises() {
		return pendingExercises;
	}
}

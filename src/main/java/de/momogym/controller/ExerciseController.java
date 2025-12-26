package de.momogym.controller;

import de.momogym.exceptions.EntityAlreadyExistsException;
import de.momogym.persistence.Exercise;
import de.momogym.services.ExerciseService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named("exerciseController")
@ViewScoped
public class ExerciseController implements Serializable {

	@Inject
	private ExerciseService exerciseService;

	// The controller needs a list of all exercises (for the table) and fields for a new exercise.
	private List<Exercise> allExercises;
	private Exercise newExercise;

	@PostConstruct
	public void init() {
		this.allExercises = exerciseService.findAllExercises();
		this.newExercise = new Exercise();
	}

	public void createExercise(){
		try {
			this.exerciseService.createExercise(newExercise);
			addMessage(FacesMessage.SEVERITY_INFO, "Erfolg", "Neues Training erfolgreich angelegt.");
			init();

		} catch (EntityAlreadyExistsException e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Fehler", e.getMessage());
		}
	}

	public List<Exercise> getAllExercises() {
		return this.allExercises;
	}

	public Exercise getNewExercise() {
		return newExercise;
	}

	public void setNewExercise(Exercise newExercise) {
		this.newExercise = newExercise;
	}

	/**
	 * Hilfsmethode zum Anzeigen von Nachrichten in JSF
	 */
	private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
		FacesContext.getCurrentInstance()
			.addMessage(null, new FacesMessage(severity, summary, detail));
	}


}

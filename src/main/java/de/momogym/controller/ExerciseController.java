package de.momogym.controller;

import de.momogym.auth.UserSession;
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

	@Inject
	private UserSession userSession;

	private List<Exercise> allExercises;
	private Exercise newExercise;

	private Exercise exerciseToEdit;

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

	public void prepareEdit(Exercise exercise) {
		this.exerciseToEdit = exercise;
	}

	public void cancelEdit() {
		this.exerciseToEdit = null;
		init();
	}

	public void saveEdit() {
		if (!userSession.getLoggedInAthlete().isAdmin()) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Nur Admins dürfen Übungen bearbeiten!");
			return;
		}
		try {
			exerciseService.updateExercise(exerciseToEdit);
			addMessage(FacesMessage.SEVERITY_INFO, "Aktualisiert", "Übung erfolgreich bearbeitet.");
			this.exerciseToEdit = null;
			init();
		} catch (EntityAlreadyExistsException e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Fehler", e.getMessage());
		}
	}

	public void deleteExercise(Long exerciseId){
		if(!userSession.getLoggedInAthlete().isAdmin()){
			addMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Nur Admins dürfen Übungen löschen!");
			return;
		}
		try {
			this.exerciseService.deleteExercise(exerciseId);
			addMessage(FacesMessage.SEVERITY_INFO, "Gelöscht", "Die Übung ist erfolgreich gelöscht.");
			init();
		} catch (Exception e) {
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

	public Exercise getExerciseToEdit() { return exerciseToEdit; }
	public void setExerciseToEdit(Exercise exerciseToEdit) { this.exerciseToEdit = exerciseToEdit; }

	/**
	 * Hilfsmethode zum Anzeigen von Nachrichten in JSF
	 */
	private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
		FacesContext.getCurrentInstance()
			.addMessage(null, new FacesMessage(severity, summary, detail));
	}


}

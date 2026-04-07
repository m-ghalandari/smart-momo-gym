package de.momogym.controller;

import de.momogym.auth.UserSession;
import de.momogym.persistence.Athlete;
import de.momogym.persistence.TrainingDay;
import de.momogym.persistence.TrainingPlan;
import de.momogym.services.AthleteService;
import de.momogym.services.TrainingPlanService;
import jakarta.faces.view.ViewScoped; // Wichtig: Hält die Daten, solange man auf der Seite ist
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import jakarta.annotation.PostConstruct; // Für das Laden der Daten beim Seitenaufruf
import jakarta.faces.context.FacesContext;
import jakarta.faces.application.FacesMessage;
import jakarta.servlet.http.Part;

@Named("athleteDetailController")
@ViewScoped
public class AthleteDetailController implements Serializable {

	@Inject
	private AthleteService athleteService;

	@Inject
	private TrainingPlanService trainingPlanService;

	private Athlete athlete;

	@Inject
	private UserSession userSession;

	private TrainingPlan planToEdit;
	private String newPlanName;
	private List<String> selectedNewDays;
	private List<String> missingDays;

	private final List<String> allWeekDays = List.of("Montag", "Dienstag", "Mittwoch", "Donnerstag",
		"Freitag", "Samstag", "Sonntag");

	private String croppedImageBase64;

	/**
	 * Diese Methode wird direkt nach Erstellung der Bean aufgerufen (@PostConstruct). Sie liest den
	 * 'athleteId'-Parameter aus der URL und lädt die Daten.
	 */
	@PostConstruct
	public void init() {
		// Den 'athleteId' Parameter aus der URL lesen (den AthleteSearchController gesendet hat)
		String idParam = FacesContext.getCurrentInstance().getExternalContext()
			.getRequestParameterMap().get("athleteId");

		if (idParam != null && !idParam.isEmpty()) {
			try {
				refreshAthlete(Long.parseLong(idParam));
			} catch (NumberFormatException e) {
				addErrorMessage("Ungültige Athleten-ID in der URL.");
			}
		}
	}

	public void toggleVisibility() {
		if (athlete != null && userSession.isLoggedIn() && athlete.getId().equals(userSession.getLoggedInAthlete().getId())) {
			athleteService.updateVisibility(athlete.getId(), athlete.isProfilePublic());
		}
	}

	private void refreshAthlete(long l) {
		this.athlete = athleteService.findAthleteByIdWithPlans(l);

		if (this.athlete != null && this.athlete.getTrainingPlans() != null) {
			this.athlete.getTrainingPlans().sort((p1, p2) -> {
				int activeCompare = Boolean.compare(p2.isActive(), p1.isActive());
				if (activeCompare != 0)
					return activeCompare;
				return p1.getName().compareTo(p2.getName());
			});
		}
	}

	public void prepareEditPlan(TrainingPlan plan) {
		this.planToEdit = plan;
		this.newPlanName = plan.getName();
		this.selectedNewDays = new ArrayList<>();

		List<String> existingDays = plan.getTrainingDays().stream().map(TrainingDay::getName)
			.toList();

		this.missingDays = allWeekDays.stream().filter(day -> !existingDays.contains(day)).toList();
	}

	public void savePlanChanges() {
		try {
			trainingPlanService.updatePlanName(planToEdit.getId(), newPlanName);
			if (!selectedNewDays.isEmpty()) {
				trainingPlanService.addDaysToPlan(planToEdit.getId(), selectedNewDays);
			}
			addMessage(FacesMessage.SEVERITY_INFO, "Erfolg", "Plan wurde aktualisiert.");
			refreshAthlete(athlete.getId());
			this.planToEdit = null;
		} catch (Exception e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Fehler", e.getMessage());
		}
	}

	public void cancelEdit() {
		this.planToEdit = null;
	}

	public String deletePlan(Long planId) {
		try {
			trainingPlanService.deleteTrainingPlan(planId);

			addMessage(FacesMessage.SEVERITY_INFO, "Erfolg", "Plan wurde gelöscht.");

			// WICHTIG: Athlet neu laden, damit der gelöschte Plan aus der Liste verschwindet!
			//this.athlete = athleteService.findAthleteByIdWithPlans(this.athlete.getId());
			refreshAthlete(athlete.getId());

		} catch (Exception e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Fehler",
				"Löschen fehlgeschlagen: " + e.getMessage());
		}
		return null; // Bleibt auf der gleichen Seite
	}

	public String deleteAccount() {
		if (athlete != null && userSession.isLoggedIn() && athlete.getId().equals(userSession.getLoggedInAthlete().getId())) {

			athleteService.deleteAthlete(athlete.getId());

			return userSession.logout();
		}
		return null;
	}

	private void addErrorMessage(String detail) {
		FacesContext.getCurrentInstance()
			.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", detail));
	}

	// Getter für JSF, um auf den Athleten zuzugreifen
	public Athlete getAthlete() {
		return athlete;
	}

	private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
		FacesContext.getCurrentInstance()
			.addMessage(null, new FacesMessage(severity, summary, detail));
	}

	public TrainingPlan getPlanToEdit() {
		return planToEdit;
	}

	public String getNewPlanName() {
		return newPlanName;
	}

	public void setNewPlanName(String newPlanName) {
		this.newPlanName = newPlanName;
	}

	public List<String> getSelectedNewDays() {
		return selectedNewDays;
	}

	public void setSelectedNewDays(List<String> selectedNewDays) {
		this.selectedNewDays = selectedNewDays;
	}

	public List<String> getMissingDays() {
		return missingDays;
	}

	public String getCroppedImageBase64() { return croppedImageBase64; }
	public void setCroppedImageBase64(String croppedImageBase64) { this.croppedImageBase64 = croppedImageBase64; }

	public void uploadProfilePicture() {
		if (croppedImageBase64 != null && !croppedImageBase64.isEmpty()) {
			try {
				// Der String sieht so aus: "data:image/jpeg;base64,/9j/4AAQ..."
				// Wir schneiden den vorderen Teil ab und decodieren den Rest
				String base64Data = croppedImageBase64.split(",")[1];
				byte[] bytes = Base64.getDecoder().decode(base64Data);

				athlete.setProfilePicture(bytes);
				athleteService.updateAthlete(athlete);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}

package de.momogym.controller;

import de.momogym.persistence.Athlete;
import de.momogym.services.AthleteService;
import de.momogym.services.TrainingPlanService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Named("planController")
@ViewScoped
public class TrainingPlanController implements Serializable {

	@Inject
	private AthleteService athleteService;

	@Inject
	private TrainingPlanService trainingPlanService;

	private Long athleteId; // Die ID aus der URL
	private Athlete athlete; // Zur Anzeige (z.B. "Plan erstellen für Momo")

	// Formular-Daten
	private String planName;
	private boolean active = false;

	// NEU: Auswahl der Tage
	private List<String> selectedDays = new ArrayList<>(); // Die Auswahl des Users

	// Die Optionen zur Auswahl (Getter wird benötigt)
	private final List<String> availableDays = Arrays.asList(
		"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"
	);

	@PostConstruct
	public void init() {
		// Wir holen die ID aus der URL (z.B. ttrainingPlan.xhtml?athleteId=5)
		String idParam = FacesContext.getCurrentInstance().getExternalContext()
			.getRequestParameterMap().get("athleteId");

		if (idParam != null) {
			this.athleteId = Long.valueOf(idParam);
			this.athlete = athleteService.findAthleteByIdWithPlans(athleteId);
		}
	}

	public String save() {
		try {
			trainingPlanService.createTrainingPlan(athleteId, planName, active, selectedDays);

			// Nach Erfolg: Zurück zur Detailseite des Athleten leiten!
			// faces-redirect=true sorgt für eine saubere URL-Änderung.
			return "athleteDetail.xhtml?faces-redirect=true&athleteId=" + athleteId;

		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", e.getMessage()));
			return null; // Auf der Seite bleiben bei Fehler
		}
	}

	// Wichtig für die Checkbox-Liste in der View
	public List<String> getAvailableDays() {
		return availableDays;
	}

	public List<String> getSelectedDays() {
		return selectedDays;
	}

	public void setSelectedDays(List<String> selectedDays) {
		this.selectedDays = selectedDays;
	}

	// --- Getter und Setter ---
	public Long getAthleteId() { return athleteId; }
	public void setAthleteId(Long athleteId) { this.athleteId = athleteId; }

	public Athlete getAthlete() { return athlete; }

	public String getPlanName() { return planName; }
	public void setPlanName(String planName) { this.planName = planName; }

	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }
}
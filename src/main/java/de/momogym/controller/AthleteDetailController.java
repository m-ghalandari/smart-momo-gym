package de.momogym.controller;
import de.momogym.persistence.Athlete;
import de.momogym.services.AthleteService;
import de.momogym.services.TrainingPlanService;
import jakarta.faces.view.ViewScoped; // Wichtig: Hält die Daten, solange man auf der Seite ist
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import jakarta.annotation.PostConstruct; // Für das Laden der Daten beim Seitenaufruf
import jakarta.faces.context.FacesContext;
import jakarta.faces.application.FacesMessage;

@Named("athleteDetailController")
@ViewScoped // Hält den 'athlete', solange die Detailseite offen ist
public class AthleteDetailController implements Serializable {

    @Inject
    private AthleteService athleteService;

	@Inject
    private TrainingPlanService trainingPlanService;

    private Athlete athlete; // Der Athlet, den wir anzeigen



    /**
     * Diese Methode wird direkt nach Erstellung der Bean aufgerufen (@PostConstruct).
     * Sie liest den 'athleteId'-Parameter aus der URL und lädt die Daten.
     */
    @PostConstruct
    public void init() {
        // Den 'athleteId' Parameter aus der URL lesen (den AthleteSearchController gesendet hat)
        String idParam = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap().get("athleteId");

        if (idParam != null && !idParam.isEmpty()) {
            try {
                Long athleteId = Long.parseLong(idParam);

                // Athleten (inkl. Pläne) über den Service laden
                this.athlete = athleteService.findAthleteByIdWithPlans(athleteId);

                if (this.athlete == null) {
                    addErrorMessage("Athlet nicht gefunden.");
                }
            } catch (NumberFormatException e) {
                addErrorMessage("Ungültige Athleten-ID in der URL.");
            }
        }
    }

	public String deletePlan(Long planId) {
		try {
			trainingPlanService.deleteTrainingPlan(planId);

			addMessage(FacesMessage.SEVERITY_INFO, "Erfolg", "Plan wurde gelöscht.");

			// WICHTIG: Athlet neu laden, damit der gelöschte Plan aus der Liste verschwindet!
			this.athlete = athleteService.findAthleteByIdWithPlans(this.athlete.getId());

		} catch (Exception e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Löschen fehlgeschlagen: " + e.getMessage());
		}
		return null; // Bleibt auf der gleichen Seite
	}

    private void addErrorMessage(String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", detail));
    }

    // Getter für JSF, um auf den Athleten zuzugreifen
    public Athlete getAthlete() {
        return athlete;
    }


    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, summary, detail));
    }

}

package de.momogym.controller;
import de.momogym.persistence.Athlete;
import de.momogym.services.AthleteService;
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

    private Athlete athlete; // Der Athlet, den wir anzeigen

    private String newPlanName;
    private boolean newPlanIsActive = false; // Standardmäßig nicht aktiv



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

    private void addErrorMessage(String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", detail));
    }

    // Getter für JSF, um auf den Athleten zuzugreifen
    public Athlete getAthlete() {
        return athlete;
    }

    /**
     * Wird vom "Erstellen"-Button auf der Detailseite aufgerufen.
     */
    public String createNewPlan() {
        if (athlete == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Kein Athlet geladen.");
            return null;
        }

        try {
            // Service-Logik aufrufen
            athleteService.createTrainingPlan(athlete.getId(), newPlanName, newPlanIsActive);

            addMessage(FacesMessage.SEVERITY_INFO, "Erfolg", "Plan '" + newPlanName + "' erstellt.");

            // WICHTIG: Athleten (und seine Pläne) neu laden, damit die Liste aktuell ist!
            this.athlete = athleteService.findAthleteByIdWithPlans(athlete.getId());

            // Formularfelder zurücksetzen
            this.newPlanName = null;
            this.newPlanIsActive = false;

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Fehler", e.getMessage());
        }

        // Auf der Seite bleiben (null returned)
        return null;
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, summary, detail));
    }

    // --- NEUE GETTER/SETTER (für das Formular) ---
    public String getNewPlanName() {
        return newPlanName;
    }

    public void setNewPlanName(String newPlanName) {
        this.newPlanName = newPlanName;
    }

    public boolean isNewPlanIsActive() {
        return newPlanIsActive;
    }

    public void setNewPlanIsActive(boolean newPlanIsActive) {
        this.newPlanIsActive = newPlanIsActive;
    }
}

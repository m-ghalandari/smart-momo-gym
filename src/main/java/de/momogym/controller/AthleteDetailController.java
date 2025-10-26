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
}

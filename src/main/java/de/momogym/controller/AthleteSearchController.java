package de.momogym.controller;

import de.momogym.persistence.Athlete;
import de.momogym.services.AthleteService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("athleteSearchController")
@RequestScoped
public class AthleteSearchController {

    @Inject
    private AthleteService athleteService;

    private String username; // Gebunden an das JSF-Eingabefeld

    /**
     * Wird vom Such-Button aufgerufen.
     * Sucht den Athleten und leitet zur Detailseite weiter.
     */
    public String search() {
        Athlete athlete = athleteService.findAthleteWithPlans(username);

        if (athlete != null) {
            // Erfolg: Zur Detailseite weiterleiten und die ID als Parameter übergeben.
            // Der AthleteDetailController (siehe unten) fängt diese ID ab.
            return "athleteDetail.xhtml?faces-redirect=true&athleteId=" + athlete.getId();

        } else {
            // Fehler: Nachricht anzeigen und auf der Suchseite bleiben.
            addMessage(FacesMessage.SEVERITY_WARN, "Nicht gefunden", "Kein Athlet mit dem Namen '" + username + "' gefunden.");
            return null; // Bleibe auf der aktuellen Seite (athleteSearch.xhtml)
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, summary, detail));
    }

    // Getter & Setter für username (von JSF benötigt)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

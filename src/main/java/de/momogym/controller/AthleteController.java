package de.momogym.controller;

import de.momogym.services.AthleteService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("athleteController")
@RequestScoped
public class AthleteController {

    @Inject
    private AthleteService athleteService;

    private String username;

    public String register() {
        try {
            // Ruft die Logik im Service auf
            athleteService.createAthlete(username);

            // Erfolg an den Benutzer melden
            addMessage(FacesMessage.SEVERITY_INFO, "Erfolg", "Athlet '" + username + "' wurde erstellt.");

            // Leert das Feld nach Erfolg
            this.username = null;

            // Bleibe auf derselben Seite (oder navigiere zu "login.xhtml")
            return "index.xhtml?faces-redirect=true"; // Redirect, um Formular-Neusenden zu verhindern

        } catch (Exception e) {
            // Fehler an den Benutzer melden
            addMessage(FacesMessage.SEVERITY_ERROR, "Fehler", e.getMessage());

            // Bleibe auf der Registrierungsseite, um den Fehler anzuzeigen
            return null;
        }
    }

    /**
     * Hilfsmethode zum Anzeigen von Nachrichten in JSF
     */
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

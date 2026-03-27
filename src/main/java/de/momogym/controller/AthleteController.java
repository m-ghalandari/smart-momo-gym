package de.momogym.controller;

import de.momogym.services.AthleteService;
import de.momogym.services.LoginService;
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

	private String password;

    public String register() {
        try {
            String hashPassword = LoginService.hashPassword(password);
            athleteService.createAthlete(username, hashPassword);

            addMessage(FacesMessage.SEVERITY_INFO, "Erfolg", "Athlet '" + username + "' wurde erstellt.");

            this.username = null;

            return "index.xhtml?faces-redirect=true";

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Fehler", e.getMessage());

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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}

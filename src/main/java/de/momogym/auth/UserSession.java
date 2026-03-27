package de.momogym.auth;

import de.momogym.persistence.Athlete;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.Serializable;

@Named(value = "userSession")
@SessionScoped
public class UserSession implements Serializable {

	private Athlete loggedInAthlete;

	public Athlete getLoggedInAthlete() {
		return loggedInAthlete;
	}

	public void setLoggedInAthlete(Athlete loggedInAthlete) {
		this.loggedInAthlete = loggedInAthlete;
	}

	public boolean isLoggedIn() {
		return loggedInAthlete != null;
	}

	public String logout() {
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		this.loggedInAthlete = null;
		return "/login.xhtml?faces-redirect=true";
	}
}

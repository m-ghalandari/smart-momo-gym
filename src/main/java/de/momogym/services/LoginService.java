package de.momogym.services;

import de.momogym.auth.UserSession;
import de.momogym.persistence.Athlete;
import jakarta.ejb.Stateless;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Stateless
public class LoginService {

	@Inject
	private UserSession userSession;

	@PersistenceContext(unitName = "trainingsverwaltung-pu")
	private EntityManager em;

	public String login(String username, String password) {
		try {
			String hashedPw = hashPassword(password);

			Athlete athlete = em.createQuery(
					"SELECT a FROM Athlete a WHERE a.username = :username AND a.password = :password", Athlete.class)
				.setParameter("username", username)
				.setParameter("password", hashedPw)
				.getSingleResult();

			userSession.setLoggedInAthlete(athlete);
			return "/index.xhtml?faces-redirect=true";

		} catch (NoResultException e) {
			FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_ERROR, "Falscher Benutzername oder Passwort.", null));
			return null;
		}
	}

	public static String hashPassword(String plainTextPassword) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hash = md.digest(plainTextPassword.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(hash);
		} catch (Exception e) {
			throw new RuntimeException("Fehler beim Hashen des Passworts", e);
		}
	}
}

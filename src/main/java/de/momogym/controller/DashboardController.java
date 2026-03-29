package de.momogym.controller;

import de.momogym.auth.UserSession;
import de.momogym.persistence.Athlete;
import de.momogym.services.AthleteService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;

@Named("dashboardController")
@RequestScoped
public class DashboardController {

	@Inject
	private AthleteService athleteService;

	@Inject
	private UserSession userSession;

	private List<Athlete> allAthletes;

	@PostConstruct
	public void init(){
		Long loggedInId = userSession.isLoggedIn() ? userSession.getLoggedInAthlete().getId() : -1L;
		this.allAthletes = athleteService.findAllAthletes(loggedInId);
		if (userSession.isLoggedIn()) {
			//Long loggedInId = userSession.getLoggedInAthlete().getId();
			Athlete loggedInAthlete = null;

			for (int i = 0; i < allAthletes.size(); i++) {
				if (allAthletes.get(i).getId().equals(loggedInId)) {
					loggedInAthlete = allAthletes.remove(i);
					break;
				}
			}

			if (loggedInAthlete != null) {
				allAthletes.add(0, loggedInAthlete);
			}
		}
	}

	public List<Athlete> getAllAthletes(){
		return this.allAthletes;
	}

	public String deleteAthlete(Long id){
		athleteService.deleteAthlete(id);
		if (userSession.isLoggedIn() && userSession.getLoggedInAthlete().getId().equals(id)) {
			return userSession.logout();
		}
		init();
		return null;
	}
}

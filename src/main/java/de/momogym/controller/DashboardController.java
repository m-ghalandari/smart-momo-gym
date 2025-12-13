package de.momogym.controller;

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

	private List<Athlete> allAthletes;

	@PostConstruct
	public void init(){
		this.allAthletes = athleteService.findAllAthletes();
	}

	public List<Athlete> getAllAthletes(){
		return this.allAthletes;
	}

	public String deleteAthlete(Long id){
		athleteService.deleteAthlete(id);
		init();
		return null;
	}
}

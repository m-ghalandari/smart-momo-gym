package de.momogym.services;

import de.momogym.persistence.Athlete;
import de.momogym.persistence.Exercise;
import de.momogym.persistence.ExerciseLog;
import de.momogym.persistence.PlannedExercise;
import de.momogym.persistence.PlannedSet;
import de.momogym.persistence.TrainingDay;
import de.momogym.persistence.TrainingPlan;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Subgraph;
import jakarta.persistence.TypedQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class TrainingPlanService {

	@PersistenceContext(unitName = "trainingsverwaltung-pu")
	private EntityManager entityManager;

	/**
	 * Erstellt einen neuen, leeren Trainingsplan für einen Athleten
	 */
	public TrainingPlan createTrainingPlan(Long athleteId, String planName, boolean makeActive, List<String> selectedDays) throws Exception {

		// Schritt 1: Den Besitzer (Athleten) laden (JETZT VOR DER PRÜFUNG)
		Athlete athlete = entityManager.find(Athlete.class, athleteId);
		if (athlete == null) {
			throw new Exception("Athlet mit ID " + athleteId + " nicht gefunden.");
		}

		// Schritt 2: Angepasste Prüfung (jetzt mit Athlete-Objekt)
		if (planNameExistsForAthlete(planName, athlete)) { // ANPASSUNG
			throw new Exception("Der Plan-Name '" + planName + "' ist für DIESEN Athleten bereits vergeben."); // ANPASSUNG
		}

		// Schritt 3: Neuen Plan erstellen
		TrainingPlan newPlan = new TrainingPlan();
		newPlan.setAthlete(athlete);
		newPlan.setName(planName);
		newPlan.setActive(makeActive);
		newPlan.setCurrentDaySequence(1);
		entityManager.persist(newPlan);

		// Schritt 4: Trainings-Tage erstellen
		if (selectedDays != null && !selectedDays.isEmpty()) {
			int sequence = 1;
			for (String dayName : selectedDays) {
				TrainingDay day = new TrainingDay();
				day.setName(dayName);
				day.setTrainingPlan(newPlan);

				entityManager.persist(day);
			}
		}

		return newPlan;
	}

	public void deleteTrainingPlan(Long planId) {
		TrainingPlan plan = entityManager.find(TrainingPlan.class, planId);

		if (plan != null) {
			entityManager.createQuery("DELETE FROM ExerciseLog l WHERE l.trainingPlan = :plan")
				.setParameter("plan", plan)
				.executeUpdate();

			entityManager.remove(plan);
		}
	}

	public void removePlannedExercise(Long exerciseId) {
		PlannedExercise exercise = entityManager.find(PlannedExercise.class, exerciseId);
		if (exercise != null) {
			entityManager.remove(exercise);
		}
	}

	public void addExerciseToTrainingDay(Long dayId, Long exerciseId, int sets, String reps, Double weight) {
		TrainingDay day = entityManager.find(TrainingDay.class, dayId);
		if (day == null) throw new IllegalArgumentException("Tag nicht gefunden");

		Exercise exercise = entityManager.find(Exercise.class, exerciseId);
		if (exercise == null) throw new IllegalArgumentException("Übung nicht gefunden");

		PlannedExercise plannedExercise = new PlannedExercise();
		plannedExercise.setTrainingDay(day);
		plannedExercise.setExercise(exercise);
		plannedExercise.setSets(sets);
		plannedExercise.setReps(reps);
		plannedExercise.setWeight(weight);

		plannedExercise.setSortOrder(day.getPlannedExercises().size() + 1);

		// Automatische Sätze (PlannedSets) generieren ---
		for (int i = 1; i <= sets; i++) {
			PlannedSet plannedSet = new PlannedSet();
			plannedSet.setPlannedExercise(plannedExercise);
			plannedSet.setSetNumber(i);
			plannedSet.setReps(reps);

			String weightStr = (weight != null) ? String.valueOf(weight) : "0";
			if (weightStr.endsWith(".0")) {
				weightStr = weightStr.substring(0, weightStr.length() - 2);
			}
			plannedSet.setWeight(weightStr);

			plannedExercise.getPlannedSets().add(plannedSet);
		}

		entityManager.persist(plannedExercise);
	}

	public void updatePlannedExercise(PlannedExercise exerciseToUpdate) {
		// 1. Die Übung aus der Datenbank laden, um sie sicher zu aktualisieren
		PlannedExercise managedExercise = entityManager.find(PlannedExercise.class, exerciseToUpdate.getId());

		if (managedExercise != null) {
			// Basiswerte aktualisieren
			managedExercise.setSets(exerciseToUpdate.getSets());
			managedExercise.setReps(exerciseToUpdate.getReps());
			managedExercise.setWeight(exerciseToUpdate.getWeight());

			List<PlannedSet> managedSets = managedExercise.getPlannedSets();
			List<PlannedSet> incomingSets = exerciseToUpdate.getPlannedSets();

			// 2. Bestehende Sätze mit den neuen Freitext-Eingaben (Wdh/Gewicht) überschreiben
			for (int i = 0; i < managedSets.size() && i < incomingSets.size(); i++) {
				managedSets.get(i).setReps(incomingSets.get(i).getReps());
				managedSets.get(i).setWeight(incomingSets.get(i).getWeight());
			}

			// 3. SYNCHRONISATION: Hat der User die Anzahl der Sätze geändert?
			int targetSets = managedExercise.getSets();

			// Fall A: Sätze wurden hinzugefügt (z.B. von 3 auf 4)
			if (managedSets.size() < targetSets) {
				for (int i = managedSets.size() + 1; i <= targetSets; i++) {
					PlannedSet newSet = new PlannedSet();
					newSet.setPlannedExercise(managedExercise);
					newSet.setSetNumber(i);

					// Wir kopieren als Standard einfach die Werte aus dem letzten Satz
					if (!managedSets.isEmpty()) {
						PlannedSet lastSet = managedSets.get(managedSets.size() - 1);
						newSet.setReps(lastSet.getReps());
						newSet.setWeight(lastSet.getWeight());
					} else {
						newSet.setReps(managedExercise.getReps());
						newSet.setWeight(String.valueOf(managedExercise.getWeight()));
					}
					managedSets.add(newSet);
				}
			}
			// Fall B: Sätze wurden reduziert (z.B. von 4 auf 2)
			else if (managedSets.size() > targetSets) {
				while (managedSets.size() > targetSets) {
					// Den letzten Satz entfernen
					managedSets.remove(managedSets.size() - 1);
				}
			}

			entityManager.merge(managedExercise);
		}
	}

	public TrainingPlan findTrainingPlanByIdWithDaysAndExercises(Long planId) {

		// Wir aktivieren den oben definierten Graphen
		EntityGraph<?> graph = entityManager.getEntityGraph("TrainingPlan.withDaysAndExercises");

		Map<String, Object> properties = new HashMap<>();
		properties.put("jakarta.persistence.fetchgraph", graph);

		return entityManager.find(TrainingPlan.class, planId, properties);
	}

	public void deleteTrainingDay(Long dayId){
		TrainingDay day = entityManager.find(TrainingDay.class, dayId);
		if (day != null) {
			TrainingPlan plan = day.getTrainingPlan();
			plan.getTrainingDays().remove(day);
			entityManager.remove(day);
		}
	}

	public void updatePlanStatus(Long planId, boolean isActive){
		TrainingPlan plan = entityManager.find(TrainingPlan.class, planId);
		if (plan != null) plan.setActive(isActive);
	}

	public void updatePlanName(Long planId, String newName) throws Exception {
		TrainingPlan plan = entityManager.find(TrainingPlan.class, planId);
		if (plan != null){
			if(!plan.getName().equals(newName) && planNameExistsForAthlete(newName, plan.getAthlete())){
				throw new Exception("Name existiert bereits.");
			}
			plan.setName(newName);
		}
	}

	public void addDaysToPlan(Long planId, List<String> daysToAdd){
		TrainingPlan plan = entityManager.find(TrainingPlan.class, planId);
		if (plan != null && daysToAdd != null && !daysToAdd.isEmpty()){
			for(String dayToAdd : daysToAdd){
				TrainingDay day = new TrainingDay();
				day.setName(dayToAdd);
				day.setTrainingPlan(plan);
				entityManager.persist(day);
				plan.getTrainingDays().add(day);
			}
		}
	}

	/**
	 * Lädt genau einen Tag inklusive seiner Übungen (Sortierung passiert automatisch durch @OrderBy in der Entity)
	 */
	public TrainingDay findTrainingDayWithExercises(Long dayId) {
		EntityGraph<TrainingDay> graph = entityManager.createEntityGraph(TrainingDay.class);

		Subgraph<PlannedExercise> peSubgraph = graph.addSubgraph("plannedExercises");

		peSubgraph.addAttributeNodes("exercise");
		peSubgraph.addAttributeNodes("plannedSets");

		Map<String, Object> properties = new HashMap<>();
		properties.put("jakarta.persistence.fetchgraph", graph);

		return entityManager.find(TrainingDay.class, dayId, properties);
	}

	public void logWorkoutExercise(Long planId, Long exerciseId, int sets, String reps, Double weight){
		TrainingPlan plan = entityManager.find(TrainingPlan.class, planId);
		Exercise exercise = entityManager.find(Exercise.class, exerciseId);

		ExerciseLog log = new ExerciseLog();
		log.setAthlete(plan.getAthlete());
		log.setTrainingPlan(plan);
		log.setExercise(exercise);
		log.setLogDate(java.time.LocalDate.now());
		log.setSets(sets);
		log.setReps(reps);
		log.setWeight(weight);

		entityManager.persist(log);
	}

	public List<ExerciseLog> getLogsForExerciseInPlan(Long planId, Long exerciseId) {
		return entityManager.createQuery(
				"SELECT l FROM ExerciseLog l " +
					"WHERE l.trainingPlan.id = :planId " +
					"AND l.exercise.id = :exerciseId " +
					"ORDER BY l.logDate ASC", ExerciseLog.class)
			.setParameter("planId", planId)
			.setParameter("exerciseId", exerciseId)
			.getResultList();
	}

	/**
	 * Prüft, ob der Plan-Name FÜR EINEN SPEZIFISCHEN Athleten existiert.
	 */
	private boolean planNameExistsForAthlete(String planName, Athlete athlete) {
		TypedQuery<Long> query = entityManager.createQuery(
			"SELECT COUNT(p) FROM TrainingPlan p WHERE p.name = :name AND p.athlete = :athlete", Long.class);

		query.setParameter("name", planName);
		query.setParameter("athlete", athlete);

		return query.getSingleResult() > 0;
	}

	public List<ExerciseLog> getLogsForTodayAndPlan(Long planId) {
		return entityManager.createQuery("SELECT l FROM ExerciseLog l WHERE l.trainingPlan.id = :planId AND l.logDate = CURRENT_DATE", ExerciseLog.class)
			.setParameter("planId", planId)
			.getResultList();
	}
}

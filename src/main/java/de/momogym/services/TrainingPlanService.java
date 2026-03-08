package de.momogym.services;

import de.momogym.persistence.Athlete;
import de.momogym.persistence.Exercise;
import de.momogym.persistence.ExerciseLog;
import de.momogym.persistence.PlannedExercise;
import de.momogym.persistence.TrainingDay;
import de.momogym.persistence.TrainingPlan;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

		// Schritt 3: (WICHTIG) ... (Logik bleibt gleich)
		if (makeActive) {
			entityManager.createQuery("UPDATE TrainingPlan p SET p.isActive = false WHERE p.athlete = :athlete")
				.setParameter("athlete", athlete)
				.executeUpdate();
		}

		// Schritt 4: Neuen Plan erstellen
		TrainingPlan newPlan = new TrainingPlan();
		newPlan.setAthlete(athlete); // Wir nutzen das geladene Objekt
		newPlan.setName(planName);
		newPlan.setActive(makeActive);
		newPlan.setCurrentDaySequence(1);
		entityManager.persist(newPlan);
		// --- NEU: Trainings-Tage erstellen ---
		// Wir gehen die Liste der Strings durch (z.B. "Montag", "Mittwoch")
		if (selectedDays != null && !selectedDays.isEmpty()) {
			int sequence = 1;
			for (String dayName : selectedDays) {
				TrainingDay day = new TrainingDay();
				day.setName(dayName);           // Name z.B. "Montag"
				day.setTrainingPlan(newPlan);   // Verknüpfung zum Plan
				// Optional: Falls du eine Reihenfolge in der DB hast
				// day.setSequence(sequence++);

				entityManager.persist(day);
			}
		}

		return newPlan;
	}

	public void deleteTrainingPlan(Long planId) {
		TrainingPlan plan = entityManager.find(TrainingPlan.class, planId);
		if (plan != null) {
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

		entityManager.persist(plannedExercise);
	}

	public void updatePlannedExercise(Long id, int sets, String reps, Double weight){
		PlannedExercise exercise = entityManager.find(PlannedExercise.class, id);
		if( exercise != null){
			exercise.setSets(sets);
			exercise.setReps(reps);
			exercise.setWeight(weight);
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
		if(isActive){
			entityManager.createQuery("UPDATE TrainingPlan p SET p.isActive = false WHERE p.athlete = :athlete")
				.setParameter("athlete", plan.getAthlete())
				.executeUpdate();
			entityManager.refresh(plan);
		}
		plan.setActive(isActive);
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
		graph.addSubgraph("plannedExercises").addAttributeNodes("exercise");

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

}

package de.momogym.services;

import de.momogym.persistence.Athlete;
import de.momogym.persistence.TrainingPlan;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Stateless
public class AthleteService {

    @PersistenceContext(unitName = "trainingsverwaltung-pu")
    private EntityManager entityManager;

    public Athlete createAthlete(String username) throws Exception{
        if (usernameExists(username)){
            throw new Exception("Benutzername '" + username + "' ist bereits vergeben.");
        }
        Athlete athlete = new Athlete(username);
        entityManager.persist(athlete);
        return athlete;
    }

    private boolean usernameExists(String username) {
        try {
            entityManager.createQuery("SELECT a FROM Athlete a WHERE a.username = :username", Athlete.class)
                    .setParameter("username", username)
                    .getSingleResult();

            return true;

        } catch (NoResultException e) {

            return false;
        }
    }

    /**
     * Findet einen Athleten anhand seines Benutzernamens.
     * Lädt seine Trainingspläne direkt mit (JOIN FETCH),
     * um LazyInitializationExceptions in der View zu vermeiden.
     *
     * @param username Der zu suchende Benutzername.
     * @return Der Athlete (mit Plänen) oder null, wenn nicht gefunden.
     */
    public Athlete findAthleteWithPlans(String username) {
        try {
            // JPQL: Wir nutzen 'LEFT JOIN FETCH', um die LAZY 'trainingPlans' sofort mitzuladen.
            return entityManager.createQuery(
                            "SELECT a FROM Athlete a LEFT JOIN FETCH a.trainingPlans WHERE a.username = :username",
                            Athlete.class)
                    .setParameter("username", username)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null; // Nichts gefunden
        }
    }

	public List<Athlete> findAllAthletes(){
		return entityManager.createQuery("SELECT a FROM Athlete a", Athlete.class).getResultList();
	}

    /**
     * Findet einen Athleten anhand seiner ID (inkl. Pläne).
     * Wird von der Detailseite benötigt, nachdem wir weitergeleitet haben.
     *
     * @param id Die ID des Athleten.
     * @return Der Athlete (mit Plänen) oder null.
     */
    public Athlete findAthleteByIdWithPlans(Long id) {
        try {
            return entityManager.createQuery(
                            "SELECT a FROM Athlete a LEFT JOIN FETCH a.trainingPlans WHERE a.id = :id",
                            Athlete.class)
                    .setParameter("id", id)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null; // Nichts gefunden
        }
    }




    /**
     * Erstellt einen neuen, leeren Trainingsplan für einen Athleten
     */
    public TrainingPlan createTrainingPlan(Long athleteId, String planName, boolean makeActive) throws Exception {

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

        // Schritt 5: Speichern
        entityManager.persist(newPlan);

        return newPlan;
    }

    /**
     * Prüft, ob der Plan-Name FÜR EINEN SPEZIFISCHEN Athleten existiert.
     */
    private boolean planNameExistsForAthlete(String planName, Athlete athlete) { // ANPASSUNG
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(p) FROM TrainingPlan p WHERE p.name = :name AND p.athlete = :athlete", Long.class); // ANPASSUNG

        query.setParameter("name", planName);
        query.setParameter("athlete", athlete); // ANPASSUNG

        return query.getSingleResult() > 0;
    }

	public void deleteAthlete(Long id){
		Athlete athlete = entityManager.find(Athlete.class, id);

		if (athlete != null) {
			entityManager.remove(athlete);
		}
	}

}

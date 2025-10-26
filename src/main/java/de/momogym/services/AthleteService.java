package de.momogym.services;

import de.momogym.persistence.Athlete;
import de.momogym.persistence.TrainingPlan;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

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
     * Erstellt einen neuen, leeren Trainingsplan für einen Athleten.
     *
     * @param athleteId Die ID des Athleten, dem der Plan gehören soll.
     * @param planName Der Name für den neuen Plan (z.B. "3er Split").
     * @param makeActive Soll dieser Plan als der neue aktive Plan gesetzt werden?
     * @return Der erstellte TrainingPlan.
     * @throws Exception Wenn der Plan-Name bereits global vergeben ist (wegen unique=true).
     */
    public TrainingPlan createTrainingPlan(Long athleteId, String planName, boolean makeActive) throws Exception {

        // Schritt 1: Prüfen, ob der Plan-Name global eindeutig ist
        // (Wie in deiner Entity-Definition @Column(unique = true) gefordert)
        if (planNameExists(planName)) {
            throw new Exception("Der Plan-Name '" + planName + "' ist bereits vergeben.");
        }

        // Schritt 2: Den Besitzer (Athleten) laden
        Athlete athlete = entityManager.find(Athlete.class, athleteId);
        if (athlete == null) {
            throw new Exception("Athlet mit ID " + athleteId + " nicht gefunden.");
        }

        // Schritt 3: (WICHTIG) Wenn dieser Plan aktiv sein soll, alle anderen Pläne
        // dieses Athleten zuerst deaktivieren.
        if (makeActive) {
            entityManager.createQuery("UPDATE TrainingPlan p SET p.isActive = false WHERE p.athlete = :athlete")
                    .setParameter("athlete", athlete)
                    .executeUpdate();
        }

        // Schritt 4: Neuen Plan erstellen
        TrainingPlan newPlan = new TrainingPlan();
        newPlan.setAthlete(athlete);
        newPlan.setName(planName);
        newPlan.setActive(makeActive);
        newPlan.setCurrentDaySequence(1); // Neuer Plan startet immer an Tag 1

        // Schritt 5: Speichern
        entityManager.persist(newPlan);

        return newPlan;
    }

    /**
     * Private Hilfsmethode, um die globale Einzigartigkeit des Plan-Namens zu prüfen.
     */
    private boolean planNameExists(String planName) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(p) FROM TrainingPlan p WHERE p.name = :name", Long.class);
        query.setParameter("name", planName);
        return query.getSingleResult() > 0;
    }

}

package de.momogym.services;

import de.momogym.persistence.Athlete;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

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


}

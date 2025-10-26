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


}

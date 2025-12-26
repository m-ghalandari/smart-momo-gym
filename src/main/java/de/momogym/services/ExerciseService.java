package de.momogym.services;

import de.momogym.exceptions.EntityAlreadyExistsException;
import de.momogym.persistence.Exercise;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Stateless
public class ExerciseService {

	@PersistenceContext(unitName = "trainingsverwaltung-pu")
	private EntityManager entityManager;

	public Exercise createExercise(Exercise exercise) throws EntityAlreadyExistsException {
		TypedQuery<Long> query = entityManager.createQuery(
			"SELECT COUNT(e) FROM Exercise e WHERE e.name = :name", Long.class);
		query.setParameter("name", exercise.getName());
		Long count = query.getSingleResult();

		if (count > 0) {
			throw new EntityAlreadyExistsException("Eine Übung mit dem Namen '" + exercise.getName() + "' existiert bereits!");
		}
		entityManager.persist(exercise);
		return exercise;
	}

	// Method findAllExercises(): Should return all exercises sorted alphabetically.
	public List<Exercise> findAllExercises() {
		return entityManager.createQuery("SELECT e FROM Exercise e ORDER BY e.name ASC", Exercise.class).getResultList();
	}
}

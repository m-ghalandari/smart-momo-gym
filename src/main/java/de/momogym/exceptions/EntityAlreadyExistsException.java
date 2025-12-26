package de.momogym.exceptions;

public class EntityAlreadyExistsException extends Exception {
	private static final long serialVersionUID = 1L;
	public EntityAlreadyExistsException(String message) {
		super(message);
	}
}

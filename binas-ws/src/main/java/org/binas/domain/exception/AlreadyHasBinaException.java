package org.binas.domain.exception;

public class AlreadyHasBinaException extends Exception {
	private static final long serialVersionUID = 1L;

	public AlreadyHasBinaException () {
	}
	
	public AlreadyHasBinaException (String message) {
		super(message);
	}
}

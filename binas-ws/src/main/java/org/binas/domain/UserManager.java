package org.binas.domain;

import java.util.HashSet;
import java.util.Set;

import org.binas.domain.exception.EmailExistsException;
import org.binas.domain.exception.InvalidEmailException;
import org.binas.domain.exception.UserNotExistsException;

public class UserManager {
	
	/* Binas users */
	private Set<User> users = new HashSet<>();
	
	/* New user initial credit */
	private int userInitialPoints = 0;
	
	private UserManager() {
		
	}
	
	/**
	 * SingletonHolder is loaded on the first execution of Singleton.getInstance()
	 * or the first access to SingletonHolder.INSTANCE, not before.
	 */
	private static class SingletonHolder {
		private static final UserManager INSTANCE = new UserManager();
	}

	public static synchronized UserManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public synchronized void setUserInitialPoints(int userInitialPoints) {
		this.userInitialPoints = userInitialPoints;
	}

	public synchronized int getUserInitialPoints() {
		return this.userInitialPoints;
	}
	
	/**
     * Gets user from email
     * @param email
     * @return Found user
     * @throws UserNotExistsException if not found
     */
	public synchronized User getUserByEmail(String email) throws UserNotExistsException {
		for (User user : users) {
			if (user.getEmail().equals(email))
				return user;
		}
		throw new UserNotExistsException();
	}
	
	/**
     * Activates user
     * @param email used to register the new user
     * @return new User
     * @throws InvalidEmailException
     * @throws EmailExistsException
     */
	public synchronized User activateUser(String email) throws InvalidEmailException, EmailExistsException {
		if (email == null || !email.matches("^([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+@([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+$")) {
			throw new InvalidEmailException();
		}
		try {
			getUserByEmail(email);

		} catch (UserNotExistsException unee) {
			User user = new User(email, false, userInitialPoints);
			users.add(user);
			return user;
		}
		throw new EmailExistsException();
	}
	
	/**
	 * Deletes all users and set userInitialPoints to zero 
	 */
	public synchronized void clear() {
		users.clear();	
		userInitialPoints = 0;
	}	
}

package org.binas.station.domain;

import java.util.HashSet;
import java.util.Set;

import org.binas.station.domain.exception.InvalidEmailException;
import org.binas.station.domain.exception.UserNotExistsException;


public class UserManager {
	private Set<User> users = new HashSet<>();
	
	//Singleton ------------------------------------
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
	
	/**
	 * Adds regist of new user
	 * @param email
	 * @param credit
	 * @param tag
	 * @throws InvalidEmailException
	 */
	public synchronized void activateUser(String email, int credit, int tag) throws InvalidEmailException {
		if (email == null || !email.matches("^([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+@([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+$")) {
			throw new InvalidEmailException();
		}
		users.add(new User(email, credit, tag));
	}
	
	/**
     * Gets user from email
     * @param email
     * @return user
     */
	private synchronized User getUserByEmail(String email)  {
		for (User user : users) {
			if (user.getEmail().equals(email))
				return user;
		}
		return null;
	}
	
	/**
	 * Checks if user exists, and if so returns its credit and associated tag
	 * @param email
	 * @return user credit and tag
	 * @throws UserNotExistsException
	 */
	public synchronized Balance getBalance(String email) throws UserNotExistsException {
		User user = getUserByEmail(email);
		
		if(user == null)
			throw new UserNotExistsException();
		
		int credit = user.getCredit();
		int tag = user.getTag();
		
		return new Balance(credit, tag);
	}
	
	/**
	 * If user exists, updates its credit and associated tag.
	 * If not, adds new user.
	 * @param email
	 * @param credit
	 * @param tag
	 * @throws InvalidEmailException 
	 */
	public synchronized void setBalance(String email, int credit, int tag) throws InvalidEmailException {
		User user = getUserByEmail(email);
		
		if(user != null) {
			if(user.getTag() >= tag) 
				return;
			user.setCredit(credit);
			user.setTag(tag);
			
		}else {
			activateUser(email, credit, tag);
		}
	}
	
	public void reset() {
		users.clear();
	}
}

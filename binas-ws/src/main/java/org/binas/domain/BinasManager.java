package org.binas.domain;

import java.util.Set;

import org.binas.domain.exception.EmailExistsException;
import org.binas.domain.exception.InvalidEmailException;
import org.binas.domain.exception.UserNotExistsException;

public class BinasManager {

	/* Binas identifier */
	private String id;
	
	/* Binas users */
	private Set<User> users;
	
	// Singleton -------------------------------------------------------------

	private BinasManager() {
	}

	/**
	 * SingletonHolder is loaded on the first execution of Singleton.getInstance()
	 * or the first access to SingletonHolder.INSTANCE, not before.
	 */
	private static class SingletonHolder {
		private static final BinasManager INSTANCE = new BinasManager();
	}

	public static synchronized BinasManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public  void setId(String id) {
		this.id = id;	
	}
	
	public String getId(String id) {
		return this.id;
	}
	
	public User getUserByEmail(String email) throws UserNotExistsException {
		for(User user :  users) {
			if(user.getEmail().equals(email)) 
				return user;
		}
		throw new UserNotExistsException();
	}
	
	public User activateUser(String email) throws InvalidEmailException, EmailExistsException {
		if(email == null && email.matches("^([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+@([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+$")) {
			throw new InvalidEmailException();
		}
		try {
			getUserByEmail(email);
			
		}catch(UserNotExistsException unee) {
			User user = new User(email,false,0);
			users.add(user);
			return user;
		}
		throw new EmailExistsException();
	}
	
	public int getCredit(String email) throws UserNotExistsException {
		return getUserByEmail(email).getCredit();
	}
}

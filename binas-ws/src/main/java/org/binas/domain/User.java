package org.binas.domain;

import java.util.Set;

import org.binas.domain.exception.EmailExistsException;
import org.binas.domain.exception.InvalidEmailException;
import org.binas.domain.exception.UserNotExistsException;

/** Class to store user info. */
public class User {
	
	private static Set<User> users;

	private String email;
	private boolean hasBina;
	private int credit;

	public User(String email, boolean hasBina, int credit) throws EmailExistsException, InvalidEmailException {
		
		if(email == null && email.matches("^([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+@([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+$")) 
			throw new InvalidEmailException();
		
		if(credit < 0) 
			credit = 0;
		
		try {
			getUserByEmail(email);
			
		}catch(UserNotExistsException unee) {
			this.email = email;
			this.hasBina = hasBina;
			this.credit = credit;
		
		users.add(this);
		}
		
		throw new EmailExistsException();		
	}
	
	public static User getUserByEmail(String email) throws UserNotExistsException {
		for(User user : users) {
			if(user.getEmail().equals(email)) 
				return user;
		}
		throw new UserNotExistsException();
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean getHasBina() {
		return hasBina;
	}

	public void setHasBina(boolean hasBina) {
		this.hasBina = hasBina;
	}
	
	public int getCredit() {
		return credit;
	}

	public void setCredit(int credit) {
		this.credit = credit;
	}
/*
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}
*/

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (email != other.email)
			return false;
		if (hasBina != other.hasBina)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("User [email=");
		builder.append(email);
		builder.append(", hasBina=");
		builder.append(hasBina);
		builder.append(", credit=");
		builder.append(credit);
		builder.append("]");
		return builder.toString();
	}

}

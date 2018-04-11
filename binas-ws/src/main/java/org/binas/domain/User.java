package org.binas.domain;

/** Class to store user info. */
public class User {

	private String email;
	private boolean hasBina;
	private int credit;

	public User(String email, boolean hasBina, int credit) {
			this.email = email;
			this.hasBina = hasBina;
			this.credit = credit;
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

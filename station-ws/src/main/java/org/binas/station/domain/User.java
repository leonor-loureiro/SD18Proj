package org.binas.station.domain;

/** Class to store user info. */
public class User {

	private String email;
	private int credit;
	private int tag;

	public User(String email, int credit, int tag) {
			this.email = email;
			this.credit = credit;
			this.tag = tag;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public synchronized int getCredit() {
		return credit;
	}
	
	public synchronized void setCredit(int credit) {
		this.credit = credit;
	}
	
	public synchronized int getTag() {
		return this.tag;
	}

	public synchronized void setTag(int tag) {
		this.tag = tag;
	}

}

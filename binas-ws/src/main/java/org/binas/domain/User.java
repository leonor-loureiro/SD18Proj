package org.binas.domain;

/** Class to store user info. */
public class User {

	private String email;
	private boolean hasBina;
	private int credit;
	private int tag;

	public User(String email, boolean hasBina, int credit, int tag) {
			this.email = email;
			this.hasBina = hasBina;
			this.credit = credit;
			this.tag = tag;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public synchronized boolean getHasBina() {
		return hasBina;
	}

	public synchronized void setHasBina(boolean hasBina) {
		this.hasBina = hasBina;
	}
	
	public synchronized int getCredit() {
		return credit;
	}

	public synchronized int removeOneCredit() {
		return credit--;
	}
	
	public synchronized void receiveBonus(int bonus) {
		this.credit += bonus;
	}
	
	public synchronized void setCredit(int credit) {
		this.credit = credit;
	}
	
	public synchronized int getTag() {
		return tag;
	}

	public synchronized void setTag(int tag) {
		this.tag = tag;
	}


}

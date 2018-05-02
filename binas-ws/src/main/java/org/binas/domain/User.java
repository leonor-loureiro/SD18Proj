package org.binas.domain;

/** Class to store user info. */
public class User {

	/**
	 * User's unique identifier
	 */
	private String email;

	/**
	 * true if user is using a Bina
	 */
	private boolean hasBina;

	/**
	 * user currency to rent binas
	 */
	private int credit;

	/**
	 * value which represents this user's credit version to check if it's updated with servers or not
	 */
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

	/**
	 * Decreases user credit by one
	 * @return new credit
	 */
	public synchronized int removeOneCredit() {
		return credit--;
	}
	
	/**
	 * Adds bonus to user credit
	 * @param bonus
	 */
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

	public synchronized void updateTag() {
		tag++;
	}

}

package org.binas.station.domain;

/** Class to store user info. */
public class User {

	/** User email */
	private String email;
	/** User credit */
	private int credit;
	/** User tag */
	private int tag;

	/** Simple constructor*/
	public User(String email, int credit, int tag) {
			this.email = email;
			this.credit = credit;
			this.tag = tag;
	}

	/** Getter for Email */
	public String getEmail() {
		return email;
	}

	/** Setter for Email */
	public void setEmail(String email) {
		this.email = email;
	}

	/** Getter for Credit */
	public synchronized int getCredit() {
		return credit;
	}

	/** Setter for Credit */
	public synchronized void setCredit(int credit) {
		this.credit = credit;
	}

	/** Getter for Tag */
	public synchronized int getTag() {
		return this.tag;
	}

	/** Setter for Tag */
	public synchronized void setTag(int tag) {
		this.tag = tag;
	}
}

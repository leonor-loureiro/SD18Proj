package org.binas.station.domain;

/**
 * Class to store user credit and tag
 */
public class Balance {

	// Value of users credit
	private int value;

	// Version control tag
	private int tag;

	/**
	 * Balance constructor
	 * @param value initial value
	 * @param tag initial tag
	 */
	public Balance(int value, int tag) {
		this.value = value;
		this.tag = tag;
	}

	/**
	 * Getter for Value
	 * @return balance value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Setter for Value
	 * @param value value to set
	 */
	public void setValue(int value) {
		this.value = value;
	}

	/**
	 * Getter for Tag
	 * @return balance tag
	 */
	public int getTag() {
		return tag;
	}

	/**
	 * Setter for Tag
	 * @param tag tag to set
	 */
	public void setTag(int tag) {
		this.tag = tag;
	}
}

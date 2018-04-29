package org.binas.station.domain;

/* Class to store user credit and tag */
public class Balance {
	private int value;
	private int tag;
	
	public Balance(int value, int tag) {
		this.value = value;
		this.tag = tag;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getTag() {
		return tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}
}

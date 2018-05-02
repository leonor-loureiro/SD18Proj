package org.binas.station.domain;

/** Class to store map coordinates. */
public class Coordinates {

	private int x;
	private int y;

	/**
	 * Coordinate constructor
	 * @param x x value
	 * @param y y value
	 */
	public Coordinates(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * X getter
	 * @return
	 */
	public int getX() {
		return x;
	}

	/**
	 * X setter
	 * @param x
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * Y getter
	 * @return
	 */
	public int getY() {
		return y;
	}

	/**
	 * Y setter
	 * @param y
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * Hash function
	 * @return hash of coordinate
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	/**
	 * Equals Override
	 * @param obj object to compare
	 * @return equal or not
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Coordinates other = (Coordinates) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	/**
	 * toString Override
	 * @return object in  string format
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Coordinates [x=");
		builder.append(x);
		builder.append(", y=");
		builder.append(y);
		builder.append("]");
		return builder.toString();
	}

}

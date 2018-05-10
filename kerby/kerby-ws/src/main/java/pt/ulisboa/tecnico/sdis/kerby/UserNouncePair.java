package pt.ulisboa.tecnico.sdis.kerby;

class UserNouncePair {
	private String username;
	private long nounce;
	
	public UserNouncePair(String name, long n) {
		username = name;
		nounce = n;
	}
	
	public String getUsername() {
		return username;
	}
	
	public long getNounce() {
		return nounce;
	}
	
	// object methods --------------------------------------------------------

	/** Create a textual representation of the UserNouncePair. */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserNouncePair [username=");
		builder.append(username);
		builder.append(", nounce=");
		builder.append(nounce);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (nounce ^ (nounce >>> 32));
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserNouncePair other = (UserNouncePair) obj;
		if (nounce != other.nounce)
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}	

}

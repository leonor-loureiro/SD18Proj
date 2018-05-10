package pt.ulisboa.tecnico.sdis.kerby;

/**
 * Class used as an Entry for a Ticket Collection. Stores a SessionKeyAndTicketView and its Final Valid Time.
 * @author Guilherme Ilunga
 * */
public class TicketCollectionEntry {
	private SessionKeyAndTicketView sessionKeyAndTicketView;
	private long finalValidTime;
	
	/**
	 * Creates a TicketCollectionEntry.
	 * @param sessionKeyAndTicketView The sessionKeyAndTicketView to store.
	 * @param finalValidTime The Final ValidTime of the sessionKeyAndTicketView, in miliseconds since January 1s 1970.
	 * */
	public TicketCollectionEntry(SessionKeyAndTicketView sessionKeyAndTicketView, long finalValidTime) {
		this.sessionKeyAndTicketView = sessionKeyAndTicketView;
		this.finalValidTime = finalValidTime;
	}
	
	/** Returns the SessionKeyAndTicketView associated with this TicketCollectionEntry. */
	public SessionKeyAndTicketView getSessionKeyAndTicketView() {
		return sessionKeyAndTicketView;
	}
	
	/** Returns the Final Valid Time associated with this TicketCollectionEntry. */
	public long getFinalValidTime() {
		return finalValidTime;
	}
	
	// object methods --------------------------------------------------------
	
	/** Create a textual representation of the TicketCollectionEntry. */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(); 
		builder.append("TicketCollectionEntry [sessionKeyAndTicketView=");
		builder.append(sessionKeyAndTicketView.toString());
		builder.append(", finalValidTime=");
		builder.append(finalValidTime);
		builder.append("]");
		return builder.toString();
	}

	/** Returns a hash code value for the TicketCollectionEntry. */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (finalValidTime ^ (finalValidTime >>> 32));
		result = prime * result + ((sessionKeyAndTicketView == null) ? 0 : sessionKeyAndTicketView.hashCode());
		return result;
	}

	/** Indicates whether some other object is "equal to" this TicketCollectionEntry. */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TicketCollectionEntry other = (TicketCollectionEntry) obj;
		if (finalValidTime != other.finalValidTime)
			return false;
		if (sessionKeyAndTicketView == null) {
			if (other.sessionKeyAndTicketView != null)
				return false;
		} else if (!sessionKeyAndTicketView.equals(other.sessionKeyAndTicketView))
			return false;
		return true;
	}

	
	
}

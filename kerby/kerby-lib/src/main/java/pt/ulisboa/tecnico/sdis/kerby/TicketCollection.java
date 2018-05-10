package pt.ulisboa.tecnico.sdis.kerby;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that stores and returns Tickets if they are still valid, within an accepted difference between final valid time and current time.
 * @author Guilherme Ilunga
 * */
public class TicketCollection {
	/** Maps Server Name to TicketEntry */
	private ConcurrentHashMap<String, TicketCollectionEntry> ticketCollection = new ConcurrentHashMap<String, TicketCollectionEntry>();
	/** Accepted Difference between Ticket's Final Valid Time and Current Time. Measured in milliseconds. */
	private long acceptedDifference = 2000;
	
	/** Default constructor with accepted difference of 2000 miliseconds. */
	public TicketCollection() {}
	
	/**
	 * Constructor which sets the accepted difference.
	 * @param acceptedDifference The accepted difference between a Ticket's final valid time and the current time. Used in getTicket.
	 * */
	public TicketCollection(long acceptedDifference) {
		this.acceptedDifference = acceptedDifference;
	}
	
	/** Stores the Ticket and FinalTime Indexed by the Server Name.
	 * @param serverName The name of server.
	 * @param sessionKeyAndTicketView The SessionKeyAndTicketView to store in the collection.
	 * @param finalValidTime Final Valid Time in Milliseconds since January 1st 1970. */
	public void storeTicket(String serverName, SessionKeyAndTicketView sessionKeyAndTicketView, long finalValidTime) {
		TicketCollectionEntry newEntry = new TicketCollectionEntry(sessionKeyAndTicketView, finalValidTime);
		ticketCollection.put(serverName, newEntry);
	}
	
	/** 
	 * Returns a Stored SessionKeyAndTicketView for the Given Server if a Valid Entry Exists. Else, Returns null. 
	 * @param serverName The name of the server.
	 * @return The Stored SessionKeyAndTicketView or Null if it has expired or does not exist.
	 * */
	public SessionKeyAndTicketView getTicket(String serverName) {
		TicketCollectionEntry storedEntry = ticketCollection.get(serverName);
		if(storedEntry == null)
			return null;
		
		long currentTime = System.currentTimeMillis();
		long entryTime = storedEntry.getFinalValidTime();
		
		// Expired Ticket
		if(entryTime - currentTime < acceptedDifference)
			return null;
		else
			return storedEntry.getSessionKeyAndTicketView();
	}
	
	/**
	 * Clears the Collection of Tickets.
	 * */
	public void clear() {
		ticketCollection.clear();
	}
	
}

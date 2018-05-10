package pt.ulisboa.tecnico.sdis.kerby;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class KerbyManager {
	
	private static final int MIN_TICKET_DURATION = 10;
	private static final int MAX_TICKET_DURATION = 300;
	private static Set<UserNouncePair> previousNounces = Collections.synchronizedSet(new HashSet<UserNouncePair>());
	private static ConcurrentHashMap<String, Key> knownKeys = new ConcurrentHashMap<String, Key>();
	private static String salt;
	
	// Singleton -------------------------------------------------------------
	private KerbyManager() {
	}

	/**
	 * SingletonHolder is loaded on the first execution of Singleton.getInstance()
	 * or the first access to SingletonHolder.INSTANCE, not before.
	 */
	private static class SingletonHolder {
		private static final KerbyManager INSTANCE = new KerbyManager();
	}

	public static synchronized KerbyManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public SessionKeyAndTicketView requestTicket(String client, String server, long nounce, int ticketDuration) 
			throws BadTicketRequestException {
		
		/* Validate parameters */
		if(client == null || client.trim().isEmpty())
			throw new BadTicketRequestException("Null Client.");
		if(knownKeys.get(client) == null)
			throw new BadTicketRequestException("Unknown Client.");
		if(server == null || server.trim().isEmpty())
			throw new BadTicketRequestException("Null Server.");
		if(knownKeys.get(server) == null)
			throw new BadTicketRequestException("Unknown Server.");
		if(ticketDuration < MIN_TICKET_DURATION || ticketDuration > MAX_TICKET_DURATION)
			throw new BadTicketRequestException("Invalid Ticked Duration.");
		
		UserNouncePair userNounce = new UserNouncePair(client, nounce);
		if(previousNounces.contains(userNounce))
			throw new BadTicketRequestException("Repeated Nounce, possible Replay Attack.");
		
		
		try {
			/* Get Previously Generated Client and Server Keys */
			Key clientKey = knownKeys.get(client);
			Key serverKey = knownKeys.get(server);
			
			/* Generate a new key for Client-Server communication */
			Key clientServerKey = SecurityHelper.generateKey();
			
			/* Create and Cipher the Ticket */
			Ticket ticket = createTicket(client, server, ticketDuration, clientServerKey);
			CipheredView cipheredTicket = ticket.cipher(serverKey);
			
			/* Create and Cipher the Session Key */
			SessionKey sessionKey = new SessionKey(clientServerKey, nounce);
			CipheredView cipheredSessionKey = sessionKey.cipher(clientKey);
			
			/* Create SessionKeyAndTicketView */
			SessionKeyAndTicketView response = new SessionKeyAndTicketView();
			response.setTicket(cipheredTicket);
			response.setSessionKey(cipheredSessionKey);
			
			/* Store UserNouncePair */
			previousNounces.add(userNounce);
			
			return response;
			
		} catch (NoSuchAlgorithmException e) {
			throw new BadTicketRequestException("Error generating shared key.");
		} catch (KerbyException e) {
			throw new BadTicketRequestException("Error while ciphering.");
		}
	}
	
	// Helpers -------------------------------------------------------------
	
	public void initSalt(String saltFilename) throws Exception {
		InputStream inputStream = KerbyManager.class.getResourceAsStream(saltFilename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line = reader.readLine();
		if(line != null && !line.trim().isEmpty())
			salt = line;
	}
	
	/** Reads Passwords from the given file, generates all keys and stores them in memory. */
	public void initKeys(String passwordFilename) throws Exception {
		InputStream inputStream = KerbyManager.class.getResourceAsStream(passwordFilename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(line.startsWith("#") || !line.contains(","))
				continue;
			String[] values = line.split(",");
			Key key;
			if(salt == null) {
				key = SecurityHelper.generateKeyFromPassword(values[1]);
			} else {
				key = SecurityHelper.generateKeyFromPassword(values[1], salt);
			}
			knownKeys.put(values[0], key);
		}
	}
	
	private Ticket createTicket(String client, String server, int ticketDuration, Key clientServerKey) {
		final Calendar calendar = Calendar.getInstance();
		final Date t1 = calendar.getTime();
		calendar.add(Calendar.SECOND, ticketDuration);
		final Date t2 = calendar.getTime();
		Ticket ticket = new Ticket(client, server, t1, t2, clientServerKey);
		return ticket;
	}
	
}

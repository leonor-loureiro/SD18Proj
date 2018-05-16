package binas.ws.handler;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import java.security.Key;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import pt.ulisboa.tecnico.sdis.kerby.Auth;
import pt.ulisboa.tecnico.sdis.kerby.BadTicketRequest_Exception;
import pt.ulisboa.tecnico.sdis.kerby.CipherClerk;
import pt.ulisboa.tecnico.sdis.kerby.CipheredView;
import pt.ulisboa.tecnico.sdis.kerby.KerbyException;
import pt.ulisboa.tecnico.sdis.kerby.RequestTime;
import pt.ulisboa.tecnico.sdis.kerby.SecurityHelper;
import pt.ulisboa.tecnico.sdis.kerby.SessionKey;
import pt.ulisboa.tecnico.sdis.kerby.SessionKeyAndTicketView;
import pt.ulisboa.tecnico.sdis.kerby.TicketCollection;
import pt.ulisboa.tecnico.sdis.kerby.cli.KerbyClient;

public class KerberosClientHandler implements SOAPHandler<SOAPMessageContext>{

	//USER INFO
	public static String username = "alice@T08.binas.org";
	public static String userPassword = "WxzsYKnJn";

	// SERVER INFO
	public static String servername = "binas@T08.binas.org";


	public static final String url = "http://sec.sd.rnl.tecnico.ulisboa.pt:8888/kerby";
	public static final String name = "kerby";
	
	// HEADERS
	public static final String TICKET_HEADER = "ticketHeader";
	public static final String AUTH_HEADER = "authHeader";
	public static final String REQUEST_TIME_HEADER = "requestedTime";

	// NAMESPACE
	public static final String KERBY_NS = "urn:binas.client.kerby";
	private static final String SERVER_NS = "urn:binas.server.authentication";
	private static final String SOAP_PREFIX = "b";
	
	// CONTEXT
	private static final String CONTEXT_REQ_TIME = "context_request_time";
	private static final String CONTEXT_SESSION_KEY = "context_session_key";
	
	// COLLECTION OF VALID TICKETS
	private static TicketCollection validTickets = new TicketCollection(500);
	
	/**
	 * Called at the conclusion of a message exchange pattern just prior to the
	 * JAX-WS runtime dispatching a message, fault or exception.
	 */
	@Override
	public void close(MessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		if(!outbound) {
			smc.put(CONTEXT_REQ_TIME, null);
			smc.put(CONTEXT_SESSION_KEY, null);
		}
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext arg0) {
		System.out.println("Ignoring fault message...");
		return true;
	}

	/**
	 * The handleMessage method is invoked for normal processing of inbound and
	 * outbound messages.
	 */
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		if(outbound) {
			return handleOutboundMessage(smc);
		}
		else {
			return handleInboundMessage(smc);
		}
	}

	/**
	 * Gets the header blocks that can be processed by this Handler instance. If
	 * null, processes all.
	 */
	@Override
	public Set<QName> getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Handles outgoing request to Binas Server
	 */
	private boolean handleOutboundMessage(SOAPMessageContext smc) {
				
		Key clientKey = null;
		try {
			clientKey = SecurityHelper.generateKeyFromPassword(userPassword);
			
		}catch(Exception e) {
			throw new RuntimeException("Error: generation user key failed.");
		}
		
		SessionKeyAndTicketView result = validTickets.getTicket(servername);
		if(result == null) {
			System.out.println("New session requested");
			KerbyClient client = null;
			try {
				client = new KerbyClient(url);	
			
			}catch(Exception e) {
				throw new RuntimeException("Error: creation of KerbyClient instance failed.");
			}
			
			// New session request
			try {
				result = client.requestTicket(username, servername,
	        			new Random().nextLong(), 60 /* seconds */);
				
				validTickets.storeTicket(servername, result, System.currentTimeMillis() + 60000);
			}catch (BadTicketRequest_Exception e1) {
				throw new RuntimeException("Error: new session request failed.");
			}
		}
		
		
		CipheredView cipheredSessionKey = null;
		CipheredView cipheredTicket = null;
		SessionKey sessionKey = null;
		
		//Extract session key and ticket
		try {
			cipheredSessionKey = result.getSessionKey(); 

	        cipheredTicket = result.getTicket();
	        
	        //Decipher session key
	        sessionKey = new SessionKey(cipheredSessionKey, clientKey); 
	        
		} catch (KerbyException e) {
			throw new RuntimeException("Error: sesssion key deciphering failed.");
		}
		
		// Generate new authenticator
		Date requestTime = new Date();
        Auth auth = new Auth(username, requestTime);
        
        CipheredView cipheredAuth = null;
        // Encrypt authenticator
        try {
			cipheredAuth = auth.cipher(sessionKey.getKeyXY());
		} catch (KerbyException e1) {
			throw new RuntimeException("Error: authenticator encryption failed.");
		}
     
		try {
			// get SOAP envelope
			SOAPMessage msg = smc.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			SOAPEnvelope se = sp.getEnvelope();
			SOAPHeader sh = se.getHeader();
			
			//Create new header with ticket
			Name ticketHeaderName = se.createName(TICKET_HEADER, SOAP_PREFIX, KERBY_NS);
			SOAPHeaderElement ticketHeader = sh.addHeaderElement(ticketHeaderName);
			
			String ticketString = printBase64Binary(cipheredTicket.getData());
			ticketHeader.addTextNode(ticketString);
			
			//Create new header with authenticator
			Name authHeaderName = se.createName(AUTH_HEADER, SOAP_PREFIX, KERBY_NS);
			SOAPHeaderElement authHeader = sh.addHeaderElement(authHeaderName);
			
			String authString = printBase64Binary(cipheredAuth.getData());
			authHeader.addTextNode(authString);			
		}catch (SOAPException e) {
			System.out.printf("Failed to add SOAP header because of %s%n", e);
			throw new RuntimeException("Error: Failed to add SOAP header.");
		}
		
		//Add session key and requestTime to context to latter handle inbound msg
		smc.put(CONTEXT_REQ_TIME, requestTime);
		smc.put(CONTEXT_SESSION_KEY, sessionKey.getKeyXY());
				
		return true;
	}
	
	/**
	 * Handles incoming messages from Binas Server 
	 */
	public boolean handleInboundMessage(SOAPMessageContext smc) {
		
		//Get session key an request time of corresponding request message
		Date requestTime = (Date) smc.get(CONTEXT_REQ_TIME);
		Key sessionKey = (Key) smc.get(CONTEXT_SESSION_KEY);
		
		if(requestTime == null && sessionKey == null) {
			System.out.println("No authentication necessary");
			return true;
		}
				
		try {
			// get SOAP envelope header
			SOAPMessage msg = smc.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			SOAPEnvelope se = sp.getEnvelope();
			SOAPHeader sh = se.getHeader();

			// check header
			if (sh == null) {
				throw new RuntimeException("Error: no SOAP header was found.");
			}	
			
			//Extract request time from header
			String requestTimeStr = getHeaderElementByName(se, REQUEST_TIME_HEADER, SERVER_NS).getValue();
			byte[] requestTimeBytes = parseBase64Binary(requestTimeStr);
			
	
			CipherClerk clerk = new CipherClerk();
			CipheredView cipheredRequestTime = clerk.cipherBuild(requestTimeBytes);
			
			//Decipher request time
			RequestTime answerRequestTime = new RequestTime(cipheredRequestTime, sessionKey);
			
			//Check if request time in answer msg corresponds to request time in request msg 
			if(!answerRequestTime.getTimeRequest().equals(requestTime)) {
				throw new RuntimeException("Error: request time in answer does not match request time in request.");
			}
						
		}catch(SOAPException e) {
			throw new RuntimeException("Error: failed to get SOAP envelope.");
			
		} catch (KerbyException e) {
			throw new RuntimeException("Error: decryption of request time failed.");
		}
		
		return true;
		
	}
	
	
	/**
	 * Return the request header element retrieved from the given envelope
	 * @param se SOAP envelope
	 * @param localName header name
	 * @param namespace
	 * @return header element
	 * @throws SOAPException
	 */
	private SOAPElement getHeaderElementByName(SOAPEnvelope se, String localName, String namespace) throws SOAPException {
		Name name = se.createName(localName, KerberosClientHandler.SOAP_PREFIX, namespace);
		Iterator<?> it = se.getHeader().getChildElements(name);

		// check header element
		if (!it.hasNext()) {
			System.out.println("Header element not found.");
			return null;
		}
		return (SOAPElement) it.next();
	}

	/**
	 * Set the user info
	 * @param user
	 * @param pass
	 */
	public static void setUser(String user, String pass) {
		if ( (user != null && !user.trim().isEmpty()) || (pass != null && !pass.trim().isEmpty())) {
			username = user;
			userPassword = pass;
		}
	}

	/**
	 * Set server info
	 * @param server
	 */
	public static void setServer(String server) {
		if ( server != null && !server.trim().isEmpty() ) {
			servername = server;
		}
	}


}

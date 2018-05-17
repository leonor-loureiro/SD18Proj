package binas.ws.handler;


import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import java.security.Key;
import java.util.Date;
import java.util.Iterator;
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
import pt.ulisboa.tecnico.sdis.kerby.CipherClerk;
import pt.ulisboa.tecnico.sdis.kerby.CipheredView;
import pt.ulisboa.tecnico.sdis.kerby.KerbyException;
import pt.ulisboa.tecnico.sdis.kerby.RequestTime;
import pt.ulisboa.tecnico.sdis.kerby.SecurityHelper;
import pt.ulisboa.tecnico.sdis.kerby.Ticket;

/**
 * Checks the validity of the ticket and the identity of the client
 */
public class BinasServerHandler implements SOAPHandler<SOAPMessageContext> {
	
	//SERVER INFO
	private static  String servername = "binas@T08.binas.org";
	private static String serverPassword = "t5h9O9B2";

	// HEADER NAMES
	private static final String TICKET_HEADER = "ticketHeader";
	private static final String AUTH_HEADER = "authHeader";
	private static final String REQUEST_TIME_HEADER = "requestedTime";

	// NAMESPACE
	public static final String BINAS_NS = "urn:binas.authentication";
	private static final String SOAP_PREFIX = "b";
	

	// CONTEXT
	private static final String CONTEXT_REQ_TIME = "context_time";
	private static final String CONTEXT_LAST_SESSION = "context_session_key";
	private static final String CONTEXT_USERNAME = "context_username";


	/**
	 * Set the server info
	 * @param name
	 * @param pass
	 */
	public static void setServer(String name, String pass) {
		if ( (name != null && !name.trim().isEmpty()) || (pass != null && !pass.trim().isEmpty())) {
			servername = name;
			serverPassword = pass;
		}
	}
	
	/**
	 * Called at the conclusion of a message exchange pattern just prior to the
	 * JAX-WS runtime dispatching a message, fault or exception.
	 */
	@Override
	public void close(MessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
				
		if(outbound) {
			smc.put(CONTEXT_REQ_TIME, null);
			smc.put(CONTEXT_LAST_SESSION, null);
		}

	}
	
	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext arg0) {
		return true;
	}

	/**
	 * Gets the header blocks that can be processed by this Handler instance. If
	 * null, processes all.
	 */
	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	/**
	 * The handleMessage method is invoked for normal processing of inbound and
	 * outbound messages.
	 */
	@Override
	public boolean handleMessage(SOAPMessageContext arg0) {
		Boolean outbound = (Boolean) arg0.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outbound) {
			return handleOutboundMessage(arg0);
		}else{
			return handleInboundMessage(arg0);
		}
	}

	/**
	 * Handles outbound messages
	 */
	private boolean handleOutboundMessage(SOAPMessageContext smc) {
		Date reqTime = (Date) smc.get(CONTEXT_REQ_TIME);
		Key lastSessionKey = (Key) smc.get(CONTEXT_LAST_SESSION);


		try {
			if(reqTime != null && lastSessionKey != null){
				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();

				// Add header if none exists
				if (sh == null)
					sh = se.addHeader();

				CipheredView cipheredReqTime = new RequestTime(reqTime).cipher(lastSessionKey);

				// Add new header with request time
				appendHeader(cipheredReqTime, REQUEST_TIME_HEADER, se, sh);
			}
		} catch (SOAPException e) {
			throw new RuntimeException("error handling header in outbound");
		} catch (KerbyException e) {
			throw new RuntimeException("error ciphering date in outbound");
		}

		return true;
	}

	/**
	 * Handles inbound messages
	 */
	private boolean handleInboundMessage(SOAPMessageContext smc) {
		Key serverKey = getServerKey();

		SOAPMessage msg = smc.getMessage();
		SOAPPart sp = msg.getSOAPPart();

		// get soap envelope
		SOAPEnvelope se;
		try {
			se = sp.getEnvelope();
		} catch (SOAPException e) { throw new RuntimeException("Error opening SOAP envelope"); }

	
		// extract and decipher ticket - obtain session key
		Ticket ticket;
		try {
			ticket = extractTicket(se, serverKey);
		} catch (KerbyException e) {  throw new RuntimeException("Unable to extract ticket"); }

		Key sessionKey = ticket.getKeyXY();


		// verify server name
		if( !servername.equals(ticket.getY()) ){
			throw new RuntimeException("Servername does not match ticket servername");
		}
		
		// Verify validity of ticket
		long currentTime = new Date().getTime();
		if(currentTime < ticket.getTime1().getTime() && currentTime > ticket.getTime2().getTime()){
			throw new RuntimeException("Ticket expired");
		}
		
		// Extract and decipher Authentication
		Auth auth;
		try {
			auth = extractAuth(se, sessionKey);
		} catch (KerbyException e) { throw new RuntimeException("unable to extract auth"); }

		try{
			auth.validate();
		}catch(KerbyException e) {throw new RuntimeException("unable to validate auth");}


		// Verify auth username matches ticket username
		if( !auth.getX().equals(ticket.getX()) ){
			throw new RuntimeException("Authentication does not match ticket user");
		}


		// Save reqTime and session key in context to be sent
		Date reqTime =  auth.getTimeRequest();
		smc.put(CONTEXT_REQ_TIME, reqTime);
		smc.put(CONTEXT_LAST_SESSION, sessionKey);
		smc.put(CONTEXT_USERNAME, auth.getX());

		return true;
	}


	/**
	 * Generates the server key from the servers password
	 */
	private Key getServerKey(){
		try {
			return SecurityHelper.generateKeyFromPassword(serverPassword);

		}catch(Exception e) {
			throw new RuntimeException("Error: generation user key failed.");
		}
	}

	/**
	 * Extracts the ticket from the given header and deciphers it
	 */
	private Ticket extractTicket(SOAPEnvelope se, Key serverKey) throws KerbyException {
		CipheredView cipheredTicket;
		try {
			byte[] result = parseBase64Binary(getHeaderElementByName(se, TICKET_HEADER).getValue());
			cipheredTicket = new CipherClerk().cipherBuild(result);
		} catch (SOAPException e) {  throw new RuntimeException("Unable to decipher Ticket"); }

		return new Ticket(cipheredTicket, serverKey);
	}

	/**
	 * Extracts the authenticator from the given header and deciphers it
	 */
	private Auth extractAuth(SOAPEnvelope se, Key sessionKey) throws KerbyException {
		CipheredView cipheredAuth;
		try {
			byte[] result = parseBase64Binary(getHeaderElementByName(se, AUTH_HEADER).getValue());
			cipheredAuth = new CipherClerk().cipherBuild(result);
		} catch (SOAPException e) {  throw new RuntimeException("Unable to decipher Ticket"); }

		return new Auth(cipheredAuth, sessionKey);
	}
	
	/**
	 * Creates a new header with the given element and name
	 * @param cipheredElem element to include
	 * @param localname header name
	 * @param se SOAP envelope
	 * @param sh SOAP header
	 */
	private void appendHeader(CipheredView cipheredElem, String localname, SOAPEnvelope se, SOAPHeader sh) {
		try {
			Name headerName = se.createName(localname, SOAP_PREFIX, BINAS_NS);
			SOAPHeaderElement header = sh.addHeaderElement(headerName);
			
			String cipheredElemString = printBase64Binary(cipheredElem.getData());
			header.addTextNode(cipheredElemString);	
			
		}catch(SOAPException e) {
			throw new RuntimeException("Error: failed to include header " + localname);
		}
	}
	
	/**
	 * Retrieves the header with the given name
	 * @param se SOAP Envelop
	 * @param localName header name
	 * @return corresponding header 
	 * @throws SOAPException
	 */
	private SOAPElement getHeaderElementByName(SOAPEnvelope se, String localName) throws SOAPException {
		Name name = se.createName(localName, SOAP_PREFIX, BINAS_NS);
		Iterator<?> it = se.getHeader().getChildElements(name);

		// check header element
		if (!it.hasNext()) {
			System.out.println("Header element not found.");
			return null;
		}
		return (SOAPElement) it.next();
	}
}


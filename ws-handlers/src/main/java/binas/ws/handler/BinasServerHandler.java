package binas.ws.handler;


import java.util.*;

import java.security.Key;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.NodeList;
import pt.ulisboa.tecnico.sdis.kerby.*;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

public class BinasServerHandler implements SOAPHandler<SOAPMessageContext> {

	private final static int VALID_DURATION = 30;

	private static final String servername = "binas@T08.binas.org";
	private static final String serverPassword = "t5h9O9B2";

	private static final String url = "http://sec.sd.rnl.tecnico.ulisboa.pt:8888/kerby";
	private static final String name = "kerby";

	// Header names
	private static final String TICKET_HEADER = "ticketHeader";
	private static final String AUTH_HEADER = "authHeader";

	// Namespace
	private static final String KERBY_NS = "urn:binas.client.kerby";
	private static final String SERVER_NS = "urn:binas.server.authentication";


	private static final String SOAP_PREFIX = "b";
	private static final String SOAP_EMAIL_TAG = "email";

	private static final String REQ_TIME_HEADER = "requestedTime";

	// CONTEXT
	private static final String CONTEXT_REQ_TIME = "context_time";
	private static final String CONTEXT_LAST_SESSION = "context_session_key";

	@Override
	public void close(MessageContext arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean handleFault(SOAPMessageContext arg0) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Set<QName> getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean handleMessage(SOAPMessageContext arg0) {
		Boolean outbound = (Boolean) arg0.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outbound) {
			return handleOutboundMessage(arg0);
		}else{
			return handleInboundMessage(arg0);
		}
	}

	private boolean handleOutboundMessage(SOAPMessageContext smc) {
		Date reqTime = (Date) smc.get(CONTEXT_REQ_TIME);
		Key lastSessionKey = (Key) smc.get(CONTEXT_LAST_SESSION);


		try {
			if(reqTime != null && lastSessionKey != null){
				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();

				// add header
				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();

				CipheredView cipheredReqTime = new RequestTime(reqTime).cipher(lastSessionKey);

				// add header element (name, namespace prefix, namespace)
				Name name = se.createName(REQ_TIME_HEADER, SOAP_PREFIX, SERVER_NS);
				SOAPHeaderElement reqTimeHeader = sh.addHeaderElement(name);
				
				String reqTimeString = printBase64Binary(cipheredReqTime.getData());
				reqTimeHeader.addTextNode(reqTimeString);

			}
		} catch (SOAPException e) {
			throw new RuntimeException("error handling header in outbound");
		} catch (KerbyException e) {
			throw new RuntimeException("error ciphering date in outbound");
		}

		return true;
	}

	private boolean handleInboundMessage(SOAPMessageContext smc) {
		Key serverKey = getServerKey();

		SOAPMessage msg = smc.getMessage();
		SOAPPart sp = msg.getSOAPPart();

		// get soap envelope
		SOAPEnvelope se;
		try {
			se = sp.getEnvelope();
		} catch (SOAPException e) { throw new RuntimeException("Error opening SOAP envelope"); }

		if( !needsAuthentication(msg) ){
			return true;
		}

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

		if( !isReqTimeFresh(reqTime)){
			throw new RuntimeException("Request Time is not fresh");
		}

		//user has permission for operation
		if( !hasPermission(auth.getX(), msg)){
			throw new RuntimeException("User " + auth.getX() + " has no permission for requested operation." );
		}
		return true;
	}

	private boolean hasPermission(String username, SOAPMessage msg) {
		try {

			NodeList email = msg.getSOAPBody().getElementsByTagName(SOAP_EMAIL_TAG);

			// if request has email
			if(email.getLength() > 0) {
				return username.equals(msg.getSOAPBody().getElementsByTagName(SOAP_EMAIL_TAG).item(0).getTextContent());
			}

			// if there's no email field, every user has permissions to access
			return true;

		} catch (SOAPException e) {
			return false;
		}
	}

	private boolean isReqTimeFresh(Date reqTime) {
		Date current = new Date();
		return current.getTime() - 500 < reqTime.getTime() && reqTime.getTime() < current.getTime() + 500;
	}

	private boolean needsAuthentication(SOAPMessage msg) {
		return true;
//		try {
//			return msg.getSOAPBody().getElementsByTagName(SOAP_EMAIL_TAG).getLength() > 0;
//		} catch (SOAPException e) {
//			throw new RuntimeException("Can't find SoapMessage body while searching for email");
//		}
	}


	private SOAPElement getHeaderElementByName(SOAPEnvelope se, String localName) throws SOAPException {
		Name name = se.createName(localName, BinasServerHandler.SOAP_PREFIX, BinasServerHandler.KERBY_NS);
		Iterator<?> it = se.getHeader().getChildElements(name);

		// check header element
		if (!it.hasNext()) {
			System.out.println("Header element not found.");
			return null;
		}
		return (SOAPElement) it.next();
	}


	private Key getServerKey(){
		try {
			return SecurityHelper.generateKeyFromPassword(serverPassword);

		}catch(Exception e) {
			throw new RuntimeException("Error: generation user key failed.");
		}
	}

	private Ticket extractTicket(SOAPEnvelope se, Key serverKey) throws KerbyException {
		CipheredView cipheredTicket;
		try {
			byte[] result = parseBase64Binary(getHeaderElementByName(se, TICKET_HEADER).getValue());
			cipheredTicket = new CipherClerk().cipherBuild(result);
		} catch (SOAPException e) {  throw new RuntimeException("Unable to decipher Ticket"); }

		return new Ticket(cipheredTicket, serverKey);
	}

	private Auth extractAuth(SOAPEnvelope se, Key sessionKey) throws KerbyException {
		CipheredView cipheredAuth;
		try {
			byte[] result = parseBase64Binary(getHeaderElementByName(se, AUTH_HEADER).getValue());
			cipheredAuth = new CipherClerk().cipherBuild(result);
		} catch (SOAPException e) {  throw new RuntimeException("Unable to decipher Ticket"); }

		return new Auth(cipheredAuth, sessionKey);
	}
}

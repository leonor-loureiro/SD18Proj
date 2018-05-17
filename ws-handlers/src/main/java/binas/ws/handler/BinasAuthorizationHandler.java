package binas.ws.handler;


import java.util.*;

import java.security.Key;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.NodeList;
import pt.ulisboa.tecnico.sdis.kerby.*;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

/**
 * Checks if the client has permissions to execute the operation
 */
public class BinasAuthorizationHandler implements SOAPHandler<SOAPMessageContext> {
	
	// EMAIL TAG
	private static final String SOAP_EMAIL_TAG = "email";

	// CONTEXT
	private static final String CONTEXT_REQ_TIME = "context_time";
	private static final String CONTEXT_LAST_SESSION = "context_session_key";
	private static final String CONTEXT_USERNAME = "context_username";

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
		if (!outbound) {
			// Get reqTime and authorized username
			Date reqTime = (Date) arg0.get(CONTEXT_REQ_TIME);
			String username = (String) arg0.get(CONTEXT_USERNAME);
			

			// Verifies freshness of request time
			if( !isReqTimeFresh(reqTime)){
				throw new RuntimeException("Request Time is not fresh");
			}

			SOAPMessage msg = arg0.getMessage();
			
			//user has permission for operation
			if( !hasPermission(username, msg)){
				throw new RuntimeException("User " + username + " has no permission for requested operation." );
			}
		}
		return true;
	}

	/**
	 * Checks if the user has permission to execute the operation
	 */
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

	/**
	 * Checks if given time matches the current time, with an maximum offset of 500 milliseconds
	 */
	private boolean isReqTimeFresh(Date reqTime) {
		Date current = new Date();
		return current.getTime() - 600 < reqTime.getTime() && reqTime.getTime() < current.getTime() + 600;
	}

}


package binas.ws.handler;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.NodeList;

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
	
	// REQUEST TIMES
	private static Map<String,Date> oldRequestTimes = new HashMap<>();

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
			if( !isReqTimeFresh(reqTime, username)){
				throw new RuntimeException("Request Time is not fresh");
			}
			
			oldRequestTimes.put(username, reqTime);

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
	private boolean isReqTimeFresh(Date reqTime, String username) {
		Date oldReqTime = oldRequestTimes.get(username); 
		if(oldReqTime!= null && oldReqTime.equals(reqTime))
			throw new RuntimeException("This request has been sent before.");
		
		Date current = new Date();
		return current.getTime() - 750 < reqTime.getTime() && reqTime.getTime() < current.getTime() + 750;
	}

}


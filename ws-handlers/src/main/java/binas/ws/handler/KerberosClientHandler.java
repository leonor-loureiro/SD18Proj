package binas.ws.handler;

import java.security.Key;
import java.util.Date;
import java.util.Random;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
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
import pt.ulisboa.tecnico.sdis.kerby.SecurityHelper;
import pt.ulisboa.tecnico.sdis.kerby.SessionKey;
import pt.ulisboa.tecnico.sdis.kerby.SessionKeyAndTicketView;
import pt.ulisboa.tecnico.sdis.kerby.cli.KerbyClient;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;

public class KerberosClientHandler implements SOAPHandler<SOAPMessageContext>{

	public static final String username = "alice@T08.binas.org";
	public static final String userPassword = "WxzsYKnJn";
	
	public static final String servername = "binas@T08.binas.org";
	public static final String serverPassword = "t5h9O9B2";
	
	public static final String url = "http://sec.sd.rnl.tecnico.ulisboa.pt:8888/kerby";
	public static final String uddiUrl = "http://localhost:9090";
	public static final String name = "kerby";
	
	// Header names
	public static final String TICKET_HEADER = "ticketHeader";
	public static final String AUTH_HEADER = "authHeader";

	// Namespace
	public static final String KERBY_NS = "urn:binas.client.kerby";

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
	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		if(outbound) {
			return handleOutboundMessage(smc);
		}
		return false;
	}

	@Override
	public Set<QName> getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private boolean handleOutboundMessage(SOAPMessageContext smc) {
		
		Key clientKey = null;
		try {
			clientKey = SecurityHelper.generateKeyFromPassword(userPassword);
			
		}catch(Exception e) {
			throw new RuntimeException("Error: generation user key failed.");
		}
		
		KerbyClient client = null;
		try {
			client = new KerbyClient(url);	
		
		}catch(Exception e) {
			throw new RuntimeException("Error: creation of KerbyClient instance failed.");
		}
		
		CipheredView cipheredSessionKey = null;
		CipheredView cipheredTicket = null;
		SessionKey sessionKey = null;

		// New session request
		try {
			SessionKeyAndTicketView result = client.requestTicket(username, servername,
			        			new Random().nextLong(), 60 /* seconds */);

			cipheredSessionKey = result.getSessionKey(); 

	        cipheredTicket = result.getTicket();
	        
	        //Decipher session key
	        sessionKey = new SessionKey(cipheredSessionKey, clientKey); 
	        
		} catch (BadTicketRequest_Exception e1) {
			throw new RuntimeException("Error: new session request failed.");
		} catch (KerbyException e) {
			throw new RuntimeException("Error: sesssion key deciphering failed.");
		}
		
		// Generate new authenticator
		Date reqTime = new Date();
        Auth auth = new Auth(username, reqTime);
        
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
			Name ticketHeaderName = se.createName(TICKET_HEADER, "b", KERBY_NS);
			SOAPHeaderElement ticketHeader = sh.addHeaderElement(ticketHeaderName);
			
			String ticketString = printBase64Binary(cipheredTicket.getData());
			ticketHeader.addTextNode(ticketString);
			
			//Create new header with authenticator
			Name authHeaderName = se.createName(AUTH_HEADER, "b", KERBY_NS);
			SOAPHeaderElement authHeader = sh.addHeaderElement(authHeaderName);
			
			String authString = printBase64Binary(cipheredAuth.getData());
			authHeader.addTextNode(authString);			
		}catch (SOAPException e) {
			System.out.printf("Failed to add SOAP header because of %s%n", e);
			throw new RuntimeException("Error: Failed to add SOAP header.");
		}
		
		return true;
	}

}

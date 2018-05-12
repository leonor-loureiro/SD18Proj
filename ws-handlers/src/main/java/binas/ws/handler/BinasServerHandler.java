package binas.ws.handler;


import java.util.*;

import java.security.Key;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import pt.ulisboa.tecnico.sdis.kerby.*;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;

public class BinasServerHandler implements SOAPHandler<SOAPMessageContext> {

	private final static int VALID_DURATION = 30;

	private static final String servername = "binas@T08.binas.org";
	private static final String serverPassword = "t5h9O9B2";

	private static final String url = "http://sec.sd.rnl.tecnico.ulisboa.pt:8888/kerby";
	private static final String uddiUrl = "http://localhost:9090";
	private static final String name = "kerby";

	// Header names
	private static final String TICKET_HEADER = "ticketHeader";
	private static final String AUTH_HEADER = "authHeader";

	// Namespace
	private static final String KERBY_NS = "urn:binas.client.kerby";

	// PREFIX
	private static final String SOAP_PREFIX = "b";

	private static final String SOAP_EMAIL_TAG = "email";

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
		//TODO!!!!!!!!!!!!!
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

		//	1.	Extrair o ticket e decifra-lo, obtendo a chave de sessão
		Ticket ticket;
		try {
			ticket = extractTicket(se, serverKey);
		} catch (KerbyException e) {  throw new RuntimeException("Unable to extract ticket"); }

		Key sessionKey = ticket.getKeyXY();


		//	2.	Verificar que o nome do servidor no ticket está correto
		if( !servername.equals(ticket.getY()) ){
			throw new RuntimeException("Servername does not match ticket servername");
		}


		//	3.	Extrair o autentificador e decifra-lo
		Auth auth;
		try {
			auth = extractAuth(se, sessionKey);
		} catch (KerbyException e) { throw new RuntimeException("unable to extract auth"); }


		//	4.	Verificar que o username no ticket coincide com o username no autentificador
		if( !auth.getX().equals(ticket.getX()) ){
			throw new RuntimeException("Authentication does not match ticket user");
		}


		//	5.	Guardar o request time para posteriormente incluir na mensagem de resposta
		//TODO :(

		return true;
	}

	private boolean needsAuthentication(SOAPMessage msg) {

		try {
			return msg.getSOAPBody().getElementsByTagName(SOAP_EMAIL_TAG).getLength() > 0;
		} catch (SOAPException e) {
			throw new RuntimeException("Can't find SoapMessage body while searching for email");
		}
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

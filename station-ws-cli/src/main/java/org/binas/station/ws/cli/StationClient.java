package org.binas.station.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;

import javax.xml.ws.BindingProvider;

import org.binas.station.ws.BadInit_Exception;
import org.binas.station.ws.BalanceView;
import org.binas.station.ws.InvalidEmail_Exception;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.binas.station.ws.NoSlotAvail_Exception;
import org.binas.station.ws.StationPortType;
import org.binas.station.ws.StationService;
import org.binas.station.ws.StationView;
import org.binas.station.ws.UserNotExists_Exception;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

/**
 * Client port wrapper.
 *
 * Adds easier end point address configuration to the Port generated by
 * wsimport.
 */

public class StationClient implements StationPortType {

	/** WS service */
	StationService service = null;

	/** WS port (port type is the interface, port is the implementation) */
	StationPortType port = null;

	/** UDDI server URL */
	private String uddiURL = null;

	/** WS name */
	private String wsName = null;

	/** WS end point address */
	private String wsURL = null; // default value is defined inside WSDL

	public String getWsURL() {
		return wsURL;
	}

	/** output option **/
	private boolean verbose = false;

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/** constructor with provided web service URL */
	public StationClient(String wsURL) throws StationClientException {
		this.wsURL = wsURL;
		createStub();
	}

	/** constructor with provided UDDI location and name */
	public StationClient(String uddiURL, String wsName) throws StationClientException {
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		uddiLookup();
		createStub();
	}

	/** UDDI lookup */
	private void uddiLookup() throws StationClientException {
		try {
			UDDINaming uddiNaming = new UDDINaming(uddiURL);
			wsURL = uddiNaming.lookup(wsName);
		}catch(UDDINamingException e) {
			if(verbose) {
				System.out.printf("Caught exception on uddiLookup: %s%n", e);
			}
			throw new StationClientException();
		}
		if(wsURL == null) {
			System.out.println(wsName + ": not found!");
			return;
		}
		System.out.println("Found " + wsURL);
		
	}


	/** Stub creation and configuration */
	private void createStub() {
		 if (verbose)
			 System.out.println("Creating stub ...");
		 
		 service = new StationService();
		 port = service.getStationPort();
		
		 if (wsURL != null) {
			 if (verbose)
				 System.out.println("Setting endpoint address ...");
			 
			 BindingProvider bindingProvider = (BindingProvider) port;
			 Map<String, Object> requestContext = bindingProvider.getRequestContext();
			 requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
		 }
	}

	// remote invocation methods ----------------------------------------------

	 @Override
	 public StationView getInfo() {
		 return port.getInfo();
	 }
	
	 @Override
	 public void getBina() throws NoBinaAvail_Exception {
		 port.getBina();
	 }
	
	 @Override
	 public int returnBina() throws NoSlotAvail_Exception {
		 return port.returnBina();
	 }
	 
	 @Override
	public BalanceView getBalance(String email) throws UserNotExists_Exception {
		return port.getBalance(email);
	}

	@Override
	public void setBalance(String email, int value, int tag) throws InvalidEmail_Exception {
		port.setBalance(email, value, tag);
	}

	// test control operations ------------------------------------------------

	 @Override
	 public String testPing(String inputMessage) {
		 return port.testPing(inputMessage);
	 }
	
	 @Override
	 public void testClear() {
		 port.testClear();
	 }
	
	 @Override
	 public void testInit(int x, int y, int capacity, int returnPrize) throws
		 BadInit_Exception {
		 port.testInit(x, y, capacity, returnPrize);
	 }

}

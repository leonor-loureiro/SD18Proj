package org.binas.ws;

import java.io.IOException;

import javax.xml.ws.Endpoint;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

public class BinasEndpointManager {
	
	/** UDDI naming server location */
	private String uddiURL = null;
	/** Web Service name */
	private String wsName = null;

	/** Get Web Service UDDI publication name */
	public String getWsName() {
		return wsName;
	}
	
	/** Obtain Port implementation */
	 public BinasPortType getPort() {
		 return portImpl;
	 }
	
	/** Web Service location to publish */
	private String wsURL = null;

	/** Port implementation */
	private BinasPortImpl portImpl = new BinasPortImpl(this);
	
	/** Web Service end point */
	private Endpoint endpoint = null;

	 /** UDDI Naming instance for contacting UDDI server */
	 private UDDINaming uddiNaming = null;
	
	 /** Get UDDI Naming instance for contacting UDDI server */
	 UDDINaming getUddiNaming() {
		 return uddiNaming;
	 }

	/** output option */
	private boolean verbose = true;

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/** constructor with provided UDDI location, WS name, and WS URL */
	public BinasEndpointManager(String uddiURL, String wsName, String wsURL) {
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		this.wsURL = wsURL;
	}

	/** constructor with provided web service URL */
	public BinasEndpointManager(String wsName, String wsURL) {
		this.wsName = wsName;
		this.wsURL = wsURL;
	}

	/* end point management */

	public void start() throws Exception {
		try {
			// publish end point
			endpoint = Endpoint.create(this.portImpl);
			if (verbose) {
				System.out.printf("Starting %s%n", wsURL);
			}
			endpoint.publish(wsURL);
		} catch (Exception e) {
			endpoint = null;
			if (verbose) {
				System.out.printf("Caught exception when starting: %s%n", e);
				e.printStackTrace();
			}
			throw e;
		}
		publishToUDDI();
	}

	public void awaitConnections() {
		if (verbose) {
			System.out.println("Awaiting connections");
			System.out.println("Press enter to shutdown");
		}
		try {
			System.in.read();
		} catch (IOException e) {
			if (verbose) {
				System.out.printf("Caught i/o exception when awaiting requests: %s%n", e);
			}
		}
	}

	public void stop() throws Exception {
		try {
			if (endpoint != null) {
				// stop end point
				endpoint.stop();
				if (verbose) {
					System.out.printf("Stopped %s%n", wsURL);
				}
			}
		} catch (Exception e) {
			if (verbose) {
				System.out.printf("Caught exception when stopping: %s%n", e);
			}
		}
		this.portImpl = null;
		unpublishFromUDDI();
	}

	/* UDDI */

	void publishToUDDI() throws Exception {
		uddiNaming = new UDDINaming(uddiURL);
		uddiNaming.rebind(wsName, wsURL);
		
	}

	void unpublishFromUDDI() {
		try {
			if(uddiNaming != null) {
				uddiNaming.unbind(wsName);
				System.out.printf("Deleted '%s' from UDDI%n", wsName);
			}
		}catch(Exception e) {
			if (verbose) {
				System.out.printf("Caught exception when stopping: %s%n", e);
			}
		}
		
	}
}

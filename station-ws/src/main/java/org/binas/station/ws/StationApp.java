package org.binas.station.ws;

import org.binas.station.domain.Station;

/**
 * The application is where the service starts running. The program arguments
 * are processed here. Other configurations can also be done here.
 */
public class StationApp {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + StationApp.class.getName() + "wsName wsURL OR wsName wsURL uddiURL");
			return;
		}
		String wsName = args[0];
		String wsURL = args[1];
		
		// TODO handle UDDI arguments

		StationEndpointManager endpoint = new StationEndpointManager(wsName, wsURL);
		Station.getInstance().setId(wsName);

		System.out.println(StationApp.class.getSimpleName() + " running");

		// TODO start Web Service
		// try {
		// endpoint.start();
		// endpoint.awaitConnections();
		// } finally {
		// endpoint.stop();
		// }

	}

}
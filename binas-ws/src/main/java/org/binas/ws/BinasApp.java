package org.binas.ws;

import org.binas.domain.BinasManager;

public class BinasApp {

	public static void main(String[] args) throws Exception {
		
		// Check arguments
		if (args.length < 5) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + BinasApp.class.getName() + "wsName wsURL OR wsName wsURL uddiURL");
			return;
		}
		String wsName = args[0];
		String wsURL = args[1];
		String UDDINaming = args[2];
		String stationWSName = args[3];
		String numberOfStations = args[4];


		BinasEndpointManager endpoint = new BinasEndpointManager(UDDINaming, wsName, wsURL);
		BinasManager.getInstance().setId(wsName);
		BinasManager.getInstance().setUddiUrl(UDDINaming);
		BinasManager.getInstance().setStationWSName(stationWSName);
		BinasManager.getInstance().setQ(Integer.parseInt(numberOfStations));

		System.out.println(BinasApp.class.getSimpleName() + " running with stations name: " + stationWSName);
		System.out.println("Setup for " + numberOfStations + " stations for the replication system.");
		
		try {
			endpoint.start();
			endpoint.awaitConnections();
		 } finally {
			 endpoint.stop();
		 }

	}
}

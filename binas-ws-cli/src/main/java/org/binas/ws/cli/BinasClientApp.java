package org.binas.ws.cli;

import java.util.List;

import org.binas.ws.CoordinatesView;
import org.binas.ws.StationView;

public class BinasClientApp {

    public static void main(String[] args) throws Exception {
        // Check arguments
        if (args.length == 0) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + BinasClientApp.class.getName()
                    + " wsURL OR uddiURL wsName");
            return;
        }
        String uddiURL = null;
        String wsName = null;
        String wsURL = null;
        if (args.length == 1) {
            wsURL = args[0];
        } else if (args.length >= 2) {
            uddiURL = args[0];
            wsName = args[1];
        }

		System.out.println(BinasClientApp.class.getSimpleName() + " running");

        // Create client
        BinasClient client = null;

        if (wsURL != null) {
            System.out.printf("Creating client for server at %s%n", wsURL);
            client = new BinasClient(wsURL);
        } else if (uddiURL != null) {
            System.out.printf("Creating client using UDDI at %s for server with name %s%n",
                uddiURL, wsName);
            client = new BinasClient(uddiURL, wsName);
        }

        // the following remote invocations are just basic examples
        // the actual tests are made using JUnit

		 System.out.println("Invoke ping()...");
		 String result = client.testPing("client");
		 System.out.print(result);
		 
		 
		 client.testInit(10);
		 client.activateUser("username@domain");
		 int credit = client.getCredit("username@domain");
		 System.out.println("username@domain credit = " + credit);
		 
		 client.rentBina("T08_Station1","username@domain");
		 
		 System.out.println(client.getInfoStation("T08_Station1").getFreeDocks());
		 
		 client.returnBina("T08_Station1","username@domain");
		 System.out.println("Tried to return bina");
		 
		 CoordinatesView coord = new CoordinatesView();
		 coord.setX(1);
		 coord.setY(1);
		 
		 List<StationView> stations = client.listStations(new Integer(1), coord);
		 System.out.println("Were stations found?: " + !stations.isEmpty());
		 
		 for (StationView s : stations)
			 System.out.println(s.getFreeDocks());
	 }
}


package pt.ulisboa.tecnico.sdis.kerby.cli;

import java.util.Random;

import pt.ulisboa.tecnico.sdis.kerby.SessionKeyAndTicketView;

public class KerbyClientApp {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length == 0) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + KerbyClientApp.class.getName() + " wsURL OR uddiURL wsName");
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

		// Create client
		KerbyClient client = null;

		if (wsURL != null) {
			System.out.printf("Creating client for server at %s%n", wsURL);
			client = new KerbyClient(wsURL);
		} else if (uddiURL != null) {
			System.out.printf("Creating client using UDDI at %s for server with name %s%n", uddiURL, wsName);
			client = new KerbyClient(uddiURL, wsName);
		}

		// the following remote invocations are just basic examples
		// the actual tests are made using JUnit

		System.out.println("Invoke dummy()...");
		SessionKeyAndTicketView result = client.requestTicket("alice@CXX.binas.org", "binas@CXX.binas.org",
				new Random().nextLong(), 60 /* seconds */);
		System.out.print("Result: ");
		System.out.println(result);

	}

}

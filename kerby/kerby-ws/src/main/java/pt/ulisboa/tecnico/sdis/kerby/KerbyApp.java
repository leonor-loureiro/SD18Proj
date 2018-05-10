package pt.ulisboa.tecnico.sdis.kerby;

/**
 * Kerby Web Service application.
 * 
 * @author Miguel Pardal
 *
 */
public class KerbyApp {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length < 2 || args.length > 5) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + KerbyApp.class.getName() + " wsURL passwordFile <saltFile> OR uddiURL wsName wsURL passwordFile <saltFile>");
			return;
		}

		String uddiURL = null;
		String wsName = null;
		String wsURL = null;
		
		// Create server implementation object, according to options
		KerbyEndpointManager endpoint = null;
		if (args.length == 2 || args.length == 3) {
			wsURL = args[0];
			if(args.length == 3)
				KerbyManager.getInstance().initSalt(args[2]);
			else
				System.out.println("WARNING: Using default Salt from kerby-lib for Key Generation.");
			
			KerbyManager.getInstance().initKeys(args[1]);
						
			endpoint = new KerbyEndpointManager(wsURL);

		} else if (args.length == 4 || args.length == 5) {
			uddiURL = args[0];
			wsName = args[1];
			wsURL = args[2];
			if(args.length == 5)
				KerbyManager.getInstance().initSalt(args[4]);
			else
				System.out.println("WARNING: Using default Salt from kerby-lib for Key Generation.");
			
			KerbyManager.getInstance().initKeys(args[3]);
			
			endpoint = new KerbyEndpointManager(uddiURL, wsName, wsURL);
			endpoint.setVerbose(true);
		}
		
		try {
			endpoint.start();
			endpoint.awaitConnections();
		} finally {
			endpoint.stop();
		}

	}

}
package pt.ulisboa.tecnico.sdis.kerby;

import javax.jws.WebService;

/**
 * Kerby Web Service implementation class.
 * 
 * @author Miguel Pardal
 *
 */
@WebService(endpointInterface = "pt.ulisboa.tecnico.sdis.kerby.KerbyPortType",
wsdlLocation = "KerbyService.wsdl",
name ="KerbyService",
portName = "KerbyPort",
targetNamespace="http://kerby.sdis.tecnico.ulisboa.pt/",
serviceName = "KerbyService"
)
public class KerbyPortImpl implements KerbyPortType {

	// end point manager
	private KerbyEndpointManager endpointManager;

	public KerbyPortImpl(KerbyEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
	}

	@Override
	public SessionKeyAndTicketView requestTicket(String client, String server, long nounce, int ticketDuration) 
			throws BadTicketRequest_Exception {
		SessionKeyAndTicketView result = new SessionKeyAndTicketView();
		KerbyManager manager = KerbyManager.getInstance();
		
		try {
			result = manager.requestTicket(client, server, nounce, ticketDuration);
		} catch (BadTicketRequestException e) {
			throwBadTicketRequest(e.getMessage());
		}
		
		return result;
	}

	
	// Exception helper -----------------------------------------------------
	
	private void throwBadTicketRequest(final String message) 
			throws BadTicketRequest_Exception {
		BadTicketRequest faultInfo = new BadTicketRequest();
		faultInfo.setMessage(message);
		throw new BadTicketRequest_Exception(message, faultInfo);
	}

}


package org.binas.ws;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;

import org.binas.domain.BinasManager;
import org.binas.domain.User;
import org.binas.domain.exception.AlreadyHasBinaException;
import org.binas.domain.exception.EmailExistsException;
import org.binas.domain.exception.InvalidEmailException;
import org.binas.domain.exception.InvalidStationException;
import org.binas.domain.exception.NoBinaAvailException;
import org.binas.domain.exception.NoBinaRentedException;
import org.binas.domain.exception.NoCreditException;
import org.binas.domain.exception.UserNotExistsException;
import org.binas.station.ws.NoSlotAvail_Exception; // FIXME is this correct?!
import org.binas.station.ws.cli.StationClientException;

/**
 * This class implements the Web Service port type (interface). The annotations
 * below "map" the Java class to the WSDL definitions.
 */

@WebService(
	endpointInterface = "org.binas.ws.BinasPortType",
	wsdlLocation = "binas.1_0.wsdl", 
	name = "BinasWebService", 
	portName = "BinasPort", 
	targetNamespace = "http://ws.binas.org/", 
	serviceName = "BinasService"
)
public class BinasPortImpl implements BinasPortType {

	private BinasEndpointManager endpointManager;

	/** Constructor receives a reference to the endpoint manager. */
	public BinasPortImpl(BinasEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
		BinasManager.getInstance().setUddiUrl(this.endpointManager.getUddiURL());
	}

	@Override
	public List<StationView> listStations(Integer numberOfStations, CoordinatesView coordinates) {
		List<StationView> list = new ArrayList<>();
		if (numberOfStations == null) {
			return list;
		}

		try {
			List<org.binas.station.ws.StationView> temp;
			temp = BinasManager.getInstance().listStations((int)numberOfStations, buildCoordinatesView(coordinates));

			for ( org.binas.station.ws.StationView view : temp)
				list.add(buildStationView(view));
			
			return list;
		} catch (StationClientException e) {
			return list;
		}
	}

	@Override
	public StationView getInfoStation(String stationId) throws InvalidStation_Exception {
		try {
			return buildStationView(
					BinasManager.getInstance().getInfoStation(stationId)
					);
		}catch(InvalidStationException ise) {
			throwInvalidStation("Station " + stationId + " does not exist.");
		}
		return null;
	}

	@Override
	public int getCredit(String email) throws UserNotExists_Exception {
		try {
			return BinasManager.getInstance().getCredit(email);
		} catch (UserNotExistsException unee) {
			throwUserNotExists("getCredit: no user with given email");
		}
		return 0;
	}

	@Override
	public UserView activateUser(String email) throws EmailExists_Exception, InvalidEmail_Exception {
		try {
			return buildUserView(BinasManager.getInstance().activateUser(email));
		} catch (EmailExistsException eee) {
			throwEmailExists("activateUser: email taken");
		} catch (InvalidEmailException iee) {
			throwInvalidEmail("activateUser: email format invalid");
		}
		return null;
	}

	@Override
	public void rentBina(String stationId, String email) throws AlreadyHasBina_Exception, InvalidStation_Exception,
			NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception {
		try{
			BinasManager.getInstance().rentBina(stationId, email);
		}catch(AlreadyHasBinaException ahbe) {
			throwAlreadyHasBina("User already has a bina.");
			
		}catch(InvalidStationException ise) {
			throwInvalidStation("Station " + stationId + " does not exist.");
			
		}catch(NoCreditException nce) {
			throwNoCredit("User does not have credit.");
			
		}catch(UserNotExistsException unee) {
			throwUserNotExists("No user " + email + ".");
			
		}catch(NoBinaAvailException nbae) {
			throwNoBinaAvail("No bina available in station " +  stationId + ".");
		}

	}

	@Override
	public void returnBina(String stationId, String email)
			throws FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {
		try {
			BinasManager.getInstance().returnBina(stationId, email);
			
		} catch (InvalidStationException e) {
			throwInvalidStation("Station "+ stationId+ " does not exists.");
			
		} catch (UserNotExistsException e) {
			throwUserNotExists("No user " + email + ".");
			
		} catch (NoSlotAvail_Exception e) {
			throwFullStation("Station " + stationId+ " is full.");
			
		} catch (NoBinaRentedException e) {
			throwNoBinaRented("User " + email + " has no bina rented to return.");
			
		}

	}
	// Test Control operations -----------------------------------------------

	@Override
	public String testPing(String inputMessage) {

		return BinasManager.getInstance().testPing(this.endpointManager.getWsName());
//		// If no input is received, return a default name.
//		if (inputMessage == null || inputMessage.trim().length() == 0)
//			inputMessage = "Buddy!";
//
//		// If the station does not have a name, return a default.
//		String wsName = endpointManager.getWsName();
//		if (wsName == null || wsName.trim().length() == 0)
//			wsName = "Binas";
//
//		// Build a string with a message to return.
//		StringBuilder builder = new StringBuilder();
//		builder.append("Hello ").append(inputMessage);
//		builder.append(" from ").append(wsName);
//		return builder.toString();
	}

	@Override
	public void testClear() {
		BinasManager.getInstance().clear();
		BinasManager.getInstance().setUserInitialPoints(0);
	}

	@Override
	public void testInitStation(String stationId, int x, int y, int capacity, int returnPrize)
			throws BadInit_Exception {
		try {
			BinasManager.getInstance().initStation(stationId, x, y, capacity, returnPrize);			
		}catch(InvalidStationException e) {
			throwBadInit("Station "+ stationId+ "does not exists.");
		}catch(Exception e) {
			throwBadInit("Invalid init values");
		}
	}

	@Override
	public void testInit(int userInitialPoints) throws BadInit_Exception {
		BinasManager.getInstance().setUserInitialPoints(userInitialPoints);
	}

	// View helpers ----------------------------------------------------------

	/** Helper to convert a domain coordinates to a view. */
	private CoordinatesView buildCoordinatesView(org.binas.station.ws.CoordinatesView coordinates) {
		CoordinatesView view = new CoordinatesView();
		view.setX(coordinates.getX());
		view.setY(coordinates.getY());
		return view;
	}

	/** Helper to convert a view coordinates to doamin coordinates. */
	private org.binas.station.ws.CoordinatesView buildCoordinatesView(CoordinatesView coordinates) {
		org.binas.station.ws.CoordinatesView view = new org.binas.station.ws.CoordinatesView();
		view.setX(coordinates.getX());
		view.setY(coordinates.getY());
		return view;
	}

	/** Helper to convert a domain user to a view. */
	private UserView buildUserView(User user) {
		UserView view = new UserView();
		view.setEmail(user.getEmail());
		view.setHasBina(user.getHasBina());
		view.setCredit(user.getCredit());
		return view;
	}

	/** Helper to convert a domain station to a view. */
	private StationView buildStationView(org.binas.station.ws.StationView station) {
		StationView view = new StationView();
		view.setId(station.getId());
		view.setCoordinate(buildCoordinatesView(station.getCoordinate()));
		view.setCapacity(station.getCapacity());
		view.setTotalGets(station.getTotalGets());
		view.setTotalReturns(station.getTotalReturns());
		view.setFreeDocks(station.getFreeDocks());
		view.setAvailableBinas(station.getAvailableBinas());
		return view;
	}

	// Exception helpers -----------------------------------------------------

	/** Helper to throw a new NoBinaAvail exception. */
	private void throwNoBinaAvail(final String message) throws NoBinaAvail_Exception {
		NoBinaAvail faultInfo = new NoBinaAvail();
		faultInfo.message = message;
		throw new NoBinaAvail_Exception(message, faultInfo);
	}

	/** Helper to throw a new AlreadyExists exception. */
	private void throwAlreadyHasBina(final String message) throws AlreadyHasBina_Exception {
		AlreadyHasBina faultInfo = new AlreadyHasBina();
		faultInfo.message = message;
		throw new AlreadyHasBina_Exception(message, faultInfo);
	}

	/** Helper to throw a new EmailExists exception. */
	private void throwEmailExists(final String message) throws EmailExists_Exception {
		EmailExists faultInfo = new EmailExists();
		faultInfo.message = message;
		throw new EmailExists_Exception(message, faultInfo);
	}

	/** Helper to throw a new FullStation exception. */
	private void throwFullStation(final String message) throws FullStation_Exception {
		FullStation faultInfo = new FullStation();
		faultInfo.message = message;
		throw new FullStation_Exception(message, faultInfo);
	}

	/** Helper to throw a new InvalidEmail exception. */
	private void throwInvalidEmail(final String message) throws InvalidEmail_Exception {
		InvalidEmail faultInfo = new InvalidEmail();
		faultInfo.message = message;
		throw new InvalidEmail_Exception(message, faultInfo);
	}

	/** Helper to throw a new InvalidStation exception. */
	private void throwInvalidStation(final String message) throws InvalidStation_Exception {
		InvalidStation faultInfo = new InvalidStation();
		faultInfo.message = message;
		throw new InvalidStation_Exception(message, faultInfo);
	}

	/** Helper to throw a new InvalidEmail exception. */
	private void throwNoBinaRented(final String message) throws NoBinaRented_Exception {
		NoBinaRented faultInfo = new NoBinaRented();
		faultInfo.message = message;
		throw new NoBinaRented_Exception(message, faultInfo);
	}

	/** Helper to throw a new NoCredit exception. */
	private void throwNoCredit(final String message) throws NoCredit_Exception {
		NoCredit faultInfo = new NoCredit();
		faultInfo.message = message;
		throw new NoCredit_Exception(message, faultInfo);
	}

	/** Helper to throw a new UserNotExists exception. */
	private void throwUserNotExists(final String message) throws UserNotExists_Exception {
		UserNotExists faultInfo = new UserNotExists();
		faultInfo.message = message;
		throw new UserNotExists_Exception(message, faultInfo);
	}

	/** Helper to throw a new BadInit exception. */
	private void throwBadInit(final String message) throws BadInit_Exception {
		BadInit faultInfo = new BadInit();
		faultInfo.message = message;
		throw new BadInit_Exception(message, faultInfo);
	}
}
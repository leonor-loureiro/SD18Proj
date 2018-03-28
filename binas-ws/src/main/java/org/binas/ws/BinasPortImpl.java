
package org.binas.ws;

import java.util.List;

import javax.jws.WebService;

import org.binas.domain.Coordinates;
import org.binas.domain.Station;
import org.binas.domain.User;

/**
 * This class implements the Web Service port type (interface). The annotations
 * below "map" the Java class to the WSDL definitions.
 */

@WebService(
	endpointInterface = "org.binas.ws.BinasPortType",
	wsdlLocation = "binas.1_0.wsdl",
	name ="BinasWebService",
	portName = "BinasPort",	
	targetNamespace="http://ws.binas.org/",
	serviceName = "BinasService"
 )
public class BinasPortImpl implements BinasPortType{

	@Override
	public List<StationView> listStations(Integer numberOfStations, CoordinatesView coordinates) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StationView getInfoStation(String stationId) throws InvalidStation_Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getCredit(String email) throws UserNotExists_Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public UserView activateUser(String email) throws EmailExists_Exception, InvalidEmail_Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void rentBina(String stationId, String email) throws AlreadyHasBina_Exception, InvalidStation_Exception,
			NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void returnBina(String stationId, String email)
			throws FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {
		// TODO Auto-generated method stub
		
	}
	// Test Control operations -----------------------------------------------

	@Override
	public String testPing(String inputMessage) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void testClear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void testInitStation(String stationId, int x, int y, int capacity, int returnPrize)
			throws BadInit_Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void testInit(int userInitialPoints) throws BadInit_Exception {
		// TODO Auto-generated method stub
		
	}
	
	// View helpers ----------------------------------------------------------

	 /** Helper to convert a domain coordinates to a view. */
	 private CoordinatesView buildCoordinatesView(Coordinates coordinates) {
		 CoordinatesView view = new CoordinatesView();
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
	 private StationView buildStationView(Station station) {
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
	 private void throwNoBinaAvail(final String message) throws
		 NoBinaAvail_Exception {
		 NoBinaAvail faultInfo = new NoBinaAvail();
		 faultInfo.message = message;
		 throw new NoBinaAvail_Exception(message, faultInfo);
	 }
	
	 /** Helper to throw a new AlreadyExists exception. */
	 private void throwAlreadyHasBina(final String message) throws
		 AlreadyHasBina_Exception {
		 AlreadyHasBina faultInfo = new AlreadyHasBina();
		 faultInfo.message = message;
		 throw new AlreadyHasBina_Exception(message, faultInfo);
	 }
	
	 /** Helper to throw a new EmailExists exception. */
	 private void throwEmailExists(final String message) throws
		 EmailExists_Exception {
		 EmailExists faultInfo = new EmailExists();
		 faultInfo.message = message;
		 throw new EmailExists_Exception(message, faultInfo);
	 }
	 
	 /** Helper to throw a new FullStation exception. */
	 private void throwFullStation(final String message) throws
		 FullStation_Exception {
		 FullStation faultInfo = new FullStation();
		 faultInfo.message = message;
		 throw new FullStation_Exception(message, faultInfo);
	 }
	 
	 /** Helper to throw a new InvalidEmail exception. */
	 private void throwInvalidEmail(final String message) throws
		 InvalidEmail_Exception {
		 InvalidEmail faultInfo = new InvalidEmail();
		 faultInfo.message = message;
		 throw new InvalidEmail_Exception(message, faultInfo);
	 }
	 
	 /** Helper to throw a new InvalidStation exception. */
	 private void throwInvalidStation(final String message) throws
		 InvalidStation_Exception {
		 InvalidStation faultInfo = new InvalidStation();
		 faultInfo.message = message;
		 throw new InvalidStation_Exception(message, faultInfo);
	 }
	
	 /** Helper to throw a new InvalidEmail exception. */
	 private void throwNoBinaRented(final String message) throws
		 NoBinaRented_Exception {
		 NoBinaRented faultInfo = new NoBinaRented();
		 faultInfo.message = message;
		 throw new NoBinaRented_Exception(message, faultInfo);
	 }
	 
	 /** Helper to throw a new NoCredit exception. */
	 private void throwNoCredit(final String message) throws
		 NoCredit_Exception {
		 NoCredit faultInfo = new NoCredit();
		 faultInfo.message = message;
		 throw new NoCredit_Exception(message, faultInfo);
	 }
	
	 /** Helper to throw a new UserNotExists exception. */
	 private void throwUserNotExists(final String message) throws
		 UserNotExists_Exception {
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
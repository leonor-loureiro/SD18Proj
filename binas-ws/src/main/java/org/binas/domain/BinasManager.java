package org.binas.domain;

import org.binas.domain.exception.*;
import org.binas.station.ws.BadInit_Exception;
import org.binas.station.ws.CoordinatesView;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.binas.station.ws.NoSlotAvail_Exception;
import org.binas.station.ws.StationView;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BinasManager {

	/* Binas Manager ID */
	private String id;
	
	/* UDDI url */
	private String uddiURL;

	/* Binas users */
	private Set<User> users = new HashSet<>();

	/* Station name org template */
	private String stationWSName;
	
	/* New user initial credit */
	private int userInitialPoints = 0;

	// Singleton -------------------------------------------------------------

	private BinasManager() {
	}

	/**
	 * SingletonHolder is loaded on the first execution of Singleton.getInstance()
	 * or the first access to SingletonHolder.INSTANCE, not before.
	 */
	private static class SingletonHolder {
		private static final BinasManager INSTANCE = new BinasManager();
	}

	public static synchronized BinasManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId(String id) {
		return this.id;
	}
	public void setUserInitialPoints(int userInitialPoints) {
		this.userInitialPoints = userInitialPoints;
	}
	
	public int getUserInitialPoints() {
		return this.userInitialPoints;
	}
	
	public void setUddiUrl(String uddiUrl) {
		this.uddiURL = uddiUrl;
	}
	public String getUddiUrl() {
		return uddiURL;
	}

	public void setStationWSName(String stationWSName) {
		this.stationWSName = stationWSName;
	}

	public String getStationWSName() {
		return this.stationWSName;
	}
	
	public User getUserByEmail(String email) throws UserNotExistsException {
		for (User user : users) {
			if (user.getEmail().equals(email))
				return user;
		}
		throw new UserNotExistsException();
	}

	/**
	 * Lists n stacions closest to coords in crescent order.
	 * Connects to UDDI to get all stations and then filters.
	 * @param n
	 * @param coords
	 * @return
	 */
	public List<StationView> listStations(int n, CoordinatesView coords) {
		// Get all stations
		// Filter closest n stations
		return null;
	}

	public User activateUser(String email) throws InvalidEmailException, EmailExistsException {
		if (email == null || !email.matches("^([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+@([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+$")) {
			throw new InvalidEmailException();
		}
		try {
			getUserByEmail(email);

		} catch (UserNotExistsException unee) {
			User user = new User(email, false, userInitialPoints);
			users.add(user);
			return user;
		}
		throw new EmailExistsException();
	}

	public void clearUsers() {
		users.clear();
	}

	public int getCredit(String email) throws UserNotExistsException {
		return getUserByEmail(email).getCredit();
	}

	public void rentBina(String stationId, String email) throws UserNotExistsException, InvalidStationException,
			NoBinaAvailException, AlreadyHasBinaException, NoCreditException {

		User user = getUserByEmail(email);
		if (user.getHasBina()) {
			throw new AlreadyHasBinaException();
		}
		if (user.getCredit() < 0) {
			throw new NoCreditException();
		}
		try {
			StationClient client = new StationClient(uddiURL, stationId);
			client.getBina();
			user.setHasBina(true);
			
		} catch (NoBinaAvail_Exception nbae) {
			throw new NoBinaAvailException();
			
		} catch (StationClientException sce) {
			throw new InvalidStationException();
		}

	}
	
	public StationView getInfoStation(String stationId) throws InvalidStationException {
		try {
			StationClient client = new StationClient(uddiURL, stationId);
			return client.getInfo();
		}catch(StationClientException sce) {
			throw new InvalidStationException();
		}
	}
	
	public void returnBina(String stationId, String email) throws InvalidStationException, 
			UserNotExistsException, NoSlotAvail_Exception, NoBinaRentedException {		
		
		User user = getUserByEmail(email);
		if (!user.getHasBina()) {
			throw new NoBinaRentedException();
		}
		
		try {
			StationClient client = new StationClient(uddiURL, stationId);
			int bonus = client.returnBina();
			user.setHasBina(false);
			user.receiveBonus(bonus);
			
		} catch (StationClientException e) {
			throw new InvalidStationException();
		}
	}
	
	public void initStation(String stationId, int x, int y, int capacity, int returnPrize) throws StationClientException, Exception{
		try {
			StationClient client = new StationClient(uddiURL, stationId);
			client.testInit(x, y, capacity, returnPrize);
		}catch(StationClientException e) {
			throw new InvalidStationException();
		}catch(BadInit_Exception e) {
			throw new Exception();
		}
	}
}

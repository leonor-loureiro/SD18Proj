package org.binas.domain;

import java.util.HashSet;
import java.util.Set;

import org.binas.domain.exception.AlreadyHasBinaException;
import org.binas.domain.exception.EmailExistsException;
import org.binas.domain.exception.InvalidEmailException;
import org.binas.domain.exception.InvalidStationException;
import org.binas.domain.exception.NoBinaAvailException;
import org.binas.domain.exception.NoBinaRentedException;
import org.binas.domain.exception.NoCreditException;
import org.binas.domain.exception.UserNotExistsException;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.binas.station.ws.NoSlotAvail_Exception;
import org.binas.station.ws.StationView;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;

public class BinasManager {

	/* Binas identifier */
	private String id;
	
	/* UDDI url */
	private String uddiURL;

	/* Binas users */
	private Set<User> users = new HashSet<>();

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
	
	public void setUddiUrl(String uddiUrl) {
		this.uddiURL = uddiUrl;
	}
	public String getUddiUrl() {
		return uddiURL;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getId(String id) {
		return this.id;
	}

	public User getUserByEmail(String email) throws UserNotExistsException {
		for (User user : users) {
			if (user.getEmail().equals(email))
				return user;
		}
		throw new UserNotExistsException();
	}

	public User activateUser(String email) throws InvalidEmailException, EmailExistsException {
		if (email == null || !email.matches("^([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+@([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+$")) {
			throw new InvalidEmailException();
		}
		try {
			getUserByEmail(email);

		} catch (UserNotExistsException unee) {
			User user = new User(email, false, 0);
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
			client.returnBina();
			user.setHasBina(false);
		} catch (StationClientException e) {
			throw new InvalidStationException();
		}
	}
}

package ds.gae;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import ds.gae.entities.Car;
import ds.gae.entities.Log;
import ds.gae.entities.CarRentalCompany;
import ds.gae.entities.CarType;
import ds.gae.entities.Quote;
import ds.gae.entities.Reservation;
import ds.gae.entities.ReservationConstraints;

public class CarRentalModel {

	public Map<String,CarRentalCompany> CRCS = new HashMap<String, CarRentalCompany>();	

	private static CarRentalModel instance;

	public static CarRentalModel get() {
		if (instance == null)
			instance = new CarRentalModel();
		return instance;
	}

	/**
	 * Get the car types available in the given car rental company.
	 *
	 * @param 	crcName
	 * 			the car rental company
	 * @return	The list of car types (i.e. name of car type), available
	 * 			in the given car rental company.
	 */
	public Set<String> getCarTypesNames(String crcName) {
		EntityManager em = EMF.get().createEntityManager();
		try{
			Set<String> typeNames = new HashSet<String>();
			Query query = em.createQuery("SELECT crc.cartypes FROM CarRentalCompany crc WHERE crc.name = :name");
			query.setParameter("name", crcName);
			List<HashSet<CarType>> result = query.getResultList();
			Set<CarType> types = result.get(0);
			for (CarType type: types) {
				typeNames.add(type.getName());
			}
			return typeNames;
		} finally {
			em.close();
		}
	}

	/**
	 * Get all registered car rental companies
	 *
	 * @return	the list of car rental companies
	 */
	public Collection<String> getAllRentalCompanyNames() {
		EntityManager em = EMF.get().createEntityManager();
		Collection<String> companies = new ArrayList<String>();
		try{
			Query query = em.createQuery("SELECT crc.name FROM CarRentalCompany crc");
			List<String> res = query.getResultList();
			return res;
		} finally {
			em.close();
		}
	}

	/**
	 * Create a quote according to the given reservation constraints (tentative reservation).
	 * 
	 * @param	company
	 * 			name of the car renter company
	 * @param	renterName 
	 * 			name of the car renter 
	 * @param 	constraints
	 * 			reservation constraints for the quote
	 * @return	The newly created quote.
	 *  
	 * @throws ReservationException
	 * 			No car available that fits the given constraints.
	 */
	public Quote createQuote(String company, String renterName, ReservationConstraints constraints) throws ReservationException {
		EntityManager em = EMF.get().createEntityManager();
		CarRentalCompany crc = null;
		try{
			Query query = em.createQuery("SELECT crc FROM CarRentalCompany crc WHERE crc.name = :name");
			query.setParameter("name", company);
			List<CarRentalCompany> result = query.getResultList();
			if (result.size() > 0) {
				crc = result.get(0);
				Quote q =  crc.createQuote(constraints, renterName);
				return q;
			} else {
				return null;
			}
		} finally {
			em.close();
		}
		/*CarRentalCompany crc = CRCS.get(company);
    	Quote out = null;

        if (crc != null) {
            out = crc.createQuote(constraints, renterName);
        } else {
        	throw new ReservationException("CarRentalCompany not found.");    	
        }

        return out;*/
	}

	/**
	 * Confirm the given quote.
	 *
	 * @param 	q
	 * 			Quote to confirm
	 * 
	 * @throws ReservationException
	 * 			Confirmation of given quote failed.	
	 */
	public Reservation confirmQuote(Quote q) throws ReservationException {
		EntityManager em = EMF.get().createEntityManager();
		EntityTransaction trans = em.getTransaction();
		try {
			trans.begin();
			CarRentalCompany crc = em.find(CarRentalCompany.class, q.getRentalCompany());
			Reservation r = crc.confirmQuote(q);
			trans.commit();
			return r;
		} catch(ReservationException e) {
			trans.rollback();
			throw e;
		} finally {
			em.close();
		}	
	}

	/**
	 * Confirm the given list of quotes
	 * 
	 * @param 	quotes 
	 * 			the quotes to confirm
	 * @return	The list of reservations, resulting from confirming all given quotes.
	 * 
	 * @throws 	ReservationException
	 * 			One of the quotes cannot be confirmed. 
	 * 			Therefore none of the given quotes is confirmed.
	 */
	public List<Reservation> confirmQuotes(List<Quote> quotes) throws ReservationException {  
		HashMap<String, List<Quote>> companies = new HashMap<String, List<Quote>>();
		for (Quote quote: quotes) {
			String crc = quote.getRentalCompany();
			if (companies.containsKey(crc)) {
				List<Quote> qs = companies.get(crc);
				qs.add(quote);
				companies.put(crc, qs);
			} else {
				List<Quote> qs = new ArrayList<Quote>();
				qs.add(quote);
				companies.put(crc, qs);
			}
		}
		List<Reservation> reservations = new ArrayList<Reservation>();
		try {
			for (List<Quote> qs: companies.values()) {
				reservations.addAll(this.confirmQuotesForCompany(qs));
			}
			return reservations;
		} catch (ReservationException e) {
			for (String comp : companies.keySet()){
				List<Reservation> toBeDeleted = new ArrayList<Reservation>(); 
				for (Reservation r: reservations){
					if (r.getRentalCompany().equalsIgnoreCase(comp))
						toBeDeleted.add(r);
				}
				for (Reservation r: toBeDeleted) {
					EntityManager em = EMF.get().createEntityManager();
					try {
						CarRentalCompany crc = em.find(CarRentalCompany.class, r.getRentalCompany());
						crc.cancelReservation(r);
					} finally {
						em.close();
					}
				}
			}
			throw e;
		} 
	}
	
	private List<Reservation> confirmQuotesForCompany(List<Quote> quotes) throws ReservationException {
		List<Reservation> res = new ArrayList<Reservation>();
		EntityManager em = EMF.get().createEntityManager();
		EntityTransaction trans = em.getTransaction();
		try {
			trans.begin();
			for (Quote q : quotes){
				Reservation r = this.confirmQuote(q);
				res.add(r);
			}
			trans.commit();
			return res;
		} catch (Exception c){
			for (Reservation r: res){
				em.find(CarRentalCompany.class, r.getRentalCompany()).cancelReservation(r);
			}
			trans.commit();
			throw c;
		}finally {
			em.close();
		}
	}

	/**
	 * Get all reservations made by the given car renter.
	 *
	 * @param 	renter
	 * 			name of the car renter
	 * @return	the list of reservations of the given car renter
	 */
	public List<Reservation> getReservations(String renter) {
		// use persistence instead
		EntityManager em = EMF.get().createEntityManager();
		try{
			Query query = em.createQuery("SELECT r FROM Reservation r WHERE r.carRenter = :renter");
			query.setParameter("renter", renter);
			List<Reservation> res = query.getResultList();
			return res;
		} finally {
			em.close();
		}


		/*List<Reservation> out = new ArrayList<Reservation>();

		for (CarRentalCompany crc : CRCS.values()) {
			for (Car c : crc.getCars()) {
				for (Reservation r : c.getReservations()) {
					if (r.getCarRenter().equals(renter)) {
						out.add(r);
					}
				}
			}
		}

		return out;*/
	}

	/**
	 * Get the car types available in the given car rental company.
	 *
	 * @param 	crcName
	 * 			the given car rental company
	 * @return	The list of car types in the given car rental company.
	 */
	public Collection<CarType> getCarTypesOfCarRentalCompany(String crcName) {
		// use persistence instead
		EntityManager em = EMF.get().createEntityManager();
		try{
			Query query = em.createQuery("SELECT crc.cartypes FROM CarRentalCompany crc WHERE crc.name = :name");
			query.setParameter("name", crcName);
			Collection<CarType> res = (Collection<CarType>) query.getResultList().get(0);
			return res;
		} finally {
			em.close();
		}

		/*CarRentalCompany crc = CRCS.get(crcName);
		Collection<CarType> out = new ArrayList<CarType>(crc.getAllCarTypes());
		return out;*/
	}
	
	/**
	 * Get the list of cars of the given car type in the given car rental company.
	 *
	 * @param	crcName
	 * 			name of the car rental company
	 * @param 	carType
	 * 			the given car type
	 * @return	A list of car IDs of cars with the given car type.
	 */
	public Collection<Long> getCarIdsByCarType(String crcName, CarType carType) {
		Collection<Long> out = new ArrayList<Long>();
		for (Car c : getCarsByCarType(crcName, carType)) {
			out.add( c.getId().getId());
		}
		return out;
	}

	/**
	 * Get the amount of cars of the given car type in the given car rental company.
	 *
	 * @param	crcName
	 * 			name of the car rental company
	 * @param 	carType
	 * 			the given car type
	 * @return	A number, representing the amount of cars of the given car type.
	 */
	public int getAmountOfCarsByCarType(String crcName, CarType carType) {
		return this.getCarsByCarType(crcName, carType).size();
	}

	/**
	 * Get the list of cars of the given car type in the given car rental company.
	 *
	 * @param	crcName
	 * 			name of the car rental company
	 * @param 	carType
	 * 			the given car type
	 * @return	List of cars of the given car type
	 */
	private List<Car> getCarsByCarType(String crcName, CarType carType) {				
		// use persistence instead
		EntityManager em = EMF.get().createEntityManager();
		try{
			Collection<CarType> types = getCarTypesOfCarRentalCompany(crcName);
			if (!types.contains(carType)) {
				throw new IllegalArgumentException("Car type with key: "+carType.getID()+
						" is not a car type of the company: "+crcName);
			}
			Query query = em.createQuery("SELECT t.cars FROM CarType t WHERE t.id = :id");
			query.setParameter("id", carType.getID());
			Set<Car> res = (Set<Car>) query.getResultList().get(0);
			List<Car> result = new ArrayList<Car>(res);
			return result;
		} finally {
			em.close();
		}

		/*List<Car> out = new ArrayList<Car>(); 
		for(CarRentalCompany crc : CRCS.values()) {
			for (Car c : crc.getCars()) {
				if (c.getType().equalsIgnoreCase(carType.getName())) { 
					out.add(c);
				}
			}
		}
		return out;*/
	}

	/**
	 * Check whether the given car renter has reservations.
	 *
	 * @param 	renter
	 * 			the car renter
	 * @return	True if the number of reservations of the given car renter is higher than 0.
	 * 			False otherwise.
	 */
	public boolean hasReservations(String renter) {
		return this.getReservations(renter).size() > 0;		
	}	

	public Collection<Log> getLogForUser(String carRenter) {
		EntityManager em = EMF.get().createEntityManager();
		Collection<Log> log = new ArrayList<Log>();
		try{
			Query query = em.createQuery("SELECT l FROM Log l WHERE l.carRenter = :renter ORDER BY l.date DESC");
			query.setParameter("renter", carRenter);
			List<Log> res = query.getResultList();
			return res;
		} finally {
			em.close();
		}
	}
}

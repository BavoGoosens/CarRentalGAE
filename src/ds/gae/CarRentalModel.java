package ds.gae;

import java.util.*;

import com.google.appengine.api.datastore.Key;
import ds.gae.entities.Car;
import ds.gae.entities.CarRentalCompany;
import ds.gae.entities.CarType;
import ds.gae.entities.Quote;
import ds.gae.entities.Reservation;
import ds.gae.entities.ReservationConstraints;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class CarRentalModel {
	
	// public Map<String,CarRentalCompany> CRCS = new HashMap<String, CarRentalCompany>();

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
		Set<String>  names = new HashSet<String>();
		try{
			Query query = em.createQuery(
				          "SELECT crc.carTypes "
						+ "FROM CarRentalCompany crc "
						+ "WHERE crc.name = :name");
			query.setParameter("name", crcName);
			List<CarType> types = query.getResultList();
			for (CarType type: types) {
				names.add(type.getName());
			}
			return names;
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
		List<String> comps = new ArrayList<String>();
		try{
			Query query = em.createQuery("SELECT c.name FROM CarRentalCompany c");
			comps = query.getResultList();
			return comps;
		}
		finally {
			em.close();
		}
    }

	private CarRentalCompany getCompany(String companyName) {
		EntityManager em = EMF.get().createEntityManager();
		try {
			return em.find(CarRentalCompany.class, companyName);
		} catch (Exception e){
			return  null;
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
    	CarRentalCompany crc = this.getCompany(company);

		Quote out = null;

        if (crc != null) {
            out = crc.createQuote(constraints, renterName);
        } else {
        	throw new ReservationException("CarRentalCompany not found.");    	
        }
        
        return out;
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
	public void confirmQuote(Quote q) throws ReservationException {
		CarRentalCompany crc = this.getCompany(q.getRentalCompany());
        crc.confirmQuote(q);
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
		List<Reservation> reservations = new ArrayList<Reservation>();
		try {
			for (Quote q : quotes){
				CarRentalCompany crc = this.getCompany(q.getRentalCompany());
				reservations.add(crc.confirmQuote(q));
			}
		} catch (ReservationException e){
			e.printStackTrace();
		}
		return reservations;
    }
	
	/**
	 * Get all reservations made by the given car renter.
	 *
	 * @param 	renter
	 * 			name of the car renter
	 * @return	the list of reservations of the given car renter
	 */
	public List<Reservation> getReservations(String renter) {
		EntityManager em = EMF.get().createEntityManager();
		try{
			Query query = em.createQuery(" SELECT r " +
				"FROM Reservation r " +
				"WHERE r.carRenter = :renter");
			query.setParameter("renter", renter);
			List<Reservation> result = query.getResultList();
			return result;
		} finally {
			em.close();
		}
	}

    /**
     * Get the car types available in the given car rental company.
     *
     * @param 	crcName
     * 			the given car rental company
     * @return	The list of car types in the given car rental company.
     */
    public Collection<CarType> getCarTypesOfCarRentalCompany(String crcName) {
		EntityManager em = EMF.get().createEntityManager();
		try{
			Query query = em.createQuery("SELECT crc.carTypes " +
				"FROM CarRentalCompany crc " +
				"WHERE crc.name = :name");
			query.setParameter("name", crcName);
			List<CarType> carTypes = query.getResultList();
			return carTypes;
		} finally {
			em.close();
		}
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
    public Collection<Key> getCarIdsByCarType(String crcName, CarType carType) {
    	Collection<Key> out = new ArrayList<Key>();
    	for (Car c : getCarsByCarType(crcName, carType)) {
    		out.add(c.getId());
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
		EntityManager em = EMF.get().createEntityManager();
		try {
			Query query = em.createQuery(
					"			SELECT 	crc.cars " +
							"	FROM	CarRentalCompany crc " +
							"	WHERE 	crc.name = :name");
			query.setParameter("name", crcName);
			List<Car> cars = query.getResultList();
			Query query2 = em.createQuery(
					"			SELECT 	c " +
							"	FROM 	Car c " +
							"	WHERE  	c.type = :ty AND c IN :cars");

			query.setParameter("ty", carType);
			query.setParameter("cars", cars);
			List<Car> out = query2.getResultList();
			return out;
		} finally {
			em.close();
		}
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
}
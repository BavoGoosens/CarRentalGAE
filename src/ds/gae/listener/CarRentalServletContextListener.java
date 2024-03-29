package ds.gae.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.w3c.dom.Entity;

import com.google.appengine.api.datastore.Key;

import ds.gae.CarRentalModel;
import ds.gae.EMF;
import ds.gae.entities.Car;
import ds.gae.entities.CarRentalCompany;
import ds.gae.entities.CarType;

public class CarRentalServletContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// This will be invoked as part of a warming request, 
		// or the first user request if no warming request was invoked.
						
		// check if dummy data is available, and add if necessary
		if(!isDummyDataAvailable()) {
			addDummyData();
		}
	}
	
	private boolean isDummyDataAvailable() {
		// If the Hertz car rental company is in the datastore, we assume the dummy data is available
		EntityManager em = EMF.get().createEntityManager();
		try{
			CarRentalCompany crc = em.find(CarRentalCompany.class, "Hertz");
			boolean b = crc != null;
			return b;
			//return CarRentalModel.get().CRCS.containsKey();
		} finally {
			em.close();
		}

	}
	
	private void addDummyData() {
		loadRental("Hertz","hertz.csv");
        loadRental("Dockx","dockx.csv");
	}
	
	private void loadRental(String name, String datafile) {
		EntityManager em = EMF.get().createEntityManager();
		Logger.getLogger(CarRentalServletContextListener.class.getName()).log(Level.INFO, "loading {0} from file {1}", new Object[]{name, datafile});
        try {
        	EntityTransaction trans = em.getTransaction();
        	trans.begin();
        	CarRentalCompany company = loadData(name, datafile);
            
    		// FIXME: use persistence instead
            //CarRentalModel.get().CRCS.put(name, company);
            em.persist(company);
            trans.commit();

        } catch (NumberFormatException ex) {
            Logger.getLogger(CarRentalServletContextListener.class.getName()).log(Level.SEVERE, "bad file", ex);
        } catch (IOException ex) {
            Logger.getLogger(CarRentalServletContextListener.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        	em.close();
        	Logger.getLogger(CarRentalServletContextListener.class.getName()).log(Level.INFO, "Entity manager closed succesfully");
        }
	}
	
	public static CarRentalCompany loadData(String name, String datafile) throws NumberFormatException, IOException {
		// FIXME: adapt the implementation of this method to your entity structure
		
		Set<Car> cars = new HashSet<Car>();
		Set<CarType> carTypes = new HashSet<CarType>();

		//open file from jar
		BufferedReader in = new BufferedReader(new InputStreamReader(CarRentalServletContextListener.class.getClassLoader().getResourceAsStream(datafile)));
		//while next line exists
		while (in.ready()) {
			//read line
			String line = in.readLine();
			//if comment: skip
			if (line.startsWith("#")) {
				continue;
			}
			//tokenize on ,
			StringTokenizer csvReader = new StringTokenizer(line, ",");
			//create new car type from first 5 fields
			CarType type = new CarType(csvReader.nextToken(),
					Integer.parseInt(csvReader.nextToken()),
					Float.parseFloat(csvReader.nextToken()),
					Double.parseDouble(csvReader.nextToken()),
					Boolean.parseBoolean(csvReader.nextToken())
					);
			
			//create N new cars with given type, where N is the 5th field
			for (int i = Integer.parseInt(csvReader.nextToken()); i > 0; i--) {
				type.addCar(new Car(type.getName()));
			}
			carTypes.add(type);
		}

		return new CarRentalCompany(name, carTypes);
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// App Engine does not currently invoke this method.
	}
}
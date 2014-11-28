package ds.gae.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.google.appengine.api.datastore.Key;

@Entity
public class CarType {
    
	@Id
    @GeneratedValue(strategy=
            GenerationType.IDENTITY)
    private Key id;
    private String name;
    private int nbOfSeats;
    private boolean smokingAllowed;
    private double rentalPricePerDay;
    //trunk space in liters
    private float trunkSpace;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Car> cars = new ArrayList<Car>();
    
    /***************
	 * CONSTRUCTOR *
	 ***************/
    
    public CarType(String name, int nbOfSeats, float trunkSpace, double rentalPricePerDay, boolean smokingAllowed) {
        this.name = name;
        this.nbOfSeats = nbOfSeats;
        this.trunkSpace = trunkSpace;
        this.rentalPricePerDay = rentalPricePerDay;
        this.smokingAllowed = smokingAllowed;
    }
    
    public Set<Car> getCars(){
    	return new HashSet<Car>(this.cars);
    }
    
    public void addCar(Car c){
    	this.cars.add(c);
    }

    public String getName() {
    	return name;
    }
    
    public int getNbOfSeats() {
        return nbOfSeats;
    }
    
    public boolean isSmokingAllowed() {
        return smokingAllowed;
    }

    public double getRentalPricePerDay() {
        return rentalPricePerDay;
    }
    
    public float getTrunkSpace() {
    	return trunkSpace;
    }
    
    public Key getID(){
    	return this.id;
    }
    
    /*************
     * TO STRING *
     *************/
    
    @Override
    public String toString() {
    	return String.format("Car type: %s \t[seats: %d, price: %.2f, smoking: %b, trunk: %.0fl]" , 
                getName(), getNbOfSeats(), getRentalPricePerDay(), isSmokingAllowed(), getTrunkSpace());
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CarType other = (CarType) obj;
		if (id == null) {
			if (other.getID() != null)
				return false;
		} else if (!id.equals(other.getID()))
			return false;
		return true;
	}
}
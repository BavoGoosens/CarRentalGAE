package ds.gae.entities;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.google.appengine.api.datastore.Key;

@Entity
public class Car {

	@Id
    @GeneratedValue(strategy=
            GenerationType.IDENTITY)
    private Key id;
	
    private String type;
	
	@OneToMany(cascade = CascadeType.ALL)
    private Set<Reservation> reservations = new HashSet<Reservation>();

    /***************
     * CONSTRUCTOR *
     ***************/
    
    public Car(String type) {
        this.type = type;
    }

    /******
     * ID *
     ******/
    
    public Key getId() {
    	return id;
    }
    
    /************
     * CAR TYPE *
     ************/
    
    public String getType() {
        return type;
    }
    
    @Override
    public String toString() {
    	return "Car of the type: " + this.type;
    }

    /****************
     * RESERVATIONS *
     ****************/
    
    public Set<Reservation> getReservations() {
    	return reservations;
    }

    public boolean isAvailable(Date start, Date end) {
        if(!start.before(end))
            throw new IllegalArgumentException("Illegal given period");

        for(Reservation reservation : this.getReservations()) {
            if(reservation.getEndDate().before(start) || reservation.getStartDate().after(end))
                continue;
            return false;
        }
        return true;
    }
    
    public void addReservation(Reservation res) {
        this.getReservations().add(res);
    }
    
    public void removeReservation(Reservation reservation) {
        // equals-method for Reservation is required!
        this.getReservations().remove(reservation);
    }
}
package ds.gae.entities;

import com.google.appengine.api.datastore.Key;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Car implements Serializable {

    @Id
    @GeneratedValue(strategy=
            GenerationType.IDENTITY)
    private Key id;
    @ManyToOne
    private CarType type;
    @OneToMany(cascade = CascadeType.ALL)
    private Set<Reservation> reservations;

    /***************
     * CONSTRUCTOR *
     ***************/

    public Car(){};

    public Car(int uid, CarType type) {
        this.type = type;
        this.reservations = new HashSet<Reservation>();
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
    
    public CarType getType() {
        return type;
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

        for(Reservation reservation : reservations) {
            if(reservation.getEndDate().before(start) || reservation.getStartDate().after(end))
                continue;
            return false;
        }
        return true;
    }
    
    public void addReservation(Reservation res) {
        reservations.add(res);
    }
    
    public void removeReservation(Reservation reservation) {
        // equals-method for Reservation is required!
        reservations.remove(reservation);
    }
}
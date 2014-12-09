package ds.gae.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;

@Entity
public class Log {
	
	@Id
    @GeneratedValue(strategy=
            GenerationType.IDENTITY)
    private Key id;
	private String carRenter;
	private Date date;
	private String description;
	
	public Log(String carRenter, Date date, String description) {
		this.carRenter = carRenter;
		this.date = date;
		this.description = description;
	}
	
	public Date getDate() {
		return this.date;
	}
	
	public String getDescription() {
		return this.description;
	}
	

}

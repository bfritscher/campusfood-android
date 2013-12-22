package ch.fritscher.campusfood.android.model;

import java.io.Serializable;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonManagedReference;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Campus implements Serializable{

	private static final long serialVersionUID = 3195616026288557810L;
	private Long id;
	private String name;
	private List<Location> locations;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@JsonManagedReference("campus")
	public List<Location> getLocations() {
		return locations;
	}
	@JsonManagedReference("campus")
	public void setLocations(List<Location> locations) {
		this.locations = locations;
	}
	
}

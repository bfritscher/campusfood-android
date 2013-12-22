package ch.fritscher.campusfood.android.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonBackReference;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonManagedReference;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Menu implements Item{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2771409281457693523L;
	private Long id;
	private String name;
	private List<Meal> meals = new ArrayList<Meal>();
	private Location location;
	
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
	@JsonManagedReference("menu")
	public List<Meal> getMeals() {
		return meals;
	}
	@JsonManagedReference("menu")
	public void setMeals(List<Meal> meals) {
		this.meals = meals;
	}
	
	@JsonBackReference("location")
	public Location getLocation() {
		return location;
	}
	@JsonBackReference("location")
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public String toString(){
		return name;
	}
	@Override
	public boolean hasList() {
		return true;
	}
	@JsonIgnore
	@Override
	public List<? extends Item> getList() {
		return meals;
	}
	@JsonIgnore
	@Override
	public String getTitle(){
		if(location != null){
			return location.getName() + " - " + name;
		}else{
			return name;
		}
		
	}

}

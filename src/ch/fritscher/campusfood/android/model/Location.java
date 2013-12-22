package ch.fritscher.campusfood.android.model;

import java.util.List;

import org.codehaus.jackson.annotate.JsonBackReference;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonManagedReference;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Location implements Item{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7845784732204629691L;
	private Long id;
	private String name;
	private Campus campus;
	private List<Menu> menus;
	
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
	
	@JsonBackReference("campus")
	public Campus getCampus() {
		return campus;
	}
	@JsonBackReference("campus")
	public void setCampus(Campus campus) {
		this.campus = campus;
	}
	
	@JsonManagedReference("location")
	public List<Menu> getMenus(){
		return menus;
	}
	
	@JsonManagedReference("location")
	public void setMenus(List<Menu> menus){
		this.menus = menus;
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
		return menus;
	}
	@JsonIgnore
	@Override
	public String getTitle(){
		return name;
	}

}

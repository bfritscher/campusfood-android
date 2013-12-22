package ch.fritscher.campusfood.android.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import android.text.TextUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Datastore implements Serializable{

	public static final int VERSION = 1;
	private static final long serialVersionUID = 1L;
	private Integer version = Datastore.VERSION;
	private List<Campus> campus = new ArrayList<Campus>();
	private int[] date = new int[3];
	private List<Long> menusOrder = new ArrayList<Long>();
	
	//not persisted
	private List<Menu> menus = new ArrayList<Menu>();
	private boolean campusDirty = true;
	
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public List<Campus> getCampus() {
		return campus;
	}
	public void setCampus(List<Campus> campus) {
		this.campus = campus;
		campusDirty = true;
	}
	public int[] getDate() {
		return date;
	}
	public void setDate(int[] date) {
		this.date = date;
	}
	public List<Long> getMenusOrder() {
		return menusOrder;
	}
	public void setMenusOrder(List<Long> menusOrder) {
		this.menusOrder = menusOrder;
	}

	
	@JsonIgnore
	public List<Menu> getMenus(){
		if(campusDirty){
			int nbMenus = menusOrder.size();
			Menu[] m = new Menu[nbMenus]; 
			for(Campus c: campus){
				for(Location location: c.getLocations()){
					for(Menu menu: location.getMenus()){
						int idx = menusOrder.indexOf(menu.getId());
						if(idx >= 0 && idx < nbMenus){
							m[idx] = menu;
						}
					}
				}
			}
			menus.clear();
			Collections.addAll(menus, m);
			//fix missing menus
			menus.removeAll(Collections.singleton(null));
			campusDirty = false;
			notifyMenuOrderChanged();
		}
		return menus;
	}
	
	@JsonIgnore
	public void notifyMenuOrderChanged(){
		menusOrder.clear();
		for(Menu menu : getMenus()){
			menusOrder.add(menu.getId());
		}
	}
	
	
	@JsonIgnore
	public Menu getMenuById(Long id){
		for(Menu menu : menus){
			if(menu.getId() == id){
				return menu;
			}	
		}
		return null;
	}
	
	@JsonIgnore
	public Campus getCampusById(Long id){
		for(Campus c : campus){
			if(c.getId() == id){
				return c;
			}	
		}
		return null;
	}
	
	@JsonIgnore
	public Location getLocationById(Long id){
		for(Campus c : campus){
			for(Location location : c.getLocations()){
				if(location.getId() == id){
					return location;
				}	
			}
		}
		return null;
	}
	
	@JsonIgnore
	public String getMenuOrderString(){
		return TextUtils.join(",", menusOrder);
	}
	
	@JsonIgnore
	public void removeMenu(Menu m){
		//m can be in the tree or no
		Menu menu = getMenuById(m.getId());
		if(menu != null){
			menu.getLocation().getMenus().remove(menu);
			menus.remove(menu);
		}
	}
	
	@JsonIgnore
	public void addMenu(Menu m){
		//m can be is not in the tree
		Menu menu = getMenuById(m.getId());
		if(menu == null){
			Location l = m.getLocation();
			Location location = getLocationById(l.getId());
			if(location == null){
				location = l;
				location.getMenus().clear();
				Campus c = location.getCampus();
				Campus rc  = getCampusById(c.getId());
				if(rc == null){
					rc = c; 
					rc.getLocations().clear();
					campus.add(rc);
				}
				location.setCampus(rc);
				rc.getLocations().add(location);
			}
			m.setLocation(location);
			location.getMenus().add(m);
			menus.add(m);
		}
	}
	
}

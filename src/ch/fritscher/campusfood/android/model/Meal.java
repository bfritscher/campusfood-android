package ch.fritscher.campusfood.android.model;

import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonBackReference;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Meal implements Item{

	/**
	 * 
	 */
	private static final long serialVersionUID = 928382089663108321L;
	private Long id;
	private Long mid;
	private Menu menu;
	private String content;
	private Date date;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonBackReference("menu")
	public Menu getMenu() {
		return menu;
	}

	@JsonBackReference("menu")
	public void setMenu(Menu menu) {
		this.menu = menu;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public Long getMid() {
		return mid;
	}
	
	public void setMid(long mid){
		this.mid = mid;
	}
	
	@JsonIgnore
	@Override
	public String getTitle(){
		StringBuilder sb = new StringBuilder();
		if(menu != null){
			if(menu.getLocation() != null){
				sb.append(menu.getLocation().getName());
				sb.append(" - ");
			}
			sb.append(menu.getName());
		}
		return sb.toString();
	}

	
	public String toString(){
		return content;
	}

	@Override
	public boolean hasList() {
		return false;
	}

	@JsonIgnore
	@Override
	public List<? extends Item> getList() {
		return null;
	}
	
}

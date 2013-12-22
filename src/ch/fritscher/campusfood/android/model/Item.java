package ch.fritscher.campusfood.android.model;

import java.io.Serializable;
import java.util.List;

public interface Item extends Serializable{

	public Long getId();
	
	public boolean hasList();

	public List<? extends Item> getList();
	
	public String getTitle();

}

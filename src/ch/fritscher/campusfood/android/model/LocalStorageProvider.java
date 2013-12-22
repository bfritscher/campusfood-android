package ch.fritscher.campusfood.android.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import ch.fritscher.campusfood.android.CampusFoodApplication;
import ch.fritscher.campusfood.android.util.RestClient;
import ch.fritscher.campusfood.android.util.RestClient.RequestMethod;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;

public class LocalStorageProvider{
	public static final String EVENT_LOCATIONS_LOADED = "ch.fritscher.campusfood.android.LOCATIONS_LOADED";
	public static final String HAS_LOADED = "hasLoaded";
	public static final String EVENT_MEALS_UPDATED = "ch.fritscher.campusfood.android.MEALS_UPDATED";
	private final String FILENAME = "datastore";
	private Datastore datastore = new Datastore();
	private ObjectMapper mapper = new ObjectMapper();
	private List<Campus> campus;
	public boolean isFirstTime = false;
	
	public LocalStorageProvider(){
		loadLocal();
	}
	
	public void loadLocal(){
		FileInputStream fis;
		try {
			fis = CampusFoodApplication.getContext().openFileInput(FILENAME);

			StringBuffer fileContent = new StringBuffer("");
			byte[] buffer = new byte[1024];
			while ((fis.read(buffer)) != -1) {
			    fileContent.append(new String(buffer));
			}
			fis.close();
			datastore =  mapper.readValue(fileContent.toString(), new TypeReference<Datastore>() {});

		} catch (Exception e) {
			isFirstTime = true;
		}
	}

	
	private void notifyDatastoreChanged(Boolean hasLoaded) {
	    Intent intent = new Intent(LocalStorageProvider.EVENT_MEALS_UPDATED);
	    intent.putExtra(LocalStorageProvider.HAS_LOADED, hasLoaded);
	    CampusFoodApplication.getContext().sendBroadcast(intent);
	}
	
	private void notifyLocationsLoaded(Boolean hasLoaded) {
	    Intent intent = new Intent(LocalStorageProvider.EVENT_LOCATIONS_LOADED);
	    intent.putExtra(LocalStorageProvider.HAS_LOADED, hasLoaded);
	    LocalBroadcastManager.getInstance(CampusFoodApplication.getContext()).sendBroadcast(intent);
	}

	public void saveLocal(){
		FileOutputStream fos;
		try {
		    long startTime = System.currentTimeMillis();
			fos = CampusFoodApplication.getContext().openFileOutput(FILENAME, Context.MODE_PRIVATE);
			datastore.notifyMenuOrderChanged();
			mapper.writeValue(fos, datastore);
			fos.close();
		    long elapseTime = System.currentTimeMillis() - startTime;
		    EasyTracker.getTracker().trackTiming("resources", elapseTime, "save", null);
		    notifyDatastoreChanged(true);
		} catch (Exception e) {
			e.printStackTrace();
			Tracker myTracker = EasyTracker.getTracker(); // Get a reference to tracker.
			myTracker.trackException(e.getMessage(), false); // false indicates non-fatal exception.
		}
	}
	
	public void loadRemoteMenus(){
		new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
			RestClient client = new RestClient(CampusFoodApplication.SERVER_URL + "api/menus");
			try {
				long startTime = System.currentTimeMillis();
			    client.Execute(RequestMethod.GET);
			    long elapseTime = System.currentTimeMillis() - startTime;
			    EasyTracker.getTracker().trackTiming("resources", elapseTime, "net", "menus");
			    
			    startTime = System.currentTimeMillis();
			    updateCampusFromJson(client.getResponse());
			    elapseTime = System.currentTimeMillis() - startTime;
			    EasyTracker.getTracker().trackTiming("resources", elapseTime, "parse", "menus");
			    notifyLocationsLoaded(true);			    
			    
			} catch (Exception e) {
				Tracker myTracker = EasyTracker.getTracker(); // Get a reference to tracker.
				myTracker.trackException(e.getMessage(), false); // false indicates non-fatal exception.
				notifyLocationsLoaded(false);
			}
			return null;
			}
		}.execute();
	}
	
	public void loadRemoteMeals(){
		new AsyncTask<String, Void, Void>(){
			@Override
			protected Void doInBackground(String... params) {
				int[] date = getDate();
				String url = new StringBuilder(CampusFoodApplication.SERVER_URL)
				.append("api/meals/")
	        		.append(date[0]).append("-")
	        		//Month is 0 based so add 1	        		
	        		.append(String.format("%02d", date[1] + 1)).append("-")
	        		.append(String.format("%02d", date[2]))
	        		.append("?m=").append(datastore.getMenuOrderString()).toString();
	        		
				RestClient client = new RestClient(url);
				try {
					long startTime = System.currentTimeMillis();
				    client.Execute(RequestMethod.GET);
				    long elapseTime = System.currentTimeMillis() - startTime;
				    EasyTracker.getTracker().trackTiming("resources", elapseTime, "net", "meals");
				    
				    startTime = System.currentTimeMillis();
				    updateMeals(client.getResponse());
				    elapseTime = System.currentTimeMillis() - startTime;
				    EasyTracker.getTracker().trackTiming("resources", elapseTime, "parse", "meals");
				    
				    saveLocal();
				} catch (Exception e) {
					Tracker myTracker = EasyTracker.getTracker(); // Get a reference to tracker.
					myTracker.trackException(e.getMessage(), false); // false indicates non-fatal exception.
					notifyDatastoreChanged(false);
				}
				return null;
			}
		}.execute();
		
	}
	
	private void updateCampusFromJson(String json){
		try{
			campus = mapper.readValue(json, new TypeReference<List<Campus>>() {});
	    		    	
	    } catch(Exception e) {
	    	Tracker myTracker = EasyTracker.getTracker(); // Get a reference to tracker.
			myTracker.trackException(e.getMessage(), false); // false indicates non-fatal exception.
			e.printStackTrace();
		}
	}
	
	private void updateMeals(String json){
		try{
			List<Meal> meals =  mapper.readValue(json, new TypeReference<List<Meal>>() {});			
			for(int i=0; i< datastore.getMenus().size(); i++){
				try{
					Menu menu = datastore.getMenus().get(i);
					Meal meal = meals.get(i);
					if(menu.getId() == meal.getMid()){
						meal.setMenu(menu);
						menu.getMeals().clear();
						menu.getMeals().add(meal);
						//what about the date attribute?
					}
					//else ignore
			   }catch(IndexOutOfBoundsException e){
				   //ignore
			   }
			}
	    } catch(Exception e) {
	    	Tracker myTracker = EasyTracker.getTracker(); // Get a reference to tracker.
			myTracker.trackException(e.getMessage(), false); // false indicates non-fatal exception.
			e.printStackTrace();
		}
	}
	
	public List<Menu> getMenuSelection(){
		return datastore.getMenus();
	}

	public List<Meal> getVisibleMeals() {
		List<Meal> meals = new ArrayList<Meal>();
		for(Menu menu: datastore.getMenus()){
			try{
				meals.add(menu.getMeals().get(0));
			}catch(Exception e){
				e.printStackTrace();	
			}
		}
		return meals;
	}
	
	public List<Campus> getCampus(){
		return campus;
	}
	
	public List<Long> getMenusOrder(){
		return datastore.getMenusOrder();
	}

	public void updateMenuSelection(Map<Menu, Boolean> selectedMenus) {
		for(Map.Entry<Menu, Boolean> entry : selectedMenus.entrySet()){
			if(entry.getValue()){
				datastore.addMenu(entry.getKey());
			}else{
				datastore.removeMenu(entry.getKey());
			}
		}
		datastore.notifyMenuOrderChanged();
		loadRemoteMeals();
	}

	public int[] getDate() {
		return datastore.getDate();
	}
	
	
	public void setDate(int[] today) {
		int[] date = getDate();
		date[0] = today[0];
		date[1] = today[1];
		date[2] = today[2];
	}
	
	public int[] getTodayDate(){
        	final Calendar c = Calendar.getInstance();
        	int year = c.get(Calendar.YEAR);
        	int month = c.get(Calendar.MONTH);
        	int day = c.get(Calendar.DAY_OF_MONTH);
        	return new int[]{year, month, day};
	}

}

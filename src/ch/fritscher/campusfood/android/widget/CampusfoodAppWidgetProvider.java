package ch.fritscher.campusfood.android.widget;

import java.util.List;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;
import ch.fritscher.campusfood.android.R;
import ch.fritscher.campusfood.android.model.LocalStorageProvider;
import ch.fritscher.campusfood.android.model.Meal;
import ch.fritscher.campusfood.android.ui.DailyMealsActivity;

public class CampusfoodAppWidgetProvider extends AppWidgetProvider {

	 public static String CLICK_ACTION = "ch.fritscher.campusfood.android.widget.CLICK_ACTION";
	 public static String REFRESH_ACTION = "ch.fritscher.campusfood.android.widget.REFRESH_ACTION";
	 public static String EXTRA_DIRECTION = "ch.fritscher.campusfood.android.widget.EXTRA_DIRECTION";
	 public static String EXTRA_CURRENT_POSITION = "ch.fritscher.campusfood.android.widget.EXTRA_CURRENT_POSITION";
	

	@Override
	public void onReceive(Context context, Intent intent) {
		
		final String action = intent.getAction();
        if (action.equals(LocalStorageProvider.EVENT_MEALS_UPDATED) || action.equals(REFRESH_ACTION) || action.equals(CLICK_ACTION)) {       	
	        	 intent.setClass(context, UpdateService.class);
	             context.startService(intent);
	        }
		super.onReceive(context, intent);
	}
	
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
        // To prevent any ANR timeouts, we perform the update in a service
        context.startService(new Intent(context, UpdateService.class));
	}
	
	 public static class UpdateService extends Service {
		 
		 	Context context = this;
		 	List<Meal> meals;
		 	
	        @Override
	        public void onStart(Intent intent, int startId) {
	        	
	        	AppWidgetManager manager = AppWidgetManager.getInstance(this);
	        	
	        	LocalStorageProvider storageProvider = new LocalStorageProvider();
	        	storageProvider.loadLocal();
	        	meals = storageProvider.getVisibleMeals();       	
	        	
	        	//DEFAULTS
	        	int current_position = 0;
	    		int direction = 0;
	    		String title = "";
                String content = getResources().getString(R.string.no_meal_content);
	        	
	        	//eval action
	        	String action = null;
	    		
	    		if(intent != null){ 
	    			action = intent.getAction();
	    		}
	    		
	        	if (action != null && action.equals(REFRESH_ACTION)) {
	        		storageProvider.setDate(storageProvider.getTodayDate());
	        		storageProvider.loadRemoteMeals();
	        		title = "";
	                content = getResources().getString(R.string.meal_loading);
	        	}else{
		        	if(meals.size() > 0){
		        		if(action != null &&  action.equals(CLICK_ACTION)){	
		        			direction = intent.getIntExtra(CampusfoodAppWidgetProvider.EXTRA_DIRECTION, 0);
			                current_position = intent.getIntExtra(CampusfoodAppWidgetProvider.EXTRA_CURRENT_POSITION, 0);
			                current_position = (meals.size() + current_position + direction) % meals.size();
		        		}
		        		Meal meal = meals.get(current_position);
		            	if(meal != null){
		            		title = meal.getTitle();
		            		content = meal.getContent();
		            	}	
		        	}
	        	}
	        	

	        	if(action != null &&  action.equals(CLICK_ACTION)){
	                int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
	                manager.updateAppWidget(appWidgetId, updateView(appWidgetId, current_position, title, content));
	                
	        	}else{
		        	//load all widgets
	        		int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(this, CampusfoodAppWidgetProvider.class));
	        		final int N = appWidgetIds.length;
			        // Perform this loop procedure for each App Widget that belongs to this provider
			        for (int i=0; i<N; i++) {
			        	int appWidgetId = appWidgetIds[i];
			        	manager.updateAppWidget(appWidgetId, updateView(appWidgetId, current_position, title, content));
			        }	
	        	}
	        }
	        
	        private RemoteViews updateView(int appWidgetId, int current_position, String title, String content){
            	
	            // Build an update that holds the updated widget contents
	        	RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.meals_widget);
	        	updateViews.setTextViewText(R.id.daily_meal_title, title);
                updateViews.setTextViewText(R.id.daily_meal_content, content);
               

                Intent onClickIntent;
                PendingIntent onClickPendingIntent;
                onClickIntent = new Intent(context, CampusfoodAppWidgetProvider.class);
                onClickIntent.setAction(CampusfoodAppWidgetProvider.CLICK_ACTION);
                onClickIntent.putExtra(CampusfoodAppWidgetProvider.EXTRA_DIRECTION, -1);
                onClickIntent.putExtra(CampusfoodAppWidgetProvider.EXTRA_CURRENT_POSITION, current_position);
                onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                onClickPendingIntent = PendingIntent.getBroadcast(context, appWidgetId - 1000,
                        onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                updateViews.setOnClickPendingIntent(R.id.widget_click_left, onClickPendingIntent);


                onClickIntent = new Intent(context, CampusfoodAppWidgetProvider.class);
                onClickIntent.setAction(CampusfoodAppWidgetProvider.REFRESH_ACTION);
                onClickIntent.putExtra(CampusfoodAppWidgetProvider.EXTRA_CURRENT_POSITION, current_position);
                onClickPendingIntent = PendingIntent.getBroadcast(context, appWidgetId,
                        onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                updateViews.setOnClickPendingIntent(R.id.widget_click_refresh, onClickPendingIntent);
                
                onClickIntent = new Intent(context, CampusfoodAppWidgetProvider.class);
                onClickIntent.setAction(CampusfoodAppWidgetProvider.CLICK_ACTION);
                onClickIntent.putExtra(CampusfoodAppWidgetProvider.EXTRA_DIRECTION, 1);
                onClickIntent.putExtra(CampusfoodAppWidgetProvider.EXTRA_CURRENT_POSITION, current_position);
                onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                onClickPendingIntent = PendingIntent.getBroadcast(context, appWidgetId + 1000,
                        onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                updateViews.setOnClickPendingIntent(R.id.widget_click_right, onClickPendingIntent);

                onClickIntent = new Intent(context, DailyMealsActivity.class);
                onClickIntent.putExtra(CampusfoodAppWidgetProvider.EXTRA_CURRENT_POSITION, current_position);
                onClickPendingIntent = PendingIntent.getActivity(context, appWidgetId,
                        onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                updateViews.setOnClickPendingIntent(R.id.widget, onClickPendingIntent);
                return updateViews;
	        }
		 
		 
		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}

	 }
	
}

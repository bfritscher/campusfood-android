package ch.fritscher.campusfood.android;

import com.google.analytics.tracking.android.EasyTracker;

import ch.fritscher.campusfood.android.model.LocalStorageProvider;
import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

public class CampusFoodApplication extends Application {
	
	private static Context context;
	public static Typeface FONT_ARCHITECTS_DAUGHTER;
	public static String SERVER_URL = "https://isisvn.unil.ch/campusfood/";
	private LocalStorageProvider localStorage;
	
	@Override
	public void onCreate() {
		super.onCreate();
		CampusFoodApplication.context = getApplicationContext();
		CookieSyncManager.createInstance(CampusFoodApplication.context);
		CookieManager.getInstance().setAcceptCookie(true);
		FONT_ARCHITECTS_DAUGHTER = Typeface.createFromAsset(getAssets(),"fonts/ArchitectsDaughter.ttf");
		localStorage = new LocalStorageProvider();
		EasyTracker.getInstance().setContext(CampusFoodApplication.context);
	}
	
	public LocalStorageProvider getLocalStorage(){
		return localStorage;
	}
	
	public static Context getContext(){
		return CampusFoodApplication.context;
	}
}

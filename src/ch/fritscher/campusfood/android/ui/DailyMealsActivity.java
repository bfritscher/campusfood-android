package ch.fritscher.campusfood.android.ui;

import java.util.Arrays;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction3D;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.TextView;
import ch.fritscher.campusfood.android.CampusFoodApplication;
import ch.fritscher.campusfood.android.R;
import ch.fritscher.campusfood.android.model.DailyMealPagerAdapter;
import ch.fritscher.campusfood.android.model.LocalStorageProvider;
import ch.fritscher.campusfood.android.widget.CampusfoodAppWidgetProvider;

import com.actionbarsherlock.app.ActionBar.LayoutParams;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

public class DailyMealsActivity extends SherlockFragmentActivity implements DatePickerDialog.OnDateSetListener {
    private TextView dailyMealDate;

	private ViewPager mPager;
    private PageIndicator mIndicator;
	private DailyMealPagerAdapter mAdapter;
	private LocalStorageProvider data;

    static final int DATE_DIALOG_ID = 0;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
    	  @Override
    	  public void onReceive(Context context, Intent intent) {
    		  setProgressBarIndeterminateVisibility(false);
    		  updateDateDisplay();
	  		if(intent.getExtras().getBoolean(LocalStorageProvider.HAS_LOADED)){
	  			updateList();
	  		}else{
	  			 // DialogFragment.show() will take care of adding the fragment
	  		    // in a transaction.  We also want to remove any currently showing
	  		    // dialog, so make our own transaction and take care of that here.
	  		    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	  		    Fragment prev = getSupportFragmentManager().findFragmentByTag("errorLoadingDialog");
	  		    if (prev != null) {
	  		        ft.remove(prev);
	  		    }
	  		    ft.addToBackStack(null);
	  			
	  			
	  		    ErrorLoadingDialogFragment frag = new ErrorLoadingDialogFragment();
	  		    frag.setOnPositiveClick(new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                	loadRemoteMeals();
	                }
	            });
	  		    frag.show(ft, "errorLoadingDialog");
	  		}
    	  }
    	};
    	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        if(data.isFirstTime){
	        QuickAction3D  qa = new QuickAction3D(DailyMealsActivity.this);
	        ActionItem a = new ActionItem(0, getResources().getString(R.string.info_change_screen));
	        qa.addActionItem(a);                
	        qa.show((View) mIndicator);
	        
	        qa = new QuickAction3D(DailyMealsActivity.this);
	        a = new ActionItem(0, getResources().getString(R.string.info_change_date));
	        qa.addActionItem(a);                
	        qa.show(dailyMealDate);
        }

        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
    	Intent intent;
        switch (item.getItemId()) {
            case R.id.sort_button:
            	intent = new Intent(DailyMealsActivity.this, DailyMealsSorterListActivity.class);
                startActivity(intent);
            	
                return true;
            case R.id.changelist_button:
            	intent = new Intent(DailyMealsActivity.this, MenusSelectionActivity.class);
                startActivity(intent);
                return true;
                
            case R.id.about_button:
            	Uri uri = Uri.parse("http://isisvn.unil.ch/campusfood/android");
            	intent = new Intent(Intent.ACTION_VIEW).setData(uri);
            	startActivity(intent);                
                return true;
                
                
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        data = ((CampusFoodApplication) getApplication()).getLocalStorage();
        setContentView(R.layout.daily_meals);

        dailyMealDate = (TextView) LayoutInflater.from(this).inflate(R.layout.daily_meal_date, null);
        dailyMealDate.setTypeface(CampusFoodApplication.FONT_ARCHITECTS_DAUGHTER);
        dailyMealDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				 SherlockDialogFragment frag = new DatePickerFragment();
				 Bundle args = new Bundle();
			     args.putIntArray("date", data.getDate());
			     frag.setArguments(args);
				 frag.show(getSupportFragmentManager(), "datePicker");
			}
		});
        //add the datepicker into the action bar at the center
        LayoutParams params = new LayoutParams(Gravity.CENTER); 
        getSupportActionBar().setCustomView(dailyMealDate, params);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        
        mPager = (ViewPager)findViewById(R.id.pager);
        mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        
        mAdapter = new DailyMealPagerAdapter(mPager, mIndicator);
        
        registerReceiver(mMessageReceiver, new IntentFilter(LocalStorageProvider.EVENT_MEALS_UPDATED));
        
        updateList();
        

        if (savedInstanceState != null) {
        	mPager.setCurrentItem(savedInstanceState.getInt("currentItem"));
        }else{
			int[] today = data.getTodayDate();
			if(!Arrays.equals(data.getDate(), today)){
				data.setDate(today);
				if(data.isFirstTime){
					setProgressBarIndeterminateVisibility(true);
					mAdapter.setLoading(true);
					mAdapter.notifyDataSetChanged();
					Intent intent = new Intent(DailyMealsActivity.this, MenusSelectionActivity.class);
	                startActivity(intent);
				}else{
					loadRemoteMeals();
				}
			}
        }
		updateDateDisplay();
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putInt("currentItem", mPager.getCurrentItem());
        
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }
    
    @Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this); // Add this method.
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
    	if(intent.hasExtra(CampusfoodAppWidgetProvider.EXTRA_CURRENT_POSITION)){
          	mPager.setCurrentItem(intent.getIntExtra(CampusfoodAppWidgetProvider.EXTRA_CURRENT_POSITION, 0));
        }
    }    

    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this); // Add this method.
    }    

    @Override
	protected void onDestroy() {
    	unregisterReceiver(mMessageReceiver);
		super.onDestroy();
	}

	private void updateDateDisplay() {
		int[] date = data.getDate();
        dailyMealDate.setText(
            new StringBuilder()
                    // Month is 0 based so add 1
            		.append(String.format("%02d", date[2])).append(".")        
            		.append(String.format("%02d", date[1] + 1)).append(".")
                    .append(date[0]));
    }
	
	private void updateList(){
		mAdapter.update(data.getVisibleMeals());
	}
	
	private void loadRemoteMeals() {
		setProgressBarIndeterminateVisibility(true);
		mAdapter.setLoading(true);
    	data.loadRemoteMeals();
	}
	
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		   int[] date = data.getDate();
		   date[0] = year;
		   date[1] = monthOfYear;
		   date[2] = dayOfMonth;
		   updateDateDisplay();
           loadRemoteMeals();
	}
	
	public static class DatePickerFragment extends SherlockDialogFragment {
	
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int[] date = getArguments().getIntArray("date");
			return new DatePickerDialog(getActivity(), (OnDateSetListener) getActivity(), date[0], date[1], date[2] );
		}

	}
	
 
}
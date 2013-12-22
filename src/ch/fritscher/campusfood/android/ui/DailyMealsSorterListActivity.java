package ch.fritscher.campusfood.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import ch.fritscher.campusfood.android.CampusFoodApplication;
import ch.fritscher.campusfood.android.R;
import ch.fritscher.campusfood.android.model.Menu;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.mobeta.android.dslv.DragSortListView;

public class DailyMealsSorterListActivity extends SherlockListActivity {
	
    private MenuAdapter adapter;

 

    private DragSortListView.DropListener onDrop =
        new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                Menu item = adapter.getItem(from);
                adapter.remove(item);
                adapter.insert(item, to);
            	Log.d("drop", "" + from +" " + to);
            }
        };

    private DragSortListView.RemoveListener onRemove = 
        new DragSortListView.RemoveListener() {
            @Override
            public void remove(int which) {
                adapter.remove(adapter.getItem(which));
            }
        };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
    	Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
            	intent = new Intent(this, DailyMealsActivity.class);
		        intent.addFlags(
		                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
		                    Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
            	finish();
                return true;
            case R.id.changelist_button:
				intent = new Intent(DailyMealsSorterListActivity.this, MenusSelectionActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	            startActivity(intent);
				return true;               
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
    	MenuItem mi = menu.add(0, R.id.changelist_button, 1, R.string.add_remove);
    	mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }
        
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.daily_meals_sorter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.sort);
        
        DragSortListView lv = (DragSortListView) getListView(); 

        lv.setDropListener(onDrop);
        lv.setRemoveListener(onRemove);

        adapter = new MenuAdapter();
        setListAdapter(adapter);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	adapter.notifyDataSetChanged();
    }
	
    @Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	((CampusFoodApplication) getApplication()).getLocalStorage().saveLocal();
    }
    
    private class ViewHolder {
        public TextView menu;
        public TextView location;
      }

      private class MenuAdapter extends ArrayAdapter<Menu> {
        
        public MenuAdapter() {
          super(DailyMealsSorterListActivity.this, R.layout.menu_item,
            R.id.menu, ((CampusFoodApplication) getApplication()).getLocalStorage().getMenuSelection());
        }

        public View getView(int position, View convertView, ViewGroup parent) {
          View v = super.getView(position, convertView, parent);

          if (v != convertView && v != null) {
            ViewHolder holder = new ViewHolder();

            TextView tv = (TextView) v.findViewById(R.id.menu);
            tv.setTypeface(CampusFoodApplication.FONT_ARCHITECTS_DAUGHTER);
            holder.menu = tv;
            tv = (TextView) v.findViewById(R.id.location);
            tv.setTypeface(CampusFoodApplication.FONT_ARCHITECTS_DAUGHTER);
            holder.location = tv;
            
            v.setTag(holder);
          }

          ViewHolder holder = (ViewHolder) v.getTag();
          Menu menu = getItem(position);

          holder.menu.setText(menu.getName());
          if(menu.getLocation() != null){
        	 holder.location.setText(menu.getLocation().getName());
          }else{
        	  holder.location.setText("");
          }

          return v;
        }
      }
    
}

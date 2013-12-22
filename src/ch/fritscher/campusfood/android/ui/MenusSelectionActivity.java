package ch.fritscher.campusfood.android.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction3D;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.TextView;
import ch.fritscher.campusfood.android.CampusFoodApplication;
import ch.fritscher.campusfood.android.R;
import ch.fritscher.campusfood.android.model.Campus;
import ch.fritscher.campusfood.android.model.Item;
import ch.fritscher.campusfood.android.model.LocalStorageProvider;
import ch.fritscher.campusfood.android.model.Location;
import ch.fritscher.campusfood.android.model.Menu;

import com.actionbarsherlock.app.SherlockExpandableListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;

public class MenusSelectionActivity extends SherlockExpandableListActivity {

	private CampusExpandableListAdapter mAdapter;
	private LocalStorageProvider data;

	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			setProgressBarIndeterminateVisibility(false);
			if (intent.getExtras().getBoolean(LocalStorageProvider.HAS_LOADED)) {
				List<Campus> campus = data.getCampus();
				mAdapter = new CampusExpandableListAdapter(
						MenusSelectionActivity.this, campus);
				setListAdapter(mAdapter);
				// TODO: maybe cache and restore expandList...
				getExpandableListView().expandGroup(0);
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						MenusSelectionActivity.this);
				builder.setMessage(R.string.error_load)
						.setPositiveButton(R.string.retry,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										setProgressBarIndeterminateVisibility(true);
										data.loadRemoteMenus();
									}
								})
						.setNegativeButton(R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// User cancelled the dialog
									}
								});
				builder.create().show();
			}
		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		Intent intent;
		switch (item.getItemId()) {
		case android.R.id.home:
			intent = new Intent(this, DailyMealsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			finish();
			return true;
		case R.id.sort_button:
			intent = new Intent(MenusSelectionActivity.this,
					DailyMealsSorterListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		MenuItem mi = menu.add(0, R.id.sort_button, 1, R.string.sort);
		mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		if (data.isFirstTime) {
			QuickAction3D qa = new QuickAction3D(MenusSelectionActivity.this);
			ActionItem a = new ActionItem(0, getResources().getString(
					R.string.info_select_menus));
			qa.addActionItem(a);
			View view = findViewById(android.R.id.home);
			if (view == null)
				view = findViewById(R.id.abs__home);
			qa.show(view);
			data.isFirstTime = false;
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		data = ((CampusFoodApplication) getApplication()).getLocalStorage();

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.add_remove);

		LocalBroadcastManager.getInstance(this).registerReceiver(
				mMessageReceiver,
				new IntentFilter(LocalStorageProvider.EVENT_LOCATIONS_LOADED));

		setProgressBarIndeterminateVisibility(true);
		data.loadRemoteMenus();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mAdapter != null) {
			data.updateMenuSelection(mAdapter.getSelectedMenus());
		}
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
	protected void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mMessageReceiver);
		super.onDestroy();
	}

	public class CampusExpandableListAdapter extends BaseExpandableListAdapter {

		private List<Campus> campus;
		private LayoutInflater mInflater;
		private Map<Long, List<Item>> children;
		private Set<Menu> selectedMenus;

		public CampusExpandableListAdapter(Context context, List<Campus> campus) {
			super();
			mInflater = LayoutInflater.from(context);
			this.campus = campus;
			selectedMenus = new HashSet<Menu>();
			children = new HashMap<Long, List<Item>>();
			List<Long> menuIds = data.getMenusOrder();
			for (Campus c : campus) {
				List<Item> items = new ArrayList<Item>();
				for (Location location : c.getLocations()) {
					items.add(location);
					items.addAll(location.getMenus());
					for (Menu menu : location.getMenus()) {
						if (menuIds.contains(menu.getId())) {
							selectedMenus.add(menu);
						}
					}
				}
				children.put(c.getId(), items);
			}

		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return children.get(getGroupId(groupPosition)).get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return ((Item) getChild(groupPosition, childPosition)).getId();
		}

		class ViewHolder {
			CheckedTextView text;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// A ViewHolder keeps references to children views to avoid
			// unneccessary calls
			// to findViewById() on each row.
			final ViewHolder holder;
			final Item i = (Item) getChild(groupPosition, childPosition);
			if (convertView == null) {
				convertView = mInflater
						.inflate(
								android.R.layout.simple_list_item_multiple_choice,
								null);
				// Creates a ViewHolder and store references to the views
				holder = new ViewHolder();
				holder.text = (CheckedTextView) convertView
						.findViewById(android.R.id.text1);
				holder.text
						.setTypeface(CampusFoodApplication.FONT_ARCHITECTS_DAUGHTER);
				convertView.setTag(holder);

			} else {
				// Get the ViewHolder back to get fast access to the TextView
				holder = (ViewHolder) convertView.getTag();
			}

			if (i instanceof Location) {
				holder.text.setText(((Location) i).getName());
				holder.text.setTextColor(getResources().getColor(
						android.R.color.primary_text_light));
				holder.text.setBackgroundColor(getResources().getColor(
						android.R.color.background_light));
				holder.text.setTypeface(CampusFoodApplication.FONT_ARCHITECTS_DAUGHTER, Typeface.BOLD);
				holder.text.setChecked(selectedMenus.containsAll(((Location) i)
						.getMenus()));
				holder.text.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (holder.text.isChecked()) {
							selectedMenus.removeAll(((Location) i).getMenus());
						} else {
							selectedMenus.addAll(((Location) i).getMenus());
						}
						notifyDataSetChanged();
					}
				});

			} else {
				holder.text.setTextColor(getResources().getColor(
						android.R.color.white));
				holder.text.setBackgroundColor(getResources().getColor(
						android.R.color.background_dark));
				holder.text.setTypeface(CampusFoodApplication.FONT_ARCHITECTS_DAUGHTER, Typeface.NORMAL);
				holder.text.setText(((Menu) i).getName());
				holder.text.setChecked(selectedMenus.contains(i));
				holder.text.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (holder.text.isChecked()) {
							selectedMenus.remove((Menu) i);
						} else {
							selectedMenus.add((Menu) i);
						}
						notifyDataSetChanged();
					}
				});
			}

			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return children.get(getGroupId(groupPosition)).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return campus.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return campus.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return ((Campus) getGroup(groupPosition)).getId();
		}

		class GroupViewHolder {
			TextView text;
			CheckBox chk;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			// A ViewHolder keeps references to children views to avoid
			// unneccessary calls
			// to findViewById() on each row.
			final GroupViewHolder holder;
			final Campus c = (Campus) getGroup(groupPosition);
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.menu_item_group, null);
				// Creates a ViewHolder and store references to the views
				holder = new GroupViewHolder();
				holder.text = (TextView) convertView
						.findViewById(android.R.id.text1);
				holder.chk = (CheckBox) convertView.findViewById(R.id.chkAll);
				holder.chk.setFocusable(false);
				convertView.setTag(holder);
			} else {
				// Get the ViewHolder back to get fast access to the TextView
				holder = (GroupViewHolder) convertView.getTag();
			}

			holder.text.setText(c.getName());
			// TODO: maybe move to Campus + cache?
			List<Menu> cm = new ArrayList<Menu>();
			for (Location l : c.getLocations()) {
				cm.addAll(l.getMenus());
			}

			holder.chk.setChecked(selectedMenus.containsAll(cm));
			holder.chk.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					for (Location l : c.getLocations()) {
						if (holder.chk.isChecked()) {
							selectedMenus.addAll(l.getMenus());
						} else {
							selectedMenus.removeAll(l.getMenus());
						}
					}
					notifyDataSetChanged();
				}
			});

			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}

		public Map<Menu, Boolean> getSelectedMenus() {
			// restore order of downloaded list
			Map<Menu, Boolean> map = new LinkedHashMap<Menu, Boolean>();
			for (Campus c : campus) {
				for (Location l : c.getLocations()) {
					for (Menu m : l.getMenus()) {
						map.put(m, selectedMenus.contains(m));
					}
				}
			}
			return map;
		}
	}
}

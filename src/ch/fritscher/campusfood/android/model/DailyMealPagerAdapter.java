package ch.fritscher.campusfood.android.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import ch.fritscher.campusfood.android.CampusFoodApplication;
import ch.fritscher.campusfood.android.R;

import com.viewpagerindicator.PageIndicator;

public class DailyMealPagerAdapter extends PagerAdapter {

	private List<Meal> meals = new ArrayList<Meal>();
	private ViewPager mPager;
	private PageIndicator mIndicator;
	private boolean loading = false;
	
    public DailyMealPagerAdapter(ViewPager mPager, PageIndicator mIndicator) {
        super();
        this.mPager = mPager;
        mPager.setAdapter(this);
        mIndicator.setViewPager(mPager);
        this.mIndicator = mIndicator;
    }

    
    @Override
    public Object instantiateItem(View collection, int position) {
    	Meal meal;
    	if(meals.size()==0){
    		meal = new Meal();
    		meal.setContent(CampusFoodApplication.getContext().getResources().getString(R.string.no_meal_content));	
    	}else{
    		meal = meals.get(position);
    	}
    	
		// Inflate the layout for this fragment
    	LayoutInflater layoutInflater = (LayoutInflater) collection.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.daily_meal_item, null);
		
		TextView title = (TextView) view.findViewById(R.id.daily_meal_title);
        title.setTypeface(CampusFoodApplication.FONT_ARCHITECTS_DAUGHTER);
        title.setText(meal.getTitle());
               
        TextView content = (TextView) view.findViewById(R.id.daily_meal_content);
        content.setTypeface(CampusFoodApplication.FONT_ARCHITECTS_DAUGHTER);
        content.setText(loading ? CampusFoodApplication.getContext().getResources().getString(R.string.meal_loading) : meal.getContent());


        // Add View to the ViewPager collection
        ((ViewPager) collection).addView(view);

        return view;
    }


	
	public void update(List<Meal> meals){
		loading=false;
		long mid=0;
		try{
			mid = this.meals.get(mPager.getCurrentItem()).getMid();
		}catch(Exception e){
			
		}
		this.meals.clear();
		this.meals.addAll(meals);
		notifyDataSetChanged();
		mIndicator.notifyDataSetChanged();
		mPager.setCurrentItem(findMenuIndex(mid));
	}
	
	private int findMenuIndex(long mid){
		for(int i=0;i<meals.size();i++ ){
			Meal meal = meals.get(i);
			if(meal.getMenu().getId() == mid){
				return i;
			}
		}
		return 0;
	}
	
	public void clear(){
		meals.clear();
	}

	@Override
	public int getCount() {
		if(meals.size() == 0){
			return 1;
		}else{
			return meals.size();	
		}
	}


	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == (View) object;
	}

	//force redraw
	@Override
	public int getItemPosition(Object object) {
		   return POSITION_NONE;
	}

	@Override
    public void destroyItem(View collection, int position, Object view){ 
        ((ViewPager) collection).removeView((View) view);
    }

    @Override
    public void finishUpdate(View arg0) {}


    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {}

    @Override
    public Parcelable saveState() {
        return null;
    }


	public boolean isLoading() {
		return loading;
	}


	public void setLoading(boolean loading) {
		this.loading = loading;
		notifyDataSetChanged();
		mIndicator.notifyDataSetChanged();
	}

	
	/*
    @Override
    public CharSequence getPageTitle(int position) {
      return;
    }
    */

}

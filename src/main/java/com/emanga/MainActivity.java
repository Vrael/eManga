package com.emanga;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.emanga.database.OrmLiteFragment;
import com.emanga.models.Chapter;
import com.emanga.views.Thumbnail;

public class MainActivity extends FragmentActivity {
	
	AppSectionsPagerAdapter mAppSectionsPagerAdapter;
	ViewPager mViewPager;
	
    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after 
     * previously being shut down then this Bundle contains the data it most 
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Home button navigates to main activity
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        //ViewPager uses support library fragments
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(
        		getSupportFragmentManager());
        
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
     
        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        initTabs(actionBar);
    }
    
    /**
     * Initialize tabs
     * @param actionBar
     */
    public void initTabs(ActionBar actionBar) {
    	// Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			public void onTabReselected(Tab tab,
					android.app.FragmentTransaction ft) {
				// TODO Auto-generated method stub
				// show the given tab
				
			}

			public void onTabSelected(Tab tab,
					android.app.FragmentTransaction ft) {
				// TODO Auto-generated method stub
				// hide the given tab
				
			}

			public void onTabUnselected(Tab tab,
					android.app.FragmentTransaction ft) {
				// TODO Auto-generated method stub
				// probably ignore this event
				
			}
        };

        // 4 Tabs: New, Library, Read, Favourites
        for(String title : getResources().getStringArray(R.array.tabs_titles)) {
	        actionBar.addTab(actionBar.newTab()
	        		.setText(title)
	                .setTabListener(tabListener));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(com.emanga.R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                // openSearch();
                return true;
            case R.id.action_settings:
                // openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {
    	
    	public AppSectionsPagerAdapter(FragmentManager fm) {
    		super(fm);
    	}
    	
    	@Override
    	public Fragment getItem(int i){
    		Fragment fragment = new SectionFragment();
            return fragment;
    	}

		@Override
		public int getCount() {
			return 4;
		}
		
		@Override
	    public CharSequence getPageTitle(int position) {
	        return "OBJECT " + (position + 1);
	    }
    }
    
    public static class SectionFragment extends OrmLiteFragment {  	
    	
    	@Override
        public View onCreateView(LayoutInflater inflater,
                ViewGroup container, Bundle savedInstanceState) {
			
			// Adapter for the gridview
			ThumbnailAdapter adapter = new ThumbnailAdapter(getActivity());

			new LoadLatestMangas(adapter).execute();
			
			// The last two arguments ensure LayoutParams are inflated properly.
            View rootView = inflater.inflate(
                    R.layout.fragment_section, container, false);
            
            GridView gridview = (GridView) rootView.findViewById(R.id.grid_view); 
            gridview.setAdapter(adapter);
        
            gridview.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Toast.makeText(getActivity(), "" + position, Toast.LENGTH_SHORT).show();
                }
            });

            return rootView;
        }
    	
    	private class LoadLatestMangas extends AsyncTask<Void, Integer, List<Thumbnail>> {

        	private ThumbnailAdapter adapter;
        	
        	public LoadLatestMangas(ThumbnailAdapter adap){
        		adapter = adap;
        	}
        	
    		@Override
    		protected List<Thumbnail> doInBackground(Void... params) {
    			//TODO: For the moment this returns all chapters, but this will must return last week chapters
    			Iterator<Chapter> it = getHelper().getChapterRunDao().queryForAll().iterator();
    			List<Thumbnail> thumbs = new ArrayList<Thumbnail>();
    			while(it.hasNext()) {
    				thumbs.add(new Thumbnail(it.next()));
    			}
    			return thumbs;
    		}
        	
    		@Override
    		protected void onPostExecute(List<Thumbnail> result) {
    			adapter.updateThumbnails(result);
    			super.onPostExecute(result);
    		}
        }
    }
}


package com.emanga.emanga.app.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.view.Window;

import com.emanga.emanga.app.R;
import com.emanga.emanga.app.fragments.HistorySectionFragment;
import com.emanga.emanga.app.fragments.LatestSectionFragment;
import com.emanga.emanga.app.fragments.LibrarySectionFragment;
import com.emanga.emanga.app.fragments.MangaDetailFragment;
import com.emanga.emanga.app.fragments.MangaListFragment;

public class MainActivity extends ActionBarActivity
	implements ActionBar.TabListener, MangaListFragment.Callbacks {
	
	public static final String TAG = MainActivity.class.getName();
	public static final String ACTION_TASK_ENDED = TAG + ".TASKENDED";

    private ViewPager mViewPager;

    private BroadcastReceiver mProgressBarReceiver = new BroadcastReceiver() {
        private int taskEnded;

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ACTION_TASK_ENDED)){
                taskEnded++;
                // 2: LatestChapters sync and Library sync
                if(taskEnded == 2){
                    setSupportProgressBarIndeterminateVisibility(false);
                }
            }
        }
    };

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after 
     * previously being shut down then this Bundle contains the data it most 
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
        setSupportProgressBarIndeterminateVisibility(true);

        AppSectionsPagerAdapter mAppSectionsPagerAdapter =
                new AppSectionsPagerAdapter(getSupportFragmentManager(), this);
        
        // Home button navigates to main activity
        final ActionBar actionBar = getSupportActionBar();
        // actionBar.setHomeButtonEnabled(false);
        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        
        // 3 Tabs: New, Library, Read
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
        	// Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
        	actionBar.addTab(
    			actionBar.newTab()
                	.setText(mAppSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }
        
        /**
         * on swiping the viewpager make respective tab selected
         * */
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
			}

			public void onPageSelected(int position) {
				// on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
			}

			public void onPageScrollStateChanged(int state) {
			}
        });
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public final static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

    	private Context mContext;
    	
        public AppSectionsPagerAdapter(FragmentManager fm, Context ctx) {
            super(fm);
            mContext = ctx;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new LatestSectionFragment();
                case 1:
                	return new LibrarySectionFragment();
                default:
                    return new HistorySectionFragment();

            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
        	return mContext.getResources().getStringArray(R.array.tabs_titles)[position];
        }
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
		
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	/**
	 * Callback method from {@link MangaListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	public void onItemSelected(String id) {
        Bundle arguments = new Bundle();
        arguments.putString(MangaDetailFragment.ARG_MANGA_ID, id);
        MangaDetailFragment fragment = new MangaDetailFragment();
        fragment.setArguments(arguments);

        if (LibrarySectionFragment.mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			getSupportFragmentManager()
                    .beginTransaction()
					.replace(R.id.manga_detail_container, fragment)
                    .commit();
		}
		else {
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.manga_list, fragment)
					.addToBackStack(null)
					.commit();
		}
	}

    @Override
    public void onResume(){
        super.onResume();
        // Register the receiver again
        LocalBroadcastManager.getInstance(this).registerReceiver(mProgressBarReceiver,
                new IntentFilter(ACTION_TASK_ENDED));
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mProgressBarReceiver);
        super.onPause();
    }
}


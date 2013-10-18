package com.emanga.activities;



import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.emanga.R;
import com.emanga.adapters.ThumbnailChapterAdapter;
import com.emanga.fragments.LibrarySectionFragment;
import com.emanga.fragments.MangaDetailFragment;
import com.emanga.fragments.MangaListFragment;
import com.emanga.loaders.LatestChaptersLoader;
import com.emanga.models.Chapter;
import com.emanga.services.UpdateDatabase;

public class MainActivity extends FragmentActivity 
	implements ActionBar.TabListener, MangaListFragment.Callbacks {
	
	private static final String TAG = MainActivity.class.getName();
	
	private AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    private ViewPager mViewPager;
	
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
        
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager(), this);
        
        // Home button navigates to main activity
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        
        // 4 Tabs: New, Library, Read, Favourites
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
        	// Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
        	actionBar.addTab(
    			actionBar.newTab()
                	.setText(mAppSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }
    }
    
    
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

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
                	
                case 2: 
                	return new FavouritesSectionFragment();
                
                default:
                   return new HistorySectionFragment();
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
        	return mContext.getResources().getStringArray(R.array.tabs_titles)[position];
        }
    }
    
    /**
     * A fragment that with Latest Chapters of Manga
     */
    public static class LatestSectionFragment extends Fragment
    	implements LoaderManager.LoaderCallbacks<List<Chapter>> {
    	
    	private ThumbnailChapterAdapter mAdapter;
        
    	@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			mAdapter = new ThumbnailChapterAdapter(getActivity());
			
			// Starts Update Database Services
			getActivity().startService(new Intent(getActivity(), UpdateDatabase.class));
		}
    	
    	@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	View rootView = inflater.inflate(
	                R.layout.fragment_section, container, false);
        	
            GridView gridview = (GridView) rootView.findViewById(R.id.grid_view); 
	        gridview.setAdapter(mAdapter);
	    
	        gridview.setOnItemClickListener(new OnItemClickListener() {
	        	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            		Intent intent = new Intent(getActivity(), ReaderActivity.class);
	            	intent.putExtra(ReaderActivity.ACTION_OPEN_CHAPTER, ((ThumbnailChapterAdapter.ViewHolder) v.getTag()).id);
	            	
	            	Toast.makeText(getActivity(), "Enjoy reading!", Toast.LENGTH_SHORT).show();
	            	startActivity(intent);
	            }
	        });
	        
	        return rootView;
        }
    	
    	@Override
    	public void onActivityCreated(Bundle savedInstanceState) {
    		super.onActivityCreated(savedInstanceState);
    		
    		getLoaderManager().initLoader(0, null, this);
    	}

		public Loader<List<Chapter>> onCreateLoader(int id, Bundle args) {
			return new LatestChaptersLoader(getActivity());
		}

		public void onLoadFinished(Loader<List<Chapter>> loader, List<Chapter> chapters) {
			mAdapter.addChapters(chapters);
		}

		public void onLoaderReset(Loader<List<Chapter>> chapters) {
			mAdapter.setChapters(null);
		}
		
    }
    
    /**
     * A fragment that with all Mangas orders by genre
     */
    /*
    public static class LibrarySectionFragment extends OrmliteFragment 
    	implements LoaderManager.LoaderCallbacks<List<Manga>> {    	  	
        
    	private HorizontalListView mHorizontalList;
        private MangaItemListAdapter mAdapter;
        
        @Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mAdapter = new MangaItemListAdapter(getActivity());
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	
        	View rootView = inflater.inflate(
	                R.layout.library, container, false);
    		
        	mHorizontalList = (HorizontalListView) rootView.findViewById(R.id.carousel_covers);
        	
        	mHorizontalList.setOnItemClickListener(new OnItemClickListener() {
	            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	            	// TODO: For now it will take always the first chapter (it must change in the future to last read)
	            	try {
	            		RuntimeExceptionDao<Chapter, Integer> chapterDao = getHelper().getChapterRunDao();
	            		QueryBuilder<Chapter, Integer> qBc = chapterDao.queryBuilder();
	            	
						qBc.where().eq(Chapter.MANGA_COLUMN_NAME, mAdapter.getItem(position).title);
						Chapter chapter = qBc.queryForFirst();
						// TODO: Change this, query two times, one here and then in ReaderActivity
						Intent intent = new Intent(getActivity(), ReaderActivity.class);
		            	intent.putExtra(ReaderActivity.ACTION_OPEN_CHAPTER, chapter.id);
		            	
		            	Toast.makeText(getActivity(), "Enjoy reading!", Toast.LENGTH_SHORT).show();
		            	startActivity(intent);
					} catch (SQLException e) {
						Toast.makeText(getActivity(), "Sorry, this manga hasn't chapters yet!", Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
	            }
	        });
        	
        	mHorizontalList.setOnHoverListener(new OnHoverListener() {
				
				public boolean onHover(View v, MotionEvent event) {
					Toast.makeText(getActivity(), "HOVER EVENT!", Toast.LENGTH_SHORT).show();
					System.out.println("HOVER EVENT!");
					return false;
				}
			});
        	
        	mHorizontalList.setOnGenericMotionListener(new OnGenericMotionListener() {
				
				public boolean onGenericMotion(View v, MotionEvent event) {
					Toast.makeText(getActivity(), "GENERIC MOTION EVENT!", Toast.LENGTH_SHORT).show();
					System.out.println("GENERIC MOTION EVENT!");
					return false;
				}
			});
        	
        	mHorizontalList.setOnDragListener(new OnDragListener() {
				
				public boolean onDrag(View v, DragEvent event) {
					Toast.makeText(getActivity(), "DRAG MOTION EVENT!", Toast.LENGTH_SHORT).show();
					System.out.println("DRAG MOTION EVENT!");
					return false;
				}
			});
        	
          	mHorizontalList.setAdapter(mAdapter);
        	
        	return rootView;
        }
        
        @Override
    	public void onActivityCreated(Bundle savedInstanceState) {
    		super.onActivityCreated(savedInstanceState);
    		
    		getLoaderManager().initLoader(1, null, this);
    	}

		public Loader<List<Manga>> onCreateLoader(
				int id, Bundle args) {
			return new LibraryLoader(getActivity());
		}

		public void onLoadFinished( Loader<List<Manga>> loader, List<Manga> mangas ) {			
			mAdapter.setMangas(mangas);
		}

		public void onLoaderReset(Loader<List<Manga>> mangas) {
			mAdapter.setMangas(null);
		}
    }
    */
    /**
     * A fragment that with favourites Mangas
     */
    public static class FavouritesSectionFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	View rootView = inflater.inflate(
	                R.layout.fragment_section, container, false);
        	
        	return rootView;
        }
    }
    
    /**
     * A fragment that with history
     */
    public static class HistorySectionFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	View rootView = inflater.inflate(
	                R.layout.fragment_section, container, false);
        	
        	return rootView;
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
        	case android.R.id.home:
        		NavUtils.navigateUpFromSameTask(this);
        		return true;
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
		if (LibrarySectionFragment.mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(MangaDetailFragment.ARG_MANGA_ID, id);
			Log.d(TAG, "Id of manga selected" + id);
			MangaDetailFragment fragment = new MangaDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.manga_detail_container, fragment).commit();
		}
		else {
			Bundle arguments = new Bundle();
			arguments.putString(MangaDetailFragment.ARG_MANGA_ID, id);
			Log.d(TAG, "Id of manga selected");
			MangaDetailFragment fragment = new MangaDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.manga_list, fragment).commit();
		}
	}
}

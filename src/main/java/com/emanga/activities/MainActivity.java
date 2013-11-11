package com.emanga.activities;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.WordUtils;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.emanga.R;
import com.emanga.adapters.ThumbnailChapterAdapter;
import com.emanga.adapters.ThumbnailChapterAdapter.ViewHolder;
import com.emanga.database.OrmliteFragment;
import com.emanga.fragments.LibrarySectionFragment;
import com.emanga.fragments.MangaDetailFragment;
import com.emanga.fragments.MangaListFragment;
import com.emanga.loaders.LatestChaptersLoader;
import com.emanga.models.Chapter;
import com.emanga.models.Manga;
import com.emanga.services.UpdateLatestChaptersService;
import com.emanga.utils.Image;
import com.emanga.utils.Notification;
import com.j256.ormlite.stmt.QueryBuilder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

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
        actionBar.setHomeButtonEnabled(false);
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
        
        /**
         * on swiping the viewpager make respective tab selected
         * */
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				// TODO Auto-generated method stub
				
			}

			public void onPageSelected(int position) {
				// on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
			}

			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub
				
			}
         
           
        });
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
    
    /**
     * A fragment that with Latest Chapters of Manga
     */
    public static class LatestSectionFragment extends Fragment
    	implements LoaderManager.LoaderCallbacks<List<Chapter>> {
    	
    	private ThumbnailChapterAdapter mAdapter;
    	private ProgressBar bar;
    	
    	// This Receiver updates the progress bar (that indicates there are chapters 
    	// restoring and processing from internet)
    	private BroadcastReceiver mChapterReceiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		    	Log.d(TAG, "Progress received from Latest Chapters Service");
		    	
		    	bar.setProgress(
		    		intent.getIntExtra(UpdateLatestChaptersService.EXTRA_CHAPTERS_PROCESS, 100)
		    		);
		   }
		};
		
    	@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mAdapter = new ThumbnailChapterAdapter(getActivity());
			
			Activity activity = getActivity();
			
			// Starts Update Database Services
			activity.startService(new Intent(getActivity(), UpdateLatestChaptersService.class));
			activity.registerReceiver(mChapterReceiver, new IntentFilter(UpdateLatestChaptersService.ACTION_PROGRESS));
		}
    		
    	@Override
    	public void onDestroy(){
    		super.onDestroy();
    		getActivity().unregisterReceiver(mChapterReceiver);
    	}
    	
    	@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	View rootView = inflater.inflate(
	                R.layout.fragment_section, container, false);
        	
            bar = (ProgressBar) getActivity().findViewById(R.id.progressbar_background_tasks);
            bar.setProgress(1);
            
        	GridView gridview = (GridView) rootView.findViewById(R.id.grid_view); 
	        gridview.setAdapter(mAdapter);
	    
	        gridview.setOnItemClickListener(new OnItemClickListener() {
	        	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            		Intent intent = new Intent(getActivity(), ReaderActivity.class);
            		Chapter chapter = mAdapter.getItem(position);
	            	intent.putExtra(ReaderActivity.ACTION_OPEN_CHAPTER, chapter.number);
	            	intent.putExtra(Chapter.ID_COLUMN_NAME, chapter.id);
	            	intent.putExtra(Manga.TITLE_COLUMN_NAME, chapter.manga.title);
	            	intent.putExtra(Manga.ID_COLUMN_NAME, chapter.manga.id);
	            	
	            	Notification.enjoyReading(getActivity()).show();
	            	
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
     * A fragment that with history
     */
    public static class HistorySectionFragment extends OrmliteFragment {
    	
    	private GridView mGridView;
    	private ItemAdapter mAdapter;
    	
    	@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    		// Get chapters from database
    		new LoadHistory().execute();
    		
    		// Set view fot this fragment
        	View rootView = inflater.inflate(
	                R.layout.fragment_section, container, false);
            
        	mGridView = (GridView) rootView.findViewById(R.id.grid_view); 
        	mAdapter = new ItemAdapter(getActivity());
        	
        	mGridView.setOnItemClickListener(new OnItemClickListener() {
	        	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            		Intent intent = new Intent(getActivity(), ReaderActivity.class);
            		Chapter chapter = mAdapter.getItem(position);
	            	intent.putExtra(ReaderActivity.ACTION_OPEN_CHAPTER, chapter.number);
	            	intent.putExtra(Chapter.ID_COLUMN_NAME, chapter.id);
	            	intent.putExtra(Manga.TITLE_COLUMN_NAME, chapter.manga.title);
	            	intent.putExtra(Manga.ID_COLUMN_NAME, chapter.manga.id);
	            	
	            	Notification.enjoyReading(getActivity()).show();
	            	
	            	startActivity(intent);
	            }
	        });
	        
	        return rootView;
        }
    	
    	private static class ItemAdapter extends BaseAdapter {
        	private Context mContext;
        	private DisplayImageOptions options;
        	private ImageLoader imageLoader;
    		public Chapter[] mChapters;
    		
    		public ItemAdapter(Context context){
    			mContext = context;
                
            	options = new DisplayImageOptions.Builder()
        	    	.showImageForEmptyUri(R.drawable.ic_content_picture)
        	    	.showImageOnFail(R.drawable.ic_content_remove)
        	    	.cacheInMemory(true)
        	    	.cacheOnDisc(true)
        	    	.build();
            	
            	imageLoader = ImageLoader.getInstance();
    		}
    		
			public int getCount() {
				return mChapters.length;
			}

			public Chapter getItem(int position) {
				return mChapters[position];
			}

			public long getItemId(int position) {
				return mChapters[position].id;
			}

			public View getView(int position, View convertView, ViewGroup parent) {
		    	final ViewHolder holder;
		    	
		    	// if it's not recycled, initialize some attributes
		    	if (convertView == null) {
		    		convertView = LayoutInflater.from(mContext).inflate(R.layout.thumbnail_read_item, parent, false);
		    		
		    		holder = new ViewHolder();
		    		holder.cover = (ImageView) convertView.findViewById(R.id.thumb_read_cover);
		    		holder.date = (TextView) convertView.findViewById(R.id.thumb_read_date);
		    		holder.title = (TextView) convertView.findViewById(R.id.thumb_read_title);
		    		
		    		convertView.setTag(holder);
				} else {
				    holder = (ViewHolder) convertView.getTag();
				}	
		    	
		    	Chapter chapter = getItem(position);
		    	
		    	holder.date.setText(ThumbnailChapterAdapter.formatDate(chapter.read));
		    	holder.title.setText(WordUtils.capitalize(chapter.manga.title));
		    	
		    	imageLoader.displayImage(chapter.manga.cover, holder.cover, options, new ImageLoadingListener() {

					public void onLoadingStarted(String imageUri, View view) {
						// TODO Auto-generated method stub
						
					}

					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
						// TODO Auto-generated method stub
						
					}

					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						holder.cover.setImageBitmap(Image.getCroppedBitmap(loadedImage));
					}

					public void onLoadingCancelled(String imageUri, View view) {
						// TODO Auto-generated method stub
						
					}
		    		
		    	});    	
		        return convertView;
			}
    	}
    	
    	private class LoadHistory extends AsyncTask<Void, Integer, Chapter[]>{

			@Override
			protected Chapter[] doInBackground(Void... arg0) {
				List<Chapter> chapters = null;
				try {
					QueryBuilder<Chapter, Integer> qBc = getHelper().getChapterRunDao().queryBuilder();
					qBc.where().isNotNull(Chapter.READ_COLUMN_NAME);
					qBc.orderBy(Chapter.READ_COLUMN_NAME, false);
					qBc.groupBy(Chapter.MANGA_COLUMN_NAME);
				
					chapters = qBc.query();
				} catch (SQLException e) {

					e.printStackTrace();
				}
				return (chapters != null)? chapters.toArray(new Chapter[chapters.size()]) : null;
			}
    		
			@Override
			protected void onPostExecute(Chapter[] result) {
		         if(result != null){
		        	 mAdapter.mChapters = result;
		        	 mGridView.setAdapter(mAdapter);
		         }
		    }
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
	public void onItemSelected(int id) {
		if (LibrarySectionFragment.mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putInt(MangaDetailFragment.ARG_MANGA_ID, id);
			Log.d(TAG, "Id of manga selected" + id);
			MangaDetailFragment fragment = new MangaDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.manga_detail_container, fragment).commit();
		}
		else {
			Bundle arguments = new Bundle();
			arguments.putInt(MangaDetailFragment.ARG_MANGA_ID, id);
			Log.d(TAG, "Id of manga selected: " + id);
			MangaDetailFragment fragment = new MangaDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.manga_list, fragment)
					.addToBackStack(null)
					.commit();
		}
	}
}


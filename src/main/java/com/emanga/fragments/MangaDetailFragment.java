package com.emanga.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.emanga.R;
import com.emanga.activities.ReaderActivity;
import com.emanga.database.OrmliteFragment;
import com.emanga.loaders.MangaDetailLoader;
import com.emanga.models.Category;
import com.emanga.models.Manga;
import com.emanga.services.UpdateDescriptionService;
import com.emanga.utils.Notification;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

/**
 * A fragment representing a single Manga detail screen. This fragment is either
 * contained in a {@link MangaListActivity} in two-pane mode (on tablets) or a
 * {@link MangaDetailActivity} on handsets.
 */
public class MangaDetailFragment extends OrmliteFragment 
	implements LoaderManager.LoaderCallbacks<Manga>{
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_MANGA_ID = "manga_id";
	
	/**
	 * The manga content this fragment is presenting.
	 */
	private int mangaId;
	private Manga mManga;
	
	private DisplayImageOptions options;
	private ImageLoader imageLoader;
	
	private View rootView;
	private Loader<Manga> mLoader;
	
	
	private BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
		@Override
        public void onReceive(Context context, Intent intent) {
			// If there is a new description in DB, loader reload the data
			if(intent.getBooleanExtra(UpdateDescriptionService.RELOAD, false)){
        		mLoader.onContentChanged();
        	} else {
        		// If not, shows a message
        		TextView description = (TextView) rootView.findViewById(R.id.manga_description);
        		description.setText("< No description yet >");
        		description.setGravity(Gravity.CENTER);
        	}
        }
    };
    
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MangaDetailFragment() {}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getArguments().containsKey(ARG_MANGA_ID)) {
			mangaId = getArguments().getInt(ARG_MANGA_ID);
			mLoader = getLoaderManager().restartLoader(4, null, this);
		}
		
		options = new DisplayImageOptions.Builder()
	    	.showImageForEmptyUri(R.drawable.ic_content_picture)
	    	.showImageOnFail(R.drawable.ic_content_remove)
	    	.cacheInMemory(true)
	    	.cacheOnDisc(true)
	    	.displayer(new RoundedBitmapDisplayer(5))
	    	.build();
		
		imageLoader = ImageLoader.getInstance();
		
		// Register the result of service that gets the description from internet
		getActivity().registerReceiver(mResultReceiver, 
				new IntentFilter(UpdateDescriptionService.ACTION_RELOAD));
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_manga_detail,
				container, false);
	
		return rootView;
	}
	
	public Loader<Manga> onCreateLoader(int id, Bundle args) {
		return new MangaDetailLoader(getActivity(), mangaId);
	}
	
	public void onLoadFinished(Loader<Manga> loader, final Manga manga) {
		mManga = manga;
		
		ProgressBar bar = (ProgressBar) rootView.findViewById(R.id.manga_progressbar);
		bar.setVisibility(View.GONE);
		
		TextView text = (TextView) rootView.findViewById(R.id.manga_title); 
		text.setText(manga.title);
		
		ImageView image = (ImageView) rootView.findViewById(R.id.manga_cover); 
		imageLoader.displayImage(manga.cover, image, options);
		
		
		text = (TextView) rootView.findViewById(R.id.manga_categories);
		StringBuilder names = new StringBuilder();
		// Create a string with the categories names and upper case the first letter, eg: Love, Action
		Category category = manga.categories.get(0);
		names.append(" ").append(Character.toUpperCase(category.name.charAt(0))).append(category.name.substring(1));
		manga.categories.remove(0);
		for(Category c : manga.categories){
			names.append(", ").append(c.toString());
		}
		text.setText(names);
			
		text = (TextView) rootView.findViewById(R.id.manga_description); 
		String descriptionText = (manga.description == null || manga.description.isEmpty())?
				"Loading..." : manga.description;
		text.setText(descriptionText);
		text.setVisibility(View.VISIBLE);
		
		if(manga.chaptersList != null && manga.chaptersList.length > 0){
			Button buttonContinue = (Button) rootView.findViewById(R.id.manga_button_continue);
			buttonContinue.setVisibility(View.VISIBLE);
			buttonContinue.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	            	Intent intent = new Intent(getActivity(), ReaderActivity.class);
	            	intent.putExtra(ReaderActivity.ACTION_OPEN_CHAPTER, manga.chaptersList[0].number);
	            	intent.putExtra(Manga.TITLE_COLUMN_NAME, mManga.title);
	            	intent.putExtra(Manga.ID_COLUMN_NAME, mManga.id);
	            	
	            	Notification.enjoyReading(getActivity()).show();
	            	
	            	startActivity(intent);
	            }
	        });
		}
		Button buttonStart = (Button) rootView.findViewById(R.id.manga_button_start);
		buttonStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				Intent intent = new Intent(getActivity(), ReaderActivity.class);
            	intent.putExtra(ReaderActivity.ACTION_OPEN_CHAPTER, 0);
            	intent.putExtra(Manga.TITLE_COLUMN_NAME, mManga.title);
            	intent.putExtra(Manga.ID_COLUMN_NAME, mManga.id);
            	
            	Notification.enjoyReading(getActivity()).show();
            	
            	startActivity(intent);
            }
        });
	}
	
	public void onLoaderReset(Loader<Manga> loader) {
		mManga = null;
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		// Unregister service broadcast receiver
		getActivity().unregisterReceiver(mResultReceiver);
	}
}

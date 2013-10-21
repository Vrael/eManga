package com.emanga.fragments;

import java.sql.SQLException;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.emanga.R;
import com.emanga.activities.ReaderActivity;
import com.emanga.database.OrmliteFragment;
import com.emanga.loaders.MangaDetailLoader;
import com.emanga.models.Category;
import com.emanga.models.Chapter;
import com.emanga.models.MangaContent;
import com.emanga.models.MangaContent.MangaItem;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

/**
 * A fragment representing a single Manga detail screen. This fragment is either
 * contained in a {@link MangaListActivity} in two-pane mode (on tablets) or a
 * {@link MangaDetailActivity} on handsets.
 */
public class MangaDetailFragment extends OrmliteFragment 
	implements LoaderManager.LoaderCallbacks<MangaContent.MangaItem>{
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_MANGA_ID = "manga_id";
	
	/**
	 * Manga that data will look for in database
	 */
	private String mangaId;
	/**
	 * The manga content this fragment is presenting.
	 */
	private MangaContent.MangaItem mManga;
	
	private DisplayImageOptions options;
	private ImageLoader imageLoader;
	
	private View rootView;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MangaDetailFragment() {}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getArguments().containsKey(ARG_MANGA_ID)) {
			mangaId = getArguments().getString(ARG_MANGA_ID);
			getLoaderManager().initLoader(8, null, this);
		}
		
		options = new DisplayImageOptions.Builder()
	    	.showImageForEmptyUri(R.drawable.ic_content_picture)
	    	.showImageOnFail(R.drawable.ic_content_remove)
	    	.cacheInMemory(true)
	    	.cacheOnDisc(true)
	    	.displayer(new RoundedBitmapDisplayer(5))
	    	.build();
		
		imageLoader = ImageLoader.getInstance();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_manga_detail,
				container, false);
	
		return rootView;
	}
	
	public Loader<MangaItem> onCreateLoader(int id, Bundle args) {
		return new MangaDetailLoader(getActivity(), mangaId);
	}
	
	public void onLoadFinished(Loader<MangaItem> loader, MangaItem data) {
		mManga = data;
		
		ProgressBar bar = (ProgressBar) rootView.findViewById(R.id.manga_progressbar);
		bar.setVisibility(View.GONE);
		
		TextView text = (TextView) rootView.findViewById(R.id.manga_title); 
		text.setText(data.manga.title);
		
		ImageView image = (ImageView) rootView.findViewById(R.id.manga_cover); 
		imageLoader.displayImage(mManga.manga.cover, image, options);
		
		
		text = (TextView) rootView.findViewById(R.id.manga_categories);
		StringBuilder names = new StringBuilder();
		// Create a string with the categories names and upper case the first letter, eg: Love, Action
		Category category = data.categories.get(0);
		names.append(" ").append(Character.toUpperCase(category.name.charAt(0))).append(category.name.substring(1));
		data.categories.remove(0);
		for(Category c : data.categories){
			names.append(", ").append(Character.toUpperCase(c.name.charAt(0))).append(c.name.substring(1));
		}
		text.setText(names);
			
		text = (TextView) rootView.findViewById(R.id.manga_description); 
		String descriptionText = (mManga.manga.description == null || mManga.manga.description.isEmpty())?
				"< No description yet >" : data.manga.description;
		text.setText(descriptionText);
		text.setVisibility(View.VISIBLE);
		
		Button button = (Button) rootView.findViewById(R.id.manga_button_start);
		button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	// TODO: For now it will take always the first chapter (it must change in the future to last read)
            	try {
            		RuntimeExceptionDao<Chapter, Integer> chapterDao = getHelper().getChapterRunDao();
            		QueryBuilder<Chapter, Integer> qBc = chapterDao.queryBuilder();
            	
					qBc.where().eq(Chapter.MANGA_COLUMN_NAME, mManga.manga.title);
					Chapter chapter = qBc.queryForFirst();
					
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
	}
	
	public void onLoaderReset(Loader<MangaItem> loader) {
		mManga = null;
	}
}

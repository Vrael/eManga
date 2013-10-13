package com.emanga.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.emanga.R;
import com.emanga.loaders.MangaDetailLoader;
import com.emanga.models.MangaContent;
import com.emanga.models.MangaContent.MangaItem;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

/**
 * A fragment representing a single Manga detail screen. This fragment is either
 * contained in a {@link MangaListActivity} in two-pane mode (on tablets) or a
 * {@link MangaDetailActivity} on handsets.
 */
public class MangaDetailFragment extends Fragment 
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
	text.setText(mManga.manga.title);
	text.setVisibility(View.VISIBLE);
	
	ImageView image = (ImageView) rootView.findViewById(R.id.manga_cover); 
	imageLoader.displayImage(mManga.manga.cover, image, options);
	image.setVisibility(View.VISIBLE);
	
	text = (TextView) rootView.findViewById(R.id.manga_description); 
	text.setText((mManga.manga.description == null || mManga.manga.description.isEmpty())? 
			"< There isn't a description about this manga yet >": mManga.manga.description);
	text.setVisibility(View.VISIBLE);
	
	((Button) rootView.findViewById(R.id.manga_button_start)).setVisibility(View.VISIBLE);
	
	// TODO: Falta las categorías!!
	// TODO: Falta el Listener del botón!!
}

public void onLoaderReset(Loader<MangaItem> loader) {
	mManga = null;
}
}

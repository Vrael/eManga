package com.emanga.emanga.app.adapters;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.emanga.emanga.app.R;
import com.emanga.emanga.app.cache.BitmapLruCache;
import com.emanga.emanga.app.cache.ImageCacheManager;
import com.emanga.emanga.app.controllers.App;
import com.emanga.emanga.app.models.Genre;
import com.emanga.emanga.app.models.Manga;
import com.emanga.emanga.app.utils.CoverNetworkImageView;

import org.apache.commons.lang.WordUtils;

public class MangaItemListCursorAdapter extends SimpleCursorAdapter
implements SectionIndexer {

	private LayoutInflater mInflater;
	
	private AlphabetIndexer mAlphaIndexer;

	private ImageLoader imageLoader;
	
	public MangaItemListCursorAdapter(Context context, int layout, Cursor c,
	    String[] from, int[] to, int flags) {
	    super(context, layout, c, from, to, flags);
	    
	    mInflater = LayoutInflater.from(context);

        imageLoader = new ImageLoader(App.getInstance().mRequestQueue, new BitmapLruCache());
	}
	
	public int getPositionForSection(int section) {
	    return mAlphaIndexer.getPositionForSection(section);
	}
	
	public int getSectionForPosition(int position) {
	    return mAlphaIndexer.getSectionForPosition(position);
	}
	
	public Object[] getSections() {
	    return mAlphaIndexer.getSections();
	}

	@Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.manga_item_list, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView title = (TextView) view.findViewById(R.id.manga_list_title);
        TextView categories = (TextView) view.findViewById(R.id.manga_list_categories);

        title.setText(cursor.getString(cursor.getColumnIndex(Manga.TITLE_COLUMN_NAME)));
        categories.setText(WordUtils.capitalize(cursor.getString(cursor.getColumnIndex(Genre.NAME_COLUMN_NAME))));

        CoverNetworkImageView cover = (CoverNetworkImageView) view.findViewById(R.id.manga_list_cover);
        String url = cursor.getString(cursor.getColumnIndex(Manga.COVER_COLUMN_NAME));
        cover.setImageUrl(url, ImageCacheManager.getInstance().getImageLoader());
        cover.setErrorImageResId(R.drawable.no_cover);
    }
    
	public Cursor swapCursor(Cursor c) {
	    Cursor mCursor = getCursor();
		if(mCursor != null){
	    	mCursor.close();
	    }
		// Create our indexer
	    if (c != null) {
	        mAlphaIndexer = new AlphabetIndexer(c, c.getColumnIndex(Manga.TITLE_COLUMN_NAME),
	            " -0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
	    }
	    return super.swapCursor(c);
	}
}
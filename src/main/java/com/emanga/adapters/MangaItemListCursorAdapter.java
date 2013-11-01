package com.emanga.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.emanga.R;
import com.emanga.models.Category;
import com.emanga.models.Manga;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class MangaItemListCursorAdapter extends SimpleCursorAdapter 
implements SectionIndexer{

	private LayoutInflater mInflater;
	
	private AlphabetIndexer mAlphaIndexer;
	
	private DisplayImageOptions options;
	private ImageLoader imageLoader;
	
	public MangaItemListCursorAdapter(Context context, int layout, Cursor c,
	    String[] from, int[] to, int flags) {
	    super(context, layout, c, from, to, flags);
	    
	    mInflater = LayoutInflater.from(context);
        
        options = new DisplayImageOptions.Builder()
	    	.showImageForEmptyUri(R.drawable.ic_content_picture)
	    	.showImageOnFail(R.drawable.ic_content_remove)
	    	.cacheInMemory(true)
	    	.cacheOnDisc(true)
	    	.displayer(new RoundedBitmapDisplayer(10))
	    	.build();
	
        imageLoader = ImageLoader.getInstance();
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
        View v = mInflater.inflate(R.layout.manga_item_list, parent, false);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView title = (TextView) view.findViewById(R.id.manga_list_title);
        TextView categories = (TextView) view.findViewById(R.id.manga_list_categories);

        title.setText(cursor.getString(cursor.getColumnIndex(Manga.TITLE_COLUMN_NAME)));
        categories.setText(cursor.getString(cursor.getColumnIndex(Category.NAME_COLUMN_NAME)));
        
        ImageView cover = (ImageView) view.findViewById(R.id.manga_list_cover);
        imageLoader.displayImage(cursor.getString(cursor.getColumnIndex(Manga.COVER_COLUMN_NAME)),
                cover, options);
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
	
	static class ViewHolder {
		ImageView cover;
		TextView title;
		TextView categories;
	}
}
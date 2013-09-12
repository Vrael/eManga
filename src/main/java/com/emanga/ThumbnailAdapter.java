package com.emanga;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.emanga.views.Thumbnail;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ThumbnailAdapter extends BaseAdapter {
	public static String TAG = ThumbnailAdapter.class.getName();
	
	private Context mContext;

	public List<Thumbnail> thumbnails = new ArrayList<Thumbnail>();
	
	private DisplayImageOptions options;
	private ImageLoader imageLoader;
	
    public ThumbnailAdapter(Context c) {
        mContext = c;
        
    	options = new DisplayImageOptions.Builder()
	    	.showImageForEmptyUri(R.drawable.ic_content_picture)
	    	.showImageOnFail(R.drawable.ic_content_remove)
	    	.cacheInMemory(true)
	    	.cacheOnDisc(true)
	    	.bitmapConfig(Bitmap.Config.RGB_565)
	    	.build();
    	imageLoader = ImageLoader.getInstance();
    }

    public int getCount() {
        return thumbnails.size();
    }

    public Thumbnail getItem(int position) {
        return thumbnails.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new TextView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
    	
    	LinearLayout layout;
    	TextView date;
    	ImageView cover;
    	TextView title;
        // if it's not recycled, initialize some attributes
    	if (convertView == null) {  
    		/* --------- */
    		/* | Date  | */
    		/* | Cover | */
    		/* | Title | */
    		/* --------- */
    		layout = new LinearLayout(mContext);
    		layout.setOrientation(LinearLayout.VERTICAL);
    		layout.setLayoutParams(new GridView.LayoutParams(
    			     (int) mContext.getResources().getDimension(R.dimen.gridview_thumb_width),
    			     (int) mContext.getResources().getDimension(R.dimen.gridview_thumb_height)
    			     ));
    		
    		layout.setBackgroundColor(Color.WHITE);
    		
    		date = new TextView(mContext);
    		cover = new ImageView(mContext);
    		title = new TextView(mContext);
    		
    		LinearLayout.LayoutParams paramsText = new LinearLayout.LayoutParams(
    				LayoutParams.MATCH_PARENT,
    				LayoutParams.WRAP_CONTENT
    				);
    		
    		paramsText.setMargins(8, 8, 8, 8);
    		
    		date.setLayoutParams(paramsText);
    		title.setLayoutParams(paramsText);
    		
    		date.setMaxLines(1);
    		title.setMaxLines(2);
    		
    		cover.setLayoutParams(new LinearLayout.LayoutParams(
    				(int) mContext.getResources().getDimension(R.dimen.gridview_thumb_width),
    				(int) mContext.getResources().getDimension(R.dimen.gridview_cover_height)
    				));
    		
    		date.setGravity(Gravity.CENTER);
    		title.setGravity(Gravity.CENTER);
    		
    		date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
    		title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
    		
    		layout.addView(date);
    		layout.addView(cover);
    		layout.addView(title);
    		
    	} else {
    		layout = (LinearLayout) convertView;
    		
    		date = (TextView) layout.getChildAt(0);
    		cover = (ImageView) layout.getChildAt(1);
    		title = (TextView) layout.getChildAt(2);
    	}
    	
    	Thumbnail thumb = getItem(position);
    	
    	date.setText(thumb.date);
    	imageLoader.displayImage(getItem(position).image, cover, options);
    	title.setText(thumb.title);
    	
        return layout;
    }
    
}

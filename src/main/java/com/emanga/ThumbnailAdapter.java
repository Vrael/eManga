package com.emanga;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

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
    	
        final ImageView imageView;
        // if it's not recycled, initialize some attributes
    	if (convertView == null) {  
    		imageView = new ImageView(mContext);
    		imageView.setLayoutParams(new GridView.LayoutParams(
    				(int) mContext.getResources().getDimension(R.dimen.gridview_thumb_width),
    				(int) mContext.getResources().getDimension(R.dimen.gridview_thumb_height)
    				));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(4, 4, 4, 4);
    	}
    	else {
    		imageView = (ImageView) convertView;
    	}
    	
    	imageLoader.displayImage(getItem(position).image, imageView, options);
    	
        return imageView;
    }
    
}

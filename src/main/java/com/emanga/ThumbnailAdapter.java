package com.emanga;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.emanga.views.Thumbnail;

public class ThumbnailAdapter extends BaseAdapter{
	private Context mContext;
	private List<Thumbnail> thumbnails = Collections.emptyList();

    public ThumbnailAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return thumbnails.size();
    }

    public Thumbnail getItem(int position) {
        return thumbnails.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new TextView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        // if it's not recycled, initialize some attributes
    	if (convertView == null) {  
    		textView = new TextView(mContext);
    		textView.setLayoutParams(new GridView.LayoutParams(200, 260));
            textView.setPadding(4, 4, 4, 4);
    	}
    	else {
    		textView = (TextView) convertView;
    	}
    	
    	textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0 ,getItem(position).image);
    	
        return textView;
    }
    
    public void updateThumbnails(List<Thumbnail> thumbList){
    	thumbnails = thumbList;
    	notifyDataSetChanged();
    }
    
}

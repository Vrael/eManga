package com.emanga.emanga.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.emanga.emanga.app.R;
import com.emanga.emanga.app.cache.BitmapLruCache;
import com.emanga.emanga.app.cache.ImageCacheManager;
import com.emanga.emanga.app.controllers.App;
import com.emanga.emanga.app.listeners.CoverListener;
import com.emanga.emanga.app.models.Chapter;
import com.emanga.emanga.app.utils.CustomNetworkImageView;

import org.apache.commons.lang.WordUtils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ThumbnailChapterAdapter extends BaseAdapter {
	public static String TAG = ThumbnailChapterAdapter.class.getName();
	
	private Context mContext;

	public List<Chapter> chapters = new ArrayList<Chapter>(40);

	private ImageLoader imageLoader;
	
	private Comparator<Chapter> mComparator; 
	
	private static DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());
	
    public ThumbnailChapterAdapter(Context c) {
        mContext = c;

        imageLoader = new ImageLoader(App.getInstance().mRequestQueue, new BitmapLruCache());

    	mComparator = new Comparator<Chapter>() {
    		public int compare(Chapter chap1, Chapter chap2) {
    	        return chap1.created_at.compareTo(chap2.created_at) * -1;
    	    }
    	};
    }

    public int getCount() {
        return chapters.size();
    }

    public Chapter getItem(int position) {
        return chapters.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public void addChapters(Collection<Chapter> list) {
    	chapters.addAll(list);
    	Collections.sort(chapters, mComparator);
    }

    // create a new TextView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
    	final ViewHolder holder;

        Chapter chapter = getItem(position);

        // if it's not recycled, initialize some attributes
    	if (convertView == null) {
    		convertView = LayoutInflater.from(mContext).inflate(R.layout.thumbnail_item, parent, false);
    		
    		holder = new ViewHolder();
    		holder.date = (TextView) convertView.findViewById(R.id.thumb_date);
            holder.title = (TextView) convertView.findViewById(R.id.thumb_title);
            holder.number = (TextView) convertView.findViewById(R.id.thumb_number);
    		holder.cover = (CustomNetworkImageView) convertView.findViewById(R.id.thumb_cover);
    		holder.cover.setErrorImageResId(R.drawable.empty_cover);

    		convertView.setTag(holder);
		} else {
		    holder = (ViewHolder) convertView.getTag();
		}


    	holder.date.setText(ThumbnailChapterAdapter.formatDate(chapter.created_at));
    	holder.title.setText(WordUtils.capitalize(chapter.manga.title));
    	holder.number.setText(String.valueOf(chapter.number));
        holder.cover.setImageUrl(chapter.manga.cover, ImageCacheManager.getInstance().getImageLoader(), new CoverListener(chapter.manga, holder.cover));

        return convertView;
    }
    
    static class ViewHolder {
    	TextView date;
        CustomNetworkImageView cover;
    	TextView title;
    	TextView number;
    }
    
    public static String formatDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);

        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            return App.getInstance().getResources().getString(R.string.date_today_label);
        } else if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)) {
            return App.getInstance().getResources().getString(R.string.date_yesterday_label);
        } else {
            return df.format(date);
        }
    }
}
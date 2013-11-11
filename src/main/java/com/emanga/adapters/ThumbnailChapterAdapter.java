package com.emanga.adapters;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.WordUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.emanga.R;
import com.emanga.models.Chapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class ThumbnailChapterAdapter extends BaseAdapter {
	public static String TAG = ThumbnailChapterAdapter.class.getName();
	
	private Context mContext;

	public List<Chapter> chapters = new LinkedList<Chapter>();
	
	private DisplayImageOptions options;
	private ImageLoader imageLoader;
	
	private Comparator<Chapter> mComparator; 
	
	private static DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT, new Locale("es"));
	
    public ThumbnailChapterAdapter(Context c) {
        mContext = c;
        
    	options = new DisplayImageOptions.Builder()
	    	.showImageForEmptyUri(R.drawable.ic_content_picture)
	    	.showImageOnFail(R.drawable.ic_content_remove)
	    	.cacheInMemory(true)
	    	.cacheOnDisc(true)
	    	.displayer(new RoundedBitmapDisplayer(5))
	    	.build();
    	
    	imageLoader = ImageLoader.getInstance();
    	
    	mComparator = new Comparator<Chapter>() {
    		public int compare(Chapter chap1, Chapter chap2) {
    	        return chap1.date.compareTo(chap2.date) * -1;
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
    
    public void setChapters(List<Chapter> list) {
    	chapters = list;
    	notifyDataSetChanged();
    }
    
    public void addChapters(List<Chapter> list) {
    	chapters = list;
    	Collections.sort(chapters, mComparator);
    	notifyDataSetChanged();
    }

    // create a new TextView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
    	final ViewHolder holder;
    	
        // if it's not recycled, initialize some attributes
    	if (convertView == null) {  
    		convertView = LayoutInflater.from(mContext).inflate(R.layout.thumbnail_item, parent, false);
    		
    		holder = new ViewHolder();
    		holder.date = (TextView) convertView.findViewById(R.id.thumb_date);
    		holder.cover = (ImageView) convertView.findViewById(R.id.thumb_cover);
    		holder.title = (TextView) convertView.findViewById(R.id.thumb_title);
    		holder.number = (TextView) convertView.findViewById(R.id.thumb_number);
    		
    		convertView.setTag(holder);
		} else {
		    holder = (ViewHolder) convertView.getTag();
		}	
    	
    	Chapter chapter = getItem(position);
    	
    	holder.date.setText(ThumbnailChapterAdapter.formatDate(chapter.date));
    	holder.title.setText(WordUtils.capitalize(chapter.manga.title));
    	holder.number.setText(String.valueOf(chapter.number));
    	
    	imageLoader.displayImage(chapter.manga.cover, holder.cover, options);    	
        return convertView;
    }
    
    public static class ViewHolder {
    	public TextView date;
    	public ImageView cover;
    	public TextView title;
    	public TextView number;
    }
    
    public static String formatDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);

        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            return "Hoy";
        } else if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)) {
            return "Ayer";
        } else {
            return df.format(date);
        }
    }
}
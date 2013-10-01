package com.emanga.adapters;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.emanga.R;
import com.emanga.models.Chapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class ThumbnailChapterAdapter extends BaseAdapter {
	public static String TAG = ThumbnailChapterAdapter.class.getName();
	
	private Context mContext;

	public List<Chapter> chapters = new ArrayList<Chapter>();
	
	private DisplayImageOptions options;
	private ImageLoader imageLoader;
	
	private static DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT, new Locale("es"));
	
    public ThumbnailChapterAdapter(Context c) {
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
    	chapters.addAll(list);
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
    		holder.progressbar = (ProgressBar) convertView.findViewById(R.id.thumb_progressbar);
    		holder.title = (TextView) convertView.findViewById(R.id.thumb_title);
    		holder.number = (TextView) convertView.findViewById(R.id.thumb_number);
    		
    		convertView.setTag(holder);
		} else {
		    holder = (ViewHolder) convertView.getTag();
		}	
    	
    	Chapter chapter = getItem(position);
    	
    	holder.date.setText(ThumbnailChapterAdapter.formatDate(chapter.date));
    	holder.title.setText(String.valueOf(chapter.manga.title));
    	holder.number.setText(String.valueOf(chapter.number));
    	
    	imageLoader.displayImage(chapter.manga.cover, holder.cover, options, new SimpleImageLoadingListener() {
    		@Override
    		public void onLoadingStarted(String imageUri, View view) {
    			holder.progressbar.setVisibility(View.VISIBLE);
    			holder.cover.setVisibility(View.GONE);
    		}

    		@Override
    		public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
    			String message = null;
    			switch (failReason.getType()) {
		    		case IO_ERROR:
		    			message = "Input/Output error";
		    		break;
		    		case DECODING_ERROR:
		    			message = "Image can't be decoded";
		    		break;
		    		case NETWORK_DENIED:
		    			message = "Downloads are denied";
		    		break;
		    		case OUT_OF_MEMORY:
		    			message = "Out Of Memory error";
		    		break;
		    		case UNKNOWN:
		    			message = "Unknown error";
		    		break;
    		}
    			Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();

    			holder.progressbar.setVisibility(View.GONE);
    		}

    		@Override
    		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
    			holder.progressbar.setVisibility(View.GONE);
    			holder.cover.setVisibility(View.VISIBLE);
    		}
    	});
    	
        return convertView;
    }
    
    class ViewHolder {
    	public TextView date;
    	public ProgressBar progressbar;
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
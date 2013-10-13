package com.emanga.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.emanga.R;
import com.emanga.models.Manga;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MangaItemListAdapter extends BaseAdapter {
    private Context mContext;

    public List<Manga> mangas = new ArrayList<Manga>();
    
    private DisplayImageOptions options;
	private ImageLoader imageLoader;
	
    public MangaItemListAdapter(Context c) {
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
        return mangas.size();
    }

    public Manga getItem(int position) {
        return mangas.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent){
    	ViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.manga_item_list, parent, false);

			holder = new ViewHolder();
			holder.cover = (ImageView) convertView.findViewById(R.id.manga_list_cover);
			holder.title = (TextView) convertView.findViewById(R.id.manga_list_title);
			// holder.description = (TextView) convertView.findViewById(R.id.carousel_description);

			convertView.setTag(holder);
		} else {
		    holder = (ViewHolder) convertView.getTag();
		}
		
		Manga m = getItem(position);
        
		holder.title.setText(m.title);
        // holder.description.setText(m.description);
        imageLoader.displayImage(m.cover, holder.cover, options);
		
        return convertView;
    }
    
    static class ViewHolder {
    	public ImageView cover;
    	public TextView title;
    	// public TextView description;
    }
}
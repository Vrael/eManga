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

public class CarouselMangaAdapter extends BaseAdapter {
    private Context mContext;

    public List<Manga> mangas = new ArrayList<Manga>();
    
    private DisplayImageOptions options;
	private ImageLoader imageLoader;
	
    public CarouselMangaAdapter(Context c) {
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
    
    public void setMangas(List<Manga> list) {
    	mangas = list;
    	notifyDataSetChanged();
    }
    
    public View getView(int position, View convertView, ViewGroup parent){
    	ViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.carousel_item, parent, false);

			holder = new ViewHolder();
			holder.cover = (ImageView) convertView.findViewById(R.id.carousel_cover);
			holder.title = (TextView) convertView.findViewById(R.id.carousel_title);
			holder.description = (TextView) convertView.findViewById(R.id.carousel_description);

			convertView.setTag(holder);
		} else {
		    holder = (ViewHolder) convertView.getTag();
		}
		
		Manga m = getItem(position);
        
		holder.title.setText(m.title);
		//TODO: Change to m.description
        holder.description.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
    			+ "Duis ut bibendum magna. Quisque tempus imperdiet lacus, ut tincidunt purus hendrerit ac. "
    			+ "Praesent adipiscing, nisl eu tincidunt pellentesque, orci ipsum iaculis nisi, "
    			+ "a scelerisque turpis massa in libero. Fusce et magna urna. Aliquam tristique viverra diam nec ornare. "
    			+ "Maecenas ornare enim eu est iaculis luctus. Praesent at imperdiet sapien. "
    			+ "Cras molestie ultricies neque sit amet cursus. Donec condimentum pretium porta. "
    			+ "Aliquam elementum dignissim tellus sit amet vulputate. Fusce nec mauris feugiat, sodales est sit amet, "
    			+ "congue mi. Vivamus iaculis molestie velit, sit amet dictum eros bibendum eu. "
    			+ "Nam libero nulla, mattis ut augue ut, porta dignissim lectus.");
        imageLoader.displayImage(m.cover, holder.cover, options);
		
        return convertView;
    }
    
    static class ViewHolder {
    	public ImageView cover;
    	public TextView title;
    	public TextView description;
    }
}
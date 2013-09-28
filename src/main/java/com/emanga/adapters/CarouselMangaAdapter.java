package com.emanga.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.emanga.models.Manga;
import com.emanga.views.CarouselItemView;

public class CarouselMangaAdapter extends BaseAdapter {
    private Context mContext;

    public List<Manga> mangas = new ArrayList<Manga>();
    
    public CarouselMangaAdapter(Context c) {
    	mContext = c;
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

    public View getView(int position, View convertView, ViewGroup parent) {
    	CarouselItemView itemView;
    	
    	if (convertView == null) {  
    		itemView = new CarouselItemView(mContext);
    	}
    	else {
    		itemView = (CarouselItemView) convertView;
    	}
    	Manga m = getItem(position);
    	 
    	itemView.title.setText(m.title);
    	itemView.setCover(m.cover);
    	itemView.description.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
    			+ "Duis ut bibendum magna. Quisque tempus imperdiet lacus, ut tincidunt purus hendrerit ac. "
    			+ "Praesent adipiscing, nisl eu tincidunt pellentesque, orci ipsum iaculis nisi, "
    			+ "a scelerisque turpis massa in libero. Fusce et magna urna. Aliquam tristique viverra diam nec ornare. "
    			+ "Maecenas ornare enim eu est iaculis luctus. Praesent at imperdiet sapien. "
    			+ "Cras molestie ultricies neque sit amet cursus. Donec condimentum pretium porta. "
    			+ "Aliquam elementum dignissim tellus sit amet vulputate. Fusce nec mauris feugiat, sodales est sit amet, "
    			+ "congue mi. Vivamus iaculis molestie velit, sit amet dictum eros bibendum eu. "
    			+ "Nam libero nulla, mattis ut augue ut, porta dignissim lectus.");
     
        return itemView;
    }
}
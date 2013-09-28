package com.emanga.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.emanga.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CarouselItemView extends ScrollView{
	private DisplayImageOptions options;
	private ImageLoader imageLoader;
	private Context mContext;
	
	private ImageView cover;
	public TextView title;
	public TextView description;
	
	public CarouselItemView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public void setCover(String url){
		imageLoader.displayImage(url, cover, options);
	}
	
	private void init() {
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        
        LinearLayout layout = new LinearLayout(mContext);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);
        
        addView(layout);
        
        options = new DisplayImageOptions.Builder()
    	.showImageForEmptyUri(R.drawable.ic_content_picture)
    	.showImageOnFail(R.drawable.ic_content_remove)
    	.cacheInMemory(true)
    	.cacheOnDisc(true)
    	.bitmapConfig(Bitmap.Config.RGB_565)
    	.build();
        
        imageLoader = ImageLoader.getInstance();
        
        cover = new ImageView(getContext());
        title = new TextView(getContext());
        description = new TextView(getContext());
        
        
        LinearLayout.LayoutParams paramsTitle = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT
				);
        
        paramsTitle.setMargins(8, 18, 8, 8);
        title.setLayoutParams(paramsTitle);
        title.setGravity(Gravity.CENTER);
        
        cover.setLayoutParams(new LinearLayout.LayoutParams(
				(int) mContext.getResources().getDimension(R.dimen.carousel_cover_width),
				(int) mContext.getResources().getDimension(R.dimen.carousel_cover_height)
				));
        
        LinearLayout.LayoutParams paramsDescription = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT
				);
        
        paramsDescription.setMargins(14, 20, 18, 10);
        
        description.setLayoutParams(paramsDescription);
        
        layout.addView(cover);
        layout.addView(title);
        layout.addView(description);
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
		ViewParent viewParent = getParent();
		if(viewParent != null) {
			viewParent.requestDisallowInterceptTouchEvent(false);
		}
		
        return super.onTouchEvent(event);
    }
	
	@Override
    public boolean canScrollVertically(int direction){
    	return true;
    }
}

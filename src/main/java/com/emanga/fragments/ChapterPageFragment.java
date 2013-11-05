package com.emanga.fragments;

import uk.co.senab.photoview.PhotoViewAttacher;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;

import com.emanga.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

/**
 * Fragment with an ImageView that is a manga chapter page
 */
public class ChapterPageFragment extends Fragment {
    private static final String URL_PAGE_IMAGE = "link";
    
    private DisplayImageOptions options;
    private ImageLoader imageLoader;
    private ImageView mImageView;    
    private String mUrl;

    public static ChapterPageFragment newInstance(String pageLink) {
        final ChapterPageFragment f = new ChapterPageFragment();
        final Bundle args = new Bundle();
        args.putString(URL_PAGE_IMAGE, pageLink);
        f.setArguments(args);
        return f;
    }

    // Empty constructor, required as per Fragment docs
    public ChapterPageFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        options = new DisplayImageOptions.Builder()
    	.showImageForEmptyUri(R.drawable.ic_content_picture)
    	.showImageOnFail(R.drawable.ic_content_remove)
    	.cacheInMemory(true)
    	.cacheOnDisc(true)
    	.bitmapConfig(Bitmap.Config.RGB_565)
    	.build();
        
        imageLoader = ImageLoader.getInstance();
	
        mUrl = getArguments() != null ? getArguments().getString(URL_PAGE_IMAGE) : null;
    }
    
    private PhotoViewAttacher mAttacher;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	ScrollView mScroll = new ScrollView(getActivity());
    	mScroll.setLayoutParams(new LayoutParams(
        		LayoutParams.MATCH_PARENT,
        		LayoutParams.MATCH_PARENT));
    	
    	mScroll.setVerticalScrollBarEnabled(false);
    	mScroll.setHorizontalScrollBarEnabled(false);
    	
    	mImageView = new ImageView(getActivity());
        mImageView.setLayoutParams(new LayoutParams(
        		LayoutParams.MATCH_PARENT,
        		LayoutParams.WRAP_CONTENT));
        
        mScroll.addView(mImageView);
        return mScroll;
    }
    
    public void onActivityCreated (Bundle savedInstanceState){
    	super.onActivityCreated(savedInstanceState);

    	// Load image into ImageView
        imageLoader.displayImage(mUrl, mImageView, options, new ImageLoadingListener() {

			public void onLoadingStarted(String imageUri, View view) {
				// TODO Auto-generated method stub
				
			}

			public void onLoadingFailed(String imageUri, View view,
					FailReason failReason) {
				// TODO Auto-generated method stub
				
			}

			public void onLoadingComplete(String imageUri, View view,
					Bitmap loadedImage) {

		        // Resize the image view to full with screen
				mImageView.setImageBitmap(adjustWith(mImageView));
				// Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
		        mAttacher = new PhotoViewAttacher(mImageView);
			}

			public void onLoadingCancelled(String imageUri, View view) {
				// TODO Auto-generated method stub
				
			}
        	
        });
        
    }
  
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mAttacher != null){
	        // Need to call clean-up
	        mAttacher.cleanup();
        }
    }
    
    private Bitmap adjustWith(ImageView image){
    	Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
    	// Get scaling factor to fit the max possible width of the ImageView
        float scalingFactor = getBitmapScalingFactor(bitmap);

        // Create a new bitmap with the scaling factor
        return ChapterPageFragment.ScaleBitmap(bitmap, scalingFactor);
    }
    
    private float getBitmapScalingFactor(Bitmap bm) {
        // Get display width from device
    	Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        
        // Get margin to use it for calculating to max width of the ImageView
        FrameLayout.LayoutParams layoutParams = 
            (FrameLayout.LayoutParams) mImageView.getLayoutParams();
        int leftMargin = layoutParams.leftMargin;
        int rightMargin = layoutParams.rightMargin;

        // Calculate the max width of the imageView
        int imageViewWidth = size.x - (leftMargin + rightMargin);

        // Calculate scaling factor and return it
        return ( (float) imageViewWidth / (float) bm.getWidth() );
    }
    
    public static Bitmap ScaleBitmap(Bitmap bm, float scalingFactor) {
        int scaleHeight = (int) (bm.getHeight() * scalingFactor);
        int scaleWidth = (int) (bm.getWidth() * scalingFactor);
        
        scaleWidth = scaleWidth > 2048? 2048 : scaleWidth;
        scaleHeight = scaleHeight > 2048? 2048 : scaleHeight;

        return Bitmap.createScaledBitmap(bm, scaleWidth, scaleHeight, true);
    }
}
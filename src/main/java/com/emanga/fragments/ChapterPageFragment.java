package com.emanga.fragments;

import uk.co.senab.photoview.PhotoViewAttacher;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
    private ScrollView mScrollView;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
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
    	
    	View view = inflater.inflate(R.layout.page_chapter, container, false);
    	mScrollView = (ScrollView) view.findViewById(R.id.reader_scroll_image);
    	mImageView = (ImageView) mScrollView.findViewById(R.id.reader_page_image);
    	mProgressBar = (ProgressBar) view.findViewById(R.id.reader_progressbar);
        return view;
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
				// Hide progress bar
				mProgressBar.setVisibility(View.GONE);

				// Show message with the error
				LayoutInflater inflater = (LayoutInflater) getActivity().getLayoutInflater();
            	View viewToast = inflater.inflate(R.layout.toast_with_image, (ViewGroup) getActivity().findViewById(R.id.toast));
            	TextView message = (TextView) viewToast.findViewById(R.id.toast_text);
            	message.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.error, 0, 0);
            	message.setText("Could not download the page!\nTry to check the internet conexion.");
            	
            	Toast toast = new Toast(getActivity());
            	toast.setDuration(Toast.LENGTH_LONG);
            	toast.setView(viewToast);
            	toast.show();
				
			}

			public void onLoadingComplete(String imageUri, View view,
					Bitmap loadedImage) {
				// Swap visibility
				mProgressBar.setVisibility(View.GONE);
				mScrollView.setVisibility(View.VISIBLE);
				
				if(loadedImage != null && mImageView != null){
			        // Resize the image view to full with screen
					mImageView.setImageBitmap(adjustWith(loadedImage));
				}
			}

			public void onLoadingCancelled(String imageUri, View view) {
				
			}
        	
        });
        
    }
    
    public void onStart(){
    	super.onStart();
    	//Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
        mAttacher = new PhotoViewAttacher(mImageView);
    }
    
    public void onStop(){
    	super.onStop();
    	mAttacher.cleanup();
    }
    
    private Bitmap adjustWith(Bitmap bitmap){
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
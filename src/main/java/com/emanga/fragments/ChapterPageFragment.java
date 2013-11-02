package com.emanga.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.emanga.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	// A linear layout with a Image View
    	final LinearLayout linearLayout = new LinearLayout(getActivity());
    	linearLayout.setOrientation(LinearLayout.VERTICAL);

        linearLayout.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        mImageView = new ImageView(getActivity());
        mImageView.setLayoutParams(new LayoutParams(
        		LayoutParams.MATCH_PARENT,
        		LayoutParams.MATCH_PARENT));
        
        linearLayout.addView(mImageView);
        
        return linearLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Load image into ImageView
        imageLoader.displayImage(mUrl, mImageView, options);
    }
}
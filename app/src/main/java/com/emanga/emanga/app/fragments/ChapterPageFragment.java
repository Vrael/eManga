package com.emanga.emanga.app.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.emanga.emanga.app.R;
import com.emanga.emanga.app.activities.ReaderActivity;
import com.emanga.emanga.app.cache.ImageCacheManager;
import com.emanga.emanga.app.database.OrmliteFragment;
import com.emanga.emanga.app.listeners.PageListener;
import com.emanga.emanga.app.models.Page;

import uk.co.senab.photoview.PhotoViewAttacher;


// import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Fragment with an ImageView that is a manga chapter page
 */
public class ChapterPageFragment extends OrmliteFragment {
    public static final String TAG = ChapterPageFragment.class.getSimpleName();
    private static final String PAGE = "link";

    private PhotoViewAttacher mAttacher;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private Page mPage;

    public static ChapterPageFragment newInstance(Page page) {
        final ChapterPageFragment f = new ChapterPageFragment();
        final Bundle args = new Bundle();
        args.putParcelable(PAGE, page);
        f.setArguments(args);
        return f;
    }

    // Empty constructor, required as per Fragment docs
    public ChapterPageFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments() != null ? (Page) getArguments().getParcelable(PAGE) : null;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.page_chapter, container, false);
        mProgressBar = (ProgressBar) view.findViewById(R.id.reader_progressbar);
        mImageView = (ImageView) view.findViewById(R.id.reader_page_image);
        Log.d(TAG, "Page url: " + mPage.url);

        ImageCacheManager.getInstance().getImageLoader().get(mPage.url,
                new PageListener(mPage, mImageView, mProgressBar,
                        (ReaderActivity) getActivity()));

        return view;
    }

    public void onStart(){
        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setZoomable(true);
        mAttacher.setScaleType(ImageView.ScaleType.CENTER_CROP);

        super.onStart();
    }

    public void onResume(){
        if(mAttacher != null)
            mAttacher.update();

        super.onStart();
    }

    public void onPause(){
        if (mAttacher != null)
            mAttacher.cleanup();

        super.onPause();
    }
}
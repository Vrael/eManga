package com.emanga.emanga.app.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.emanga.emanga.app.R;
import com.emanga.emanga.app.cache.ImageCacheManager;
import com.emanga.emanga.app.controllers.App;
import com.emanga.emanga.app.database.OrmliteFragment;
import com.emanga.emanga.app.models.Page;
import com.emanga.emanga.app.requests.PageRequest;
import com.emanga.emanga.app.utils.Internet;

// import uk.co.senab.photoview.PhotoViewAttacher;
import com.emanga.emanga.app.utils.PhotoViewAttacher;

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
    private Activity mActivity;

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
        mActivity = getActivity();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.page_chapter, container, false);
        mProgressBar = (ProgressBar) view.findViewById(R.id.reader_progressbar);
        mImageView = (ImageView) view.findViewById(R.id.reader_page_image);
        Log.d(TAG, "Page url: " + mPage.url);
        return view;
    }

    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        class CustomImageListener implements ImageLoader.ImageListener {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    mImageView.setImageBitmap(response.getBitmap());
                    mAttacher = new PhotoViewAttacher(mImageView);
                    mAttacher.setZoomable(true);
                    mAttacher.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    mProgressBar.setVisibility(View.GONE);
                    mImageView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                final CustomImageListener customImageListener = this;
                Log.d(TAG, "Ask new page to: " + Internet.HOST + "manga/" + mPage.chapter.manga._id
                        + "/chapter/" + mPage.chapter._id
                        + "/page/" + mPage._id
                        + "?urls[]=" + mPage.url);
                PageRequest pageRequest = new PageRequest(
                        Request.Method.GET,
                        Internet.HOST + "manga/" + mPage.chapter.manga._id
                                + "/chapter/" + mPage.chapter._id
                                + "/page/" + mPage._id
                                + "?urls[]=" + mPage.url,
                        new Response.Listener<Page>() {
                            @Override
                            public void onResponse(Page page) {
                                if(page.url != null) {
                                    mPage.url = page.url;
                                    Log.e(TAG, "New page " + mPage.number + " with url: " + mPage.url);
                                    ImageCacheManager.getInstance().getImageLoader().get(mPage.url, customImageListener);
                                    getHelper().getPageRunDao().update(mPage);
                                }
                                else {
                                    Log.e(TAG, "There aren't url alternatives for: " + mPage.number);
                                    mProgressBar.setVisibility(View.GONE);
                                    mImageView.setVisibility(View.VISIBLE);
                                    mImageView.setScaleType(ImageView.ScaleType.CENTER);
                                    mImageView.setImageResource(R.drawable.error);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, "Error in the request for get another url for the page: " + mPage._id);
                                mProgressBar.setVisibility(View.GONE);
                                mImageView.setVisibility(View.VISIBLE);
                                mImageView.setScaleType(ImageView.ScaleType.CENTER);
                                mImageView.setImageResource(R.drawable.error);
                            }
                        }
                );
                App.getInstance().mRequestQueue.add(pageRequest);
            }
        }

        ImageCacheManager.getInstance().getImageLoader().get(mPage.url, new CustomImageListener());
    }

    public void onResume(){
        super.onStart();
        if(mAttacher != null)
            mAttacher.update();
    }

    public void onPause(){
        super.onPause();
        if (mAttacher != null)
            mAttacher.cleanup();
    }
}
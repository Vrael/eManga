package com.emanga.emanga.app.listeners;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.emanga.emanga.app.R;
import com.emanga.emanga.app.cache.ImageCacheManager;
import com.emanga.emanga.app.controllers.App;
import com.emanga.emanga.app.database.DatabaseHelper;
import com.emanga.emanga.app.models.Page;
import com.emanga.emanga.app.requests.PageRequest;
import com.emanga.emanga.app.utils.Internet;
import com.emanga.emanga.app.utils.PhotoViewAttacher;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.util.HashSet;

/**
 * Created by Ciro on 12/05/2014.
 */
public class PageListener implements ImageLoader.ImageListener {
    private String TAG = CoverListener.class.getSimpleName();

    private Page mPage;

    private PhotoViewAttacher mAttacher;
    private ImageView mImageView;
    private ProgressBar mProgressBar;

    private HashSet<String> mUrlError;
    private int mRetries = 3;

    public PageListener(Page page, ImageView imageView, PhotoViewAttacher attacher, ProgressBar progressBar){
        mPage = page;
        mImageView = imageView;
        mAttacher = attacher;
        mProgressBar = progressBar;
    }

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
        final PageListener listenerReference = this;

        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                if (mUrlError == null) {
                    mUrlError = new HashSet<String>(3);
                }

                mUrlError.add(mPage.url);

                if(mRetries > 0){
                    Log.d(TAG, "Page: " + mPage.url + "\nAsk to: " + Internet.HOST + "manga/" + mPage.chapter.manga._id
                            + "/chapter/" + mPage.chapter._id
                            + "/page/" + mPage._id + "?" + Internet.arrayParams(mUrlError, "urls"));

                    App.getInstance().addToRequestQueue(
                            new PageRequest(
                                    Request.Method.GET,
                                    Internet.HOST + "manga/" + mPage.chapter.manga._id
                                            + "/chapter/" + mPage.chapter._id
                                            + "/page/" + mPage._id
                                            + "?" + Internet.arrayParams(mUrlError, "urls"),
                                    new Response.Listener<Page>() {
                                        @Override
                                        public void onResponse(Page response) {
                                            Log.d(TAG, response.toString());
                                            if(response.url != null) {
                                                mPage.url = response.url;
                                                Log.e(TAG, "New page " + response.number + " with url: " + response.url);

                                                // Reload image
                                                ImageCacheManager.getInstance().getImageLoader().get(mPage.url,
                                                        listenerReference);

                                                // Update database
                                                DatabaseHelper dbs = OpenHelperManager.getHelper(
                                                        App.getInstance().getApplicationContext(),
                                                        DatabaseHelper.class);
                                                dbs.getPageRunDao().update(mPage);
                                                OpenHelperManager.releaseHelper();
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
                                    null
                            )
                            ,"New Page"
                    );
                    mRetries--;
                } else {
                    Log.d(TAG, "Reached maximum intents number for ask a new page with: " + mPage.url);
                }
                return null;
            }

        }.execute();
    }
}

package com.emanga.emanga.app.listeners;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.RequestFuture;
import com.emanga.emanga.app.R;
import com.emanga.emanga.app.activities.ReaderActivity;
import com.emanga.emanga.app.cache.ImageCacheManager;
import com.emanga.emanga.app.controllers.App;
import com.emanga.emanga.app.database.DatabaseHelper;
import com.emanga.emanga.app.models.Page;
import com.emanga.emanga.app.requests.PageRequest;
import com.emanga.emanga.app.utils.Internet;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Ciro on 12/05/2014.
 */
public class PageListener implements ImageLoader.ImageListener {
    private String TAG = PageListener.class.getSimpleName();

    private Page mPage;

    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private TextView mTextView;

    private ReaderActivity mActivity;

    private HashSet<String> mUrlError;
    private int mRetries = 3;
    public PageRequest mRetryRequest;

    public PageListener(Page page, ImageView imageView,
                        ProgressBar progressBar, TextView textView,
                        ReaderActivity activity){
        mPage = page;
        mImageView = imageView;
        mProgressBar = progressBar;
        mTextView = textView;
        mActivity = activity;
    }

    @Override
    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
        if (response.getBitmap() != null) {
            mImageView.setImageBitmap(response.getBitmap());
            mProgressBar.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        final PageListener listenerRef = this;

        new AsyncTask<Void,Void,Page>() {
            @Override
            protected Page doInBackground(Void... voids) {

                if (mUrlError == null) {
                    mUrlError = new HashSet<String>(3);
                }

                mUrlError.add(mPage.url);

                if (mRetries > 0) {
                    mRetries--;

                    // Sync request
                    RequestFuture<Page> future = RequestFuture.newFuture();
                    mRetryRequest = new PageRequest(
                            Request.Method.GET,
                            Internet.HOST + "manga/" + mPage.chapter.manga._id
                                    + "/chapter/" + mPage.chapter._id
                                    + "/page/" + mPage._id
                                    + "?" + Internet.arrayParams(mUrlError, "urls"),
                            future,
                            future
                    );

                    App.getInstance().addToRequestQueue(mRetryRequest, "Ask new url for the page");

                    try {
                        Page response = future.get(30L, TimeUnit.SECONDS); // this will block
                        if (response.url != null) {
                            mPage.url = response.url;
                            Log.e(TAG, "New page " + response.number + " with url: " + response.url);

                            return response;
                        } else {
                            Log.e(TAG, "There aren't url alternatives for: " + mPage.number);
                            return null;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.d(TAG, "Reached maximum intents number for ask a new page with: " + mPage.url
                            + "\nRemoving this page from adapter");
                    return null;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Page result) {
                if(result != null){
                    // Reload image
                    ImageCacheManager.getInstance().getImageLoader().get(mPage.url, listenerRef);
                } else {
                    errorMessage(listenerRef.mActivity.getResources()
                            .getString(R.string.message_page_not_available));
                }
            }

        }.execute();
    }

    private void errorMessage(String message){
        mProgressBar.setVisibility(View.GONE);
        mTextView.setText(message);
        mTextView.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.noooo,0,0);
        mTextView.setCompoundDrawablePadding(20);
        mTextView.setVisibility(View.VISIBLE);
    }

    public void cancelRequest(){
        if(mRetryRequest != null){
            mRetryRequest.cancel();
        }
    }

}

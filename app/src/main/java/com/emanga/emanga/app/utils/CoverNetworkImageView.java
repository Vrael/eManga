package com.emanga.emanga.app.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonRequest;
import com.emanga.emanga.app.controllers.App;
import com.emanga.emanga.app.database.DatabaseHelper;
import com.emanga.emanga.app.models.Manga;
import com.emanga.emanga.app.requests.MangaRequest;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.HashSet;

/**
 * Created by Ciro on 06/05/2014.
 */
public class CoverNetworkImageView extends CustomNetworkImageView {
    public static final String TAG = CoverNetworkImageView.class.getSimpleName();
    private int mRetries = 3;
    private Manga mManga;
    private HashSet<String> mUrlError;

    public CoverNetworkImageView(Context context) {
        super(context);
    }
    public CoverNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public CoverNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    private void searchMangaByCover(String cover){
        DatabaseHelper dbs = OpenHelperManager.getHelper(
                App.getInstance().getApplicationContext(),
                DatabaseHelper.class);
        try {
            mManga = dbs.getMangaRunDao().queryBuilder().where().eq(Manga.COVER_COLUMN_NAME, cover)
                    .queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            OpenHelperManager.releaseHelper();
        }
    }

    public void setImageUrl(String url, ImageLoader imageLoader) {
        searchMangaByCover(url);

        if(mManga != null){
            super.setImageUrl(mManga.cover, imageLoader, mListener);
        }
    }

    public void setImageUrl(String url, ImageLoader imageLoader, final ImageLoader.ImageListener listener) {
        searchMangaByCover(url);

        if(mManga != null){
            super.setImageUrl(mManga.cover,imageLoader,new ImageLoader.ImageListener(){

                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    mListener.onResponse(response,isImmediate);
                    listener.onResponse(response,true);
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    mListener.onErrorResponse(error);
                    listener.onErrorResponse(error);
                }
            });
        }
    }

    public void setImageUrl(Manga manga, ImageLoader imageLoader) {
        mManga = manga;
        super.setImageUrl(manga.cover,imageLoader,mListener);
    }

    public void setImageUrl(Manga manga, ImageLoader imageLoader, final ImageLoader.ImageListener listener) {
        mManga = manga;
        super.setImageUrl(manga.cover,imageLoader,new ImageLoader.ImageListener(){

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                mListener.onResponse(response,isImmediate);
                listener.onResponse(response,true);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                mListener.onErrorResponse(error);
                listener.onErrorResponse(error);
            }
        });
    }

    void loadImageIfNecessary(final boolean isInLayoutPass, final ImageLoader.ImageListener listener) {
        super.loadImageIfNecessary(false, listener);
    }

    protected ImageLoader.ImageListener mListener = new ImageLoader.ImageListener() {
        @Override
        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
            // If there was an error as it saves the new url that works
            if(mUrlError != null){
                DatabaseHelper dbs = OpenHelperManager.getHelper(
                        App.getInstance().getApplicationContext(),
                        DatabaseHelper.class);

                Log.d(TAG, "Updating: " + mManga.title + " " + mManga.cover);
                dbs.getMangaRunDao().update(mManga);
                OpenHelperManager.releaseHelper();
            } else {
                Log.d(TAG, "mUrlError IS EMPTY");
            }
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if(mUrlError == null){
                mUrlError = new HashSet<String>(3);
            }

            if(mManga.cover != null)
                mUrlError.add(mManga.cover);
            else
                Log.d(TAG, "Manga with null cover " + mManga._id);

            if(mRetries > 0) {
                Log.d(TAG, "Cover: " + mUrl + " not found, asking by a new");
                Log.d(TAG, "Ask to: " + Internet.HOST + "manga/" + mManga._id + "/cover?" + Internet.arrayParams(mUrlError,"e"));
                MangaRequest newCover = new MangaRequest(
                        JsonRequest.Method.GET,
                        Internet.HOST + "manga/" + mManga._id + "/cover?" + Internet.arrayParams(mUrlError,"e"),
                        new Response.Listener<Manga>() {
                            @Override
                            public void onResponse(Manga response) {
                                Log.d(TAG, response.toString());
                                if (response.cover != null) {
                                    Log.d(TAG, "New cover received: " + response.cover);
                                    mManga.cover = response.cover;
                                    mUrl = response.cover;

                                    // Reload image
                                    setImageUrl(mUrl,mImageLoader,mListener);
                                } else {
                                    Log.d(TAG, "There aren't new covers for: " + mManga._id + " " + mManga.title);
                                }
                            }
                        },
                        null
                );
                newCover.setTag("Request: New cover");
                App.getInstance().mRequestQueue.add(newCover);
                mRetries--;
            } else {
                Log.d(TAG, "Reached maximum intents number for ask a new cover with: " + mUrl);
            }
        }
    };
}

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

/**
 * Handles fetching an image from a URL as well as the life-cycle of the
 * associated request.
 */
public class CoverNetworkImageView extends CustomNetworkImageView {
    public static final String TAG = CoverNetworkImageView.class.getSimpleName();

    private static final DatabaseHelper dbs = OpenHelperManager.getHelper(
                    App.getInstance().getApplicationContext(),
                    DatabaseHelper.class);

    public CoverNetworkImageView(Context context) {
        super(context);
    }

    public CoverNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CoverNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setImageUrl(String url, ImageLoader imageLoader, ImageLoader.ImageListener listener) {
        throw new UnsupportedOperationException("You should use: setImageUrl(Manga manga, ImageLoader imageLoader) instead of this");
    }

    public void setImageUrl(String url, ImageLoader imageLoader) {
        Manga manga = null;
        try {
            manga = dbs.getMangaRunDao().queryBuilder().where().eq(Manga.COVER_COLUMN_NAME, url)
                    .queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(manga != null){
            setImageUrl(manga, imageLoader);
        }
    }

    private void invokeSetImageUrlParent(String url, ImageLoader imageLoader){
        super.setImageUrl(url,imageLoader);
    }

    public void setImageUrl(final Manga manga, final ImageLoader imageLoader, final ImageLoader.ImageListener listener) {
        Log.d(TAG, "Display cover: " + manga.cover);
        final CoverNetworkImageView networkImage = this;
        super.setImageUrl(manga.cover, imageLoader, new ImageLoader.ImageListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Ask new cover instead of: " + manga.cover);
                MangaRequest newCover = new MangaRequest(
                        JsonRequest.Method.GET,
                        Internet.HOST + "manga/" + manga._id + "/cover?covers[]=" + manga.cover,
                        new Response.Listener<Manga>() {
                            @Override
                            public void onResponse(Manga response) {
                                Log.d(TAG, response.toString());
                                if (response.cover != null) {
                                    Log.d(TAG, "New cover received: " + response.cover);
                                    manga.cover = response.cover;
                                    // Update the manga with the new cover
                                    dbs.getMangaRunDao().update(manga);

                                    // Try to load the image again
                                    invokeSetImageUrlParent(manga.cover, imageLoader);
                                } else {
                                    Log.d(TAG, "There aren't new covers for: " + manga._id + " " + manga.title);
                                }
                            }
                        },
                        null
                );

                newCover.setTag("Request: New cover");
                App.getInstance().mRequestQueue.add(newCover);

                if(listener != null){
                    listener.onErrorResponse(error);
                }
            }

            @Override
            public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                if(listener != null){
                    listener.onResponse(response,isImmediate);
                }
            }
        });
    }

    public void setImageUrl(final Manga manga, final ImageLoader imageLoader) {
        setImageUrl(manga,imageLoader,null);
    }
}
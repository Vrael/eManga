package com.emanga.emanga.app.listeners;

import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonRequest;
import com.emanga.emanga.app.cache.ImageCacheManager;
import com.emanga.emanga.app.controllers.App;
import com.emanga.emanga.app.database.DatabaseHelper;
import com.emanga.emanga.app.models.Manga;
import com.emanga.emanga.app.requests.MangaRequest;
import com.emanga.emanga.app.utils.Internet;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.HashSet;

/**
 * Created by Ciro on 3/03/14.
 */
public class CoverListener implements ImageLoader.ImageListener {
    private String TAG = CoverListener.class.getSimpleName();

    private Manga mManga;
    private HashSet<String> mUrlError;
    private int mRetries = 3;
    private ImageView mImageView;
    private String url;

    public CoverListener(Manga manga, ImageView imageView){
        mManga = manga;
        mImageView = imageView;
    }

    public CoverListener(Manga manga, ImageView imageView, int retries){
        this(manga,imageView);
        mRetries = retries;
    }

    public CoverListener(String url, ImageView imageView){
        this(searchMangaByCover(url),imageView);
    }

    private static Manga searchMangaByCover(String cover){

        DatabaseHelper dbs = OpenHelperManager.getHelper(
                App.getInstance().getApplicationContext(),
                DatabaseHelper.class);
        Manga manga = null;
        try {
            manga = dbs.getMangaRunDao().queryBuilder().where().eq(Manga.COVER_COLUMN_NAME, cover)
                    .queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            OpenHelperManager.releaseHelper();
        }

        return manga;
    }

    @Override
    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if(mUrlError == null){
            mUrlError = new HashSet<String>(3);
        }

        mUrlError.add(mManga.cover);

        if(mRetries > 0) {
            Log.d(TAG, "Cover: " + mManga.cover + "\nAsk to: " + Internet.HOST + "manga/" + mManga._id + "/cover?" + Internet.arrayParams(mUrlError,"e"));
            App.getInstance().addToRequestQueue(
                    new MangaRequest(
                            JsonRequest.Method.GET,
                            Internet.HOST + "manga/" + mManga._id + "/cover?" + Internet.arrayParams(mUrlError,"e"),
                            new Response.Listener<Manga>() {
                                @Override
                                public void onResponse(Manga response) {
                                    Log.d(TAG, response.toString());
                                    if (response.cover != null) {
                                        Log.d(TAG, "New cover received: " + response.title + " " + response.cover);
                                        mManga.cover = response.cover;

                                        // Reload image
                                        ImageCacheManager.getInstance().getImage(mManga.cover, mImageView, new CoverListener(mManga,mImageView,--mRetries));
                                        DatabaseHelper dbs = OpenHelperManager.getHelper(
                                                App.getInstance().getApplicationContext(),
                                                DatabaseHelper.class);
                                        dbs.getMangaRunDao().update(mManga);
                                        OpenHelperManager.releaseHelper();
                                    } else {
                                        Log.d(TAG, "There aren't new covers for: "  + mManga.title);
                                    }
                                }
                            },
                            null
                    ),
                    "New Cover");
            mRetries--;
        } else {
            Log.d(TAG, "Reached maximum intents number for ask a new cover with: " + mManga.cover);
        }
    }
}

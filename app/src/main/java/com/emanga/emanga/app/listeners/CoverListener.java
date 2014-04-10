package com.emanga.emanga.app.listeners;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonRequest;
import com.emanga.emanga.app.controllers.App;
import com.emanga.emanga.app.database.DatabaseHelper;
import com.emanga.emanga.app.loaders.LatestChaptersLoader;
import com.emanga.emanga.app.models.Manga;
import com.emanga.emanga.app.requests.MangasRequest;
import com.emanga.emanga.app.utils.Internet;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import com.emanga.emanga.app.R;

/**
 * Created by Ciro on 3/03/14.
 */
public class CoverListener implements ImageLoader.ImageListener {
    private String TAG = CoverListener.class.getSimpleName();

    private ImageView cover;
    private Manga manga;
    private Context ctx;

    public CoverListener(ImageView cover, Manga manga){
        this.cover = cover;
        this.manga = manga;
        this.ctx = cover.getContext();
    }

    @Override
    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
        if (response.getBitmap() != null) {
            cover.setImageBitmap(response.getBitmap());
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e(TAG, "It was an error with a cover!");

        MangasRequest newCover = new MangasRequest(
                JsonRequest.Method.GET,
                Internet.HOST + "manga/" + manga._id + "/cover?e[]=" + manga.cover,
                new Response.Listener<Manga[]>() {
                    @Override
                    public void onResponse(Manga[] response) {
                        if(response.length > 0){
                            Log.d(TAG, response.toString());
                            new AsyncTask<Manga, Void, Void>() {
                                @Override
                                protected Void doInBackground(Manga... mangas) {
                                    DatabaseHelper dbs =
                                            OpenHelperManager.getHelper(
                                                    App.getInstance().getApplicationContext(),
                                                    DatabaseHelper.class);
                                    Log.d(TAG, "New cover recived: " + mangas[0].cover);
                                    manga.cover = mangas[0].cover;
                                    dbs.getMangaRunDao().update(manga);
                                    OpenHelperManager.releaseHelper();
                                    ctx.sendBroadcast(new Intent(LatestChaptersLoader.ACTION_RELOAD));
                                    return null;
                                }
                            }.execute(response[0]);
                        } else {
                            Log.d(TAG, "There aren't any new cover for: " + manga._id + " " + manga.title);
                            cover.setImageResource(R.drawable.enjoy_reading);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.d(TAG, "Error in response!");
                        cover.setImageResource(R.drawable.enjoy_reading);
                    }
                }
        );

        newCover.setRetryPolicy(new DefaultRetryPolicy(
                3 * 60 * 1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );

        newCover.setTag("Request: Latest Chapters");
        App.getInstance().mRequestQueue.add(newCover);
    }
}

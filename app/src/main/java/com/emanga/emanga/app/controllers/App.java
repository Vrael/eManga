package com.emanga.emanga.app.controllers;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.emanga.emanga.app.cache.ImageCacheManager;
import com.emanga.emanga.app.utils.SmartRequestQueue;
import com.emanga.emanga.app.utils.Volley;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

public class App extends Application {
	private static String TAG = App.class.getName();

    public static final String PREFS_NAME = "MangappPrefs";
    public UUID userId;

    private static App sInstance;

    public SmartRequestQueue mRequestQueue;
    public ObjectMapper mMapper;

    private static int DISK_IMAGECACHE_SIZE = 1024*1024*50;
    private static Bitmap.CompressFormat DISK_IMAGECACHE_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;
    private static int DISK_IMAGECACHE_QUALITY = 100; //PNG is lossless so quality is ignored but must be provided

    private BroadcastReceiver connectivity;

    @Override
	public void onCreate() {

		super.onCreate();
		Log.d(TAG, "Init eManga app");

        sInstance = this;
        mRequestQueue = (SmartRequestQueue) Volley.newRequestQueue(this);
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                mRequestQueue.stopNetworkRequest();
                mMapper = new ObjectMapper();
                mRequestQueue.startNetworkRequest();
                return null;
            }

        }.execute();

        ImageCacheManager.getInstance().init(this,
                this.getPackageCodePath()
                , DISK_IMAGECACHE_SIZE
                , DISK_IMAGECACHE_COMPRESS_FORMAT
                , DISK_IMAGECACHE_QUALITY
                , ImageCacheManager.CacheType.MEMORY);

        connectivity = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)){
                    Log.d(TAG, "Lost internet connection. RequestQueue stopped");
                    mRequestQueue.stopNetworkRequest();
                } else {
                    Log.d(TAG, "Internet connection works. RequestQueue restarted");
                    mRequestQueue.startNetworkRequest();
                }
            }
        };

        registerReceiver(
                connectivity,
                new IntentFilter(
                        ConnectivityManager.CONNECTIVITY_ACTION)
        );

        initUserId();
	}

    private void initUserId(){
        UUID id = checkUserId();
        if(id == null){
            userId = createUserId();
        } else {
            userId = id;
        }
    }

    private UUID checkUserId(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String id = settings.getString("userId",null);
        if(id != null){
            return UUID.fromString(id);
        }
        return null;
    }

    private UUID createUserId(){
        UUID id = UUID.randomUUID();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("userId", id.toString());
        editor.commit();
        return id;
    }
    /**
     * Adds the specified request to the global queue, if tag is specified
     * then it is used else Default TAG is used.
     *
     * @param req request
     * @param tag label for the request
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        mRequestQueue.add(req);
    }

    public static synchronized App getInstance() {
        return sInstance;
    }
}

package com.emanga.emanga.app.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.emanga.emanga.app.controllers.App;

/**
 * Created by Ciro on 5/03/14.
 */
public class ImageCacheManager {

    /**
     * Volley recommends in-memory L1 cache but both a disk and memory cache are provided.
     * Volley includes a L2 disk cache out of the box but you can technically use a disk cache as an L1 cache provided
     * you can live with potential i/o blocking.
     *
     */
    public enum CacheType {
        DISK
        , MEMORY
    }

    private static ImageCacheManager mInstance;

    /**
     * Volley image loader
     */
    private ImageLoader mImageLoader;

    /**
     * Image cache implementation
     */
    private ImageCache mImageCache;

    /**
     * @return
     * 		instance of the cache manager
     */
    public static ImageCacheManager getInstance(){
        if(mInstance == null)
            mInstance = new ImageCacheManager();

        return mInstance;
    }

    /**
     * Initializer for the manager. Must be called prior to use.
     *
     * @param context
     * 			application context
     * @param uniqueName
     * 			name for the cache location
     * @param cacheSize
     * 			max size for the cache
     * @param compressFormat
     * 			file type compression format.
     * @param quality
     */
    public void init(Context context, String uniqueName, int cacheSize, CompressFormat compressFormat, int quality, CacheType type){
        switch (type) {
            case DISK:
                mImageCache= new DiskLruImageCache(context, uniqueName, cacheSize, compressFormat, quality);
                break;
            case MEMORY:
                mImageCache = new BitmapLruImageCache(cacheSize);
            default:
                mImageCache = new BitmapLruImageCache(cacheSize);
                break;
        }

        mImageLoader = new ImageLoader(App.getInstance().mRequestQueue, mImageCache);
    }

    public Bitmap getBitmap(String url) {
        try {
            return mImageCache.getBitmap(createKey(url));
        } catch (NullPointerException e) {
            throw new IllegalStateException("Disk Cache Not initialized");
        }
    }

    public void putBitmap(String url, Bitmap bitmap) {
        try {
            mImageCache.putBitmap(createKey(url), bitmap);
        } catch (NullPointerException e) {
            throw new IllegalStateException("Disk Cache Not initialized");
        }
    }


    /**
     * 	Executes and image load
     * @param url
     * 		location of image
     * @param listener
     * 		Listener for completion
     */
    public void getImage(String url, ImageListener listener){
        mImageLoader.get(url, listener);
    }

    /**
     * @return
     * 		instance of the image loader
     */
    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    /**
     * Creates a unique cache key based on a url value
     * @param url
     * 		url to be used in key creation
     * @return
     * 		cache key value
     */
    private String createKey(String url){
        return String.valueOf(url.hashCode());
    }


}

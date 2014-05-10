package com.emanga.emanga.app.cache;

/**
 * Created by Ciro on 5/03/14.
 */
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;

public class BitmapLruImageCache extends LruCache<String, Bitmap> implements ImageCache{

    private final String TAG = this.getClass().getSimpleName();

    public BitmapLruImageCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }
}

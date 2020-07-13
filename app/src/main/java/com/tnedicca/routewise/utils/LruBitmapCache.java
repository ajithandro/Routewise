package com.tnedicca.routewise.utils;

import android.graphics.Bitmap;
import androidx.collection.LruCache;

import com.android.volley.toolbox.ImageLoader;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.fragments.Settings;

/**
 * Created by new on 08-02-2017.
 */

public class LruBitmapCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {
    private final RouteLog logger;

    public LruBitmapCache() {
        this(getDefaultLruCacheSize());
    }

    public LruBitmapCache(int sizeInKiloBytes) {
        super(sizeInKiloBytes);
        logger = new RouteLog();
        logger.setLoggerClass(Settings.class);
    }

    public static int getDefaultLruCacheSize() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        return cacheSize;
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
//        logger.info("Loading image from cache");
        return value.getRowBytes() * value.getHeight() / 1024;
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

package com.emanga.controllers;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class App extends Application {
	private static String TAG = App.class.getName();
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d(TAG, "Init eManga app");
		initImageLoader(getApplicationContext());
	}

	public static void initImageLoader(Context context) {
		//Image Loader Configuration
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
			.threadPriority(Thread.NORM_PRIORITY - 2)
			.denyCacheImageMultipleSizesInMemory()
			.discCacheFileNameGenerator(new Md5FileNameGenerator())
			.tasksProcessingOrder(QueueProcessingType.LIFO)
			.writeDebugLogs() // Remove for release app
			.build();
		
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}
}

package com.emanga.controllers;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class App extends Application {
	private static String TAG = App.class.getName();
	private static boolean DEVELOPER_MODE = true;
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "Init eManga app");
		
		if (DEVELOPER_MODE) {
	         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
	                 .detectDiskReads()
	                 .detectDiskWrites()
	                 .detectNetwork()
	                 .detectAll() 
	                 .penaltyLog()
	                 .build());
	         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
	                 .detectLeakedSqlLiteObjects()
	                 .detectLeakedClosableObjects()
	                 .detectAll()
	                 .penaltyLog()
	                 .penaltyDeath()
	                 .build());
	     }

		initImageLoader(getApplicationContext());
	}

	public static void initImageLoader(Context context) {
		//Image Loader Configuration
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
			.threadPriority(Thread.NORM_PRIORITY - 2)
			.denyCacheImageMultipleSizesInMemory()
			.discCacheFileNameGenerator(new Md5FileNameGenerator())
			.writeDebugLogs() // Remove for release app
			.build();
		
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}
}

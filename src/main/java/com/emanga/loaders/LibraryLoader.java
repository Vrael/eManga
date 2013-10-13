package com.emanga.loaders;

import java.sql.SQLException;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.emanga.database.DatabaseHelper;
import com.emanga.models.Manga;
import com.emanga.services.UpdateDatabase;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.stmt.QueryBuilder;

public class LibraryLoader extends AsyncTaskLoader<List<Manga>> {
	
	private static final String TAG = LibraryLoader.class.getName(); 
			
	private List<Manga> mangas;
	private MangaIntentReceiver mMangaObserver;
	
	private DatabaseHelper helper;
	
	private QueryBuilder<Manga, String> qBm;
	private static final long POSITION = 0;
	private static final long AMOUNT = 12;
	
	public LibraryLoader(Context context) {
		super(context);
		helper = OpenHelperManager.getHelper(getContext(), DatabaseHelper.class);
		
		try {	
			 qBm = helper.getMangaRunDao().queryBuilder();
			 qBm.orderBy(Manga.TITLE_COLUMN_NAME, true);
			 qBm.offset(POSITION).limit(AMOUNT);
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public List<Manga> loadInBackground() {
		Log.d(TAG, "Getting latest mangas from DB");
		
		//TODO: This must be change for all mangas, but loaded when there is scroll event
	
		try {
			mangas = qBm.query();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mangas;
	}
	
   @Override
   public void deliverResult(List<Manga> chaps) {
	   if (isReset()) {
		   // An async query came in while the loader is stopped.  We
           // don't need the result.
		   if (chaps != null) {
			   onReleaseResources(chaps);
		   }
	   }
 
	   // Hold a reference to the old data so it doesn't get garbage collected.
	   // We must protect it until the new data has been delivered.
	   List<Manga> oldData = mangas;
	   mangas = chaps;
 
	   if (isStarted()) {
		   // If the Loader is currently started, we can immediately
           // deliver its results.
		   super.deliverResult(chaps);
	   }
 
	   // Invalidate the old data as we don't need it any more.
	   if (oldData != null && oldData != mangas) {
		   onReleaseResources(oldData);
	   }
   }
   
   @Override
   protected void onStartLoading() {
	   if (mangas != null) {
		   deliverResult(mangas);
	   }
	   
	   if (mMangaObserver == null) {
		   mMangaObserver = new MangaIntentReceiver(this);
	   }
	   
	   if (takeContentChanged() || mangas == null) {
		   // If the data has changed since the last time it was loaded
           // or is not currently available, start a load 
		   forceLoad();
     }
   }
  
   @Override
   protected void onStopLoading() {
	   // Attempt to cancel the current load task if possible.
	   cancelLoad();
   }
  
   @Override
   protected void onReset() {
	   super.onReset();

	   onStopLoading();	 // Ensure the loader has been stopped.
  
	   // At this point we can release the resources associated with 'mData'.
	   if (mangas != null) {
		   onReleaseResources(mangas);
		   mangas = null;
	   }
	   
	   if (mMangaObserver != null) {
		   getContext().unregisterReceiver(mMangaObserver);
		   mMangaObserver = null;
	   }
   }
  
   @Override
   public void onCanceled(List<Manga> chaps) {
	   super.onCanceled(chaps);
  
	   // The load has been canceled, so we should release the resources
	   // associated with 'chaps'.
	   onReleaseResources(chaps);
   }
   
   /**
    * Helper function to take care of releasing resources associated
    * with an actively loaded data set.
    */
   protected void onReleaseResources(List<Manga> data) {
	   // For a simple List<> there is nothing to do.  For something
       // like a Cursor, we would close it here.
   }
   
   public static class MangaIntentReceiver extends BroadcastReceiver {
	   final LibraryLoader mLoader;
	   
	   public MangaIntentReceiver(LibraryLoader loader) {
		   mLoader = loader;
		   IntentFilter filter = new IntentFilter(UpdateDatabase.ACTION_LATEST_MANGAS);
		   mLoader.getContext().registerReceiver(this, filter);
	   }
	   
	   @Override
	   public void onReceive(Context context, Intent intent) {
		   Log.d(TAG, "New Manga received in the Loader!");
		   // Tell the loader about the change
		   mLoader.onContentChanged();
	   }
   }
}

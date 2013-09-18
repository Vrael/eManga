package com.emanga.tasks;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.emanga.database.DatabaseHelper;
import com.emanga.models.Chapter;
import com.emanga.services.UpdateDatabase;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class LatestChaptersLoader extends AsyncTaskLoader<List<Chapter>> {
	
	private static final String TAG = LatestChaptersLoader.class.getName(); 
			
	private List<Chapter> chapters;
	private ChapterIntentReceiver mChapterObserver;
	
	public LatestChaptersLoader(Context context) {
		super(context);
	}

	@Override
	public List<Chapter> loadInBackground() {
		Log.d(TAG, "Getting latest chapters from DB");
		DatabaseHelper helper = OpenHelperManager.getHelper(getContext(), DatabaseHelper.class);
		
		//TODO: This must be change for last chapters (eg: 20)
		chapters = helper.getChapterRunDao().queryForAll();
		return chapters;
	}
	
   @Override
   public void deliverResult(List<Chapter> chaps) {
	   if (isReset()) {
		   // An async query came in while the loader is stopped.  We
           // don't need the result.
		   if (chaps != null) {
			   onReleaseResources(chaps);
		   }
	   }
 
	   // Hold a reference to the old data so it doesn't get garbage collected.
	   // We must protect it until the new data has been delivered.
	   List<Chapter> oldData = chapters;
	   chapters = chaps;
 
	   if (isStarted()) {
		   // If the Loader is currently started, we can immediately
           // deliver its results.
		   super.deliverResult(chaps);
	   }
 
	   // Invalidate the old data as we don't need it any more.
	   if (oldData != null && oldData != chapters) {
		   onReleaseResources(oldData);
	   }
   }
   
   @Override
   protected void onStartLoading() {
	   if (chapters != null) {
		   deliverResult(chapters);
	   }
	   
	   if (mChapterObserver == null) {
		   mChapterObserver = new ChapterIntentReceiver(this);
	   }
	   
	   if (takeContentChanged() || chapters == null) {
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
	   if (chapters != null) {
		   onReleaseResources(chapters);
		   chapters = null;
	   }
	   
	   if (mChapterObserver != null) {
		   getContext().unregisterReceiver(mChapterObserver);
		   mChapterObserver = null;
	   }
   }
  
   @Override
   public void onCanceled(List<Chapter> chaps) {
	   super.onCanceled(chaps);
  
	   // The load has been canceled, so we should release the resources
	   // associated with 'chaps'.
	   onReleaseResources(chaps);
   }
   
   /**
    * Helper function to take care of releasing resources associated
    * with an actively loaded data set.
    */
   protected void onReleaseResources(List<Chapter> data) {
	   // For a simple List<> there is nothing to do.  For something
       // like a Cursor, we would close it here.
   }
   
   public static class ChapterIntentReceiver extends BroadcastReceiver {
	   final LatestChaptersLoader mLoader;
	   
	   public ChapterIntentReceiver(LatestChaptersLoader loader) {
		   mLoader = loader;
		   IntentFilter filter = new IntentFilter(UpdateDatabase.ACTION_LATEST_CHAPTERS);
		   mLoader.getContext().registerReceiver(this, filter);
	   }
	   
	   @Override
	   public void onReceive(Context context, Intent intent) {
		   Log.d(TAG, "New Chapter received in the Loader!");
		   // Tell the loader about the change
		   mLoader.onContentChanged();
	   }
   }
}

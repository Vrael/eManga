package com.emanga.emanga.app.loaders;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.emanga.emanga.app.database.DatabaseHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class LibraryLoader extends AsyncTaskLoader<Cursor> {
	
	private static final String TAG = LibraryLoader.class.getName(); 
			
	private Cursor mData;
	
	private DatabaseHelper helper;
	
	public LibraryLoader(Context context) {
		super(context);
		helper = OpenHelperManager.getHelper(getContext(), DatabaseHelper.class);
	}

	@Override
	public Cursor loadInBackground() {
		Log.d(TAG, "Getting mangas from DB");

		return helper.getMangasWithGenres();
	}
	
   @Override
   public void deliverResult(Cursor chaps) {
	   if (isReset()) {
		   // An async query came in while the loader is stopped.  We
           // don't need the result.
		   if (chaps != null) {
			   onReleaseResources(chaps);
		   }
	   }
 
	   // Hold a reference to the old data so it doesn't get garbage collected.
	   // We must protect it until the new data has been delivered.
	   Cursor oldData = mData;
	   mData = chaps;
 
	   if (isStarted()) {
		   // If the Loader is currently started, we can immediately
           // deliver its results.
		   super.deliverResult(chaps);
	   }
 
	   // Invalidate the old data as we don't need it any more.
	   if (oldData != null && oldData != mData) {
		   onReleaseResources(oldData);
	   }
   }
   
   @Override
   protected void onStartLoading() {
	   if (mData != null) {
		   deliverResult(mData);
	   }

	   if (takeContentChanged() || mData == null) {
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
	   if (mData != null) {
		   onReleaseResources(mData);
		   mData = null;
	   }
   }
  
   @Override
   public void onCanceled(Cursor chaps) {
	   super.onCanceled(chaps);
  
	   // The load has been canceled, so we should release the resources
	   // associated with 'chaps'.
	   onReleaseResources(chaps);
   }
   
   /**
    * Helper function to take care of releasing resources associated
    * with an actively loaded data set.
    */
   protected void onReleaseResources(Cursor data) {
	   // For a simple List<> there is nothing to do.  For something
       // like a Cursor, we would close it here.
	   if(data != null){
		   data.close();
	   }
   }
}

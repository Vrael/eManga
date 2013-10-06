package com.emanga.loaders;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.emanga.database.DatabaseHelper;
import com.emanga.models.Link;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

public class ReaderLoader extends AsyncTaskLoader<String []> {
	
	private static final String TAG = ReaderLoader.class.getName(); 
 
	private static final String ACTION = "com.manga.intent.action";
	public static final String ACTION_NEW_CHAPTER = ACTION + ".chapter";
			
	private String[] links;
	
	private ChapterIntentReceiver mChapterObserver;
	
	public Link chapterLink;

	public ReaderLoader(Context context, int chapterId) throws MalformedURLException, NumberFormatException, SQLException {
		super(context);
		
		RuntimeExceptionDao<Link, Integer> linkDao = OpenHelperManager.getHelper(getContext(), DatabaseHelper.class)
				.getLinkRunDao();
		
		QueryBuilder<Link, Integer> qBl = linkDao.queryBuilder();
		qBl.where().eq(Link.CHAPTER_COLUMN_NAME, chapterId);
		chapterLink = qBl.queryForFirst();
	}
	
	/**
	 * How links of chapters don't save in the database, always they will get from internet
	 * @see android.support.v4.content.AsyncTaskLoader#loadInBackground()
	 */
	@Override
	public String[] loadInBackground() {
		// For esMangaHere
		long start = System.currentTimeMillis();
		ArrayList<String> imagesLink = new ArrayList<String>();
		
		try {
			Document doc;
			Element page;
			String nextPage = chapterLink.url;
			do {
				doc = Jsoup.connect(nextPage)
				  .userAgent("Mozilla")
				  .cookie("auth", "token")
				  .timeout(20000)
				  .get();
				
				// Link wrapper where within is the image
				page = doc.select(".read_img a").first();
				// Next page link
				nextPage = page.absUrl("href");
				// Complete src url
				String linkImage = page.select("img").first().absUrl("src");
				imagesLink.add(linkImage);
			}
			while(nextPage != "");
			links = imagesLink.toArray(new String[imagesLink.size()]);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end - start));
		return links;
	}
	
   @Override
   public void deliverResult(String[] data) {
	   if (isReset()) {
		   // An async query came in while the loader is stopped.  We
           // don't need the result.
		   if (data != null) {
			   onReleaseResources(data);
		   }
	   }
 
	   // Hold a reference to the old data so it doesn't get garbage collected.
	   // We must protect it until the new data has been delivered.
	   String[] oldData = links;
	   links = data;
 
	   if (isStarted()) {
		   // If the Loader is currently started, we can immediately
           // deliver its results.
		   super.deliverResult(data);
	   }
 
	   // Invalidate the old data as we don't need it any more.
	   if (oldData != null && oldData != data) {
		   onReleaseResources(oldData);
	   }
   }
   
   @Override
   protected void onStartLoading() {
	   if (links != null) {
		   deliverResult(links);
	   }
	   
	   if (mChapterObserver == null) {
		   mChapterObserver = new ChapterIntentReceiver(this);
	   }
	   
	   if (takeContentChanged() || links == null) {
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
	   if (links != null) {
		   onReleaseResources(links);
		   links = null;
	   }
	   
	   if (mChapterObserver != null) {
		   getContext().unregisterReceiver(mChapterObserver);
		   mChapterObserver = null;
	   }
   }
  
   @Override
   public void onCanceled(String[] data) {
	   super.onCanceled(data);
  
	   // The load has been canceled, so we should release the resources
	   // associated with 'chaps'.
	   onReleaseResources(data);
   }
   
   /**
    * Helper function to take care of releasing resources associated
    * with an actively loaded data set.
    */
   protected void onReleaseResources(String[] data) {
	   // For a simple array there is nothing to do.  For something
       // like a Cursor, we would close it here.
   }
   
   public static class ChapterIntentReceiver extends BroadcastReceiver {
	   final ReaderLoader mLoader;
	   
	   public ChapterIntentReceiver(ReaderLoader loader) {
		   mLoader = loader;
		   IntentFilter filter = new IntentFilter(ReaderLoader.ACTION_NEW_CHAPTER);
		   mLoader.getContext().registerReceiver(this, filter);
	   }
	   
	   @Override
	   public void onReceive(Context context, Intent intent) {
		   Log.d(TAG, "New Chapter received in the Loader!");
		   // Tell the loader about the new chapters
		   mLoader.onContentChanged();
	   }
   }
}

package com.emanga.loaders;

import java.sql.SQLException;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.emanga.database.DatabaseHelper;
import com.emanga.models.Category;
import com.emanga.models.CategoryManga;
import com.emanga.models.Manga;
import com.emanga.models.MangaContent;
import com.emanga.models.MangaContent.MangaItem;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

public class MangaDetailLoader extends AsyncTaskLoader<MangaContent.MangaItem> {
	
	// It is the same as manga title
	private String mangaId;
	private MangaItem manga;
	
	private DatabaseHelper helper;
	private RuntimeExceptionDao<Manga, String> mangaDao;
	private RuntimeExceptionDao<Category, Integer> categoryDao;
	private RuntimeExceptionDao<CategoryManga, Integer> categoryMangaDao;
	
	public MangaDetailLoader(Context context, String id) {
		super(context);
		System.out.println("Loader creado con " + id);
		mangaId = id;
		helper = OpenHelperManager.getHelper(getContext(), DatabaseHelper.class);
		mangaDao = helper.getMangaRunDao();
		categoryDao = helper.getCategoryRunDao();
		categoryMangaDao = helper.getCategoryMangaRunDao();
	}

	@Override
	public MangaItem loadInBackground() {
		MangaContent.MangaItem mangaItem = null;
		try {
			mangaItem = new MangaItem();
			// Get the manga by Id
			mangaItem.manga = mangaDao.queryForId(mangaId);
			
			// Get the category list
			QueryBuilder<Category, Integer> qBc = categoryDao.queryBuilder();
			QueryBuilder<CategoryManga, Integer> qBcm = categoryMangaDao.queryBuilder();
			qBcm.where().eq(CategoryManga.MANGA_COLUMN_NAME, mangaId);
			qBc.join(qBcm);
			mangaItem.categories = qBc.query();
			manga = mangaItem;
			// TODO: Make something with chapters...
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mangaItem;
	}
	
	@Override
	   public void deliverResult(MangaItem data) {
		   if (isReset()) {
			   // An async query came in while the loader is stopped.  We
	           // don't need the result.
			   if (data != null) {
				   onReleaseResources(data);
			   }
		   }
	 
		   // Hold a reference to the old data so it doesn't get garbage collected.
		   // We must protect it until the new data has been delivered.
		   MangaItem oldData = manga;
		   manga = data;
	 
		   if (isStarted()) {
			   // If the Loader is currently started, we can immediately
	           // deliver its results.
			   super.deliverResult(data);
		   }
	 
		   // Invalidate the old data as we don't need it any more.
		   if (oldData != null && oldData != manga) {
			   onReleaseResources(oldData);
		   }
	   }
	   
	   @Override
	   protected void onStartLoading() {
		   if (manga != null) {
			   deliverResult(manga);
		   }
		   
		   if (takeContentChanged() || manga == null) {
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
		   if (manga != null) {
			   onReleaseResources(manga);
			   manga = null;
		   }
	   }
	  
	   @Override
	   public void onCanceled(MangaItem data) {
		   super.onCanceled(data);
	  
		   // The load has been canceled, so we should release the resources
		   // associated with 'chaps'.
		   onReleaseResources(data);
	   }
	   
	   /**
	    * Helper function to take care of releasing resources associated
	    * with an actively loaded data set.
	    */
	   protected void onReleaseResources(MangaItem data) {
		   // For a simple List<> there is nothing to do.  For something
	       // like a Cursor, we would close it here.
	   }
}

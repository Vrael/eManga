package com.emanga.loaders;

import java.sql.SQLException;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.AsyncTaskLoader;

import com.emanga.database.DatabaseHelper;
import com.emanga.models.Category;
import com.emanga.models.CategoryManga;
import com.emanga.models.Chapter;
import com.emanga.models.Manga;
import com.emanga.services.UpdateDescriptionService;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;

public class MangaDetailLoader extends AsyncTaskLoader<Manga> {

	// It is the same as manga title
	private int id;
	private Manga manga;
	
	private RuntimeExceptionDao<Manga, Integer> mangaDao;
	private QueryBuilder<Category, Integer> qBc;
	private QueryBuilder<Chapter, Integer> cBc;
    
	private SelectArg mangaIdQueryCategory;
	private SelectArg mangaIdQueryChapters;
	
	public MangaDetailLoader(Context context, int mangaId) {
		super(context);
		
		id = mangaId;
		mangaIdQueryCategory = new SelectArg();
		mangaIdQueryChapters = new SelectArg();
		
		DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
		
		mangaDao = helper.getMangaRunDao();
		
		try {
			// Query for category list
			qBc = helper.getCategoryRunDao().queryBuilder();
			QueryBuilder<CategoryManga, Integer> qBcm = helper.getCategoryMangaRunDao().queryBuilder();
			qBcm.where().eq(CategoryManga.MANGA_COLUMN_NAME, mangaIdQueryCategory);
			qBc.join(qBcm);
			
			// Query for last chapter
			cBc = helper.getChapterRunDao().queryBuilder();
			cBc.where().eq(Chapter.MANGA_COLUMN_NAME, mangaIdQueryChapters);
			cBc.orderBy(Chapter.READ_COLUMN_NAME, true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Manga loadInBackground() {
		try {
			// Get the manga by Id
			manga = mangaDao.queryForId(id);
			
			// Get manga description if it isn't in database yet
			if(manga.description == null){
				Intent intent = new Intent(getContext(), UpdateDescriptionService.class);
				intent.putExtra(UpdateDescriptionService.MANGAID, manga.id);
				getContext().startService(intent);
			}
			
			// Get the category list
			mangaIdQueryCategory.setValue(id);
			manga.categories = qBc.query();
			
			// Get last chapter
			mangaIdQueryChapters.setValue(id);
			Chapter c = cBc.queryForFirst();
			if(c != null) { 
				manga.chaptersList = new Chapter[]{c};
			}
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return manga;
	}
	
	@Override
	   public void deliverResult(Manga data) {
		   if (isReset()) {
			   // An async query came in while the loader is stopped.  We
	           // don't need the result.
			   if (data != null) {
				   onReleaseResources(data);
			   }
		   }
	 
		   // Hold a reference to the old data so it doesn't get garbage collected.
		   // We must protect it until the new data has been delivered.
		   Manga oldData = manga;
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
	   public void onCanceled(Manga data) {
		   super.onCanceled(data);
	  
		   // The load has been canceled, so we should release the resources
		   // associated with 'chaps'.
		   onReleaseResources(data);
	   }
	   
	   /**
	    * Helper function to take care of releasing resources associated
	    * with an actively loaded data set.
	    */
	   protected void onReleaseResources(Manga data) {
		   // For a simple List<> there is nothing to do.  For something
	       // like a Cursor, we would close it here.
	   }
}

package com.emanga.database;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.emanga.R;
import com.emanga.models.Category;
import com.emanga.models.CategoryManga;
import com.emanga.models.Chapter;
import com.emanga.models.Link;
import com.emanga.models.Manga;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;



/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "emanga.db";
	private static final int DATABASE_VERSION = 2;

	private RuntimeExceptionDao<Category, Integer> categoryRuntimeDao = null;
	private RuntimeExceptionDao<Manga, Integer> mangaRuntimeDao = null;
	private RuntimeExceptionDao<CategoryManga, Integer> CategoryMangaRuntimeDao = null;
	private RuntimeExceptionDao<Chapter, Integer> chapterRuntimeDao = null;
	private RuntimeExceptionDao<Link, Integer> linkRuntimeDao = null;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}

	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, CategoryManga.class);
			TableUtils.createTable(connectionSource, Category.class);
			TableUtils.createTable(connectionSource, Manga.class);
			TableUtils.createTable(connectionSource, Chapter.class);
			TableUtils.createTable(connectionSource, Link.class);
			
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	
	private QueryBuilder<Chapter, Integer> cQbIsChapters = null;
	private SelectArg cQbIsChaptersNchapter = null; 
	private SelectArg cQbIsChaptersMTitle = null;
	
	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, Link.class, true);
			TableUtils.dropTable(connectionSource, Chapter.class, true);
			TableUtils.dropTable(connectionSource, Manga.class, true);
			TableUtils.dropTable(connectionSource, Category.class, true);
			TableUtils.dropTable(connectionSource, CategoryManga.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns the RuntimeExceptionDao, a version of a Dao for our classes. It will
	 * create it or just give the cached value. 
	 * RuntimeExceptionDao only through RuntimeExceptions.
	 */
	public RuntimeExceptionDao<Category, Integer> getCategoryRunDao() {
		if (categoryRuntimeDao == null) 
			categoryRuntimeDao = getRuntimeExceptionDao(Category.class);
		return categoryRuntimeDao;
	}
	
	public RuntimeExceptionDao<Manga, Integer> getMangaRunDao() {
		if (mangaRuntimeDao == null) 
			mangaRuntimeDao = getRuntimeExceptionDao(Manga.class);
		return mangaRuntimeDao;
	}
	
	public RuntimeExceptionDao<CategoryManga, Integer> getCategoryMangaRunDao() {
		if (CategoryMangaRuntimeDao == null) 
			CategoryMangaRuntimeDao = getRuntimeExceptionDao(CategoryManga.class);
		return CategoryMangaRuntimeDao;
	}
	
	public RuntimeExceptionDao<Chapter, Integer> getChapterRunDao() {
		if (chapterRuntimeDao == null) 
			chapterRuntimeDao = getRuntimeExceptionDao(Chapter.class);
		return chapterRuntimeDao;
	}
	
	public RuntimeExceptionDao<Link, Integer> getLinkRunDao() {
		if (linkRuntimeDao == null) 
			linkRuntimeDao = getRuntimeExceptionDao(Link.class);
		return linkRuntimeDao;
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		categoryRuntimeDao = null;
		mangaRuntimeDao = null;
		chapterRuntimeDao = null;
		linkRuntimeDao = null;
	}
	
	public Cursor getMangasWithCategories(){
		// Field _id is the manga's title too
		return this.getReadableDatabase().rawQuery(
				"SELECT manga._id, manga.title, manga.cover, GROUP_CONCAT(category.name, ', ')"
				+ " AS " + Category.NAME_COLUMN_NAME + " FROM manga"
				+ " INNER JOIN categorymanga ON categorymanga.manga_id = manga._id"
				+ " INNER JOIN category ON category._id = categorymanga.category_id"
				+ " GROUP BY manga._id"
				+ " ORDER BY manga.title ASC", null);
	}
	
	public void saveCategories(final Category[] categories){
		final RuntimeExceptionDao<Category, Integer> dao = getCategoryRunDao();
		
		dao.callBatchTasks(new Callable<Void>() {
            public Void call() throws Exception {
            	for(Category c: categories){
        			dao.createIfNotExists(c);
        		}
             return null;
            }
		});
	}
	
	public void saveMangas(final Manga[] mangas){
		final RuntimeExceptionDao<Manga, Integer> mangaDao = getMangaRunDao();
		final RuntimeExceptionDao<Category, Integer> categoryDao = getCategoryRunDao();
		final RuntimeExceptionDao<CategoryManga, Integer> categoryMangaDao = getCategoryMangaRunDao();
		final RuntimeExceptionDao<Link, Integer> linkDao = getLinkRunDao();
		final RuntimeExceptionDao<Chapter, Integer> chapterDao = getChapterRunDao();
		
		mangaDao.callBatchTasks(
				new Callable<Void>(){
					public Void call() throws Exception {
						for(Manga m : mangas){
							mangaDao.createIfNotExists(m);
							if(m.categories != null){
								for(Category c : m.categories) {
									categoryDao.createIfNotExists(c);
									categoryMangaDao.createIfNotExists(new CategoryManga(c,m)); 
								}
							}
							if(m.chaptersList != null){
								for(Chapter c : m.chaptersList) {
									chapterDao.createIfNotExists(c);
									if(c.linksList != null){
										for(Link l: c.linksList){
											linkDao.createOrUpdate(l);
										}
									}
								}
							}
						}
						return null;
					}
				}
			);
	}
	
	public boolean isChapter(Manga manga, int number){
		Chapter chapter = null;
		try {
			if(cQbIsChapters == null){
				cQbIsChapters = getChapterRunDao().queryBuilder();
				cQbIsChaptersNchapter = new SelectArg();
				cQbIsChaptersMTitle = new SelectArg();
				
				cQbIsChapters.where().eq(Chapter.MANGA_COLUMN_NAME, cQbIsChaptersMTitle).and()
					.eq(Chapter.NUMBER_COLUMN_NAME, cQbIsChaptersNchapter);
			}
			
			cQbIsChaptersMTitle.setValue(manga.title);
			cQbIsChaptersNchapter.setValue(number);		
			
			chapter = cQbIsChapters.queryForFirst();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return (chapter) != null? true : false;
	}
	
	/**
	 * Check if Manga entity in database is empty
	 * @return true if there is at least one manga, false in the opposite case
	 */
	public boolean isMangas(){
		boolean result = false;
		CloseableIterator<Manga> it = getMangaRunDao().iterator();
		result = it.hasNext()? true : false;
		it.closeQuietly();
		return result;
	}
}

package com.emanga.database;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.emanga.R;
import com.emanga.models.Category;
import com.emanga.models.CategoryManga;
import com.emanga.models.Chapter;
import com.emanga.models.Link;
import com.emanga.models.Manga;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;


/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "emanga.db";
	private static final int DATABASE_VERSION = 2;

	// the DAO objects
	private Dao<Category, Integer> categoryDao = null;
	private Dao<Manga, String> mangaDao = null;
	private Dao<CategoryManga, Integer> CategoryMangaDao = null;
	private Dao<Chapter, Integer> chapterDao = null;
	private Dao<Link, Integer> linkDao = null;
	
	private RuntimeExceptionDao<Category, Integer> categoryRuntimeDao = null;
	private RuntimeExceptionDao<Manga, String> mangaRuntimeDao = null;
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
	 * Returns the Database Access Object (DAO) for our classes. It will create it or just give the cached
	 * value.
	 */
	public Dao<Category, Integer> getCategoryDao() throws SQLException {
		if(categoryDao == null)
			getDao(Category.class);
		return categoryDao;
	}
	
	public Dao<Manga, String> getMangaDao() throws SQLException {
		if(mangaDao == null)
			getDao(Manga.class);
		return mangaDao;
	}
	
	public Dao<CategoryManga, Integer> getCategoryMangaDao() throws SQLException {
		if(CategoryMangaDao == null)
			getDao(CategoryManga.class);
		return CategoryMangaDao;
	}
	public Dao<Chapter, Integer> getChapterDao() throws SQLException {
		if(chapterDao == null)
			getDao(Chapter.class);
		return chapterDao;
	}
	
	public Dao<Link, Integer> getLinkDao() throws SQLException {
		if(linkDao == null)
			getDao(Link.class);
		return linkDao;
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
	
	public RuntimeExceptionDao<Manga, String> getMangaRunDao() {
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
}

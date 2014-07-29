package com.emanga.emanga.app.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.emanga.emanga.app.R;
import com.emanga.emanga.app.models.Author;
import com.emanga.emanga.app.models.AuthorManga;
import com.emanga.emanga.app.models.Chapter;
import com.emanga.emanga.app.models.Genre;
import com.emanga.emanga.app.models.GenreManga;
import com.emanga.emanga.app.models.Manga;
import com.emanga.emanga.app.models.Page;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "emanga.db";
	private static final int DATABASE_VERSION = 6;

	private RuntimeExceptionDao<Genre, String> genreRuntimeDao = null;
    private RuntimeExceptionDao<Author, String> authorRuntimeDao = null;
	private RuntimeExceptionDao<Manga, String> mangaRuntimeDao = null;
	private RuntimeExceptionDao<GenreManga, String> genremangaRuntimeDao = null;
    private RuntimeExceptionDao<AuthorManga, String> authormangaRuntimeDao = null;
    private RuntimeExceptionDao<Chapter, String> chapterRuntimeDao = null;
	private RuntimeExceptionDao<Page, String> pageRuntimeDao = null;

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
			TableUtils.createTable(connectionSource, GenreManga.class);
			TableUtils.createTable(connectionSource, Genre.class);
            TableUtils.createTable(connectionSource, AuthorManga.class);
            TableUtils.createTable(connectionSource, Author.class);
			TableUtils.createTable(connectionSource, Manga.class);
			TableUtils.createTable(connectionSource, Chapter.class);
            TableUtils.createTable(connectionSource, Page.class);
			
			// Table for search using fts3: Ormlite doesn't support it yet
			// The table has same data that cursor manga list on library tab
            if (android.os.Build.VERSION.SDK_INT >= 11) {
                db.execSQL("CREATE VIRTUAL TABLE manga_fts USING fts4(_id, title, cover, name)");
            } else {
                db.execSQL("CREATE VIRTUAL TABLE manga_fts USING fts3(_id, title, cover, name)");
            }
			
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
			TableUtils.dropTable(connectionSource, Page.class, true);
			TableUtils.dropTable(connectionSource, Chapter.class, true);
			TableUtils.dropTable(connectionSource, Manga.class, true);
			TableUtils.dropTable(connectionSource, Genre.class, true);
			TableUtils.dropTable(connectionSource, GenreManga.class, true);
            TableUtils.dropTable(connectionSource, Author.class, true);
            TableUtils.dropTable(connectionSource, AuthorManga.class, true);
			
			db.execSQL("DROP TABLE manga_fts");
			
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
	public RuntimeExceptionDao<Genre, String> getGenreRunDao() {
		if (genreRuntimeDao == null)
			genreRuntimeDao = getRuntimeExceptionDao(Genre.class);
		return genreRuntimeDao;
	}

    public RuntimeExceptionDao<Author, String> getAuthorRunDao() {
        if (authorRuntimeDao == null)
            authorRuntimeDao = getRuntimeExceptionDao(Author.class);
        return authorRuntimeDao;
    }
	
	public RuntimeExceptionDao<Manga, String> getMangaRunDao() {
		if (mangaRuntimeDao == null) 
			mangaRuntimeDao = getRuntimeExceptionDao(Manga.class);
		return mangaRuntimeDao;
	}

	public RuntimeExceptionDao<GenreManga, String> getGenreMangaRunDao() {
		if (genremangaRuntimeDao == null)
			genremangaRuntimeDao = getRuntimeExceptionDao(GenreManga.class);
		return genremangaRuntimeDao;
	}

    public RuntimeExceptionDao<AuthorManga, String> getAuthorMangaRunDao() {
        if (authormangaRuntimeDao == null)
            authormangaRuntimeDao = getRuntimeExceptionDao(AuthorManga.class);
        return authormangaRuntimeDao;
    }
	
	public RuntimeExceptionDao<Chapter, String> getChapterRunDao() {
		if (chapterRuntimeDao == null) 
			chapterRuntimeDao = getRuntimeExceptionDao(Chapter.class);
		return chapterRuntimeDao;
	}

    public RuntimeExceptionDao<Page, String> getPageRunDao() {
        if (pageRuntimeDao == null)
            pageRuntimeDao = getRuntimeExceptionDao(Page.class);
        return pageRuntimeDao;
    }

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		genreRuntimeDao = null;
		mangaRuntimeDao = null;
		chapterRuntimeDao = null;
		pageRuntimeDao = null;
	}
	
	private static String mangasWithGenresQuery =
			"SELECT manga._id, manga.title, manga.cover, GROUP_CONCAT(" + GenreManga.CATEGORY_COLUMN_NAME + ", ', ')"
			+ " AS " + Genre.NAME_COLUMN_NAME + " FROM manga"
			+ " LEFT OUTER JOIN genremanga ON genremanga.manga_id = manga._id"
			+ " GROUP BY manga._id"
			+ " ORDER BY manga.title ASC";
			
	public Cursor getMangasWithGenres(){
		return this.getReadableDatabase().rawQuery(mangasWithGenresQuery, null);
	}
	
	/**
	 * Repopulate the table of search
	 */
	public void updateMangaFTS(){
		this.getReadableDatabase().execSQL("DELETE FROM manga_fts");
		this.getReadableDatabase().execSQL("INSERT INTO manga_fts (_id,title,cover,name)"
        + " SELECT manga._id, manga.title, manga.cover, GROUP_CONCAT(genremanga.genre_id, ', ')"
		+ " AS name FROM manga"
		+ " INNER JOIN genremanga ON genremanga.manga_id = manga._id"
		+ " GROUP BY manga._id"
		+ " ORDER BY manga.title ASC");
		this.getReadableDatabase().execSQL("INSERT INTO manga_fts(manga_fts) VALUES(?)", new String[]{"optimize"});
				
	}

    public String lastMangaDate(){
        Cursor cursor = this.getReadableDatabase().rawQuery(
                "SELECT MAX(" + Manga.DATE_COLUMN_NAME + ") AS last FROM manga", null);
        cursor.moveToFirst();
        String date = cursor.getString(cursor.getColumnIndex("last"));
        cursor.close();
        return date != null? date : "";
    }

    public String lastChapterDate(){
        Cursor cursor = this.getReadableDatabase().rawQuery(
                "SELECT MAX(" + Chapter.DATE_COLUMN_NAME + ") AS last FROM chapter", null);
        cursor.moveToFirst();
        String date = cursor.getString(cursor.getColumnIndex("last"));
        cursor.close();
        return date != null? date : "";
    }

	public Cursor searchOnLibrary(String text){
        if(text.length() == 0)
            return this.getReadableDatabase().rawQuery("SELECT * FROM manga_fts", null);
        else
            return this.getReadableDatabase().rawQuery(
                    "SELECT * FROM manga_fts WHERE manga_fts MATCH ?", new String[]{text + "*"});

	}

	public void saveMangas(final Manga[] mangas){
		final RuntimeExceptionDao<Manga, String> mangaDao = getMangaRunDao();
        final RuntimeExceptionDao<Chapter, String> chapterDao = getChapterRunDao();
		final RuntimeExceptionDao<Genre, String> genreDao = getGenreRunDao();
		final RuntimeExceptionDao<GenreManga, String> genremangaDao = getGenreMangaRunDao();
        final RuntimeExceptionDao<Author, String> authorDao = getAuthorRunDao();
        final RuntimeExceptionDao<AuthorManga, String> authormangaDao = getAuthorMangaRunDao();

		mangaDao.callBatchTasks(
				new Callable<Void>(){
					public Void call() throws Exception {
						for(Manga m : mangas){
							mangaDao.createIfNotExists(m);
                            if(m.chapters != null){
                                for(Chapter c: m.chapters){
                                    c.manga = m;
                                    chapterDao.createIfNotExists(c);
                                }
                            }
							if(m.genres != null){
								for(Genre g : m.genres) {
									genreDao.createIfNotExists(g);
									genremangaDao.createIfNotExists(new GenreManga(g,m));
								}
							}
                            if(m.authors != null){
                                for(Author a : m.authors){
                                    authorDao.createIfNotExists(a);
                                    authormangaDao.createIfNotExists(new AuthorManga(a,m));
                                }
                            }
						}
						return null;
					}
				}
			);
	}

    public void save(final Manga m){
        final RuntimeExceptionDao<Manga, String> dao = getMangaRunDao();
        dao.createOrUpdate(m);
    }

    private PreparedQuery<Genre> genresForMangaQuery = null;

    public List<Genre> genresForManga(Manga manga) throws SQLException{
            if (genresForMangaQuery == null) {
                genresForMangaQuery = makeGenresForMangaQuery();
            }
            genresForMangaQuery.setArgumentHolderValue(0, manga);
            return genreRuntimeDao.query(genresForMangaQuery);
    }

    public void genresForMangas(List<Manga> mangas){
        Iterator<Manga> it = mangas.iterator();
        while(it.hasNext()){
            Manga m = it.next();
            try {
                m.genres = genresForManga(m);
            } catch(SQLException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Build our query for Genre objects that match a Manga.
     */
    private PreparedQuery<Genre> makeGenresForMangaQuery() throws SQLException {

        QueryBuilder<GenreManga, String> mangaGenreQb = getGenreMangaRunDao().queryBuilder();
        mangaGenreQb.selectColumns(GenreManga.CATEGORY_COLUMN_NAME);
        SelectArg userSelectArg = new SelectArg();
        mangaGenreQb.where().eq(GenreManga.MANGA_COLUMN_NAME, userSelectArg);

        QueryBuilder<Genre, String> genreQb = getGenreRunDao().queryBuilder();
        genreQb.where().in(Genre.NAME_COLUMN_NAME, mangaGenreQb);

        return genreQb.prepare();
    }

}

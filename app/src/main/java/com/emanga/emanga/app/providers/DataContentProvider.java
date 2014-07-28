package com.emanga.emanga.app.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.emanga.emanga.app.database.DatabaseHelper;
import com.emanga.emanga.app.models.Manga;
import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;

/**
 * Created by Ciro on 23/07/2014.
 */
public class DataContentProvider extends ContentProvider {
    private static final String AUTHORITY = "com.mangapp.provider";
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static class Type {
        static final short MANGA_ALL = 0;
        static final short MANGA_ID = 1;
    }

    static {
        uriMatcher.addURI(AUTHORITY, Manga.MODEL_NAME + "", Type.MANGA_ALL);
        uriMatcher.addURI(AUTHORITY, Manga.MODEL_NAME + "/*", Type.MANGA_ID);
    }

    private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = OpenHelperManager.getHelper(getContext(), DatabaseHelper.class);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        switch (uriMatcher.match(uri)){
            case Type.MANGA_ALL:
                return ((AndroidDatabaseResults) dbHelper.getMangaRunDao().iterator().getRawResults()).getRawCursor();
            case Type.MANGA_ID:
                QueryBuilder<Manga,String> qb = dbHelper.getMangaRunDao().queryBuilder();
                try {
                    qb.where().eq(Manga.ID_COLUMN_NAME, selection);
                    return ((AndroidDatabaseResults) dbHelper.getMangaRunDao().iterator(qb.prepare()).getRawResults()).getRawCursor();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            default:
                throw new RuntimeException("No content provider URI match.");
        }

    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case Type.MANGA_ALL:
                return "vnd.android.cursor.dir/vnd.com.mangapp.provider.manga";
            case Type.MANGA_ID:
                return "vnd.android.cursor.item/vnd.com.mangapp.provider.manga";
            default:
                throw new RuntimeException("No content provider URI match.");
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}

package com.emanga.database;

import java.io.IOException;
import java.sql.SQLException;

import com.emanga.models.Chapter;
import com.emanga.models.Category;
import com.emanga.models.CategoryManga;
import com.emanga.models.Link;
import com.emanga.models.Manga;
import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {
	private static final Class<?>[] classes = new Class[] {
		Link.class,
		Chapter.class,
		Manga.class,
		Category.class,
		CategoryManga.class,
	};
	
	public static void main(String[] args) throws SQLException, IOException {
		writeConfigFile("ormlite_config.txt", classes);
	}
}
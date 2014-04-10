package com.emanga.emanga.app.database;

import com.emanga.emanga.app.models.Chapter;
import com.emanga.emanga.app.models.Genre;
import com.emanga.emanga.app.models.GenreManga;
import com.emanga.emanga.app.models.Manga;
import com.emanga.emanga.app.models.Page;
import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {
	private static final Class<?>[] classes = new Class[] {
		Page.class,
		Chapter.class,
		Manga.class,
		Genre.class,
		GenreManga.class,
	};
	
	public static void main(String[] args) throws SQLException, IOException {
		writeConfigFile(new File("c:/workspace/eManga/app/src/main/res/raw/ormlite_config.txt"), classes);
	}
}
package com.emanga.emanga.app.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


/**
 * Many to many relationship between Genres and Mangas
 */
@DatabaseTable
public class GenreManga {
	public static final String ID_COLUMN_NAME = "_id";
	public static final String CATEGORY_COLUMN_NAME = "genre_id";
	public static final String MANGA_COLUMN_NAME = "manga_id";

	@DatabaseField(id = true, columnName = ID_COLUMN_NAME)
	public String _id;
	@DatabaseField(foreign = true, columnName = CATEGORY_COLUMN_NAME)
	public Genre genre;
	@DatabaseField(foreign = true, columnName = MANGA_COLUMN_NAME)
	public Manga manga;
	
	public GenreManga(){
		//needed by ormlite
	}
	
	public GenreManga(Genre genre, Manga manga){
		this._id = genre.name + "_" + manga.title;
        this.genre = genre;
		this.manga = manga;
	}
}

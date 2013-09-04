package com.emanga.models;

import com.j256.ormlite.field.DatabaseField;

/**
 * Many to many relationship between Genres and Mangas
 */

public class GenreManga {
	@DatabaseField(generatedId = true)
	public int id;
	@DatabaseField(foreign = true, columnName="genre_id")
	public Genre genre;
	@DatabaseField(foreign = true, columnName="manga_id")
	public Manga manga;
	
	public GenreManga(){
		//needed by ormlite
	}
	
	public GenreManga(Genre g, Manga m){
		genre = g;
		manga = m;
	}
}

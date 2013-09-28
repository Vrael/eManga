package com.emanga.models;

import com.j256.ormlite.field.DatabaseField;

/**
 * Many to many relationship between Genres and Mangas
 */

public class CategoryManga {
	@DatabaseField(generatedId = true)
	public int id;
	@DatabaseField(foreign = true, columnName="category_id")
	public Category category;
	@DatabaseField(foreign = true, columnName="manga_id")
	public Manga manga;
	
	public CategoryManga(){
		//needed by ormlite
	}
	
	public CategoryManga(Category g, Manga m){
		category = g;
		manga = m;
	}
}

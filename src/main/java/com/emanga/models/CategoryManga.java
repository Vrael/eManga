package com.emanga.models;

import com.j256.ormlite.field.DatabaseField;

/**
 * Many to many relationship between Genres and Mangas
 */

public class CategoryManga {
	public static final String CATEGORY_COLUMN_NAME = "category_id";
	public static final String MANGA_COLUMN_NAME = "manga_id";
	
	@DatabaseField(generatedId = true)
	public int id;
	@DatabaseField(foreign = true, columnName=CATEGORY_COLUMN_NAME)
	public Category category;
	@DatabaseField(foreign = true, columnName=MANGA_COLUMN_NAME)
	public Manga manga;
	
	public CategoryManga(){
		//needed by ormlite
	}
	
	public CategoryManga(Category g, Manga m){
		category = g;
		manga = m;
	}
}

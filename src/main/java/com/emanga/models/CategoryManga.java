package com.emanga.models;

import com.google.common.base.Objects;
import com.j256.ormlite.field.DatabaseField;

/**
 * Many to many relationship between Genres and Mangas
 */

public class CategoryManga {
	public static final String ID_COLUMN_NAME = "_id";
	public static final String CATEGORY_COLUMN_NAME = "category_id";
	public static final String MANGA_COLUMN_NAME = "manga_id";
	
	@DatabaseField(id = true, columnName = ID_COLUMN_NAME)
	public int id;
	@DatabaseField(foreign = true, columnName = CATEGORY_COLUMN_NAME)
	public Category category;
	@DatabaseField(foreign = true, columnName = MANGA_COLUMN_NAME)
	public Manga manga;
	
	public CategoryManga(){
		//needed by ormlite
	}
	
	public CategoryManga(Category g, Manga m){
		category = g;
		manga = m;
		id = hashCode();
	}
	
	@Override
	public int hashCode() {
		// Two category - mangas are equals if they have same name + title
		return Objects.hashCode(category.name + manga.title);
	}
}

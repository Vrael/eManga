package com.emanga.models;

import java.util.List;
import java.util.Locale;

import com.google.common.base.Objects;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Manga {
	public static final String ID_COLUMN_NAME = "_id";
	public static final String TITLE_COLUMN_NAME = "title";
	public static final String COVER_COLUMN_NAME = "cover";
	public static final String DESCRIPTION_COLUMN_NAME = "description";
	public static final String CHAPTERS_COLUMN_NAME = "chapters";
	public static final String LINK_COLUMN_NAME = "chapters";
	
	@DatabaseField(id = true, columnName = ID_COLUMN_NAME)
	public int id;
	@DatabaseField(columnName = TITLE_COLUMN_NAME)
	public String title;
	@DatabaseField
	public String cover;
	@DatabaseField(columnName = DESCRIPTION_COLUMN_NAME)
	public String description;
	@ForeignCollectionField
	public ForeignCollection<Chapter> chapters;
	@DatabaseField(columnName = LINK_COLUMN_NAME)
	public String link;
	
	// Handle categories for N - N relationship in CategoryManga
	public List<Category> categories;
	
	// Handle chapters for 1 - N relationship in CategoryManga
	public Chapter[] chaptersList;
	
	
	public Manga() {
		// needed by ormlite
	}
	
	public Manga(String name) {
		title = name.toLowerCase(Locale.ENGLISH);
		id = hashCode();
	}
	
	public Manga(String name, String image) {
		title = name.toLowerCase(Locale.ENGLISH);
		cover = image;
		id = hashCode();
	}
	
	public Manga(String name, String image, List<Category> categoriesList, String url) {
		title = name.toLowerCase(Locale.ENGLISH);
		cover = image;
		categories = categoriesList;
		link = url;
		id = hashCode();
	}
	
	@Override
	public int hashCode() {
		// Two mangas are equals if they have same title
		return Objects.hashCode(title);
	}
	
	
}

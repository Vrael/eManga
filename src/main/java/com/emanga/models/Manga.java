package com.emanga.models;

import java.util.Date;
import java.util.List;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Manga {
	public static final String TITLE_COLUMN_NAME = "_id";
	public static final String COVER_COLUMN_NAME = "cover";
	public static final String DESCRIPTION_COLUMN_NAME = "description";
	public static final String PUBLICATIONSTART_COLUMN_NAME = "publicationStart";
	public static final String PUBLICATIONEND_COLUMN_NAME = "publicationEnd";
	public static final String CHAPTERS_COLUMN_NAME = "chapters";
	
	@DatabaseField(id = true, columnName = TITLE_COLUMN_NAME)
	public String title;
	@DatabaseField
	public String cover;
	@DatabaseField(columnName = DESCRIPTION_COLUMN_NAME)
	public String description;
	@DatabaseField
	public Date publicationStart;
	@DatabaseField
	public Date publicationEnd;
	@ForeignCollectionField
	public ForeignCollection<Chapter> chapters;
	@DatabaseField(foreign = true)
	public Link link;
	
	// Handle categories for N - N relationship in CategoryManga
	public List<Category> categories;
	
	public Manga() {
		// needed by ormlite
	}
	
	public Manga(String name) {
		title = name;
	}
	
	public Manga(String name, String image) {
		title = name;
		cover = image;
	}
	
	public Manga(String name, String image, List<Category> categoriesList, Link url) {
		title = name;
		cover = image;
		categories = categoriesList;
		link = url;
	}
}

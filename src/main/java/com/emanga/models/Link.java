package com.emanga.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Link {
	public static final String ID_COLUMN_NAME = "id";
	public static final String URL_COLUMN_NAME = "url";
	public static final String CHAPTER_COLUMN_NAME = "chapter_id";
	
	@DatabaseField(generatedId = true)
	public int id;
	@DatabaseField
	public String url;
	@DatabaseField(foreign = true)
	public Chapter chapter;
	
	public Link() {
		// needed by ormlite
	}
	
	public Link(String link, Chapter c) {
		url = link;
		chapter = c;
	}

	public Link(String link){
		url = link;
	}
}

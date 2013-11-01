package com.emanga.models;

import java.util.Locale;

import com.google.common.base.Objects;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Link {
	public static final String ID_COLUMN_NAME = "_id";
	public static final String URL_COLUMN_NAME = "url";
	public static final String CHAPTER_COLUMN_NAME = "chapter_id";
	
	@DatabaseField(id = true, columnName = ID_COLUMN_NAME)
	public int id;
	@DatabaseField(columnName = URL_COLUMN_NAME)
	public String url;
	@DatabaseField(foreign = true, columnName = CHAPTER_COLUMN_NAME)
	public Chapter chapter;
	
	public Link() {
		// needed by ormlite
	}
	
	public Link(String link, Chapter c){
		url = link.toLowerCase(Locale.ENGLISH);
		chapter = c;
		id = hashCode();
	}

	public Link(String link){
		url = link.toLowerCase(Locale.ENGLISH);		
		id = hashCode();
	}
	
	@Override
	public int hashCode() {
		// Two links are equals if they have same url
		return Objects.hashCode(url);
	}
}

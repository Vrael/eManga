package com.emanga.models;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import com.google.common.base.Objects;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Chapter {
	public static final String ID_COLUMN_NAME = "_id";
	public static final String DATE_COLUMN_NAME = "date";
	public static final String NUMBER_COLUMN_NAME = "number";
	public static final String MANGA_COLUMN_NAME = "manga_id";
	public static final String LINKS_COLUMN_NAME = "links";
	
	@DatabaseField(id = true, columnName = ID_COLUMN_NAME)
	public int id;
	@DatabaseField(columnName = DATE_COLUMN_NAME)
	public Date date;
	@DatabaseField(index = true, columnName = NUMBER_COLUMN_NAME)
	public int number;
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = MANGA_COLUMN_NAME)
	public Manga manga;
	@ForeignCollectionField(columnName = LINKS_COLUMN_NAME)
    public ForeignCollection<Link> links;
	
	// Mange N - N relationship
	public Link[] linksList;
	
	public Chapter() {
		// needed by ormlite
	}
	
	public Chapter(int num, Date d, Manga m) throws UnsupportedEncodingException {
		date = d;
		number = num;
		manga = m;
		id = hashCode();
	}
	
	public Chapter(int num, Date d, Manga m, Link ... link ) throws UnsupportedEncodingException {
		date = d;
		number = num;
		manga = m;
		linksList = link;
		id = hashCode();
	}
	
	@Override
	public int hashCode() {
		// Two chapters are equals if they have same title manga and number
		return Objects.hashCode(manga.title + number);
	}
}

package com.emanga.models;

import java.util.Date;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Chapter {
	public static final String ID_COLUMN_NAME = "id";
	public static final String DATE_COLUMN_NAME = "date";
	public static final String NUMBER_COLUMN_NAME = "number";
	public static final String MANGA_COLUMN_NAME = "manga_id";
	public static final String LINKS_COLUMN_NAME = "links";
	
	@DatabaseField(generatedId = true)
	public int id;
	@DatabaseField
	public Date date;
	@DatabaseField(index = true)
	public int number;
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	public Manga manga;
	@ForeignCollectionField
    public ForeignCollection<Link> links;
	
	public Chapter() {
		// needed by ormlite
	}
	
	public Chapter(int num, Date d, Manga m) {
		date = d;
		number = num;
		manga = m;
	}
}

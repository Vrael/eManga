package com.emanga.models;

import java.util.Date;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Chapter {
	
	@DatabaseField(generatedId = true)
	public int id;
	@DatabaseField
	public Date date;
	@DatabaseField(index = true)
	public int number;
	@DatabaseField(foreign = true)
	public Manga manga;
	@ForeignCollectionField
    ForeignCollection<Link> links;
	
	public Chapter() {
		// needed by ormlite
	}

}

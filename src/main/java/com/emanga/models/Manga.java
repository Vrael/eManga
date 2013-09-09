package com.emanga.models;

import java.util.Date;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Manga {
	@DatabaseField(generatedId = true)
	public int id;
	@DatabaseField(index = true)
	public String title;
	@DatabaseField
	public String cover;
	@DatabaseField
	public String description;
	@DatabaseField
	public Date publicationStart;
	@DatabaseField
	public Date publicationEnd;
	@ForeignCollectionField
	ForeignCollection<Chapter> chapters;
	
	public Manga() {
		// needed by ormlite
	}
	
}

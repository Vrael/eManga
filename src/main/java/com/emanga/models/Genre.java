package com.emanga.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Genre {
	
	@DatabaseField(generatedId = true)
	public int id;
	@DatabaseField(index = true)
	public String name;
	
	public Genre() {
		// needed by ormlite
	}
}

package com.emanga.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Link {
	
	@DatabaseField(generatedId = true)
	public int id;
	@DatabaseField
	public String url;
	@DatabaseField
	public int state;
	@DatabaseField
	public int attempts;
	@DatabaseField
	public float timeResponse;
	@DatabaseField(foreign = true)
	private Chapter chapter;
	
	public Link() {
		// needed by ormlite
	}
	
	
}

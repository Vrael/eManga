package com.emanga.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Category {
	
	public static final String ID_COLUMN_NAME = "id";
	public static final String NAME_COLUMN_NAME = "name";
	
	@DatabaseField(generatedId = true)
	public int id;
	@DatabaseField(index = true)
	public String name;
	
	public Category() {
		// needed by ormlite
	}
	
	public Category(String title){
		name = title;
	}
}

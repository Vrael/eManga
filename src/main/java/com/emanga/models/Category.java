package com.emanga.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Category {
	
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

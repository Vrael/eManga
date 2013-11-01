package com.emanga.models;

import java.util.Locale;

import com.google.common.base.Objects;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Category {
	
	public static final String ID_COLUMN_NAME = "_id";
	public static final String NAME_COLUMN_NAME = "name";
	
	@DatabaseField(id = true, columnName = ID_COLUMN_NAME)
	public int id;
	@DatabaseField(index = true, columnName = NAME_COLUMN_NAME)
	public String name;
	
	public Category() {
		// needed by ormlite
	}
	
	public Category(String title) {
		name = title.toLowerCase(Locale.ENGLISH);
		id = hashCode();
	}
	
	@Override
	public int hashCode() {
		// Two categories are equals if they have same name
		return Objects.hashCode(name);
	}
}

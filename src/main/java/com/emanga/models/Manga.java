package com.emanga.models;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Manga implements Parcelable {
	public static final String ID_COLUMN_NAME = "id";
	public static final String TITLE_COLUMN_NAME = "title";
	public static final String COVER_COLUMN_NAME = "cover";
	public static final String DESCRIPTION_COLUMN_NAME = "description";
	public static final String PUBLICATIONSTART_COLUMN_NAME = "publicationStart";
	public static final String PUBLICATIONEND_COLUMN_NAME = "publicationEnd";
	public static final String CHAPTERS_COLUMN_NAME = "chapters";
	
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
	public ForeignCollection<Chapter> chapters;
	
	public Manga() {
		// needed by ormlite
	}
	
	public Manga(String name) {
		title = name;
	}
	
	public Manga(String name, String image) {
		title = name;
		cover = image;
	}
	
	public Manga(Parcel in) {
		id = in.readInt();
		title = in.readString();
		cover = in.readString();
		description = in.readString();
		publicationStart = new Date(in.readLong());
		publicationEnd = new Date(in.readLong());
		//TODO: Falta la colección!
	}
	
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(title);
		dest.writeString(cover);
		dest.writeString(description);
		dest.writeLong(publicationStart.getTime());
		dest.writeLong(publicationEnd.getTime());
		//TODO: Falta la colección!
	}
	
	public static final Parcelable.Creator<Chapter> CREATOR = new Parcelable.Creator<Chapter>() {
		public Chapter createFromParcel(Parcel in) {
			return new Chapter(in);
		}
		
		public Chapter[] newArray(int size) {
		    return new Chapter[size];
		}
	};
}

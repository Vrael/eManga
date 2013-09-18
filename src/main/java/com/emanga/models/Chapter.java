package com.emanga.models;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Chapter implements Parcelable {
	public static final String ID_COLUMN_NAME = "id";
	public static final String DATE_COLUMN_NAME = "date";
	public static final String NUMBER_COLUMN_NAME = "number";
	public static final String MANGA_COLUMN_NAME = "manga";
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
	

	public Chapter(Parcel in) {
		id = in.readInt();
		date = new Date(in.readLong());
		number = in.readInt();
		manga = in.readParcelable(getClass().getClassLoader());
		//TODO: Falta la colección!
	}
	
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeLong(date.getTime());
		dest.writeInt(number);
		dest.writeParcelable(manga, flags);
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

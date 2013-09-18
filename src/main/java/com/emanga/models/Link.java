package com.emanga.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Link implements Parcelable{
	
	@DatabaseField(generatedId = true)
	public int id;
	@DatabaseField
	public String url;
	@DatabaseField(foreign = true)
	private Chapter chapter;
	
	public Link() {
		// needed by ormlite
	}
	
	public Link(String link, Chapter c) {
		url = link;
		chapter = c;
	}
	
	public Link(Parcel in) {
		id = in.readInt();
		url = in.readString();
		chapter = in.readParcelable(getClass().getClassLoader());
	}
	
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(url);
		dest.writeParcelable(chapter, flags);
	}
	
	public static final Parcelable.Creator<Link> CREATOR = new Parcelable.Creator<Link>() {
		public Link createFromParcel(Parcel in) {
			return new Link(in);
		}
		
		public Link[] newArray(int size) {
		    return new Link[size];
		}
	};
	
}

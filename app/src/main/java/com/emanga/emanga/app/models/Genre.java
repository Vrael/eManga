package com.emanga.emanga.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Locale;

@DatabaseTable
public class Genre implements Parcelable {

	public static final String NAME_COLUMN_NAME = "name";

	@DatabaseField(id = true, columnName = NAME_COLUMN_NAME)
	public String name;
	
	public Genre() {
		// needed by ormlite
	}

    public Genre(Parcel p){
        name = p.readString();
    }
	
	public Genre(String title) {
		name = title.toLowerCase(Locale.ENGLISH);
	}

	@Override
	public String toString(){
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

    public static final Parcelable.Creator<Genre> CREATOR = new Creator<Genre>() {
        @Override
        public Genre createFromParcel(Parcel p){
            return new Genre(p);
        }

        @Override
        public Genre[] newArray(int size) {
            return new Genre[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int i) {
        p.writeString(name);
    }
}

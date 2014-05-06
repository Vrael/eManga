package com.emanga.emanga.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Locale;

/**
 * Created by Ciro on 19/04/2014.
 */
@DatabaseTable
public class Author implements Parcelable {

    public static final String NAME_COLUMN_NAME = "name";

    @DatabaseField(id = true, columnName = NAME_COLUMN_NAME)
    public String name;

    public Author() {
        // needed by ormlite
    }

    public Author(Parcel p){
        name = p.readString();
    }

    public Author(String name) {
        this.name = name.toLowerCase(Locale.ENGLISH);
    }

    @Override
    public String toString(){
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public static final Parcelable.Creator<Author> CREATOR = new Parcelable.Creator<Author>() {
        @Override
        public Author createFromParcel(Parcel p){
            return new Author(p);
        }

        @Override
        public Author[] newArray(int size) {
            return new Author[size];
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

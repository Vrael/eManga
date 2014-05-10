package com.emanga.emanga.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.emanga.emanga.app.deserializers.PageDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@JsonDeserialize(using = PageDeserializer.class)
@DatabaseTable
public class Page implements Parcelable {
    public static final String ID_COLUMN_NAME = "_id";
    public static final String NUMBER_COLUMN_NAME = "number";
    public static final String CHAPTER_COLUMN_NAME = "chapter_id";
    public static final String LINK_COLUMN_NAME = "link";
    public static final String READ_COLUMN_NAME = "read";

    @DatabaseField(id = true, columnName = ID_COLUMN_NAME)
    public String _id;
    @DatabaseField(index = true, columnName = NUMBER_COLUMN_NAME)
    public int number;
    @DatabaseField(columnName = READ_COLUMN_NAME)
    public Date read;
    @DatabaseField(columnName = LINK_COLUMN_NAME)
    public String url;
    @DatabaseField(foreign = true, columnName = CHAPTER_COLUMN_NAME)
    public Chapter chapter;

    public Page(){
        // needed by ormlite
    }

    public Page(String _id, int number, String url, Chapter chapter) {
        this._id = _id;
        this.number = number;
        this.chapter = chapter;
        this.url = url;
    }

    public Page(Parcel p){
        this._id = p.readString();
        this.number = p.readInt();
        long date = p.readLong();
        if (date != 0L)
            this.read = new Date(date);
        this.url = p.readString();
        // this.chapter = (Chapter) p.readValue(Chapter.class.getClassLoader());
    }

    public static final Parcelable.Creator<Page> CREATOR = new Creator<Page>() {
        @Override
        public Page createFromParcel(Parcel p){
            return new Page(p);
        }

        @Override
        public Page[] newArray(int size) {
            return new Page[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int i) {
        p.writeString(_id);
        p.writeInt(number);
        p.writeLong(read != null? read.getTime(): 0L);
        p.writeString(url);
        // p.writeValue(chapter);
    }

    public String toString(){
        return _id + " - " + number + " - " + ( (read != null)? read.toString() + "\n" : "--- \n" );
    }
}

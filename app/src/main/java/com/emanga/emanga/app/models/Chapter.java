package com.emanga.emanga.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.emanga.emanga.app.deserializers.ChapterDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@JsonDeserialize(using = ChapterDeserializer.class)
@DatabaseTable
public class Chapter implements Parcelable{
	public static final String ID_COLUMN_NAME = "_id";
	public static final String DATE_COLUMN_NAME = "created_at";
	public static final String NUMBER_COLUMN_NAME = "number";
	public static final String MANGA_COLUMN_NAME = "manga_id";
    public static final String CHAPTERS_COLUMN_NAME = "pages";
    public static final String READ_COLUMN_NAME = "read";
	
	@DatabaseField(id = true, columnName = ID_COLUMN_NAME)
	public String _id;
	@DatabaseField(index = true, columnName = NUMBER_COLUMN_NAME)
	public Integer number;
    @DatabaseField(columnName = DATE_COLUMN_NAME, dataType = DataType.DATE_LONG)
    public Date created_at;
    @DatabaseField(columnName = READ_COLUMN_NAME)
    public Date read;
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = MANGA_COLUMN_NAME)
	public Manga manga;
    @ForeignCollectionField(columnName = CHAPTERS_COLUMN_NAME)
    public Collection<Page> pages;

	public Chapter() {
		// needed by ormlite
	}

    public Chapter(Parcel p){
        this._id =  p.readString();
        this.number = (Integer) p.readValue(Integer.class.getClassLoader());
        this.created_at = new Date(p.readLong());
        this.manga = (Manga) p.readValue(Manga.class.getClassLoader());
        this.pages = unparcelCollection(p, Page.CREATOR);

        if(this.pages != null){
            Iterator<Page> it = this.pages.iterator();
            while (it.hasNext()){
                it.next().chapter = this;
            }
        }
    }

    public Chapter(String _id, Integer number, Date created_at, Manga manga) {
        this._id = _id;
        this.number = number;
        this.created_at = created_at;
        this.manga = manga;
    }

    public String toString(){
        return _id + "\n" + number + "\n" + created_at.toString() + "\n" + ((manga != null)? manga._id : "null" + "\n" + pages);
    }

    public static final Parcelable.Creator<Chapter> CREATOR = new Creator<Chapter>() {
        @Override
        public Chapter createFromParcel(Parcel p){
            return new Chapter(p);
        }

        @Override
        public Chapter[] newArray(int size) {
            return new Chapter[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int i) {
        p.writeString(_id);
        p.writeValue(number);
        p.writeLong(created_at.getTime());
        p.writeValue(manga);
        parcelCollection(p,pages);
    }

    protected final static <T extends Page> void parcelCollection(final Parcel out, final Collection<T> collection) {
        if (collection != null) {
            out.writeInt(collection.size());
            out.writeTypedList(new ArrayList<T>(collection));
        } else {
            out.writeInt(-1);
        }
    }

    protected final static <T extends Page> Collection<T> unparcelCollection(final Parcel in, final Creator<T> creator) {
        final int size = in.readInt();
        if (size >= 0) {
            final List<T> list = new ArrayList<T>(size);
            in.readTypedList(list, creator);
            return list;
        } else {
            return null;
        }
    }
}

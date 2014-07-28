package com.emanga.emanga.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.emanga.emanga.app.deserializers.MangaDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@JsonDeserialize(using = MangaDeserializer.class)
@DatabaseTable
public class Manga implements Parcelable{
    public static final String MODEL_NAME = "manga";
	public static final String ID_COLUMN_NAME = "_id";
	public static final String TITLE_COLUMN_NAME = "title";
	public static final String COVER_COLUMN_NAME = "cover";
	public static final String DESCRIPTION_COLUMN_NAME = "summary";
    public static final String DATE_COLUMN_NAME = "created_at";
    public static final String MODIFY_COLUMN_NAME = "modified_at";
    public static final String CHAPTERS_COLUMN_NAME = "chapters";
    public static final String NUMBER_CHAPTERS_COLUMN_NAME = "number_chapters";
    public static final String FAVOURITE_COLUMN_NNAME = "manga";

    @DatabaseField(id = true, columnName = ID_COLUMN_NAME)
    public String _id;
	@DatabaseField(columnName = TITLE_COLUMN_NAME)
	public String title;
    @DatabaseField(columnName = COVER_COLUMN_NAME)
    @JsonProperty("covers")
    public String cover;
	@DatabaseField(columnName = DESCRIPTION_COLUMN_NAME)
	public String summary;
    @DatabaseField(columnName = DATE_COLUMN_NAME)
    public Date created_at;
    @DatabaseField(columnName = MODIFY_COLUMN_NAME)
    public Date modified_at;
	@ForeignCollectionField(columnName = CHAPTERS_COLUMN_NAME)
	public Collection<Chapter> chapters;
    @DatabaseField(columnName = NUMBER_CHAPTERS_COLUMN_NAME)
    public int numberChapters;
    @DatabaseField(columnName = FAVOURITE_COLUMN_NNAME)
    public boolean favourite = false;

	
	// Handle genres for N - N relationship in GenreManga
	public List<Genre> genres;

    // Handle authors for N - N relationship in AuthorManga
    public List<Author> authors;
	
	public Manga() {
		// needed by ormlite
	}

    public Manga(Parcel p){
        this._id = p.readString();
        this.title = p.readString();
        this.cover = p.readString();
        this.summary = p.readString();
        this.created_at = new Date(p.readLong());
        this.modified_at = new Date(p.readLong());
        // this.chapters = unparcelCollection(p, Chapter.CREATOR);
        // p.readList(this.genres, Genre.class.getClassLoader());
    }

    public Manga(String _id, String title, String cover, Date created_at, Date modified_at){
        this._id = _id;
        this.title = title != null? title.replaceAll("[^a-zñ0-9 ]","") : null;
        this.cover = cover;
        this.created_at = created_at;
        this.modified_at = modified_at;
        this.numberChapters = 0;
    }

	public Manga(String _id, String title, String cover, String summary, Date created_at,
                 Date modified_at, List<Genre> genres) {
        this(_id, title, cover, created_at, modified_at);
        this.summary = summary;
		this.genres = genres;
	}

    public Manga(String _id, String title, List<Author> authors, String cover, String summary,
                 Date created_at, Date modified_at, List<Genre> genres, int nChapters){
        this(_id, title, cover, summary, created_at, modified_at, genres);
        this.numberChapters = nChapters;
        this.authors = authors;
    }

    public String toString(){
        return _id + "\n"
                + ((title != null)? title + "\n": "")
                + ((cover != null)? cover + "\n": "")
                + ((summary != null)? summary + "\n": "")
                + ((created_at != null)? created_at.toString() + "\n": "")
                + ((genres != null)? genres: "") + "\n"
                + numberChapters  + "\n";
    }

    public static final Parcelable.Creator<Manga> CREATOR = new Creator<Manga>() {
        @Override
        public Manga createFromParcel(Parcel p){
            return new Manga(p);
        }

        @Override
        public Manga[] newArray(int size) {
            return new Manga[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int i) {
        p.writeString(_id);
        p.writeString(title);
        p.writeString(cover);
        p.writeString(summary);
        if(created_at != null)
            p.writeLong(created_at.getTime());
        if(modified_at != null)
            p.writeLong(modified_at.getTime());
        // parcelCollection(p, null);
        // p.writeList(genres);
    }

    protected final static <T extends Chapter> void parcelCollection(final Parcel out, final Collection<T> collection) {
        if (collection != null) {
            out.writeInt(collection.size());
            out.writeTypedList(new ArrayList<T>(collection));
        } else {
            out.writeInt(-1);
        }
    }

    protected final static <T extends Chapter> Collection<T> unparcelCollection(final Parcel in, final Creator<T> creator) {
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

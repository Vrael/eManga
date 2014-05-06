package com.emanga.emanga.app.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Ciro on 19/04/2014.
 */
@DatabaseTable
public class AuthorManga {

    public static final String ID_COLUMN_NAME = "_id";
    public static final String AUTHOR_COLUMN_NAME = "author_id";
    public static final String MANGA_COLUMN_NAME = "manga_id";

    @DatabaseField(id = true, columnName = ID_COLUMN_NAME)
    public String _id;
    @DatabaseField(foreign = true, columnName = AUTHOR_COLUMN_NAME)
    public Author author;
    @DatabaseField(foreign = true, columnName = MANGA_COLUMN_NAME)
    public Manga manga;

    public AuthorManga() {
        //needed by ormlite
    }

    public AuthorManga(Author author, Manga manga) {
        this._id = author.name + "_" + manga.title;
        this.author = author;
        this.manga = manga;
    }
}
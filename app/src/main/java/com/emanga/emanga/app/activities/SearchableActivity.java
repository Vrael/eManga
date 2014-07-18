package com.emanga.emanga.app.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.emanga.emanga.app.R;
import com.emanga.emanga.app.adapters.MangaItemListCursorAdapter;
import com.emanga.emanga.app.database.OrmliteActionBarActivity;
import com.emanga.emanga.app.models.Genre;
import com.emanga.emanga.app.models.Manga;

/**
 * Created by Ciro on 29/05/2014.
 */
public class SearchableActivity extends OrmliteActionBarActivity implements AdapterView.OnItemClickListener {
    public static final String TAG = SearchableActivity.class.getSimpleName();

    public static final String EXTRA_MANGA = TAG + ".EXTRA_MANGA";

    private MangaItemListCursorAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            mAdapter = new MangaItemListCursorAdapter(
                    this,
                    R.layout.manga_item_list,
                    getHelper().searchOnLibrary(query),
                    new String[]{Manga.TITLE_COLUMN_NAME, Genre.NAME_COLUMN_NAME},
                    new int[]{R.id.manga_list_title, R.id.manga_list_categories},
                    SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

            ListView results = (ListView) findViewById(android.R.id.list);
            results.setAdapter(mAdapter);
            results.setEmptyView(findViewById(android.R.id.empty));
            results.setOnItemClickListener(this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
        cursor.moveToPosition(i);
        Manga manga = new Manga(
                cursor.getString(cursor.getColumnIndex(Manga.ID_COLUMN_NAME)),
                cursor.getString(cursor.getColumnIndex(Manga.TITLE_COLUMN_NAME)),
                cursor.getString(cursor.getColumnIndex(Manga.COVER_COLUMN_NAME)),
                null,
                null);

        Intent intent = new Intent(MainActivity.ACTION_OPEN_MANGA, null, this, MainActivity.class);
        intent.putExtra(EXTRA_MANGA, manga);
        startActivity(intent);
    }
}

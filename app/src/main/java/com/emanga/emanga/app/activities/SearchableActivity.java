package com.emanga.emanga.app.activities;

import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.emanga.emanga.app.R;
import com.emanga.emanga.app.adapters.MangaItemListAdapter;
import com.emanga.emanga.app.adapters.MangaItemListCursorAdapter;
import com.emanga.emanga.app.database.OrmliteActionBarActivity;
import com.emanga.emanga.app.fragments.LibrarySectionFragment;
import com.emanga.emanga.app.fragments.MangaDetailFragment;
import com.emanga.emanga.app.fragments.MangaListFragment;
import com.emanga.emanga.app.models.Genre;
import com.emanga.emanga.app.models.Manga;

/**
 * Created by Ciro on 29/05/2014.
 */
public class SearchableActivity extends OrmliteActionBarActivity
        implements MangaListFragment.Callbacks {
    public static final String TAG = SearchableActivity.class.getSimpleName();

    public static final String EXTRA_MANGA = TAG + ".EXTRA_MANGA";

    private MangaListFragment mListFragment;
    private MangaItemListCursorAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manga_list);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mListFragment = (MangaListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.manga_list);

        mAdapter = new MangaItemListCursorAdapter(
                this,
                R.layout.manga_item_list,
                getHelper().getMangasWithGenres(),
                new String[]{Manga.TITLE_COLUMN_NAME, Genre.NAME_COLUMN_NAME},
                new int[]{R.id.manga_list_title, R.id.manga_list_categories},
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        mListFragment.setListAdapter(mAdapter);

        mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence constraint) {
                return getHelper().searchOnLibrary(constraint.toString());
            }
        });

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            Filter filter = mAdapter.getFilter();
            filter.filter(query.replaceAll("[^a-zA-ZñÑ0-9 ]",""));
            mAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onItemSelected(Manga manga) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(MangaDetailFragment.ARG_MANGA, manga);
        MangaDetailFragment fragment = new MangaDetailFragment();
        fragment.setArguments(arguments);

        if (LibrarySectionFragment.mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.manga_detail_container, fragment)
                    .commit();
        }
        else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.manga_list, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

}

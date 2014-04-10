package com.emanga.emanga.app.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.emanga.emanga.app.R;
import com.emanga.emanga.app.activities.ReaderActivity;
import com.emanga.emanga.app.adapters.ThumbnailChapterAdapter;
import com.emanga.emanga.app.controllers.App;
import com.emanga.emanga.app.database.OrmliteFragment;
import com.emanga.emanga.app.loaders.LatestChaptersLoader;
import com.emanga.emanga.app.models.Chapter;
import com.emanga.emanga.app.models.Manga;
import com.emanga.emanga.app.requests.MangasRequest;
import com.emanga.emanga.app.utils.Internet;
import com.emanga.emanga.app.utils.Notification;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * A fragment that with Latest Chapters of Manga
 */
public class LatestSectionFragment extends OrmliteFragment
        implements LoaderManager.LoaderCallbacks<List<Chapter>> {

    public static final String TAG = LatestSectionFragment.class.getSimpleName();

    private ThumbnailChapterAdapter mAdapter;
    private ProgressBar bar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ThumbnailChapterAdapter(getActivity());

        String chapterDate = getHelper().lastChapterDate();
        try {
            chapterDate = URLEncoder.encode(chapterDate, "utf-8");
            Log.d(TAG,Internet.HOST + "chapters/newest?&c=" + chapterDate);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        bar = (ProgressBar) getActivity().findViewById(R.id.progressbar_background_tasks);
        bar.setProgress(1);

        MangasRequest latestChaptersRequest = new MangasRequest(
                Request.Method.GET,
                Internet.HOST + "chapters/newest?&c=" + chapterDate,
                new Response.Listener<Manga[]>() {
                    @Override
                    public void onResponse(final Manga[] mangas){
                        new AsyncTask<Void,Void,Void>(){
                            @Override
                            protected Void doInBackground(Void... voids) {
                                Log.d(TAG, "Mangas received and parsed: " + mangas.length);
                                bar.setProgress(35);
                                getHelper().saveMangas(mangas);
                                bar.setProgress(80);
                                Log.d(TAG, "Notify new chapters in the database");
                                getLoaderManager().getLoader(0).onContentChanged();
                                bar.setProgress(100);
                                return null;
                            }
                        }.execute();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.d(TAG, "Error in response!");
                        Log.d(TAG, volleyError.toString());
                        Notification.errorMessage(getActivity(),
                                getResources().getString(R.string.volley_error_title),
                                getResources().getString(R.string.volley_error_body),
                                R.drawable.sorry);
                        bar.setProgress(100);
                    }
                });

        latestChaptersRequest.setRetryPolicy(new DefaultRetryPolicy(
                        30 * 1000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );

        latestChaptersRequest.setTag("Request: Latest Chapters");
        App.getInstance().mRequestQueue.add(latestChaptersRequest);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_section, container, false);

        GridView gridview = (GridView) rootView.findViewById(R.id.grid_view);
        gridview.setAdapter(mAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if(Internet.checkConnection(getActivity())) {
                    Intent intent = new Intent(getActivity(), ReaderActivity.class);
                    Chapter chapter = mAdapter.getItem(position);
                    intent.putExtra(ReaderActivity.ACTION_OPEN_CHAPTER, chapter);

                    Notification.enjoyReading(getActivity()).show();
                    startActivity(intent);
                } else {
                    Notification.errorMessage(
                            getActivity(),
                            getResources().getString(R.string.volley_error_title),
                            getResources().getString(R.string.connectivity_error_body),
                            R.drawable.alone
                    );
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    public Loader<List<Chapter>> onCreateLoader(int id, Bundle args) {
        return new LatestChaptersLoader(getActivity());
    }

    public void onLoadFinished(Loader<List<Chapter>> loader, List<Chapter> chapters) {
        mAdapter.addChapters(chapters);
    }

    public void onLoaderReset(Loader<List<Chapter>> chapters) {
        mAdapter.setChapters(null);
    }

}
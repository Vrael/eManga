package com.emanga.emanga.app.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.emanga.emanga.app.R;
import com.emanga.emanga.app.activities.MainActivity;
import com.emanga.emanga.app.activities.ReaderActivity;
import com.emanga.emanga.app.adapters.ThumbnailChapterAdapter;
import com.emanga.emanga.app.controllers.App;
import com.emanga.emanga.app.database.DatabaseHelper;
import com.emanga.emanga.app.database.OrmliteFragment;
import com.emanga.emanga.app.models.Chapter;
import com.emanga.emanga.app.models.Manga;
import com.emanga.emanga.app.requests.MangasRequest;
import com.emanga.emanga.app.utils.Dates;
import com.emanga.emanga.app.utils.Internet;
import com.emanga.emanga.app.utils.Notification;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A fragment that with Latest Chapters of Manga
 */
public class LatestSectionFragment extends OrmliteFragment {

    public static final String TAG = LatestSectionFragment.class.getName();

    private ThumbnailChapterAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ThumbnailChapterAdapter(getActivity());

        String chapterDate = getHelper().lastChapterDate();
        Date date = null;
        if(!chapterDate.equals(""))
            date = new Date(Long.valueOf(chapterDate));

        try {
            if(date != null)
                chapterDate = URLEncoder.encode(Dates.sdf.format(date), "utf-8");
            else
                chapterDate = URLEncoder.encode("", "utf-8");
            Log.d(TAG,Internet.HOST + "chapters/newest?&c=" + chapterDate);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Load chapters from database
        new AsyncTask<Void,Integer,List<Chapter>>(){
            @Override
            protected List<Chapter> doInBackground(Void... voids) {
                RuntimeExceptionDao<Chapter, String> chapterDao;
                QueryBuilder<Chapter, String> qBcLocal;

                Calendar time = Calendar.getInstance();
                time.add(Calendar.getInstance().DATE, -7);

                chapterDao = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class)
                        .getChapterRunDao();

                List<Chapter> chapters = null;
                try {
                    qBcLocal = chapterDao.queryBuilder();
                    qBcLocal.where().ge(Chapter.DATE_COLUMN_NAME, time.getTime());
                    qBcLocal.groupBy(Chapter.MANGA_COLUMN_NAME).having("MAX(" + Chapter.DATE_COLUMN_NAME + ")");
                    qBcLocal.orderBy(Chapter.DATE_COLUMN_NAME, true);
                    chapters = qBcLocal.query();
                } catch (SQLException e) {
                    Log.e(TAG, "Error when it was building the chapters query");
                    e.printStackTrace();
                }

                return chapters;
            }

            @Override
            public void onPostExecute(List<Chapter> chapters){
                if(chapters != null && chapters.size() > 0) {
                    mAdapter.addChapters(chapters);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }.execute();

        MangasRequest latestChaptersRequest = new MangasRequest(
                Request.Method.GET,
                Internet.HOST + "chapters/newest?&c=" + chapterDate,
                new Response.Listener<Manga[]>() {
                    @Override
                    public void onResponse(final Manga[] mangas){
                        Log.d(TAG, "Mangas received and parsed: " + mangas.length);
                        for(Manga m: mangas){
                            mAdapter.addChapters(m.chapters);
                        }
                        mAdapter.notifyDataSetChanged();

                        // Notify for hide the progressbar
                        LocalBroadcastManager.getInstance(App.getInstance().getApplicationContext())
                                .sendBroadcast(new Intent(MainActivity.ACTION_TASK_ENDED));

                        new AsyncTask<Void,Void,Void>(){
                            @Override
                            protected Void doInBackground(Void... voids) {
                                getHelper().saveMangas(mangas);
                                return null;
                            }
                        }.execute();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        // Notify for hide the progressbar
                        LocalBroadcastManager.getInstance(App.getInstance().getApplicationContext())
                                .sendBroadcast(new Intent(MainActivity.ACTION_TASK_ENDED));

                        Log.e(TAG, "Error in response!");
                        Log.e(TAG, volleyError.toString());
                        Notification.errorMessage(getActivity(),
                                getResources().getString(R.string.volley_error_title),
                                getResources().getString(R.string.volley_error_body),
                                R.drawable.sorry);
                    }
                });

        latestChaptersRequest.setRetryPolicy(new DefaultRetryPolicy(
                        30 * 1000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );

        App.getInstance().addToRequestQueue(latestChaptersRequest, "Latest Chapters");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_section, parent, false);

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
}
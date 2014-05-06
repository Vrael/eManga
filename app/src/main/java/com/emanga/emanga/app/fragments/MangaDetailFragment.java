package com.emanga.emanga.app.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.emanga.emanga.app.R;
import com.emanga.emanga.app.activities.ReaderActivity;
import com.emanga.emanga.app.cache.ImageCacheManager;
import com.emanga.emanga.app.controllers.App;
import com.emanga.emanga.app.database.OrmliteFragment;
import com.emanga.emanga.app.models.Author;
import com.emanga.emanga.app.models.Chapter;
import com.emanga.emanga.app.models.Genre;
import com.emanga.emanga.app.models.GenreManga;
import com.emanga.emanga.app.models.Manga;
import com.emanga.emanga.app.requests.MangaRequest;
import com.emanga.emanga.app.utils.CoverNetworkImageView;
import com.emanga.emanga.app.utils.Internet;
import com.emanga.emanga.app.utils.Notification;
import com.j256.ormlite.stmt.QueryBuilder;

import org.apache.commons.lang.WordUtils;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Iterator;

/**
 * A fragment representing a single Manga detail screen. This fragment is either
 * contained in a {@link com.emanga.emanga.app.fragments.MangaListFragment} in two-pane mode (on tablets).
 */
public class MangaDetailFragment extends OrmliteFragment {
	public static final String TAG = MangaDetailFragment.class.getSimpleName();
    /**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_MANGA_ID = "manga_id";

	/**
	 * The manga content this fragment is presenting.
	 */
	private Manga manga;
    private MangaRequest request;

    private Chapter lastChapterRead;

    private ViewHolder holder;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MangaDetailFragment() {}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getArguments().containsKey(ARG_MANGA_ID)) {
            String mangaId = getArguments().getString(ARG_MANGA_ID);
            manga = getHelper().getMangaRunDao().queryForId(mangaId);
            try {
                // Query for genres
                QueryBuilder<Genre, String> qBg = getHelper()
                        .getGenreRunDao().queryBuilder();
                QueryBuilder<GenreManga, String> qBgm = getHelper()
                        .getGenreMangaRunDao().queryBuilder();
                qBgm.where().eq(GenreManga.MANGA_COLUMN_NAME, manga._id);
                qBg.join(qBgm);
                manga.genres = qBg.query();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            request = new MangaRequest(
                Request.Method.GET,
                Internet.HOST + "manga/" + manga._id,
                new Response.Listener<Manga>(){
                    @Override
                    public void onResponse(Manga response) {
                        Log.d(TAG, "Manga details: " + manga.toString());
                        getHelper().getMangaRunDao().update(response);
                        manga = response;
                        updateValues();
                    }
                },
                null
            );

            App.getInstance().mRequestQueue.add(request);
        }
	}

    public void onResume(){
        super.onResume();
        if(manga != null){
            try {
                // Query for the last read chapter
                QueryBuilder<Chapter, String> qBc = getHelper()
                        .getChapterRunDao().queryBuilder();
                qBc.where().eq(Chapter.MANGA_COLUMN_NAME, manga._id).and().isNotNull(Chapter.READ_COLUMN_NAME);
                qBc.orderBy(Chapter.READ_COLUMN_NAME, false);
                qBc.limit(1L);

                lastChapterRead = qBc.queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Show resume button if there is a last chapter read
        if(lastChapterRead != null){
            ImageButton buttonContinue = holder.resume;
            buttonContinue.setVisibility(View.VISIBLE);
            buttonContinue.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if(Internet.checkConnection(getActivity())) {
                        Intent intent = new Intent(getActivity(), ReaderActivity.class);
                        intent.putExtra(ReaderActivity.ACTION_OPEN_CHAPTER, lastChapterRead);

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

            holder.start.setImageResource(R.drawable.ic_action_replay);

            holder.last_read.setText(SimpleDateFormat.getInstance().format(lastChapterRead.read));
            holder.last_read.getLayoutParams().height = 30;

            holder.percent.setText(calculatePercent(manga.numberChapters, lastChapterRead.number) + "%");
        }
    }

    private static class ViewHolder {
        TextView title;
        TextView author;
        CoverNetworkImageView cover;
        TextView numberChapters;
        TextView percent;
        TextView last_read;
        TableLayout genres;
        TextView summary;
        ImageButton start;
        ImageButton resume;
        ImageButton go;
    }

    private void updateValues(){
        holder.numberChapters.setText(manga.numberChapters + "");

        holder.cover.setImageUrl(manga, ImageCacheManager.getInstance().getImageLoader());

        if(!request.hasHadResponseDelivered()){
            holder.summary.setText(
                    (manga.summary == null || manga.summary.isEmpty()) ? getResources().getString(R.string.loading) : manga.summary);
        } else {
            holder.summary.setText(
                    (manga.summary == null || manga.summary.isEmpty()) ? "" : manga.summary);
        }

        if(manga.authors != null && manga.authors.size() > 0){
            Iterator<Author> it = manga.authors.iterator();
            String authors = "";
            if (it.hasNext()) {
                authors = it.next().name;
            }
            while (it.hasNext()) {
                authors += "\n" + it.next().name;
            }
            holder.author.setText(authors);
            holder.author.getLayoutParams().height = 50;
        }

        if(manga.genres != null && manga.genres.size() > 0){
            holder.genres.removeAllViews();
            TableRow tableRow = null;
            for(int i = 0; i < manga.genres.size(); i++){
                if(i % 4 == 0){
                    tableRow = new TableRow(getActivity());
                    holder.genres.addView(tableRow);
                }
                TextView label = new TextView(getActivity());
                label.setPadding(20,0,20,0);
                label.setText(Html.fromHtml(WordUtils.capitalize(manga.genres.get(i).name.trim())));
                tableRow.addView(label);
            }
        }

        if(lastChapterRead != null) {
            holder.percent.setText(calculatePercent(manga.numberChapters, lastChapterRead.number) + "%");
        }
    }

    private static int calculatePercent(int total, int part){
        Log.d(TAG, "Total: " + total + ", Part: " + part);
        return (total > 0)? part * 100 / total : 0;
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_manga_detail,
                container, false);

        if(holder == null){
            holder = new ViewHolder();
            holder.title = (TextView) rootView.findViewById(R.id.manga_title);
            holder.author = (TextView) rootView.findViewById(R.id.manga_author);
            holder.numberChapters = (TextView) rootView.findViewById(R.id.manga_chapters);
            holder.cover = (CoverNetworkImageView) rootView.findViewById(R.id.manga_cover);
            holder.percent = (TextView) rootView.findViewById(R.id.manga_read);
            holder.last_read = (TextView) rootView.findViewById(R.id.manga_last_read_date);
            holder.genres = (TableLayout) rootView.findViewById(R.id.manga_categories_table);
            holder.summary = (TextView) rootView.findViewById(R.id.manga_description);
            holder.resume = (ImageButton) rootView.findViewById(R.id.manga_button_continue);
            holder.start = (ImageButton) rootView.findViewById(R.id.manga_button_start);
            holder.go = (ImageButton) rootView.findViewById(R.id.manga_button_go);
        }

        holder.title.setText(manga.title);
        updateValues();

        holder.start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(Internet.checkConnection(getActivity())) {
                    Intent intent = new Intent(getActivity(), ReaderActivity.class);
                    intent.putExtra(ReaderActivity.ACTION_OPEN_MANGA, manga);

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

        holder.go.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(Internet.checkConnection(getActivity())) {
                    goToChapterDialog(getActivity());
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
    public void onDestroy(){
        super.onDestroy();
        if(request != null)
            request.cancel();
    }

    public void goToChapterDialog(Activity activity){
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        final View layout = layoutInflater.inflate(R.layout.dialog_go_to_chapter, null);

        TextView maxNumber = (TextView) layout.findViewById(R.id.dialog_max_chapter_number);
        Log.d(TAG, "Max chapters: " + manga.numberChapters);
        maxNumber.setText("/ " + manga.numberChapters);

        final EditText inputNumber = (EditText) layout.findViewById(R.id.dialog_go_to_chapter_number);

        final AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(layout)
                .setTitle(activity.getResources().getString(R.string.go_to_chapter_label))
                .setIcon(R.drawable.go)
                .setPositiveButton(activity.getResources().getString(R.string.go_to_chapter_button_go), null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int number = Integer.parseInt(inputNumber.getText().toString());
                if(number > manga.numberChapters){
                    inputNumber.setBackgroundResource(R.drawable.input_error);
                    return;
                }

                Intent intent = new Intent(getActivity(), ReaderActivity.class);
                intent.putExtra(ReaderActivity.ACTION_OPEN_MANGA, manga);
                intent.putExtra(ReaderActivity.ACTION_OPEN_CHAPTER_NUMBER, number);

                dialog.dismiss();
                Notification.enjoyReading(getActivity()).show();
                startActivity(intent);
            }
        });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        // Hide the keyboard if it is active
                        // if(inputSearch.isActivated()){
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(inputNumber.getWindowToken(), 0);

                        dialog.dismiss();
                    }
                });
    }
}

package com.emanga.emanga.app.activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.emanga.emanga.app.R;
import com.emanga.emanga.app.controllers.App;
import com.emanga.emanga.app.database.OrmliteFragmentActivity;
import com.emanga.emanga.app.fragments.ChapterPageFragment;
import com.emanga.emanga.app.models.Chapter;
import com.emanga.emanga.app.models.Manga;
import com.emanga.emanga.app.models.Page;
import com.emanga.emanga.app.requests.ChapterRequest;
import com.emanga.emanga.app.requests.MangaRequest;
import com.emanga.emanga.app.utils.CustomViewPager;
import com.emanga.emanga.app.utils.Internet;
import com.emanga.emanga.app.utils.Notification;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 */
public class ReaderActivity extends OrmliteFragmentActivity {

	public static final String TAG = "ReaderActivity";

	public static final String ACTION_OPEN_CHAPTER = "com.emanga.emanga.app.intent.action"
            + TAG + ".openChapter";
    public static final String ACTION_OPEN_CHAPTER_NUMBER = "com.emanga.emanga.app.intent.action"
            + TAG + ".openChapterNumber";
    public static final String ACTION_OPEN_MANGA = "com.emanga.emanga.app.intent.action"
			+ TAG + ".openManga";
	
	// Current manga and chapter
    private Page mark;
	private Chapter mChapter;
    private Manga mManga;
	private ImagePagerAdapter mAdapter;
	private CustomViewPager mPager;

    private ChapterRequest request;
    private Boolean asked;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        if(getIntent().getExtras().get(ACTION_OPEN_CHAPTER) != null) {
            mChapter = (Chapter) getIntent().getExtras().get(ACTION_OPEN_CHAPTER);
            mManga = mChapter.manga;

            if(mManga.numberChapters == 0){
                App.getInstance().addToRequestQueue(new MangaRequest(Request.Method.GET,
                        Internet.HOST + "manga/" + mManga._id,
                        new Response.Listener<Manga>(){
                            @Override
                            public void onResponse(Manga response) {
                                Log.d(TAG, "Manga details: " + mManga.toString());
                                UpdateBuilder<Manga, String> updateBuilder = getHelper().getMangaRunDao().updateBuilder();
                                try {
                                    updateBuilder.where().eq(Manga.ID_COLUMN_NAME, response._id);
                                    updateBuilder.updateColumnValue(Manga.NUMBER_CHAPTERS_COLUMN_NAME, response.numberChapters);
                                    updateBuilder.update();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                                mManga = response;
                            }
                        },
                        null
                ), "Manga info");
            }

            try {
                // Query for the last page read
                QueryBuilder<Page, String> qBp = getHelper()
                        .getPageRunDao().queryBuilder();
                qBp.where().eq(Page.CHAPTER_COLUMN_NAME, mChapter._id);
                qBp.orderBy(Page.READ_COLUMN_NAME, false);
                qBp.limit(1L);
                mark = qBp.queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (mark != null) {
                Log.d(TAG, "Last page read: " + mark.toString());
            } else {
                Log.d(TAG, "There is not a last page read");
            }

            askChapter(mChapter.number);
        } else {
            mManga = (Manga) getIntent().getExtras().get(ACTION_OPEN_MANGA);
            askChapter(getIntent().getExtras().getInt(ACTION_OPEN_CHAPTER_NUMBER,1));
        }

        setContentView(R.layout.activity_reader);
        mPager = (CustomViewPager) findViewById(R.id.fullscreen_pager);

		mAdapter = new ImagePagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mAdapter);
		
		mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
				// Save when a page is read
				Page page = mAdapter.pages.get(position);
                Log.d(TAG, "Saving " + page.number + " as mark");
                page.read = new Date();
                getHelper().getPageRunDao().createOrUpdate(page);

                if(mChapter.number < mManga.numberChapters){
                    // When only rest 5 pages for the end, it loads the next chapter
                    // Position begins from 0 whereas that size() does it from 1
                    if((mAdapter.pages.size() - 1) - position < 5 && !asked ){
                        askChapter(mChapter.number + 1);
                    }
                } else {
                    // First page of last chapter
                    if((mAdapter.pages.size() - 1) - position == (mChapter.pages.size() - 1)){
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.messasge_last_chapter), Toast.LENGTH_LONG).show();
                    } else if((mAdapter.pages.size() - 1) - position < 1){  // Last page of the last chapter
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.messasge_last_page_last_chapter), Toast.LENGTH_LONG).show();
                    }
                }
			}

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
	}

	// Adapter for fragments which contains the ImageViews children
    private static class ImagePagerAdapter extends FragmentStatePagerAdapter {
        public LinkedList<Page> pages = new LinkedList<Page>();

        public ImagePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return pages.size();
        }
        
        @Override
        public Fragment getItem(int position) {
            return ChapterPageFragment.newInstance(pages.get(position));
        }
    }
	
	@Override
	public void onDestroy(){
        super.onDestroy();
        request.cancel();
	}

    private void askChapter(final int number){
        asked = true;
        final Activity activity = this;
        Log.d(TAG, "Request to url: " + Internet.HOST + "manga/" + mManga._id + "/chapter/" + number);
        request = new ChapterRequest(
                Request.Method.GET,
                Internet.HOST + "manga/" + mManga._id + "/chapter/" + number,
                new Response.Listener<Chapter>() {
                    @Override
                    public void onResponse(final Chapter chapter){
                        if(chapter._id != null){
                            Log.d(TAG, "Chapter recived:\n" + chapter.toString());
                            // Add new pages to adapter
                            mAdapter.pages.addAll(chapter.pages);
                            mChapter = chapter;
                            mAdapter.notifyDataSetChanged();

                            // Set as read the chapter
                            mChapter.read = new Date();
                            mChapter.manga = mManga;

                            // If is a resume reading
                            if(mark != null){
                                Log.d(TAG, "Set current page: " + (mark.number - 1) );
                                Log.d(TAG, "Page adapter has " + mAdapter.getCount() + " pages loaded");
                                mPager.setCurrentItem(mark.number - 1);
                                mark = null;
                            }

                            new AsyncTask<Void,Void,Void>(){
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    Log.d(TAG, "Saving chapter as read");
                                    getHelper().getChapterRunDao().createOrUpdate(mChapter);
                                    return null;
                                }
                            }.execute();

                            asked = false;
                        } else {
                            Toast.makeText(activity, getResources().getString(R.string.chapter_not_found_error) + ": " + number, Toast.LENGTH_LONG).show();
                            Log.d(TAG, "The chapter: " + number + " doesn't exist");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.d(TAG, "Error in response!");
                        Log.d(TAG, volleyError.toString());
                        Notification.errorMessage(activity,
                                getResources().getString(R.string.volley_error_title),
                                getResources().getString(R.string.volley_error_body),
                                R.drawable.nooo);
                        asked = false;
                    }
                });
        request.setTag("Request pages for chapter " + number);
        request.setRetryPolicy(new DefaultRetryPolicy(
            2 * 60 * 1000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );
        App.getInstance().mRequestQueue.add(request);
    }
}

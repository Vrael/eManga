package com.emanga.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.emanga.R;
import com.emanga.database.OrmliteFragmentActivity;
import com.emanga.fragments.ChapterPageFragment;
import com.emanga.models.Manga;
import com.emanga.services.PagesService;
import com.emanga.utils.CustomViewPager;
import com.emanga.utils.Internet;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class ReaderActivity extends OrmliteFragmentActivity {

	public static final String TAG = "ReaderActivity";
	
	public static final String INTENT_CHAPTER_ID = "chapterId";
	
	public static final String ACTION_OPEN_CHAPTER = "com.manga.intent.action" 
			+ TAG + ".openChapter";
	
	private int currentChapter;
	private ImagePagerAdapter mAdapter;
	private CustomViewPager mPager;
	
	protected AtomicReference<Integer> numberChapters; 
	
	// This Receiver updates urls of pages from Pages Service
	private BroadcastReceiver mPageReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	Log.d(TAG, "Received new url image from Page Service");
	    	String url = intent.getStringExtra(PagesService.EXTRA_PAGE_URL);
	    	if(url != null){
	    		mAdapter.pagesLinks.add(url);
	    		mAdapter.notifyDataSetChanged();
	    	}
	   }
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_reader);
		currentChapter = getIntent().getIntExtra(ACTION_OPEN_CHAPTER, 0);
		
		// Search the number chapters of the manga
		// new NumberChapters().execute(getIntent().getIntExtra(Manga.ID_COLUMN_NAME, 0));
		
		Intent intent = new Intent(this, PagesService.class);
		intent.putExtra(PagesService.EXTRA_MANGA_TITLE, getIntent().getStringExtra(Manga.TITLE_COLUMN_NAME));
		intent.putExtra(PagesService.EXTRA_CHAPTER_NUMBER, currentChapter);
		startService(intent);
		
		mAdapter = new ImagePagerAdapter(getSupportFragmentManager());
		mPager = (CustomViewPager) findViewById(R.id.fullscreen_pager);
		mPager.setAdapter(mAdapter);
		
		registerReceiver(mPageReceiver, new IntentFilter(PagesService.ACTION_ADD_PAGE));
	}
	
	// Adapter for framents which contains the ImageViews children
	private static class ImagePagerAdapter extends FragmentPagerAdapter {
        public ArrayList<String> pagesLinks = new ArrayList<String>(30);

        public ImagePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return pagesLinks.size();
        }

        @Override
        public Fragment getItem(int position) {
            return ChapterPageFragment.newInstance(pagesLinks.get(position));
        }
    }
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		unregisterReceiver(mPageReceiver);
	}
	
	private class NumberChapters extends AsyncTask<Integer, Integer, Integer>{

		@Override
		protected Integer doInBackground(Integer... params) {
			// params[0] = ID Manga
			Manga m = getHelper().getMangaRunDao().queryForId(params[0]);
			int number = 0;
			
			try {
				Document doc = Internet.getURL(m.link);
				Elements chapters = doc.select(".detail_list ul li");
				number = chapters.size();
			} catch (IOException e) {
				e.printStackTrace();
				number = -1;
			}
			
			return number;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			if(result != -1){
				numberChapters.set(result);
			} else {
				//TODO: Toast with the message for the connexion problem
			}
	    }
	}
}

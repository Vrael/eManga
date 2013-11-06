package com.emanga.services;

import java.io.IOException;

import org.jsoup.nodes.Document;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.emanga.database.OrmliteIntentService;
import com.emanga.parsers.esMangaHere;
import com.emanga.utils.Internet;

public class PagesService extends OrmliteIntentService{

	public static final String TAG = "PagesService";
	
	public static final String ACTION_ADD_PAGE = "com.manga.services." + TAG + ".reload";
	
	public static final String EXTRA_MANGA_TITLE = "title";
	public static final String EXTRA_CHAPTER_NUMBER = "number";
	public static final String EXTRA_PAGE_URL = "url";
	
	public PagesService() {
		super("PagesService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		String title = intent.getStringExtra(EXTRA_MANGA_TITLE);
		int number = intent.getIntExtra(EXTRA_CHAPTER_NUMBER, -1);
		
		Log.d(TAG, "Getting chapter pages: " + title + " ( " + number + " )");
		
		long start = System.currentTimeMillis();
		
		
		Intent result = new Intent(ACTION_ADD_PAGE);
		String url = esMangaHere.buildChapterUrl(title, number);

		Document doc;
		String nextPage = url;
	
		try {
			do {
				doc = Internet.getURL(nextPage);
				
				try{
					// Next page link
					nextPage = esMangaHere.nextPageChapter(doc);
				} catch(NullPointerException e){
					System.out.println("Chapter url error: " + nextPage);
					doc = Internet.getURL(esMangaHere.buildChapterUrl(title, ++number));
					nextPage = esMangaHere.nextPageChapter(doc);
				}
				
				// Notify with image src
				result.putExtra(EXTRA_PAGE_URL, esMangaHere.getPageImage(doc));
				sendBroadcast(result);
			}
			while(nextPage != "");
			
		} catch (IOException e) {
			e.printStackTrace();
			Log.d(TAG, "Couldn't retrives url: " + nextPage);
		} catch(NullPointerException e){
			Log.d(TAG, "Chapter url error: " + nextPage);
			Toast.makeText(this, "Sorry, this manga hasn't chapters yet!", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end - start));
	}

}

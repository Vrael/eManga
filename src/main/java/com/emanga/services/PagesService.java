package com.emanga.services;

import java.io.IOException;
import java.util.Locale;

import org.jsoup.nodes.Document;

import android.content.Intent;
import android.util.Log;

import com.emanga.database.OrmliteIntentService;
import com.emanga.parsers.esMangaHere;
import com.emanga.parsers.esMangaHere.DocException;
import com.emanga.utils.Internet;

public class PagesService extends OrmliteIntentService{

	public static final String TAG = "PagesService";
	
	public static final String ACTION_ADD_PAGE = "com.manga.services." + TAG + ".reload";
	public static final String ACTION_COUNT_PAGES = "com.manga.services." + TAG + ".numberPages";
	public static final String ACTION_ERROR = "com.manga.services." + TAG + ".error";
	
	public static final String EXTRA_MANGA_TITLE = "title";
	public static final String EXTRA_CHAPTER_NUMBER = "number";
	public static final String EXTRA_PAGE_URL = "url";
	public static final String EXTRA_NUMBER_PAGES = "numberPages";
	
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

		Document doc = null;
		int nPages = 0;
		
		try {
			// Count the pages number
			doc = Internet.getURL(url);
			nPages = esMangaHere.numberPageChapter(doc);
			
			// Notify the number of pages
			Intent numberPages = new Intent(ACTION_COUNT_PAGES);
			System.out.println("nPages: " + nPages);
			numberPages.putExtra(EXTRA_NUMBER_PAGES, nPages);
			sendBroadcast(numberPages);
		
			// Get pages
			for(int i = 1; i <= nPages; i++){
				System.out.println(String.format(Locale.US, "%s/%d.html", url, i));
				doc = Internet.getURL(String.format(Locale.US, "%s/%d.html", url, i));
				// Notify with image src
				result.putExtra(EXTRA_PAGE_URL, esMangaHere.getPageImage(doc));
				sendBroadcast(result);
			}
		} catch (IOException e) {
			//TODO: 
			Log.d(TAG, "Couldn't retrive page");
			e.printStackTrace();
		} catch (DocException e) {
			// Notify the error
			Intent error = new Intent(ACTION_ERROR);
			error.putExtra(EXTRA_MANGA_TITLE, title);
			error.putExtra(EXTRA_CHAPTER_NUMBER, number);
			sendBroadcast(error);
			Log.d(TAG, "Thats chapter doesn't exists");
		}
		
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end - start));
	}

}

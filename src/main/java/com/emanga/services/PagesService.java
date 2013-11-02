package com.emanga.services;

import java.io.IOException;
import java.sql.SQLException;

import org.jsoup.nodes.Document;

import android.content.Intent;
import android.util.Log;

import com.emanga.database.OrmliteIntentService;
import com.emanga.models.Link;
import com.emanga.parsers.esMangaHere;
import com.emanga.utils.Internet;
import com.j256.ormlite.stmt.QueryBuilder;

public class PagesService extends OrmliteIntentService{

	public static final String TAG = "PagesService";
	
	public static final String ACTION_ADD_PAGE = "com.manga.services." + TAG + ".reload";
	
	public static final String EXTRA_CHAPTER_ID = "id";
	public static final String EXTRA_PAGE_URL = "url";
	
	public PagesService() {
		super("PagesService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		int id = intent.getIntExtra(EXTRA_CHAPTER_ID, -1);
		
		Log.d(TAG, "Getting pages of chapter: " + id);
		
		long start = System.currentTimeMillis();
		
		try {
			Intent result = new Intent(ACTION_ADD_PAGE);
			String url = getUrl(id);

			Document doc;
			String nextPage = url;
			do {
				doc = Internet.getURL(nextPage);
				
				// Next page link
				nextPage = esMangaHere.nextPageChapter(doc);
				
				// Notify with image src
				result.putExtra(EXTRA_PAGE_URL, esMangaHere.getPageImage(doc));
				sendBroadcast(result);
			}
			while(nextPage != "");
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end - start));
	}
	
	private String getUrl(int chapterId) throws SQLException{
		QueryBuilder<Link, Integer> linkByChapterId = getHelper()
			.getLinkRunDao().queryBuilder();
		linkByChapterId.where().eq(Link.CHAPTER_COLUMN_NAME, chapterId);
		
		return linkByChapterId.queryForFirst().url;
	}

}

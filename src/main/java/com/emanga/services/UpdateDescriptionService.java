package com.emanga.services;

import java.io.IOException;

import org.jsoup.nodes.Document;

import android.content.Intent;
import android.util.Log;

import com.emanga.database.OrmliteIntentService;
import com.emanga.models.Manga;
import com.emanga.utils.Internet;
import com.j256.ormlite.dao.RuntimeExceptionDao;

public class UpdateDescriptionService extends OrmliteIntentService {

	private static final String TAG = "UpdateDescriptionService";
	
	public static final String MANGAID = "mangaId";
	public static final String RELOAD = "reload";
	
	public static final String ACTION_RELOAD = "com.manga.services." + TAG + ".reload";
	
	public UpdateDescriptionService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "Updating description of manga");
		long start = System.currentTimeMillis();
		
		int mangaId = intent.getIntExtra(MANGAID, -1);
		if(mangaId > -1){
			try {
				RuntimeExceptionDao<Manga, Integer> mangaDao = getHelper().getMangaRunDao();
				
				Manga manga = mangaDao.queryForId(mangaId);
				
				Document doc = Internet.getURL(manga.link);
				String description = doc.select("#show").first().ownText();
				
				int rows = 0;
				// If the description in the web has something written
				if(description.matches(".*\\w.*")){
					manga.description = description;
					rows = mangaDao.update(manga);
				}
				
				// Notifiy with result (updated or not updated)
				sendNotitification((rows > 0)? true : false);
				
			} catch (IOException e) {
				Log.e(TAG, "Manga description couldn't be retrived!");
				sendNotitification(false);
			}
		}
		
		long end = System.currentTimeMillis();
		Log.d(TAG, "Updated description in: " + (end - start) + " ms");
	}
	
	
	public void sendNotitification(Boolean result){
		sendBroadcast((new Intent(ACTION_RELOAD))
				.putExtra(RELOAD, result));
	}
}

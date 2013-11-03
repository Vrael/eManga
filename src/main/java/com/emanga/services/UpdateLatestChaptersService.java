package com.emanga.services;

import java.io.IOException;
import java.sql.SQLException;

import org.jsoup.nodes.Document;

import android.content.Intent;
import android.util.Log;

import com.emanga.database.OrmliteIntentService;
import com.emanga.models.Manga;
import com.emanga.parsers.esMangaHere;
import com.emanga.utils.Internet;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

public class UpdateLatestChaptersService extends OrmliteIntentService {
	
	private static String TAG = "UpdateLatestChaptersService";
	
	public static final String ACTION_RELOAD = "com.manga.services." + TAG + ".reload";
	public static final String ACTION_PROGRESS = "com.manga.services." + TAG + ".progress";
	
	public static final String EXTRA_CHAPTERS_IDS = "ids";
	public static final String EXTRA_CHAPTERS_PROCESS = "process";
	
	private QueryBuilder<Manga, Integer> mangasEmptyCovers; 
	
	public UpdateLatestChaptersService() {
		super("UpdateLatestChaptersService");
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		mangasEmptyCovers = getHelper().getMangaRunDao().queryBuilder();
		try {
			mangasEmptyCovers.where().isNull(Manga.COVER_COLUMN_NAME);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "Updating latest chapters");
		long start = System.currentTimeMillis();
		
		// Intent that notify progress bar about service progress
		Intent process = new Intent(ACTION_PROGRESS);
		process.putExtra(EXTRA_CHAPTERS_PROCESS, 5);
		sendBroadcast(process);
		
		try {
			// Get all latest chapters list
			Document doc = Internet.getURL(esMangaHere.LATEST_CHAPTERS_URL);
			
			// Notify progress
			process.putExtra(EXTRA_CHAPTERS_PROCESS, 20);
			sendBroadcast(process);
			
			// Parse mangas
			Manga[] mangas = esMangaHere.parseMangasWithChapters(doc, intent.getIntExtra("number", 20));
			
			// Notify progress
			process.putExtra(EXTRA_CHAPTERS_PROCESS, 60);
			sendBroadcast(process);
			
			// if some manga is new in the db its cover doesn't exists yet 
			// So it will recover covers for all new mangas
			mangas = updateCovers(mangas);
			
			// Notify progress
			process.putExtra(EXTRA_CHAPTERS_PROCESS, 80);
			sendBroadcast(process);
						
			// Save mangas in the DB
			getHelper().saveMangas(mangas);
			
			// Notify progress
			process.putExtra(EXTRA_CHAPTERS_PROCESS, 100);
			sendBroadcast(process);
			
			sendBroadcast(new Intent(ACTION_RELOAD));
			sendBroadcast(new Intent(UpdateMangasService.ACTION_RELOAD));
			
		} catch (IOException e){
			Log.e(TAG, "Latest chapters couldn't be retrived");
		}
		
		long end = System.currentTimeMillis();
		Log.d(TAG, "Updated chapters in: " + (end - start) + " ms");
	}
	
	private Manga[] updateCovers(Manga[] mangas){
		try {
			RuntimeExceptionDao<Manga, Integer> dao = getHelper().getMangaRunDao();
			
			for(Manga m : mangas){
				if(!dao.idExists(m.id)){
					m.cover = esMangaHere.parseCoverManga(Internet.getURL(m.link));
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return mangas;
	}
}

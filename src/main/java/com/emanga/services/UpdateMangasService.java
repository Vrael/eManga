package com.emanga.services;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jsoup.nodes.Document;

import android.content.Intent;
import android.util.Log;

import com.emanga.database.DatabaseHelper;
import com.emanga.database.OrmliteIntentService;
import com.emanga.parsers.esMangaHere;
import com.emanga.utils.Internet;

public class UpdateMangasService extends OrmliteIntentService {

	private static final String TAG = "UpdateMangasService";
	
	public static final String ACTION_RELOAD = "com.manga.services." + TAG + ".reload";
	
	public UpdateMangasService() {
		super("UpdateMangasService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "Updating manga list");
		long start = System.currentTimeMillis();
		
		if(!getHelper().isMangas()){
			Log.d(TAG, "Getting mangas from " + esMangaHere.MANGAS_LIST_URL);
			
			try {
				final DatabaseHelper helper = getHelper();
				
				Document htmlDirectory = Internet.getURL(esMangaHere.MANGA_CATALOG_URL);
				
				// N pages from http://es.mangahere.com/directory/1...N.htm
				// +1 is for performance in the loops
				final int pages = Integer.valueOf(htmlDirectory.select(".next-page a:nth-last-child(2)").first().text()) + 1;
				
				// Queue with html of each page http://es.mangahere.com/directory/1...N.htm
				final BlockingQueue<Document> downloads = new LinkedBlockingQueue<Document>();
				
				// Thread for downloads
				new Thread(new Runnable(){
					public void run(){
						for(int i = 1; i < pages; i++){
							Log.d(TAG, "Download: " + i);
							try {
								downloads.put(Internet.getURL((new StringBuilder(esMangaHere.ROOT_URL))
										.append("/directory/").append(i).append(".htm").toString()));
							} catch (IOException e){
								Log.e(TAG, "Error downloading " + esMangaHere.ROOT_URL + "/directory/" + i + ".htm");
							} catch (InterruptedException e) {
								Log.e(TAG, "Error while it was adding a Doc to queue");
								e.printStackTrace();
							}
						}
					}
				}).start();
				
				Intent result = new Intent(ACTION_RELOAD);
				// Processed mangas
				for(int i = 1; i < pages; i++){
					Log.d(TAG, "Processing directory ( " + i + " )");
					helper.saveMangas(esMangaHere.parseMangasDirectory(downloads.take()));
					
					Log.d(TAG, "Sending mangas to loader");
					sendBroadcast(result);
				}
				
				
			} catch (IOException e1) {
				Log.e(TAG, "Manga Catalog couldn't be retrived!");
				e1.printStackTrace();
			} catch (InterruptedException e) {
				Log.e(TAG, "It was an error with downloads queue");
				e.printStackTrace();
			}
		}
		
		long end = System.currentTimeMillis();
		Log.d(TAG, "Updated manga list in: " + (end - start) + " ms");
	}
}

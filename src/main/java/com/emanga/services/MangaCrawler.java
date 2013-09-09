package com.emanga.services;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.emanga.views.Thumbnail;

public class MangaCrawler extends IntentService {
	
	private static String TAG = MangaCrawler.class.getName();
	
	public static final String ROOTURL = "http://es.mangahere.com";
	public static final String URL = ROOTURL + "/latest";
	public static final String NOTIFICATION = "com.emanga.services";
	public static final String THUMBNAIL = "thumb";
	
	public MangaCrawler() {
		super("MangaCrawler");
	}
		
	@Override
	protected void onHandleIntent(Intent arg0) {
		try {
			Log.d(TAG, "Getting latest mangas from repository");
			Document doc = Jsoup.connect(URL)
					  .userAgent("Mozilla")
					  .cookie("auth", "token")
					  .timeout(6000)
					  .get();
			
			Elements mangas = doc.select(".manga_updates dl");
			
			int size = mangas.size();
			int i = 1;
			for(Element manga: mangas){
				Elements images = Jsoup.connect(ROOTURL + manga.select("dt a[href]").attr("href"))
						.userAgent("Mozilla")
						.cookie("auth", "token")
						.get()
    					.select(".manga_detail_top img");
				
				Log.d(TAG, "Procesing manga " + i + " of " + size);
				// When new thumb is ready it publishes
				publishThumb(
					new Thumbnail(
						manga.select("dd a[href]").first().text(), // Get title
						images.get(0).attr("src") // Get cover
					));
				
				// Get date
				manga.select("dt .time").first().text();
				// Get link of chapter
				manga.select("dd a[href]").first().attr("href");
				i++;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d(TAG, "Finished");
	}
	
	private void publishThumb(Thumbnail thumb) {
		Log.d(TAG, "Publishing thumb");
		Intent intent = new Intent(NOTIFICATION);
		intent.putExtra(THUMBNAIL, thumb);
		sendBroadcast(intent);
	}
}

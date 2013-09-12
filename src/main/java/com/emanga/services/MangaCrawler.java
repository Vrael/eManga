package com.emanga.services;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	private static final byte NUMBEROFCHAPTERS = 5;
	
	private ExecutorService executor;
	
	public MangaCrawler() {
		super("MangaCrawler");
		// Pool of threads for manga processing.
		executor = Executors.newFixedThreadPool(2);
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
			
			Elements mangas = doc.select(".manga_updates dl:lt(" + NUMBEROFCHAPTERS + ")");
			
			Log.d(TAG, mangas.size() + " will be process");
			
			// Task to get latest chapters
			class LastChapterTask implements Runnable {
				private final int i;
				private final Element manga;
				public LastChapterTask(int num, Element m){ i=num; manga=m; }
				
				public void run(){
					Log.d(TAG, "Processing new manga (" + i + ")");
					try {
						Elements mangaHeader = manga.select("dt a[href]");
						Elements images = Jsoup.connect(ROOTURL + mangaHeader.attr("href"))
								.userAgent("Mozilla")
								.cookie("auth", "token")
								.get()
								.select(".manga_detail_top img");
						
						
						Element link = manga.select("dd a[href]").first();
						
						// Pattern for looking for the number of chapter, eg: Dr· Frost 24
						Pattern p = Pattern.compile("\\d+$");
						Matcher m = p.matcher(link.text());
						m.find();
						
						publishThumb(new Thumbnail(
								mangaHeader.first().text(), 				// Get title
								images.get(0).attr("src"), 					// Get cover
								manga.select("dt .time").first().text(), 	// Get date
								m.group(),		 							// Get chapter number
								link.attr("href")						 	// Get link of chapter
								));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			int i = 1;
			// Launch all tasks
			for(Element manga: mangas){
				executor.execute(new LastChapterTask(i,manga));
				i++;
			}
			// Disable new tasks from being submitted
			executor.shutdown();
		   try {
		     // Wait a while for existing tasks to terminate
		     if (!executor.awaitTermination(3, TimeUnit.MINUTES)) {
		       executor.shutdownNow(); // Cancel currently executing tasks
		       // Wait a while for tasks to respond to being cancelled
		       if (!executor.awaitTermination(60, TimeUnit.SECONDS))
		           System.err.println("Executor of mangas did not terminate");
		     }
		   } catch (InterruptedException ie) {
	         // (Re-)Cancel if current thread also interrupted
	         executor.shutdownNow();
	         // Preserve interrupt status
	         Thread.currentThread().interrupt();
		   }
	       System.out.println("Finished all Mangas");
	       
		} catch (IOException e1) {
			e1.printStackTrace();
		} 
	}
	
	private void publishThumb(Thumbnail thumb) {
		Log.d(TAG, "Publishing thumb");
		Intent intent = new Intent(NOTIFICATION);
		intent.putExtra(THUMBNAIL, thumb);
		sendBroadcast(intent);
	}
}

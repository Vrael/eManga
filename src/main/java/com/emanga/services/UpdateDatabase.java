package com.emanga.services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Intent;
import android.util.Log;

import com.emanga.database.OrmliteIntentService;
import com.emanga.models.Chapter;
import com.emanga.models.Link;
import com.emanga.models.Manga;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

public class UpdateDatabase extends OrmliteIntentService {
	private static String TAG = UpdateDatabase.class.getName();
	
	private static final String ACTION = "com.manga.intent.action";
	public static final String ACTION_LATEST_CHAPTERS = ACTION + ".latestChapters";
	
	private static final String ROOTURL = "http://es.mangahere.com";
	private static final String LATEST_CHAPTERS_URL = ROOTURL + "/latest";
	
	private static final byte NUMBER_OF_CHAPTERS = 10;
	private static final byte THREADPOOL = 2;
	
	private ExecutorService executor;
	
	public UpdateDatabase() {
		super("UpdateDBService");
		executor = Executors.newFixedThreadPool(THREADPOOL);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		updateLatestChapters();
	}
	
	private void updateLatestChapters() {
		Log.d(TAG, "Updating Database");
		Log.d(TAG, "Getting chapters from " + LATEST_CHAPTERS_URL);
		Document doc = getURL(LATEST_CHAPTERS_URL);
		
		if (doc != null) {
			Elements mangas = doc.select(".manga_updates dl:lt(" + NUMBER_OF_CHAPTERS + ")");
			
			// Task to get latest chapters from web and updates DB
			class LastChapterTask implements Runnable {
				private final int i;
				private final Element mangaHtml;
				public LastChapterTask(int num, Element m){ i=num; mangaHtml=m; }
				
				public void run(){
					Log.d(TAG, "Processing new manga (" + i + ")");
					
					String mangaTitle = mangaHtml.select("dt a[href]").first().text();	// Get title	 
					Manga m = getMangaFromDB(mangaTitle);
					
					// If manga doesn't exist, it is created and its chapters too.
					if(m == null){
						Log.d(TAG, "Manga doesn't exists yet!");
						createMangaWithChapter(mangaHtml);
						publishChapter(null);
					}
					else {
						Log.d(TAG, "Manga already exists!");
						Elements chaptersHtml = mangaHtml.select("dd a[href]");
						// It sees if chapter already exists
						for(Element chapterHtml: chaptersHtml) {
							// Pattern for looking for the number of chapter, eg: Dr· Frost 24
							Pattern p = Pattern.compile("\\d+$");
							Matcher matcher = p.matcher(chapterHtml.text());
							matcher.find();
							int number = Integer.valueOf(matcher.group());
							
							if(!isChapter(mangaTitle, number)) {
								Log.d(TAG, "Chapter doesn't exists yet!");
								String url = chapterHtml.attr("href");
								createChapter(m, number, url);
								publishChapter(null);
							} else { 
								// Manga and chaptes is already in DB, it isn't continue search
								Log.d(TAG, "Chapter already exists!");
								executor.shutdownNow();
							}
						}
					}
				}
				
				/**
				 * Search a manga in the DB by title
				 * @param title string with the title of manga
				 * @return manga if exists else null
				 */
				private Manga getMangaFromDB(String title){
					RuntimeExceptionDao<Manga, Integer> mangaDao = getHelper().getMangaRunDao();
					QueryBuilder<Manga, Integer> mQb = mangaDao.queryBuilder();
					Manga m = null;
					try {
						mQb.where().eq(Manga.TITLE_COLUMN_NAME, title);
						m = mQb.queryForFirst();
					} catch (SQLException e) {
						Log.e(TAG, "Error while it builds titles query");
						e.printStackTrace();
					}
					return m;
				}
				
				/**
				 * Parse html source and create a Manga with its Chapters in the DB
				 * @param html (currently only valid for http://es.mangahere.com/latest )
				 */
				private void createMangaWithChapter(Element html){
					Element mangaHeader = html.select("dt a[href]").first();
					Manga m = new Manga(
							mangaHeader.text(),									// Get title
							getURL(ROOTURL + mangaHeader.attr("href"))			// Get cover
								.select(".manga_detail_top img").first().attr("src")
								);
					
					RuntimeExceptionDao<Manga, Integer> mangaDao = getHelper().getMangaRunDao();
					RuntimeExceptionDao<Chapter, Integer> chapterDao = getHelper().getChapterRunDao();
					RuntimeExceptionDao<Link, Integer> linkDao = getHelper().getLinkRunDao();
					
					mangaDao.create(m);
					
					Chapter chapter = null;
					Elements chaptersHtml = html.select("dd a[href]");
					for(Element chapterHtml: chaptersHtml) {
						// Pattern for looking for the number of chapter, eg: Dr· Frost 24
						Pattern p = Pattern.compile("\\d+$");
						Matcher matcher = p.matcher(chapterHtml.text());
						matcher.find();
						
						chapter = new Chapter(
								Integer.valueOf(matcher.group()),
								new Date(),	// TODO: It must changes by real chapter date!
								// thumb.date = mangaHtml.select("dt .time").first().text(); 	// Get date
								m
								);
						
						Link link = new Link(
								chapterHtml.attr("href"),
								chapter
								);
						
						chapterDao.create(chapter);
						linkDao.create(link);
					}
				}
				
				/**
				 * Check if chapter already exists in DB
				 * @param mangaTitle
				 * @param number of chapter
				 * @return true or false
				 */
				private boolean isChapter(String mangaTitle, int number) {
					RuntimeExceptionDao<Manga, Integer> mangaDao = getHelper().getMangaRunDao();
					RuntimeExceptionDao<Chapter, Integer> chapterDao = getHelper().getChapterRunDao();
					
					QueryBuilder<Chapter, Integer> cQb = chapterDao.queryBuilder();
					QueryBuilder<Manga, Integer> mQb = mangaDao.queryBuilder();
					
					Chapter chapter = null;
					try {
						mQb.where().eq(Manga.TITLE_COLUMN_NAME, mangaTitle);
						cQb.where().eq(Chapter.NUMBER_COLUMN_NAME, number);
						chapter = cQb.join(mQb).queryForFirst();
					} catch (SQLException e) {
						Log.e(TAG, "An error happened when it was checking if the chapter already exists");
						e.printStackTrace();
					}
					return (chapter) != null? true : false;
				}
				
				/**
				 * Create and store a chapter for the manga passed by parameter
				 * @param m
				 * @param number
				 * @param htmlChapter
				 */
				private void createChapter(Manga m, int number, String url) {
					RuntimeExceptionDao<Chapter, Integer> chapterDao = getHelper().getChapterRunDao();
					RuntimeExceptionDao<Link, Integer> linkDao = getHelper().getLinkRunDao();
					
					Chapter chapter = new Chapter(
							number,
							new Date(),	//TODO: It must changes by real chapter date!
							m
							);
					
					Link link = new Link(
							url,
							chapter
							);
					
					chapterDao.create(chapter);
					linkDao.create(link);
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
		           Log.e(TAG, "Executor of mangas did not terminate");
		     }
		   } catch (InterruptedException ie) {
	         // (Re-)Cancel if current thread also interrupted
	         executor.shutdownNow();
	         // Preserve interrupt status
	         Thread.currentThread().interrupt();
		   }
	       Log.d(TAG, "Finished all Chapters");
		} else {
			Log.e(TAG, "Service web unviable");
		}
		
	}
	
	/**
	 * Get a URL from its string text
	 * @param url
	 * @return Document or null
	 */
	private Document getURL(String url) {
		Document doc = null;
		try {
			doc = Jsoup.connect(url)
					  .userAgent("Mozilla")
					  .cookie("auth", "token")
					  .timeout(20000)
					  .get();
		} catch (IOException e) {
			Log.e(TAG, "URL cannot be retrieved");
			e.printStackTrace();
		}
		return doc;
	}
	
	private void publishChapter(Chapter c) {
		Log.d(TAG, "New chapter published");
		Intent intent = new Intent(ACTION_LATEST_CHAPTERS);
		if (c != null) {
			intent.putExtra("chapter", c);  
		}
		sendBroadcast(intent);
	}
}

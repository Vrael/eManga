package com.emanga.services;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Intent;
import android.util.Log;

import com.emanga.database.OrmliteIntentService;
import com.emanga.models.Category;
import com.emanga.models.CategoryManga;
import com.emanga.models.Chapter;
import com.emanga.models.Link;
import com.emanga.models.Manga;
import com.emanga.parsers.esMangaHere;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

public class UpdateDatabase extends OrmliteIntentService {
	private static String TAG = UpdateDatabase.class.getName();
	
	private static final String ACTION = "com.manga.intent.action";
	public static final String ACTION_LATEST_CHAPTERS = ACTION + ".latestChapters";
	public static final String INTENT_CHAPTER_ID = "chapterId";
	
	private static final byte NUMBER_OF_MANGAS = 30;
	
	public UpdateDatabase() {
		super("UpdateDBService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "Updating Database");
		long start = System.currentTimeMillis();
		updateLatestChapters();
		updateCategories();
		updateMangaList();
		long end = System.currentTimeMillis();
		Log.d(TAG, "Services live in: " + (end - start)/1000 + " s");
	}
	
	private void updateLatestChapters() {
		Log.d(TAG, "Updating latest chapters from " + esMangaHere.LATEST_CHAPTERS_URL);
		try {
			Document doc = getURL(esMangaHere.LATEST_CHAPTERS_URL);
			Elements mangas = doc.select(".manga_updates dl:lt(" + NUMBER_OF_MANGAS + ")");
			
			RuntimeExceptionDao<Manga, String> mangaDao = getHelper().getMangaRunDao();
			Manga m;
			int chapterId = -1;
			for(Element manga : mangas){
				// See if manga exists previously
				m = mangaDao.queryForId(esMangaHere.parseTitleManga(manga));
				if (m == null){
					chapterId = createMangaWithChapter(mangaDao, manga);
				} else {
					// Manga already exists so it will save only new manga chapters
					chapterId = createChapters(mangaDao, m, manga.select("dd a[href]"));
				}
				
				// Send an intent with the latest chapter id of this manga (most recently chapter only)
				sendBroadcast((new Intent(ACTION_LATEST_CHAPTERS))
						.putExtra(INTENT_CHAPTER_ID, chapterId));
			}
		} catch (IOException e){
			Log.e(TAG, "Latest chapters couldn't be retrived");
		}
	}
	
	private int createChapters(RuntimeExceptionDao<Manga, String> mangaDao, Manga manga, Elements html){
		List<Integer> ids = new ArrayList<Integer>(html.size());
		for(Element chapter: html) {
			try {
				int number = esMangaHere.parseNumberChapter(chapter);
				
				if(!hasChapter(mangaDao, manga, number)) {
					// Chapters doesn't exists so we create it
					ids.add(createChapter(manga, number, esMangaHere.parseUrlChapter(chapter), 
							esMangaHere.parseChapterDate(chapter)));
				} 
				// If chapters exist do nothing
			} catch (ParseException e){
				Log.e(TAG, "Couldn't parse from chapter html");
			}
		}
		// First id is the latest chapter (see esMangaHere/latest)
		return (!ids.isEmpty())? ids.get(0) : -1;
	}
	/**
	 * Parse html source and create a Manga with its Chapters in the DB
	 * @param html (currently only valid for http://es.mangahere.com/latest )
	 * @throws IOException 
	 */
	private int createMangaWithChapter(RuntimeExceptionDao<Manga, String> mangaDao, Element html) {
		int chapterId = -1;
		try {
			Element mangaHeader = html.select("dt a[href]").first();
			String cover = getURL(esMangaHere.ROOT_URL + mangaHeader.attr("href"))		// Get cover
					.select(".manga_detail_top img").first().attr("src");
					 
			Manga manga = new Manga(mangaHeader.text(), cover);
			mangaDao.create(manga);
			
			// Elements from each html chapters list
			Elements chaptersHtml = html.select("dd a[href]");	
			
			try {
				// Latest chapter will process separated for to get its id
				chapterId = createChapter(manga, esMangaHere.parseNumberChapter(chaptersHtml.get(0)), 
						chaptersHtml.get(0).attr("href"), esMangaHere.parseChapterDate(html));
				chaptersHtml.remove(0);
				
				// Process the rest of chapters
				for(Element chapterHtml: chaptersHtml) {
					createChapter(manga, esMangaHere.parseNumberChapter(chapterHtml), 
							chapterHtml.attr("href"), esMangaHere.parseChapterDate(html));
				}				
			} catch (ParseException e){
				Log.e(TAG, "Couldn't parse the date of chapter. Chapter wasn't save in database");
				e.printStackTrace();
			}
			
		} catch (IOException e){
			Log.e(TAG, "Cover couldn't be retrived");
		}
		return chapterId;
	}
	
	private boolean hasChapter(RuntimeExceptionDao<Manga, String> mangaDao, Manga manga, int number) {
		RuntimeExceptionDao<Chapter, Integer> chapterDao = getHelper().getChapterRunDao();
		
		QueryBuilder<Chapter, Integer> cQb = chapterDao.queryBuilder();
		QueryBuilder<Manga, String> mQb = mangaDao.queryBuilder();
		
		Chapter chapter = null;
		try {
			mQb.where().eq(Manga.TITLE_COLUMN_NAME, manga.title);
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
	private int createChapter(Manga manga, int number, String url, Date date) {
		RuntimeExceptionDao<Chapter, Integer> chapterDao = getHelper().getChapterRunDao();
		RuntimeExceptionDao<Link, Integer> linkDao = getHelper().getLinkRunDao();
	
		Chapter chapter = new Chapter(number, date, manga);
		chapterDao.create(chapter);
		linkDao.create(new Link(url, chapter));
		return chapter.id;
	}
	
	private void updateCategories() {
		Log.d(TAG, "Getting categories from " + esMangaHere.MANGA_CATALOG_URL);
		
		try {
			Document doc = getURL(esMangaHere.MANGA_CATALOG_URL);
			final RuntimeExceptionDao<Category, Integer> categoryDao = getHelper().getCategoryRunDao();
			// Check number of mangas in DB and remote site
			if (esMangaHere.parseCategoryCount(doc) != categoryDao.countOf()) {
				// Get categories from html source
				final Category[] categories = esMangaHere.parseCategories(doc);
				// Save categories in DB
				categoryDao.callBatchTasks(new Callable<Void>() {
					public Void call() throws Exception {
						storeCategories(categoryDao, categories);
				    	return null;
				    }
				});
				Log.d(TAG, "Categories updated!");
			} else {
				Log.d(TAG, "Categories already are updated");
			}
		} catch (IOException e) {
			Log.e(TAG, "Error downloading " + esMangaHere.MANGA_CATALOG_URL);
		}
	}
	
	private void storeCategories(final RuntimeExceptionDao<Category, Integer> dao, Category[] categories ){
		for(Category c: categories){
			dao.createOrUpdate(c);
		}
	}
	
	/**
	 * @see @link{http://stackoverflow.com/questions/11761472/ormlites-createorupdate-seems-slow-what-is-normal-speed}
	 */
	private void updateMangaList() {
		Log.d(TAG, "Getting mangas from " + esMangaHere.MANGAS_LIST_URL);
		
		try {
			Document html = getURL(esMangaHere.MANGAS_LIST_URL);
			final RuntimeExceptionDao<Manga, String> mangaDao = getHelper().getMangaRunDao();
			
			Document htmlDirectory = getURL(esMangaHere.MANGA_CATALOG_URL);
			// N pages in http://es.mangahere.com/directory/1...N.htm
			final int pages = Integer.valueOf(htmlDirectory.select(".next-page a:nth-last-child(2)").first().text()) + 1; 
			// +1 is for performance in the loops
			
			// Check number of mangas in DB and remote site
			if (esMangaHere.parseMangasCount(html) != mangaDao.countOf()) {
				final HashMap<String, Category> categories = getCategories();
				
				// Queue with html of each page http://es.mangahere.com/directory/1...N.htm
				final BlockingQueue<Document> downloads = new LinkedBlockingQueue<Document>();
				
				// Thread for downloads
				new Thread(new Runnable(){
					public void run(){
						for(int i = 1; i < pages; i++){
							Log.d(TAG, "Download: " + i);
							try {
								downloads.put(getURL((new StringBuilder(esMangaHere.ROOT_URL))
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
				
				// Processed mangas
				for(int i = 1; i < pages; i++){
					Log.d(TAG, "Processing directory ( " + i + " )");
					mangaDao.callBatchTasks(
						new Callable<Void>(){
							public Void call() throws Exception {	
								storeMangas(mangaDao, esMangaHere.parseMangasDirectory(downloads.take(), categories));
								return null;
							}
						}
					);
				}
			} else {
				Log.d(TAG, "Mangas already are updated");
			}
		} catch (IOException e) {
			Log.e(TAG, "Error downloading " + esMangaHere.MANGAS_LIST_URL);
			e.printStackTrace();
		}
	}
	
	private void storeMangas(final RuntimeExceptionDao<Manga, String> mangaDao, final Manga[] mangas){
		RuntimeExceptionDao<CategoryManga, Integer> categoryMangaDao = getHelper().getCategoryMangaRunDao();
		for(Manga m : mangas){
			mangaDao.createOrUpdate(m);
			for(Category c : m.categories) {
				categoryMangaDao.createOrUpdate(new CategoryManga(c,m)); 
			}
		}
	}
	
	private HashMap<String,Category> getCategories(){
		RuntimeExceptionDao<Category, Integer> categoryDao = getHelper().getCategoryRunDao();
		List<Category> categories = categoryDao.queryForAll();
		HashMap<String,Category> mCategories = new HashMap<String, Category>();
		for(Category c: categories){
			mCategories.put(c.name, c);
		}
		return mCategories;
	}
	/**
	 * Get a URL from its string text
	 * @param url
	 * @return Document or null
	 */
	private Document getURL(String url) throws IOException {
		return Jsoup.connect(url)
					  .userAgent("Mozilla")
					  .cookie("auth", "token")
					  .timeout(20000)
					  .get();
	}
}

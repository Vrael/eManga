package com.emanga.parsers;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.emanga.models.Category;
import com.emanga.models.Chapter;
import com.emanga.models.Link;
import com.emanga.models.Manga;
import com.emanga.utils.Dictionary;

public class esMangaHere {
	public static final String ROOT_URL = "http://es.mangahere.com";
	public static final String LATEST_CHAPTERS_URL = ROOT_URL + "/latest";
	public static final String MANGAS_LIST_URL = ROOT_URL + "/mangalist";
	public static final String MANGA_CATALOG_URL = ROOT_URL + "/directory/1.htm?last_chapter_time_za=";
	
	public static final String TAG = "esMangaHere";
	
	/**
	 * eg: http://es.mangahere.com/store/manga/976/53.0/compressed/k003.jpg
	 * @param doc with chapter page html
	 * @return url as a string of page image
	 */
	public static String getPageImage(Document doc){
		return doc.select(".read_img a img").first().absUrl("src");
	}
	
	/**
	 * eg: http://es.mangahere.com/manga/nanatsu_no_taizai/c53/
	 * @param doc with chapter page html
	 * @return url as a string of next page
	 */
	public static String nextPageChapter(Document doc){
		return doc.select(".read_img a").first().absUrl("href");
	}
	
	public static String parseCoverManga(Document doc){
		return doc.select(".img").attr("src");
	}
	/**
	 * Given source code html of a chapter parse its number (LATEST_CHAPTERS_URL)
	 * @param htmlChapter
	 * @return chapter's number
	 */
	public static int parseNumberChapter(Element chapterHtml){
		// Pattern for looking for the number of chapter, eg: Dr· Frost 24
		Pattern p = Pattern.compile("\\d+$");
		Matcher matcher = p.matcher(chapterHtml.text());
		matcher.find();
		return Integer.valueOf(matcher.group());
	}
	
	/**
	 * Given source code html of a chapter parse its link (LATEST_CHAPTERS_URL)
	 * @param htmlChapter
	 * @return chapter's url
	 */
	public static String parseUrlChapter(Element chapterHtml){
		return chapterHtml.attr("href");
	}

	/**
	 * Given an string date of chapter, it match with a date object
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public static Date parseChapterDate(Element mangaHtml) throws ParseException{
		String date = mangaHtml.select(".time").first().text().toUpperCase();
		// esMangaHere has this types of dates
		
		SimpleDateFormat sdfDate = new SimpleDateFormat("MMM dd, yyyy hh:mmaa", Locale.US);
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mmaa", Locale.US);
		
		Calendar now = Calendar.getInstance();
		Calendar parse = Calendar.getInstance(); 
		
		if(date.indexOf("HOY") != -1 ){
			date = date.replaceAll("\\p{Cntrl}", "").replace("HOY ", "");
	    	parse.setTime(sdf.parse(date));
	    	
	    	parse.set(Calendar.SECOND, now.get(Calendar.SECOND));
	    	parse.set(Calendar.YEAR, now.get(Calendar.YEAR));
			parse.set(Calendar.MONTH, now.get(Calendar.MONTH));
			parse.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
			parse.set(Calendar.ZONE_OFFSET, now.get(Calendar.ZONE_OFFSET));
			
	    } else if(date.indexOf("AYER") != -1){
	    	date = date.replaceAll("\\p{Cntrl}", "").replace("AYER ", "");
	    	parse.setTime(sdf.parse(date));
	    	
	    	parse.set(Calendar.SECOND, now.get(Calendar.SECOND));
	    	parse.set(Calendar.YEAR, now.get(Calendar.YEAR));
			parse.set(Calendar.MONTH, now.get(Calendar.MONTH));
			parse.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH) -1);
			parse.set(Calendar.ZONE_OFFSET, now.get(Calendar.ZONE_OFFSET));
			
	    } else {
	    	date = date.replaceAll("\\p{Cntrl}", "");
	    	parse.setTime(sdfDate.parse(date));
	    }
		
		return parse.getTime();
	}
	
	/**
	 * Given source code html of a manga, this parse its title (LATEST_CHAPTERS_URL)
	 * @param mangaHtml
	 * @return
	 */
	public static String parseTitleManga(Element mangaHtml) {
		return mangaHtml.select("dt a[href]").first().text();
	}
	
	/**
	 * Given source code html of a manga list, this parse its title and url (MANGA_LIST_URL)
	 * @param mangaListHtml
	 * @return an arry with title and links of mangas
	 */
	public static Manga[] parseMangasFromList(Document mangaListHtml) {
		Elements mangasHtml = mangaListHtml.select(".manga_info");
		Manga[] mangas = new Manga[mangasHtml.size()];
		int i = 0;
		for(Element m: mangasHtml){
			mangas[i] = new Manga(m.text(), m.attr("href"));
			i++;
		}
		return mangas;
	}
	
	/**
	 * Given source code html of a manga list, this count the number of mangas (MANGA_LIST_URL)
	 * @param mangaListHtml
	 * @return number of mangas in remote repository
	 */
	public static int parseMangasCount(Document mangasListHtml){
		return mangasListHtml.select(".manga_info").size();
	}
	
	/**
	 * Match an string with its category object
	 * @param text
	 * @param categories
	 * @return
	 */
	private static Category[] getCategoriesFromString(String text){
		String[] titles = text.toLowerCase().split(", ");
		ArrayList<Category> category = new ArrayList<Category>(titles.length);
		String name = null;
		for(String title : titles) {
			name = Dictionary.categories.get(title);
			if(name == null)
				name = title;
			// Fix some gaps in the translation
			category.add(new Category(name));
		}
		return category.toArray(new Category[category.size()]);
	}
	
	/**
	 * Given a source code html and a HashMap with the categories, it return an array of instanciate mangas
	 * @param documentsHtml
	 * @param categories
	 * @return
	 */
	public static Manga[] parseMangasDirectory(Document documentsHtml){
		Elements mangasHtml = documentsHtml.select(".directory_list ul li");
		Manga[] mangas = new Manga[mangasHtml.size()];
		
		Elements covers = mangasHtml.select(".manga_img img");
		Elements titles = mangasHtml.select(".title a");
		Elements categoriesName = mangasHtml.select(".manga_text p:nth-of-type(2)");
		
		Iterator<Element> itcategoriesName = categoriesName.iterator();
		Iterator<Element> itcovers = covers.iterator();
		
		int i = 0;
		for(Element title: titles){
			mangas[i] = new Manga(
					title.text(),
					itcovers.next().attr("src").replaceAll("thumb_", ""),
					// Arrays.asList is not a real list (it can't add objects as .add instead of it use [i])
					Arrays.asList(getCategoriesFromString(itcategoriesName.next().text())),
					title.attr("abs:href")
					);
			i++;
		}
		
		return mangas;
	}
	
	public static Manga[] parseMangasWithChapters(Document doc, int number){
		Manga[] mangas = null;
		Elements mangasHtml = doc.select(".manga_updates dl:lt(" + number + ")");
		mangas = new Manga[mangasHtml.size()];
		int i = 0;
		int j = 0;
		
		Manga manga = null;
		Chapter[] chapters;
		Link url;
		Chapter c;
		Elements chaptersHtml;
		Element mangaHeader;
		
		// Process mangas
		for(Element mangaHtml : mangasHtml){
			mangaHeader = mangaHtml.select("dt a").first();
			manga = new Manga(mangaHeader.text());
			manga.link = mangaHeader.attr("abs:href");
			chaptersHtml = mangaHtml.select("dd a");
			chapters = new Chapter[chaptersHtml.size()];
			Date date;
			try {
				date = esMangaHere.parseChapterDate(mangaHtml);
			} catch (ParseException e){
				System.out.println("It was an error parsing date for chapters in manga: " + manga.title);
				e.printStackTrace();
				date = new Date();
			}
			
			// Process chapters
			for(Element chapterHtml : chaptersHtml){
				try {
					url = new Link(chapterHtml.attr("href"));				
					c = new Chapter(
							esMangaHere.parseNumberChapter(chapterHtml),
							date, 
							manga,
							new Link[]{url});
					url.chapter = c;
					
					chapters[j] = c;
					j++;
					// All chapters has same publish date, so this emulate a second difference
					date.setTime(date.getTime() - 1000);

				} catch (UnsupportedEncodingException e){
					System.out.println("It was an error parsing title for to create manga ID: " + manga.title);
				}

			}
			
			manga.chaptersList = chapters;
			j = 0;
			
			mangas[i] = manga;
			i++;
		}
		
		return mangas;
	}
		
	/**
	 * Given source code html of a manga directory, this count the number of categories (MANGA_CATALOG_URL)
	 * @param mangaDirectoryHtml
	 * @return number of categories
	 */
	public static int parseCategoryCount(Element mangaDirectoryHtml){
		// -1 is 'Todo' category
		return mangaDirectoryHtml.select(".by_categories li").size() -1;
	}
	
	/**
	 * Given source code html of a manga directory, this parse the categories (MANGA_CATALOG_URL)
	 * @param mangaDirectoryHtml
	 * @return categories
	 */
	public static Category[] parseCategories(Element mangaDirectoryHtml){
		Elements categoriesHtml = mangaDirectoryHtml.select(".by_categories li");
		categoriesHtml.remove(0);	// Remove 'Todo' category
		Category[] categories = new Category[categoriesHtml.size()];
		int i = 0;
		for(Element category: categoriesHtml){
			categories[i] = new Category(category.text());
			i++;
		}
		return categories;
	}
	
	public static HashMap<String,Category> parseMapCategories(Element mangaDirectoryHtml){
		Elements categoriesHtml = mangaDirectoryHtml.select(".by_categories li");
		categoriesHtml.remove(0);	// Remove 'Todo' category
		HashMap<String,Category> categories = new HashMap<String,Category>();
		Category c;
		for(Element category: categoriesHtml){
			c = new Category(category.text());
			categories.put(c.name, c);
		}
		return categories;
	}
	
	/**
	 * Build an url with esMangaHere pattern
	 * Note(Android Docs):  If you actually want to match only ASCII characters, 
	 * specify the explicit characters you want; if you mean 0-9 use [0-9] rather than \d, 
	 * which would also include Gurmukhi digits and so forth
	 * eg: http://es.mangahere.com/manga/yamada_kun_to_nananin_no_majo/c83/
	 */
	public static String buildChapterUrl(String mangaName, int numberChapter){
		mangaName = mangaName.replaceAll("[^a-zA-Z_0-9]+", "_").replaceAll("_+", "_")
				.replaceAll("^_|_$", "")
				.toLowerCase(Locale.US);
		System.out.println("Manga name: " + mangaName);
		return String.format(Locale.US, "http://es.mangahere.com/manga/%s/c%d", mangaName, numberChapter);
	}

	public static int numberPageChapter(Document doc) throws DocException {
		int size = doc.select(".readpage_top .wid60 option").size();
		if(size == 0){
			throw new DocException("Incorrect html document");
		}
		return size;
	}
	
	public static class DocException extends Exception {
	    
		private static final long serialVersionUID = 1L;

		public DocException( String s ) {
	      super(s);
	    }
	}
}

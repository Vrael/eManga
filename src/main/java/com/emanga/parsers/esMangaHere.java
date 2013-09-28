package com.emanga.parsers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.emanga.models.Category;
import com.emanga.models.Manga;
import com.emanga.utils.Dictionary;

public class esMangaHere {
	public static final String ROOT_URL = "http://es.mangahere.com";
	public static final String LATEST_CHAPTERS_URL = ROOT_URL + "/latest";
	public static final String MANGAS_LIST_URL = ROOT_URL + "/mangalist";
	public static final String MANGA_CATALOG_URL = ROOT_URL + "/directory/?name.az";
	
	public static final String TAG = "esMangaHere";
	
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
		String date = mangaHtml.select(".time").first().text();
		// esMangaHere has this types of dates
		DateTimeFormatter fmtDate = DateTimeFormat.forPattern("MMM dd, yyyy hh:mma"); 
		DateTimeFormatter fmt = DateTimeFormat.forPattern("hh:mma");
		
		DateTime dt = null;
	    if(date.indexOf("Hoy") != -1){
	    	dt = fmt.parseLocalTime(date.replace("Hoy ", "")).toDateTimeToday();
	    } else if(date.indexOf("Ayer") != -1){
	    	dt = fmt.parseLocalTime(date.replace("Ayer ", "")).toDateTimeToday();
	    	dt = dt.minusDays(1);
	    } else {
	    	dt = fmtDate.parseDateTime(date);
	    }
	    return dt.toDate();
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
			mangas[i] = new Manga(m.text().toLowerCase(), m.attr("href"));
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
	private static Category[] getCategoriesFromString(String text, HashMap<String,Category> categories){
		String[] titles = text.toLowerCase().split(", ");
		Category[] category = new Category[titles.length];
		int i=0;
		for(String title : titles) {
			category[i] = categories.get(title);
			if (category[i] == null){
				category[i] = categories.get(Dictionary.categories.get(title));
			}
			i++;
		}
		return category;
	}
	
	/**
	 * Given a source code html and a HashMap with the categories, it return an array of instanciate mangas
	 * @param documentsHtml
	 * @param categories
	 * @return
	 */
	public static Manga[] parseMangasDirectory(Document documentsHtml, HashMap<String,Category> categories){
		Elements mangasHtml = documentsHtml.select(".directory_list ul li");
		
		Manga[] mangas = new Manga[mangasHtml.size()];
		
		Elements covers = mangasHtml.select(".manga_img img");
		Elements categoriesName = mangasHtml.select(".manga_text p:nth-of-type(2)");
		
		Iterator<Element> itcategoriesName = categoriesName.iterator();
		int i = 0;
		for(Element cover : covers) {
			mangas[i] = new Manga(
					cover.attr("alt").toLowerCase(),
					cover.attr("src").replaceAll("thumb_", ""),
					// Arrays.asList is not a real list (it can't add objects as .add instead of it use [i])
					Arrays.asList(getCategoriesFromString(itcategoriesName.next().text(), categories))
					);
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
			categories[i] = new Category(category.text().toLowerCase());
			i++;
		}
		return categories;
	}
	
	public static HashMap<String,Category> parseMapCategories(Element mangaDirectoryHtml){
		Elements categoriesHtml = mangaDirectoryHtml.select(".by_categories li");
		categoriesHtml.remove(0);	// Remove 'Todo' category
		HashMap<String,Category> categories = new HashMap<String,Category>();
		for(Element category: categoriesHtml){
			categories.put(category.text().toLowerCase(), new Category(category.text().toLowerCase()));
		}
		return categories;
	}
}

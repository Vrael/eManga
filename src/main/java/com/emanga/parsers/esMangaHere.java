package com.emanga.parsers;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
	public static final String MANGA_CATALOG_URL = ROOT_URL + "/directory/?name.az";
	
	public static final String TAG = "esMangaHere";
	
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
		for(Element mangaHtml : mangasHtml){
			mangaHeader = mangaHtml.select("dt a").first();
			try {
				manga = new Manga(mangaHeader.text());
				manga.link = mangaHeader.attr("abs:href");
				chaptersHtml = mangaHtml.select("dd a");
				chapters = new Chapter[chaptersHtml.size()];
			
				Date date = esMangaHere.parseChapterDate(mangaHtml);
				for(Element chapterHtml : chaptersHtml){
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
				}
				
				manga.chaptersList = chapters;
				j = 0;
				
			} catch (ParseException e){
				System.out.println("It was an error parsing date for chapters in manga: " + manga.title);
			} catch (UnsupportedEncodingException e){
				System.out.println("It was an error parsing title for to create manga ID: " + manga.title);
			}
			
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
}

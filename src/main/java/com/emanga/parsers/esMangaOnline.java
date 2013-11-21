package com.emanga.parsers;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.emanga.models.Manga;

public class esMangaOnline {
	public static final String ROOT_URL = "http://esmangaonline.com";
	public static final String MANGAS_LIST_URL = ROOT_URL + "/lista-de-manga/all/any/top-rating/";
	
	/**
	 * Given source code html of a manga list, this parse its title and url (MANGA_LIST_URL)
	 * @param mangaListHtml
	 * @return an array with title, cover and links of mangas and its categories
	 */
	public static Manga[] parseMangasFromList(Document mangaListHtml) {
		Elements mangasHtml = mangaListHtml.select(".nde");
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
}

package com.emanga.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for manga that includes its category and chapter list.
 * This is used in MangaDetail where the view shows all information about a manga.
 * This is used too for the Loader
 * It uses in the interfaces
 */
public class MangaContent{
	
	public static List<MangaItem> MANGAS = new ArrayList<MangaItem>();
	public static Map<String, MangaItem> MANGA_MAP = new HashMap<String, MangaItem>();
	
	public static class MangaItem {
		public List<Category> categories;
		public Manga manga;
		public List<Chapter> chapters;
		
		public MangaItem() {}
		
		public MangaItem(Manga m, List<Category> c){
			manga = m;
			categories = c;
		}
		
		public MangaItem(Manga m, List<Category> c, List<Chapter> ch){
			manga = m;
			categories = c;
			chapters = ch;
		}
	}
}


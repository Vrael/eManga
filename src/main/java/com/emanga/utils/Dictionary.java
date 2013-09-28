package com.emanga.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Some repositories like es.mangahere.com has not well translations, so a dictionary is needed for mapping correctly
 * English => Spanish 
 */
public class Dictionary {
	public static final Map<String, String> categories;
    static
    {
        categories = new HashMap<String, String>();
        categories.put("action", "acción");
        categories.put("mystery", "misterio");
        categories.put("sci-fi", "ciencia ficción");
        categories.put("sliceoflife", "vida cotidiana");
        categories.put("schoollife", "escolar");
        categories.put("comedy", "comedia");
        categories.put("fantasy", "fantasía");
    }
}

package com.emanga.views;

import com.emanga.models.Chapter;

/**
 *	Is a model where there are an image and a title or text bottom
 *	for ThumbnailAdapter 
 */
public class Thumbnail {
	public CharSequence title;
	public int image;
	
	public Thumbnail(CharSequence text, int img) {
		title = text;
		image = img;
	}
	
	public Thumbnail(Chapter chapter) {
		title = chapter.manga.title;
		image = chapter.manga.cover;
	}
}

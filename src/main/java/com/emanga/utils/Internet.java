package com.emanga.utils;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Internet {
	public static Document getURL(String url) throws IOException {
		return Jsoup.connect(url)
					  .userAgent("Mozilla")
					  .cookie("auth", "token")
					  .timeout(20000)
					  .get();
	}
}

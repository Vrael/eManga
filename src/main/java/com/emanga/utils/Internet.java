package com.emanga.utils;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Internet {
	public static Document getURL(String url) throws IOException {
		return Jsoup.connect(url)
					  .userAgent("Mozilla")
					  .cookie("auth", "token")
					  .timeout(20000)
					  .get();
	}
	
	public static boolean checkConnection(Context context){
		ConnectivityManager conMgr = (ConnectivityManager) context
	            .getSystemService(Context.CONNECTIVITY_SERVICE);
		
		NetworkInfo i = conMgr.getActiveNetworkInfo();
			if (i == null)
				return false;
			if (!i.isConnected())
				return false;
			if (!i.isAvailable())
				return false;
		return true;
	}
}

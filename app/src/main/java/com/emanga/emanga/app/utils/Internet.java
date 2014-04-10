package com.emanga.emanga.app.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Internet {
    public static final String HOST = "http://10.0.3.2:3000/api/";

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

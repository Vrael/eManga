package com.emanga.emanga.app.database;

import android.app.Service;

import com.j256.ormlite.android.apptools.OpenHelperManager;

public abstract class OrmliteService extends Service {
	private DatabaseHelper databaseHelper = null;

	protected DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper =
                OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }
}

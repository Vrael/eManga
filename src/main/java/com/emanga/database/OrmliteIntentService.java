package com.emanga.database;

import android.app.IntentService;
import android.content.Intent;

import com.j256.ormlite.android.apptools.OpenHelperManager;

public abstract class OrmliteIntentService extends IntentService {
	public OrmliteIntentService(String name) {
		super(name);
	}

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

	@Override
	protected abstract void onHandleIntent(Intent intent);
}

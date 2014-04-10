package com.emanga.emanga.app.database;
import android.support.v4.app.FragmentActivity;

import com.j256.ormlite.android.apptools.OpenHelperManager;

public abstract class OrmliteFragmentActivity extends FragmentActivity {
	public OrmliteFragmentActivity() {
		super();
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
}

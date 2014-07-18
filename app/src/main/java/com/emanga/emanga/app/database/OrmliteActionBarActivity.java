package com.emanga.emanga.app.database;

import android.support.v7.app.ActionBarActivity;

import com.j256.ormlite.android.apptools.OpenHelperManager;

/**
 * Created by Ciro on 30/05/2014.
 */
public class OrmliteActionBarActivity extends ActionBarActivity {
    public OrmliteActionBarActivity() {
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

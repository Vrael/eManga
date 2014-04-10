package com.emanga.emanga.app.loaders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.emanga.emanga.app.database.DatabaseHelper;
import com.emanga.emanga.app.models.Chapter;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

public class LatestChaptersLoader extends AsyncTaskLoader<List<Chapter>> {

    private static final String TAG = LatestChaptersLoader.class.getName();

    public static final String ACTION_RELOAD = "com.emanga.emanga.app.loaders." + TAG + ".reload";

    public List<Chapter> chapters;
    private ChapterIntentReceiver mChapterObserver;

    private RuntimeExceptionDao<Chapter, String> chapterDao;
    private QueryBuilder<Chapter, String> qBcLocal;

    private Calendar time = Calendar.getInstance();

    public LatestChaptersLoader(Context context) {
        super(context);
        time.add(Calendar.DATE, -20);

        chapterDao = OpenHelperManager.getHelper(getContext(), DatabaseHelper.class)
                .getChapterRunDao();

        try {
            qBcLocal = chapterDao.queryBuilder();
            qBcLocal.where().ge(Chapter.DATE_COLUMN_NAME, time.getTime());
            qBcLocal.groupBy(Chapter.MANGA_COLUMN_NAME).having("MAX(" + Chapter.DATE_COLUMN_NAME + ")");
            qBcLocal.orderBy(Chapter.DATE_COLUMN_NAME, true);
        } catch (SQLException e) {
            Log.e(TAG, "Error when it was building the chapters query");
            e.printStackTrace();
        }
    }


    @Override
    public List<Chapter> loadInBackground() {
        List<Chapter> latest = null;
        try {
            latest = qBcLocal.query();
            chapters = latest;
        } catch (SQLException e) {
            Log.e(TAG, "Error when it was loading chapters from DB");
            e.printStackTrace();
        }

        return latest;
    }

    @Override
    public void deliverResult(List<Chapter> data) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (data != null) {
                onReleaseResources(data);
            }
        }

        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        List<Chapter> oldData = chapters;
        chapters = data;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(data);
        }

        // Invalidate the old data as we don't need it any more.
        if (oldData != null && oldData != data) {
            onReleaseResources(oldData);
        }
    }

    @Override
    protected void onStartLoading() {
        if (chapters != null) {
            deliverResult(chapters);
        }

        if (mChapterObserver == null) {
            mChapterObserver = new ChapterIntentReceiver(this);
        }

        if (takeContentChanged() || chapters == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();	 // Ensure the loader has been stopped.

        // At this point we can release the resources associated with 'mData'.
        if (chapters != null) {
            onReleaseResources(chapters);
            chapters = null;
        }

        if (mChapterObserver != null) {
            getContext().unregisterReceiver(mChapterObserver);
            mChapterObserver = null;
        }
    }

    @Override
    public void onCanceled(List<Chapter> chaps) {
        super.onCanceled(chaps);

        // The load has been canceled, so we should release the resources
        // associated with 'chaps'.
        onReleaseResources(chaps);
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(List<Chapter> data) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }

    public static class ChapterIntentReceiver extends BroadcastReceiver {
        final LatestChaptersLoader mLoader;

        public ChapterIntentReceiver(LatestChaptersLoader loader) {
            mLoader = loader;
            mLoader.getContext().registerReceiver(this, new IntentFilter(ACTION_RELOAD));
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "There are new chapters in the database. Chapters will be load again");
            mLoader.onContentChanged();
        }
    }
}
package com.emanga.emanga.app.fragments;

/**
 * Created by Ciro on 24/03/2014.
 */

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.emanga.emanga.app.R;
import com.emanga.emanga.app.activities.ReaderActivity;
import com.emanga.emanga.app.adapters.ThumbnailChapterAdapter;
import com.emanga.emanga.app.cache.ImageCacheManager;
import com.emanga.emanga.app.database.OrmliteFragment;
import com.emanga.emanga.app.listeners.CoverListener;
import com.emanga.emanga.app.models.Chapter;
import com.emanga.emanga.app.utils.CustomNetworkImageView;
import com.emanga.emanga.app.utils.Internet;
import com.emanga.emanga.app.utils.Notification;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

/**
 * A fragment that with history
 */
public class HistorySectionFragment extends OrmliteFragment {

    public final static String TAG = HistorySectionFragment.class.getSimpleName();

    private GridView mGridView;
    private ItemAdapter mAdapter;

    @Override
    public void onStart(){
        super.onStart();
        // Get chapters from database
        new LoadHistory().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Set view fot this fragment
        View rootView = inflater.inflate(
                R.layout.fragment_section, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.grid_view);
        mAdapter = new ItemAdapter(getActivity());


        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if(Internet.checkConnection(getActivity())) {
                    Intent intent = new Intent(getActivity(), ReaderActivity.class);
                    Chapter chapter = mAdapter.getItem(position);
                    intent.putExtra(ReaderActivity.ACTION_OPEN_CHAPTER, chapter);

                    Notification.enjoyReading(getActivity()).show();
                    startActivity(intent);
                } else {
                    Notification.errorMessage(
                            getActivity(),
                            getResources().getString(R.string.volley_error_title),
                            getResources().getString(R.string.connectivity_error_body),
                            R.drawable.stop
                    );
                }
            }
        });

        return rootView;
    }

    private static class ItemAdapter extends BaseAdapter{
        private Context mContext;
        public Chapter[] mChapters;

        public ItemAdapter(Context context){
            mContext = context;
        }

        public int getCount() {
            return mChapters.length;
        }

        public Chapter getItem(int position) {
            return mChapters[position];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            // if it's not recycled, initialize some attributes
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.thumbnail_read_item, parent, false);

                holder = new ViewHolder();
                holder.date = (TextView) convertView.findViewById(R.id.thumb_read_date);
                holder.number = (TextView) convertView.findViewById(R.id.thumb_read_number);
                holder.cover = (CustomNetworkImageView) convertView.findViewById(R.id.thumb_read_cover);
                holder.cover.setErrorImageResId(R.drawable.empty_cover);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Chapter chapter = getItem(position);

            holder.date.setText(ThumbnailChapterAdapter.formatDate(chapter.read));
            holder.number.setText(chapter.number + "");

            holder.cover.setImageUrl(chapter.manga.cover, ImageCacheManager.getInstance().getImageLoader(), new CoverListener(chapter.manga, holder.cover));

            return convertView;
        }
    }

    static class ViewHolder {
        TextView date;
        CustomNetworkImageView cover;
        TextView number;
    }

    private class LoadHistory extends AsyncTask<Void, Integer, Chapter[]> {

        @Override
        protected Chapter[] doInBackground(Void... arg0) {
            List<Chapter> chapters = null;
            try {
                QueryBuilder<Chapter, String> qBc = getHelper().getChapterRunDao().queryBuilder();
                qBc.where().isNotNull(Chapter.READ_COLUMN_NAME);
                qBc.orderBy(Chapter.READ_COLUMN_NAME, false);
                qBc.groupBy(Chapter.MANGA_COLUMN_NAME);

                chapters = qBc.query();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return (chapters != null)? chapters.toArray(new Chapter[chapters.size()]) : null;
        }

        @Override
        protected void onPostExecute(Chapter[] result) {
            if(result != null){
                mAdapter.mChapters = result;
                mGridView.setAdapter(mAdapter);
            }
        }
    }
}

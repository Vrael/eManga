package com.emanga.emanga.app.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.emanga.emanga.app.R;
import com.emanga.emanga.app.cache.ImageCacheManager;
import com.emanga.emanga.app.database.DatabaseHelper;
import com.emanga.emanga.app.listeners.CoverListener;
import com.emanga.emanga.app.models.Genre;
import com.emanga.emanga.app.models.Manga;
import com.emanga.emanga.app.utils.CustomNetworkImageView;
import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import org.apache.commons.lang.WordUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Ciro on 25/05/2014.
 */
public class MangaItemListAdapter extends BaseAdapter implements SectionIndexer{

    private static String sections = " -0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private AlphabetIndexer mAlphabetIndexer;
    private Context mContext;
    private ImageLoader mImageLoader;
    private DatabaseHelper databaseHelper;
    private CloseableIterator<Manga> itMangas;
    private AndroidDatabaseResults mMangas;

    private PreparedQuery<Manga> mangaQuery = null;

    public MangaItemListAdapter(Context context){
        super();
        mContext = context;
        mImageLoader = ImageCacheManager.getInstance().getImageLoader();
        databaseHelper = OpenHelperManager.getHelper(mContext, DatabaseHelper.class);
        try{
            mangaQuery = makeMangasQuery();
            reload();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private PreparedQuery<Manga> makeMangasQuery() throws SQLException {
        QueryBuilder<Manga,String> mangaQb = databaseHelper.getMangaRunDao().queryBuilder();
        mangaQb.orderBy(Manga.TITLE_COLUMN_NAME, true);
        return mangaQb.prepare();
    }

    public void reload(){
        if(itMangas != null){
            itMangas.closeQuietly();
        }

        try{
            itMangas = databaseHelper.getMangaRunDao().iterator(mangaQuery);
            mMangas = (AndroidDatabaseResults) itMangas.getRawResults();
            mAlphabetIndexer = new AlphabetIndexer(mMangas.getRawCursor(),
                mMangas.findColumn(Manga.TITLE_COLUMN_NAME), sections);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void destroy(){
        if(databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
        if(itMangas != null){
            itMangas.closeQuietly();
            itMangas = null;
            mMangas = null;
        }
    }

    @Override
    public int getPositionForSection(int section) {
        return mAlphabetIndexer.getPositionForSection(section);
    }

    @Override
    public int getSectionForPosition(int position) {
        return mAlphabetIndexer.getSectionForPosition(position);
    }

    @Override
    public Object[] getSections() {
        String[] sectionsArr = new String[sections.length()];
        for (int i=0; i < sections.length(); i++)
            sectionsArr[i] = "" + sections.charAt(i);

        return sectionsArr;
    }

    @Override
    public int getCount() {
        return mMangas.getCount();
    }

    @Override
    public Manga getItem(int position) {
        mMangas.moveAbsolute(position);
        try {
            return itMangas.current();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        Manga manga = getItem(position);

        // if it's not recycled, initialize some attributes
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.manga_item_list, parent, false);

            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.manga_list_title);
            holder.cover = (CustomNetworkImageView) convertView.findViewById(R.id.manga_list_cover);
            holder.categories = (TextView) convertView.findViewById(R.id.manga_list_categories);

            holder.cover.setErrorImageResId(R.drawable.empty_cover);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(manga.title.toUpperCase());

        new AsyncTask<Manga, Void, Manga>(){

            @Override
            protected Manga doInBackground(Manga... manga) {
                try {
                    manga[0].genres = databaseHelper.genresForManga(manga[0]);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return manga[0];
            }

            @Override
            protected void onPostExecute(Manga manga){
                holder.categories.setText(manga.genres != null? MangaItemListAdapter.toString(manga.genres) : "");
            }

        }.execute(manga);

        holder.cover.setImageUrl(manga.cover, mImageLoader, new CoverListener(manga.cover, holder.cover));

        return convertView;
    }

    static class ViewHolder {
        TextView title;
        CustomNetworkImageView cover;
        TextView categories;
    }

    private static String toString(List<Genre> genres){
        String list = "";
        StringBuilder sb = new StringBuilder();
        for (Genre g : genres) {
            sb.append(list).append(WordUtils.capitalize(g.name));
            list = ", ";
        }
        return sb.toString();
    }
}

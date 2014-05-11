package com.emanga.emanga.app.adapters;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.emanga.emanga.app.R;
import com.emanga.emanga.app.cache.ImageCacheManager;
import com.emanga.emanga.app.listeners.CoverListener;
import com.emanga.emanga.app.models.Genre;
import com.emanga.emanga.app.models.Manga;
import com.emanga.emanga.app.utils.CustomNetworkImageView;

import org.apache.commons.lang.WordUtils;

public class MangaItemListCursorAdapter extends SimpleCursorAdapter
implements SectionIndexer {

    private LayoutInflater mInflater;

    private AlphabetIndexer mAlphaIndexer;

    private ImageLoader imageLoader;

    public MangaItemListCursorAdapter(Context context, int layout, Cursor c,
                                      String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);

        mInflater = LayoutInflater.from(context);

        imageLoader = ImageCacheManager.getInstance().getImageLoader();
    }

    public int getPositionForSection(int section) {
        return mAlphaIndexer.getPositionForSection(section);
    }

    public int getSectionForPosition(int position) {
        return mAlphaIndexer.getSectionForPosition(position);
    }

    public Object[] getSections() {
        return mAlphaIndexer.getSections();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View convertView = mInflater.inflate(R.layout.manga_item_list, parent, false);

        final ViewHolder holder = new ViewHolder();
        ;
        holder.title = (TextView) convertView.findViewById(R.id.manga_list_title);
        holder.cover = (CustomNetworkImageView) convertView.findViewById(R.id.manga_list_cover);
        holder.categories = (TextView) convertView.findViewById(R.id.manga_list_categories);

        convertView.setTag(holder);
        return convertView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        holder.title.setText(cursor.getString(cursor.getColumnIndex(Manga.TITLE_COLUMN_NAME)));
        holder.categories.setText(WordUtils.capitalize(cursor.getString(cursor.getColumnIndex(Genre.NAME_COLUMN_NAME))));

        String url = cursor.getString(cursor.getColumnIndex(Manga.COVER_COLUMN_NAME));
        holder.cover.setImageUrl(url, imageLoader, new CoverListener(url, holder.cover));
    }

    public Cursor swapCursor(Cursor c) {
        Cursor mCursor = getCursor();
        if (mCursor != null) {
            mCursor.close();
        }
        // Create our indexer
        if (c != null) {
            mAlphaIndexer = new AlphabetIndexer(c, c.getColumnIndex(Manga.TITLE_COLUMN_NAME),
                    " -0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        }
        notifyDataSetChanged();
        return super.swapCursor(c);
    }

    static class ViewHolder {
        TextView title;
        CustomNetworkImageView cover;
        TextView categories;
    }
}
package com.emanga.emanga.app.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.emanga.emanga.app.fragments.ChapterPageFragment;
import com.emanga.emanga.app.models.Page;

import java.util.LinkedList;

/**
 * Created by Ciro on 12/05/2014.
 */
// Adapter for fragments which contains the ImageViews children
public class ImagePagerAdapter extends FragmentStatePagerAdapter {
    public LinkedList<Page> pages = new LinkedList<Page>();

    public ImagePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public Fragment getItem(int position) {
        return ChapterPageFragment.newInstance(pages.get(position));
    }

    @Override
    public int getItemPosition(Object object){
        return PagerAdapter.POSITION_NONE;
    }
}

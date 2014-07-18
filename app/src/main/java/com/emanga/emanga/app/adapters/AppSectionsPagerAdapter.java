package com.emanga.emanga.app.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.emanga.emanga.app.R;
import com.emanga.emanga.app.fragments.HistorySectionFragment;
import com.emanga.emanga.app.fragments.LatestSectionFragment;
import com.emanga.emanga.app.fragments.LibrarySectionFragment;

/**
 * Created by Ciro on 31/05/2014.
 */
/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
 * sections of the app.
 */
public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;

    public AppSectionsPagerAdapter(FragmentManager fm, Context ctx) {
        super(fm);
        mContext = ctx;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return new LatestSectionFragment();
            case 1:
                return new LibrarySectionFragment();
            default:
                return new HistorySectionFragment();

        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getStringArray(R.array.tabs_titles)[position];
    }
}
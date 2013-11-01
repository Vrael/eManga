package com.emanga.activities;

import java.net.MalformedURLException;
import java.sql.SQLException;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;

import com.emanga.R;
import com.emanga.fragments.ChapterPageFragment;
import com.emanga.loaders.ReaderLoader;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class ReaderActivity extends FragmentActivity 
	implements LoaderManager.LoaderCallbacks<String[]>{

	public static final String INTENT_CHAPTER_ID = "chapterId";
	
	private static final String ACTION = "com.manga.intent.action";
	public static final String ACTION_OPEN_CHAPTER = ACTION + ".openChapter";
	
	private int chapterId;
	private ImagePagerAdapter mAdapter;
	private ViewPager mPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_reader);
		
		chapterId = getIntent().getIntExtra(ReaderActivity.ACTION_OPEN_CHAPTER, -1);
		
		mAdapter = new ImagePagerAdapter(getSupportFragmentManager());
		mPager = (ViewPager) findViewById(R.id.fullscreen_pager);
		mPager.setAdapter(mAdapter);
		
		getSupportLoaderManager().initLoader(9, null, this);
	}
	
	// Adapter for framents which contains the ImageViews children
	public static class ImagePagerAdapter extends FragmentStatePagerAdapter {
        public String[] pagesLinks = new String[0];

        public ImagePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return pagesLinks.length;
        }

        @Override
        public Fragment getItem(int position) {
            return ChapterPageFragment.newInstance(pagesLinks[position]);
        }
    }
	
	public Loader<String[]> onCreateLoader(int id, Bundle args) {
		try {
			return new ReaderLoader(this, chapterId);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void onLoadFinished(Loader<String[]> loader, String[] links) {
		mAdapter.pagesLinks = links;
		mAdapter.notifyDataSetChanged();
	}

	public void onLoaderReset(Loader<String[]> links) {
		mAdapter.pagesLinks = null;
	}
}

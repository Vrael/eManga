package com.emanga.activities;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.emanga.R;
import com.emanga.fragments.ChapterPageFragment;
import com.emanga.services.PagesService;
import com.emanga.utils.CustomViewPager;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class ReaderActivity extends FragmentActivity {

	public static final String TAG = "ReaderActivity";
	
	public static final String INTENT_CHAPTER_ID = "chapterId";
	
	public static final String ACTION_OPEN_CHAPTER = "com.manga.intent.action" 
			+ TAG + ".openChapter";
	
	private int chapterId;
	private ImagePagerAdapter mAdapter;
	private CustomViewPager mPager;
	
	// This Receiver updates urls of pages from Pages Service
	private BroadcastReceiver mPageReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	Log.d(TAG, "Received new url image from Page Service");
	    	String url = intent.getStringExtra(PagesService.EXTRA_PAGE_URL);
	    	if(url != null){
	    		mAdapter.pagesLinks.add(url);
	    		mAdapter.notifyDataSetChanged();
	    	}
	   }
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_reader);
		chapterId = getIntent().getIntExtra(ACTION_OPEN_CHAPTER, 0);
		
		Intent intent = new Intent(this, PagesService.class);
		intent.putExtra(PagesService.EXTRA_CHAPTER_ID, chapterId);
		startService(intent);
		
		mAdapter = new ImagePagerAdapter(getSupportFragmentManager());
		mPager = (CustomViewPager) findViewById(R.id.fullscreen_pager);
		mPager.setAdapter(mAdapter);
		
		registerReceiver(mPageReceiver, new IntentFilter(PagesService.ACTION_ADD_PAGE));
	}
	
	// Adapter for framents which contains the ImageViews children
	public static class ImagePagerAdapter extends FragmentPagerAdapter {
        public ArrayList<String> pagesLinks = new ArrayList<String>(30);

        public ImagePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return pagesLinks.size();
        }

        @Override
        public Fragment getItem(int position) {
            return ChapterPageFragment.newInstance(pagesLinks.get(position));
        }
    }
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		unregisterReceiver(mPageReceiver);
	}
}

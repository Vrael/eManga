package com.emanga.emanga.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.emanga.emanga.app.R;

public class LibrarySectionFragment extends Fragment {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	public static boolean mTwoPane;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
            Bundle savedInstanceState) {
		
		View view = getLayoutInflater(savedInstanceState).inflate(R.layout.manga_list, parent, false);
		
		if (view.findViewById(R.id.manga_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((MangaListFragment) getFragmentManager().findFragmentById(
					R.id.manga_list)).setActivateOnItemClick(true);
		}

    	return view;
    }
}

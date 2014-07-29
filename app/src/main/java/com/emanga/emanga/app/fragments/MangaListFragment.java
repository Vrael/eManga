package com.emanga.emanga.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.emanga.emanga.app.R;
import com.emanga.emanga.app.activities.MainActivity;
import com.emanga.emanga.app.adapters.MangaItemListAdapter;
import com.emanga.emanga.app.controllers.App;
import com.emanga.emanga.app.database.DatabaseHelper;
import com.emanga.emanga.app.models.Manga;
import com.emanga.emanga.app.requests.MangasRequest;
import com.emanga.emanga.app.utils.Internet;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * A list fragment representing a list of Mangas. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link MangaDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class MangaListFragment extends ListFragment {

    private static final String TAG = MangaListFragment.class.getName();

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sMangaCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
        public void onItemSelected(Manga manga);
	}

	/**
	 * A manga implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sMangaCallbacks = new Callbacks() {
        public void onItemSelected(Manga manga){}
	};

    private MangasRequest mangasRequest;
    private boolean sync;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MangaListFragment() {}

	// private MangaItemListCursorAdapter mAdapter;
    private MangaItemListAdapter mAdapter;

    private DatabaseHelper databaseHelper = null;
	
	protected DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper =
                OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
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
        if(mAdapter != null){
            mAdapter.destroy();
            mAdapter = null;
        }
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mAdapter = new MangaItemListAdapter(this.getActivity());
        setListAdapter(mAdapter);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.manga_fragment_list, container, false);

    	return view;
    }

	@Override
    public void onActivityCreated(Bundle saved) { 
        super.onActivityCreated(saved);
        
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

    @Override
    public void onResume(){
        if(!sync){
            new AsyncTask<Void,Void,Void>(){

                @Override
                protected Void doInBackground(Void... voids) {
                    String mangaDate = getHelper().lastMangaDate();

                    try {
                        mangaDate = URLEncoder.encode(mangaDate, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    mangasRequest = new MangasRequest(
                            Request.Method.GET,
                            Internet.HOST + "mangas/newest?m=" + mangaDate,
                            new Response.Listener<Manga[]>() {
                                @Override
                                public void onResponse(final Manga[] mangas){
                                    new AsyncTask<Void,Void,Void>(){
                                        @Override
                                        protected Void doInBackground(Void... voids) {
                                            Log.d(TAG, "Mangas received and parsed: " + mangas.length);
                                            getHelper().saveMangas(mangas);

                                            // Manage the adapter needs to be in the Main UI
                                            Handler mainHandler = new Handler(Looper.getMainLooper());

                                            mainHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mAdapter.reload();
                                                    mAdapter.notifyDataSetChanged();

                                                    // Notify for hide the progressbar
                                                    LocalBroadcastManager.getInstance(App.getInstance().getApplicationContext())
                                                            .sendBroadcast(new Intent(MainActivity.ACTION_TASK_ENDED));
                                                }
                                            });
                                            getHelper().updateMangaFTS();
                                            return null;
                                        }
                                    }.execute();

                                    // Mark the request as done
                                    sync = true;
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    Log.e(TAG, volleyError.toString());
                                    // Notify for hide the progressbar
                                    LocalBroadcastManager.getInstance(App.getInstance().getApplicationContext())
                                            .sendBroadcast(new Intent(MainActivity.ACTION_TASK_ENDED));
                                }
                            });

                    mangasRequest.setRetryPolicy(new DefaultRetryPolicy(
                                    2 * 60 * 1000,
                                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                    );

                    App.getInstance().addToRequestQueue(mangasRequest, MangasRequest.TAG + "." + TAG);

                    return null;
                }
            }.execute();
        }

        super.onResume();
    }

    @Override
    public void onPause(){
        if(!sync){
            Log.d(TAG, "Canceling Request");
            mangasRequest.cancel();
            App.getInstance().mRequestQueue.cancelAll(MangasRequest.TAG + "." + TAG);
        }

        super.onPause();
    }

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sMangaCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		listView.setItemChecked(position, true);
		
		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(mAdapter.getItem(position));

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
}

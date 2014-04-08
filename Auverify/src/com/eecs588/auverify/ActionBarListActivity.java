package com.eecs588.auverify;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public abstract class ActionBarListActivity extends ActionBarActivity {
    private ListView mListView;

	protected ListView getListView() {
	    if (mListView == null)
	        mListView = (ListView) findViewById(R.id.list);
	    if (mListView == null)
        	Log.d("MyApp", "ListView is null!");
	    return mListView;
	}
	
	protected void setListAdapter(ListAdapter adapter) {
	    getListView().setAdapter(adapter);
	}
	
	protected ListAdapter getListAdapter() {
	    ListAdapter adapter = getListView().getAdapter();
	    if (adapter instanceof HeaderViewListAdapter) {
	        return ((HeaderViewListAdapter)adapter).getWrappedAdapter();
	    } else {
	        return adapter;
	    }
	}
}
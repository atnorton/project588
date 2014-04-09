package com.eecs588.auverify;

import java.util.ArrayList;
import java.util.Map.Entry;

import com.google.android.apps.authenticator.PasscodeGenerator;
import com.google.android.apps.authenticator.TOTPUtility;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.os.Build;


public class Accounts extends ActionBarListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_accounts);
		
		final Handler handler = new Handler();
		handler.post(new Runnable() {
			   @Override
			   public void run() {
				  initListView();
			      handler.postDelayed(this, 1000);
			   }
			});
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}
	
	private void initListView()
	{
	    final String[] matrix  = { "_id", "name", "code" };
	    final String[] columns = { "name", "code"  };
	    final int[]    layouts = { android.R.id.text1, android.R.id.text2 };

	    MatrixCursor  cursor = new MatrixCursor(matrix);
	    int key = 0;
	    String prefs_name = getString(R.string.prefs_name);
		SharedPreferences prefs = getSharedPreferences(prefs_name, MODE_PRIVATE);
		for( Entry<String, ?> entry : prefs.getAll().entrySet() ) {
			if (entry.getKey().equals("hname"))
				continue;
			if (entry.getKey().equals("pword"))
				continue;
			if (entry.getKey().equals("uname"))
				continue;
			if (entry.getKey().contains("address"))
				continue;
			String code = "";
			try {
				code = TOTPUtility.getCurrentCode((String) entry.getValue());
			} catch (Exception e) {
				e.printStackTrace();
			}
			cursor.addRow(new Object[] { key++, entry.getKey(), code });
		}

	    SimpleCursorAdapter data =
	        new SimpleCursorAdapter(this,
	                R.layout.unlock_list,
	                cursor,
	                columns,
	                layouts);

	    setListAdapter( data );
	    
	    ListView listView = getListView();
	    listView.setTextFilterEnabled(true);
	    
	    
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d("MyApp", "pos " + position + " clicked");
			}
		});

	}
	
	public void myUnlockHandler(View v){
		LinearLayout vwParentRow = (LinearLayout)v.getParent();
		CharSequence host = ((TextView)vwParentRow.getChildAt(0)).getText();
		Log.d("MyApp", "Request to unlock " + host);
		
		Intent myIntent = new Intent(Accounts.this, CameraTestActivity.class);
		myIntent.putExtra("is_unlock", true);
		myIntent.putExtra("host", host);
		Accounts.this.startActivity(myIntent);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.accounts, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_accounts) {
			return true;
		}
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, Settings.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_accounts,
					container, false);
			return rootView;
		}
	}

}

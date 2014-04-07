package com.eecs588.auverify;

import com.eecs588.auverify.R;
import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class Settings extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		SharedPreferences settings = getSharedPreferences(getString(R.string.prefs_name), MODE_PRIVATE);
		String username = settings.getString("uname", "");
		String password = settings.getString("pword", "");
		String hostname = settings.getString("hname", "");
		
		EditText edit_username = (EditText) findViewById(R.id.username);
		EditText edit_password = (EditText) findViewById(R.id.password);
		EditText edit_hostname = (EditText) findViewById(R.id.hostname);
		
		edit_username.setText(username);
		edit_password.setText(password);
		edit_hostname.setText(hostname);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
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
			View rootView = inflater.inflate(R.layout.fragment_settings,
					container, false);
			
			return rootView;
		}
	}
	
	public void updateSettings(View view) {
	    // Do something in response to button
		SharedPreferences settings = getSharedPreferences(getString(R.string.prefs_name), MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		EditText username = (EditText) findViewById(R.id.username);
		EditText password = (EditText) findViewById(R.id.password);
		EditText hostname = (EditText) findViewById(R.id.hostname);
		editor.putString("uname", username.getText().toString());
		editor.putString("pword", password.getText().toString());
		editor.putString("hname", hostname.getText().toString());
		editor.commit();
	}

}


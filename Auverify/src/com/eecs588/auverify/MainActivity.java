package com.eecs588.auverify;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.eecs588.auverify.R;

public class MainActivity extends Activity {
	public static final String PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String username = settings.getString("uname", "");
        Log.v("MyApp", "UNAME_create: "+username);
        if(username.equals("")) {
        	Intent intent = new Intent(this, Settings.class);
			startActivity(intent);
        }
                
        
        Intent intent = new Intent(this, EmailRetreiver.class);
        startService(intent);

        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			
			Intent intent = new Intent(this, Settings.class);
			startActivity(intent);
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    
}
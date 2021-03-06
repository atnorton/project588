package com.eecs588.auverify;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;


import com.eecs588.auverify.R;
import com.google.android.apps.authenticator.PasscodeGenerator;
import com.google.android.apps.authenticator.TOTPUtility;

public class MainActivity extends Activity {

	Boolean myAnimationLoaded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent i = getIntent();
        if(i!=null && i.getData()!=null) {
        	Log.v("Auverify", "path: " + i.getData().getPath());
        }
        final String secret = "p3im76r6cu3kb32k";
        
        try {
			Log.v("Auverify", "code: " + TOTPUtility.getCurrentCode(secret));
			Log.v("Auverify", "interval: " + PasscodeGenerator.INTERVAL);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        String prefs_name = getString(R.string.prefs_name);
        SharedPreferences settings = getSharedPreferences(prefs_name, MODE_PRIVATE);
        if (settings.getInt("radius", -1) == -1){
        	SharedPreferences.Editor prefEditor = settings.edit();
        	prefEditor.putInt("radius", 10);
        	prefEditor.commit();
        }

	    Log.v("Auverify", settings.getAll().toString());
	    
        String username = settings.getString("uname", "");
        Log.v("Auverify", "UNAME_create: "+username);
        if(username.equals("")) {
        	Intent intent = new Intent(this, Settings.class);
			startActivity(intent);
        }

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
		if (id == R.id.action_accounts) {
			
			Intent intent = new Intent(this, Accounts.class);
			startActivity(intent);
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		if (!myAnimationLoaded){
			StartLoading();
		    myAnimationLoaded = true;
		}
		
		Intent intent = new Intent(this, EmailRetreiver.class);
		Intent i = getIntent();
		
		// Mobile log in
        if(i  !=null && i.getData() != null) {
        	String path = i.getData().getPath();
        	String user_token = path.substring(path.lastIndexOf("/")+1);
        	intent.putExtra("user_token", user_token);
        	Log.v("Auverify", "user_token: " + user_token);
        	startService(intent);
        	return;
        }

		Bundle b = i.getExtras();
		if (b == null || !b.getBoolean("is_unlock")){
			Log.d("Auverify", "Starting EmailRetreiver normally");
	        startService(intent);
		}
		else{
			Log.d("Auverify", "Starting EmailRetreiver in unlock mode");
			intent.putExtra("user_token", b.getString("user_token"));
			startService(intent);
			return;
		}
		
		if (b == null || b.getString("post_success") == null) return;
		if (b.getString("post_success").equals("success")){
			CharSequence text = "Log in succeeded!";
			int duration = Toast.LENGTH_LONG;

			Toast toast = Toast.makeText(getApplicationContext(), text, duration);
			toast.show();
		}
		else if (b.getString("post_success").equals("failure")){
			CharSequence text = "Log in failed...";
			int duration = Toast.LENGTH_LONG;

			Toast toast = Toast.makeText(getApplicationContext(), text, duration);
			toast.show();
		}
	}

    @Override
    protected void onPause(){
    	super.onPause();
    	if (myAnimationLoaded){
    		StopLoading();
    	    myAnimationLoaded = false;
    	}
    	    
        Log.v("Auverify", "Stopping service");
        	
        Intent intent = new Intent(this, EmailRetreiver.class);
        stopService(intent);
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    }

    public void StartLoading() {
        ImageView refreshImage = (ImageView) findViewById(R.id.anim_example);
        if (refreshImage == null){
        	Log.d("Auverify", "ImageView is null!");
        	return;
        }
        refreshImage.setImageDrawable(getResources().getDrawable(R.drawable.loading_icon));
        Animation rotateLoading = AnimationUtils.loadAnimation(this, R.anim.rotate);
        refreshImage.clearAnimation();
        refreshImage.setAnimation(rotateLoading);
    }

    public void StopLoading() {
        ImageView refreshImage = (ImageView) findViewById(R.id.anim_example);
        if (refreshImage.getAnimation() != null){
            refreshImage.clearAnimation();
            refreshImage.setImageDrawable(getResources().getDrawable(R.drawable.loading_icon));
        }
    }
}
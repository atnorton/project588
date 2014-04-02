package com.eecs588.auverify;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.eecs588.auverify.R;

public class MainActivity extends Activity {
	public class ReceiveMessages extends BroadcastReceiver 
	{
	@Override
	   public void onReceive(Context context, Intent intent) 
	   {    
	   }
	};
	
	public static final String PREFS_NAME = "MyPrefsFile";
	ReceiveMessages myReceiver = null;
	Boolean myReceiverIsRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        myReceiver = new ReceiveMessages();
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
	
	@Override
	protected void onResume(){
		super.onResume();
		if (!myReceiverIsRegistered) {
			StartLoading();
		    registerReceiver(myReceiver, new IntentFilter("com.mycompany.myapp.SOME_MESSAGE"));
		    myReceiverIsRegistered = true;
		}
		Bundle b = getIntent().getExtras();
		if (b == null) return;
		if (b.getString("post_success").equals("success")){
			Context context = getApplicationContext();
			CharSequence text = "Log in succeeded!";
			int duration = Toast.LENGTH_LONG;

			Toast toast = Toast.makeText(getApplicationContext(), text, duration);
			toast.show();
		}
		else if (b.getString("post_success").equals("failure")){
			Context context = getApplicationContext();
			CharSequence text = "Log in failed...";
			int duration = Toast.LENGTH_LONG;

			Toast toast = Toast.makeText(getApplicationContext(), text, duration);
			toast.show();
		}
	}

    @Override
    protected void onPause(){
    	super.onPause();
    	if (myReceiverIsRegistered) {
    		StopLoading();
    	    unregisterReceiver(myReceiver);
    	    myReceiverIsRegistered = false;
    	}
    }
    
    public void RunAnimation(View v)
    {	
        //The onClick method has to be present and must take the above parameter.
        StartLoading();
    }

    public void StartLoading() {
        ImageView refreshImage = (ImageView) findViewById(R.id.anim_example);
        if (refreshImage == null){
        	Log.d("MyApp", "ImageView is null!");
        	return;
        }
        refreshImage.setImageDrawable(getResources().getDrawable(R.drawable.loading_icon));
        Animation rotateLoading = AnimationUtils.loadAnimation(this, R.anim.rotate);
        refreshImage.clearAnimation();
        refreshImage.setAnimation(rotateLoading);
    }

    public void StopLoading() {
        ImageView refreshImage = (ImageView) findViewById(R.id.anim_example);
        if (refreshImage.getAnimation() != null)
        {
            refreshImage.clearAnimation();
            refreshImage.setImageDrawable(getResources().getDrawable(R.drawable.loading_icon));
        }
    }
}
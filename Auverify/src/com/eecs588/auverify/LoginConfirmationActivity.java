package com.eecs588.auverify;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class LoginConfirmationActivity extends Activity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_confirmation);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		Bundle b = getIntent().getExtras();
        TextView tv = (TextView) findViewById(R.id.confirmation_text);
        String text = b.getString("host") + " is requesting login authentication.";
        text += "\n From " + Math.round(b.getDouble("distance")) + " mile(s) away.\n";
        text += "Do you want to log in?";
        
        tv.setText(text);
	}
	
	public void goToCamera(View v){		
		Bundle b = getIntent().getExtras();
		Intent myIntent;
		
		// Go straight to POST if unlocking
		if(b.getString("user_token")!= null && !b.getString("user_token").isEmpty()){
			myIntent = new Intent(this, POSTActivity.class);
			myIntent.putExtra("user_token", b.getString("user_token"));
		} 
		else
			myIntent = new Intent(this, CameraTestActivity.class);

		myIntent.putExtra("email_token", b.getString("email_token"));
		myIntent.putExtra("address", b.getString("address"));
		myIntent.putExtra("host", b.getString("host"));
		
		this.startActivity(myIntent);
	}
	
	public void goToMain(View v){
		Intent myIntent = new Intent(this, MainActivity.class);
		this.startActivity(myIntent);
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
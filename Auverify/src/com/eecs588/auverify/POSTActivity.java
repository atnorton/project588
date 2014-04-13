package com.eecs588.auverify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.apps.authenticator.TOTPUtility;

public class POSTActivity extends Activity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
	}
	
	@Override
    protected void onResume(){
		super.onResume();
		
		// Start POST thread
		Bundle bundle = getIntent().getExtras();
		Boolean is_unlock = bundle.getBoolean("is_unlock", false);
        String address = bundle.getString("address");

        String email_token = bundle.getString("email_token");
        String qr_data = bundle.getString("user_token");
        Runnable r = new sendPOSTThread(address, email_token, qr_data, is_unlock);
        new Thread(r).start();
	}
	
    private class sendPOSTThread implements Runnable {

    	private String address, email_token, user_token;
    	private Boolean is_unlock;
    	public sendPOSTThread(String address, String email_token, String user_token, Boolean is_unlock) {
    		this.address = address;
    		this.email_token = email_token;
    	    this.user_token = user_token;
    	    this.is_unlock = is_unlock;
    	}

		public void run() {
    		// Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(address);

            try {
            	Bundle bundle = getIntent().getExtras();
            	String host = bundle.getString("host");
            	String prefs_name = getString(R.string.prefs_name);
				SharedPreferences settings = getSharedPreferences(prefs_name, MODE_PRIVATE);
			    SharedPreferences.Editor prefEditor = settings.edit();
			    String shared_secret = settings.getString(host, "");
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

                if (is_unlock){
                	nameValuePairs.add(new BasicNameValuePair("unlock[user_token]", user_token));
                	String curr_code = TOTPUtility.getCurrentCode(shared_secret);
                	nameValuePairs.add(new BasicNameValuePair("unlock[validation_code]", curr_code));
                }
                else{
	                nameValuePairs.add(new BasicNameValuePair("login[email_token]", email_token));
	                nameValuePairs.add(new BasicNameValuePair("login[user_token]", user_token));
	                if (!shared_secret.equals("")){
	                	String curr_code = TOTPUtility.getCurrentCode(shared_secret);
	                	nameValuePairs.add(new BasicNameValuePair("login[validation_code]", curr_code));
	                }
                }
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                String post = EntityUtils.toString(httppost.getEntity());
                Log.d("Auverify", "Sending post: " + post);
                String result = EntityUtils.toString(response.getEntity());
                if (result != null)
                	Log.d("Auverify", "Response: " + result);
                else
                	Log.d("Auverify", "No response");
                
                // On first time, add shared secret and address
                if (!result.equals("success") && !result.equals("failure")){
                	Log.d("Auverify", "Storing host: " + host);

               		prefEditor.putString(host, result);
               		
                	address = address.substring(0, address.indexOf("authenticate"));
                	prefEditor.putString(host + "address", address);

    			    prefEditor.commit();
                }

                // Go back to main activity
                Intent myIntent = new Intent(POSTActivity.this, MainActivity.class);
        		String s;
        		if (result.equals("failure"))
        			s = "failure";
        		else
        			s = "success";
        		myIntent.putExtra("post_success", s);
        		myIntent.putExtra("is_unlock", is_unlock);
        		myIntent.putExtra("unlock_user_token", user_token);
        		Log.d("Auverify", "Returning to MainActivity");
        		POSTActivity.this.startActivity(myIntent);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
				e.printStackTrace();
			}
    	}
	}
}
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
import android.os.Bundle;
import android.util.Log;

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
        String address = bundle.getString("address");
        String email_token = bundle.getString("token");
        String qr_data = bundle.getString("qr_data");
        Runnable r = new sendPOSTThread(address, email_token, qr_data);
        new Thread(r).start();
	}
	
    private class sendPOSTThread implements Runnable {
    	private String address, email_token, qr_token;
    	public sendPOSTThread(String address, String email_token, String qr_token) {
    		this.address = address;
    		this.email_token = email_token;
    	    this.qr_token = qr_token;
    	}

    	public void run() {
    		// Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(address);

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("login[email_token]", email_token));
                nameValuePairs.add(new BasicNameValuePair("login[user_token]", qr_token));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                String post = EntityUtils.toString(httppost.getEntity());
                Log.d("MyApp", "Sending post: " + post);
                Log.d("MyApp", "Sent POST!");
                String result = EntityUtils.toString(response.getEntity());
                if (result != null)
                	Log.d("MyApp", "Response: " + result);
                else
                	Log.d("MyApp", "No response");
                
                // Go back to main activity
                Intent myIntent = new Intent(POSTActivity.this, MainActivity.class);
        		Bundle bundle = getIntent().getExtras();
        		String s;
        		if (result.equals("failure"))
        			s = "failure";
        		else
        			s = "success";
        		myIntent.putExtra("post_success", s);
        		POSTActivity.this.startActivity(myIntent);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
    	}
	}
}
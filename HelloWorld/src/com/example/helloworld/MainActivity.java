package com.example.helloworld;

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
import android.view.Menu;

public class MainActivity extends Activity {
	
	public class sendPOSTThread implements Runnable {
    	private String email_token, qr_token;
    	public sendPOSTThread(String email_token_, String qr_token_) {
    		email_token = email_token_;
    	    qr_token = qr_token_;
    	}

    	public void run() {
    		// Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("https://www.authdemo.com/authenticate");

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("login[email_token]", email_token));
                nameValuePairs.add(new BasicNameValuePair("login[user_token]", qr_token));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                Log.d("MyApp", "Sent POST!");
                String result = EntityUtils.toString(response.getEntity());
                if (result != null)
                	Log.d("MyApp", "Response: " + result);
                else
                	Log.d("MyApp", "No response");
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
    	}
	}

    /**
     * Return the primary text content of the message.
     */

	/*
	private class EmailListener implements MessageCountListener
	{

		@Override
		public void messagesAdded(MessageCountEvent arg0) {
			Message[] msg_array = arg0.getMessages();
			processMessage(msg_array[0]);
		}

		@Override
		public void messagesRemoved(MessageCountEvent arg0) {}
		
	}
	*/
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, EmailRetreiver.class);
        startService(intent);
        //Runnable r = new sendPOSTThread("hey", "hi");
        //new Thread(r).start();
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    
}
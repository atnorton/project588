package com.eecs588.auverify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.sun.mail.imap.IMAPFolder;

public class EmailRetreiver extends IntentService implements
		MessageCountListener {
	private int NUM_MESSAGES = 10; // This assumes the email is within the
									// latest 10...
	private int TIMEOUT = 60 * 5 * 1000; // 5 minutes in milliseconds
	private Folder inbox;
	public static final String PREFS_NAME = "MyPrefsFile";

	Thread thread;
	Store store;
	String userToken;
	AtomicBoolean isOpen;

	public EmailRetreiver() {
		super("EmailRetreiver");
		
		isOpen = new AtomicBoolean(false);
	}

	public class IdleFolder implements Runnable {
		private IMAPFolder f;
		private Store s;

		public IdleFolder(Store s_, IMAPFolder f_) {
			f = f_;
			s = s_;
		}

		public void run() {
			Log.v("Auverify", "Email listener started...");
			try {
				while (!Thread.interrupted()) {
					f.getMessageCount();
					Thread.sleep(1000);
				}
				
				f.close(false);
				Log.d("Auverify", "Closed IMAPFolder");
				s.close();
			} catch (MessagingException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			Log.v("Auverify", "Email listener finished...");
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		isOpen.set(true);
		super.onCreate();
	}
	
	@Override
	public void onHandleIntent(Intent intent) {
		if(!isOpen.get())
				return;
		
		Log.d("Auverify", "EmailRetreiver created");
		
        if(intent.getStringExtra("user_token")!=null) {
        	userToken = intent.getStringExtra("user_token");
        	Log.v("Auverify", "Started with user_token:" + userToken);
        } else {
        	userToken = null;
        }
		
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		Session session = Session.getDefaultInstance(props, null);
		
		try {
			// Establish connection with inbox
			store = session.getStore("imaps");

			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			String username = settings.getString("uname", "");
			String password = settings.getString("pword", "");
			String hostname = settings.getString("hname", "");

			Log.v("Auverify", "uname: " + username);
			Log.v("Auverify", "hname: " + hostname);

			store.connect(hostname, username, password);

			inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_WRITE);
			
			// Idle the IMAPFolder
			Runnable r = new IdleFolder(store, (IMAPFolder) inbox);
			thread = new Thread(r);
			// Add listener for new emails
			inbox.addMessageCountListener(this);
			thread.start();
			
			// Get most recent messages
			int num_msgs = inbox.getMessageCount();
			Message[] messages = inbox.getMessages(num_msgs - NUM_MESSAGES,
					num_msgs);
			Collections.reverse(Arrays.asList(messages));
			for (int i = 0; i < messages.length; i++) {					
				long time_diff = processMessage(messages[i]);
				if (time_diff > TIMEOUT || !isOpen.get())
					break;
			}

			if(!isOpen.get())
				thread.interrupt();
			
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public void onDestroy() {
		isOpen.set(false);
		super.onDestroy();
		/*try {
			inbox.close(false);
		} catch (MessagingException e) {
			e.printStackTrace();
		}*/
		Log.v("Auverify", "onDestroy called");
		if(thread != null)
			thread.interrupt();
	}

	@Override
	public void messagesAdded(MessageCountEvent arg0) {
		Message[] msg_array = arg0.getMessages();
		processMessage(msg_array[0]);
	}

	@Override
	public void messagesRemoved(MessageCountEvent arg0) {
	}

	// Return how long ago the message was sent so the logic can ignore anything
	// past the timeout
	private long processMessage(Message m) {
		long time_diff = 0;
		try {
			// Check if email was sent recently
			Date sentDate = m.getSentDate();
			String subject = m.getSubject();
			
			// Check if the subject is correct
			if (!subject.equals("Log in request"))
				return time_diff;
			
			time_diff = (System.currentTimeMillis() - sentDate.getTime()) / 1000;
			if (time_diff > TIMEOUT) {
				Log.d("Auverify", "Processed email not sent within time limit");
				return time_diff;
			}

			// Check if the email is already seen
			Flags flags = m.getFlags();
			if (flags.contains(Flags.Flag.SEEN))
				return time_diff;

			// Extract and display the link
			String body = getText(m);
			int idx = body.indexOf("https://");
			if (idx == -1)
				return time_diff;
			
			String link = body.substring(idx).split(" ")[0];
			String email_token = link.substring(link.lastIndexOf("/") + 1, link.length());
			if (email_token.endsWith("\r\n"))
				email_token = email_token.substring(0, email_token.indexOf("\r"));
			String address = link.substring(0, link.lastIndexOf("/"));

			Log.d("Auverify", "Got email token: " + email_token);

			inbox.setFlags(new Message[] { m }, new Flags(Flags.Flag.SEEN),
					true);
			
			
			//Find client ip
			String ip_searchString = "client ip:";
			int ipStart = body.indexOf(ip_searchString) + ip_searchString.length();
			int ipEnd = ipStart;
			while(Character.isLetterOrDigit(body.charAt(ipEnd)) || body.charAt(ipEnd) == '.') {
				ipEnd++;
			}
		
			
			String clientIP = body.substring(ipStart, ipEnd);
			Log.v("MyTag", clientIP);
			Log.v("MyTag", "" + clientIP.length());
			
			
			
			LocationManager locationManager; 
			String context = Context.LOCATION_SERVICE; 
			locationManager = (LocationManager)getSystemService(context); 

			Criteria crta = new Criteria(); 
			crta.setAccuracy(Criteria.ACCURACY_FINE); 
			crta.setAltitudeRequired(false); 
			crta.setBearingRequired(false); 
			crta.setCostAllowed(true); 
			crta.setPowerRequirement(Criteria.POWER_LOW); 
			String provider = locationManager.getBestProvider(crta, true);

			//String provider = LocationManager.NETWORK_PROVIDER;
			// String provider = LocationManager.GPS_PROVIDER; 
			Location location = locationManager.getLastKnownLocation(provider); 
			double my_latitude;
			double my_longitude;
			if(location!=null) { 
				my_latitude = location.getLatitude(); 
				my_longitude = location.getLongitude(); 
			} else {
				return time_diff;
			}
			
			Log.v("MyTag", "my_lat: " + my_latitude);
            Log.v("MyTag", "my_lon: " + my_longitude);
			
				
			
			IPLookup ipl = new IPLookup(clientIP, new Point(my_latitude, my_longitude));
			Thread ipl_thread = new Thread(ipl);
			ipl_thread.start();
			try {
				ipl_thread.join();
			} catch(Exception e) {
				Log.v("MyTag", "Location was NULL");
				return time_diff;
			}
			
			Log.v("MyTag", "dist: " + ipl.getDist());
			
			Intent dialogIntent = new Intent(getBaseContext(),
					LoginConfirmationActivity.class);
			
			Bundle b = new Bundle();
			String host = m.getFrom()[0].toString();
			host = host.substring(0, host.indexOf("<"));
			b.putString("host", host);
			b.putString("email_token", email_token);
			b.putString("address", address);
			b.putDouble("distance", ipl.getDist());
			if (userToken != null)
				b.putString("user_token", userToken);
			dialogIntent.putExtras(b);
			dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplication().startActivity(dialogIntent);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return time_diff;
	}

	private String getText(Part p) throws MessagingException, IOException {
		if (p.isMimeType("text/*")) {
			String s = (String) p.getContent();
			return s;
		}

		if (p.isMimeType("multipart/alternative")) {
			// prefer html text over plain text
			Multipart mp = (Multipart) p.getContent();
			String text = null;
			for (int i = 0; i < mp.getCount(); i++) {
				Part bp = mp.getBodyPart(i);
				if (bp.isMimeType("text/plain")) {
					if (text == null)
						text = getText(bp);
					continue;
				} else if (bp.isMimeType("text/html")) {
					String s = getText(bp);
					if (s != null)
						return s;
				} else {
					return getText(bp);
				}
			}
			return text;
		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				String s = getText(mp.getBodyPart(i));
				if (s != null)
					return s;
			}
		}

		return null;
	}
	
	private static final double earth_rad = 3963.1676; //miles
	private class Point {
		public double lat;
		public double lon;
		
		public Point(String lat_deg, String lon_deg) {
			lat = (Math.PI / 180) * Double.parseDouble(lat_deg);
			lon = (Math.PI / 180) * Double.parseDouble(lon_deg);
		}
		public Point(double lat_deg, double lon_deg) {
			lat = (Math.PI / 180) * lat_deg;
			lon = (Math.PI / 180) * lon_deg;
		}
	}
	
	private static double haver(double ang) {
		return (1 - Math.cos(ang)) / 2;
	}
	
	private static double haver_dist(Point p1, Point p2) {
		double val1 = haver(p2.lat - p1.lat);
		double val2 = Math.cos(p1.lat) * Math.cos(p1.lat) * haver(p2.lon - p1.lon);
		return 2 * earth_rad * Math.asin(Math.sqrt(val1 + val2));
	}
	
	private class IPLookup implements Runnable {
    	private String address;
    	private double dist;
    	private Point my_loc;
    	public IPLookup(String ip, Point ml) {
    		address = "http://freegeoip.net/xml/" + ip;
    		my_loc = ml;
    	}
    	
    	public double getDist() {
    		return dist;
    	}

		public void run() {
    		// Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(address);

            HttpResponse response;
            try {
                response = httpclient.execute(httpget);
                // Examine the response status

                // Get hold of the response entity
                HttpEntity entity = response.getEntity();
                // If the response does not enclose an entity, there is no need
                // to worry about connection release

                if (entity != null) {

                    // A Simple JSON Response Read
                    InputStream instream = entity.getContent();
                    String result= convertStreamToString(instream);
                    // now you have the string representation of the HTML request
                    
                    String lat_begin = "<Latitude>";
                    String lat_close = "</Latitude>";
                    String lon_begin = "<Longitude>";
                    String lon_close = "</Longitude>";
                    
                    int lat_start = result.indexOf(lat_begin) + lat_begin.length();
                    int lat_end = result.indexOf(lat_close);
                    int lon_start = result.indexOf(lon_begin) + lon_begin.length();
                    int lon_end = result.indexOf(lon_close);
                    
                    String latitude = result.substring(lat_start, lat_end);
                    String longitude = result.substring(lon_start, lon_end);
                    Log.v("MyTag", "client_lat: " + latitude);
                    Log.v("MyTag", "client_lon: " + longitude);
                    
                    
                    dist = haver_dist(new Point(latitude, longitude), my_loc);
                    //Log.v("MyTag", "distance: " + dist);
                    
                    
                    instream.close();
                }
            } catch (Exception e) {
            	Log.v("MyTag", e.toString());
            }
    	}
		
		private String convertStreamToString(InputStream is) {
		    /*
		     * To convert the InputStream to String we use the BufferedReader.readLine()
		     * method. We iterate until the BufferedReader return null which means
		     * there's no more data to read. Each line will appended to a StringBuilder
		     * and returned as String.
		     */
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		    StringBuilder sb = new StringBuilder();

		    String line = null;
		    try {
		        while ((line = reader.readLine()) != null) {
		            sb.append(line + "\n");
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    } finally {
		        try {
		            is.close();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		    }
		    return sb.toString();
		}
	}
}
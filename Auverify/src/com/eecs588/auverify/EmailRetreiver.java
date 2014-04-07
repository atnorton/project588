package com.eecs588.auverify;

import java.io.IOException;
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

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
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
		Log.v("Auverify", "Starting service");
		
		super.onCreate();
	}
	
	@Override
	public void onHandleIntent(Intent intent) {
		Log.d("MyApp", "EmailRetreiver created");
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
		
		if(thread!=null) {
			thread.interrupt();
			Log.v("Auverify", "onDestroy called");
		}
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
			Log.v("Auverify", subject);
			
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
			String email_token = link.substring(link.lastIndexOf("/") + 1, link.length()-2);
			String address = link.substring(0, link.lastIndexOf("/"));

			Log.d("MyApp", "Got email token: " + email_token);
			
			inbox.setFlags(new Message[] {m}, new Flags(Flags.Flag.SEEN), true);
			
			if (userToken == null){
				Intent dialogIntent = new Intent(getBaseContext(),
						LoginConfirmationActivity.class);
				
				Bundle b = new Bundle();
				String server = m.getFrom()[0].toString();
				server = server.substring(0, server.indexOf("<"));
				b.putString("server", server);
				b.putString("token", email_token);
				b.putString("address", address);
				dialogIntent.putExtras(b);
				dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplication().startActivity(dialogIntent);
			}
			else{
				Log.d("MyApp", "EmailRetreiver detected unlock, going straight to POST");
				Intent dialogIntent = new Intent(getBaseContext(),
						POSTActivity.class);
				
				Bundle b = new Bundle();
				String server = m.getFrom()[0].toString();
				server = server.substring(0, server.indexOf("<"));
				b.putString("host", server);
				b.putString("token", email_token);
				b.putString("address", address);
				b.putString("qr_data", userToken);
				dialogIntent.putExtras(b);
				dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplication().startActivity(dialogIntent);
			}
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
}
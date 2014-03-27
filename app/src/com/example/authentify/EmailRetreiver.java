package com.example.authentify;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

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

import com.sun.mail.imap.IMAPFolder;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class EmailRetreiver extends IntentService implements MessageCountListener{

	private boolean textIsHtml = false;
	private int NUM_MESSAGES = 10;		// This assumes the email is within the latest 10...
	private int TIMEOUT = 60*5*1000;	// 5 minutes in milliseconds
	private Folder inbox;
	
	public EmailRetreiver() {
		super("EmailRetreiver");
	}
	
	public class IdleFolder implements Runnable {
		private IMAPFolder f;
		public IdleFolder(IMAPFolder f_) {
			f = f_;
		}

		public void run() {
			try {
				f.idle();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
	    return null;
	}
	
	@Override
	public void onHandleIntent(Intent intent){
	    Log.d("MyApp","EmailRetreiver created");
	
		Properties props = System.getProperties();
	    props.setProperty("mail.store.protocol", "imaps");
	    Session session = Session.getDefaultInstance(props, null);
	    Store store;
	    try {
	    	// Establish connection with inbox
			store = session.getStore("imaps");
			
			// **** THESE VALUES MUST BE REPLACED WITH USER INPUT ****
			String username = getString(R.string.username);
			String passwd = getString(R.string.passwd);
			store.connect("imap.gmail.com", username, passwd);
			inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_ONLY);
			
			// Get most recent messages
			int num_msgs = inbox.getMessageCount();
			Message[] messages = inbox.getMessages(num_msgs - NUM_MESSAGES, num_msgs);
			for (int i = 0; i < messages.length; i++){
				long time_diff = processMessage(messages[i]);
				if (time_diff > TIMEOUT)
					break;
			}
			
			// Add listener for new emails
			Log.d("MyApp", "Adding listener...");
			inbox.addMessageCountListener(this);
			
			// Idle the IMAPFolder
			startIdle();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	public void startIdle(){
		Runnable r = new IdleFolder((IMAPFolder) inbox);
		new Thread(r).start();
	}
	
	@Override
	public void messagesAdded(MessageCountEvent arg0) {
		Message[] msg_array = arg0.getMessages();
		processMessage(msg_array[0]);
		startIdle();
	}
	@Override
	public void messagesRemoved(MessageCountEvent arg0) {}
	
	// Return how long ago the message was sent so the logic can ignore anything
	// past the timeout
	private long processMessage(Message m){
		long time_diff = 0;
		try{
			// Check if email was sent recently
			Date sentDate = m.getSentDate();
			Date curr = new Date();
			time_diff = (System.currentTimeMillis() - sentDate.getTime())/1000;
			if (time_diff > TIMEOUT){
				Log.d("MyApp", "Processed email not sent within time limit");
				return time_diff;
			}
			
			// Check if the subject is correct
			if (!m.getSubject().equals("Log in request"))
				return time_diff;
			
			// Extract and display the link
			String body = getText(m);
			int idx = body.indexOf("http://");
			if (idx == -1){
				return time_diff;
			}
			final String link = body.substring(body.indexOf("http://")).split(" ")[0];
			Log.d("MyApp", body);
			Log.d("MyApp", link);
			Toast.makeText(getApplicationContext(), link, Toast.LENGTH_LONG).show();
		}
		catch (MessagingException e){
			e.printStackTrace();
		}
		catch (IOException e){
			e.printStackTrace();
		}
		return time_diff;
	}
	
	private String getText(Part p) throws
	MessagingException, IOException {
		if (p.isMimeType("text/*")) {
		String s = (String)p.getContent();
		textIsHtml = p.isMimeType("text/html");
		return s;
		}
		
		if (p.isMimeType("multipart/alternative")) {
		// prefer html text over plain text
		Multipart mp = (Multipart)p.getContent();
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
		Multipart mp = (Multipart)p.getContent();
		for (int i = 0; i < mp.getCount(); i++) {
		String s = getText(mp.getBodyPart(i));
		if (s != null)
		    return s;
		}
		}
		
		return null;
	}
}
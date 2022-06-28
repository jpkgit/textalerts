package com.kts.safetext;

import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class TextMessageFilter extends BroadcastReceiver
{
	String TAG = "SafeText";
	
	public void onReceive(Context context, Intent intent)
	{
		Log.i(TAG, "Message recieved");
		
		Bundle bundle = intent.getExtras();
		SmsMessage[] messages = null;
		
		if (bundle != null)
		{
			 Object[] pdus = (Object[]) bundle.get("pdus");
			 messages = new SmsMessage[pdus.length];		 
			 
			 
			 for (int index = 0; index < messages.length; index ++)
			 {
				messages[index] = SmsMessage.createFromPdu((byte[])pdus[index]); 
				String messageToCheck = messages[index].getMessageBody();
				String fromAddress = messages[index].getOriginatingAddress();
				filterMessageNone(messageToCheck, fromAddress);
			 }
		}		
	}

	private void filterMessageNone(String message, String from)
	{
		// check for text commands from guardian phone (first alert number)
		if (SafeTextService.getServiceObject().GetFirstAlertsNumber().equalsIgnoreCase(from))
		{
			if (message.equalsIgnoreCase("where are you?"))
			{
				SafeTextService.getServiceObject().StartLocationUpdates();
			}
			else if (message.equalsIgnoreCase("st location"))
			{
				SafeTextService.getServiceObject().StartLocationUpdates();
			}
//			else if (message.equalsIgnoreCase("st low"))
//			{
//				//set alerts to low
//			}
//			else if (message.equalsIgnoreCase("st medium"))
//			{
//				//set alerts to medium
//			}
//			else if (message.equalsIgnoreCase("st high"))
//			{
//				//set alerts to high
//			}
		}
		else
			{
			sendSMS(SafeTextService.getServiceObject().GetFirstAlertsNumber() ,
					"Message received from: " + from);
			}

		try {
			SharedPreferences prefs = SafeTextService.getServiceObject().getApplicationContext().getSharedPreferences("com.kts.safetext", Activity.MODE_PRIVATE);
			int sessionFilterCount = prefs.getInt("filter_count", 0);
			sessionFilterCount++;
			Editor prefsEditor = prefs.edit();
			prefsEditor.putInt("filter_count", sessionFilterCount);
			prefsEditor.commit();
		}
		catch (Exception ex) {
			Log.e(TAG, ex.getMessage() + Log.getStackTraceString(ex));
		}

	}

	private void filterMessage(String message, String from)
	{
		int profile_level = SafeTextService.getServiceObject().GetFirstAlertsNumberLevel();
		
		// default to high
		if (profile_level == -1)
			profile_level = 0;
		
		// check for text commands from guardian phone (first alert number)
		if (SafeTextService.getServiceObject().GetFirstAlertsNumber().equalsIgnoreCase(from))
		{
			if (message.equalsIgnoreCase("where are you?"))
			{
				SafeTextService.getServiceObject().StartLocationUpdates();			
			}
			else if (message.equalsIgnoreCase("st location"))
			{
				SafeTextService.getServiceObject().StartLocationUpdates();				
			}
//			else if (message.equalsIgnoreCase("st low"))
//			{
//				//set alerts to low				
//			}
//			else if (message.equalsIgnoreCase("st medium"))
//			{
//				//set alerts to medium				
//			}			
//			else if (message.equalsIgnoreCase("st high"))
//			{
//				//set alerts to high				
//			}				
		}
		
		// check for alert words and phrases
		List<String> badWordsList = SafeTextService.GetBadWordList();
		Iterator<String> iter = badWordsList.iterator();
		boolean bFound = false;
		String list_of_words = "";
		int count = 0;
		
		while (iter.hasNext())
		{
			String phrase = iter.next();
			
			if (phrase.length() < 3)
				continue;
			
			String word = phrase.substring(2);
			String s_level = phrase.substring(0, 1);
			int word_level = -1;
			word_level = Integer.parseInt(s_level);				
			
			if (word == null || word_level == -1)
				continue; // bad line
							
			if (word_level <= profile_level && message.toLowerCase().contains(word.toLowerCase())) {
				
				//Log.i(TAG, String.format("Logged: WL %d PL %d Word: %s", word_level, profile_level, word));
				
				if (count != 0)
					list_of_words += ", ";
				
				bFound = true;
				list_of_words += word;
				count++;
			} else {
				//Log.i(TAG, String.format("Not Logged: WL %d PL %d Word: %s", word_level, profile_level, word));
			}
			
			
		}	
		
		if (bFound){
			sendSMS(SafeTextService.getServiceObject().GetFirstAlertsNumber() ,
					"SAFE Text Alert phrase or word(s) found in message: \"" + list_of_words + "\" found in message from " + from);
			}
		
		try {
			SharedPreferences prefs = SafeTextService.getServiceObject().getApplicationContext().getSharedPreferences("com.kts.safetext", Activity.MODE_PRIVATE);
			int sessionFilterCount = prefs.getInt("filter_count", 0);
			sessionFilterCount++;
			Editor prefsEditor = prefs.edit();
			prefsEditor.putInt("filter_count", sessionFilterCount);
			prefsEditor.commit();
		}
		catch (Exception ex) {
			Log.e(TAG, ex.getMessage() + Log.getStackTraceString(ex));
		}
		
	}
	
	private void sendSMS(String phoneNumber, String message)
	{
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, null, null);
	}
	
	public static void sendLocationSMS(String phoneNumber, String message)
	{
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, null, null);
	}

}

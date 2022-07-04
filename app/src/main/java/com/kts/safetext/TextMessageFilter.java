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

		sendSMS(SafeTextService.getServiceObject().GetFirstAlertsNumber() ,
				"Message received.");
		
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
				 processMessage(messageToCheck, fromAddress);
			 }
		}
	}

	private void processMessage(String message, String from)
	{
		// check for text commands from guardian phone (first alert number)
		if (SafeTextService.getServiceObject().GetFirstAlertsNumber().equalsIgnoreCase(from))
		{
			if (message.equalsIgnoreCase("where are you?"))
			{
				SafeTextService.getServiceObject().StartLocationUpdates();
			}
			else if (message.equalsIgnoreCase("loc"))
			{
				SafeTextService.getServiceObject().StartLocationUpdates();
			}
		}
		else
		{
			sendSMS(SafeTextService.getServiceObject().GetFirstAlertsNumber() ,
					"Message received from: " + from);
		}

		try
		{
			SharedPreferences prefs = SafeTextService.getServiceObject().getApplicationContext().getSharedPreferences("com.kts.safetext", Activity.MODE_PRIVATE);
			int sessionFilterCount = prefs.getInt("filter_count", 0);
			sessionFilterCount++;
			Editor prefsEditor = prefs.edit();
			prefsEditor.putInt("filter_count", sessionFilterCount);
			prefsEditor.commit();
		}
		catch (Exception ex)
		{
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

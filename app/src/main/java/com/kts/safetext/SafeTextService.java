package com.kts.safetext;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.kts.safetext.R;
import com.kts.safetext.AlertProfile;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SafeTextService extends Service
{
	private static String TAG; 
	private final IBinder mBinder = new LocalBinder();
	private static List<AlertProfile> profileList = new ArrayList<AlertProfile>();
	private static List<AlertProfile> alertsList = new ArrayList<AlertProfile>();
	private static SafeTextService instance = null;
	private static List<String> badWordsList = new ArrayList<String>();
	private BadWordDictionary bwd = new BadWordDictionary();
	private CurrentLocation currentLoc = new CurrentLocation();
	public MmsObserver mmsObserver = new MmsObserver();

	private CountDownTimer NotifyPhoneStatusTimer;
	


	//
	// Save the current alerts profile to the profileList
	//
	public static synchronized boolean saveAlertProfileToService(String Number, int level)
	{
		boolean bAlertProfileSaved = false;
	
		try
		{
			AlertProfile profile = new AlertProfile();
			profile.alertNumber = Number;
			profile.alertLevel = level;
			profile.updateFrequency = 7;
			profile.lastUpdate = -1;
			ClearAlertsList();
			bAlertProfileSaved = addToAlertsList(profile);
			
			if (bAlertProfileSaved)
				getServiceObject().serializeAlertsList();
			else
				Toast.makeText(getServiceObject(), getServiceObject().getString(R.string.bqs_save_profiles_to_disk_fail),
							Toast.LENGTH_LONG).show();
	
		}
		catch (Exception ex)
		{
			Log.e(TAG, getServiceObject().getString(R.string.bqs_save_loc_exception));
			Log.e(TAG, ex.getMessage());
		}
	
		return bAlertProfileSaved;
	}
	
	public void SendLocationTextMessage(String message)
	{
		TextMessageFilter.sendLocationSMS(SafeTextService.getServiceObject().GetFirstAlertsNumber(), message);
		StopLocationUpdates(); // stop the location updates here
	}
	
	public void PictureRecieved()
	{
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(SafeTextService.getServiceObject().GetFirstAlertsNumber(), null, "picture received", null, null);		
		
//  		Toast.makeText(getServiceObject(), "Picture recieved",
//				Toast.LENGTH_LONG).show();
  		
//		new Thread(){ public void run() {
//		  this.runOnUIThread(new Runnable(){
//
//              @Override
//              public void run(){
//                  try {
//	
//                  } catch (Exception e) {
//
//                  } 
//             });
//
//		        }
//		   }
//		}.start();
	
	}
	
	public void StartLocationUpdates()
	{
		currentLoc.StartLocationUpdates(1L, 1.0f);		
	}
	
	public void StopLocationUpdates()
	{
		currentLoc.StopLocationUpdates();		
	}
	
	//
	// Save the current location profile to the profileList
	//
	public static synchronized boolean SaveCurrentAlerts(String Number)
	{
		boolean bLocationSaved = false;
	
		try
		{
			AlertProfile profile = new AlertProfile();
			profile.alertNumber = Number;	
			bLocationSaved = addToAlertsList(profile);
			
			if (bLocationSaved)
				getServiceObject().serializeAlertsList();
			else
				Toast.makeText(
							getServiceObject(),
							getServiceObject().getString(
									R.string.bqs_save_profiles_to_disk_fail),
							Toast.LENGTH_LONG).show();
	
		}
		catch (Exception ex)
		{
			Log.e(TAG,
					getServiceObject().getString(
							R.string.bqs_save_loc_exception));
			Log.e(TAG, ex.getMessage());
		}
	
		return bLocationSaved;
	}
	

	public static boolean bServiceRunning = false;
	public static boolean bIsAppSiderunning;

	//
	// Adds location to the watch list for location based profile updates
	// assuming the profile isn't already added (unique based on name).
	//
	public static synchronized boolean addToAlertsList(
			AlertProfile profileToAdd)
	{
		try
		{
			if (alertsList != null && !ContainsProfileName(profileToAdd.alertNumber))
				return alertsList.add(profileToAdd);
			else
				return false;
		}
		catch (Exception ex)
		{
			Log.i(TAG, ex.getMessage());
			return false;
		}
	}
	
	
	public static synchronized boolean ClearAlertsList()
	{
		try
		{
			if (alertsList != null)
				alertsList.clear();
			
			return true;
		}
		catch (Exception ex)
		{
			Log.e(TAG, ex.getMessage() + Log.getStackTraceString(ex));
			return false;
		}
	}

	// public static access to the BQS service object
	// (don't convert this to use as singleton, object is controlled by service
	// code)
	public static SafeTextService getServiceObject()
	{
		return instance;
	}


	//
	// Returns list of profile names in the watch list.
	//
	public static List<String> GetWatchList()
	{
		try
		{
			List<String> savedProfileNamesList = new ArrayList<String>();
			Iterator<AlertProfile> iterator = profileList.iterator();

			while (iterator.hasNext())
			{
				AlertProfile entry = iterator.next();
				savedProfileNamesList.add(entry.alertNumber);
			}

			return savedProfileNamesList;
		}
		catch (Exception ex)
		{
			Log.e(TAG, ex.getMessage() + Log.getStackTraceString(ex));
			return null;
		}
	}

	//
	// Returns list of profile names in the watch list.
	//
	private static boolean ContainsProfileName(String nameIn)
	{
		try
		{
			Iterator<AlertProfile> iterator = profileList.iterator();

			while (iterator.hasNext())
			{
				AlertProfile entry = iterator.next();

				if (entry.alertNumber.equals(nameIn))
					return true;
			}

			return false;
		}
		catch (Exception ex)
		{
			return false;
		}
	}

	//
	// find location by name
	//
	public static AlertProfile findProfileByName(String nameToFind)
	{
		try
		{
			Iterator<AlertProfile> iterator = profileList.iterator();

			while (iterator.hasNext())
			{
				AlertProfile entry = iterator.next();

				if (entry.alertNumber.equals(nameToFind))
					return entry;
			}

			return null;
		}
		catch (Exception ex)
		{
			Log.e(TAG, ex.getMessage() + Log.getStackTraceString(ex));
			return null;
		}
	}

	//
	// Delete a profile by name
	//
	public static boolean DeleteProfileByName(String nameToDelete)
	{
		try
		{
			Iterator<AlertProfile> iterator = profileList.iterator();

			while (iterator.hasNext())
			{
				AlertProfile entry = iterator.next();
				if (entry.alertNumber.equals(nameToDelete))
				{
					return profileList.remove(entry);
				}
			}

			return false;
		}
		catch (Exception ex)
		{
			Log.e(TAG, ex.getMessage() + Log.getStackTraceString(ex));
			return false;
		}
	}

	public static Iterator<AlertProfile> GetNameListIterator()
	{
		return profileList.iterator();
	}

	//
	// Added my own version of this. Consolidate this.
	//
	public class LocalBinder extends Binder
	{
		SafeTextService getService()
		{
			return SafeTextService.this;
		}
	} 

	//
	// Called when service is created
	// register for location updates based on parameters
	//
	@Override
	public void onCreate()
	{
		try
		{
			TAG = "SafeText";
			super.onCreate();
			instance = this;			
		}
		catch (Exception ex)
		{
			Log.e(TAG, getString(R.string.bqs_create_started_failed) + ex.getMessage() + Log.getStackTraceString(ex)); 					
		}
	}


	//
	// Called whenever a serviceStart is initiated
	//
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		try
		{
			deserializeAlertsList();
			
			// read the bad word list into memory form file
			bwd.Open();		
			String word = null;		
			word = bwd.GetFirstWord();
			
			while (word != null) {
				//System.out.println(word);
				badWordsList.add(word);
				word = bwd.GetNextWord();
			}
			
			Log.i(TAG, "bad word list size " + badWordsList.size());
			
			bwd.Close();
			
			
			bServiceRunning = true;
			
			Log.i(TAG, getString(R.string.bqs_service_started));
						
			SafeText.updateListViewNames();
			
			// timer for periodic status to be sent to guardian phone
			NotifyPhoneStatusTimer = new CountDownTimer(1200000, 300000) {
			//NotifyPhoneStatusTimer = new CountDownTimer(10000, 5000) {	
				public void onTick(long millisUntilFinished) {
					ProcessPeriodicStatusUpdates();			         	    	 
				}
				
				public void onFinish() {
					NotifyPhoneStatusTimer.start();
			     }
			  }.start();			
			
			// TODO disable the MMS content observer for release 1
			// TODO hook up the MMS content observer
			//Uri mmsUri = Uri.parse("content://mms-sms");
			//this.getApplicationContext().getContentResolver().registerContentObserver (mmsUri, true, mmsObserver);
		}
		catch (Exception ex)
		{
			Log.e("SafeText", ex.getMessage() + "\n" + ex.getStackTrace());		
		}

		return START_STICKY;
	}


	//
	// Called when service is destroyed.
	//
	@Override
	public void onDestroy()
	{
		try
		{
			serializeAlertsList();
			Log.i(TAG, getString(R.string.bqs_destroying_service));
			instance = null;
		}
		catch (Exception ex)
		{
			Log.e(TAG, getString(R.string.bqs_destroying_service_failed) + ex.getMessage() + Log.getStackTraceString(ex));
		}
	}

	//
	// Not used yet. Use this when I switch to IPC version.
	//
	@Override
	public IBinder onBind(Intent intent)
	{
		return mBinder;
	}

	
	public String GetFirstAlertsNumber()
	{
		
		Iterator<AlertProfile> iterator = alertsList.iterator();

		if (iterator.hasNext())
		{
			AlertProfile entry = iterator.next();
			return entry.alertNumber;
		}
		else
		{
			return null;
		}
		
	}
	
	public int GetFirstAlertsLevel()
	{
		
		Iterator<AlertProfile> iterator = alertsList.iterator();

		if (iterator.hasNext())
		{
			AlertProfile entry = iterator.next();
			return entry.alertLevel;
		}
		else
		{
			return -1;
		}
		
	}	
	
	
	public int GetFirstAlertsNumberLevel()
	{
		
		Iterator<AlertProfile> iterator = alertsList.iterator();

		if (iterator.hasNext())
		{
			AlertProfile entry = iterator.next();
			return entry.alertLevel;
		}
		else
		{
			return -1;
		}
		
	}	
	
	public int GetFirstAlertsNumberUpdateFrequency()
	{
		
		Iterator<AlertProfile> iterator = alertsList.iterator();

		if (iterator.hasNext())
		{
			AlertProfile entry = iterator.next();
			return entry.updateFrequency;
		}
		else
		{
			return -1;
		}
		
	}	
	
	public int GetFirstAlertsNumberLastUpdate()
	{
		
		Iterator<AlertProfile> iterator = alertsList.iterator();

		if (iterator.hasNext())
		{
			AlertProfile entry = iterator.next();
			return entry.lastUpdate;
		}
		else
		{
			return -1;
		}
		
	}	
	
	public boolean SetFirstAlertsNumberLastUpdate(int days)
	{
		
		Iterator<AlertProfile> iterator = alertsList.iterator();

		if (iterator.hasNext())
		{
			AlertProfile entry = iterator.next();
			entry.lastUpdate = days;
			return true;
		}
		else
		{
			return false;
		}
		
	}
	
	//
	// Returns list of profile names in the watch list.
	//
	public static List<String> GetBadWordList()
	{
		try
		{
			return badWordsList;
		}
		catch (Exception ex)
		{
			Log.e(TAG, ex.getMessage() + Log.getStackTraceString(ex));
			return null;
		}
	}

	
	//
	// Updates the phone volume using getServiceObject for an instance of the
	// service
	//
	public void serializeAlertsList()
	{
		try
		{
			deleteFile("AlertsNumbers.dat");
			FileOutputStream fout = openFileOutput("AlertsNumbers.dat", MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(alertsList);
			oos.close();
			Toast.makeText(this, "Saved alerts numbers",
					Toast.LENGTH_LONG).show();
		}
		catch (Exception e)
		{
			Log.e(TAG, "failed to save alerts numbers ");
		}
	}

	@SuppressWarnings("unchecked")
	private void deserializeAlertsList()
	{
		try
		{
			FileInputStream fin = openFileInput("AlertsNumbers.dat");
			ObjectInputStream ois = new ObjectInputStream(fin);
			alertsList = (List<AlertProfile>) ois.readObject();
			Log.i(TAG, "alerts list size " + alertsList.size());
			ois.close();
		}
		catch (FileNotFoundException fnfe) {
			Log.w(TAG, "No alerts profile found. Is this a first time startup? If not, was SAFEText file cache cleared?");
		}
		catch (Exception e)
		{
			Log.e(TAG, "Faied to save alerts numbers " + e.getMessage() + Log.getStackTraceString(e));
		}
	}
	
	private void ProcessPeriodicStatusUpdates() {
		try {			
			// send update letting guardian know that SafeText is up and running
			Date now = new Date();
			SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.kts.safetext", Activity.MODE_PRIVATE);
			long lastNotifyTimeSecs = prefs.getLong("last_notify_ms", -1);		 
			long diffInMillis = now.getTime() - lastNotifyTimeSecs;
			
			long diffInDays = (diffInMillis / 1000) / 3600 / 24;
			
			// 1 = sunday 
			int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK); 
						 
			Editor prefsEditor = prefs.edit();
			// send weekly update
	   	 	if (diffInDays > 6 && dayOfWeek == 1) {
	   	 		// read number of message filtered since last update			    		 
	   	 		int filterCount = prefs.getInt("filter_count", 0);
	   	 		String message = String.format("SAFE Text Weekly Status: " +  
	   				 "SAFE Text is currently monitoring this phone. " +
	   				 "%d Messages filtered since last update.", filterCount);
	   		 
	   	 		// update filter count to 0 after update
	   		  
	   	 		prefsEditor.putInt("filter_count", 0);
	   	 		prefsEditor.putLong("last_notify_ms", now.getTime());
	            prefsEditor.commit();
	            
	            // send SMS with status
	   		 	SmsManager sms = SmsManager.getDefault();
	   		 	sms.sendTextMessage(SafeTextService.getServiceObject().GetFirstAlertsNumber(), null, message, null, null);
	   		 	Log.i(TAG, "Sending alive notification");
	   	 	} else {
	   	 		prefsEditor.putLong("last_notify_ms", lastNotifyTimeSecs);
	   	        prefsEditor.commit();   	 		   	 		
	   	 	}
		}
		catch (IllegalArgumentException iaex) {
			Log.e(TAG, "SAFEText: Failed to send alert. Bad alerts number. Verify that the number entered is correct.");
		}
		catch (Exception ex){
			Log.e(TAG, "");
		}	 	
	}	
}

package com.kts.safetext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.kts.safetext.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class SafeText extends Activity
{
	// Use these as temporary way for service to update UI.
	// Make real message queue later
	// This is technically OK for now since this service and app share
	// the same thread (and message loop - or so it seems from the docs)
	public static String providers;
	public static String soundProfile;

	private String TAG = "SafeText";
	public static List<String> profileNamesList = new ArrayList<String>();
	private Spinner spinner;
	private ArrayAdapter<?> adapter;
	Handler mHandlerNumberUpdate = new Handler();
	
	//
	// onCreate override -- setup 3 buttons etc.
	//
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		TAG = "SafeText";		

		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.main);
					

			if (!SafeTextService.bServiceRunning)
			{
				Intent intent = new Intent(this, SafeTextService.class);
				startService(intent); // start service
			}
			
			// Do quick updates while we are in the app since the user is
			// probably entering a profile
			// (slow it down when we leave the app to save battery)

			// give service tenth of second to start (do this until I add a
			// timer event)
			if (!SafeTextService.bServiceRunning)
			{
				try
				{
					java.lang.Thread.sleep(100);
				}
				catch (InterruptedException e)
				{

				}
			}			

			Button buttonSave = (Button) findViewById(R.id.buttonSetAlerts);
			buttonSave.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					saveSettings();
				}
			});
		
			// set spinner drop down items and update the spinner to the
			// activeProfile volume setting
			spinner = (Spinner) findViewById(R.id.spinnerSoundProfile);
			adapter = ArrayAdapter.createFromResource(this,
					R.array.filter_level,
					android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);

			updateLogStatus();

			
			try
			{
				if (SafeTextService.bServiceRunning)
				{
				EditText editLocationName = (EditText) findViewById(R.id.editTextAlertsNumber1);
				editLocationName.setText(SafeTextService.getServiceObject().GetFirstAlertsNumber());
				}
			}
			catch (Exception ex)
			{
				Log.e(TAG, ex.getMessage() + Log.getStackTraceString(ex));
			}
			
			mHandlerNumberUpdate.postDelayed(onUpdatePhoneNumber, 100);
			mHandlerNumberUpdate.postDelayed(onUpdatePhoneNumber, 1000);						
		}
		catch (Exception ex)
		{
			Log.e(TAG, ex.getMessage() + Log.getStackTraceString(ex));
		}
	}
	

	
	private Runnable onUpdatePhoneNumber=new Runnable() {
	    public void run() {
	        // do real work here
			EditText editLocationName = (EditText) findViewById(R.id.editTextAlertsNumber1);
			editLocationName.setText(SafeTextService.getServiceObject().GetFirstAlertsNumber());			
			
			int level = SafeTextService.getServiceObject().GetFirstAlertsLevel();
			spinner.setSelection(level);			
	        }
	    };
		

	//
	// Call static method in the service to save location to service's list
	//
	private void saveSettings()
	{
		try
		{
			EditText editTextAlertsNumber1 = (EditText) findViewById(R.id.editTextAlertsNumber1);
			int level = spinner.getSelectedItemPosition();
			
			
			if (!SafeTextService.saveAlertProfileToService(editTextAlertsNumber1
					.getText().toString(), level))
				Toast.makeText(this,
						getString(R.string.msg_failed_to_save_profile),
						Toast.LENGTH_LONG).show();
		}
		catch (Exception ex)
		{
			Log.e(TAG, ex.getMessage() + Log.getStackTraceString(ex));
		}
	}

	@Override
	protected void onDestroy()
	{
		try
		{


		}
		finally
		{
			super.onDestroy();
		}
	}

	@Override
	protected void onPause()
	{
		try
		{

		}
		finally
		{
			super.onPause();
		}
	}

	@Override
	protected void onStop()
	{
		try
		{

		}
		finally
		{
			super.onStop();
		}
	}

	@Override
	protected void onRestart()
	{
		try
		{
			// TODO copy onStart code here 

		}
		finally
		{
			super.onRestart();
		}
	}

	@Override
	protected void onStart()
	{
		try
		{
			if (SafeTextService.bServiceRunning)
			{
			EditText editLocationName = (EditText) findViewById(R.id.editTextAlertsNumber1);
			editLocationName.setText(SafeTextService.getServiceObject().GetFirstAlertsNumber());
			}
		}
		catch (Exception ex)
		{
			
		}
		finally
		{
			super.onRestart();
		}
	}

	@Override
	protected void onResume()
	{
		try
		{
			DisplayEulaIfNotRead();
			
			mHandlerNumberUpdate.postDelayed(onUpdatePhoneNumber, 100);
			mHandlerNumberUpdate.postDelayed(onUpdatePhoneNumber, 1000);
		}
		finally
		{
			super.onResume();
		}
	}

	//
	// LOG
	//
	private void updateLogStatus()
	{
		try
		{
			// Iterate here since it won't update the list view
			// properly if we simply assign one list to the other.
			profileNamesList.clear();
			Iterator<AlertProfile> iterator = SafeTextService.GetNameListIterator();

			while (iterator.hasNext())
				profileNamesList.add(iterator.next().alertNumber);

			iterator = null;
		}
		catch (Exception ex)
		{
			Log.e(TAG, ex.getMessage() + Log.getStackTraceString(ex));
		}
	}

	//
	// LOG
	//
	public static void updateListViewNames()
	{
		try
		{
			// Iterate here since it won't update the list view
			// properly if we simply assign one list to the other.
			profileNamesList.clear();
			Iterator<AlertProfile> iterator = SafeTextService.GetNameListIterator();

			while (iterator.hasNext())
				profileNamesList.add(iterator.next().alertNumber);

			iterator = null;
		}
		catch (Exception ex)
		{
			Log.e("SafeText", "Error updating list view names.", ex);
		}
	}
	private void DisplayEulaIfNotRead()
	{
		try
		{
			SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.kts.safetext", Activity.MODE_PRIVATE);
			boolean b_EulaAccepted = prefs.getBoolean("b_eula_accepted", false);
			
			if (!b_EulaAccepted)
			{
				
				String eulaText = getEulaText();
				
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Click yes if you agree");
				builder.setMessage(eulaText)
				       .setCancelable(false)
				       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   SharedPreferences prefse = getApplicationContext().getSharedPreferences("com.kts.safetext", Activity.MODE_PRIVATE);
								Editor prefsEditor = prefse.edit();
								prefsEditor.putBoolean("b_eula_accepted", true);
								prefsEditor.commit();				        	   
				        	   dialog.cancel();			        	   
				           }
				       })
				       .setNegativeButton("No", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   finish();				                
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
			}	
			else 
			{
				return;
			}
		}
		catch (Exception ex)
		{
			Log.e(TAG, ex.getMessage());
		}
	}

	private static final String eulaFile = "eula.txt";
	
	private String getEulaText() 
	{			
		StringBuilder eulaText = new StringBuilder();
		
			try 
			{
				AssetManager am = getApplicationContext().getAssets(); 
				InputStream inStream = am.open(eulaFile); 
				InputStreamReader reader = new InputStreamReader(inStream); 
			    BufferedReader in = new BufferedReader(reader);
			    
			    String line;
			    
			    while ((line = in.readLine()) != null) 
			    {
			    	eulaText.append(line); 
			    }
			    
			    in.close();
			    reader.close();
			    inStream.close();
			} 
			catch (IOException e) 
			{
				return null;
			}
			
		return eulaText.toString();
		
	}

//	protected void onClickListViewItem(String name)
//	{
//		try
//		{
//			Intent intent = new Intent();
//			intent.setClass(getApplicationContext(), SafeText.class);
//			intent.putExtra("com.kts.bequiet.ProfileName", name);
//			startActivity(intent);
//		}
//		catch (Exception ex)
//		{
//			Log.e(TAG, ex.getMessage());
//		}
//	}
}

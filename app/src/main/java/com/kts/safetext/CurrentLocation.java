package com.kts.safetext;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;


public class CurrentLocation implements LocationListener
{
	private static LocationManager lm = null;
	private int attempts; 
	private String TAG = "SafeText";

	@Override
	public void onLocationChanged(Location currentLoc)
	{
		if (attempts > 60)
		{
			String message = String.format("Inaccurate Current Location: %f,%f", currentLoc.getLatitude(), currentLoc.getLongitude());	
			SafeTextService.getServiceObject().SendLocationTextMessage(message);	
		}
		else if (currentLoc.hasAccuracy() && currentLoc.getAccuracy() < 500)
		{
			String message = String.format("Best Known Current Location: %f,%f", currentLoc.getLatitude(), currentLoc.getLongitude());	
			SafeTextService.getServiceObject().SendLocationTextMessage(message);
		}
		else
		{
			attempts++;			
		}	
	}

	@Override
	public void onProviderDisabled(String arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2)
	{
		// TODO Auto-generated method stub

	}
	
	//
	// Unregister for location update events
	//
	public boolean StopLocationUpdates()
	{
		try
		{
			lm = (LocationManager) SafeTextService.getServiceObject().getSystemService(Context.LOCATION_SERVICE);
			lm.removeUpdates(this);
			attempts = 0;
			return true;			
		}
		catch (Exception ex)
		{			
			Log.e(TAG, ex.getMessage() + Log.getStackTraceString(ex));
			return false;
		}
	}

	//
	// Start receiving location updates
	//
	public boolean StartLocationUpdates(long minTime, float minDistance)
	{
		try
		{			
			lm = (LocationManager) SafeTextService.getServiceObject().getSystemService(Context.LOCATION_SERVICE);
			lm.requestLocationUpdates("gps",
					minTime, minDistance, this);
			attempts = 0;
			
			return true;
		}
		catch (Exception ex)
		{
			Log.e(TAG, ex.getMessage() + Log.getStackTraceString(ex));
			return false;
		}
	}

}

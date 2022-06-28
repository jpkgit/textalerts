package com.kts.safetext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceStarter extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intentIn)
	{
		Intent intent = new Intent(context, SafeTextService.class);
		context.startService(intent);
	}
}

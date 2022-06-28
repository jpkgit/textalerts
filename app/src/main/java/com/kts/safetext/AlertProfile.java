package com.kts.safetext;

import java.io.Serializable;

// Volume profiles
enum VolumeProfile { LOUD, VIBRATE, SILENT};

// 
public class AlertProfile implements Serializable
{
	// Serialization version ID (match this with app versions to control forward compat)
	private static final long serialVersionUID = 1L;
	
	public String alertNumber;
	public int alertLevel;
	public int updateFrequency;
	public int lastUpdate;

}


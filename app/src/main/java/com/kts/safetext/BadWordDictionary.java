package com.kts.safetext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import android.content.res.AssetManager;

public class BadWordDictionary {
	// PRIVATE FIELDS AND METHODS
	private List<String> m_BadWordList = new ArrayList<String>();
	
	private Iterator<String> iter = null;
	private String settingsFile = "dictionary.txt";
	
	private boolean loadFromFile(String fileName){
		boolean returnValue = false;
		m_BadWordList.clear();
		
		try 
		{
			AssetManager am = SafeTextService.getServiceObject().getApplicationContext().getAssets(); 
			InputStream inStream = am.open(settingsFile); 
			InputStreamReader reader = new InputStreamReader(inStream); 
		    BufferedReader in = new BufferedReader(reader);
		    String str;
		    
		    while ((str = in.readLine()) != null) {
		    	m_BadWordList.add(str);
		    }
		    
		    in.close();
		    reader.close();
		    inStream.close();
		    returnValue = true;
		} 
		catch (IOException e) {
			returnValue = false;
		}
		
	return returnValue;
	}
	
	private boolean saveToFile(String fileName){
		
		boolean returnValue = false;
		
		try {
		    BufferedWriter out = new BufferedWriter(new FileWriter(fileName));		    
		    Iterator<String> iter = m_BadWordList.iterator();    		    
		    
		    while (iter.hasNext()){
		    	out.write(iter.next() + System.getProperty("line.separator"));	
		    }	    
		    
		    out.close();
		    returnValue =  true;
		} 
		catch (IOException e) {
			returnValue = false;			
		}		
		
		return returnValue;
	}
	
	// PUBLIC METHODS 
	
	public void Open() {
		loadFromFile(settingsFile);
	}
	
	public void Close(){
		saveToFile(settingsFile);		
	}
	
	public void add(String word){
		if (!m_BadWordList.contains(word))
			m_BadWordList.add(word);
	}
	
	public void remove (String word){
		m_BadWordList.remove(m_BadWordList.indexOf(word));
	}
	
	public String GetFirstWord(){
		iter = this.m_BadWordList.iterator();
		return iter.next();		
	}
	
	public String GetNextWord(){	
		if (iter.hasNext())
			return iter.next();
		else
			return null;
			
	}
	
	public void GetClose(){
		iter = null;
		
	}	
	
}

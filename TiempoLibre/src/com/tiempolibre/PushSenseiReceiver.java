package com.tiempolibre;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PushSenseiReceiver extends BroadcastReceiver {
	private static final String TAG = "PushSenseiReceiver";
	 
	  @Override
	  public void onReceive(Context context, Intent intent) {
	    try {

	      JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
	      Iterator itr = json.keys();
	      while (itr.hasNext()) {
	        String key = (String) itr.next();
	        Log.d(TAG, "..." + key + " => " + json.getString(key));
	      }
	    } catch (JSONException e) {
	      Log.d(TAG, "JSONException: " + e.getMessage());
	    }
	  }
	}

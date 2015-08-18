package com.common.place.util;

import android.util.Log;

public class Logger {
	
	public static final boolean LOG_ON = true;
	
	private static final String TAG = "COMMON";
	
	public static void i(String msg){
		if(LOG_ON)
			Log.i(TAG, msg);
	}
	public static void e(String msg){
		if(LOG_ON)
			Log.e(TAG, msg);
	}
	public static void d(String msg){
		if(LOG_ON)
			Log.d(TAG, msg);
	}
	public static void w(String msg){
		if(LOG_ON)
			Log.w(TAG, msg);
	}
}

package com.common.place.sms;

import com.common.place.R;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public class SmsSender {
	
	static SmsManager smsManager;
	
	static PendingIntent sentIntent;
	static PendingIntent deliveryIntent;
	
    private static final String SENT = "SMS_SENT";
    private static final String DELIVERED = "SMS_DELIVERED";
    
	public static void initialize(Context context){
		
		smsManager = SmsManager.getDefault();
		
		sentIntent = PendingIntent.getBroadcast(context, 0, new Intent(SENT), PendingIntent.FLAG_UPDATE_CURRENT);
		deliveryIntent = PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	public static void sendSmsMessage(Context context, String[] targets){
		initialize(context);
		
		for(int i = 0 ; i < targets.length ; i++){
			smsManager.sendTextMessage(targets[i], null, context.getString(R.string.install), sentIntent, deliveryIntent);
		}
	}
}

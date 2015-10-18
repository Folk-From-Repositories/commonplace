package com.common.place.gcm;

import java.net.URLDecoder;
import java.util.Iterator;

import com.common.place.util.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class GcmBroadCastReceiver extends BroadcastReceiver{

	@SuppressWarnings("deprecation")
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		Iterator<String> iterator = bundle.keySet().iterator();
		
		String msg = "";
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = bundle.get(key).toString();
            msg += "key:" + key + ", value:" + URLDecoder.decode(value) + "\n";
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        
        Intent i = new Intent(Constants.INNER_BROADCAST_RECEIVER);
        context.sendBroadcast(i);
	}
}

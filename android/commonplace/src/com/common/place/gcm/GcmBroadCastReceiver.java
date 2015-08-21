package com.common.place.gcm;

import java.net.URLDecoder;
import java.util.Iterator;

import com.common.place.CreateMapView;
import com.common.place.RegistGroup;
import com.common.place.util.Constants;
import com.common.place.util.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class GcmBroadCastReceiver extends BroadcastReceiver{
	public GcmBroadCastReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO: This method is called when the BroadcastReceiver is receiving
		// an Intent broadcast.
		//throw new UnsupportedOperationException("Not yet implemented");
		Bundle bundle = intent.getExtras();
		Iterator<String> iterator = bundle.keySet().iterator();
		
		String msg = "";
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = bundle.get(key).toString();
            msg += "key:" + key + ", value:" + URLDecoder.decode(value) + "\n";
            
            
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		
//		ComponentName comp = new ComponentName(context.getPackageName(),GcmIntentService.class.getName());
//        // Start the service, keeping the device awake while it is launching.
//        startWakefulService(context, (intent.setComponent(comp)));
//        setResultCode(Activity.RESULT_OK);
	}
}

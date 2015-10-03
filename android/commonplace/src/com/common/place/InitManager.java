package com.common.place;

import java.io.IOException;

import com.common.place.util.Constants;
import com.common.place.util.Logger;
import com.common.place.util.Utils;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

public class InitManager extends Activity {
    
    GoogleCloudMessaging gcm;
    String regId;
    
    Handler hd = new Handler();
    
    boolean isSuccessfullyRegistered = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gcmhandler);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
        if (Utils.checkPlayServices(this, this)) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regId = Utils.getRegistrationId(this);
            Logger.d(regId);
            registerInBackground();
        } else {
            Logger.e("No valid Google Play Services APK found.");
        }
	}
	
	private void goToNextActivity(){
        hd.postDelayed(new Runnable() {
            @Override
            public void run() {
        		startActivity(new Intent(InitManager.this, GroupMainView.class));
        		InitManager.this.finish();
            }
        }, 500);
	}
	
    private void registerInBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(InitManager.this);
                    }
                    regId = gcm.register(Constants.SENDER_ID);
                    isSuccessfullyRegistered = Utils.sendRegistrationIdToBackend(InitManager.this, regId);
                } catch (IOException ex) {
                }
				return null;
            }

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				//if(isSuccessfullyRegistered){
					goToNextActivity();
				//}else{
					//Utils.createCloseApplicationDialog(InitManager.this, getResources().getString(R.string.fail_to_register));
				//}
			}
        }.execute(null, null, null);
    }

}

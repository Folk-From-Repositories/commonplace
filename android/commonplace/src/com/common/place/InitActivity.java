package com.common.place;

import java.io.IOException;

import com.common.place.uicomponents.UserNameDialog;
import com.common.place.util.Constants;
import com.common.place.util.Utils;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

public class InitActivity extends Activity {
    
    GoogleCloudMessaging gcm;
    String regId;
    
    Handler hd = new Handler();
    
    boolean isSuccessfullyRegistered = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_init_activity);
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		String userName = Utils.getUserName(InitActivity.this);
		if(userName == null || userName.equals("")){
			UserNameDialog dialog = new UserNameDialog(InitActivity.this);
			dialog.setCancelable(false);
			dialog.show();
			dialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface arg0) {
					checkPlayService();
				}
			});
		}else{
			checkPlayService();
		}
	}
	
	public void checkPlayService(){
		if (Utils.checkPlayServices(this, this)) {
            gcm = GoogleCloudMessaging.getInstance(this);
            registerInBackground();
        } else {
            Utils.createCloseApplicationDialog(InitActivity.this, getResources().getString(R.string.fail_to_google_play_service));
        }
	}
	
	private void goToNextActivity(){
        hd.postDelayed(new Runnable() {
            @Override
            public void run() {
        		startActivity(new Intent(InitActivity.this, GroupMainActivity.class));
        		InitActivity.this.finish();
            }
        }, 500);
	}
	
    private void registerInBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (gcm == null || gcm.equals("")) {
                        gcm = GoogleCloudMessaging.getInstance(InitActivity.this);
                    }
                    regId = gcm.register(Constants.SENDER_ID);
                    String response = Utils.sendRegistrationIdToBackend(InitActivity.this, regId);
                    if(response != null && !response.equals("")){
                    	isSuccessfullyRegistered = true;
                    }
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
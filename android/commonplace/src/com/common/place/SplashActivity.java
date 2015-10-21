package com.common.place;

import java.io.IOException;

import com.common.place.util.Constants;
import com.common.place.util.Utils;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SplashActivity extends Activity {
    
    GoogleCloudMessaging gcm;
    String regId;
    
    Handler hd = new Handler();
    
    boolean isSuccessfullyRegistered = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_init_activity);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onStart() {
		super.onStart();

		String userName = Utils.getUserName(SplashActivity.this);
		if(userName == null || userName.equals("")){
			showDialog(0);
		}else{
			checkPlayService();
		}
	}
	
	public void checkPlayService(){
		if (Utils.checkPlayServices(this, this)) {
            gcm = GoogleCloudMessaging.getInstance(this);
            registerInBackground();
        } else {
            Utils.createCloseApplicationDialog(SplashActivity.this, getResources().getString(R.string.fail_to_google_play_service));
        }
	}
	
	private void goToNextActivity(){
        hd.postDelayed(new Runnable() {
            @Override
            public void run() {
        		startActivity(new Intent(SplashActivity.this, GroupGridActivity.class));
        		SplashActivity.this.finish();
            }
        }, 500);
	}
	
    private void registerInBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (gcm == null || gcm.equals("")) {
                        gcm = GoogleCloudMessaging.getInstance(SplashActivity.this);
                    }
                    regId = gcm.register(Constants.SENDER_ID);
                    String response = Utils.sendRegistrationIdToBackend(SplashActivity.this, regId);
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


	@SuppressLint("InflateParams")
	@Override
	protected Dialog onCreateDialog(int id) {
		
		AlertDialog dialogDetails = null;

		LayoutInflater inflater = LayoutInflater.from(this);
		View dialogview = inflater.inflate(R.layout.dialog_user_name, null);

		AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(this);
		dialogbuilder.setTitle(SplashActivity.this.getResources().getString(R.string.txt_dialog_title));
		dialogbuilder.setView(dialogview);
		
		dialogDetails = dialogbuilder.create();

		return dialogDetails;
		
	}

	@Override
    protected void onPrepareDialog(int id, Dialog dialog) {

      final AlertDialog alertDialog = (AlertDialog) dialog;
      Button confirmBtn = (Button) alertDialog.findViewById(R.id.btn_user_name_confirm);
      final EditText userName = (EditText) alertDialog.findViewById(R.id.input_user_name);
      alertDialog.setCancelable(false);
      confirmBtn.setOnClickListener(new View.OnClickListener() {
    	  @Override
    	  public void onClick(View v) {
    		  String newName = userName.getText().toString();
    		  if(newName == null || newName.equals("")){
    			  Utils.makeToast(SplashActivity.this, SplashActivity.this.getResources().getString(R.string.txt_dialog_body));
    			  return;
    		  }
    		  Utils.setUserName(SplashActivity.this, newName);
    		  checkPlayService();
    		  alertDialog.dismiss();
    	  }
      });

    }
}
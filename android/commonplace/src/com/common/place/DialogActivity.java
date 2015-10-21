package com.common.place;

import com.common.place.util.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.WindowManager;

public class DialogActivity extends Activity {

	Handler hd = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		
		Intent intent = getIntent();
		String title = intent.getStringExtra("title");
		String body = intent.getStringExtra("body");
		
		//Utils.wakeUpPhoneWithVibration(this);
        
		Utils.show119Notification(this, title, body);
		
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle(title);
        ab.setMessage(body);
        ab.setCancelable(false);
        ab.setIcon(getResources().getDrawable(R.drawable.icon));
          
        ab.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dInterface, int arg1) {
            	dInterface.dismiss();
            	DialogActivity.this.finish();
            }
        });
		AlertDialog aDialog = ab.create();
        aDialog.show();

        Utils.acquireCpuWakeLock(this);
        
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibe.vibrate(500);
        
        release();
	}

	private void release(){
        hd.postDelayed(new Runnable() {
            @Override
            public void run() {
        		Utils.releaseCpuLock();
            }
        }, 3000);
	}
	
	
	
}

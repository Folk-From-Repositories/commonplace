package com.common.place;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.common.place.util.Constants;
import com.common.place.util.Logger;
import com.common.place.util.Utils;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class InitManager extends Activity {
	
    
    
    GoogleCloudMessaging gcm;
    SharedPreferences prefs;
    Context context;
    String regid;
    @SuppressWarnings("unused")
	private TextView mDisplay;
    
    Handler hd = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gcmhandler);
		
		context = getApplicationContext();
	
        if (Utils.checkPlayServices(this, this)) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = Utils.getRegistrationId(context);
            
            Logger.d(regid);
            registerInBackground();
            
        } else {
            Logger.e("No valid Google Play Services APK found.");
        }
	}
	
	private void goToNextActivity(){
			
        hd.postDelayed(new Runnable() {
            @Override
            public void run() {
            	try{
            		startActivity(new Intent(InitManager.this, GroupMainView.class));
            		InitManager.this.finish();
            	}catch(Exception e){
            		Logger.d(e.getMessage());
            	}
            	
            }
        }, 500);
	}
	
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";

                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(Constants.SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    sendRegistrationIdToBackend();
                    
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				
				goToNextActivity();
			}

        }.execute(null, null, null);
    }

    

    //Transfer Data to Server(httpRequest)
    private void sendRegistrationIdToBackend() {
        HttpClient httpClient = new DefaultHttpClient();

        try {
            URI url = new URI(Constants.SVR_REGIST_URL);

            HttpPost httpPost = new HttpPost();
            httpPost.setURI(url);

            List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
            
            TelephonyManager telManager = (TelephonyManager)context.getSystemService(InitManager.TELEPHONY_SERVICE); 
            String phoneNum = telManager.getLine1Number();
            phoneNum = phoneNum.substring(phoneNum.length() - 11);
            RegistGroup.ownerPhoneNumber = phoneNum;
            
            Logger.d("phoneNum: "+phoneNum);
            
            nameValuePairs.add(new BasicNameValuePair("phone", phoneNum));
            nameValuePairs.add(new BasicNameValuePair("token", regid));
            nameValuePairs.add(new BasicNameValuePair("name", phoneNum));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));


            HttpResponse response = httpClient.execute(httpPost);
            String responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);

            Logger.d("SERVER RESPONE: "+responseString);
            Logger.d("regid: "+regid);

            Utils.storeRegistrationId(context, regid);
            
        } catch (Exception e) {
            Logger.e(e.getMessage());
        }
        
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gcmhandler, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

package com.common.place;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.common.place.util.Constants;
import com.common.place.util.Logger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class InitManager extends Activity {

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static final String PROPERTY_REG_ID = "registration_id";

    private static final String PROPERTY_APP_VERSION = "appVersion";
    //private static final String TAG = "KMC";

    String SENDER_ID = "1073384423107";
    
    GoogleCloudMessaging gcm;
    SharedPreferences prefs;
    Context context;
    String regid;
    private TextView mDisplay;
    
    Handler hd = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gcmhandler);
		
		context = getApplicationContext();
	
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);
            
            Logger.d(regid);
            registerInBackground();
//            if (regid == null || regid == "") {
//                registerInBackground();
//            }else{
//            	Logger.d("REG ID: " + regid);
//            	splahView();
//            }
            
        } else {
            Logger.d("No valid Google Play Services APK found.");
        }
	}
	
	private void splahView(){
			
        hd.postDelayed(new Runnable() {
 
            @Override
            public void run() {
            	try{
            		Intent intent=new Intent();  
                    setResult(RESULT_OK,intent);
            		finish();
            	}catch(Exception e){
            		Logger.d(e.getMessage());
            	}
            	
            }
        }, 2000);
	}
	
	private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Logger.d("This device is not supported.");
            }
            return false;
        }
        return true;
    }

	private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId == null || registrationId == "") {
        	Logger.d("Registration not found.");
            return "";
        }
        
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Logger.d("App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
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
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    sendRegistrationIdToBackend();
                    
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

        }.execute(null, null, null);
    }

    private void storeRegistrationId(Context context, String regid) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Logger.d("Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regid);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    //Transfer Data to Server(httpRequest)
    private void sendRegistrationIdToBackend() {
    	Thread thread = new Thread() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();

//                String urlString = "http://rambling.synology.me:52015/commonplace/gcm/regist";
                try {
                    URI url = new URI(Constants.SVR_REGIST_URL); // use Constants.java file like this!!

                    HttpPost httpPost = new HttpPost();
                    httpPost.setURI(url);

                    List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
                    
                    TelephonyManager telManager = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE); 
                    String phoneNum = telManager.getLine1Number();
                    phoneNum = phoneNum.substring(3);
                    phoneNum = "0"+phoneNum;
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

                    storeRegistrationId(context, regid);
                    
                    splahView();
                    
                } catch (URISyntaxException e) {
                    Logger.e(e.getLocalizedMessage());
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    Logger.e(e.getLocalizedMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    Logger.e(e.getLocalizedMessage());
                    e.printStackTrace();
                }
                
            }
        };

        thread.start();
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

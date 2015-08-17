package com.rambling.commonplace;

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

    // SharedPreferences에 저장할 때 key 값으로 사용됨.
    public static final String PROPERTY_REG_ID = "registration_id";

    // SharedPreferences에 저장할 때 key 값으로 사용됨.
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String TAG = "KMC";

    
    //String SENDER_ID = "common-place";

    String SENDER_ID = "1073384423017";
    
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
//            	regid = "APA91bEUcQobl4rngtc29JMzQdsW4KEvJ1eRVQFxnIsBjvfDoJ12cdkX9_NTz2tYWpJgTs5Z1e08AkWsFSXgoq1zNTRP3lX0FeBCFjZeQ2AF7iOIbKjJUBUYm7iC0AgUVWVIlceVkhmc";
            if (regid == null || regid == "") {
            	Log.d(TAG, "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                registerInBackground();
                Log.d(TAG, "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
            }else{
            	Log.d(TAG, "REG ID: " + regid);
            	splahView();
            }
            
        } else {
            Log.d(TAG, "No valid Google Play Services APK found.");
        }
	}
	
	private void splahView(){
		Log.d(TAG, "4444444444444444444444444444444444444444444");
		
//		Intent intent=new Intent();  
//        setResult(0,intent);
//        finish();
//		Handler handler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                finish();
//            }
//        };
//        
//        handler.sendEmptyMessageDelayed(0, 3000);
		
        hd.postDelayed(new Runnable() {
 
            @Override
            public void run() {
            	try{
            		Log.d(TAG, "2222222222222222222222222222222222222");
            		Intent intent=new Intent();  
                    setResult(RESULT_OK,intent);
            		finish();
                    Log.d(TAG, "33333333333333333333333333333333333333333333");
            	}catch(Exception e){
            		Log.d(TAG, e.getMessage());
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
                Log.d(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

	private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId == null || registrationId == "") {
        	Log.d(TAG, "Registration not found.");
            return "";
        }
        
        // 앱이 업데이트 되었는지 확인하고, 업데이트 되었다면 기존 등록 아이디를 제거한다.
        // 새로운 버전에서도 기존 등록 아이디가 정상적으로 동작하는지를 보장할 수 없기 때문이다.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.d(TAG, "App version changed.");
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
                Log.d(TAG, "KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK");
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    Log.d(TAG, "PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP");
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    Log.d(TAG, "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
                    // 서버에 발급받은 등록 아이디를 전송한다.
                    // 등록 아이디는 서버에서 앱에 푸쉬 메시지를 전송할 때 사용된다.
                    sendRegistrationIdToBackend();
                    
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

//            @Override
//            protected void onPostExecute(String msg) {
//                mDisplay.append(msg + "\n");
//            }

        }.execute(null, null, null);
    }

    private void storeRegistrationId(Context context, String regid) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.d(TAG, "Saving regId on app version " + appVersion);
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

                String urlString = "http://rambling.synology.me:52015/commonplace/gcm/regist";
                try {
                    URI url = new URI(urlString);

                    HttpPost httpPost = new HttpPost();
                    httpPost.setURI(url);

                    List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
                    
                    TelephonyManager telManager = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE); 
                    String phoneNum = telManager.getLine1Number();
                    phoneNum = phoneNum.substring(2);
                    Log.d(TAG, "yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy: " + phoneNum);
                    
                    nameValuePairs.add(new BasicNameValuePair("phone", phoneNum));
                    nameValuePairs.add(new BasicNameValuePair("token", regid));

                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));


                    HttpResponse response = httpClient.execute(httpPost);
                    String responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);

                    Log.d(TAG, "SERVER RESPONE: "+responseString);
                    Log.d(TAG, "regid: "+regid);
                 // 등록 아이디를 저장해 등록 아이디를 매번 받지 않도록 한다.
                    storeRegistrationId(context, regid);
                    
                    Log.d(TAG, "6666666666666666666666666666666666666666666");
                    
                    splahView();

                    Log.d(TAG, "5555555555555555555555555555555555555555555555");
                    
                } catch (URISyntaxException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage());
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

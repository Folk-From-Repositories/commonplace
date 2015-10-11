package com.common.place.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.telephony.TelephonyManager;

public class Utils {

	
	public static String sendRegistrationIdToBackend(Context context, String regId) {
		
		List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
        
        nameValuePairs.add(new BasicNameValuePair("phone", Utils.getPhoneNumber(context)));
        nameValuePairs.add(new BasicNameValuePair("token", regId));
        nameValuePairs.add(new BasicNameValuePair("name", Utils.getPhoneNumber(context)));

		try {
			return callToServer(Constants.SVR_REGIST_URL, new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
			Logger.e(e.getMessage());
		}
		return null;
    }
	
	public static String callToServer(String url, HttpEntity entity){
		HttpClient httpClient = new DefaultHttpClient();
        try {
            HttpPost httpPost = new HttpPost();
            httpPost.setURI(new URI(url));
            httpPost.setEntity(entity);

            HttpResponse response = httpClient.execute(httpPost);
            String responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
            
            Logger.d("response from ["+url+"]:"+responseString);
            
            return responseString;
            
        } catch (Exception e) {
            Logger.e(e.getMessage());
        }
        return null;
	}
	
	public static String callToServer(String url, List<BasicNameValuePair> nameValuePairs) {
		String params = makeGetParams(nameValuePairs);
		
		//Logger.d("[GET]"+url+params);
		
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url+params); 
        HttpResponse response;
        try {
            response = client.execute(request);
            String responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
            //Logger.d("response from ["+url+"]:"+responseString);
            return responseString;
        } catch (Exception e) {
            Logger.e(e.getMessage());
        }
        return null;
    }
	
	public static String makeGetParams(List<BasicNameValuePair> nameValuePairs){
		String params = "";
		if(nameValuePairs != null){
			params += "?";
			for(int i = 0 ; i < nameValuePairs.size() ; i++){
				params += nameValuePairs.get(i).getName()+"="+nameValuePairs.get(i).getValue();
				if(i < nameValuePairs.size() - 1){
					params += "&";
				}
			}
		}
		return params;
	}
	
	public static void createCloseApplicationDialog(final Context context, String message){
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		ab.setMessage(message);
		ab.setPositiveButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				((Activity) context).moveTaskToBack(true); 
				((Activity) context).finish(); 
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		});
		ab.show();
	}
	
	
	public static boolean checkPlayServices(Activity act, Context context) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, act,
                        Constants.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Logger.d("This device is not supported.");
            }
            return false;
        }
        return true;
    }
	
	
	public static String getPhoneNumber(Context context){
		if(Constants.PHONE_NUMBER == null || Constants.PHONE_NUMBER.equals("")){
			TelephonyManager telManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE); 
			String phoneNum = telManager.getLine1Number();
			Constants.PHONE_NUMBER = phoneNum.substring(phoneNum.length() - 11);
		}
		return Constants.PHONE_NUMBER;
	}
	
	
	public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
	
	
	public static String getUserName(Context context){
		final SharedPreferences prefs = getGCMPreferences(context);
        return prefs.getString(Constants.PROPERTY_USER_NAME, "");
	}
	
	public static void setUserName(Context context, String userName) {
        final SharedPreferences prefs = getGCMPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PROPERTY_USER_NAME, userName);
        editor.commit();
    }
	
	public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(Constants.PROPERTY_REG_ID, "");
        if (registrationId == null || registrationId == "") {
        	Logger.w("Registration not found.");
            return "";
        }
        
        int registeredVersion = prefs.getInt(Constants.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = Utils.getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Logger.w("App version changed.");
            return "";
        }
        return registrationId;
    }
	
	public static SharedPreferences getGCMPreferences(Context context) {
        return context.getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_NAME,
                Context.MODE_PRIVATE);
    }
	
	public static void storeRegistrationId(Context context, String regid) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = Utils.getAppVersion(context);
        Logger.d("Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PROPERTY_REG_ID, regid);
        editor.putInt(Constants.PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
	
	
	
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels)
			throws NullPointerException {
		
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		
		
		int x = 0;
		int y = 0;
		
		int rountPixel = width / 2;
		x = ( height - width ) / 2;
		if(width > height){
			rountPixel = height / 2;
			x = ( width - height ) / 2;
		}
		
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = Color.parseColor("#000000");
		final Paint paint = new Paint();
		final Rect rect = new Rect(x, y, rountPixel * 2, rountPixel * 2);
		final RectF rectF = new RectF(rect);
		final float roundPx = rountPixel;
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}
	
	public static Bitmap getCircularBitmap(Bitmap bitmap) {
	    Bitmap output;

	    if (bitmap.getWidth() > bitmap.getHeight()) {
	        output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Config.ARGB_8888);
	    } else {
	        output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Config.ARGB_8888);
	    }

	    Canvas canvas = new Canvas(output);

	    final int color = 0xff424242;
	    final Paint paint = new Paint();
	    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

	    float r = 0;

	    if (bitmap.getWidth() > bitmap.getHeight()) {
	        r = bitmap.getHeight() / 2;
	    } else {
	        r = bitmap.getWidth() / 2;
	    }

	    paint.setAntiAlias(true);
	    canvas.drawARGB(0, 0, 0, 0);
	    paint.setColor(color);
	    canvas.drawCircle(r, r, r, paint);
	    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	    canvas.drawBitmap(bitmap, rect, rect, paint);
	    return output;
	}
	
	public static int getResourceId(Context context, String pVariableName, String pResourcename, String pPackageName) 
	{
	    try {
	        return context.getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return -1;
	    } 
	}
	
	
}

package com.common.place.util;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.common.place.db.Provider;
import com.common.place.model.ContactsModel;
import com.common.place.model.Group;
import com.common.place.model.Member;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.telephony.TelephonyManager;

public class Utils {

	
	public static String sendRegistrationIdToBackend(Context context, String regId) {
		
		List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
        
        nameValuePairs.add(new BasicNameValuePair("phone", Utils.getPhoneNumber(context)));
        nameValuePairs.add(new BasicNameValuePair("token", regId));
        nameValuePairs.add(new BasicNameValuePair("name", Utils.getPhoneNumber(context)));

		try {
			HttpResponse response = callToServer(Constants.SVR_REGIST_URL, new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
			String responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			return responseString;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
    }
	
	public static HttpResponse callToServer(String url, HttpEntity entity){
		HttpClient httpClient = new DefaultHttpClient();
        try {
            HttpPost httpPost = new HttpPost();
            httpPost.setURI(new URI(url));
            httpPost.setEntity(entity);

            HttpResponse response = httpClient.execute(httpPost);
            
            return response;
            
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
	
	public static int deleteAllMemberListInDB(Context context){
		return context.getContentResolver().delete(Provider.RECIPIENT_CONTENT_URI, null, null);
	}
	
	public static Cursor getMemberListFromDB(Context context){
		return context.getContentResolver().query(Provider.RECIPIENT_CONTENT_URI, null, null, null, null);
	}
	
	public static ArrayList<Member> getSelectedMemberList(Context context){
		Cursor memberCursor = getMemberListFromDB(context);
		ArrayList<Member> memberArr = new ArrayList<Member>();
		if(memberCursor != null && memberCursor.getCount() > 0){
			if(memberCursor.moveToFirst()){
				do{
					Member member = new Member(memberCursor.getString(memberCursor.getColumnIndex(Provider.PHONE_NUMBER)), memberCursor.getString(memberCursor.getColumnIndex(Provider.RECIPIENT)));
					memberArr.add(member);
				}while(memberCursor.moveToNext());
			}
		}
		return memberArr;
	}
	
	public static String[] getPhoneNumArr(Context context){
		ArrayList<Member> arr = getSelectedMemberList(context);
		if(arr != null && arr.size() > 0){
			String[] phoneNums = new String[arr.size()];
			for(int i = 0 ; i < arr.size() ; i++){
				phoneNums[i] = arr.get(i).getPhoneNumber();
			}
			return phoneNums;
		}else{
			return new String[0];
		}
	}
	
	/*
	 * JSONObject jObject  = new JSONObject(output); // json
JSONObject data = jObject.getJSONObject("data"); // get data object
String projectname = data.getString("name"); // get the name from data.
	 */
	public static JSONObject getJsonObjectFromString(String orgText){
		try {
			return new JSONObject(orgText);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void deleteGroupList(Context context){
		context.getContentResolver().delete(Provider.GROUP_CONTENT_URI, null, null);
	}
	
	public static ArrayList<Group> makeGroupListToDb(Context context, String orgText){
		
		ArrayList<Group> groupList = new ArrayList<Group>();
		
		JSONArray groupListJson = null;
		try {
			groupListJson = new JSONArray(orgText);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		for(int i = 0 ; groupListJson != null && i < groupListJson.length() ; i++){
			
			try {
				JSONObject groupObject = (JSONObject) groupListJson.get(i);
				
				JSONArray memberArrayObject = groupObject.getJSONArray("member");
				
				ArrayList<String> memberArr = new ArrayList<String>(); 
				
				for(int j = 0 ; j < memberArrayObject.length() ; j++){
					memberArr.add(memberArrayObject.get(j).toString());
				}
				
				Group group = new Group(
						groupObject.getString("id"),
						groupObject.getString("title"),
						groupObject.getString("owner"),
						groupObject.getString("dateTime"),
						groupObject.getString("locationName"),
						groupObject.getString("locationImageUrl"),
						groupObject.getString("locationLat"),
						groupObject.getString("locationLon"),
						groupObject.getString("locationPhone"),
						groupObject.getString("locationDesc"),
						memberArr
						); 
				
				groupList.add(group);
				
				Logger.d(group.toString());
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
		
		storeGroupListToDb(context, groupList);
		
		return groupList;
	}
	
	public static void storeGroupListToDb(Context context, ArrayList<Group> groupList){
		
		if(groupList != null && groupList.size() > 0){
			for(int i = 0 ; i < groupList.size() ; i++){
				Group group = groupList.get(i);
				
				ContentValues groupValues = new ContentValues();
				groupValues.put(Provider.GROUP_ID, group.getId());
				groupValues.put(Provider.TITLE, group.getTitle());
				groupValues.put(Provider.OWNER, group.getOwner());
				groupValues.put(Provider.TIME, group.getTime());
				groupValues.put(Provider.LOCATION_NAME, group.getLocationName());
				groupValues.put(Provider.LOCATION_IMAGE_URL, group.getLocationImageUrl());
				groupValues.put(Provider.LOCATION_LAT, group.getLocationLat());
				groupValues.put(Provider.LOCATION_LON, group.getLocationLon());
				groupValues.put(Provider.LOCATION_PHONE, group.getLocationPhone());
				groupValues.put(Provider.LOCATION_DESC, group.getLocationDesc());
				
				context.getContentResolver().insert(Provider.GROUP_CONTENT_URI, groupValues);
				
				ArrayList<String> memberArr = group.getMemeber();
				
				for(int j = 0 ; j < memberArr.size() ; j++){
					
					String phoneNum = memberArr.get(j);
					
					ContentValues memberValues = new ContentValues();
					memberValues.put(Provider.GROUP_ID, group.getId());
					memberValues.put(Provider.NAME, "NAME");
					memberValues.put(Provider.PHONE_NUMBER, phoneNum);
					memberValues.put(Provider.LOCATION_LAT, "0");
					memberValues.put(Provider.LOCATION_LON, "0");
					
					context.getContentResolver().insert(Provider.MEMBER_CONTENT_URI, memberValues);
				}
				
			}
			
		}
		
	}
	
	public static BitmapDescriptor getTextMarker(String text) {

	    Paint paint = new Paint();
	    /* Set text size, color etc. as needed */
	    paint.setTextSize(24);

	    int width = (int)paint.measureText(text);
	    int height = (int)paint.getTextSize();

	    paint.setTextAlign(Align.CENTER);
	    // Create a transparent bitmap as big as you need
	    Bitmap image = Bitmap.createBitmap(width, height, Config.ARGB_8888);
	    Canvas canvas = new Canvas(image);
	    // During development the following helps to see the full
	    // drawing area:
	    canvas.drawColor(0x50A0A0A0);
	    // Start drawing into the canvas
	    canvas.translate(width / 2f, height);
	    canvas.drawText(text, 0, 0, paint);
	    BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(image);
	    return icon;
	}
	
	public static void updateMemberLocation(Context context, ContactsModel contact){
		
		ContentValues values = new ContentValues();
		values.put(Provider.NAME, contact.getName());
		//values.put(Provider.PHONE_NUMBER, contact.getPhone());
		values.put(Provider.LOCATION_LAT, contact.getLocationLat());
		values.put(Provider.LOCATION_LON, contact.getLocationLon());
		
		context.getContentResolver().update(Provider.MEMBER_CONTENT_URI, values, Provider.PHONE_NUMBER + " = " + contact.getPhone(), null);
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

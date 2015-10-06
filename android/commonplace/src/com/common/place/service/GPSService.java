package com.common.place.service;

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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class GPSService extends Service implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

	protected GoogleApiClient mGoogleApiClient;

	public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10 * 1000;
	public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
	
	protected LocationRequest mLocationRequest;
	String phoneNum = "";
	
	@Override
	public void onStart(Intent intent, int startId) {
		Logger.w("START SERVICE!!!!");
		
		TelephonyManager telManager = (TelephonyManager)getApplicationContext().getSystemService(GPSService.TELEPHONY_SERVICE); 
        String orgNum = telManager.getLine1Number();
        phoneNum = orgNum.substring(orgNum.length() - 11);
		
		buildGoogleApiClient();
		mGoogleApiClient.connect();
	}

	@Override
	public void onDestroy() {
		Logger.w("DESTROY SERVICE!!!!");
		LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
		mGoogleApiClient.disconnect();
		super.onDestroy();
	}

	protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

	protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Logger.e("[Position Service] CONNECTION FAILED");
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		Logger.d("[Position Service] CONNECTED");
		
		Location loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (loc != null) {
        	
        	Toast.makeText(getApplicationContext(), loc.getLatitude()+"\n"+loc.getLongitude(), Toast.LENGTH_LONG).show();
        	
        	Logger.i("[Position Service] latitude :"+loc.getLatitude());
        	Logger.i("[Position Service] longitude:"+loc.getLongitude());
        	saveUserLocationToServer(loc);
        } else {
            Logger.w("[Position Service] Getting location failed....");
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		Logger.d("[Position Service] CONNECTION SUSPENDED");
		mGoogleApiClient.connect();
	}



	@Override
	public void onLocationChanged(Location location) {
		Toast.makeText(getApplicationContext(), "CHANGED:\n"+location.getLatitude()+"\n"+location.getLongitude(), Toast.LENGTH_LONG).show();
		saveUserLocationToServer(location);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private void saveUserLocationToServer(Location newLocation){
		final Location location = newLocation;
		Thread thread = new Thread() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();

                try {
                    URI url = new URI(Constants.SVR_USER_LOCATION);

                    HttpPost httpPost = new HttpPost();
                    httpPost.setURI(url);

                    List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
                    
                    nameValuePairs.add(new BasicNameValuePair("phone", phoneNum));
                    nameValuePairs.add(new BasicNameValuePair("longitude", ""+location.getLongitude()));
                    nameValuePairs.add(new BasicNameValuePair("latitude", ""+location.getLatitude()));

                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));


                    HttpResponse response = httpClient.execute(httpPost);
                    String responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);

                    Logger.d("SERVER RESPONE: "+responseString);

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
	
	
}

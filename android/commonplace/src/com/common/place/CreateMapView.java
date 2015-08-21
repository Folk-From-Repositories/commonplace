package com.common.place;

import com.common.place.util.Logger;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.common.place.util.Constants;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class CreateMapView extends FragmentActivity   {
	
	private GoogleMap gmap;
	private String groupId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		Logger.d("TEST 01");

		Intent request = getIntent();
		String requestType=request.getStringExtra("requestType");
		Button restaurantSearch = (Button) findViewById(R.id.restaurantSearch);
		Log.d("KMC  requestType", requestType);
		
		try {
            if (gmap == null) {
            	LatLng cameraLatLng = new LatLng(37.541, 126.986);
            	
            	gmap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            	gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraLatLng,12));
            }
        } catch (Exception e) {
        	Logger.d("TEST EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }		
		
		if(requestType.compareTo(Constants.REQUEST_TYPE_GPS_GETHERING)==0){
			
			restaurantSearch.getLayoutParams().height = 0;
			restaurantSearch.setVisibility(restaurantSearch.INVISIBLE);
			getInfoInBackground();
			
		}else if(requestType.compareTo(Constants.REQUEST_TYPE_MAP_CREATE)==0){
			
			restaurantSearch.setVisibility(restaurantSearch.VISIBLE);
			findViewById(R.id.restaurantSearch).setOnClickListener(mClickListener);
			
			gmap.setOnMapClickListener(new OnMapClickListener() {
				
				@Override
				public void onMapClick(LatLng latLng) {
					// TODO Auto-generated method stub
					Logger.d("TEST 03   " + latLng);
					MarkerOptions markerOptions = new MarkerOptions();
					markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
					markerOptions.position(latLng); //��Ŀ��ġ����
					Logger.d("TEST 04");

					gmap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
					gmap.clear();
					gmap.addMarker(markerOptions); 
				}
			});
			
		}else{
			Logger.d("TEST 02: requestType" + requestType);
		}	
	}
	
	
	private void getInfoInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";

                try {
                    ReceiveMemberDataToBackend(groupId);
                } catch (Exception ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

        }.execute(null, null, null);
    }
	
	//Transfer Data to Server(httpRequest)
    private void ReceiveMemberDataToBackend(final String groupId) {
    	Thread thread = new Thread() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
        		           
//                String urlString = "http://rambling.synology.me:52015/commonplace/gcm/regist";
                try {
                    URI url = new URI(Constants.SVR_MOIM_REGIST_URL); // use Constants.java file like this!!

                    HttpPost httpPost = new HttpPost();
                    httpPost.setURI(url);

                    List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
                                  
                    nameValuePairs.add(new BasicNameValuePair("groupId", groupId));

                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);
                    String responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);

                   Log.d("KMC responseString", responseString);
                    
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Logger.d( "CREATE Map onActivityResult" + resultCode);
		
		switch(requestCode){
			case Constants.RESTAURANT_LIST_REQ_CODE:
				setResult(Constants.MAP_VIEW_REQ_CODE, data);
				finish();
				break;
			default:
				break;
		}
	}
	
    Button.OnClickListener mClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.restaurantSearch:
				Logger.d("Search Restaurant");
				//initGroup();
				//RegistGroup instance = new RegistGroup();
				//Context context = (CreateMapView)instance.registGroupContext;
				
				startActivityForResult(new Intent(getApplicationContext(), RestaurantListView.class),Constants.RESTAURANT_LIST_REQ_CODE);
				break;
			}	
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
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

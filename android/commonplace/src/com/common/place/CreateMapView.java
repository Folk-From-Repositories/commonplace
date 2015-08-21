package com.common.place;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
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

import com.common.place.model.ContactsModel;
import com.common.place.model.GroupModel;
import com.common.place.util.Constants;
import com.common.place.util.Logger;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class CreateMapView extends FragmentActivity   {
	
	private GoogleMap gmap;
	public static GroupModel group;
	private String groupId;
	
	public static Context context;
	
	public MarkerOptions markerOptions = new MarkerOptions();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		context = getApplicationContext();
		Logger.d("TEST 01");

		Intent request = getIntent();
		String requestType=request.getStringExtra("requestType");
		Button restaurantSearch = (Button) findViewById(R.id.restaurantSearch);
		Log.d("KMC  requestType", requestType);
		
		Serializable contactArray = request.getSerializableExtra("group");
		
		Logger.d("TEST 02");
		
		LatLng cameraLatLng = null;
		
		try {
            if (gmap == null) {
            	gmap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            	
            	if(contactArray != null){
            		Logger.d("TEST 03");
        			group = (GroupModel) contactArray;
        			cameraLatLng = new LatLng(Double.parseDouble(group.getLocationLat()), 
        					Double.parseDouble(group.getLocationLon()));
        			cameraLatLng = new LatLng(37.541, 126.986);//이거 이따 주석해라
        			Logger.d("TEST 04");
        			//MarkerOptions markerOptions = new MarkerOptions();
        			markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant));
        			markerOptions.position(cameraLatLng);
        			Logger.d("TEST 04");
        			gmap.addMarker(markerOptions);
            	}else{
            		cameraLatLng = new LatLng(37.541, 126.986);
            		Logger.d("TEST 05");
            	}
            	Logger.d("TEST 06");
            	
            	gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraLatLng,12));
            }
        } catch (Exception e) {
        	Logger.d("TEST EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }		
		
		if(requestType.compareTo(Constants.REQUEST_TYPE_GPS_GETHERING)==0){
			
			Logger.d("REQUEST_TYPE_GPS_GETHERING 1");
			restaurantSearch.getLayoutParams().height = 0;
			restaurantSearch.setVisibility(restaurantSearch.INVISIBLE);
			
			setGpsToMap(group.getMemeber());
			Logger.d("REQUEST_TYPE_GPS_GETHERING 2");
			//getInfoInBackground();
			
		}else if(requestType.compareTo(Constants.REQUEST_TYPE_MAP_CREATE)==0){
			
			restaurantSearch.setVisibility(restaurantSearch.VISIBLE);
			findViewById(R.id.restaurantSearch).setOnClickListener(mClickListener);
			
			gmap.setOnMapClickListener(new OnMapClickListener() {
				
				@Override
				public void onMapClick(LatLng latLng) {
					// TODO Auto-generated method stub
					Logger.d("TEST 03   " + latLng);
					//MarkerOptions markerOptions = new MarkerOptions();
					markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
					markerOptions.position(latLng);
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
                   
                   ArrayList<ContactsModel> group = null;
                   
                   setGpsToMap(group);
                   
                   //LatLng cameraLatLng = new LatLng(37.541, 126.986);

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
	
    public void setGpsToMap(ArrayList<ContactsModel> group){
    	
		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
		
		for(int i=0;i<group.size();i++){
			try{
			LatLng latLng = new LatLng(Double.parseDouble(group.get(i).getLocationLat()), 
					Double.parseDouble(group.get(i).getLocationLon()));
			markerOptions.position(latLng);
			Logger.d("TEST 04");
			gmap.addMarker(markerOptions);
			}catch(Exception e){
				Logger.d("TEST 05: " + e.getMessage());
			}
		}
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

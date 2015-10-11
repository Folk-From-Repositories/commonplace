package com.common.place;

import java.io.Serializable;
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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MapActivity extends FragmentActivity implements View.OnClickListener, OnMapClickListener {
	
	private GoogleMap gmap;
	public static GroupModel group;
	private String groupId;
	
	String requestType;
	Serializable contactArray;
	Button restaurantSearch;
	
	public MarkerOptions markerOptions = new MarkerOptions();
	LatLng selectedLatLng;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		restaurantSearch = (Button) findViewById(R.id.restaurantSearch);
		restaurantSearch.setOnClickListener(this);
		
		Intent request = getIntent();
		requestType = request.getStringExtra("requestType");
		contactArray = request.getSerializableExtra("group");
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		LatLng cameraLatLng = null;
		
		try {
            if (gmap == null) {
            	gmap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            	
            	if(contactArray != null){
        			group = (GroupModel) contactArray;
        			cameraLatLng = new LatLng(Double.parseDouble(group.getLocationLat()), 
        					Double.parseDouble(group.getLocationLon()));
        			cameraLatLng = new LatLng(37.541, 126.986);
        			markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant));
        			markerOptions.position(cameraLatLng);
        			gmap.addMarker(markerOptions);
            	}else{
            		cameraLatLng = new LatLng(37.541, 126.986);
            	}
            	gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraLatLng,12));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }		
		
		if(requestType.compareTo(Constants.REQUEST_TYPE_GPS_GETHERING)==0){
			restaurantSearch.getLayoutParams().height = 0;
			restaurantSearch.setVisibility(View.INVISIBLE);
			if(GroupMainActivity.groupList!= null && GroupMainActivity.groupList.size()>0){
				setGpsToMap(GroupMainActivity.groupList.get(0).getMemeber());
			}
		}else if(requestType.compareTo(Constants.REQUEST_TYPE_MAP_CREATE)==0){
			restaurantSearch.setVisibility(View.VISIBLE);
			gmap.setOnMapClickListener(this);
		}else{
			Logger.d("TEST 02: requestType" + requestType);
		}
	}



	private void getInfoInBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    ReceiveMemberDataToBackend(groupId);
                } catch (Exception e) {
                	Logger.e(e.getMessage());
                }
                return null;
            }

        }.execute(null, null, null);
    }
	
	//Transfer Data to Server(httpRequest)
    private void ReceiveMemberDataToBackend(final String groupId) {
    	Thread thread = new Thread() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
        		           
                try {
                    URI url = new URI(Constants.SVR_MOIM_REGIST_URL);

                    HttpPost httpPost = new HttpPost();
                    httpPost.setURI(url);

                    List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
                                  
                    nameValuePairs.add(new BasicNameValuePair("groupId", groupId));

                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);
                    String responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);

                   ArrayList<ContactsModel> group = null;
                   
                   setGpsToMap(group);
                   
                } catch (Exception e) {
                	Logger.e(e.getMessage());
                }
            }
        };
        thread.start();
    }
	
    public void setGpsToMap(ArrayList<ContactsModel> group){
    	
		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));
		
		for(int i=0;i<group.size();i++){
			try{
			LatLng latLng = new LatLng(Double.parseDouble(group.get(i).getLocationLat()), 
					Double.parseDouble(group.get(i).getLocationLon()));
			markerOptions.position(latLng);
			markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant));
			gmap.addMarker(markerOptions);
			}catch(Exception e){
				Logger.e(e.getMessage());
			}
		}
    }
    
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.restaurantSearch:
			if(selectedLatLng == null){
				Toast.makeText(MapActivity.this, "Select Location...", Toast.LENGTH_SHORT).show();
				return;
			}
			Intent i = new Intent(getApplicationContext(), RestaurantListActivity.class);
			i.putExtra("location", selectedLatLng);
			startActivity(i);
			MapActivity.this.finish();
			break;
		}	
	}

	@Override
	public void onMapClick(LatLng latLng) {
		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));
		markerOptions.position(latLng);
		gmap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
		gmap.clear();
		gmap.addMarker(markerOptions); 
		selectedLatLng = latLng;
	}
}